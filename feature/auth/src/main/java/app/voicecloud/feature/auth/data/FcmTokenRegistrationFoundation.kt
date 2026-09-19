package app.voicecloud.feature.auth.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * PH02 boundary for Firebase Cloud Messaging. A later app-level FirebaseMessagingService can pass
 * every new FCM token here without knowing the backend route or device metadata contract.
 */
@Singleton
class FcmTokenRegistrationFoundation @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend fun onNewToken(token: String) {
        if (token.isNotBlank() && repository.hasSecureSession()) repository.registerPushToken(token)
    }
}
