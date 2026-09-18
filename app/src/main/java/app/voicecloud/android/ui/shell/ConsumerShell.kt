package app.voicecloud.android.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.voicecloud.android.navigation.ConsumerDestinations
import app.voicecloud.android.ui.consumer.ExploreScreen
import app.voicecloud.android.ui.consumer.HomeScreen
import app.voicecloud.android.ui.consumer.SearchScreen
import app.voicecloud.android.ui.live.HostLiveMode
import app.voicecloud.android.ui.live.HostLiveScreen
import app.voicecloud.android.ui.economy.EconomyScreen
import app.voicecloud.android.ui.messaging.ConversationScreen
import app.voicecloud.android.ui.messaging.MessagesScreen
import app.voicecloud.android.ui.messaging.VCConversationUiModel
import app.voicecloud.android.ui.profile.ProfileScreen
import app.voicecloud.android.ui.settings.SettingsScreen
import app.voicecloud.android.ui.settings.SettingsUiState
import app.voicecloud.android.ui.room.RoomPreviewScreen
import app.voicecloud.android.ui.room.RoomPreviewUiState
import app.voicecloud.android.viewmodel.ConversationViewModel
import app.voicecloud.android.viewmodel.EconomyViewModel
import app.voicecloud.android.viewmodel.ExploreViewModel
import app.voicecloud.android.viewmodel.HomeViewModel
import app.voicecloud.android.viewmodel.HostLiveViewModel
import app.voicecloud.android.viewmodel.MessagesViewModel
import app.voicecloud.android.viewmodel.ProfileViewModel
import app.voicecloud.android.viewmodel.SearchViewModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.designsystem.component.VCGiftUiModel
import app.voicecloud.core.designsystem.component.VCCommandBar
import app.voicecloud.core.designsystem.component.VCNavDestination
import app.voicecloud.core.designsystem.component.VCScaffold
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
import app.voicecloud.android.navigation.SecurityDestinations
import app.voicecloud.core.model.VoiceCloudAccountRole
import app.voicecloud.feature.auth.AuthGateViewModel
import app.voicecloud.feature.auth.AuthSessionState
import app.voicecloud.feature.auth.LoginHistoryViewModel
import app.voicecloud.feature.auth.SessionListViewModel
import app.voicecloud.feature.auth.ui.LoginHistoryScreen
import app.voicecloud.feature.auth.ui.SessionListScreen
import app.voicecloud.core.preferences.VoiceCloudPreferences
import kotlinx.coroutines.launch

@Composable
fun ConsumerShell(
    onOpenCreatorWorkspace: () -> Unit,
    onSignOut: () -> Unit,
    onOpenGuestUpgrade: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val current by navController.currentBackStackEntryAsState()
    val route = current?.destination?.route
    val liveSelected = route == ConsumerDestinations.Live
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val exploreViewModel: ExploreViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val messagesViewModel: MessagesViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val economyViewModel: EconomyViewModel = hiltViewModel()
    val hostLiveViewModel: HostLiveViewModel = hiltViewModel()
    val authGateViewModel: AuthGateViewModel = hiltViewModel()
    val sessionState by authGateViewModel.sessionState.collectAsStateWithLifecycle()
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()
    val exploreState by exploreViewModel.state.collectAsStateWithLifecycle()
    val searchState by searchViewModel.state.collectAsStateWithLifecycle()
    val messagesState by messagesViewModel.state.collectAsStateWithLifecycle()
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val economyState by economyViewModel.state.collectAsStateWithLifecycle()
    val hostLiveState by hostLiveViewModel.state.collectAsStateWithLifecycle()
    var roomPreviewState by remember { mutableStateOf<RoomPreviewUiState?>(null) }
    var messagesQuery by remember { mutableStateOf("") }
    var activeConversation by remember { mutableStateOf<VCConversationUiModel?>(null) }
    var pendingLiveRoomId by remember { mutableStateOf<String?>(null) }
    var selectedGift by remember { mutableStateOf<VCGiftUiModel?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { VoiceCloudPreferences(context.applicationContext) }
    var settingsState by remember { mutableStateOf(SettingsUiState()) }
    LaunchedEffect(sessionState, profileState) {
        val authUser = (sessionState as? AuthSessionState.Authenticated)?.user
        settingsState = settingsState.copy(
            accountDisplayName = authUser?.displayName ?: profileState.displayName,
            accountHandle = authUser?.username?.let { "@$it" } ?: profileState.handle,
            isGuestAccount = authUser?.accountRole == VoiceCloudAccountRole.GUEST,
        )
    }
    LaunchedEffect(preferences) {
        preferences.theme.collect { theme ->
            settingsState = settingsState.copy(themePreference = theme)
        }
    }
    LaunchedEffect(route, pendingLiveRoomId) {
        val roomId = pendingLiveRoomId
        if (route == ConsumerDestinations.Live && !roomId.isNullOrBlank()) {
            hostLiveViewModel.joinRoom(roomId)
            pendingLiveRoomId = null
        }
    }
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
        pendingLiveRoomId = roomPreviewState?.room?.id?.takeIf { it.isNotBlank() }
        navController.navigateTab(ConsumerDestinations.Live)
    }
    val openConversation: (VCConversationUiModel) -> Unit = { conversation ->
        activeConversation = conversation
        navController.navigate(ConsumerDestinations.MessageThread) { launchSingleTop = true }
    }
    val openWallet = {
        navController.navigate(ConsumerDestinations.Wallet) { launchSingleTop = true }
    }
    val openSettings = {
        navController.navigate(ConsumerDestinations.Settings) { launchSingleTop = true }
    }
    val showBottomBar = route != ConsumerDestinations.RoomPreview &&
        route != ConsumerDestinations.MessageThread &&
        route != ConsumerDestinations.Wallet &&
        route != ConsumerDestinations.Settings &&
        route != SecurityDestinations.Sessions &&
        route != SecurityDestinations.LoginHistory

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
                    onQueryChange = searchViewModel::onQueryChange,
                    onSearch = searchViewModel::search,
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
                val conversationViewModel: ConversationViewModel = hiltViewModel()
                val threadState by conversationViewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(activeConversation?.id) {
                    activeConversation?.let { conversationViewModel.bind(it.id, it.title) }
                }
                threadState?.let { thread ->
                    ConversationScreen(
                        state = thread,
                        onBack = { navController.popBackStack() },
                        onComposerChange = conversationViewModel::onComposerChange,
                        onSend = conversationViewModel::send,
                    )
                }
            }
            composable(ConsumerDestinations.Profile) {
                ProfileScreen(
                    state = profileState,
                    onOpenCreatorWorkspace = onOpenCreatorWorkspace,
                    onOpenWallet = openWallet,
                    onOpenSettings = openSettings,
                )
            }
            composable(ConsumerDestinations.Settings) {
                SettingsScreen(
                    state = settingsState,
                    onBack = { navController.popBackStack() },
                    onThemeSelected = { theme ->
                        settingsState = settingsState.copy(themePreference = theme)
                        scope.launch { preferences.setTheme(theme) }
                    },
                    onOpenCreatorWorkspace = onOpenCreatorWorkspace,
                    onOpenGuestUpgrade = if (settingsState.isGuestAccount) onOpenGuestUpgrade else null,
                    onOpenSessions = {
                        navController.navigate(SecurityDestinations.Sessions) { launchSingleTop = true }
                    },
                    onOpenLoginHistory = {
                        navController.navigate(SecurityDestinations.LoginHistory) { launchSingleTop = true }
                    },
                    onSignOut = onSignOut,
                )
            }
            composable(SecurityDestinations.Sessions) {
                SessionListScreen(
                    title = "Active sessions",
                    viewModel = hiltViewModel<SessionListViewModel>(),
                    onBack = { navController.popBackStack() },
                    revokeEnabled = true,
                )
            }
            composable(SecurityDestinations.LoginHistory) {
                LoginHistoryScreen(
                    viewModel = hiltViewModel<LoginHistoryViewModel>(),
                    onBack = { navController.popBackStack() },
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
