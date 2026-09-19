package app.voicecloud.android

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoiceCloudApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFirebaseDefaultApp()
    }

    /**
     * VoiceCloud intentionally keeps Firebase public client configuration in centralized BuildConfig
     * values instead of committing google-services.json. Messaging needs the default Firebase app,
     * while PH02 Google Auth continues to use its isolated named Firebase app.
     */
    private fun initializeFirebaseDefaultApp() {
        if (FirebaseApp.getApps(this).any { it.name == FirebaseApp.DEFAULT_APP_NAME }) return
        val apiKey = BuildConfig.FIREBASE_API_KEY.trim()
        val applicationId = BuildConfig.FIREBASE_APPLICATION_ID.trim()
        val projectId = BuildConfig.FIREBASE_PROJECT_ID.trim()
        if (apiKey.isBlank() || applicationId.isBlank() || projectId.isBlank()) return
        FirebaseApp.initializeApp(
            this,
            FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setApplicationId(applicationId)
                .setProjectId(projectId)
                .build(),
        )
    }
}
