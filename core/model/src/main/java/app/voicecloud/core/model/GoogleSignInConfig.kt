package app.voicecloud.core.model

/**
 * OAuth 2.0 Web client ID used by Android Credential Manager Google Sign-In.
 * Must match the Google Cloud Console Web application client linked to VoiceCloud backend verification.
 */
data class GoogleSignInConfig(
    val webClientId: String,
) {
    val isConfigured: Boolean get() = webClientId.isNotBlank()
}
