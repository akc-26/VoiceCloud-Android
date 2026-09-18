package app.voicecloud.feature.auth.google

import android.content.Context

sealed interface GoogleSignInOutcome {
    data class Success(val idToken: String) : GoogleSignInOutcome
    data object Cancelled : GoogleSignInOutcome
    data class Failure(val message: String, val retryable: Boolean = true) : GoogleSignInOutcome
}

interface GoogleSignInGateway {
    suspend fun signIn(context: Context): GoogleSignInOutcome
}
