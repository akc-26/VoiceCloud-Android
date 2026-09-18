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

/** PH01 start remains bootstrap. Creator portal is presentation shell only. */
object VoiceCloudRoutes {
    const val Bootstrap = "bootstrap"
    const val UserPortal = "user"
    const val CreatorPortal = "creator"
}

object CreatorDestinations {
    const val Dashboard = "creator/dashboard"
    const val Audience = "creator/audience"
    const val LiveStudio = "creator/live"
    const val Analytics = "creator/analytics"
    const val Workspace = "creator/workspace"
}

object ConsumerDestinations {
    const val Home = "user/home"
    const val Discover = "user/discover"
    const val Search = "user/search"
    const val RoomPreview = "user/room/preview"
    const val Live = "user/live"
    const val Messages = "user/messages"
    const val MessageThread = "user/messages/thread"
    const val Profile = "user/profile"
    const val Wallet = "user/wallet"
}

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
                onLeaveWorkspace = {
                    if (!navController.popBackStack(VoiceCloudRoutes.UserPortal, inclusive = false)) {
                        navController.popBackStack(VoiceCloudRoutes.Bootstrap, inclusive = false)
                    }
                },
            )
        }
    }
}
