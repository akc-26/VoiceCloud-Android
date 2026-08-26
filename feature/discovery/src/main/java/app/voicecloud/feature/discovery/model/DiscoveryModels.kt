package app.voicecloud.feature.discovery.model

data class PaginatedItems<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    val totalPages: Int = 1,
)

data class PaginatedData<T>(
    val data: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    val totalPages: Int = 1,
)

data class VoiceCloudUser(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val role: String? = null,
    val isGuest: Boolean = false,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val bio: String? = null,
    val statusMessage: String? = null,
    val country: String? = null,
    val preferredLanguage: String? = null,
    val interests: List<String>? = null,
    val customTags: List<String>? = null,
    val hostBadge: String? = null,
    val vipBadge: String? = null,
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val isVip: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val popularityScore: Double = 0.0,
    val wealthLevel: Int = 0,
    val charmLevel: Int = 0,
    val createdAt: String? = null,
)

data class VoiceCloudProfile(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val role: String? = null,
    val isGuest: Boolean = false,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val bio: String? = null,
    val statusMessage: String? = null,
    val country: String? = null,
    val preferredLanguage: String? = null,
    val interests: List<String>? = null,
    val customTags: List<String>? = null,
    val hostBadge: String? = null,
    val vipBadge: String? = null,
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val isVip: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val popularityScore: Double = 0.0,
    val wealthLevel: Int = 0,
    val charmLevel: Int = 0,
    val createdAt: String? = null,
    val profileCompletionPercentage: Int = 0,
    val wealthTitle: String? = null,
    val charmTitle: String? = null,
    val relationship: ProfileRelationship? = null,
    val stats: ProfileStats? = null,
)

data class ProfileRelationship(
    val isFollowing: Boolean = false,
    val isFollowedBy: Boolean = false,
    val isMutual: Boolean = false,
)

data class ProfileStats(
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val badgesCount: Int = 0,
    val visitorsCount: Int = 0,
)

data class VoiceCloudRoom(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val hostId: String = "",
    val coverUrl: String? = null,
    val isLocked: Boolean = false,
    val isLive: Boolean = false,
    val status: String = "",
    val language: String = "",
    val category: String = "",
    val listenerCount: Int = 0,
    val speakerCount: Int = 0,
    val popularityScore: Double = 0.0,
    val isPremium: Boolean = false,
    val isTicketRequired: Boolean = false,
    val isSubscriberOnly: Boolean = false,
    val isInviteOnly: Boolean = false,
    val isVerifiedOnly: Boolean = false,
    val createdAt: String? = null,
)

data class SearchResults(
    val users: PaginatedItems<VoiceCloudUser>? = null,
    val rooms: PaginatedItems<VoiceCloudRoom>? = null,
)

data class GlobalSearchResponse(
    val query: String = "",
    val type: String? = null,
    val results: SearchResults = SearchResults(),
)

data class FollowMutationResult(val isFollowing: Boolean = false)

data class FriendListItem(
    val friendshipId: String = "",
    val category: String? = null,
    val alias: String? = null,
    val addedAt: String? = null,
    val user: VoiceCloudUser = VoiceCloudUser(),
)

data class PendingFriendRequest(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val status: String = "",
    val sender: VoiceCloudUser? = null,
    val receiver: VoiceCloudUser? = null,
)

data class PendingFriendRequests(
    val incoming: List<PendingFriendRequest> = emptyList(),
    val outgoing: List<PendingFriendRequest> = emptyList(),
)

data class SuggestedFriends(
    val data: List<VoiceCloudUser> = emptyList(),
    val total: Int = 0,
)

data class FriendRequestBody(val receiverId: String)

data class ApiMessage(val message: String? = null, val success: Boolean? = null)

data class ViewerIdentity(val id: String? = null, val username: String? = null)

data class HomeSnapshot(
    val rooms: List<VoiceCloudRoom> = emptyList(),
    val people: List<VoiceCloudUser> = emptyList(),
    val creators: List<VoiceCloudUser> = emptyList(),
)

data class ExploreSnapshot(
    val liveRooms: List<VoiceCloudRoom> = emptyList(),
    val trendingRooms: List<VoiceCloudRoom> = emptyList(),
    val people: List<VoiceCloudUser> = emptyList(),
    val creators: List<VoiceCloudUser> = emptyList(),
)

data class SearchSnapshot(
    val query: String = "",
    val users: List<VoiceCloudUser> = emptyList(),
    val rooms: List<VoiceCloudRoom> = emptyList(),
)
