package app.voicecloud.feature.auth.data

import app.voicecloud.core.logging.VoiceCloudLogger
import app.voicecloud.core.model.ApiError
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.preferences.LastPortal
import app.voicecloud.core.preferences.VoiceCloudPreferences
import app.voicecloud.core.realtime.RealtimeClient
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.auth.model.*
import kotlinx.coroutines.flow.first
import retrofit2.Response
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

class AuthApiException(val apiError: ApiError) : RuntimeException(apiError.message)
class CreatorRoleRequiredException : RuntimeException("This account does not have Creator access.")

@Singleton
class AuthRepository @Inject constructor(
    private val publicApi: PublicAuthApi,
    private val authenticatedApi: AuthenticatedAuthApi,
    private val tokenVault: TokenVault,
    private val preferences: VoiceCloudPreferences,
    private val deviceMetadataProvider: DeviceMetadataProvider,
    private val errorParser: SafeApiErrorParser,
    private val realtimeClient: RealtimeClient,
    private val logger: VoiceCloudLogger,
) {
    suspend fun restoreSession(): AuthLanding {
        val access = tokenVault.accessToken()?.takeIf { it.isNotBlank() }
        val refresh = tokenVault.refreshToken()?.takeIf { it.isNotBlank() }
        if (access == null && refresh == null) return AuthLanding.PortalSelector

        return try {
            val user = requireBody(authenticatedApi.me())
            val lastPortal = preferences.lastPortal.first()
            when {
                user.normalizedRole in setOf(VoiceCloudRole.ADMIN, VoiceCloudRole.SUPER_ADMIN) ->
                    AuthLanding.Restricted("Administrative accounts are not available in the VoiceCloud Android portals.")
                lastPortal == LastPortal.CREATOR && user.normalizedRole == VoiceCloudRole.CREATOR -> AuthLanding.Creator(user)
                isRestricted(user) -> AuthLanding.Restricted("VoiceCloud could not authorize this account. Follow the account instructions or try again later.")
                else -> AuthLanding.User(user, requiresOnboarding(user))
            }
        } catch (e: AuthApiException) {
            when (e.apiError.httpStatus) {
                401 -> {
                    clearLocalSession()
                    AuthLanding.SessionExpired
                }
                403 -> AuthLanding.Restricted(e.apiError.message)
                503 -> AuthLanding.Maintenance(e.apiError.message)
                else -> throw e
            }
        }
    }

    suspend fun login(identifier: String, password: String, creatorPortal: Boolean): AuthUser {
        val normalized = identifier.trim()
        require(normalized.isNotBlank()) { "Enter your email address or username." }
        require(password.isNotBlank()) { "Enter your password." }
        val request = if (creatorPortal) {
            require('@' in normalized) { "Enter a valid Creator email address." }
            LoginRequest(email = normalized.lowercase(), password = password)
        } else if ('@' in normalized) {
            LoginRequest(email = normalized.lowercase(), password = password)
        } else {
            LoginRequest(username = normalized, password = password)
        }
        val response = requireBody(publicApi.login(request))
        storeAuth(response, if (creatorPortal) LastPortal.CREATOR else LastPortal.USER)
        val user = refreshCurrentUser(response.user)
        if (!creatorPortal && user.normalizedRole in setOf(VoiceCloudRole.ADMIN, VoiceCloudRole.SUPER_ADMIN)) {
            runCatching { authenticatedApi.logout() }
            clearLocalSession()
            throw AuthApiException(ApiError(httpStatus = 403, message = "Administrative accounts are not available in the VoiceCloud Android portals."))
        }
        if (creatorPortal && user.normalizedRole != VoiceCloudRole.CREATOR) {
            runCatching { authenticatedApi.logout() }
            clearLocalSession()
            throw CreatorRoleRequiredException()
        }
        registerDeviceFoundation(pushToken = null)
        return user
    }

    suspend fun register(username: String, displayName: String, email: String, password: String): AuthUser {
        require(username.trim().isNotBlank()) { "Enter a username." }
        require(displayName.trim().isNotBlank()) { "Enter your display name." }
        require(email.trim().contains('@')) { "Enter a valid email address." }
        require(password.length >= 8) { "Password must be at least 8 characters." }
        val response = requireBody(publicApi.register(RegisterRequest(
            username = username.trim(),
            displayName = displayName.trim(),
            email = email.trim().lowercase(),
            password = password,
        )))
        storeAuth(response, LastPortal.USER)
        val user = refreshCurrentUser(response.user)
        registerDeviceFoundation(pushToken = null)
        return user
    }

    suspend fun sendPhoneOtp(phoneNumber: String): OtpResponse {
        val phone = phoneNumber.trim()
        require(E164.matches(phone)) { "Enter a phone number in international format, for example +919876543210." }
        return requireBody(publicApi.sendPhoneOtp(SendOtpRequest(phone)))
    }

    suspend fun phoneLogin(phoneNumber: String, otpCode: String, referralCode: String?): AuthUser {
        val device = deviceMetadataProvider.current()
        val code = otpCode.filter(Char::isDigit)
        require(code.length == 6) { "Enter the 6-digit verification code." }
        val response = requireBody(publicApi.phoneLogin(PhoneLoginRequest(
            phoneNumber = phoneNumber.trim(),
            otpCode = code,
            referralCode = referralCode?.trim()?.takeIf { it.isNotEmpty() },
            deviceId = device.deviceId,
            deviceName = device.deviceName,
            deviceType = device.deviceType,
            osVersion = device.osVersion,
            appVersion = device.appVersion,
            manufacturer = device.manufacturer,
            model = device.model,
        )))
        storeAuth(response, LastPortal.USER)
        val user = refreshCurrentUser(response.user)
        registerDeviceFoundation(pushToken = null)
        return user
    }

    suspend fun googleLogin(idToken: String, referralCode: String?): AuthUser {
        require(idToken.isNotBlank()) { "Google Sign-In did not return an ID token." }
        val device = deviceMetadataProvider.current()
        val response = requireBody(publicApi.googleLogin(GoogleLoginRequest(
            idToken = idToken,
            referralCode = referralCode?.trim()?.takeIf { it.isNotEmpty() },
            deviceId = device.deviceId,
            deviceName = device.deviceName,
            deviceType = device.deviceType,
            osVersion = device.osVersion,
            appVersion = device.appVersion,
        )))
        storeAuth(response, LastPortal.USER)
        val user = refreshCurrentUser(response.user)
        registerDeviceFoundation(pushToken = null)
        return user
    }

    suspend fun guestLogin(referralCode: String?): AuthUser {
        val device = deviceMetadataProvider.current()
        val response = requireBody(publicApi.guestLogin(GuestLoginRequest(
            referralCode = referralCode?.trim()?.takeIf { it.isNotEmpty() },
            deviceId = device.deviceId,
            deviceName = device.deviceName,
            deviceType = device.deviceType,
        )))
        storeAuth(response, LastPortal.USER)
        val user = refreshCurrentUser(response.user)
        registerDeviceFoundation(pushToken = null)
        return user
    }

    suspend fun upgradeGuestWithEmail(displayName: String, email: String, password: String): AuthUser {
        require(displayName.trim().length >= 2) { "Enter your display name." }
        require(email.trim().contains('@')) { "Enter a valid email address." }
        require(password.length >= 8) { "Password must be at least 8 characters." }
        val response = requireBody(authenticatedApi.upgradeGuest(GuestUpgradeRequest(
            method = "email",
            displayName = displayName.trim(),
            email = email.trim().lowercase(),
            password = password,
        )))
        storeAuth(response, LastPortal.USER)
        registerDeviceFoundation(pushToken = null)
        return refreshCurrentUser(response.user)
    }

    suspend fun upgradeGuestWithPhone(displayName: String, phoneNumber: String, otpCode: String): AuthUser {
        require(E164.matches(phoneNumber.trim())) { "Enter a valid international phone number." }
        require(otpCode.filter(Char::isDigit).length == 6) { "Enter the 6-digit verification code." }
        val response = requireBody(authenticatedApi.upgradeGuest(GuestUpgradeRequest(
            method = "phone",
            displayName = displayName.trim().takeIf { it.isNotEmpty() },
            phoneNumber = phoneNumber.trim(),
            otpCode = otpCode.filter(Char::isDigit),
        )))
        storeAuth(response, LastPortal.USER)
        registerDeviceFoundation(pushToken = null)
        return refreshCurrentUser(response.user)
    }

    suspend fun upgradeGuestWithGoogle(displayName: String?, googleIdToken: String): AuthUser {
        val response = requireBody(authenticatedApi.upgradeGuest(GuestUpgradeRequest(
            method = "google",
            displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
            googleIdToken = googleIdToken,
        )))
        storeAuth(response, LastPortal.USER)
        registerDeviceFoundation(pushToken = null)
        return refreshCurrentUser(response.user)
    }

    suspend fun forgotPassword(email: String): String {
        require(email.trim().contains('@')) { "Enter the email address for your VoiceCloud account." }
        return requireBody(publicApi.forgotPassword(ForgotPasswordRequest(email.trim().lowercase()))).message
    }

    suspend fun resetPassword(token: String, newPassword: String): String {
        require(token.trim().length >= 32) { "This recovery link is missing or contains an invalid one-time token." }
        require(newPassword.length >= 8) { "Your new password must be at least 8 characters." }
        return requireBody(publicApi.resetPassword(ResetPasswordRequest(token.trim(), newPassword))).message
    }

    suspend fun completeOnboarding(
        bio: String,
        country: String,
        interests: List<String>,
        reminders: Boolean,
    ): AuthUser {
        requireBody(authenticatedApi.updateProfile(UpdateProfileRequest(
            bio = bio.trim().takeIf { it.isNotEmpty() },
            country = country.trim().takeIf { it.isNotEmpty() },
            preferredLanguage = "en",
            interests = interests.distinct(),
        )))
        requireBody(authenticatedApi.updateSettings(UpdateSettingsRequest(
            language = "en",
            timezone = TimeZone.getDefault().id,
            notificationPreferences = NotificationPreferences(email = false, push = reminders, inApp = reminders),
        )))
        val user = requireBody(authenticatedApi.me())
        preferences.markOnboardingCompleted(user.id)
        return user
    }

    suspend fun submitCreatorAccessApplication(request: CreatorAccessApplicationRequest): CreatorAccessApplicationResponse {
        require(request.fullName.trim().length >= 2) { "Enter your full name." }
        require(request.email.contains('@')) { "Enter a valid email address." }
        require(request.phoneNumber.trim().length >= 6) { "Enter a valid phone number." }
        require(request.country.trim().length >= 2) { "Enter your country." }
        require(request.experience.trim().length >= 20) { "Tell us more about your creator experience." }
        require(request.motivation.trim().length >= 20) { "Tell us why you want Creator access." }
        return requireBody(publicApi.creatorAccess(request.copy(
            fullName = request.fullName.trim(),
            email = request.email.trim().lowercase(),
            phoneNumber = request.phoneNumber.trim(),
            country = request.country.trim(),
            creatorCategory = request.creatorCategory?.trim()?.takeIf { it.isNotEmpty() },
            currentAudience = request.currentAudience?.trim()?.takeIf { it.isNotEmpty() },
            experience = request.experience.trim(),
            motivation = request.motivation.trim(),
            portfolioUrl = request.portfolioUrl?.trim()?.takeIf { it.isNotEmpty() },
        )))
    }

    /** Switches portal preference without creating a second account or token. Creator entry remains role-authoritative. */
    suspend fun switchPortal(target: LastPortal): AuthUser {
        val user = requireBody(authenticatedApi.me())
        if (isRestricted(user)) {
            throw AuthApiException(ApiError(httpStatus = 403, message = "This account is currently restricted."))
        }
        if (target == LastPortal.CREATOR && user.normalizedRole != VoiceCloudRole.CREATOR) {
            throw CreatorRoleRequiredException()
        }
        if (user.normalizedRole in setOf(VoiceCloudRole.ADMIN, VoiceCloudRole.SUPER_ADMIN)) {
            throw AuthApiException(ApiError(httpStatus = 403, message = "Administrative accounts are not available in the VoiceCloud Android portals."))
        }
        preferences.setLastPortal(target)
        return user
    }

    /** FCM registration foundation: the future Firebase token provider feeds this method without changing API authority. */
    suspend fun registerPushToken(pushToken: String) = registerDeviceFoundation(pushToken.trim().takeIf { it.isNotEmpty() })

    suspend fun logout(allDevices: Boolean = false) {
        if (allDevices) {
            val response = authenticatedApi.logoutAll()
            if (!response.isSuccessful) throw AuthApiException(errorParser.parse(response))
            clearLocalSession()
        } else {
            // A normal local sign-out must remain possible even if the network is unavailable.
            runCatching { authenticatedApi.logout() }
            clearLocalSession()
        }
    }

    suspend fun currentUser(): AuthUser = requireBody(authenticatedApi.me())

    suspend fun invalidateLocalSession() = clearLocalSession()

    fun hasSecureSession(): Boolean = !tokenVault.accessToken().isNullOrBlank() || !tokenVault.refreshToken().isNullOrBlank()
    fun isRestrictedAccount(user: AuthUser): Boolean = isRestricted(user)

    private suspend fun refreshCurrentUser(fallback: AuthUser): AuthUser = runCatching { requireBody(authenticatedApi.me()) }
        .getOrElse { fallback }

    private suspend fun storeAuth(response: AuthResponse, portal: LastPortal) {
        tokenVault.save(response.accessToken, response.refreshToken)
        preferences.setSessionMetadata(response.sessionId, response.deviceId)
        preferences.setLastPortal(portal)
    }

    private suspend fun registerDeviceFoundation(pushToken: String?) {
        val device = deviceMetadataProvider.current()
        runCatching {
            requireBody(authenticatedApi.registerDevice(RegisterDeviceRequest(
                deviceId = device.deviceId,
                platform = "android",
                deviceType = device.deviceType,
                deviceName = device.deviceName,
                osVersion = device.osVersion,
                appVersion = device.appVersion,
                pushToken = pushToken,
            )))
        }.onFailure { logger.warn("Auth", "PH02 device registration foundation could not sync: ${it.message}") }
    }

    private suspend fun clearLocalSession() {
        tokenVault.clear()
        realtimeClient.disconnect()
        preferences.clearAccountPreferences()
    }

    suspend fun requiresOnboarding(user: AuthUser): Boolean {
        if (user.isGuest || preferences.hasCompletedOnboarding(user.id)) return false
        // Onboarding is first-account-setup only. Server profile data is authoritative across
        // devices/sign-ins, so an existing bio/country/interests state must not reopen it.
        val serverProfileAlreadyConfigured = !user.bio.isNullOrBlank() ||
            !user.country.isNullOrBlank() || user.interests.isNotEmpty()
        if (serverProfileAlreadyConfigured) {
            preferences.markOnboardingCompleted(user.id)
            return false
        }
        return (user.profileCompletion ?: 0) < 100
    }

    private fun isRestricted(user: AuthUser): Boolean {
        val value = listOfNotNull(user.status, user.accountStatus).joinToString(" ").uppercase()
        return value.contains("SUSPEND") || value.contains("RESTRICT") || value.contains("BANNED") || value.contains("BLOCKED")
    }

    private fun <T> requireBody(response: Response<T>): T {
        if (!response.isSuccessful) throw AuthApiException(errorParser.parse(response))
        return response.body() ?: throw AuthApiException(ApiError(
            httpStatus = response.code(),
            message = "VoiceCloud returned an empty response.",
            retryable = response.code() >= 500,
        ))
    }

    private companion object { val E164 = Regex("^\\+[1-9]\\d{1,14}$") }
}
