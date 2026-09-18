package app.voicecloud.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.navigation.VoiceCloudNavHost
import app.voicecloud.android.ui.theme.VoiceCloudSystemBarAppearance
import app.voicecloud.android.ui.theme.rememberVoiceCloudDarkTheme
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme

@Composable
fun VoiceCloudRoot(initialPasswordResetToken: String? = null) {
    val darkTheme = rememberVoiceCloudDarkTheme()
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = darkTheme) {
        VoiceCloudSystemBarAppearance(darkTheme)
        VoiceCloudNavHost(
            navController = rememberNavController(),
            initialPasswordResetToken = initialPasswordResetToken,
        )
    }
}
