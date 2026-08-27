package app.voicecloud.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.BuildConfig
import app.voicecloud.android.navigation.VoiceCloudNavHost
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.feature.auth.model.FirebaseClientConfig

@Composable
fun VoiceCloudRoot(initialResetToken: String? = null, initialNavigationRoute: String? = null) {
    // Fresh/public launch is deliberately Light-first. Creator screens apply their own Creator palette.
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = false) {
        // Android 15+ enforces edge-to-edge. Keep the VoiceCloud background behind
        // system bars while consuming safe drawing insets once at the app root so
        // every route inherits the same status/navigation-bar protection.
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                VoiceCloudNavHost(
                    navController = rememberNavController(),
                    initialResetToken = initialResetToken,
                    initialNavigationRoute = initialNavigationRoute,
                    firebaseClientConfig = FirebaseClientConfig(
                        apiKey = BuildConfig.FIREBASE_API_KEY,
                        applicationId = BuildConfig.FIREBASE_APPLICATION_ID,
                        projectId = BuildConfig.FIREBASE_PROJECT_ID,
                        googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
                    ),
                )
            }
        }
    }
}
