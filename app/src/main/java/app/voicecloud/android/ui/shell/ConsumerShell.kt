package app.voicecloud.android.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.navigation.ConsumerDestinations
import app.voicecloud.android.ui.consumer.ConsumerHomeUiState
import app.voicecloud.android.ui.consumer.HomeScreen
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCCommandBar
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCIconButton
import app.voicecloud.core.designsystem.component.VCNavDestination
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCScaffold
import app.voicecloud.core.designsystem.component.VCSearchField
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun ConsumerShell(
    onOpenCreatorWorkspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val current by navController.currentBackStackEntryAsState()
    val route = current?.destination?.route
    val liveSelected = route == ConsumerDestinations.Live
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val homeState = remember { ConsumerHomeUiState() }
    val sides = remember {
        listOf(
            VCNavDestination(ConsumerDestinations.Home, "Home", VoiceCloudIcons.Home, VoiceCloudIcons.HomeSelected),
            VCNavDestination(ConsumerDestinations.Discover, "Discover", VoiceCloudIcons.Explore, VoiceCloudIcons.ExploreSelected),
            VCNavDestination(ConsumerDestinations.Messages, "Messages", VoiceCloudIcons.Messages, VoiceCloudIcons.MessagesSelected),
            VCNavDestination(ConsumerDestinations.Profile, "Profile", VoiceCloudIcons.Profile, VoiceCloudIcons.ProfileSelected),
        )
    }

    VCScaffold(
        modifier = modifier,
        bottomBar = {
            VCCommandBar(
                sideDestinations = sides,
                selectedRoute = route,
                onSelect = { destination -> navController.navigateTab(destination.route) },
                onLiveClick = { navController.navigateTab(ConsumerDestinations.Live) },
                liveSelected = liveSelected,
                liveContentDescription = "Live",
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ConsumerDestinations.Home,
            modifier = Modifier.padding(padding),
            enterTransition = { if (motionEnabled) fadeIn(tween(VoiceCloudMotion.StandardMs)) else EnterTransition.None },
            exitTransition = { if (motionEnabled) fadeOut(tween(VoiceCloudMotion.FastMs)) else ExitTransition.None },
        ) {
            composable(ConsumerDestinations.Home) {
                HomeScreen(state = homeState)
            }
            composable(ConsumerDestinations.Discover) { DiscoverSection() }
            composable(ConsumerDestinations.Live) {
                ShellSection(
                    title = "Live",
                    subtitle = "Rooms",
                    message = "Joining and hosting live audio uses the existing realtime stack when that experience is connected. No room is active in this shell.",
                )
            }
            composable(ConsumerDestinations.Messages) {
                ShellSection(
                    title = "Messages",
                    subtitle = "Conversations",
                    message = "Conversations will appear here from VoiceCloud when messaging is connected.",
                )
            }
            composable(ConsumerDestinations.Profile) {
                ShellSection(
                    title = "Profile",
                    subtitle = "You",
                    message = "Your profile will use your VoiceCloud account when authentication is connected.",
                    actionLabel = "Creator workspace",
                    onAction = onOpenCreatorWorkspace,
                )
            }
        }
    }
}

@Composable
private fun DiscoverSection() {
    val spacing = VoiceCloud.spacing
    var query by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        VCPageHeader(
            title = "Discover",
            subtitle = "Find rooms and people",
            actions = {
                VCIconButton(
                    icon = VoiceCloudIcons.Search,
                    contentDescription = "Search",
                    onClick = {},
                )
            },
        )
        VCSearchField(
            query = query,
            onQueryChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.pageGutter),
            placeholder = "Search VoiceCloud",
            onSearch = {},
        )
        VCEmptyState(
            title = "Nothing to show yet",
            message = "Search results and discovery feeds will use live VoiceCloud data when this experience is connected.",
        )
    }
}
