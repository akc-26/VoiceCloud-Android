package app.voicecloud.core.model

/** R06 account role returned by `/auth/me` and login responses. */
enum class VoiceCloudAccountRole {
    GUEST,
    USER,
    CREATOR,
    ADMIN,
    SUPER_ADMIN,
    UNKNOWN,
    ;

    companion object {
        fun fromBackend(value: String?): VoiceCloudAccountRole = when (value?.uppercase()) {
            "GUEST" -> GUEST
            "USER" -> USER
            "CREATOR" -> CREATOR
            "ADMIN" -> ADMIN
            "SUPER_ADMIN" -> SUPER_ADMIN
            else -> UNKNOWN
        }
    }

    fun canAccessUserPortal(): Boolean = this == GUEST || this == USER || this == CREATOR

    fun canAccessCreatorPortal(): Boolean = this == CREATOR
}

data class VoiceCloudUser(
    val id: String,
    val username: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val isRestricted: Boolean = false,
    val isSuspended: Boolean = false,
) {
    val accountRole: VoiceCloudAccountRole get() = VoiceCloudAccountRole.fromBackend(role)
}

data class AuthTokenBundle(
    val accessToken: String,
    val refreshToken: String,
    val user: VoiceCloudUser,
)

data class RefreshTokenRequest(val refreshToken: String)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

data class LoginRequest(
    val email: String? = null,
    val username: String? = null,
    val password: String,
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val displayName: String? = null,
)

data class LogoutRequest(val refreshToken: String? = null)

data class ForgotPasswordRequest(val email: String)

data class ResetPasswordRequest(val token: String, val password: String)

data class PhoneSendOtpRequest(val phone: String)

data class PhoneLoginRequest(val phone: String, val code: String)

data class GoogleLoginRequest(val idToken: String)

data class GuestLoginRequest(val deviceId: String? = null)

data class GuestUpgradeRequest(
    val email: String? = null,
    val username: String? = null,
    val password: String,
)
