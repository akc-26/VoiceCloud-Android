package app.voicecloud.android.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.navigation.CreatorDestinations
import app.voicecloud.android.ui.creator.CreatorAnalyticsScreen
import app.voicecloud.android.ui.creator.CreatorAnalyticsUiState
import app.voicecloud.android.ui.creator.CreatorAudienceScreen
import app.voicecloud.android.ui.creator.CreatorAudienceUiState
import app.voicecloud.android.ui.creator.CreatorDashboardScreen
import app.voicecloud.android.ui.creator.CreatorDashboardUiState
import app.voicecloud.android.ui.creator.CreatorWorkspaceScreen
import app.voicecloud.android.ui.creator.CreatorWorkspaceToolsUiState
import app.voicecloud.android.ui.settings.SettingsScreen
import app.voicecloud.android.ui.settings.SettingsUiState
import app.voicecloud.android.ui.live.HostLiveMode
import app.voicecloud.android.ui.live.HostLiveScreen
import app.voicecloud.android.ui.live.HostLiveUiState
import app.voicecloud.core.designsystem.component.VCCommandBar
import app.voicecloud.core.designsystem.component.VCNavDestination
import app.voicecloud.core.designsystem.component.VCScaffold
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.android.ui.theme.VoiceCloudSystemBarAppearance
import app.voicecloud.android.ui.theme.rememberVoiceCloudDarkTheme
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
import app.voicecloud.core.preferences.VoiceCloudPreferences
import kotlinx.coroutines.launch

@Composable
fun CreatorShell(
    onLeaveWorkspace: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = rememberVoiceCloudDarkTheme()
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = darkTheme) {
        VoiceCloudSystemBarAppearance(darkTheme)
        CreatorWorkspace(
            onLeaveWorkspace = onLeaveWorkspace,
            onSignOut = onSignOut,
            modifier = modifier,
        )
    }
}

@Composable
private fun CreatorWorkspace(
    onLeaveWorkspace: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val current by navController.currentBackStackEntryAsState()
    val route = current?.destination?.route
    val liveSelected = route == CreatorDestinations.LiveStudio
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val hostLiveState = remember { HostLiveUiState() }
    val dashboardState = remember { CreatorDashboardUiState() }
    val audienceState = remember { CreatorAudienceUiState() }
    val analyticsState = remember { CreatorAnalyticsUiState() }
    val workspaceState = remember { CreatorWorkspaceToolsUiState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { VoiceCloudPreferences(context.applicationContext) }
    var settingsState by remember { mutableStateOf(SettingsUiState()) }
    LaunchedEffect(preferences) {
        preferences.theme.collect { theme ->
            settingsState = settingsState.copy(themePreference = theme)
        }
    }
    val openLiveStudio = {
        navController.navigateCreatorTab(CreatorDestinations.LiveStudio)
    }
    val openSettings = {
        navController.navigate(CreatorDestinations.Settings) { launchSingleTop = true }
    }
    val showBottomBar = route != CreatorDestinations.Settings
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
            if (showBottomBar) {
                VCCommandBar(
                    sideDestinations = sides,
                    selectedRoute = route,
                    onSelect = { destination -> navController.navigateCreatorTab(destination.route) },
                    onLiveClick = openLiveStudio,
                    liveSelected = liveSelected,
                    liveContentDescription = "Live Studio",
                )
            }
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
                CreatorDashboardScreen(
                    state = dashboardState.copy(sessionStatus = hostLiveState.sessionStatus),
                    onOpenLiveStudio = openLiveStudio,
                    onOpenAudience = { navController.navigateCreatorTab(CreatorDestinations.Audience) },
                    onOpenAnalytics = { navController.navigateCreatorTab(CreatorDestinations.Analytics) },
                    onOpenWorkspace = { navController.navigateCreatorTab(CreatorDestinations.Workspace) },
                )
            }
            composable(CreatorDestinations.Audience) {
                CreatorAudienceScreen(state = audienceState)
            }
            composable(CreatorDestinations.LiveStudio) {
                HostLiveScreen(
                    state = hostLiveState,
                    mode = HostLiveMode.Host,
                )
            }
            composable(CreatorDestinations.Analytics) {
                CreatorAnalyticsScreen(state = analyticsState)
            }
            composable(CreatorDestinations.Workspace) {
                CreatorWorkspaceScreen(
                    state = workspaceState,
                    onLeaveWorkspace = onLeaveWorkspace,
                    onOpenSettings = openSettings,
                )
            }
            composable(CreatorDestinations.Settings) {
                SettingsScreen(
                    state = settingsState,
                    onBack = { navController.popBackStack() },
                    onThemeSelected = { theme ->
                        settingsState = settingsState.copy(themePreference = theme)
                        scope.launch { preferences.setTheme(theme) }
                    },
                    onSignOut = onSignOut,
                )
            }
        }
    }
}

private fun NavHostController.navigateCreatorTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
