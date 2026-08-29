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
            rooms = rooms.await().items.distinctBy { it.id }.take(10),
            people = people.filter { it.role?.uppercase() != "CREATOR" }.take(4),
            creators = people.filter { it.role?.uppercase() == "CREATOR" }.take(4),
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
            (online.await().items + trending.await().items + suggested.await().items).distinctBy { it.id },
            viewer,
        ).sortedWith(compareByDescending<VoiceCloudUser> { it.isOnline }.thenByDescending { it.popularityScore })
        if (creatorsOnly) merged.filter { it.role?.uppercase() == "CREATOR" } else merged.filter { it.role?.uppercase() != "CREATOR" }
    }

    suspend fun searchLanding(viewer: ViewerIdentity): SearchLandingSnapshot = coroutineScope {
        val live = async { api.liveRooms(limit = 12) }
        val online = async { api.onlineUsers(limit = 40) }
        val trending = async { api.trendingUsers(limit = 40) }
        val suggested = async { api.suggestedUsers(limit = 40) }
        val users = visibleUsers(
            (online.await().items + trending.await().items + suggested.await().items).distinctBy { it.id },
            viewer,
        ).sortedWith(compareByDescending<VoiceCloudUser> { it.isOnline }.thenByDescending { it.popularityScore })
        SearchLandingSnapshot(
            people = users.filter { it.role?.uppercase() != "CREATOR" },
            creators = users.filter { it.role?.uppercase() == "CREATOR" },
            rooms = live.await().items.distinctBy { it.id },
        )
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

    suspend fun followersPage(viewer: ViewerIdentity, search: String = "", page: Int = 1, limit: Int = 50): PaginatedData<VoiceCloudUser> {
        val raw = api.followers(page = page, limit = limit, search = search.trim().ifBlank { null })
        return raw.copy(data = visibleUsers(raw.data, viewer))
    }

    suspend fun followingPage(viewer: ViewerIdentity, search: String = "", page: Int = 1, limit: Int = 50): PaginatedData<VoiceCloudUser> {
        val raw = api.following(page = page, limit = limit, search = search.trim().ifBlank { null })
        return raw.copy(data = visibleUsers(raw.data, viewer))
    }

    suspend fun allFollowers(viewer: ViewerIdentity, search: String = ""): PaginatedData<VoiceCloudUser> {
        val first = followersPage(viewer, search, page = 1, limit = 100)
        val pages = maxOf(first.totalPages, if (first.total > 0) (first.total + first.limit - 1) / first.limit else 1)
        if (pages <= 1) return first
        val all = first.data.toMutableList()
        for (page in 2..pages.coerceAtMost(1000)) all += followersPage(viewer, search, page, 100).data
        return first.copy(data = all.distinctBy { it.id })
    }

    suspend fun allFollowing(viewer: ViewerIdentity): PaginatedData<VoiceCloudUser> {
        val first = followingPage(viewer, page = 1, limit = 100)
        val pages = maxOf(first.totalPages, if (first.total > 0) (first.total + first.limit - 1) / first.limit else 1)
        if (pages <= 1) return first
        val all = first.data.toMutableList()
        for (page in 2..pages.coerceAtMost(1000)) all += followingPage(viewer, page = page, limit = 100).data
        return first.copy(data = all.distinctBy { it.id })
    }

    suspend fun friends(viewer: ViewerIdentity): List<FriendListItem> = coroutineScope {
        friendItems(api.friends()).map { item ->
            async { authoritativeRelationshipUser(item.user, viewer)?.let { item.copy(user = it) } }
        }.mapNotNull { it.await() }.distinctBy { it.friendshipId.ifBlank { it.user.id } }
    }

    suspend fun pending(viewer: ViewerIdentity): PendingFriendRequests = coroutineScope {
        val result = pendingRequests(api.pendingFriendRequests())
        val incoming = result.incoming.map { request ->
            async { request.sender?.let { authoritativeRelationshipUser(it, viewer) }?.let { request.copy(sender = it) } }
        }.mapNotNull { it.await() }
        val outgoing = result.outgoing.map { request ->
            async { request.receiver?.let { authoritativeRelationshipUser(it, viewer) }?.let { request.copy(receiver = it) } }
        }.mapNotNull { it.await() }
        PendingFriendRequests(incoming.distinctBy { it.id }, outgoing.distinctBy { it.id })
    }

    suspend fun suggestedFriends(viewer: ViewerIdentity): List<VoiceCloudUser> = coroutineScope {
        userItems(api.suggestedFriends(), "suggestions", "suggested", "users").map { candidate ->
            async { authoritativeRelationshipUser(candidate, viewer) }
        }.mapNotNull { it.await() }.distinctBy { it.id.ifBlank { it.username.trim().lowercase() } }
    }

    suspend fun sendFriendRequest(userId: String) = api.sendFriendRequest(FriendRequestBody(userId))
    suspend fun acceptFriendRequest(requestId: String) = api.acceptFriendRequest(requestId)
    suspend fun rejectFriendRequest(requestId: String) = api.rejectFriendRequest(requestId)
    suspend fun cancelFriendRequest(requestId: String) = api.cancelFriendRequest(requestId)
    suspend fun removeFriend(userId: String) = api.removeFriend(userId)

    private fun friendItems(raw: Any?): List<FriendListItem> = itemMaps(raw, "friends", "data", "items").mapNotNull { map ->
        val userMap = map.map("user").ifEmpty { map.map("friend").ifEmpty { map.map("profile") } }
        val user = userFrom(userMap)
        if (user.id.isBlank()) null else FriendListItem(
            friendshipId = map.string("friendshipId", "id"),
            category = map.string("category").takeIf(String::isNotBlank),
            alias = map.string("alias").takeIf(String::isNotBlank),
            addedAt = map.string("addedAt", "createdAt").takeIf(String::isNotBlank),
            user = user,
        )
    }

    private fun pendingRequests(raw: Any?): PendingFriendRequests {
        val root = unwrap(raw)
        fun requests(key: String): List<PendingFriendRequest> = itemMaps(root[key], key, "data", "items").mapNotNull { map ->
            val sender = userFrom(map.map("sender")).takeIf { it.id.isNotBlank() }
            val receiver = userFrom(map.map("receiver")).takeIf { it.id.isNotBlank() }
            val id = map.string("id", "requestId")
            if (id.isBlank()) null else PendingFriendRequest(id, map.string("senderId"), map.string("receiverId"), map.string("status"), sender, receiver)
        }
        return PendingFriendRequests(requests("incoming"), requests("outgoing"))
    }

    private fun userItems(raw: Any?, vararg keys: String): List<VoiceCloudUser> = itemMaps(raw, *keys).map(::userFrom).filter { it.id.isNotBlank() }

    private fun itemMaps(raw: Any?, vararg preferred: String): List<Map<*, *>> {
        if (raw is List<*>) return raw.mapNotNull { it as? Map<*, *> }
        val root = unwrap(raw)
        for (key in (preferred.toList() + listOf("data", "items", "results")).distinct()) {
            val value = root[key]
            if (value is List<*>) return value.mapNotNull { it as? Map<*, *> }
            if (value is Map<*, *>) {
                val nested = value["items"] ?: value["data"] ?: value["results"]
                if (nested is List<*>) return nested.mapNotNull { it as? Map<*, *> }
            }
        }
        return emptyList()
    }

    private fun unwrap(raw: Any?): Map<*, *> {
        val map = raw as? Map<*, *> ?: return emptyMap<Any?, Any?>()
        return (map["data"] as? Map<*, *>) ?: map
    }

    private fun userFrom(raw: Map<*, *>) = VoiceCloudUser(
        id = raw.string("id", "userId"), username = raw.string("username"), displayName = raw.string("displayName", "name"),
        role = raw.string("role").takeIf(String::isNotBlank), isGuest = raw.boolean("isGuest"), avatarUrl = raw.string("avatarUrl", "avatar").takeIf(String::isNotBlank),
        bio = raw.string("bio").takeIf(String::isNotBlank), country = raw.string("country").takeIf(String::isNotBlank), isOnline = raw.boolean("isOnline", "online"),
        isVerified = raw.boolean("isVerified", "verified"), isVip = raw.boolean("isVip"), followersCount = raw.int("followersCount"), followingCount = raw.int("followingCount"),
        popularityScore = raw.double("popularityScore", "score"), wealthLevel = raw.int("wealthLevel"), charmLevel = raw.int("charmLevel"),
    )

    private fun Map<*, *>.string(vararg keys: String): String = keys.firstNotNullOfOrNull { key -> this[key]?.toString()?.takeIf(String::isNotBlank) }.orEmpty()
    private fun Map<*, *>.boolean(vararg keys: String): Boolean = keys.any { key -> when (val value = this[key]) { is Boolean -> value; is Number -> value.toInt() != 0; is String -> value.equals("true", true) || value.equals("online", true); else -> false } }
    private fun Map<*, *>.int(vararg keys: String): Int = keys.firstNotNullOfOrNull { key -> when (val value = this[key]) { is Number -> value.toInt(); is String -> value.toIntOrNull(); else -> null } } ?: 0
    private fun Map<*, *>.double(vararg keys: String): Double = keys.firstNotNullOfOrNull { key -> when (val value = this[key]) { is Number -> value.toDouble(); is String -> value.toDoubleOrNull(); else -> null } } ?: 0.0
    private fun Map<*, *>.map(key: String): Map<*, *> = this[key] as? Map<*, *> ?: emptyMap<Any?, Any?>()

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
