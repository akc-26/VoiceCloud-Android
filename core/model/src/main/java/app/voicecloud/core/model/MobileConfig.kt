package app.voicecloud.core.model

data class AppVersionPolicy(
    val configured: Boolean = true,
    val latestVersion: String? = null,
    val minSupportedVersion: String? = null,
    val forceUpdate: Boolean = false,
    val releaseNotes: String? = null,
    val downloadUrl: String? = null,
)

data class WalletSettings(val coinExchangeRate: Double = 100.0, val minRechargeAmount: Double = 1.0, val maxRechargeAmount: Double = 10000.0)
data class GiftSettings(val enableGiftEffects: Boolean = true, val maxBatchQuantity: Int = 999)
data class VipSettings(val dailyLoginBonus: Int = 50)
data class HostSettings(val applicationsEnabled: Boolean = true, val minFollowersRequired: Int = 50, val minCompletedRoomsRequired: Int = 3, val requireGoodStanding: Boolean = true)
data class AgencySettings(val commissionPct: Double = 10.0)
data class PushNotificationSettings(val enabled: Boolean = true)
data class MediaUploadLimits(val maxFileSizeMb: Int = 50, val allowedImageTypes: List<String> = emptyList(), val allowedAudioTypes: List<String> = emptyList())
data class AndroidBillingConfig(
    val provider: String = "google_play",
    val walletOneTimeProducts: Boolean = false,
    val vipSubscriptionCatalog: String? = null,
    val creatorSubscriptionCatalogTemplate: String? = null,
    val vipVerificationEndpoint: String? = null,
    val creatorVerificationEndpoint: String? = null,
    val rtdnReconciliation: Boolean = false,
    val agencySupported: Boolean = false,
)
data class PasswordRecoveryConfig(val httpsAppLinkPath: String? = null, val androidAppLinkCompatible: Boolean = false)
data class CoinConfiguration(
    val id: String,
    val name: String,
    val coins: Int,
    val bonusCoins: Int = 0,
    val price: Double,
    val currency: String,
    val isPopular: Boolean = false,
    val badgeText: String? = null,
    val googlePlayProductId: String? = null,
    val appleProductId: String? = null,
)
data class ProviderCapability(
    val id: String? = null,
    val providerType: String? = null,
    val name: String? = null,
    val isActive: Boolean = false,
    val isSandbox: Boolean = false,
    val priority: Int? = null,
    val healthStatus: String? = null,
)

data class MobileConfig(
    val appVersion: AppVersionPolicy? = null,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "System under maintenance",
    val featureFlags: Map<String, Boolean> = emptyMap(),
    val walletSettings: WalletSettings = WalletSettings(),
    val giftSettings: GiftSettings = GiftSettings(),
    val vipSettings: VipSettings = VipSettings(),
    val hostSettings: HostSettings = HostSettings(),
    val agencySettings: AgencySettings = AgencySettings(),
    val supportedLoginMethods: List<String> = emptyList(),
    val supportedLanguages: List<String> = listOf("en"),
    val availableRtcProviders: List<ProviderCapability> = emptyList(),
    val pushNotificationSettings: PushNotificationSettings = PushNotificationSettings(),
    val mediaUploadLimits: MediaUploadLimits = MediaUploadLimits(),
    val maxRoomCapacity: Int = 500,
    val maxSpeakerSeats: Int = 12,
    val supportedAudioProfiles: List<String> = emptyList(),
    val androidBilling: AndroidBillingConfig = AndroidBillingConfig(),
    val passwordRecovery: PasswordRecoveryConfig = PasswordRecoveryConfig(),
    val coinConfigurations: List<CoinConfiguration> = emptyList(),
    val timestamp: String? = null,
)
