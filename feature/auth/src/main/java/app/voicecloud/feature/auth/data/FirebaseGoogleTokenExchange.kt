package app.voicecloud.feature.auth.data

import android.content.Context
import app.voicecloud.feature.auth.model.FirebaseClientConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Converts the Google OAuth ID token produced on Android into the Firebase Authentication ID token
 * required by VoiceCloud backend GoogleAuthService.verifyGoogleIdToken().
 */
object FirebaseGoogleTokenExchange {
    private const val APP_NAME = "voicecloud-auth"

    suspend fun exchange(context: Context, googleIdToken: String, config: FirebaseClientConfig): String {
        require(config.isConfigured) { "Google Sign-In is not configured for this Android build." }
        require(googleIdToken.isNotBlank()) { "Google Sign-In did not return an ID token." }

        val firebaseApp = synchronized(FirebaseGoogleTokenExchange::class.java) {
            FirebaseApp.getApps(context).firstOrNull { it.name == APP_NAME } ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApiKey(config.apiKey)
                    .setApplicationId(config.applicationId)
                    .setProjectId(config.projectId)
                    .build(),
                APP_NAME,
            )
        }
        val auth = FirebaseAuth.getInstance(firebaseApp)
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)

        return suspendCancellableCoroutine { continuation ->
            auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                if (!signInTask.isSuccessful) {
                    continuation.resumeWithException(
                        signInTask.exception ?: IllegalStateException("Firebase Google authentication failed."),
                    )
                    return@addOnCompleteListener
                }
                val user = signInTask.result?.user ?: auth.currentUser
                if (user == null) {
                    continuation.resumeWithException(IllegalStateException("Firebase did not return an authenticated user."))
                    return@addOnCompleteListener
                }
                user.getIdToken(true).addOnCompleteListener { tokenTask ->
                    val firebaseIdToken = tokenTask.result?.token?.trim().orEmpty()
                    when {
                        !tokenTask.isSuccessful -> continuation.resumeWithException(
                            tokenTask.exception ?: IllegalStateException("Firebase ID token exchange failed."),
                        )
                        firebaseIdToken.isBlank() -> continuation.resumeWithException(
                            IllegalStateException("Firebase did not return an ID token."),
                        )
                        else -> continuation.resume(firebaseIdToken)
                    }
                }
            }
        }
    }
}
