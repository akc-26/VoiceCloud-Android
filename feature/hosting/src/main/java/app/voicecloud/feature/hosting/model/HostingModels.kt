package app.voicecloud.feature.hosting.model

data class HostProfile(
    val id: String = "",
    val userId: String = "",
    val status: String = "",
    val verificationBadge: Boolean = false,
    val displayName: String? = null,
    val username: String? = null,
)

data class HostNumericRequirement(val current: Int = 0, val minimum: Int = 0, val met: Boolean = false)
data class HostStandingRequirement(val required: Boolean = false, val met: Boolean = false)
data class HostEligibilityRequirements(
    val followers: HostNumericRequirement = HostNumericRequirement(),
    val completedRooms: HostNumericRequirement = HostNumericRequirement(),
    val communityStanding: HostStandingRequirement = HostStandingRequirement(),
)
data class HostEligibility(
    val eligible: Boolean = false,
    val applicationsEnabled: Boolean = false,
    val requirements: HostEligibilityRequirements = HostEligibilityRequirements(),
    val reasons: List<String> = emptyList(),
    val evaluatedAt: String? = null,
)

data class HostRoom(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val category: String = "Audio Lounge",
    val audioQuality: String = "324kbps Ultra HD",
    val language: String = "en",
    val hostId: String = "",
    val hostName: String? = null,
    val hostUsername: String? = null,
    val status: String = "offline",
    val isLive: Boolean = false,
    val isPrivate: Boolean = false,
    val isLocked: Boolean = false,
    val isPremium: Boolean = false,
    val isTicketRequired: Boolean = false,
    val isSubscriberOnly: Boolean = false,
    val isVerifiedOnly: Boolean = false,
    val isInviteOnly: Boolean = false,
    val ticketPriceAmount: Any? = null,
    val currency: String? = "USD",
    val clubId: String? = null,
    val scheduledRoomId: String? = null,
    val listenerCount: Int = 0,
    val speakerCount: Int = 0,
    val participantCount: Int = 0,
    val startedAt: String? = null,
    val createdAt: String? = null,
)

data class HostRoomPage(
    val data: List<HostRoom> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 100,
    val totalPages: Int = 1,
)

data class RoomEditorInput(
    val title: String,
    val description: String? = null,
    val category: String = "Audio Lounge",
    val language: String = "en",
    val isPrivate: Boolean = false,
    val isLocked: Boolean = false,
    val isInviteOnly: Boolean = false,
    val isPremium: Boolean = false,
    val isTicketRequired: Boolean = false,
    val ticketPriceAmount: Double = 0.0,
    val isSubscriberOnly: Boolean = false,
    val isVerifiedOnly: Boolean = false,
    val scheduledRoomId: String? = null,
    val clubId: String? = null,
)

data class ScheduledHostRoom(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val category: String = "General",
    val language: String = "en",
    val hostId: String = "",
    val scheduledStartTime: String = "",
    val durationMinutes: Int = 60,
    val timeZone: String = "UTC",
    val visibility: String = "PUBLIC",
    val isInviteOnly: Boolean = false,
    val isPremium: Boolean = false,
    val ticketPriceAmount: Any? = null,
    val currency: String = "USD",
    val status: String = "SCHEDULED",
    val clubId: String? = null,
)

data class ScheduledRoomPage(
    val data: List<ScheduledHostRoom> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 100,
    val totalPages: Int = 1,
)

data class ScheduledRoomInput(
    val title: String,
    val description: String? = null,
    val category: String = "General",
    val language: String = "en",
    val scheduledStartTime: String,
    val durationMinutes: Int = 60,
    val timeZone: String,
    val visibility: String = "PUBLIC",
    val isInviteOnly: Boolean = false,
    val isPremium: Boolean = false,
    val ticketPriceAmount: Double = 0.0,
    val currency: String = "USD",
    val clubId: String? = null,
)

data class StageHand(
    val userId: String = "",
    val seatIndex: Int? = null,
    val timestamp: Long? = null,
)

data class StageSpeaker(
    val userId: String = "",
    val username: String? = null,
    val isMuted: Boolean = false,
    val role: String? = null,
    val joinedStageAt: String? = null,
)

data class StageParticipant(
    val userId: String = "",
    val roomId: String? = null,
    val role: String? = null,
    val status: String? = null,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val handRaised: Boolean = false,
    val username: String? = null,
)

data class ActiveRtcSession(
    val id: String = "",
    val status: String = "",
    val concurrentUsers: Int? = null,
    val peakAudience: Int? = null,
)

data class RoomStageState(
    val roomId: String = "",
    val handQueue: List<StageHand> = emptyList(),
    val speakers: List<StageSpeaker> = emptyList(),
    val participants: List<StageParticipant> = emptyList(),
    val activeSession: ActiveRtcSession? = null,
)

data class SpeakerActionBody(val targetUserId: String, val seatIndex: Int? = null)
data class MuteUserBody(val targetUserId: String, val mute: Boolean)
data class LockSeatBody(val seatIndex: Int, val lock: Boolean)
data class JoinHostRoomBody(val roomId: String, val role: String = "host", val deviceInfo: String = "android")
data class LeaveHostRoomBody(val roomId: String)
data class HostRtcJoinResult(
    val roomId: String = "",
    val userId: String = "",
    val role: String = "host",
    val token: String = "",
    val provider: String? = null,
    val serverUrl: String? = null,
    val expiresAt: String = "",
)

data class PollOption(
    val id: String = "",
    val text: String = "",
    val optionText: String? = null,
    val voteCount: Int = 0,
)

data class RoomPoll(
    val id: String = "",
    val roomId: String = "",
    val title: String = "",
    val pollType: String = "SINGLE",
    val status: String = "DRAFT",
    val options: List<PollOption> = emptyList(),
    val durationSeconds: Int? = null,
)

data class CreatePollBody(
    val roomId: String,
    val title: String,
    val pollType: String = "SINGLE",
    val options: List<String>,
    val durationSeconds: Int? = null,
)

data class QuizQuestionInput(
    val roundNumber: Int = 1,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val durationSeconds: Int = 30,
    val points: Int = 100,
)

data class CreateQuizBody(
    val roomId: String,
    val title: String,
    val description: String? = null,
    val totalRounds: Int = 1,
    val questions: List<QuizQuestionInput>,
)

data class RoomQuiz(
    val id: String = "",
    val roomId: String = "",
    val title: String = "",
    val description: String? = null,
    val status: String = "DRAFT",
    val currentRound: Int? = null,
    val totalRounds: Int = 1,
)


data class InviteCandidate(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val role: String? = null,
    val isGuest: Boolean = false,
    val avatarUrl: String? = null,
)

data class InviteCandidatePage(
    val items: List<InviteCandidate> = emptyList(),
    val total: Int = 0,
)

data class InviteSearchResults(val users: InviteCandidatePage? = null)
data class InviteSearchResponse(
    val query: String = "",
    val type: String? = null,
    val results: InviteSearchResults = InviteSearchResults(),
)

data class HostingUiState(
    val loading: Boolean = false,
    val eligibilityChecked: Boolean = false,
    val hostProfile: HostProfile? = null,
    val eligibility: HostEligibility? = null,
    val rooms: List<HostRoom> = emptyList(),
    val schedules: List<ScheduledHostRoom> = emptyList(),
    val selectedRoom: HostRoom? = null,
    val selectedSchedule: ScheduledHostRoom? = null,
    val stage: RoomStageState? = null,
    val polls: List<RoomPoll> = emptyList(),
    val activeQuiz: RoomQuiz? = null,
    val inviteCandidates: List<InviteCandidate> = emptyList(),
    val inviteQuery: String = "",
    val rtcConnected: Boolean = false,
    val microphoneEnabled: Boolean = false,
    val microphoneBusy: Boolean = false,
    val notice: String? = null,
    val error: String? = null,
)
