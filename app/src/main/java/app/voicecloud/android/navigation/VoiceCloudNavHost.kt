package app.voicecloud.android.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import app.voicecloud.core.model.MobileConfig
import app.voicecloud.feature.auth.model.FirebaseClientConfig
import app.voicecloud.feature.auth.ui.*
import app.voicecloud.feature.bootstrap.BootstrapRoute
import app.voicecloud.feature.discovery.ui.*
import app.voicecloud.feature.engagement.ui.*

object VoiceCloudRoutes {
    const val Bootstrap = "bootstrap"
    // PH01 compatibility aliases retained while PH02 owns the auth graph.
    const val UserPortal = "user"
    const val CreatorPortal = "creator"
    const val AuthGate = "auth/gate"
    const val PortalSelector = "auth/portal"
    const val UserSignIn = "auth/user/sign-in"
    const val Register = "auth/register"
    const val PhoneSignIn = "auth/phone"
    const val OtpVerify = "auth/phone/verify"
    const val ForgotPassword = "auth/forgot-password"
    const val ResetPassword = "auth/reset-password"
    const val Onboarding = "auth/onboarding"
    const val GuestUpgrade = "auth/guest-upgrade"
    const val Restricted = "auth/restricted"
    const val SessionExpired = "auth/session-expired"
    const val UserReady = "auth/user-ready"
    const val CreatorSignIn = "auth/creator/sign-in"
    const val CreatorAccess = "auth/creator/access"
    const val CreatorReady = "auth/creator-ready"
    const val Maintenance = "auth/maintenance"

    // PH03 consumer product graph. Public profile routing is human-readable by username.
    const val Home = "home"
    const val Explore = "explore"
    const val Rooms = "rooms"
    const val People = "people"
    const val Creators = "creators"
    const val Search = "search"
    const val MyProfile = "me"
    const val Followers = "followers"
    const val Following = "following"
    const val Friends = "friends"
    const val PublicProfile = "profile/{username}"

    // PH04 communities, events, messaging and notifications.
    const val Communities = "communities"
    const val CommunityDetail = "communities/{communityId}"
    const val CommunityCreate = "communities/create"
    const val CommunityManage = "communities/{communityId}/manage"
    const val CommunityMembers = "communities/{communityId}/members"
    const val CommunityEvents = "communities/{communityId}/events"
    const val Events = "events"
    const val EventDetail = "events/{eventId}"
    const val Messages = "messages"
    const val Conversation = "messages/{conversationId}"
    const val Notifications = "notifications"

    fun profile(username: String): String = "profile/${Uri.encode(username.trim())}"
    fun community(id: String): String = "communities/${Uri.encode(id.trim())}"
    fun communityManage(id: String): String = "communities/${Uri.encode(id.trim())}/manage"
    fun communityMembers(id: String): String = "communities/${Uri.encode(id.trim())}/members"
    fun communityEvents(id: String): String = "communities/${Uri.encode(id.trim())}/events"
    fun event(id: String): String = "events/${Uri.encode(id.trim())}"
    fun conversation(id: String): String = "messages/${Uri.encode(id.trim())}"
}

@Composable
fun VoiceCloudNavHost(
    navController: NavHostController,
    initialResetToken: String? = null,
    initialNavigationRoute: String? = null,
    firebaseClientConfig: FirebaseClientConfig = FirebaseClientConfig(),
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val engagementViewModel: EngagementViewModel = hiltViewModel()
    val engagementState by engagementViewModel.state.collectAsStateWithLifecycle()
    var mobileConfig by remember { mutableStateOf<MobileConfig?>(null) }
    var resetToken by remember(initialResetToken) { mutableStateOf(initialResetToken.orEmpty()) }
    var pendingExternalRoute by remember(initialNavigationRoute) { mutableStateOf(initialNavigationRoute.orEmpty()) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    fun open(route: String) {
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) { launchSingleTop = true }
        }
    }

    fun openProfile(username: String) {
        if (username.isNotBlank()) open(VoiceCloudRoutes.profile(username))
    }

    LaunchedEffect(initialResetToken, mobileConfig) {
        val incoming = initialResetToken?.trim().orEmpty()
        if (incoming.isNotBlank()) {
            resetToken = incoming
            val route = navController.currentDestination?.route
            if (mobileConfig != null && route != VoiceCloudRoutes.Bootstrap && route != VoiceCloudRoutes.ResetPassword) {
                navController.navigate(VoiceCloudRoutes.ResetPassword) { launchSingleTop = true }
            }
        }
    }

    LaunchedEffect(initialNavigationRoute) {
        val incoming = initialNavigationRoute?.trim().orEmpty()
        if (incoming.isNotBlank()) pendingExternalRoute = incoming
    }

    LaunchedEffect(authState.user?.id) {
        if (authState.user != null) engagementViewModel.syncPushToken()
    }

    LaunchedEffect(currentRoute, pendingExternalRoute, authState.user?.id) {
        val route = pendingExternalRoute.trim()
        val authenticated = authState.user != null
        val routeNow = currentRoute
        val authRoute = routeNow == null || routeNow == VoiceCloudRoutes.Bootstrap || routeNow.startsWith("auth/")
        if (route.isNotBlank() && authenticated && !authRoute) {
            pendingExternalRoute = ""
            open(route)
        }
    }

    LaunchedEffect(authViewModel) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.Notice -> Unit
                is AuthEvent.Navigate -> {
                    val route = routeFor(event.screen)
                    if (navController.currentDestination?.route != route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                            if (event.clearAuthStack) popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = VoiceCloudRoutes.Bootstrap) {
        composable(VoiceCloudRoutes.Bootstrap) {
            BootstrapRoute { config ->
                mobileConfig = config
                if (resetToken.isNotBlank()) navController.navigate(VoiceCloudRoutes.ResetPassword) { launchSingleTop = true }
                else navController.navigate(VoiceCloudRoutes.AuthGate) { launchSingleTop = true }
            }
        }
        composable(VoiceCloudRoutes.AuthGate) {
            LaunchedEffect(Unit) { authViewModel.restoreSession() }
            AuthGateScreen()
        }
        composable(VoiceCloudRoutes.PortalSelector) {
            PortalSelectorScreen(
                state = authState,
                onUser = { open(VoiceCloudRoutes.UserSignIn) },
                onCreator = { open(VoiceCloudRoutes.CreatorSignIn) },
            )
        }
        composable(VoiceCloudRoutes.UserSignIn) {
            UserSignInScreen(
                state = authState,
                supportedLoginMethods = mobileConfig?.supportedLoginMethods.orEmpty(),
                firebaseClientConfig = firebaseClientConfig,
                onLogin = { id, password -> authViewModel.login(id, password, creatorPortal = false) },
                onRegister = { open(VoiceCloudRoutes.Register) },
                onPhone = { open(VoiceCloudRoutes.PhoneSignIn) },
                onGoogleToken = { token -> authViewModel.googleLogin(token) },
                onGuest = { authViewModel.guestLogin() },
                onForgot = { open(VoiceCloudRoutes.ForgotPassword) },
                onBack = { open(VoiceCloudRoutes.PortalSelector) },
            )
        }
        composable(VoiceCloudRoutes.Register) {
            RegisterScreen(state = authState, onSubmit = authViewModel::register, onBack = { open(VoiceCloudRoutes.UserSignIn) })
        }
        composable(VoiceCloudRoutes.PhoneSignIn) {
            PhoneSignInScreen(state = authState, onSend = authViewModel::sendPhoneOtp, onBack = { open(VoiceCloudRoutes.UserSignIn) })
        }
        composable(VoiceCloudRoutes.OtpVerify) {
            OtpVerifyScreen(state = authState, onVerify = authViewModel::verifyPhoneOtp, onResend = authViewModel::resendPhoneOtp, onBack = { open(VoiceCloudRoutes.PhoneSignIn) })
        }
        composable(VoiceCloudRoutes.ForgotPassword) {
            ForgotPasswordScreen(state = authState, onSubmit = authViewModel::forgotPassword, onBack = { open(VoiceCloudRoutes.UserSignIn) })
        }
        composable(VoiceCloudRoutes.ResetPassword) {
            ResetPasswordScreen(
                state = authState,
                token = resetToken,
                onSubmit = { password -> authViewModel.resetPassword(resetToken, password) },
                onBack = { resetToken = ""; open(VoiceCloudRoutes.UserSignIn) },
            )
        }
        composable(VoiceCloudRoutes.Onboarding) {
            OnboardingScreen(state = authState, onFinish = authViewModel::completeOnboarding)
        }
        composable(VoiceCloudRoutes.UserReady) {
            // PH02's authenticated handoff is preserved; PH03 now owns the consumer landing destination.
            LaunchedEffect(Unit) {
                navController.navigate(VoiceCloudRoutes.Home) {
                    launchSingleTop = true
                    popUpTo(VoiceCloudRoutes.UserReady) { inclusive = true }
                }
            }
        }
        composable(VoiceCloudRoutes.GuestUpgrade) {
            GuestUpgradeScreen(
                state = authState,
                firebaseClientConfig = firebaseClientConfig,
                onEmailUpgrade = authViewModel::upgradeGuestEmail,
                onSendPhoneOtp = authViewModel::sendGuestUpgradePhoneOtp,
                onPhoneUpgrade = authViewModel::upgradeGuestPhone,
                onGoogleUpgrade = authViewModel::upgradeGuestGoogle,
                onBack = { open(VoiceCloudRoutes.Home) },
            )
        }
        composable(VoiceCloudRoutes.CreatorSignIn) {
            CreatorSignInScreen(
                state = authState,
                onLogin = { id, password -> authViewModel.login(id, password, creatorPortal = true) },
                onApply = { open(VoiceCloudRoutes.CreatorAccess) },
                onBack = { open(VoiceCloudRoutes.PortalSelector) },
            )
        }
        composable(VoiceCloudRoutes.CreatorAccess) {
            CreatorAccessScreen(state = authState, onSubmit = authViewModel::submitCreatorAccess, onBack = { open(VoiceCloudRoutes.CreatorSignIn) })
        }
        composable(VoiceCloudRoutes.CreatorReady) {
            CreatorReadyScreen(state = authState, onLogout = { authViewModel.logout() }, onLogoutAll = { authViewModel.logout(allDevices = true) })
        }
        composable(VoiceCloudRoutes.Restricted) {
            RestrictedScreen(state = authState, onSignIn = { open(VoiceCloudRoutes.UserSignIn) }, onPortal = { open(VoiceCloudRoutes.PortalSelector) })
        }
        composable(VoiceCloudRoutes.SessionExpired) {
            SessionExpiredScreen(state = authState, onSignIn = { open(VoiceCloudRoutes.UserSignIn) }, onPortal = { open(VoiceCloudRoutes.PortalSelector) })
        }
        composable(VoiceCloudRoutes.Maintenance) {
            MaintenanceScreen(state = authState, onPortal = { open(VoiceCloudRoutes.PortalSelector) })
        }

        composable(VoiceCloudRoutes.Home) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            HomeScreen(
                state = state,
                isGuest = viewer?.isGuest == true,
                onLoad = { vm.setViewer(viewer?.id, viewer?.username); vm.loadHome() },
                onRooms = { open(VoiceCloudRoutes.Rooms) },
                onPeople = { open(VoiceCloudRoutes.People) },
                onCreators = { open(VoiceCloudRoutes.Creators) },
                onProfile = ::openProfile,
                onUpgrade = { open(VoiceCloudRoutes.GuestUpgrade) },
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onFriends = { open(VoiceCloudRoutes.Friends) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
                onCommunities = { open(VoiceCloudRoutes.Communities) },
                onMessages = { open(VoiceCloudRoutes.Messages) },
                onNotifications = { open(VoiceCloudRoutes.Notifications) },
            )
        }
        composable(VoiceCloudRoutes.Explore) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            ExploreScreen(
                state = state,
                onLoad = { vm.setViewer(viewer?.id, viewer?.username); vm.loadExplore() },
                onRooms = { open(VoiceCloudRoutes.Rooms) },
                onPeople = { open(VoiceCloudRoutes.People) },
                onCreators = { open(VoiceCloudRoutes.Creators) },
                onProfile = ::openProfile,
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onFriends = { open(VoiceCloudRoutes.Friends) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
            )
        }
        composable(VoiceCloudRoutes.Rooms) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            RoomsScreen(state, { vm.loadRooms() }, { navController.popBackStack() })
        }
        composable(VoiceCloudRoutes.People) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            PeopleScreen(state, false, { vm.setViewer(viewer?.id, viewer?.username); vm.loadPeople(false) }, ::openProfile, { navController.popBackStack() })
        }
        composable(VoiceCloudRoutes.Creators) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            PeopleScreen(state, true, { vm.setViewer(viewer?.id, viewer?.username); vm.loadPeople(true) }, ::openProfile, { navController.popBackStack() })
        }
        composable(VoiceCloudRoutes.Search) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            SearchScreen(
                state = state,
                onSubmit = { query -> vm.setViewer(viewer?.id, viewer?.username); vm.search(query) },
                onProfile = ::openProfile,
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onFriends = { open(VoiceCloudRoutes.Friends) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
            )
        }
        composable(VoiceCloudRoutes.PublicProfile) { backStackEntry ->
            val username = Uri.decode(backStackEntry.arguments?.getString("username").orEmpty())
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            PublicProfileScreen(
                state = state,
                username = username,
                isSelf = viewer?.username?.equals(username, ignoreCase = true) == true,
                onLoad = { vm.setViewer(viewer?.id, viewer?.username); vm.loadPublicProfile(username) },
                onFollow = vm::followProfile,
                onMessage = { userId -> engagementViewModel.startDirectConversation(userId) { conversation -> open(VoiceCloudRoutes.conversation(conversation.id)) } },
                onMyProfile = { open(VoiceCloudRoutes.MyProfile) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.MyProfile) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            MyProfileScreen(
                state = state,
                isGuest = authState.user?.isGuest == true,
                onLoad = vm::loadMyProfile,
                onFollowers = { open(VoiceCloudRoutes.Followers) },
                onFollowing = { open(VoiceCloudRoutes.Following) },
                onUpgrade = { open(VoiceCloudRoutes.GuestUpgrade) },
                onLogout = { authViewModel.logout() },
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onFriends = { open(VoiceCloudRoutes.Friends) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
            )
        }
        composable(VoiceCloudRoutes.Followers) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            SocialListScreen(
                state = state,
                mode = "followers",
                onLoad = { search -> vm.setViewer(viewer?.id, viewer?.username); vm.loadSocial("followers", search) },
                onProfile = ::openProfile,
                onUnfollow = { _, _ -> Unit },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.Following) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            SocialListScreen(
                state = state,
                mode = "following",
                onLoad = { search -> vm.setViewer(viewer?.id, viewer?.username); vm.loadSocial("following", search) },
                onProfile = ::openProfile,
                onUnfollow = vm::unfollowFromList,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.Friends) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            FriendsScreen(
                state = state,
                onLoad = { vm.setViewer(viewer?.id, viewer?.username); vm.loadFriends() },
                onProfile = ::openProfile,
                onSend = vm::sendFriendRequest,
                onAccept = vm::acceptFriendRequest,
                onReject = vm::rejectFriendRequest,
                onRemove = vm::removeFriend,
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onFriends = { open(VoiceCloudRoutes.Friends) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
            )
        }

        composable(VoiceCloudRoutes.Communities) {
            CommunitiesScreen(
                state = engagementState,
                onLoad = engagementViewModel::loadCommunities,
                onOpen = { open(VoiceCloudRoutes.community(it)) },
                onCreate = { if (authState.user?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade) else open(VoiceCloudRoutes.CommunityCreate) },
                onEvents = { open(VoiceCloudRoutes.Events) },
                onMessages = { open(VoiceCloudRoutes.Messages) },
                onNotifications = { open(VoiceCloudRoutes.Notifications) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.CommunityCreate) {
            CommunityEditorScreen(
                state = engagementState,
                onSubmit = { input -> engagementViewModel.createCommunity(input) { created -> open(VoiceCloudRoutes.community(created.handle.ifBlank { created.id })) } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.CommunityDetail) { entry ->
            val communityId = Uri.decode(entry.arguments?.getString("communityId").orEmpty())
            CommunityDetailScreen(
                state = engagementState,
                id = communityId,
                isGuest = authState.user?.isGuest == true,
                onLoad = { engagementViewModel.loadCommunity(communityId) },
                onJoin = engagementViewModel::joinCommunity,
                onLeave = engagementViewModel::leaveCommunity,
                onManage = { engagementState.community?.id?.let { open(VoiceCloudRoutes.communityManage(it)) } },
                onMembers = { engagementState.community?.id?.let { open(VoiceCloudRoutes.communityMembers(it)) } },
                onEvents = { engagementState.community?.id?.let { open(VoiceCloudRoutes.communityEvents(it)) } },
                onUpgrade = { open(VoiceCloudRoutes.GuestUpgrade) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.CommunityManage) { entry ->
            val communityId = Uri.decode(entry.arguments?.getString("communityId").orEmpty())
            LaunchedEffect(communityId) { engagementViewModel.loadCommunity(communityId) }
            CommunityEditorScreen(
                state = engagementState,
                existing = engagementState.community,
                onSubmit = engagementViewModel::updateCommunity,
                onRotateInvite = engagementViewModel::rotateInviteCode,
                onMembers = { engagementState.community?.id?.let { open(VoiceCloudRoutes.communityMembers(it)) } },
                onDelete = { engagementViewModel.deleteCommunity { open(VoiceCloudRoutes.Communities) } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.CommunityMembers) { entry ->
            val communityId = Uri.decode(entry.arguments?.getString("communityId").orEmpty())
            LaunchedEffect(communityId) { engagementViewModel.loadCommunity(communityId) }
            CommunityMembersScreen(
                state = engagementState,
                canManage = engagementState.membership?.role?.uppercase() in setOf("OWNER", "ADMIN"),
                viewerRole = engagementState.membership?.role,
                viewerId = authState.user?.id,
                onRole = engagementViewModel::updateMemberRole,
                onRemove = engagementViewModel::removeMember,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.CommunityEvents) { entry ->
            val communityId = Uri.decode(entry.arguments?.getString("communityId").orEmpty())
            LaunchedEffect(communityId) { engagementViewModel.loadCommunity(communityId) }
            EventsScreen(
                state = engagementState.copy(events = engagementState.communityEvents),
                onLoad = { _ -> engagementViewModel.loadCommunity(communityId) },
                onOpen = { open(VoiceCloudRoutes.event(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.Events) {
            EventsScreen(engagementState, engagementViewModel::loadEvents, { open(VoiceCloudRoutes.event(it)) }, { navController.popBackStack() })
        }
        composable(VoiceCloudRoutes.EventDetail) { entry ->
            val eventId = Uri.decode(entry.arguments?.getString("eventId").orEmpty())
            EventDetailScreen(
                state = engagementState,
                onLoad = { engagementViewModel.loadEvent(eventId) },
                onReminder = engagementViewModel::remindEvent,
                onCommunity = { open(VoiceCloudRoutes.community(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.Messages) {
            MessagesScreen(
                state = engagementState,
                onLoad = engagementViewModel::loadConversations,
                onOpen = { open(VoiceCloudRoutes.conversation(it)) },
                onDelete = engagementViewModel::deleteConversation,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.Conversation) { entry ->
            val conversationId = Uri.decode(entry.arguments?.getString("conversationId").orEmpty())
            ConversationScreen(
                state = engagementState,
                viewerId = authState.user?.id,
                onLoad = { engagementViewModel.loadConversation(conversationId) },
                onRefresh = { engagementViewModel.refreshConversationSilently(conversationId) },
                onSend = engagementViewModel::sendMessage,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.Notifications) {
            NotificationsScreen(
                state = engagementState,
                onLoad = { engagementViewModel.loadNotifications() },
                onOpen = ::open,
                onRead = engagementViewModel::markNotificationRead,
                onReadAll = engagementViewModel::markAllNotificationsRead,
                onDelete = engagementViewModel::deleteNotification,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun routeFor(screen: AuthScreen): String = when (screen) {
    AuthScreen.PORTAL_SELECTOR -> VoiceCloudRoutes.PortalSelector
    AuthScreen.USER_SIGN_IN -> VoiceCloudRoutes.UserSignIn
    AuthScreen.REGISTER -> VoiceCloudRoutes.Register
    AuthScreen.PHONE_SIGN_IN -> VoiceCloudRoutes.PhoneSignIn
    AuthScreen.OTP_VERIFY -> VoiceCloudRoutes.OtpVerify
    AuthScreen.FORGOT_PASSWORD -> VoiceCloudRoutes.ForgotPassword
    AuthScreen.RESET_PASSWORD -> VoiceCloudRoutes.ResetPassword
    AuthScreen.ONBOARDING -> VoiceCloudRoutes.Onboarding
    AuthScreen.GUEST_UPGRADE -> VoiceCloudRoutes.GuestUpgrade
    AuthScreen.RESTRICTED -> VoiceCloudRoutes.Restricted
    AuthScreen.SESSION_EXPIRED -> VoiceCloudRoutes.SessionExpired
    AuthScreen.USER_READY -> VoiceCloudRoutes.UserReady
    AuthScreen.CREATOR_SIGN_IN -> VoiceCloudRoutes.CreatorSignIn
    AuthScreen.CREATOR_ACCESS -> VoiceCloudRoutes.CreatorAccess
    AuthScreen.CREATOR_READY -> VoiceCloudRoutes.CreatorReady
    AuthScreen.MAINTENANCE -> VoiceCloudRoutes.Maintenance
}
