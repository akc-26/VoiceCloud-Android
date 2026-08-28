package app.voicecloud.feature.creator.model

import androidx.compose.runtime.Immutable

@Immutable
data class CreatorDashboardMetric(val key: String, val label: String, val value: String)

@Immutable
data class CreatorDashboard(
    val metrics: List<CreatorDashboardMetric> = emptyList(),
    val generatedAt: String? = null,
)

@Immutable
data class CreatorProfile(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val country: String? = null,
    val preferredLanguage: String = "en",
    val interests: List<String> = emptyList(),
    val role: String = "CREATOR",
)

@Immutable
data class CreatorNotificationPreferences(
    val email: Boolean = false,
    val push: Boolean = true,
    val inApp: Boolean = true,
    val sound: Boolean = true,
)

@Immutable
data class CreatorSettings(
    val notifications: CreatorNotificationPreferences = CreatorNotificationPreferences(),
    val language: String = "en",
    val timezone: String = "",
)

@Immutable
data class CreatorCmsPageSummary(
    val slug: String = "",
    val title: String = "",
    val excerpt: String? = null,
    val sortOrder: Int = 0,
)

@Immutable
data class CreatorCmsPage(
    val slug: String = "",
    val title: String = "",
    val content: String = "",
    val updatedAt: String? = null,
)

data class CreatorContactRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val description: String,
)

@Immutable
data class CreatorMaintenance(val enabled: Boolean = false, val message: String? = null)

@Immutable
data class CreatorUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val dashboard: CreatorDashboard? = null,
    val profile: CreatorProfile? = null,
    val settings: CreatorSettings? = null,
    val cmsPages: List<CreatorCmsPageSummary> = emptyList(),
    val cmsPage: CreatorCmsPage? = null,
    val error: String? = null,
    val notice: String? = null,
)

enum class CreatorPortalSection { DASHBOARD, PROFILE, SETTINGS, HELP }

sealed interface CreatorEvent {
    data class AuthFailure(val httpStatus: Int, val message: String? = null) : CreatorEvent
}
