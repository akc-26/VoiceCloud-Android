package app.voicecloud.core.model

data class PaginatedItems<T>(
    val items: List<T> = emptyList(),
    val total: Int? = null,
    val page: Int? = null,
    val pageSize: Int? = null,
)

/** Common VoiceCloud list/detail envelope (`data` node). */
data class DataEnvelope<T>(
    val data: T? = null,
    val message: String? = null,
)

data class DiscoveryRoomDto(
    val id: String? = null,
    val roomId: String? = null,
    val title: String? = null,
    val name: String? = null,
    val description: String? = null,
    val hostName: String? = null,
    val hostDisplayName: String? = null,
    val listenerCount: Int? = null,
    val participantCount: Int? = null,
    val isLive: Boolean? = null,
    val live: Boolean? = null,
    val status: String? = null,
    val coverImageUrl: String? = null,
    val thumbnailUrl: String? = null,
) {
    val resolvedId: String? get() = id ?: roomId
    val resolvedTitle: String get() = title ?: name ?: "VoiceCloud room"
    val resolvedLive: Boolean get() = isLive == true || live == true || status.equals("live", ignoreCase = true)
}

data class DiscoveryUserDto(
    val id: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val isOnline: Boolean? = null,
    val bio: String? = null,
) {
    val resolvedId: String? get() = id ?: userId
    val resolvedName: String get() = displayName ?: username ?: "VoiceCloud member"
}

data class SearchResultsDto(
    val rooms: List<DiscoveryRoomDto>? = null,
    val users: List<DiscoveryUserDto>? = null,
    val hosts: List<DiscoveryUserDto>? = null,
)

data class UserProfileDto(
    val id: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val displayName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null,
    val role: String? = null,
    val followerCount: Int? = null,
    val followingCount: Int? = null,
) {
    val resolvedId: String? get() = id ?: userId
}

data class ChatConversationDto(
    val id: String? = null,
    val conversationId: String? = null,
    val title: String? = null,
    val name: String? = null,
    val unreadCount: Int? = null,
    val lastMessagePreview: String? = null,
    val updatedAt: String? = null,
) {
    val resolvedId: String get() = id ?: conversationId ?: ""
    val resolvedTitle: String get() = title ?: name ?: "Conversation"
}

data class ChatMessageDto(
    val id: String? = null,
    val messageId: String? = null,
    val text: String? = null,
    val content: String? = null,
    val senderId: String? = null,
    val createdAt: String? = null,
    val isOwn: Boolean? = null,
    val outgoing: Boolean? = null,
) {
    val resolvedId: String get() = id ?: messageId ?: createdAt ?: text.hashCode().toString()
    val resolvedText: String get() = text ?: content ?: ""
    val resolvedOutgoing: Boolean get() = isOwn == true || outgoing == true
}

data class WalletBalanceDto(
    val coins: Long? = null,
    val diamonds: Long? = null,
    val balance: Long? = null,
    val currencyLabel: String? = null,
)

data class WalletTransactionDto(
    val id: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val createdAt: String? = null,
)

data class RoomDetailDto(
    val id: String? = null,
    val roomId: String? = null,
    val title: String? = null,
    val name: String? = null,
    val description: String? = null,
    val hostName: String? = null,
    val hostDisplayName: String? = null,
    val isLive: Boolean? = null,
    val status: String? = null,
    val listenerCount: Int? = null,
) {
    val resolvedId: String? get() = id ?: roomId
}

data class RtcJoinRequest(val roomId: String)

data class RtcJoinResponse(
    val token: String? = null,
    val liveKitToken: String? = null,
    val url: String? = null,
    val serverUrl: String? = null,
)

data class AuthSessionDto(
    val id: String? = null,
    val sessionId: String? = null,
    val deviceName: String? = null,
    val deviceId: String? = null,
    val ipAddress: String? = null,
    val lastActiveAt: String? = null,
    val createdAt: String? = null,
    val current: Boolean? = null,
    val userAgent: String? = null,
)

data class AuthDeviceDto(
    val id: String? = null,
    val deviceId: String? = null,
    val name: String? = null,
    val deviceName: String? = null,
    val platform: String? = null,
    val lastActiveAt: String? = null,
    val createdAt: String? = null,
    val current: Boolean? = null,
) {
    val resolvedId: String? get() = id ?: deviceId
    val resolvedName: String get() = deviceName ?: name ?: "VoiceCloud device"
}

data class NotificationDto(
    val id: String? = null,
    val notificationId: String? = null,
    val title: String? = null,
    val body: String? = null,
    val message: String? = null,
    val type: String? = null,
    val read: Boolean? = null,
    val isRead: Boolean? = null,
    val createdAt: String? = null,
    val targetType: String? = null,
    val targetId: String? = null,
    val actionUrl: String? = null,
    val link: String? = null,
    val conversationId: String? = null,
    val roomId: String? = null,
    val userId: String? = null,
    val clubId: String? = null,
) {
    val resolvedId: String? get() = id ?: notificationId
    val resolvedTitle: String get() = title ?: type ?: "Notification"
    val resolvedBody: String? get() = body ?: message
    val resolvedRead: Boolean get() = read == true || isRead == true
}

data class NotificationUnreadDto(
    val count: Int? = null,
    val unread: Int? = null,
) {
    val resolvedCount: Int get() = count ?: unread ?: 0
}

data class UpdateUserProfileRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val username: String? = null,
)

data class FollowStatsDto(
    val followerCount: Int? = null,
    val followingCount: Int? = null,
    val isFollowing: Boolean? = null,
)

data class ReferralSummaryDto(
    val code: String? = null,
    val totalReferrals: Int? = null,
    val rewardsEarned: Int? = null,
)

data class TaskItemDto(
    val id: String? = null,
    val taskId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val progress: Int? = null,
    val target: Int? = null,
    val completed: Boolean? = null,
) {
    val resolvedId: String? get() = id ?: taskId
}

data class AchievementItemDto(
    val id: String? = null,
    val achievementId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val unlocked: Boolean? = null,
) {
    val resolvedId: String? get() = id ?: achievementId
}

data class CreatorAccessApplicationRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val reason: String? = null,
)

data class FriendRequestDto(
    val id: String? = null,
    val requestId: String? = null,
    val userId: String? = null,
    val fromUserId: String? = null,
    val user: DiscoveryUserDto? = null,
    val fromUser: DiscoveryUserDto? = null,
) {
    val resolvedId: String? get() = id ?: requestId
}

data class FriendRequestBody(val userId: String)

data class ProfileVisitorDto(
    val id: String? = null,
    val visitorId: String? = null,
    val userId: String? = null,
    val visitedAt: String? = null,
    val createdAt: String? = null,
    val visitor: DiscoveryUserDto? = null,
    val user: DiscoveryUserDto? = null,
) {
    val resolvedPerson: DiscoveryUserDto? get() = visitor ?: user
    val resolvedVisitedAt: String? get() = visitedAt ?: createdAt
}

data class ProfileVisitorStatsDto(
    val totalVisitors: Int? = null,
    val recentCount: Int? = null,
    val count: Int? = null,
) {
    val resolvedTotal: Int get() = totalVisitors ?: count ?: recentCount ?: 0
}

data class RoomActivityItemDto(
    val id: String? = null,
    val roomId: String? = null,
    val roomTitle: String? = null,
    val title: String? = null,
    val joinedAt: String? = null,
    val leftAt: String? = null,
    val createdAt: String? = null,
    val durationSeconds: Int? = null,
) {
    val resolvedTitle: String get() = roomTitle ?: title ?: "VoiceCloud room"
    val resolvedWhen: String? get() = joinedAt ?: leftAt ?: createdAt
}

data class UserSettingsDto(
    val pushEnabled: Boolean? = null,
    val emailEnabled: Boolean? = null,
    val socialNotifications: Boolean? = null,
    val messageNotifications: Boolean? = null,
    val roomNotifications: Boolean? = null,
    val marketingNotifications: Boolean? = null,
)

data class UpdateUserSettingsRequest(
    val pushEnabled: Boolean? = null,
    val emailEnabled: Boolean? = null,
    val socialNotifications: Boolean? = null,
    val messageNotifications: Boolean? = null,
    val roomNotifications: Boolean? = null,
    val marketingNotifications: Boolean? = null,
)

data class ClubDto(
    val id: String? = null,
    val clubId: String? = null,
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    val memberCount: Int? = null,
    val membersCount: Int? = null,
    val coverImageUrl: String? = null,
) {
    val resolvedId: String? get() = id ?: clubId
    val resolvedName: String get() = name ?: title ?: "VoiceCloud community"
}

data class CreateClubRequest(
    val name: String,
    val description: String? = null,
)

data class UpdateClubRequest(
    val name: String? = null,
    val description: String? = null,
)

data class ScheduledRoomDto(
    val id: String? = null,
    val scheduledRoomId: String? = null,
    val title: String? = null,
    val name: String? = null,
    val description: String? = null,
    val startsAt: String? = null,
    val scheduledAt: String? = null,
    val startTime: String? = null,
    val location: String? = null,
    val hostName: String? = null,
) {
    val resolvedId: String? get() = id ?: scheduledRoomId
    val resolvedTitle: String get() = title ?: name ?: "VoiceCloud event"
    val resolvedTime: String? get() = startsAt ?: scheduledAt ?: startTime
}

data class CreateConversationRequest(
    val participantUserId: String? = null,
    val userId: String? = null,
)
