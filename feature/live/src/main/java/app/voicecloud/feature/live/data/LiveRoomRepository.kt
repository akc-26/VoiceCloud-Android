package app.voicecloud.feature.live.data

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
import app.voicecloud.feature.live.model.*
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveRoomRepository @Inject constructor(private val api: LiveRoomApi) {
    suspend fun room(roomId: String) = api.room(roomId)
    suspend fun savedStatus(roomId: String) = runCatching { api.savedStatus(roomId).saved }.getOrDefault(false)

    suspend fun join(roomId: String): RtcJoinResult {
        val result = api.join(JoinRoomBody(roomId = roomId, deviceInfo = "${VoiceCloudBrand.slug}-android"))
        runCatching { api.activityJoin(roomId, ActivityJoinBody(result.role.ifBlank { "listener" })) }
        return result
    }

    suspend fun rejoin(roomId: String, previousToken: String?) = api.rejoin(RejoinRoomBody(roomId, previousToken))

    suspend fun leave(roomId: String) {
        try { api.leave(LeaveRoomBody(roomId)) } finally { runCatching { api.activityLeave(roomId) } }
    }

    suspend fun participants(roomId: String): List<RtcPresenceState> = api.participants(roomId).participants.distinctBy { it.userId }
    suspend fun raiseHand(roomId: String) = api.raiseHand(roomId, RaiseHandBody())
    suspend fun cancelRaiseHand(roomId: String) = api.cancelRaiseHand(roomId)

    suspend fun conversation(roomId: String, roomTitle: String?): RoomConversation =
        api.roomConversation(CreateRoomConversationBody(roomId = roomId, name = roomTitle))

    suspend fun messages(conversationId: String): List<RoomChatMessage> =
        api.messages(conversationId, page = 1, limit = 100).messages.distinctBy { it.id }

    suspend fun sendMessage(conversationId: String, content: String): RoomChatMessage =
        api.sendMessage(conversationId, SendRoomMessageBody(content = content.trim()))

    suspend fun reactToMessage(messageId: String, emoji: String) = api.messageReaction(messageId, MessageReactionBody(emoji))

    suspend fun setSaved(roomId: String, saved: Boolean): Boolean =
        if (saved) api.saveRoom(roomId).saved else api.unsaveRoom(roomId).saved

    suspend fun gifts(): List<GiftCatalogItem> = api.giftCatalog().filter { it.id.isNotBlank() && it.name.isNotBlank() }.distinctBy { it.id }

    suspend fun sendGift(roomId: String, receiverId: String, giftId: String) = api.sendGift(
        SendGiftBody(
            giftId = giftId,
            roomId = roomId,
            receiverId = receiverId,
            operationKey = UUID.randomUUID().toString(),
        )
    )

    fun accessIssue(error: Throwable): RoomAccessIssue {
        val message = when (error) {
            is HttpException -> runCatching { error.response()?.errorBody()?.string() }.getOrNull().orEmpty().ifBlank { error.message() }
            else -> error.message.orEmpty()
        }.ifBlank { "The room could not be joined right now." }
        val normalized = message.lowercase()
        val pair = when {
            "ticket" in normalized -> "ticket" to "Ticket required"
            "subscription" in normalized -> "subscription" to "Subscription required"
            "verified" in normalized -> "verification" to "Verification required"
            "club membership" in normalized || "community membership" in normalized -> "club" to "Community membership required"
            "invitation" in normalized || "invite" in normalized -> "invite" to "Invitation required"
            "locked" in normalized -> "locked" to "This room is locked"
            "capacity" in normalized || "full" in normalized -> "full" to "Room is full"
            "closed" in normalized -> "closed" to "Room is closed"
            "not joinable" in normalized || "offline" in normalized || "ended" in normalized -> "offline" to "Room is not live"
            else -> "unknown" to "Unable to join this room"
        }
        return RoomAccessIssue(pair.first, pair.second, sanitizeMessage(message, pair.second))
    }

    private fun sanitizeMessage(raw: String, fallback: String): String {
        val compact = raw.replace(Regex("[{}\\\"\\[\\]]"), " ").replace(Regex("\\s+"), " ").trim()
        return compact.takeIf { it.length in 4..220 } ?: fallback
    }
}
