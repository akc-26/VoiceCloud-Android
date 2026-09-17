package app.voicecloud.android.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.voicecloud.android.ui.shell.ConsumerShell
import app.voicecloud.android.ui.shell.CreatorShell
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
import app.voicecloud.feature.bootstrap.BootstrapRoute

@Composable
fun VoiceCloudNavHost(navController: NavHostController) {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    NavHost(
        navController = navController,
        startDestination = VoiceCloudRoutes.Bootstrap,
        enterTransition = { if (motionEnabled) fadeIn(tween(VoiceCloudMotion.EntranceMs)) else EnterTransition.None },
        exitTransition = { if (motionEnabled) fadeOut(tween(VoiceCloudMotion.ExitMs)) else ExitTransition.None },
    ) {
        composable(VoiceCloudRoutes.Bootstrap) {
            BootstrapRoute(
                onContinueToConsumer = {
                    navController.navigate(VoiceCloudRoutes.UserPortal) { launchSingleTop = true }
                },
                onOpenCreatorWorkspace = {
                    navController.navigate(VoiceCloudRoutes.CreatorPortal) { launchSingleTop = true }
                },
            )
        }
        composable(VoiceCloudRoutes.UserPortal) {
            ConsumerShell(
                onOpenCreatorWorkspace = {
                    navController.navigate(VoiceCloudRoutes.CreatorPortal) { launchSingleTop = true }
                },
            )
        }
        composable(VoiceCloudRoutes.CreatorPortal) {
            CreatorShell(
                onOpenConsumerExperience = {
                    if (!navController.popBackStack(VoiceCloudRoutes.UserPortal, inclusive = false)) {
                        navController.navigate(VoiceCloudRoutes.UserPortal) { launchSingleTop = true }
                    }
                },
            )
        }
    }
}
