package app.voicecloud.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.model.CreatorAccessApplicationRequest
import app.voicecloud.core.model.VoiceCloudUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionController: AuthSessionController,
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun onUsernameChange(value: String) {
        _state.value = _state.value.copy(username = value, errorMessage = null)
    }

    fun onDisplayNameChange(value: String) {
        _state.value = _state.value.copy(displayName = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun submit(onRegistered: () -> Unit) {
        val current = _state.value
        if (current.username.isBlank() || current.email.isBlank() || current.password.length < 8) {
            _state.value = current.copy(errorMessage = "Username, email, and password (8+ chars) are required.")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true, errorMessage = null)
            when (
                val result = authRepository.register(
                    email = current.email.trim(),
                    username = current.username.trim(),
                    password = current.password,
                    displayName = current.displayName.trim().ifBlank { null },
                )
            ) {
                is AuthResult.Success -> {
                    sessionController.onAuthenticated(result.value.user)
                    _state.value = _state.value.copy(isSubmitting = false)
                    onRegistered()
                }
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
                }
            }
        }
    }
}

data class RegisterUiState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val displayName: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null, successMessage = null)
    }

    fun submit(onSent: () -> Unit) {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Enter the email on your account.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = authRepository.forgotPassword(email)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        successMessage = "If an account exists for that email, reset instructions were sent.",
                    )
                    onSent()
                }
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
                }
            }
        }
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state: StateFlow<ResetPasswordUiState> = _state.asStateFlow()

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null, successMessage = null)
    }

    fun submit(token: String, onComplete: () -> Unit) {
        val password = _state.value.password
        if (token.isBlank() || password.length < 8) {
            _state.value = _state.value.copy(errorMessage = "Enter a new password (8+ characters).")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = authRepository.resetPassword(token, password)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        successMessage = "Password updated. Sign in with your new password.",
                    )
                    onComplete()
                }
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
                }
            }
        }
    }
}

data class ResetPasswordUiState(
    val password: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionController: AuthSessionController,
) : ViewModel() {
    private val _state = MutableStateFlow(PhoneAuthUiState())
    val state: StateFlow<PhoneAuthUiState> = _state.asStateFlow()

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phone = value, errorMessage = null)
    }

    fun onCodeChange(value: String) {
        _state.value = _state.value.copy(code = value, errorMessage = null)
    }

    fun submit(onAuthenticated: () -> Unit) {
        val current = _state.value
        if (current.phone.isBlank()) {
            _state.value = current.copy(errorMessage = "Enter your phone number.")
            return
        }
        if (!current.codeSent) {
            sendCode()
            return
        }
        if (current.code.isBlank()) {
            _state.value = current.copy(errorMessage = "Enter the verification code.")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true, errorMessage = null)
            when (val result = authRepository.phoneLogin(current.phone.trim(), current.code.trim())) {
                is AuthResult.Success -> {
                    sessionController.onAuthenticated(result.value.user)
                    _state.value = _state.value.copy(isSubmitting = false)
                    onAuthenticated()
                }
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
                }
            }
        }
    }

    private fun sendCode() {
        val phone = _state.value.phone.trim()
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = authRepository.sendOtp(phone)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(isSubmitting = false, codeSent = true)
                }
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
                }
            }
        }
    }
}

data class PhoneAuthUiState(
    val phone: String = "",
    val code: String = "",
    val codeSent: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class GuestUpgradeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionController: AuthSessionController,
) : ViewModel() {
    private val _state = MutableStateFlow(GuestUpgradeUiState())
    val state: StateFlow<GuestUpgradeUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun onUsernameChange(value: String) {
        _state.value = _state.value.copy(username = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun submit(onUpgraded: () -> Unit) {
        val current = _state.value
        if (current.email.isBlank() || current.password.length < 8 || current.username.isBlank()) {
            _state.value = current.copy(errorMessage = "Username, email, and password (8+ chars) are required.")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true, errorMessage = null)
            when (
                val result = authRepository.upgradeGuest(
                    email = current.email.trim(),
                    username = current.username.trim(),
                    password = current.password,
                )
            ) {
                is AuthResult.Success -> {
                    sessionController.onAuthenticated(result.value.user)
                    _state.value = _state.value.copy(isSubmitting = false)
                    onUpgraded()
                }
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
                }
            }
        }
    }
}

data class GuestUpgradeUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class SessionListViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val sessionController: AuthSessionController,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionListUiState())
    val state: StateFlow<SessionListUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = sessionRepository.sessions()) {
                is app.voicecloud.core.model.ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, items = result.data)
                }
                is app.voicecloud.core.model.ApiResult.Failure -> {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = result.error.message)
                }
            }
        }
    }

    fun revoke(sessionId: String) {
        viewModelScope.launch {
            when (sessionRepository.revokeSession(sessionId)) {
                is app.voicecloud.core.model.ApiResult.Success -> refresh()
                is app.voicecloud.core.model.ApiResult.Failure -> Unit
            }
        }
    }

    fun logoutAll(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            when (authRepository.logoutAll()) {
                is AuthResult.Success -> {
                    sessionController.clearLoggedOut()
                    onSignedOut()
                }
                is AuthResult.Failure -> Unit
            }
        }
    }
}

data class SessionListUiState(
    val isLoading: Boolean = true,
    val items: List<AuthSessionItem> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class LoginHistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionListUiState())
    val state: StateFlow<SessionListUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = sessionRepository.loginHistory()) {
                is app.voicecloud.core.model.ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, items = result.data)
                }
                is app.voicecloud.core.model.ApiResult.Failure -> {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = result.error.message)
                }
            }
        }
    }
}

@HiltViewModel
class CreatorAccessViewModel @Inject constructor(
    private val creatorAccessRepository: CreatorAccessRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CreatorAccessUiState())
    val state: StateFlow<CreatorAccessUiState> = _state.asStateFlow()

    fun onDisplayNameChange(value: String) {
        _state.value = _state.value.copy(displayName = value, errorMessage = null)
    }

    fun onBioChange(value: String) {
        _state.value = _state.value.copy(bio = value, errorMessage = null)
    }

    fun onReasonChange(value: String) {
        _state.value = _state.value.copy(reason = value, errorMessage = null)
    }

    fun submit(onSubmitted: () -> Unit) {
        val current = _state.value
        if (current.reason.trim().length < 20) {
            _state.value = current.copy(errorMessage = "Tell us a bit more (at least 20 characters).")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true, errorMessage = null)
            when (
                val result = creatorAccessRepository.submit(
                    CreatorAccessApplicationRequest(
                        displayName = current.displayName.trim().ifBlank { null },
                        bio = current.bio.trim().ifBlank { null },
                        reason = current.reason.trim(),
                    ),
                )
            ) {
                is app.voicecloud.core.model.ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        successMessage = "Application submitted. VoiceCloud will review your request.",
                    )
                    onSubmitted()
                }
                is app.voicecloud.core.model.ApiResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
                }
            }
        }
    }
}

data class CreatorAccessUiState(
    val displayName: String = "",
    val bio: String = "",
    val reason: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class AuthEntryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionController: AuthSessionController,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthEntryUiState())
    val state: StateFlow<AuthEntryUiState> = _state.asStateFlow()

    fun continueAsGuest(onAuthenticated: (VoiceCloudUser) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = authRepository.guestLogin()) {
                is AuthResult.Success -> {
                    sessionController.onAuthenticated(result.value.user)
                    _state.value = _state.value.copy(isSubmitting = false)
                    onAuthenticated(result.value.user)
                }
                is AuthResult.Failure -> {
                    _state.value = _state.value.copy(isSubmitting = false, errorMessage = result.error.message)
                }
            }
        }
    }
}

data class AuthEntryUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class GoogleSignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionController: AuthSessionController,
    private val googleSignInGateway: app.voicecloud.feature.auth.google.GoogleSignInGateway,
    private val googleSignInConfig: app.voicecloud.core.model.GoogleSignInConfig,
) : ViewModel() {
    private val _state = MutableStateFlow(GoogleSignInUiState(isConfigured = googleSignInConfig.isConfigured))
    val state: StateFlow<GoogleSignInUiState> = _state.asStateFlow()

    fun signIn(context: android.content.Context, onAuthenticated: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null, infoMessage = null)
            when (val outcome = googleSignInGateway.signIn(context)) {
                is app.voicecloud.feature.auth.google.GoogleSignInOutcome.Success -> {
                    exchangeIdToken(outcome.idToken, onAuthenticated)
                }
                app.voicecloud.feature.auth.google.GoogleSignInOutcome.Cancelled -> {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        infoMessage = "Google Sign-In was cancelled.",
                    )
                }
                is app.voicecloud.feature.auth.google.GoogleSignInOutcome.Failure -> {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        errorMessage = outcome.message,
                    )
                }
            }
        }
    }

    private suspend fun exchangeIdToken(idToken: String, onAuthenticated: () -> Unit) {
        when (val result = authRepository.googleLogin(idToken)) {
            is AuthResult.Success -> {
                sessionController.onAuthenticated(result.value.user)
                _state.value = _state.value.copy(isSubmitting = false, errorMessage = null)
                onAuthenticated()
            }
            is AuthResult.Failure -> {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }
}

data class GoogleSignInUiState(
    val isConfigured: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DeviceListUiState())
    val state: StateFlow<DeviceListUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = sessionRepository.devices()) {
                is app.voicecloud.core.model.ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, items = result.data)
                }
                is app.voicecloud.core.model.ApiResult.Failure -> {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = result.error.message)
                }
            }
        }
    }

    fun revoke(deviceId: String) {
        viewModelScope.launch {
            when (sessionRepository.revokeDevice(deviceId)) {
                is app.voicecloud.core.model.ApiResult.Success -> refresh()
                is app.voicecloud.core.model.ApiResult.Failure -> Unit
            }
        }
    }
}

data class DeviceListUiState(
    val isLoading: Boolean = true,
    val items: List<AuthDeviceItem> = emptyList(),
    val errorMessage: String? = null,
)
