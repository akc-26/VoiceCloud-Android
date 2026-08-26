package app.voicecloud.feature.discovery.data

import app.voicecloud.feature.discovery.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepository @Inject constructor(private val api: DiscoveryApi) {
    fun isConsumerVisible(user: VoiceCloudUser): Boolean = ConsumerIdentityPolicy.isVisible(user)

    fun isDiscoverable(user: VoiceCloudUser, viewer: ViewerIdentity): Boolean = ConsumerIdentityPolicy.isDiscoverable(user, viewer)

    fun visibleUsers(users: List<VoiceCloudUser>, viewer: ViewerIdentity): List<VoiceCloudUser> = ConsumerIdentityPolicy.filter(users, viewer)

    suspend fun home(viewer: ViewerIdentity): HomeSnapshot = coroutineScope {
        val rooms = async { api.liveRooms(limit = 12) }
        val trending = async { api.trendingUsers(limit = 24) }
        val suggested = async { api.suggestedUsers(limit = 24) }
        val people = visibleUsers((trending.await().items + suggested.await().items).distinctBy { it.id }, viewer)
        HomeSnapshot(
            rooms = rooms.await().items.distinctBy { it.id }.take(6),
            people = people.take(8),
            creators = people.filter { it.role?.uppercase() == "CREATOR" }.take(8),
        )
    }

    suspend fun explore(viewer: ViewerIdentity): ExploreSnapshot = coroutineScope {
        val live = async { api.liveRooms(limit = 40) }
        val trendingRooms = async { api.trendingRooms(limit = 20) }
        val trendingUsers = async { api.trendingUsers(limit = 40) }
        val suggested = async { api.suggestedUsers(limit = 40) }
        val people = visibleUsers((trendingUsers.await().items + suggested.await().items).distinctBy { it.id }, viewer)
        ExploreSnapshot(
            liveRooms = live.await().items.distinctBy { it.id },
            trendingRooms = trendingRooms.await().items.distinctBy { it.id },
            people = people,
            creators = people.filter { it.role?.uppercase() == "CREATOR" },
        )
    }

    suspend fun liveRooms(category: String? = null): List<VoiceCloudRoom> = api.liveRooms(category = category).items.distinctBy { it.id }

    suspend fun people(viewer: ViewerIdentity, creatorsOnly: Boolean): List<VoiceCloudUser> = coroutineScope {
        val trending = async { api.trendingUsers(limit = 50) }
        val suggested = async { api.suggestedUsers(limit = 50) }
        val online = async { api.onlineUsers(limit = 50) }
        val merged = visibleUsers(
            (trending.await().items + suggested.await().items + online.await().items).distinctBy { it.id },
            viewer,
        )
        if (creatorsOnly) merged.filter { it.role?.uppercase() == "CREATOR" } else merged
    }

    suspend fun search(query: String, viewer: ViewerIdentity): SearchSnapshot = coroutineScope {
        val clean = query.trim()
        if (clean.isBlank()) return@coroutineScope SearchSnapshot()
        val users = async { api.search(clean, "users", limit = 20) }
        val rooms = async { api.search(clean, "rooms", limit = 20) }
        SearchSnapshot(
            query = clean,
            users = visibleUsers(users.await().results.users?.items.orEmpty(), viewer),
            rooms = rooms.await().results.rooms?.items.orEmpty().distinctBy { it.id },
        )
    }

    suspend fun publicProfile(username: String, viewer: ViewerIdentity): VoiceCloudProfile {
        val public = api.publicProfile(username.trim())
        require(public.username.isNotBlank()) { "VoiceCloud profile is unavailable." }
        val role = public.role?.trim()?.uppercase()
        require(!public.isGuest && (role == null || role == "USER" || role == "CREATOR")) { "This profile is not publicly available." }
        if (viewer.id.isNullOrBlank() || public.id == viewer.id) return public
        return runCatching { api.profileById(public.id) }.getOrDefault(public)
    }

    suspend fun myProfile(): VoiceCloudProfile = api.myProfile()

    suspend fun setFollowing(userId: String, follow: Boolean): FollowMutationResult =
        if (follow) api.follow(userId) else api.unfollow(userId)

    suspend fun followers(viewer: ViewerIdentity, search: String = ""): List<VoiceCloudUser> =
        visibleUsers(api.followers(search = search.trim().ifBlank { null }).data, viewer)

    suspend fun following(viewer: ViewerIdentity, search: String = ""): List<VoiceCloudUser> =
        visibleUsers(api.following(search = search.trim().ifBlank { null }).data, viewer)

    suspend fun friends(viewer: ViewerIdentity): List<FriendListItem> = coroutineScope {
        api.friends().data.map { item ->
            async { authoritativeRelationshipUser(item.user, viewer)?.let { item.copy(user = it) } }
        }.mapNotNull { it.await() }
            .distinctBy { it.friendshipId.ifBlank { it.user.id } }
    }

    suspend fun pending(viewer: ViewerIdentity): PendingFriendRequests = coroutineScope {
        val result = api.pendingFriendRequests()
        val incoming = result.incoming.map { request ->
            async { request.sender?.let { authoritativeRelationshipUser(it, viewer) }?.let { request.copy(sender = it) } }
        }.mapNotNull { it.await() }
        val outgoing = result.outgoing.map { request ->
            async { request.receiver?.let { authoritativeRelationshipUser(it, viewer) }?.let { request.copy(receiver = it) } }
        }.mapNotNull { it.await() }
        PendingFriendRequests(
            incoming = incoming.distinctBy { it.id },
            outgoing = outgoing.distinctBy { it.id },
        )
    }

    suspend fun suggestedFriends(viewer: ViewerIdentity): List<VoiceCloudUser> = coroutineScope {
        api.suggestedFriends().data.map { candidate ->
            async { authoritativeRelationshipUser(candidate, viewer) }
        }.mapNotNull { it.await() }
            .distinctBy { it.id.ifBlank { it.username.trim().lowercase() } }
    }

    suspend fun sendFriendRequest(userId: String) = api.sendFriendRequest(FriendRequestBody(userId))
    suspend fun acceptFriendRequest(requestId: String) = api.acceptFriendRequest(requestId)
    suspend fun rejectFriendRequest(requestId: String) = api.rejectFriendRequest(requestId)
    suspend fun removeFriend(userId: String) = api.removeFriend(userId)

    private suspend fun authoritativeRelationshipUser(user: VoiceCloudUser, viewer: ViewerIdentity): VoiceCloudUser? {
        // Relationship DTOs in finalized R06 may omit role. Missing role therefore fails closed
        // until /users/:id/profile confirms that the identity is a normal USER/CREATOR.
        val authoritative = if (user.role.isNullOrBlank()) {
            runCatching { api.profileById(user.id).asUser() }.getOrNull() ?: return null
        } else user
        return authoritative.takeIf { isDiscoverable(it, viewer) }
    }

    private fun VoiceCloudProfile.asUser() = VoiceCloudUser(
        id = id,
        username = username,
        displayName = displayName,
        role = role,
        isGuest = isGuest,
        avatarUrl = avatarUrl,
        coverUrl = coverUrl,
        bio = bio,
        statusMessage = statusMessage,
        country = country,
        preferredLanguage = preferredLanguage,
        interests = interests,
        customTags = customTags,
        hostBadge = hostBadge,
        vipBadge = vipBadge,
        isOnline = isOnline,
        isVerified = isVerified,
        isVip = isVip,
        followersCount = followersCount,
        followingCount = followingCount,
        popularityScore = popularityScore,
        wealthLevel = wealthLevel,
        charmLevel = charmLevel,
        createdAt = createdAt,
    )
}
