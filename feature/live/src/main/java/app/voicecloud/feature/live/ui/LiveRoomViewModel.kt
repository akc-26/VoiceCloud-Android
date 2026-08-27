package app.voicecloud.feature.live.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.realtime.RealtimeConnectionState
import app.voicecloud.feature.live.data.LiveRoomRealtimeMonitor
import app.voicecloud.feature.live.data.LiveRoomRepository
import app.voicecloud.feature.live.model.*
import app.voicecloud.feature.live.rtc.LiveKitListenerEngine
import app.voicecloud.feature.live.rtc.RtcAudioState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class LiveRoomUiState(
    val loading: Boolean = false,
    val joining: Boolean = false,
    val mutationBusy: Boolean = false,
    val room: LiveRoomDetail? = null,
    val saved: Boolean = false,
    val inRoom: Boolean = false,
    val backgrounded: Boolean = false,
    val rtcState: RtcAudioState = RtcAudioState.Disconnected,
    val realtimeState: RealtimeConnectionState = RealtimeConnectionState.Disconnected,
    val joinResult: RtcJoinResult? = null,
    val participants: List<RtcPresenceState> = emptyList(),
    val conversationId: String? = null,
    val messages: List<RoomChatMessage> = emptyList(),
    val gifts: List<GiftCatalogItem> = emptyList(),
    val handRaised: Boolean = false,
    val speakerInvitationPending: Boolean = false,
    val paused: Boolean = false,
    val ended: Boolean = false,
    val reactions: List<RoomReaction> = emptyList(),
    val accessIssue: RoomAccessIssue? = null,
    val error: String? = null,
    val notice: String? = null,
)

@HiltViewModel
class LiveRoomViewModel @Inject constructor(
    private val repository: LiveRoomRepository,
    private val rtcEngine: LiveKitListenerEngine,
    private val realtime: LiveRoomRealtimeMonitor,
) : ViewModel() {
    private val mutableState = MutableStateFlow(LiveRoomUiState())
    val state: StateFlow<LiveRoomUiState> = mutableState.asStateFlow()

    private var viewerId: String? = null
    private var viewerUsername: String? = null
    private var activeRoomId: String? = null
    private var leaving = false
    private var rejoinBusy = false
    private var presenceJoined = false
    private var joinJob: Job? = null
    private var rejoinJob: Job? = null
    private var extrasJob: Job? = null
    private var participantRefreshJob: Job? = null
    private var messageRefreshJob: Job? = null
    private var sessionGeneration = 0L
    private var reactionSequence = 0L

    init {
        viewModelScope.launch {
            rtcEngine.state.collect { rtc ->
                mutableState.value = mutableState.value.copy(rtcState = rtc)
                when (rtc) {
                    RtcAudioState.Reconnected -> refreshParticipants()
                    RtcAudioState.Disconnected -> if (mutableState.value.inRoom && !leaving && !mutableState.value.ended) scheduleRejoin()
                    else -> Unit
                }
            }
        }
        viewModelScope.launch {
            realtime.connectionState.collect { socket ->
                mutableState.value = mutableState.value.copy(realtimeState = socket)
                if (socket is RealtimeConnectionState.Authenticated && mutableState.value.inRoom) {
                    if (presenceJoined) realtime.reconnectPresence() else {
                        realtime.joinPresence(viewerUsername)
                        presenceJoined = true
                    }
                }
            }
        }
    }

    fun setViewer(id: String?, username: String?) {
        viewerId = id?.takeIf { it.isNotBlank() }
        viewerUsername = username?.takeIf { it.isNotBlank() }
    }

    fun clearMessage() {
        mutableState.value = mutableState.value.copy(error = null, notice = null, accessIssue = null)
    }

    fun loadPreview(roomId: String) {
        if (roomId.isBlank()) return
        activeRoomId = roomId
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, error = null, accessIssue = null)
            try {
                val room = repository.room(roomId)
                val saved = repository.savedStatus(roomId)
                mutableState.value = mutableState.value.copy(room = room, saved = saved, ended = room.status.equals("ended", true) || room.endedAt != null)
            } catch (error: Throwable) {
                mutableState.value = mutableState.value.copy(error = readable(error))
            } finally {
                mutableState.value = mutableState.value.copy(loading = false)
            }
        }
    }

    fun enter(roomId: String) {
        if (roomId.isBlank() || mutableState.value.joining || mutableState.value.inRoom || leaving) return
        val generation = beginSession(roomId)
        joinJob = viewModelScope.launch {
            mutableState.value = mutableState.value.copy(joining = true, error = null, accessIssue = null, ended = false)
            var joinedBackend = false
            try {
                ensureCurrentSession(generation, roomId)
                val room = mutableState.value.room?.takeIf { it.id == roomId } ?: repository.room(roomId)
                ensureCurrentSession(generation, roomId)
                val join = repository.join(roomId)
                joinedBackend = true
                ensureCurrentSession(generation, roomId)
                validateJoin(join)
                rtcEngine.connect(join.serverUrl!!.trim(), join.token)
                ensureCurrentSession(generation, roomId)
                val participants = runCatching { repository.participants(roomId) }.getOrDefault(emptyList())
                ensureCurrentSession(generation, roomId)
                val saved = repository.savedStatus(roomId)
                ensureCurrentSession(generation, roomId)
                mutableState.value = mutableState.value.copy(
                    room = room,
                    joinResult = join,
                    inRoom = true,
                    participants = participants,
                    saved = saved,
                    handRaised = join.presenceState.handRaised,
                    paused = room.status.equals("paused", true),
                )
                attachRealtime(roomId)
                loadRoomExtras(room, generation)
            } catch (cancelled: CancellationException) {
                // Leave/new-room navigation owns cleanup after invalidating this generation.
                throw cancelled
            } catch (error: Throwable) {
                if (!isCurrentSession(generation, roomId)) return@launch
                rtcEngine.disconnect()
                if (joinedBackend) runCatching { repository.leave(roomId) }
                mutableState.value = mutableState.value.copy(
                    inRoom = false,
                    accessIssue = repository.accessIssue(error),
                    error = null,
                )
            } finally {
                if (isCurrentSession(generation, roomId)) {
                    mutableState.value = mutableState.value.copy(joining = false)
                }
                if (joinJob === currentCoroutineContext()[Job]) joinJob = null
            }
        }
    }

    fun leave() {
        val roomId = activeRoomId ?: return
        if (leaving) return
        leaving = true
        val invalidatedGeneration = invalidateSession()
        cancelSessionJobs()
        mutableState.value = mutableState.value.copy(
            inRoom = false,
            joining = false,
            participants = emptyList(),
            conversationId = null,
            joinResult = null,
            handRaised = false,
            speakerInvitationPending = false,
        )
        viewModelScope.launch {
            try {
                if (presenceJoined) realtime.leavePresence()
                realtime.detach()
                rtcEngine.disconnect()
                runCatching { repository.leave(roomId) }
                presenceJoined = false
                if (sessionGeneration == invalidatedGeneration && activeRoomId == roomId) {
                    activeRoomId = null
                }
            } finally {
                leaving = false
            }
        }
    }

    fun toggleSave() = mutate {
        val roomId = activeRoomId ?: return@mutate
        val next = !mutableState.value.saved
        val saved = repository.setSaved(roomId, next)
        mutableState.value = mutableState.value.copy(saved = saved, notice = if (saved) "Room saved." else "Room removed from saved rooms.")
    }

    fun toggleHand() = mutate {
        val roomId = activeRoomId ?: return@mutate
        if (mutableState.value.handRaised) {
            repository.cancelRaiseHand(roomId)
            mutableState.value = mutableState.value.copy(handRaised = false, notice = "Hand lowered.")
        } else {
            repository.raiseHand(roomId)
            mutableState.value = mutableState.value.copy(handRaised = true, notice = "Hand raised. The room host can now review your request.")
        }
    }

    fun sendMessage(content: String) = mutate {
        val conversationId = mutableState.value.conversationId ?: return@mutate
        if (content.isBlank()) return@mutate
        val sent = repository.sendMessage(conversationId, content)
        mutableState.value = mutableState.value.copy(messages = (mutableState.value.messages + sent).distinctBy { it.id })
    }

    fun reactToMessage(messageId: String, emoji: String) = mutate {
        if (messageId.isBlank() || emoji.isBlank()) return@mutate
        repository.reactToMessage(messageId, emoji)
    }

    fun sendReaction(emoji: String) {
        if (!mutableState.value.inRoom || emoji.isBlank()) return
        realtime.sendReaction(emoji)
        addReaction(emoji, viewerId)
    }

    fun sendGift(gift: GiftCatalogItem) = mutate {
        val room = mutableState.value.room ?: return@mutate
        if (room.hostId.isBlank()) return@mutate
        repository.sendGift(room.id, room.hostId, gift.id)
        mutableState.value = mutableState.value.copy(notice = "${gift.name} sent.")
    }

    fun acceptSpeakerInvitation() {
        if (!mutableState.value.speakerInvitationPending) return
        realtime.acceptSpeakerInvitation()
        mutableState.value = mutableState.value.copy(speakerInvitationPending = false, notice = "Stage invitation accepted.")
    }

    fun rejectSpeakerInvitation() {
        if (!mutableState.value.speakerInvitationPending) return
        realtime.rejectSpeakerInvitation()
        mutableState.value = mutableState.value.copy(speakerInvitationPending = false, notice = "Stage invitation declined.")
    }

    fun onBackground() {
        mutableState.value = mutableState.value.copy(backgrounded = true)
        // Do not leave the room when the app backgrounds; listener audio/presence remains active.
    }

    fun onForeground() {
        val wasBackgrounded = mutableState.value.backgrounded
        mutableState.value = mutableState.value.copy(backgrounded = false)
        if (wasBackgrounded && mutableState.value.inRoom) {
            refreshParticipants()
            if (mutableState.value.rtcState is RtcAudioState.Disconnected) scheduleRejoin()
        }
    }

    private fun attachRealtime(roomId: String) {
        realtime.attach(roomId, ::handleRealtimeEvent)
        realtime.ensureConnected()
        if (realtime.connectionState.value is RealtimeConnectionState.Authenticated) {
            realtime.joinPresence(viewerUsername)
            presenceJoined = true
        }
    }

    private fun handleRealtimeEvent(event: String, payload: JSONObject) {
        when (event) {
            "participant_joined", "participant_left", "participant_reconnected", "presence_updated",
            "speaker_promoted", "speaker_demoted", "speaker_removed", "speaker_muted", "speaker_unmuted", "stage_updated" -> refreshParticipants()
            "reaction:broadcast", "emoji_reaction_received" -> {
                val emoji = payload.optString("emoji").takeIf { it.isNotBlank() } ?: return
                addReaction(emoji, payload.optString("userId").takeIf { it.isNotBlank() })
            }
            "hand_approved" -> if (isViewer(payload)) mutableState.value = mutableState.value.copy(handRaised = false, notice = "Your speaking request was approved.")
            "hand_rejected" -> if (isViewer(payload)) mutableState.value = mutableState.value.copy(handRaised = false, notice = "Your speaking request was not approved.")
            "speaker_invitation_sent" -> if (isViewer(payload, requireIdentity = true)) mutableState.value = mutableState.value.copy(speakerInvitationPending = true)
            "speaker_invitation_rejected" -> if (isViewer(payload)) mutableState.value = mutableState.value.copy(speakerInvitationPending = false)
            "rtc_token_refreshed" -> scheduleRejoin()
            "room.paused", "room_paused" -> mutableState.value = mutableState.value.copy(paused = true, notice = "The host paused this room.")
            "room.resumed", "room_resumed" -> mutableState.value = mutableState.value.copy(paused = false, notice = "The room is live again.")
            "room.ended", "room_ended", "rtc_session_ended" -> endFromServer()
            "gift.sent", "gift.received", "gift.animation" -> mutableState.value = mutableState.value.copy(notice = "A gift was sent in the room.")
        }
    }

    private fun isViewer(payload: JSONObject, requireIdentity: Boolean = false): Boolean {
        val target = listOf("userId", "targetUserId", "inviteeId", "participantId")
            .asSequence().map { payload.optString(it) }.firstOrNull { it.isNotBlank() }
        return if (target == null) !requireIdentity else target == viewerId
    }

    private fun addReaction(emoji: String, userId: String?) {
        reactionSequence += 1
        val next = (mutableState.value.reactions + RoomReaction(emoji, userId, reactionSequence)).takeLast(12)
        mutableState.value = mutableState.value.copy(reactions = next)
    }

    private fun loadRoomExtras(room: LiveRoomDetail, generation: Long) {
        extrasJob?.cancel()
        extrasJob = viewModelScope.launch {
            val conversation = runCatching { repository.conversation(room.id, room.title) }.getOrNull()
            if (!isCurrentSession(generation, room.id) || !mutableState.value.inRoom) return@launch
            val messages = conversation?.id?.let { runCatching { repository.messages(it) }.getOrDefault(emptyList()) }.orEmpty()
            if (!isCurrentSession(generation, room.id) || !mutableState.value.inRoom) return@launch
            val gifts = runCatching { repository.gifts() }.getOrDefault(emptyList())
            if (!isCurrentSession(generation, room.id) || !mutableState.value.inRoom) return@launch
            mutableState.value = mutableState.value.copy(conversationId = conversation?.id, messages = messages, gifts = gifts)
            if (conversation?.id != null) startMessageRefresh(conversation.id, generation, room.id)
        }
    }

    private fun startMessageRefresh(conversationId: String, generation: Long, roomId: String) {
        messageRefreshJob?.cancel()
        messageRefreshJob = viewModelScope.launch {
            while (isCurrentSession(generation, roomId) && mutableState.value.inRoom && mutableState.value.conversationId == conversationId) {
                delay(5_000)
                val refreshed = runCatching { repository.messages(conversationId) }.getOrNull() ?: continue
                if (!isCurrentSession(generation, roomId) || !mutableState.value.inRoom) break
                mutableState.value = mutableState.value.copy(messages = refreshed)
            }
        }
    }

    private fun refreshParticipants() {
        val roomId = activeRoomId ?: return
        val generation = sessionGeneration
        if (!mutableState.value.inRoom) return
        participantRefreshJob?.cancel()
        participantRefreshJob = viewModelScope.launch {
            val participants = runCatching { repository.participants(roomId) }.getOrNull() ?: return@launch
            if (!isCurrentSession(generation, roomId) || !mutableState.value.inRoom) return@launch
            mutableState.value = mutableState.value.copy(participants = participants)
        }
    }

    private fun scheduleRejoin() {
        val roomId = activeRoomId ?: return
        val prior = mutableState.value.joinResult ?: return
        val generation = sessionGeneration
        if (rejoinBusy || leaving || mutableState.value.ended || !mutableState.value.inRoom) return
        rejoinBusy = true
        rejoinJob?.cancel()
        rejoinJob = viewModelScope.launch {
            try {
                delay(700)
                ensureCurrentSession(generation, roomId)
                val refreshed = repository.rejoin(roomId, prior.token)
                ensureCurrentSession(generation, roomId)
                validateJoin(refreshed)
                rtcEngine.connect(refreshed.serverUrl!!.trim(), refreshed.token)
                ensureCurrentSession(generation, roomId)
                mutableState.value = mutableState.value.copy(joinResult = refreshed, error = null)
                realtime.reconnectPresence()
                refreshParticipants()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                if (isCurrentSession(generation, roomId) && mutableState.value.inRoom) {
                    mutableState.value = mutableState.value.copy(error = "The room audio connection was interrupted. Tap retry to reconnect.")
                }
            } finally {
                if (isCurrentSession(generation, roomId)) rejoinBusy = false
                if (rejoinJob === currentCoroutineContext()[Job]) rejoinJob = null
            }
        }
    }

    fun retryAudio() {
        if (mutableState.value.inRoom) scheduleRejoin() else activeRoomId?.let(::enter)
    }

    private fun endFromServer() {
        if (mutableState.value.ended) return
        val roomId = activeRoomId
        leaving = true
        invalidateSession()
        cancelSessionJobs()
        realtime.detach()
        rtcEngine.disconnect()
        mutableState.value = mutableState.value.copy(
            inRoom = false,
            joining = false,
            ended = true,
            paused = false,
            participants = emptyList(),
            conversationId = null,
            notice = "This room has ended.",
        )
        if (roomId != null) viewModelScope.launch {
            try { runCatching { repository.leave(roomId) } }
            finally {
                presenceJoined = false
                activeRoomId = null
                leaving = false
            }
        } else {
            activeRoomId = null
            presenceJoined = false
            leaving = false
        }
    }

    private fun beginSession(roomId: String): Long {
        sessionGeneration += 1
        joinJob?.cancel()
        rejoinJob?.cancel()
        extrasJob?.cancel()
        participantRefreshJob?.cancel()
        messageRefreshJob?.cancel()
        rejoinBusy = false
        presenceJoined = false
        activeRoomId = roomId
        return sessionGeneration
    }

    private fun invalidateSession(): Long {
        sessionGeneration += 1
        return sessionGeneration
    }

    private fun cancelSessionJobs() {
        joinJob?.cancel()
        joinJob = null
        rejoinJob?.cancel()
        rejoinJob = null
        extrasJob?.cancel()
        extrasJob = null
        participantRefreshJob?.cancel()
        participantRefreshJob = null
        messageRefreshJob?.cancel()
        messageRefreshJob = null
        rejoinBusy = false
    }

    private fun isCurrentSession(generation: Long, roomId: String): Boolean =
        sessionGeneration == generation && activeRoomId == roomId && !leaving && !mutableState.value.ended

    private fun ensureCurrentSession(generation: Long, roomId: String) {
        if (!isCurrentSession(generation, roomId)) throw CancellationException("Room session superseded")
    }

    private fun validateJoin(join: RtcJoinResult) {
        val serverUrl = join.serverUrl?.trim().orEmpty()
        if (serverUrl.isBlank() || join.token.isBlank()) throw IllegalStateException("Audio service unavailable. Please try again.")
        if (!join.provider.isNullOrBlank() && !join.provider.equals("livekit", ignoreCase = true)) {
            throw IllegalStateException("Audio service unavailable. Please try again.")
        }
    }

    private fun mutate(block: suspend () -> Unit) {
        if (mutableState.value.mutationBusy) return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(mutationBusy = true, error = null, notice = null)
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                mutableState.value = mutableState.value.copy(error = readable(error))
            } finally {
                mutableState.value = mutableState.value.copy(mutationBusy = false)
            }
        }
    }

    private fun readable(error: Throwable): String = error.message?.takeIf { it.isNotBlank() } ?: "The app could not complete this action."

    override fun onCleared() {
        invalidateSession()
        cancelSessionJobs()
        realtime.detach()
        rtcEngine.disconnect()
        super.onCleared()
    }
}
