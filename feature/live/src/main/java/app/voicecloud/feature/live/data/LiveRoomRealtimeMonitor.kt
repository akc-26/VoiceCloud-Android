package app.voicecloud.feature.live.data

import app.voicecloud.core.realtime.RealtimeClient
import app.voicecloud.core.realtime.RealtimeConnectionState
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveRoomRealtimeMonitor @Inject constructor(
    private val client: RealtimeClient,
) {
    val connectionState: StateFlow<RealtimeConnectionState> = client.state
    private var roomId: String? = null
    private var callback: ((String, JSONObject) -> Unit)? = null

    private val inboundEvents = listOf(
        "participant_joined", "participant_left", "participant_reconnected", "presence_updated",
        "reaction:broadcast", "emoji_reaction_received", "hand_raised", "hand_approved", "hand_rejected",
        "speaker_invitation_sent", "speaker_invitation_rejected", "speaker_promoted", "speaker_demoted",
        "speaker_removed", "speaker_muted", "speaker_unmuted", "speaker_queue_updated", "stage_updated",
        "rtc_session_started", "rtc_session_ended", "rtc_token_refreshed",
        "gift.sent", "gift.received", "gift.animation",
        "room.paused", "room.resumed", "room.ended", "room_paused", "room_resumed", "room_ended",
    )

    fun attach(roomId: String, onEvent: (String, JSONObject) -> Unit) {
        detach()
        this.roomId = roomId
        callback = onEvent
        inboundEvents.forEach { event ->
            client.on(event) { args ->
                val payload = args.firstOrNull() as? JSONObject ?: JSONObject()
                val eventRoomId = payload.optString("roomId").takeIf { it.isNotBlank() }
                if (eventRoomId == null || eventRoomId == this.roomId) callback?.invoke(event, payload)
            }
        }
    }

    fun ensureConnected(): Boolean = client.connect()

    fun joinPresence(username: String?) = emit("presence:join", JSONObject().apply {
        put("roomId", roomId)
        username?.takeIf { it.isNotBlank() }?.let { put("username", it) }
    })

    fun reconnectPresence() = emit("presence:reconnect", JSONObject().apply { put("roomId", roomId) })
    fun leavePresence() = emit("presence:leave", JSONObject().apply { put("roomId", roomId) })
    fun sendReaction(emoji: String) = emit("reaction:send", JSONObject().apply { put("roomId", roomId); put("emoji", emoji) })
    fun acceptSpeakerInvitation() = emit("stage:accept_invitation", JSONObject().apply { put("roomId", roomId) })
    fun rejectSpeakerInvitation() = emit("stage:reject_invitation", JSONObject().apply { put("roomId", roomId) })

    fun detach() {
        inboundEvents.forEach(client::off)
        callback = null
        roomId = null
    }

    private fun emit(event: String, payload: JSONObject) {
        if (client.state.value is RealtimeConnectionState.Authenticated) runCatching { client.emit(event, payload) }
    }
}
