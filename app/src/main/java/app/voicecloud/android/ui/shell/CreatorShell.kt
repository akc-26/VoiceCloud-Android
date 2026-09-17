package app.voicecloud.android.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.navigation.CreatorDestinations
import app.voicecloud.core.designsystem.component.VCCommandBar
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCNavDestination
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCScaffold
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun CreatorShell(
    onLeaveWorkspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        CreatorWorkspace(
            onLeaveWorkspace = onLeaveWorkspace,
            modifier = modifier,
        )
    }
}

@Composable
private fun CreatorWorkspace(
    onLeaveWorkspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val current by navController.currentBackStackEntryAsState()
    val route = current?.destination?.route
    val liveSelected = route == CreatorDestinations.LiveStudio
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val sides = remember {
        listOf(
            VCNavDestination(
                CreatorDestinations.Dashboard,
                "Dashboard",
                VoiceCloudIcons.Dashboard,
                VoiceCloudIcons.DashboardSelected,
            ),
            VCNavDestination(
                CreatorDestinations.Audience,
                "Audience",
                VoiceCloudIcons.Audience,
                VoiceCloudIcons.AudienceSelected,
            ),
            VCNavDestination(
                CreatorDestinations.Analytics,
                "Analytics",
                VoiceCloudIcons.Analytics,
                VoiceCloudIcons.AnalyticsSelected,
            ),
            VCNavDestination(
                CreatorDestinations.Workspace,
                "Workspace",
                VoiceCloudIcons.CreatorTools,
                VoiceCloudIcons.CreatorToolsSelected,
            ),
        )
    }

    VCScaffold(
        modifier = modifier,
        bottomBar = {
            VCCommandBar(
                sideDestinations = sides,
                selectedRoute = route,
                onSelect = { destination -> navController.navigateCreatorTab(destination.route) },
                onLiveClick = { navController.navigateCreatorTab(CreatorDestinations.LiveStudio) },
                liveSelected = liveSelected,
                liveContentDescription = "Live Studio",
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = CreatorDestinations.Dashboard,
            modifier = Modifier.padding(padding),
            enterTransition = { if (motionEnabled) fadeIn(tween(VoiceCloudMotion.StandardMs)) else EnterTransition.None },
            exitTransition = { if (motionEnabled) fadeOut(tween(VoiceCloudMotion.FastMs)) else ExitTransition.None },
        ) {
            composable(CreatorDestinations.Dashboard) {
                CreatorSection(
                    title = "Dashboard",
                    subtitle = "Creator workspace",
                    message = "Performance, schedule, and studio shortcuts will appear here from VoiceCloud when the creator experience is connected.",
                )
            }
            composable(CreatorDestinations.Audience) {
                CreatorSection(
                    title = "Audience",
                    subtitle = "People who follow your rooms",
                    message = "Followers, subscribers, and community members will appear here from VoiceCloud when audience data is connected.",
                )
            }
            composable(CreatorDestinations.LiveStudio) {
                CreatorSection(
                    title = "Live Studio",
                    subtitle = "Host controls",
                    message = "Hosting and stage controls use the existing realtime stack when Live Studio is connected. No live session is active in this shell.",
                )
            }
            composable(CreatorDestinations.Analytics) {
                CreatorSection(
                    title = "Analytics",
                    subtitle = "How rooms perform",
                    message = "Reach, listening, and earnings charts will appear here from VoiceCloud when analytics is connected.",
                )
            }
            composable(CreatorDestinations.Workspace) {
                CreatorSection(
                    title = "Workspace",
                    subtitle = "Creator tools",
                    message = "Profile, payouts, and creator settings will use your VoiceCloud creator account when that experience is connected.",
                    actionLabel = "Leave workspace",
                    onAction = onLeaveWorkspace,
                )
            }
        }
    }
}

@Composable
private fun CreatorSection(
    title: String,
    subtitle: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxSize()) {
        VCPageHeader(title = title, subtitle = subtitle)
        VCEmptyState(
            title = title,
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
        )
    }
}

private fun NavHostController.navigateCreatorTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
