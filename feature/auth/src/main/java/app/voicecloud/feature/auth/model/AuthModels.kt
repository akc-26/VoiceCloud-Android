package app.voicecloud.feature.auth.model

/** Backend role authority. Host status is independent and is not inferred from these values. */
enum class VoiceCloudRole { GUEST, USER, CREATOR, ADMIN, SUPER_ADMIN, UNKNOWN;
    companion object {
        fun from(raw: String?): VoiceCloudRole = entries.firstOrNull { it.name == raw?.trim()?.uppercase() } ?: UNKNOWN
    }
}

data class AuthUser(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val isVip: Boolean = false,
    val isGuest: Boolean = false,
    val role: String = "USER",
    val referralCode: String? = null,
    val profileCompletion: Int? = null,
    val bio: String? = null,
    val country: String? = null,
    val preferredLanguage: String? = null,
    val interests: List<String> = emptyList(),
    val status: String? = null,
    val accountStatus: String? = null,
) {
    val normalizedRole: VoiceCloudRole get() = VoiceCloudRole.from(role)
}


data class FirebaseClientConfig(
    val apiKey: String = "",
    val applicationId: String = "",
    val projectId: String = "",
    val googleWebClientId: String = "",
) {
    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && applicationId.isNotBlank() && projectId.isNotBlank() && googleWebClientId.isNotBlank()
}

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Int = 0,
    val user: AuthUser,
    val sessionId: String? = null,
    val deviceId: String? = null,
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Int = 0,
)

data class OtpResponse(
    val message: String,
    val expiresAt: String? = null,
    val resendCooldownSeconds: Int? = null,
    /** Present only in explicitly enabled backend development/test environments. */
    val otpCode: String? = null,
)

data class MessageResponse(val message: String = "")
data class ApiMutationResponse(val id: String? = null, val message: String? = null, val success: Boolean? = null)

data class DeviceMetadata(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String = "mobile",
    val osVersion: String,
    val appVersion: String,
    val manufacturer: String,
    val model: String,
)

data class LoginRequest(val email: String? = null, val username: String? = null, val password: String)
data class RegisterRequest(val username: String, val displayName: String, val email: String, val password: String)
data class SendOtpRequest(val phoneNumber: String)
data class PhoneLoginRequest(
    val phoneNumber: String,
    val otpCode: String? = null,
    val firebaseIdToken: String? = null,
    val referralCode: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val deviceType: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
)
data class GoogleLoginRequest(
    val idToken: String,
    val referralCode: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val deviceType: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null,
)
data class GuestLoginRequest(
    val referralCode: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val deviceType: String? = null,
)
data class GuestUpgradeRequest(
    val method: String,
    val phoneNumber: String? = null,
    val otpCode: String? = null,
    val firebaseIdToken: String? = null,
    val googleIdToken: String? = null,
    val email: String? = null,
    val password: String? = null,
    val displayName: String? = null,
)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val token: String, val newPassword: String)
data class RefreshTokenRequest(val refreshToken: String)

data class UpdateProfileRequest(
    val bio: String? = null,
    val country: String? = null,
    val preferredLanguage: String? = null,
    val interests: List<String>? = null,
)
data class NotificationPreferences(val email: Boolean = false, val push: Boolean = true, val inApp: Boolean = true)
data class UpdateSettingsRequest(
    val notificationPreferences: NotificationPreferences? = null,
    val language: String? = null,
    val timezone: String? = null,
)
data class RegisterDeviceRequest(
    val deviceId: String,
    val platform: String = "android",
    val deviceType: String = "mobile",
    val deviceName: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null,
    val pushToken: String? = null,
)

data class CreatorAccessApplicationRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val country: String,
    val creatorCategory: String? = null,
    val experienceYears: Int = 0,
    val currentAudience: String? = null,
    val experience: String,
    val motivation: String,
    val portfolioUrl: String? = null,
    val socialLinks: Map<String, String>? = null,
)

data class CreatorAccessApplicationResponse(
    val id: String? = null,
    val status: String? = null,
    val message: String? = null,
)

sealed interface AuthLanding {
    data object PortalSelector : AuthLanding
    data class User(val user: AuthUser, val needsOnboarding: Boolean) : AuthLanding
    data class Creator(val user: AuthUser) : AuthLanding
    data class Restricted(val message: String) : AuthLanding
    data class Maintenance(val message: String) : AuthLanding
    data object SessionExpired : AuthLanding
}
