package app.voicecloud.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.BuildConfig
import app.voicecloud.android.navigation.VoiceCloudNavHost
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.core.preferences.ThemePreference
import app.voicecloud.feature.auth.model.FirebaseClientConfig
import app.voicecloud.feature.settings.ui.AppearanceViewModel

@Composable
fun VoiceCloudRoot(
    initialResetToken: String? = null,
    initialNavigationRoute: String? = null,
    appearanceViewModel: AppearanceViewModel = hiltViewModel(),
) {
    val theme = appearanceViewModel.theme.collectAsStateWithLifecycle().value
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (theme) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> systemDark
    }
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = darkTheme) {
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
