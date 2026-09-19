package app.voicecloud.feature.creator.model

import androidx.compose.runtime.Immutable
import app.voicecloud.feature.discovery.model.VoiceCloudUser

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
    val phoneNumber: String? = null,
    val message: String,
)


@Immutable
data class CreatorFollower(
    val user: VoiceCloudUser,
    val followingBack: Boolean = false,
)

@Immutable
data class CreatorAudience(
    val followerTotal: Int = 0,
    val followingTotal: Int = 0,
    val subscriberTotal: Int? = null,
    val listeners: String? = null,
)

@Immutable
data class CreatorPlan(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val monthlyPrice: Double = 0.0,
    val yearlyPrice: Double? = null,
    val benefits: List<String> = emptyList(),
    val visibility: String = "PUBLIC",
    val status: String = "DRAFT",
    val subscriberCount: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Immutable
data class CreatorSubscriber(
    val id: String = "",
    val userId: String = "",
    val planId: String = "",
    val status: String = "",
    val displayName: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val planTitle: String? = null,
    val subscribedAt: String? = null,
    val expiresAt: String? = null,
    val autoRenew: Boolean? = null,
)

@Immutable
data class CreatorSubscriberPage(
    val items: List<CreatorSubscriber> = emptyList(),
    val total: Int? = null,
    val page: Int = 1,
    val limit: Int = 50,
    val totalPages: Int? = null,
)



@Immutable
data class CreatorMetric(
    val key: String,
    val label: String,
    val value: String,
)

@Immutable
data class CreatorAnalytics(
    val metrics: List<CreatorMetric> = emptyList(),
    val generatedAt: String? = null,
)

@Immutable
data class CreatorBalance(
    val type: String,
    val amount: String,
)

@Immutable
data class CreatorWalletTransaction(
    val id: String,
    val type: String = "",
    val status: String = "",
    val amount: String = "",
    val currency: String = "",
    val description: String? = null,
    val createdAt: String? = null,
)

@Immutable
data class CreatorWallet(
    val balances: List<CreatorBalance> = emptyList(),
    val summary: List<CreatorMetric> = emptyList(),
    val transactions: List<CreatorWalletTransaction> = emptyList(),
)

@Immutable
data class CreatorEarnings(
    val metrics: List<CreatorMetric> = emptyList(),
    val generatedAt: String? = null,
)

@Immutable
data class CreatorGiftRecord(
    val id: String,
    val giftName: String = "Gift",
    val direction: String = "",
    val counterparty: String? = null,
    val quantity: Int? = null,
    val amount: String? = null,
    val currency: String? = null,
    val createdAt: String? = null,
)

@Immutable
data class CreatorPayoutRequest(
    val id: String,
    val diamondAmount: String = "",
    val payoutMethod: String = "",
    val status: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val rejectionReason: String? = null,
)

@Immutable
data class CreatorPayoutPage(
    val items: List<CreatorPayoutRequest> = emptyList(),
    val total: Int? = null,
    val page: Int = 1,
    val limit: Int = 50,
    val totalPages: Int? = null,
)

enum class CreatorFollowerSort { NAME, POPULARITY, ONLINE }

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
    val audience: CreatorAudience? = null,
    val followers: List<CreatorFollower> = emptyList(),
    val followerSearch: String = "",
    val followerSort: CreatorFollowerSort = CreatorFollowerSort.NAME,
    val plans: List<CreatorPlan> = emptyList(),
    val subscribers: CreatorSubscriberPage? = null,
    val subscriberStatus: String? = null,
    val analytics: CreatorAnalytics? = null,
    val wallet: CreatorWallet? = null,
    val earnings: CreatorEarnings? = null,
    val gifts: List<CreatorGiftRecord> = emptyList(),
    val payouts: CreatorPayoutPage? = null,
    val selectedPayout: CreatorPayoutRequest? = null,
    val error: String? = null,
    val notice: String? = null,
)

enum class CreatorPortalSection { DASHBOARD, LIVE, AUDIENCE, PROFILE, SETTINGS, HELP }

sealed interface CreatorEvent {
    data class AuthFailure(val httpStatus: Int, val message: String? = null) : CreatorEvent
}
