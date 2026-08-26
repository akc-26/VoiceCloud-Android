package app.voicecloud.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.BuildConfig
import app.voicecloud.android.navigation.VoiceCloudNavHost
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.feature.auth.model.FirebaseClientConfig

@Composable
fun VoiceCloudRoot(initialResetToken: String? = null) {
    // Fresh/public launch is deliberately Light-first. Creator screens apply their own Creator palette.
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = false) {
        VoiceCloudNavHost(
            navController = rememberNavController(),
            initialResetToken = initialResetToken,
            firebaseClientConfig = FirebaseClientConfig(
                apiKey = BuildConfig.FIREBASE_API_KEY,
                applicationId = BuildConfig.FIREBASE_APPLICATION_ID,
                projectId = BuildConfig.FIREBASE_PROJECT_ID,
                googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
            ),
        )
    }
}
