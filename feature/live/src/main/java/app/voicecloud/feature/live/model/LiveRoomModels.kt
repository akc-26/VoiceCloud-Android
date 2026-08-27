package app.voicecloud.feature.live.model

data class LiveRoomDetail(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val hostId: String = "",
    val coverUrl: String? = null,
    val isLocked: Boolean = false,
    val isLive: Boolean = false,
    val status: String = "",
    val audioQuality: String? = null,
    val startedAt: String? = null,
    val endedAt: String? = null,
    val language: String = "",
    val category: String = "",
    val listenerCount: Int = 0,
    val speakerCount: Int = 0,
    val popularityScore: Double = 0.0,
    val scheduledRoomId: String? = null,
    val clubId: String? = null,
    val isPremium: Boolean = false,
    val isTicketRequired: Boolean = false,
    val isSubscriberOnly: Boolean = false,
    val isInviteOnly: Boolean = false,
    val isVerifiedOnly: Boolean = false,
    val ticketPriceAmount: Any? = null,
    val currency: String? = null,
)

data class RtcPresenceState(
    val userId: String = "",
    val roomId: String = "",
    val role: String = "listener",
    val status: String? = null,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val handRaised: Boolean = false,
    val username: String? = null,
    val deviceInfo: String? = null,
    val joinedAt: String? = null,
    val reconnectedAt: String? = null,
)

data class RtcJoinResult(
    val message: String = "",
    val roomId: String = "",
    val userId: String = "",
    val role: String = "listener",
    val token: String = "",
    val provider: String? = null,
    val appId: String? = null,
    val serverUrl: String? = null,
    val expiresAt: String = "",
    val presenceState: RtcPresenceState = RtcPresenceState(),
)

data class RtcParticipantsResult(
    val roomId: String = "",
    val totalCount: Int = 0,
    val participants: List<RtcPresenceState> = emptyList(),
)

data class JoinRoomBody(val roomId: String, val role: String = "listener", val deviceInfo: String = "android")
data class RejoinRoomBody(val roomId: String, val previousToken: String? = null)
data class LeaveRoomBody(val roomId: String)
data class RaiseHandBody(val seatIndex: Int = 1)
data class ActivityJoinBody(val role: String = "listener")

data class SavedRoomStatus(val saved: Boolean = false, val roomId: String = "", val savedAt: String? = null)

data class RoomConversation(
    val id: String = "",
    val type: String = "room",
    val name: String? = null,
    val roomId: String? = null,
)

data class ChatSender(
    val id: String? = null,
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
)

data class RoomChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val sender: ChatSender? = null,
    val type: String = "text",
    val content: String? = null,
    val createdAt: String = "",
)

data class RoomMessagePage(
    val messages: List<RoomChatMessage> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 100,
)

data class CreateRoomConversationBody(val type: String = "room", val roomId: String, val name: String? = null)
data class SendRoomMessageBody(val type: String = "text", val content: String)
data class MessageReactionBody(val emoji: String)

data class GiftCatalogItem(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val type: String = "static",
    val rarity: String = "common",
    val category: String = "Popular",
    val coinPrice: Int = 0,
    val iconUrl: String? = null,
    val animationUrl: String? = null,
    val previewUrl: String? = null,
    val isVipOnly: Boolean = false,
    val isHostExclusive: Boolean = false,
)

data class SendGiftBody(
    val giftId: String,
    val context: String = "room",
    val roomId: String,
    val receiverId: String,
    val quantity: Int = 1,
    val operationKey: String,
)

data class RoomAccessIssue(val reason: String, val title: String, val message: String)

data class RoomReaction(val emoji: String, val userId: String? = null, val sequence: Long = 0L)
