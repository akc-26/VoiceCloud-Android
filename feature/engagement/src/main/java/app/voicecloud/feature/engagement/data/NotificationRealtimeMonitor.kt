package app.voicecloud.feature.engagement.data

import app.voicecloud.core.realtime.RealtimeClient
import app.voicecloud.core.realtime.RealtimeConnectionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRealtimeMonitor @Inject constructor(
    private val realtimeClient: RealtimeClient,
) {
    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    val changes: SharedFlow<Unit> = _changes.asSharedFlow()
    val state: StateFlow<RealtimeConnectionState> get() = realtimeClient.state
    private var started = false

    @Synchronized
    fun start() {
        started = true
        realtimeClient.connect()
        registerListeners()
    }

    @Synchronized
    fun ensureListeners() {
        if (started) registerListeners()
    }

    private fun registerListeners() {
        listOf("notification:new", "notification:read", "notification:deleted").forEach { event ->
            realtimeClient.off(event)
            realtimeClient.on(event) { _changes.tryEmit(Unit) }
        }
    }

    @Synchronized
    fun stop() {
        if (!started) return
        listOf("notification:new", "notification:read", "notification:deleted").forEach(realtimeClient::off)
        started = false
    }
}
