package app.voicecloud.feature.engagement.model

import app.voicecloud.feature.discovery.model.VoiceCloudUser

data class Page<T>(
    val data: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    val totalPages: Int = 1,
)

data class Community(
    val id: String = "",
    val name: String = "",
    val handle: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val bannerUrl: String? = null,
    val category: String = "General",
    val rules: List<String> = emptyList(),
    val visibility: String = "PUBLIC",
    val memberCount: Int = 0,
    val hostCount: Int = 0,
    val upcomingRoomsCount: Int = 0,
    val ownerId: String = "",
    val isVerified: Boolean = false,
    val owner: VoiceCloudUser? = null,
    val inviteCode: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class CommunityInput(
    val name: String,
    val handle: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val bannerUrl: String? = null,
    val category: String? = null,
    val rules: List<String>? = null,
    val visibility: String? = null,
)

data class JoinCommunityBody(val inviteCode: String? = null)
data class UpdateCommunityRoleBody(val role: String)

data class CommunityMembership(
    val member: Boolean = false,
    val role: String? = null,
    val clubId: String = "",
    val visibility: String = "PUBLIC",
)

data class CommunityMember(
    val id: String = "",
    val clubId: String = "",
    val userId: String = "",
    val role: String = "MEMBER",
    val joinedAt: String = "",
    val updatedAt: String? = null,
    val user: VoiceCloudUser? = null,
)

data class ScheduledEvent(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val category: String = "General",
    val language: String = "en",
    val tags: List<String>? = null,
    val coverUrl: String? = null,
    val hostId: String = "",
    val clubId: String? = null,
    val scheduledStartTime: String = "",
    val durationMinutes: Int = 60,
    val timeZone: String = "UTC",
    val status: String = "SCHEDULED",
    val visibility: String = "PUBLIC",
    val isInviteOnly: Boolean = false,
    val maxParticipants: Int = 500,
    val rsvpCount: Int = 0,
    val isPremium: Boolean = false,
    val ticketPriceAmount: Any? = null,
    val currency: String = "USD",
    val club: Community? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class ReminderSettings(val enablePush: Boolean = true, val enableEmail: Boolean = false)
data class ReminderResult(
    val message: String = "",
    val scheduledRoomId: String = "",
    val userId: String = "",
    val rsvpCount: Int = 0,
    val settings: ReminderSettings = ReminderSettings(),
)

data class ApiMessage(val message: String? = null, val success: Boolean? = null)
data class InviteCodeResult(val clubId: String = "", val inviteCode: String = "")

data class ConversationMember(
    val id: String = "",
    val conversationId: String = "",
    val userId: String = "",
    val role: String = "member",
    val muted: Boolean = false,
    val joinedAt: String = "",
    val leftAt: String? = null,
    val lastReadMessageId: String? = null,
    val lastReadAt: String? = null,
)

data class ChatSender(
    val id: String? = null,
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
)

data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val sender: ChatSender? = null,
    val type: String = "text",
    val content: String? = null,
    val replyToId: String? = null,
    val isPinned: Boolean = false,
    val isEdited: Boolean = false,
    val deliveryStatus: String = "sent",
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class Conversation(
    val id: String = "",
    val type: String = "direct",
    val name: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null,
    val roomId: String? = null,
    val createdById: String? = null,
    val ownerId: String? = null,
    val lastMessageId: String? = null,
    val lastMessageAt: String? = null,
    val members: List<ConversationMember> = emptyList(),
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val peer: ChatSender? = null,
)

data class ConversationList(
    val conversations: List<Conversation> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 50,
)

data class MessagePage(
    val messages: List<ChatMessage> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 50,
)

data class CreateConversationBody(
    val type: String,
    val recipientId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val memberIds: List<String>? = null,
    val roomId: String? = null,
)

data class SendMessageBody(val type: String = "text", val content: String)
data class ReadConversationBody(val lastReadMessageId: String? = null)

data class VoiceCloudNotification(
    val id: String = "",
    val userId: String = "",
    val senderId: String? = null,
    val type: String = "IN_APP",
    val title: String = "",
    val message: String = "",
    val data: Map<String, Any?>? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class UnreadCount(val unreadCount: Int = 0)
data class ReadAllResult(val success: Boolean = false, val updatedCount: Int = 0)
data class SuccessResult(val success: Boolean = false)

data class EngagementRoute(val route: String)
