package app.voicecloud.feature.hosting.data

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
import app.voicecloud.core.realtime.RealtimeClient
import app.voicecloud.core.realtime.RealtimeConnectionState
import app.voicecloud.feature.hosting.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostingRepository @Inject constructor(
    private val api: HostingApi,
    private val realtime: RealtimeClient,
) {
    suspend fun hostAccess(): Pair<HostProfile?, HostEligibility?> {
        val profile = runCatching { api.hostProfile() }.getOrNull()
        val eligibility = runCatching { api.hostEligibility() }.getOrNull()
        return profile to eligibility
    }


    suspend fun hostProgression(): HostProgression {
        val raw = unwrap(api.hostProgression(), "progression", "data", "summary")
        val metrics = raw.entries.mapNotNull { (key, value) ->
            val k = key?.toString()?.trim()?.takeIf(String::isNotBlank) ?: return@mapNotNull null
            if (k.equals("updatedAt", true) || k.equals("timestamp", true) || value is Map<*, *> || value is List<*>) return@mapNotNull null
            HostProgressMetric(k, humanize(k), value?.toString().orEmpty())
        }.sortedBy { it.label }
        return HostProgression(metrics, raw.string("updatedAt", "timestamp").takeIf(String::isNotBlank))
    }

    suspend fun verificationAssets(): List<HostVerificationAsset> = items(api.verificationAssets(), "assets", "items", "data")
        .mapNotNull(::verificationAssetFrom)
        .distinctBy { it.id }

    suspend fun applyForHost(
        realName: String,
        bio: String?,
        country: String?,
        languages: List<String>,
        categories: List<String>,
        experience: String?,
    ): Pair<HostProfile?, HostEligibility?> {
        val cleanName = realName.trim()
        require(cleanName.isNotBlank()) { "Your real name is required for Host verification." }
        val assets = verificationAssets()
        fun firstAsset(vararg tokens: String) = assets.firstOrNull { asset -> tokens.any { it in asset.type.uppercase() } }?.id
        val governmentId = firstAsset("GOVERNMENT", "IDENTITY", "ID_DOCUMENT")
        val selfieId = firstAsset("SELFIE", "PROFILE_PHOTO", "VERIFICATION_PHOTO")
        val supporting = assets.filter { asset -> listOf("SUPPORT", "DOCUMENT").any { it in asset.type.uppercase() } && asset.id != governmentId }.map { it.id }.distinct()
        val body = mapOf(
            "realName" to cleanName.take(160),
            "governmentIdAssetId" to governmentId,
            "selfieAssetId" to selfieId,
            "supportingDocumentAssetIds" to supporting.takeIf(List<String>::isNotEmpty),
            "bio" to bio?.trim()?.takeIf(String::isNotBlank),
            "languages" to languages.map(String::trim).filter(String::isNotBlank).distinct().takeIf(List<String>::isNotEmpty),
            "categories" to categories.map(String::trim).filter(String::isNotBlank).distinct().takeIf(List<String>::isNotEmpty),
            "country" to country?.trim()?.takeIf(String::isNotBlank),
            "experience" to experience?.trim()?.takeIf(String::isNotBlank),
        ).filterValues { it != null }
        api.applyForHost(body)
        return hostAccess()
    }

    suspend fun uploadVerificationAsset(
        kind: HostVerificationAssetKind,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): List<HostVerificationAsset> {
        require(bytes.isNotEmpty()) { "Choose a non-empty verification file." }
        val part = verificationPart(bytes, fileName, mimeType)
        when (kind) {
            HostVerificationAssetKind.GOVERNMENT_ID -> api.uploadGovernmentId(part)
            HostVerificationAssetKind.PROFILE_PHOTO -> api.uploadProfilePhoto(part)
            HostVerificationAssetKind.SUPPORTING_DOCUMENT -> api.uploadVerificationDocument(part)
        }
        return verificationAssets()
    }

    suspend fun replaceVerificationAsset(
        assetId: String,
        kind: HostVerificationAssetKind,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): List<HostVerificationAsset> {
        require(assetId.trim().isNotBlank()) { "Verification asset is unavailable." }
        val part = verificationPart(bytes, fileName, mimeType)
        val uploaded = when (kind) {
            HostVerificationAssetKind.GOVERNMENT_ID -> api.uploadGovernmentId(part)
            HostVerificationAssetKind.PROFILE_PHOTO -> api.uploadProfilePhoto(part)
            HostVerificationAssetKind.SUPPORTING_DOCUMENT -> api.uploadVerificationDocument(part)
        }
        val replacementId = findAssetId(uploaded)
        check(replacementId.isNotBlank()) { "VoiceCloud did not return a verification asset reference." }
        api.replaceVerificationAsset(assetId.trim(), mapOf("replacementAssetId" to replacementId))
        return verificationAssets()
    }

    suspend fun rooms(): List<HostRoom> = api.myRooms(limit = 100).data.distinctBy { it.id }
    suspend fun room(id: String): HostRoom = api.room(id)
    suspend fun createRoom(input: RoomEditorInput): HostRoom = api.createRoom(input.normalized())
    suspend fun updateRoom(id: String, input: RoomEditorInput): HostRoom = api.updateRoom(id, input.normalized())
    suspend fun deleteRoom(id: String) { api.deleteRoom(id) }
    suspend fun startRoom(id: String): HostRoom = api.startRoom(id)
    suspend fun pauseRoom(id: String): HostRoom = api.pauseRoom(id)
    suspend fun resumeRoom(id: String): HostRoom = api.resumeRoom(id)
    suspend fun endRoom(id: String): HostRoom = api.endRoom(id)

    suspend fun schedules(hostId: String): List<ScheduledHostRoom> = api.schedules(hostId = hostId, limit = 100).data.distinctBy { it.id }
    suspend fun schedule(id: String): ScheduledHostRoom = api.schedule(id)
    suspend fun createSchedule(input: ScheduledRoomInput): ScheduledHostRoom = api.createSchedule(input.normalized())
    suspend fun updateSchedule(id: String, input: ScheduledRoomInput): ScheduledHostRoom = api.updateSchedule(id, input.normalized())
    suspend fun deleteSchedule(id: String) { api.deleteSchedule(id) }

    suspend fun startScheduled(schedule: ScheduledHostRoom): HostRoom {
        val existing = rooms().firstOrNull { it.scheduledRoomId == schedule.id }
        val liveRoom = existing ?: createRoom(
            RoomEditorInput(
                title = schedule.title,
                description = schedule.description,
                category = schedule.category,
                language = schedule.language,
                isPrivate = schedule.visibility.uppercase() != "PUBLIC",
                isInviteOnly = schedule.isInviteOnly,
                isPremium = schedule.isPremium,
                isTicketRequired = schedule.isPremium,
                ticketPriceAmount = schedule.ticketPriceAmount.asMoney(),
                scheduledRoomId = schedule.id,
                clubId = schedule.clubId,
            )
        )
        return startRoom(liveRoom.id)
    }

    suspend fun joinHostRtc(roomId: String): HostRtcJoinResult = api.joinHostRoom(
        JoinHostRoomBody(roomId = roomId, deviceInfo = "${VoiceCloudBrand.slug}-android-host")
    )
    suspend fun leaveHostRtc(roomId: String) { runCatching { api.leaveHostRoom(LeaveHostRoomBody(roomId)) } }
    suspend fun stage(roomId: String): RoomStageState = api.stage(roomId).deduplicated()
    suspend fun approve(roomId: String, userId: String, seatIndex: Int? = null) = api.approveSpeaker(roomId, SpeakerActionBody(userId, seatIndex))
    suspend fun reject(roomId: String, userId: String) = api.rejectSpeaker(roomId, SpeakerActionBody(userId))
    suspend fun inviteSpeaker(roomId: String, userId: String) = api.inviteSpeaker(roomId, SpeakerActionBody(userId))
    suspend fun removeSpeaker(roomId: String, userId: String) = api.removeSpeaker(roomId, SpeakerActionBody(userId))
    suspend fun mute(roomId: String, userId: String, mute: Boolean) = api.muteSpeaker(roomId, MuteUserBody(userId, mute))
    suspend fun lockSeat(roomId: String, seatIndex: Int, lock: Boolean) = api.lockSeat(roomId, LockSeatBody(seatIndex, lock))

    suspend fun searchInviteCandidates(query: String, selfId: String?): List<InviteCandidate> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        return api.searchUsers(q).results.users?.items.orEmpty()
            .asSequence()
            .filter { it.id.isNotBlank() && it.id != selfId && !it.isGuest }
            .filter { it.role?.uppercase() in setOf("USER", "CREATOR") }
            .distinctBy { it.id }
            .take(12)
            .toList()
    }

    suspend fun inviteParticipant(roomId: String, userId: String) {
        realtime.connect()
        if (realtime.state.value !is RealtimeConnectionState.Authenticated) {
            withTimeoutOrNull(4_000) { realtime.state.first { it is RealtimeConnectionState.Authenticated } }
        }
        check(realtime.state.value is RealtimeConnectionState.Authenticated) { "Realtime connection is unavailable." }
        realtime.emit(
            "invite_participant",
            JSONObject().put("roomId", roomId).put("targetUserId", userId),
        )
    }

    suspend fun polls(roomId: String): List<RoomPoll> = api.polls(roomId).distinctBy { it.id }
    suspend fun createPoll(body: CreatePollBody): RoomPoll = api.createPoll(body.copy(
        title = body.title.trim(),
        options = body.options.map(String::trim).filter(String::isNotBlank),
        pollType = body.pollType.uppercase(),
        durationSeconds = body.durationSeconds?.coerceAtLeast(10),
    ))
    suspend fun startPoll(id: String) = api.startPoll(id)
    suspend fun stopPoll(id: String) = api.stopPoll(id)
    suspend fun deletePoll(id: String) { api.deletePoll(id) }

    suspend fun activeQuiz(roomId: String): RoomQuiz? = runCatching { api.activeQuiz(roomId) }.getOrNull()
    suspend fun createQuiz(body: CreateQuizBody): RoomQuiz = api.createQuiz(body)
    suspend fun startQuiz(id: String) = api.startQuiz(id)
    suspend fun nextQuizRound(id: String) = api.nextQuizRound(id)
    suspend fun stopQuiz(id: String) = api.stopQuiz(id)


    private fun verificationPart(bytes: ByteArray, fileName: String, mimeType: String): MultipartBody.Part {
        val safeName = fileName.substringAfterLast('/').substringAfterLast('\\').replace(Regex("[^A-Za-z0-9._-]"), "_").take(120).ifBlank { "verification-file" }
        val body = bytes.toRequestBody(mimeType.ifBlank { "application/octet-stream" }.toMediaTypeOrNull())
        // The current backend controller consumes one UploadedFile part; workstation/real-backend acceptance validates the multipart field.
        return MultipartBody.Part.createFormData("file", safeName, body)
    }

    private fun verificationAssetFrom(raw: Map<*, *>): HostVerificationAsset? {
        val id = raw.string("id", "assetId", "verificationAssetId")
        if (id.isBlank()) return null
        return HostVerificationAsset(
            id = id,
            type = raw.string("type", "assetType", "category").uppercase(),
            status = raw.string("status", "verificationStatus").uppercase(),
            fileName = raw.string("fileName", "originalName", "name").takeIf(String::isNotBlank),
            mimeType = raw.string("mimeType", "contentType").takeIf(String::isNotBlank),
            rejectionReason = raw.string("rejectionReason", "reason").takeIf(String::isNotBlank),
            createdAt = raw.string("createdAt", "uploadedAt").takeIf(String::isNotBlank),
        )
    }

    private fun findAssetId(value: Any?): String {
        val raw = unwrap(value, "asset", "data", "verificationAsset")
        return raw.string("id", "assetId", "verificationAssetId")
    }

    private fun unwrap(value: Any?, vararg keys: String): Map<*, *> {
        val root = value as? Map<*, *> ?: return emptyMap<Any?, Any?>()
        for (key in keys) (root.value(key) as? Map<*, *>)?.let { return it }
        return root
    }

    private fun items(value: Any?, vararg keys: String): List<Map<*, *>> {
        if (value is List<*>) return value.mapNotNull { it as? Map<*, *> }
        val root = value as? Map<*, *> ?: return emptyList()
        for (key in (keys.toList() + listOf("items", "data", "assets")).distinct()) {
            val nested = root.value(key)
            if (nested is List<*>) return nested.mapNotNull { it as? Map<*, *> }
            if (nested is Map<*, *>) {
                for (child in listOf("items", "data", "assets")) {
                    val list = nested.value(child)
                    if (list is List<*>) return list.mapNotNull { it as? Map<*, *> }
                }
            }
        }
        return emptyList()
    }

    private fun Map<*, *>.value(vararg keys: String): Any? {
        for (key in keys) entries.firstOrNull { it.key?.toString()?.equals(key, true) == true }?.value?.let { return it }
        return null
    }
    private fun Map<*, *>.string(vararg keys: String): String = value(*keys)?.toString()?.trim().orEmpty()
    private fun humanize(value: String): String = value.replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
        .replace('_', ' ').replace('-', ' ').trim().split(Regex("\\s+")).filter(String::isNotBlank)
        .joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }

    fun userFacingError(error: Throwable, fallback: String): String {
        val raw = if (error is HttpException) {
            runCatching { error.response()?.errorBody()?.string() }.getOrNull().orEmpty().ifBlank { error.message() }
        } else error.message.orEmpty()
        val normalized = raw.lowercase()
        return when {
            error is HttpException && error.code() == 401 -> "Your Session Has Expired. Sign In Again."
            error is HttpException && error.code() == 403 -> "This Room Action Isn’t Available For Your Account."
            error is HttpException && error.code() == 429 -> "Too Many Requests. Try Again In A Moment."
            error is HttpException && error.code() >= 500 -> "Live Audio Is Temporarily Unavailable. Try Again Soon."
            "approved host" in normalized || "host verification" in normalized -> "Host Approval Is Required For This Action."
            "not found" in normalized -> "This Room Is No Longer Available."
            "forbidden" in normalized || "permission" in normalized -> "This Room Action Isn’t Available For Your Account."
            "rtc" in normalized || "livekit" in normalized || "provider" in normalized || "credential" in normalized -> "Live Audio Is Temporarily Unavailable. Try Again Soon."
            error is HttpException -> fallback
            listOf("sql", "postgres", "typeorm", "constraint", "exception", "stack trace", "relation ", "column ").any { it in normalized } -> fallback
            else -> raw.replace("{", " ").replace("}", " ").replace("\"", " ").replace("[", " ").replace("]", " ").replace(Regex("\\s+"), " ").trim().takeIf { it.length in 4..160 } ?: fallback
        }
    }

    private fun RoomEditorInput.normalized() = copy(
        title = title.trim(),
        description = description?.trim()?.takeIf(String::isNotBlank),
        category = category.trim().ifBlank { "Audio Lounge" },
        language = language.trim().ifBlank { "en" },
        isInviteOnly = isInviteOnly || isPrivate,
        isTicketRequired = isTicketRequired || isPremium,
        ticketPriceAmount = if (isPremium || isTicketRequired) ticketPriceAmount.coerceAtLeast(0.0) else 0.0,
        scheduledRoomId = scheduledRoomId?.trim()?.takeIf(String::isNotBlank),
        clubId = clubId?.trim()?.takeIf(String::isNotBlank),
    )

    private fun ScheduledRoomInput.normalized(): ScheduledRoomInput {
        require(runCatching { OffsetDateTime.parse(scheduledStartTime) }.isSuccess) { "Invalid scheduled time." }
        return copy(
            title = title.trim(),
            description = description?.trim()?.takeIf(String::isNotBlank),
            category = category.trim().ifBlank { "General" },
            language = language.trim().ifBlank { "en" },
            scheduledStartTime = OffsetDateTime.parse(scheduledStartTime).toString(),
            durationMinutes = durationMinutes.coerceIn(15, 1440),
            timeZone = timeZone.trim().ifBlank { "UTC" },
            visibility = visibility.uppercase().let { if (it == "PRIVATE") "PRIVATE" else "PUBLIC" },
            ticketPriceAmount = if (isPremium) ticketPriceAmount.coerceAtLeast(0.0) else 0.0,
            clubId = clubId?.trim()?.takeIf(String::isNotBlank),
        )
    }

    private fun RoomStageState.deduplicated() = copy(
        handQueue = handQueue.filter { it.userId.isNotBlank() }.distinctBy { it.userId },
        speakers = speakers.filter { it.userId.isNotBlank() }.distinctBy { it.userId },
        participants = participants.filter { it.userId.isNotBlank() }.distinctBy { it.userId },
    )

    private fun Any?.asMoney(): Double = when (this) {
        is Number -> toDouble()
        is String -> toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}
