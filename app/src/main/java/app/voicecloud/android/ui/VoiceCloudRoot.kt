package app.voicecloud.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.navigation.VoiceCloudNavHost
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme

@Composable
fun VoiceCloudRoot() {
    // Public/fresh launch is deliberately Light, matching finalized R06 Website authority.
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = false) {
        VoiceCloudNavHost(navController = rememberNavController())
    }
}
