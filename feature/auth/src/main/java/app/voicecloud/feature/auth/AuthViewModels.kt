package app.voicecloud.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.model.AuthTokenBundle
import app.voicecloud.core.model.VoiceCloudAccountRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val useEmail: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class UserLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionController: AuthSessionController,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onIdentifierChange(value: String) {
        _state.value = _state.value.copy(identifier = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun toggleUseEmail() {
        _state.value = _state.value.copy(useEmail = !_state.value.useEmail, errorMessage = null)
    }

    fun submit(onSuccess: (VoiceCloudAccountRole) -> Unit) {
        val current = _state.value
        if (current.identifier.isBlank() || current.password.isBlank()) {
            _state.value = current.copy(errorMessage = "Enter your credentials to continue.")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true, errorMessage = null)
            when (
                val result = authRepository.login(
                    identifier = current.identifier.trim(),
                    password = current.password,
                    useEmail = current.useEmail,
                )
            ) {
                is AuthResult.Success -> onLoginSuccess(result.value, onSuccess)
                is AuthResult.Failure -> _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }

    private fun onLoginSuccess(bundle: AuthTokenBundle, onSuccess: (VoiceCloudAccountRole) -> Unit) {
        sessionController.onAuthenticated(bundle.user)
        _state.value = _state.value.copy(isSubmitting = false, errorMessage = null)
        onSuccess(bundle.user.accountRole)
    }
}

@HiltViewModel
class CreatorLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionController: AuthSessionController,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onIdentifierChange(value: String) {
        _state.value = _state.value.copy(identifier = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun toggleUseEmail() {
        _state.value = _state.value.copy(useEmail = !_state.value.useEmail, errorMessage = null)
    }

    fun submit(onSuccess: () -> Unit, onNotCreator: () -> Unit) {
        val current = _state.value
        if (current.identifier.isBlank() || current.password.isBlank()) {
            _state.value = current.copy(errorMessage = "Enter your creator credentials.")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true, errorMessage = null)
            when (
                val result = authRepository.login(
                    identifier = current.identifier.trim(),
                    password = current.password,
                    useEmail = current.useEmail,
                )
            ) {
                is AuthResult.Success -> {
                    val role = result.value.user.accountRole
                    if (!role.canAccessCreatorPortal()) {
                        authRepository.logout()
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            errorMessage = "This account is not authorized for Creator Studio.",
                        )
                        onNotCreator()
                    } else {
                        sessionController.onAuthenticated(result.value.user)
                        _state.value = _state.value.copy(isSubmitting = false)
                        onSuccess()
                    }
                }
                is AuthResult.Failure -> _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = result.error.message,
                )
            }
        }
    }
}

@HiltViewModel
class AuthGateViewModel @Inject constructor(
    val sessionController: AuthSessionController,
) : ViewModel() {
    val sessionState: StateFlow<AuthSessionState> = sessionController.state

    fun restore(onComplete: () -> Unit) {
        viewModelScope.launch {
            sessionController.restore()
            onComplete()
        }
    }
}
