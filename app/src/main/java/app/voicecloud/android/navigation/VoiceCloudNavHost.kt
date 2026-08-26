package app.voicecloud.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.voicecloud.feature.bootstrap.BootstrapRoute

/** PH01 owns bootstrap navigation only. User and Creator auth graphs are introduced in PH02. */
object VoiceCloudRoutes {
    const val Bootstrap = "bootstrap"
    const val UserPortal = "user"
    const val CreatorPortal = "creator"
}

@Composable
fun VoiceCloudNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = VoiceCloudRoutes.Bootstrap) {
        composable(VoiceCloudRoutes.Bootstrap) { BootstrapRoute() }
    }
}
