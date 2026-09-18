package app.voicecloud.feature.auth

import app.voicecloud.core.model.VoiceCloudAccountRole
import app.voicecloud.core.model.VoiceCloudUser
import app.voicecloud.core.preferences.LastPortal
import app.voicecloud.core.preferences.VoiceCloudPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthSessionState {
    data object Unknown : AuthSessionState
    data object LoggedOut : AuthSessionState
    data class Authenticated(val user: VoiceCloudUser) : AuthSessionState
}

@Singleton
class AuthSessionController @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferences: VoiceCloudPreferences,
) {
    private val _state = MutableStateFlow<AuthSessionState>(AuthSessionState.Unknown)
    val state: StateFlow<AuthSessionState> = _state.asStateFlow()

    suspend fun restore() {
        when (val result = authRepository.restoreSession()) {
            is AuthResult.Success -> _state.value = AuthSessionState.Authenticated(result.value)
            is AuthResult.Failure -> _state.value = AuthSessionState.LoggedOut
        }
    }

    fun onAuthenticated(user: VoiceCloudUser) {
        _state.value = AuthSessionState.Authenticated(user)
    }

    suspend fun logout() {
        authRepository.logout()
        _state.value = AuthSessionState.LoggedOut
    }

    suspend fun clearLoggedOut() {
        _state.value = AuthSessionState.LoggedOut
    }

    fun markSessionExpired() {
        _state.value = AuthSessionState.LoggedOut
    }

    suspend fun persistPortal(portal: LastPortal) {
        preferences.setLastPortal(portal)
    }

    fun role(): VoiceCloudAccountRole = when (val current = _state.value) {
        is AuthSessionState.Authenticated -> current.user.accountRole
        else -> VoiceCloudAccountRole.UNKNOWN
    }
}
