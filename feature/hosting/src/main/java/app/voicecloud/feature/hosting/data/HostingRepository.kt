package app.voicecloud.feature.hosting.data

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
import app.voicecloud.core.realtime.RealtimeClient
import app.voicecloud.core.realtime.RealtimeConnectionState
import app.voicecloud.feature.hosting.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
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

    fun userFacingError(error: Throwable, fallback: String): String {
        val raw = if (error is HttpException) {
            runCatching { error.response()?.errorBody()?.string() }.getOrNull().orEmpty().ifBlank { error.message() }
        } else error.message.orEmpty()
        val normalized = raw.lowercase()
        return when {
            "approved host" in normalized || "host verification" in normalized -> "An approved Host profile is required for this action."
            "not found" in normalized -> "The requested item is no longer available."
            "forbidden" in normalized || "permission" in normalized -> "You do not have permission to perform this action."
            "rtc" in normalized || "livekit" in normalized || "provider" in normalized -> "Live audio is temporarily unavailable. Please try again."
            else -> raw.replace(Regex("[{}\\\"\\[\\]]"), " ").replace(Regex("\\s+"), " ").trim().takeIf { it.length in 4..180 } ?: fallback
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
