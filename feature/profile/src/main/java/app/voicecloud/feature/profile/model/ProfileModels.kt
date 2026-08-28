package app.voicecloud.feature.profile.model

data class ExtendedProfile(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val bio: String? = null,
    val statusMessage: String? = null,
    val country: String? = null,
    val preferredLanguage: String? = null,
    val interests: List<String>? = null,
    val customTags: List<String>? = null,
    val profileCompletionPercentage: Int = 0,
)

data class UpdateExtendedProfileBody(
    val bio: String? = null,
    val country: String? = null,
    val preferredLanguage: String? = null,
    val interests: List<String>? = null,
)

data class ReplayItem(
    val id: String = "",
    val roomId: String = "",
    val title: String = "Replay",
    val roomTitle: String? = null,
    val hostName: String? = null,
    val coverUrl: String? = null,
    val playbackUrl: String? = null,
    val status: String = "",
    val durationSeconds: Long = 0,
    val createdAt: String? = null,
    val accessAllowed: Boolean? = null,
    val accessReason: String? = null,
)

data class ActivityItem(
    val id: String = "",
    val roomId: String = "",
    val type: String = "ROOM",
    val title: String = "Room activity",
    val roomTitle: String? = null,
    val hostName: String? = null,
    val joinedAt: String? = null,
    val leftAt: String? = null,
    val durationSeconds: Long = 0,
    val createdAt: String? = null,
)

data class PersonSummary(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
)

data class BlockedUserItem(
    val id: String = "",
    val blockedUserId: String = "",
    val person: PersonSummary = PersonSummary(),
    val createdAt: String? = null,
)

data class ProfileVisitorItem(
    val id: String = "",
    val visitorUserId: String = "",
    val person: PersonSummary = PersonSummary(),
    val visitedAt: String? = null,
)

data class HelpPageSummary(
    val id: String = "",
    val slug: String = "",
    val title: String = "",
    val excerpt: String? = null,
    val category: String? = null,
    val sortOrder: Int = 0,
)

data class HelpPageDetail(
    val id: String = "",
    val slug: String = "",
    val title: String = "",
    val content: String = "",
    val updatedAt: String? = null,
)

data class VisitorStats(
    val total: Int = 0,
    val today: Int = 0,
    val thisWeek: Int = 0,
    val thisMonth: Int = 0,
)

data class ProfileUiState(
    val loading: Boolean = false,
    val mutating: Boolean = false,
    val profile: ExtendedProfile? = null,
    val replays: List<ReplayItem> = emptyList(),
    val selectedReplay: ReplayItem? = null,
    val activity: List<ActivityItem> = emptyList(),
    val blockedUsers: List<BlockedUserItem> = emptyList(),
    val visitors: List<ProfileVisitorItem> = emptyList(),
    val visitorStats: VisitorStats = VisitorStats(),
    val helpPages: List<HelpPageSummary> = emptyList(),
    val selectedHelpPage: HelpPageDetail? = null,
    val notice: String? = null,
    val error: String? = null,
)
