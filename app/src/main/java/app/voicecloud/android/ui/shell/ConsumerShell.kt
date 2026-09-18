package app.voicecloud.android.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.navigation.ConsumerDestinations
import app.voicecloud.android.ui.consumer.ConsumerExploreUiState
import app.voicecloud.android.ui.consumer.ConsumerHomeUiState
import app.voicecloud.android.ui.consumer.ConsumerSearchUiState
import app.voicecloud.android.ui.consumer.ExploreScreen
import app.voicecloud.android.ui.consumer.HomeScreen
import app.voicecloud.android.ui.consumer.SearchScreen
import app.voicecloud.android.ui.live.HostLiveMode
import app.voicecloud.android.ui.live.HostLiveScreen
import app.voicecloud.android.ui.live.HostLiveUiState
import app.voicecloud.android.ui.economy.ConsumerEconomyUiState
import app.voicecloud.android.ui.economy.EconomyScreen
import app.voicecloud.android.ui.messaging.ConsumerConversationUiState
import app.voicecloud.android.ui.messaging.ConsumerMessagesUiState
import app.voicecloud.android.ui.messaging.ConversationScreen
import app.voicecloud.android.ui.messaging.MessagesScreen
import app.voicecloud.android.ui.messaging.VCConversationUiModel
import app.voicecloud.android.ui.profile.ConsumerProfileUiState
import app.voicecloud.android.ui.profile.ProfileScreen
import app.voicecloud.android.ui.room.RoomPreviewScreen
import app.voicecloud.android.ui.room.RoomPreviewUiState
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.designsystem.component.VCGiftUiModel
import app.voicecloud.core.designsystem.component.VCCommandBar
import app.voicecloud.core.designsystem.component.VCNavDestination
import app.voicecloud.core.designsystem.component.VCScaffold
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
    val exploreState = remember { ConsumerExploreUiState() }
    var searchState by remember { mutableStateOf(ConsumerSearchUiState()) }
    var roomPreviewState by remember { mutableStateOf<RoomPreviewUiState?>(null) }
    var profileState by remember { mutableStateOf(ConsumerProfileUiState()) }
    var messagesState by remember { mutableStateOf(ConsumerMessagesUiState()) }
    var messagesQuery by remember { mutableStateOf("") }
    var conversationState by remember { mutableStateOf<ConsumerConversationUiState?>(null) }
    var economyState by remember { mutableStateOf(ConsumerEconomyUiState()) }
    var hostLiveState by remember { mutableStateOf(HostLiveUiState()) }
    var selectedGift by remember { mutableStateOf<VCGiftUiModel?>(null) }
    val sides = remember {
        listOf(
            VCNavDestination(ConsumerDestinations.Home, "Home", VoiceCloudIcons.Home, VoiceCloudIcons.HomeSelected),
            VCNavDestination(ConsumerDestinations.Discover, "Discover", VoiceCloudIcons.Explore, VoiceCloudIcons.ExploreSelected),
            VCNavDestination(ConsumerDestinations.Messages, "Messages", VoiceCloudIcons.Messages, VoiceCloudIcons.MessagesSelected),
            VCNavDestination(ConsumerDestinations.Profile, "Profile", VoiceCloudIcons.Profile, VoiceCloudIcons.ProfileSelected),
        )
    }
    val openSearch = {
        navController.navigate(ConsumerDestinations.Search) { launchSingleTop = true }
    }
    val openRoomPreview: (VCRoomUiModel) -> Unit = { room ->
        roomPreviewState = RoomPreviewUiState.fromRoom(room)
        navController.navigate(ConsumerDestinations.RoomPreview) { launchSingleTop = true }
    }
    val joinRoom = {
        navController.navigateTab(ConsumerDestinations.Live)
    }
    val openConversation: (VCConversationUiModel) -> Unit = { conversation ->
        conversationState = ConsumerConversationUiState(
            conversationId = conversation.id,
            title = conversation.title,
        )
        navController.navigate(ConsumerDestinations.MessageThread) { launchSingleTop = true }
    }
    val openWallet = {
        navController.navigate(ConsumerDestinations.Wallet) { launchSingleTop = true }
    }
    val showBottomBar = route != ConsumerDestinations.RoomPreview &&
        route != ConsumerDestinations.MessageThread &&
        route != ConsumerDestinations.Wallet

    VCScaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                VCCommandBar(
                    sideDestinations = sides,
                    selectedRoute = route,
                    onSelect = { destination -> navController.navigateTab(destination.route) },
                    onLiveClick = { navController.navigateTab(ConsumerDestinations.Live) },
                    liveSelected = liveSelected,
                    liveContentDescription = "Live",
                )
            }
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
                HomeScreen(state = homeState, onOpenSearch = openSearch, onRoomClick = openRoomPreview)
            }
            composable(ConsumerDestinations.Discover) {
                ExploreScreen(state = exploreState, onOpenSearch = openSearch, onRoomClick = openRoomPreview)
            }
            composable(ConsumerDestinations.Search) {
                SearchScreen(
                    state = searchState,
                    onQueryChange = { query ->
                        searchState = searchState.copy(query = query, errorMessage = null)
                    },
                    onSearch = {
                        searchState = searchState.copy(
                            isSearching = searchState.query.isNotBlank(),
                            isLoading = false,
                        )
                    },
                    onBack = { navController.popBackStack() },
                    onRoomClick = openRoomPreview,
                )
            }
            composable(ConsumerDestinations.RoomPreview) {
                roomPreviewState?.let { preview ->
                    RoomPreviewScreen(
                        state = preview,
                        onBack = { navController.popBackStack() },
                        onJoinRoom = joinRoom,
                    )
                }
            }
            composable(ConsumerDestinations.Live) {
                HostLiveScreen(
                    state = hostLiveState,
                    mode = HostLiveMode.Audience,
                )
            }
            composable(ConsumerDestinations.Messages) {
                MessagesScreen(
                    state = messagesState,
                    query = messagesQuery,
                    onQueryChange = { messagesQuery = it },
                    onConversationClick = openConversation,
                )
            }
            composable(ConsumerDestinations.MessageThread) {
                conversationState?.let { thread ->
                    ConversationScreen(
                        state = thread,
                        onBack = { navController.popBackStack() },
                        onComposerChange = { text ->
                            conversationState = conversationState?.copy(composerText = text)
                        },
                        onSend = {
                            conversationState = conversationState?.copy(composerText = "")
                        },
                    )
                }
            }
            composable(ConsumerDestinations.Profile) {
                ProfileScreen(
                    state = profileState,
                    onOpenCreatorWorkspace = onOpenCreatorWorkspace,
                    onOpenWallet = openWallet,
                )
            }
            composable(ConsumerDestinations.Wallet) {
                EconomyScreen(
                    state = economyState,
                    onBack = { navController.popBackStack() },
                    selectedGift = selectedGift,
                    onGiftSelect = { gift -> selectedGift = gift },
                    onGiftSheetDismiss = { selectedGift = null },
                )
            }
        }
    }
}
