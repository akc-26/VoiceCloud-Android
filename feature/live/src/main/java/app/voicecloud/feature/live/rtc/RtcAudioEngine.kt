package app.voicecloud.feature.live.rtc

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.atomic.AtomicLong

sealed interface RtcAudioState {
    data object Disconnected : RtcAudioState
    data object Connecting : RtcAudioState
    data object Connected : RtcAudioState
    data object Reconnecting : RtcAudioState
    data object Reconnected : RtcAudioState
    data class Failed(val message: String) : RtcAudioState
}

interface RtcAudioEngine {
    val state: StateFlow<RtcAudioState>
    val microphoneEnabled: StateFlow<Boolean>
    suspend fun connect(serverUrl: String, token: String)
    suspend fun setMicrophoneEnabled(enabled: Boolean): Boolean
    fun disconnect()
}

/**
 * Shared LiveKit room engine. Connections remain receive-only by default (`audio = false`,
 * `video = false`) so PH05 listener behavior is preserved. PH06 may explicitly publish local
 * microphone audio only after Android runtime permission and host/speaker user intent.
 */
@Singleton
class LiveKitListenerEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : RtcAudioEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow<RtcAudioState>(RtcAudioState.Disconnected)
    override val state: StateFlow<RtcAudioState> = mutableState.asStateFlow()
    private val mutableMicrophoneEnabled = MutableStateFlow(false)
    override val microphoneEnabled: StateFlow<Boolean> = mutableMicrophoneEnabled.asStateFlow()

    private val generation = AtomicLong(0L)
    private val ownershipLock = Any()
    private var room: Room? = null
    private var eventsJob: Job? = null

    override suspend fun connect(serverUrl: String, token: String) {
        require(serverUrl.isNotBlank() && token.isNotBlank()) { "Audio session is unavailable." }
        val (operation, previous) = beginConnectionOperation()
        cleanupOwned(previous)
        mutableMicrophoneEnabled.value = false
        ensureCurrent(operation)
        mutableState.value = RtcAudioState.Connecting

        val active = LiveKit.create(context)
        ensureCurrentOrRelease(operation, active)
        val eventJob = scope.launch {
            active.events.collect { event ->
                if (generation.get() != operation) return@collect
                when (event) {
                    is RoomEvent.Connected -> mutableState.value = RtcAudioState.Connected
                    is RoomEvent.Reconnecting -> mutableState.value = RtcAudioState.Reconnecting
                    is RoomEvent.Reconnected -> mutableState.value = RtcAudioState.Reconnected
                    is RoomEvent.Disconnected -> mutableState.value = RtcAudioState.Disconnected
                    is RoomEvent.FailedToConnect -> mutableState.value = RtcAudioState.Failed("Audio connection failed. Please try again.")
                    else -> Unit
                }
            }
        }
        val accepted = synchronized(ownershipLock) {
            if (generation.get() == operation) {
                room = active
                eventsJob = eventJob
                true
            } else false
        }
        if (!accepted) {
            cleanupLocal(active, eventJob)
            throw CancellationException("Audio connection superseded")
        }

        try {
            active.connect(
                serverUrl,
                token,
                ConnectOptions(autoSubscribe = true, audio = false, video = false),
            )
            ensureCurrent(operation)
            if (mutableState.value == RtcAudioState.Connecting) mutableState.value = RtcAudioState.Connected
        } catch (cancelled: CancellationException) {
            cleanupIfOwned(active, eventJob)
            throw cancelled
        } catch (error: Throwable) {
            if (generation.get() == operation) {
                mutableState.value = RtcAudioState.Failed("Audio connection failed. Please try again.")
            }
            cleanupIfOwned(active, eventJob)
            throw error
        }
    }

    override suspend fun setMicrophoneEnabled(enabled: Boolean): Boolean {
        val active = synchronized(ownershipLock) { room } ?: return false
        val changed = active.localParticipant.setMicrophoneEnabled(enabled)
        mutableMicrophoneEnabled.value = changed && enabled
        return changed
    }

    override fun disconnect() {
        val previous = synchronized(ownershipLock) {
            generation.incrementAndGet()
            takeOwnedConnectionLocked()
        }
        cleanupOwned(previous)
        mutableMicrophoneEnabled.value = false
        mutableState.value = RtcAudioState.Disconnected
    }

    private fun ensureCurrent(operation: Long) {
        if (generation.get() != operation) throw CancellationException("Audio connection superseded")
    }

    private fun ensureCurrentOrRelease(operation: Long, active: Room) {
        if (generation.get() != operation) {
            runCatching { active.disconnect() }
            runCatching { active.release() }
            throw CancellationException("Audio connection superseded")
        }
    }

    private data class OwnedConnection(val room: Room?, val job: Job?)

    private fun beginConnectionOperation(): Pair<Long, OwnedConnection> = synchronized(ownershipLock) {
        val operation = generation.incrementAndGet()
        operation to takeOwnedConnectionLocked()
    }

    private fun takeOwnedConnectionLocked(): OwnedConnection {
        val owned = OwnedConnection(room, eventsJob)
        room = null
        eventsJob = null
        return owned
    }

    private fun cleanupOwned(owned: OwnedConnection) {
        owned.job?.cancel()
        owned.room?.let { active ->
            runCatching { active.disconnect() }
            runCatching { active.release() }
        }
    }

    private fun cleanupIfOwned(active: Room, eventJob: Job) {
        synchronized(ownershipLock) {
            if (room === active) room = null
            if (eventsJob === eventJob) eventsJob = null
        }
        cleanupLocal(active, eventJob)
    }

    private fun cleanupLocal(active: Room, eventJob: Job) {
        eventJob.cancel()
        runCatching { active.disconnect() }
        runCatching { active.release() }
    }
}
