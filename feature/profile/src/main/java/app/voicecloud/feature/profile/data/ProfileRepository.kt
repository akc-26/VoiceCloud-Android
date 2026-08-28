package app.voicecloud.feature.profile.data

import app.voicecloud.feature.profile.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val api: ProfileApi) {
    suspend fun profile(): ExtendedProfile = api.myProfile()

    suspend fun updateProfile(body: UpdateExtendedProfileBody): ExtendedProfile {
        api.updateProfile(body.normalized())
        return api.myProfile()
    }

    suspend fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String): ExtendedProfile {
        val hasCurrent = runCatching { api.myProfile().avatarUrl?.isNotBlank() == true }.getOrDefault(false)
        val primary: suspend () -> Any = { if (hasCurrent) api.replaceAvatar(mediaPart("avatar", bytes, fileName, mimeType)) else api.uploadAvatar(mediaPart("avatar", bytes, fileName, mimeType)) }
        val fallback: suspend () -> Any = { if (hasCurrent) api.uploadAvatar(mediaPart("avatar", bytes, fileName, mimeType)) else api.replaceAvatar(mediaPart("avatar", bytes, fileName, mimeType)) }
        runCatching { primary() }.getOrElse { first -> runCatching { fallback() }.getOrElse { throw first } }
        return api.myProfile()
    }

    suspend fun deleteAvatar(): ExtendedProfile {
        api.deleteAvatar()
        return api.myProfile()
    }

    suspend fun uploadCover(bytes: ByteArray, fileName: String, mimeType: String): ExtendedProfile {
        val hasCurrent = runCatching { api.myProfile().coverUrl?.isNotBlank() == true }.getOrDefault(false)
        val primary: suspend () -> Any = { if (hasCurrent) api.replaceCover(mediaPart("cover", bytes, fileName, mimeType)) else api.uploadCover(mediaPart("cover", bytes, fileName, mimeType)) }
        val fallback: suspend () -> Any = { if (hasCurrent) api.uploadCover(mediaPart("cover", bytes, fileName, mimeType)) else api.replaceCover(mediaPart("cover", bytes, fileName, mimeType)) }
        runCatching { primary() }.getOrElse { first -> runCatching { fallback() }.getOrElse { throw first } }
        return api.myProfile()
    }

    suspend fun deleteCover(): ExtendedProfile {
        api.deleteCover()
        return api.myProfile()
    }

    suspend fun replays(): List<ReplayItem> = extractItems(api.replays(), "replays")
        .mapNotNull(::replayFrom)
        .filter { it.id.isNotBlank() }
        .distinctBy { it.id }

    suspend fun replay(id: String): ReplayItem {
        require(id.isNotBlank()) { "Replay is unavailable." }
        val raw = unwrapObject(api.replay(id), "replay")
        return replayFrom(raw) ?: error("Replay is unavailable.")
    }

    suspend fun activity(): List<ActivityItem> = extractItems(api.myActivity(), "activity", "activities")
        .mapNotNull(::activityFrom)
        .distinctBy { it.id.ifBlank { "${it.roomId}:${it.joinedAt}:${it.createdAt}" } }

    suspend fun blockedUsers(): List<BlockedUserItem> = extractItems(api.blockedUsers(), "blocks", "blockedUsers")
        .mapNotNull(::blockedFrom)
        .filter { it.blockedUserId.isNotBlank() || it.person.id.isNotBlank() }
        .distinctBy { it.blockedUserId.ifBlank { it.person.id } }

    suspend fun unblock(userId: String) {
        require(userId.isNotBlank()) { "Blocked user is unavailable." }
        api.unblock(userId)
    }

    suspend fun visitors(): List<ProfileVisitorItem> = extractItems(api.visitors(), "visitors")
        .mapNotNull(::visitorFrom)
        .filter { it.visitorUserId.isNotBlank() || it.person.id.isNotBlank() }
        .distinctBy { it.id.ifBlank { "${it.visitorUserId}:${it.visitedAt}" } }

    suspend fun visitorStats(): VisitorStats {
        val raw = unwrapObject(api.visitorStats(), "stats")
        return VisitorStats(
            total = raw.int("total", "totalVisitors", "visitorsCount"),
            today = raw.int("today", "todayVisitors"),
            thisWeek = raw.int("thisWeek", "week", "weeklyVisitors"),
            thisMonth = raw.int("thisMonth", "month", "monthlyVisitors"),
        )
    }


    suspend fun helpPages(): List<HelpPageSummary> = extractItems(api.helpPages(), "pages")
        .mapNotNull(::helpPageSummaryFrom)
        .filter { it.slug.isNotBlank() && it.title.isNotBlank() }
        .distinctBy { it.slug.lowercase() }
        .sortedWith(compareBy<HelpPageSummary> { it.sortOrder }.thenBy { it.title.lowercase() })

    suspend fun helpPage(slug: String): HelpPageDetail {
        require(slug.isNotBlank()) { "Help page is unavailable." }
        val raw = unwrapObject(api.helpPage(slug.trim()), "page")
        return HelpPageDetail(
            id = raw.string("id"),
            slug = raw.string("slug", "key").ifBlank { slug.trim() },
            title = raw.string("title", "name").ifBlank { "Help" },
            content = raw.string("content", "body", "html", "description"),
            updatedAt = raw.string("updatedAt", "publishedAt").takeIf(String::isNotBlank),
        )
    }

    private fun helpPageSummaryFrom(raw: Map<*, *>): HelpPageSummary? {
        val slug = raw.string("slug", "key", "id")
        val title = raw.string("title", "name")
        if (slug.isBlank() || title.isBlank()) return null
        return HelpPageSummary(
            id = raw.string("id"),
            slug = slug,
            title = title,
            excerpt = raw.string("excerpt", "summary", "description").takeIf(String::isNotBlank),
            category = raw.string("category", "type").takeIf(String::isNotBlank),
            sortOrder = raw.int("sortOrder", "order", "position"),
        )
    }

    private fun UpdateExtendedProfileBody.normalized() = copy(
        bio = bio?.trim()?.takeIf(String::isNotBlank),
        country = country?.trim()?.takeIf(String::isNotBlank),
        preferredLanguage = preferredLanguage?.trim()?.takeIf(String::isNotBlank),
        interests = interests?.map(String::trim)?.filter(String::isNotBlank)?.distinct()?.take(20),
    )

    private fun mediaPart(field: String, bytes: ByteArray, fileName: String, mimeType: String): MultipartBody.Part {
        require(bytes.isNotEmpty()) { "Selected media is empty." }
        val safeName = fileName.trim().ifBlank { "$field-upload" }
        val type = mimeType.trim().ifBlank { "application/octet-stream" }.toMediaTypeOrNull()
        return MultipartBody.Part.createFormData(field, safeName, bytes.toRequestBody(type))
    }

    private fun replayFrom(raw: Map<*, *>): ReplayItem? {
        val room = raw.map("room")
        val id = raw.string("id", "replayId", "recordingId")
        if (id.isBlank()) return null
        val explicitAccess = raw.booleanOrNull("canAccess", "accessAllowed", "isAccessible")
        return ReplayItem(
            id = id,
            roomId = raw.string("roomId").ifBlank { room.string("id", "roomId") },
            title = raw.string("title", "name").ifBlank { room.string("title", "name").ifBlank { "Replay" } },
            roomTitle = (room.string("title", "name").ifBlank { raw.string("roomTitle") }).takeIf(String::isNotBlank),
            hostName = (raw.string("hostName", "hostDisplayName").ifBlank {
                raw.map("host").string("displayName", "username")
            }).takeIf(String::isNotBlank),
            coverUrl = (raw.string("coverUrl", "thumbnailUrl", "imageUrl").ifBlank { room.string("coverUrl") }).takeIf(String::isNotBlank),
            playbackUrl = raw.string("playbackUrl", "recordingUrl", "audioUrl", "mediaUrl", "url").takeIf(String::isNotBlank),
            status = raw.string("status", "processingStatus"),
            durationSeconds = raw.long("durationSeconds", "duration", "lengthSeconds"),
            createdAt = raw.string("createdAt", "recordedAt", "startedAt").takeIf(String::isNotBlank),
            accessAllowed = explicitAccess,
            accessReason = raw.string("accessReason", "reason", "message").takeIf(String::isNotBlank),
        )
    }

    private fun activityFrom(raw: Map<*, *>): ActivityItem? {
        val room = raw.map("room")
        val roomId = raw.string("roomId").ifBlank { room.string("id", "roomId") }
        val id = raw.string("id", "activityId", "sessionId")
        if (id.isBlank() && roomId.isBlank()) return null
        return ActivityItem(
            id = id,
            roomId = roomId,
            type = raw.string("type", "action", "eventType").ifBlank { "ROOM" },
            title = raw.string("title").ifBlank { room.string("title", "name").ifBlank { "Room activity" } },
            roomTitle = (room.string("title", "name").ifBlank { raw.string("roomTitle") }).takeIf(String::isNotBlank),
            hostName = (raw.string("hostName").ifBlank { room.map("host").string("displayName", "username") }).takeIf(String::isNotBlank),
            joinedAt = raw.string("joinedAt", "joinTime", "startedAt").takeIf(String::isNotBlank),
            leftAt = raw.string("leftAt", "leaveTime", "endedAt").takeIf(String::isNotBlank),
            durationSeconds = raw.long("durationSeconds", "duration"),
            createdAt = raw.string("createdAt").takeIf(String::isNotBlank),
        )
    }

    private fun blockedFrom(raw: Map<*, *>): BlockedUserItem? {
        val personRaw = raw.map("blockedUser").ifEmpty { raw.map("user").ifEmpty { raw.map("targetUser") } }
        val blockedUserId = raw.string("blockedUserId", "targetUserId", "userId").ifBlank { personRaw.string("id") }
        if (blockedUserId.isBlank() && personRaw.isEmpty()) return null
        return BlockedUserItem(
            id = raw.string("id", "blockId"),
            blockedUserId = blockedUserId,
            person = personFrom(personRaw, blockedUserId),
            createdAt = raw.string("createdAt", "blockedAt").takeIf(String::isNotBlank),
        )
    }

    private fun visitorFrom(raw: Map<*, *>): ProfileVisitorItem? {
        val personRaw = raw.map("visitor").ifEmpty { raw.map("user").ifEmpty { raw.map("viewer") } }
        val visitorId = raw.string("visitorId", "visitorUserId", "userId").ifBlank { personRaw.string("id") }
        if (visitorId.isBlank() && personRaw.isEmpty()) return null
        return ProfileVisitorItem(
            id = raw.string("id", "visitId"),
            visitorUserId = visitorId,
            person = personFrom(personRaw, visitorId),
            visitedAt = raw.string("visitedAt", "createdAt", "lastVisitedAt").takeIf(String::isNotBlank),
        )
    }

    private fun personFrom(raw: Map<*, *>, fallbackId: String) = PersonSummary(
        id = raw.string("id", "userId").ifBlank { fallbackId },
        username = raw.string("username"),
        displayName = raw.string("displayName", "name"),
        avatarUrl = raw.string("avatarUrl", "avatar").takeIf(String::isNotBlank),
    )

    private fun extractItems(raw: Any?, vararg preferredKeys: String): List<Map<*, *>> {
        if (raw is List<*>) return raw.mapNotNull { it as? Map<*, *> }
        val root = raw as? Map<*, *> ?: return emptyList()
        val keys = preferredKeys.toList() + listOf("data", "items", "results")
        for (key in keys.distinct()) {
            when (val candidate = root[key]) {
                is List<*> -> return candidate.mapNotNull { it as? Map<*, *> }
                is Map<*, *> -> {
                    val nested = candidate["items"] ?: candidate["data"] ?: candidate[preferredKeys.firstOrNull()]
                    if (nested is List<*>) return nested.mapNotNull { it as? Map<*, *> }
                }
            }
        }
        return emptyList()
    }

    private fun unwrapObject(raw: Any?, preferredKey: String): Map<*, *> {
        val root = raw as? Map<*, *> ?: return emptyMap<Any?, Any?>()
        val nested = root[preferredKey] ?: root["data"] ?: root["result"]
        return nested as? Map<*, *> ?: root
    }

    private fun Map<*, *>.string(vararg keys: String): String {
        for (key in keys) {
            val value = this[key]
            when (value) {
                is String -> if (value.isNotBlank()) return value
                is Number, is Boolean -> return value.toString()
            }
        }
        return ""
    }

    private fun Map<*, *>.long(vararg keys: String): Long {
        for (key in keys) {
            when (val value = this[key]) {
                is Number -> return value.toLong()
                is String -> value.toLongOrNull()?.let { return it }
            }
        }
        return 0L
    }

    private fun Map<*, *>.int(vararg keys: String): Int = long(*keys).toInt()

    private fun Map<*, *>.booleanOrNull(vararg keys: String): Boolean? {
        for (key in keys) {
            when (val value = this[key]) {
                is Boolean -> return value
                is String -> when (value.lowercase()) { "true" -> return true; "false" -> return false }
                is Number -> return value.toInt() != 0
            }
        }
        return null
    }

    private fun Map<*, *>.map(key: String): Map<*, *> = this[key] as? Map<*, *> ?: emptyMap<Any?, Any?>()
}
