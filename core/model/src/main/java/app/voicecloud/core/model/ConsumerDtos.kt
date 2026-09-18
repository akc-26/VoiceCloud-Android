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

data class CreatorAccessApplicationRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val reason: String? = null,
)
