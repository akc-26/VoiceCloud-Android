package app.voicecloud.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.preferences.LastPortal
import app.voicecloud.feature.auth.data.AuthApiException
import app.voicecloud.feature.auth.data.AuthRepository
import app.voicecloud.feature.auth.data.CreatorRoleRequiredException
import app.voicecloud.feature.auth.model.AuthLanding
import app.voicecloud.feature.auth.model.AuthUser
import app.voicecloud.feature.auth.model.CreatorAccessApplicationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthScreen {
    PORTAL_SELECTOR, USER_SIGN_IN, REGISTER, PHONE_SIGN_IN, OTP_VERIFY,
    FORGOT_PASSWORD, RESET_PASSWORD, ONBOARDING, GUEST_UPGRADE,
    RESTRICTED, SESSION_EXPIRED, USER_READY,
    CREATOR_SIGN_IN, CREATOR_ACCESS, CREATOR_READY, MAINTENANCE,
}

sealed interface AuthEvent {
    data class Navigate(val screen: AuthScreen, val clearAuthStack: Boolean = false) : AuthEvent
    data class Notice(val message: String) : AuthEvent
}

data class AuthUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val user: AuthUser? = null,
    val pendingPhone: String = "",
    val pendingReferral: String = "",
    val otpCooldownSeconds: Int = 60,
    val developmentOtp: String? = null,
    val restrictedMessage: String? = null,
    val maintenanceMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun clearMessages() { _state.value = _state.value.copy(error = null, notice = null) }

    fun restoreSession() = launchOperation {
        when (val landing = repository.restoreSession()) {
            AuthLanding.PortalSelector -> navigate(AuthScreen.PORTAL_SELECTOR, true)
            AuthLanding.SessionExpired -> navigate(AuthScreen.SESSION_EXPIRED, true)
            is AuthLanding.Restricted -> {
                _state.value = _state.value.copy(restrictedMessage = landing.message)
                navigate(AuthScreen.RESTRICTED, true)
            }
            is AuthLanding.Maintenance -> {
                _state.value = _state.value.copy(maintenanceMessage = landing.message)
                navigate(AuthScreen.MAINTENANCE, true)
            }
            is AuthLanding.User -> {
                _state.value = _state.value.copy(user = landing.user)
                navigate(if (landing.needsOnboarding) AuthScreen.ONBOARDING else AuthScreen.USER_READY, true)
            }
            is AuthLanding.Creator -> {
                _state.value = _state.value.copy(user = landing.user)
                navigate(AuthScreen.CREATOR_READY, true)
            }
        }
    }

    fun login(identifier: String, password: String, creatorPortal: Boolean) = launchOperation {
        val user = repository.login(identifier, password, creatorPortal)
        _state.value = _state.value.copy(user = user)
        if (repository.isRestrictedAccount(user)) {
            _state.value = _state.value.copy(restrictedMessage = "This account is currently restricted. Follow the account instructions or contact VoiceCloud support.")
            navigate(AuthScreen.RESTRICTED, true)
        } else if (creatorPortal) navigate(AuthScreen.CREATOR_READY, true) else routeUser(user)
    }

    fun register(username: String, displayName: String, email: String, password: String) = launchOperation {
        val user = repository.register(username, displayName, email, password)
        _state.value = _state.value.copy(user = user)
        routeUser(user)
    }

    fun sendPhoneOtp(phoneNumber: String, referralCode: String = "") = launchOperation {
        val otp = repository.sendPhoneOtp(phoneNumber)
        _state.value = _state.value.copy(
            pendingPhone = phoneNumber.trim(),
            pendingReferral = referralCode.trim(),
            otpCooldownSeconds = otp.resendCooldownSeconds ?: 60,
            developmentOtp = otp.otpCode,
        )
        navigate(AuthScreen.OTP_VERIFY)
    }

    fun resendPhoneOtp() = launchOperation {
        val otp = repository.sendPhoneOtp(_state.value.pendingPhone)
        _state.value = _state.value.copy(
            otpCooldownSeconds = otp.resendCooldownSeconds ?: 60,
            developmentOtp = otp.otpCode,
        )
        _state.value = _state.value.copy(notice = otp.message)
    }

    fun verifyPhoneOtp(otpCode: String) = launchOperation {
        val user = repository.phoneLogin(_state.value.pendingPhone, otpCode, _state.value.pendingReferral)
        _state.value = _state.value.copy(user = user, developmentOtp = null)
        routeUser(user)
    }

    fun googleLogin(idToken: String, referralCode: String = "") = launchOperation {
        val user = repository.googleLogin(idToken, referralCode)
        _state.value = _state.value.copy(user = user)
        routeUser(user)
    }

    fun guestLogin(referralCode: String = "") = launchOperation {
        val user = repository.guestLogin(referralCode)
        _state.value = _state.value.copy(user = user)
        navigate(AuthScreen.USER_READY, true)
    }

    fun sendGuestUpgradePhoneOtp(phoneNumber: String) = launchOperation {
        val otp = repository.sendPhoneOtp(phoneNumber)
        _state.value = _state.value.copy(
            pendingPhone = phoneNumber.trim(),
            otpCooldownSeconds = otp.resendCooldownSeconds ?: 60,
            developmentOtp = otp.otpCode,
            notice = otp.message,
        )
    }

    fun upgradeGuestEmail(displayName: String, email: String, password: String) = launchOperation {
        val user = repository.upgradeGuestWithEmail(displayName, email, password)
        _state.value = _state.value.copy(user = user)
        routeUser(user)
    }

    fun upgradeGuestPhone(displayName: String, phoneNumber: String, otpCode: String) = launchOperation {
        val user = repository.upgradeGuestWithPhone(displayName, phoneNumber, otpCode)
        _state.value = _state.value.copy(user = user)
        routeUser(user)
    }

    fun upgradeGuestGoogle(displayName: String?, idToken: String) = launchOperation {
        val user = repository.upgradeGuestWithGoogle(displayName, idToken)
        _state.value = _state.value.copy(user = user)
        routeUser(user)
    }

    fun forgotPassword(email: String) = launchOperation {
        val message = repository.forgotPassword(email)
        _state.value = _state.value.copy(notice = message)
    }

    fun resetPassword(token: String, newPassword: String) = launchOperation {
        val message = repository.resetPassword(token, newPassword)
        _state.value = _state.value.copy(notice = message)
    }

    fun completeOnboarding(bio: String, country: String, interests: List<String>, reminders: Boolean) = launchOperation(authenticated = true) {
        val user = repository.completeOnboarding(bio, country, interests, reminders)
        _state.value = _state.value.copy(user = user)
        navigate(AuthScreen.USER_READY, true)
    }

    fun submitCreatorAccess(request: CreatorAccessApplicationRequest) = launchOperation {
        val response = repository.submitCreatorAccessApplication(request)
        _state.value = _state.value.copy(notice = response.message ?: "Your Creator access application has been submitted.")
    }

    fun switchToCreatorPortal() = launchOperation(authenticated = true) {
        val user = repository.switchPortal(LastPortal.CREATOR)
        _state.value = _state.value.copy(user = user)
        navigate(AuthScreen.CREATOR_READY, true)
    }

    fun switchToUserPortal() = launchOperation(authenticated = true) {
        val user = repository.switchPortal(LastPortal.USER)
        _state.value = _state.value.copy(user = user)
        routeUser(user, clear = true)
    }

    fun logout(allDevices: Boolean = false) = launchOperation(authenticated = true) {
        repository.logout(allDevices)
        _state.value = AuthUiState()
        navigate(AuthScreen.PORTAL_SELECTOR, true)
    }

    fun handleAuthenticatedFailure(httpStatus: Int, message: String? = null) {
        viewModelScope.launch {
            when (httpStatus) {
                401 -> {
                    repository.invalidateLocalSession()
                    _state.value = AuthUiState()
                    navigate(AuthScreen.SESSION_EXPIRED, true)
                }
                403 -> {
                    _state.value = _state.value.copy(restrictedMessage = message?.takeIf { it.isNotBlank() })
                    navigate(AuthScreen.RESTRICTED, true)
                }
                503 -> {
                    _state.value = _state.value.copy(maintenanceMessage = message?.takeIf { it.isNotBlank() })
                    navigate(AuthScreen.MAINTENANCE, true)
                }
            }
        }
    }

    private suspend fun routeUser(user: AuthUser, clear: Boolean = false) {
        if (repository.isRestrictedAccount(user)) {
            _state.value = _state.value.copy(restrictedMessage = "This account is currently restricted. Follow the account instructions or contact VoiceCloud support.")
            navigate(AuthScreen.RESTRICTED, true)
            return
        }
        val needsOnboarding = repository.requiresOnboarding(user)
        navigate(if (needsOnboarding) AuthScreen.ONBOARDING else AuthScreen.USER_READY, clear)
    }

    private suspend fun navigate(screen: AuthScreen, clear: Boolean = false) {
        _events.send(AuthEvent.Navigate(screen, clear))
    }

    private fun launchOperation(authenticated: Boolean = false, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null, notice = null)
            try {
                block()
            } catch (e: CreatorRoleRequiredException) {
                _state.value = _state.value.copy(error = e.message)
            } catch (e: AuthApiException) {
                when (e.apiError.httpStatus) {
                    403 -> {
                        _state.value = _state.value.copy(restrictedMessage = e.apiError.message)
                        navigate(AuthScreen.RESTRICTED, true)
                    }
                    503 -> {
                        _state.value = _state.value.copy(maintenanceMessage = e.apiError.message)
                        navigate(AuthScreen.MAINTENANCE, true)
                    }
                    401 -> if (authenticated) { repository.invalidateLocalSession(); navigate(AuthScreen.SESSION_EXPIRED, true) }
                    else -> Unit
                }
                if (!(authenticated && e.apiError.httpStatus == 401) && e.apiError.httpStatus != 403 && e.apiError.httpStatus != 503) {
                    _state.value = _state.value.copy(error = e.apiError.message)
                }
            } catch (e: IllegalArgumentException) {
                _state.value = _state.value.copy(error = e.message ?: "Check the information you entered.")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "VoiceCloud could not complete this request.")
            } finally {
                _state.value = _state.value.copy(busy = false)
            }
        }
    }
}
