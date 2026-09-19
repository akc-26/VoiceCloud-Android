package app.voicecloud.feature.engagement.data

import app.voicecloud.feature.auth.data.FcmTokenRegistrationFoundation
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationCoordinator @Inject constructor(
    private val tokenRegistration: FcmTokenRegistrationFoundation,
) {
    @Suppress("DEPRECATION") // R06 backend still persists FCM registration tokens; migrate with the backend registration contract.
    suspend fun syncCurrentToken() {
        val messaging = runCatching { FirebaseMessaging.getInstance() }.getOrNull() ?: return
        val deferred = CompletableDeferred<String?>()
        messaging.token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful) task.result?.trim() else null
            deferred.complete(token?.takeIf { it.isNotBlank() })
        }
        deferred.await()?.let { tokenRegistration.onNewToken(it) }
    }
}
