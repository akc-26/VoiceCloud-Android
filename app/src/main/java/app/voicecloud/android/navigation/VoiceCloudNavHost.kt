package app.voicecloud.android.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import app.voicecloud.core.model.MobileConfig
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.feature.auth.model.FirebaseClientConfig
import app.voicecloud.feature.auth.model.VoiceCloudRole
import app.voicecloud.feature.auth.ui.*
import app.voicecloud.feature.bootstrap.BootstrapRoute
import app.voicecloud.feature.discovery.ui.*
import app.voicecloud.feature.creator.model.CreatorEvent
import app.voicecloud.feature.creator.model.CreatorFollowerSort
import app.voicecloud.feature.creator.ui.*
import app.voicecloud.feature.engagement.ui.*
import app.voicecloud.feature.live.ui.*
import app.voicecloud.feature.hosting.ui.*
import app.voicecloud.feature.economy.model.EconomySection
import app.voicecloud.feature.economy.ui.*
import app.voicecloud.feature.profile.ui.*
import app.voicecloud.feature.settings.model.ReportTargetType
import app.voicecloud.feature.settings.ui.*

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
    const val CommunitySearch = "search/communities"
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

    // PH05 listener live-room graph. Host/publisher controls remain outside this phase.
    const val RoomPreview = "rooms/{roomId}/preview"
    const val RoomExperience = "rooms/{roomId}/live"

    // PH06 host/speaker management graph.
    const val HostStudio = "host"
    const val HostRoomCreate = "host/rooms/create"
    const val HostRoomManage = "host/rooms/{roomId}/manage"
    const val HostRoomSettings = "host/rooms/{roomId}/settings"
    const val HostLiveConsole = "host/rooms/{roomId}/console"
    const val HostScheduleCreate = "host/schedules/create"
    const val HostScheduleEdit = "host/schedules/{scheduleId}/edit"
    const val HostInteractive = "host/rooms/{roomId}/interactive"

    // PH07 consumer economy & progression.
    const val Economy = "economy"
    const val EconomySection = "economy/{section}"

    // PH08 replay, activity and extended-profile graph.
    const val ProfileTools = "me/tools"
    const val EditProfile = "me/edit"
    const val ReplayLibrary = "replays"
    const val ReplayPlayer = "replays/{replayId}"
    const val ActivityHistory = "me/activity"
    const val ProfileVisitors = "me/visitors"
    const val BlockedUsers = "me/blocked"

    // PH09 preferences, security, safety, CMS and support graph.
    const val Settings = "settings"
    const val NotificationSettings = "settings/notifications"
    const val PrivacySettings = "settings/privacy"
    const val VoiceAppearance = "settings/voice-appearance"
    const val Security = "security"
    const val SessionsDevices = "security/devices"
    const val SessionDetail = "security/sessions/{sessionId}"
    const val DeviceDetail = "security/devices/{deviceId}"
    const val LoginActivity = "security/login-activity"
    const val HelpCenter = "help"
    const val CmsContent = "content/{slug}"
    const val SafetyCenter = "safety"
    const val Report = "report"
    const val ContextReport = "report/{targetType}/{targetId}/{targetLabel}"
    const val ContactSupport = "support/contact"
    const val About = "about"

    // PH10 Creator Portal core graph. Later Creator feature phases remain separate.
    const val CreatorDashboard = "creator/dashboard"
    const val CreatorProfile = "creator/profile"
    const val CreatorSettings = "creator/settings"
    const val CreatorHelp = "creator/help"
    const val CreatorCmsContent = "creator/content/{slug}"
    const val CreatorContactSupport = "creator/support/contact"

    // PH11 Creator Live Rooms & Schedule graph.
    const val CreatorLive = "creator/live"
    const val CreatorRoomCreate = "creator/live/rooms/create"
    const val CreatorRoomManage = "creator/live/rooms/{roomId}"
    const val CreatorRoomSettings = "creator/live/rooms/{roomId}/settings"
    const val CreatorLiveConsole = "creator/live/rooms/{roomId}/console"
    const val CreatorScheduleCreate = "creator/live/schedules/create"
    const val CreatorScheduleEdit = "creator/live/schedules/{scheduleId}/edit"

    // PH12 Creator Audience, Messaging, Followers & Subscribers graph.
    const val CreatorAudience = "creator/audience"
    const val CreatorFollowers = "creator/audience/followers"
    const val CreatorSubscribers = "creator/audience/subscribers"
    const val CreatorPlans = "creator/audience/plans"
    const val CreatorMessages = "creator/messages"
    const val CreatorConversation = "creator/messages/{conversationId}"

    // PH13 Creator analytics, economy, notifications and host-verification graph.
    const val CreatorAnalytics = "creator/analytics"
    const val CreatorWallet = "creator/wallet"
    const val CreatorEarnings = "creator/earnings"
    const val CreatorGifts = "creator/gifts"
    const val CreatorPayouts = "creator/payouts"
    const val CreatorPayoutDetail = "creator/payouts/{payoutId}"
    const val CreatorNotifications = "creator/notifications"
    const val CreatorVerification = "creator/verification"
    const val HostVerification = "host/verification"

    fun creatorCmsContent(slug: String): String = "creator/content/${Uri.encode(slug.trim())}"
    fun creatorRoom(id: String): String = "creator/live/rooms/${Uri.encode(id.trim())}"
    fun creatorRoomSettings(id: String): String = "creator/live/rooms/${Uri.encode(id.trim())}/settings"
    fun creatorConsole(id: String): String = "creator/live/rooms/${Uri.encode(id.trim())}/console"
    fun creatorSchedule(id: String): String = "creator/live/schedules/${Uri.encode(id.trim())}/edit"
    fun creatorConversation(id: String): String = "creator/messages/${Uri.encode(id.trim())}"
    fun creatorPayout(id: String): String = "creator/payouts/${Uri.encode(id.trim())}"

    fun economySection(section: app.voicecloud.feature.economy.model.EconomySection): String = "economy/${section.name}"

    fun replay(id: String): String = "replays/${Uri.encode(id.trim())}"
    fun cmsContent(slug: String): String = "content/${Uri.encode(slug.trim())}"
    fun securitySession(id: String): String = "security/sessions/${Uri.encode(id.trim())}"
    fun securityDevice(id: String): String = "security/devices/${Uri.encode(id.trim())}"
    fun contextualReport(type: ReportTargetType, id: String, label: String): String =
        "report/${type.name}/${Uri.encode(id.trim())}/${Uri.encode(label.trim().ifBlank { if (type == ReportTargetType.USER) "VoiceCloud member" else "VoiceCloud room" })}"

    fun roomPreview(id: String): String = "rooms/${Uri.encode(id.trim())}/preview"
    fun roomExperience(id: String): String = "rooms/${Uri.encode(id.trim())}/live"

    fun profile(username: String): String = "profile/${Uri.encode(username.trim())}"
    fun community(id: String): String = "communities/${Uri.encode(id.trim())}"
    fun communityManage(id: String): String = "communities/${Uri.encode(id.trim())}/manage"
    fun communityMembers(id: String): String = "communities/${Uri.encode(id.trim())}/members"
    fun communityEvents(id: String): String = "communities/${Uri.encode(id.trim())}/events"
    fun event(id: String): String = "events/${Uri.encode(id.trim())}"
    fun conversation(id: String): String = "messages/${Uri.encode(id.trim())}"

    fun hostRoom(id: String): String = "host/rooms/${Uri.encode(id.trim())}/manage"
    fun hostRoomSettings(id: String): String = "host/rooms/${Uri.encode(id.trim())}/settings"
    fun hostConsole(id: String): String = "host/rooms/${Uri.encode(id.trim())}/console"
    fun hostSchedule(id: String): String = "host/schedules/${Uri.encode(id.trim())}/edit"
    fun hostInteractive(id: String): String = "host/rooms/${Uri.encode(id.trim())}/interactive"
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
    val hostingViewModel: HostingViewModel = hiltViewModel()
    val hostingState by hostingViewModel.state.collectAsStateWithLifecycle()
    val economyViewModel: EconomyViewModel = hiltViewModel()
    val economyState by economyViewModel.state.collectAsStateWithLifecycle()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val creatorViewModel: CreatorViewModel = hiltViewModel()
    val creatorState by creatorViewModel.state.collectAsStateWithLifecycle()
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
        hostingViewModel.setViewer(authState.user?.id)
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

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.events.collect { event ->
            when (event) {
                SettingsEvent.SessionExpired -> authViewModel.handleAuthenticatedFailure(401)
                is SettingsEvent.Maintenance -> authViewModel.handleAuthenticatedFailure(503, event.message)
            }
        }
    }

    LaunchedEffect(creatorViewModel) {
        creatorViewModel.events.collect { event ->
            when (event) {
                is CreatorEvent.AuthFailure -> authViewModel.handleAuthenticatedFailure(event.httpStatus, event.message)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = VoiceCloudRoutes.Bootstrap,
        enterTransition = { tabEnterTransition(initialState.destination.route, targetState.destination.route) },
        exitTransition = { tabExitTransition(initialState.destination.route, targetState.destination.route) },
        popEnterTransition = { tabEnterTransition(initialState.destination.route, targetState.destination.route) },
        popExitTransition = { tabExitTransition(initialState.destination.route, targetState.destination.route) },
    ) {
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
                onGuest = { authViewModel.guestLogin() },
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
                onBack = { open(VoiceCloudRoutes.CreatorSignIn) },
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
                onBack = { open(VoiceCloudRoutes.UserSignIn) },
            )
        }
        composable(VoiceCloudRoutes.CreatorAccess) {
            CreatorAccessScreen(state = authState, onSubmit = authViewModel::submitCreatorAccess, onBack = { open(VoiceCloudRoutes.CreatorSignIn) })
        }
        composable(VoiceCloudRoutes.CreatorReady) {
            LaunchedEffect(authState.user?.id, authState.user?.role) {
                if (authState.user?.normalizedRole == VoiceCloudRole.CREATOR) {
                    navController.navigate(VoiceCloudRoutes.CreatorDashboard) {
                        launchSingleTop = true
                        popUpTo(VoiceCloudRoutes.CreatorReady) { inclusive = true }
                    }
                } else {
                    open(VoiceCloudRoutes.Restricted)
                }
            }
        }
        composable(VoiceCloudRoutes.CreatorDashboard) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorDashboardScreen(
                    state = creatorState,
                    creatorName = authState.user?.displayName.orEmpty(),
                    onLoad = creatorViewModel::loadDashboard,
                    onDashboard = { open(VoiceCloudRoutes.CreatorDashboard) },
                    onLive = { open(VoiceCloudRoutes.CreatorLive) },
                    onProfile = { open(VoiceCloudRoutes.CreatorProfile) },
                    onSettings = { open(VoiceCloudRoutes.CreatorSettings) },
                    onHelp = { open(VoiceCloudRoutes.CreatorHelp) },
                    onAudience = { open(VoiceCloudRoutes.CreatorAudience) },
                    onMessages = { open(VoiceCloudRoutes.CreatorMessages) },
                    onAnalytics = { open(VoiceCloudRoutes.CreatorAnalytics) },
                    onWallet = { open(VoiceCloudRoutes.CreatorWallet) },
                    onEarnings = { open(VoiceCloudRoutes.CreatorEarnings) },
                    onGifts = { open(VoiceCloudRoutes.CreatorGifts) },
                    onPayouts = { open(VoiceCloudRoutes.CreatorPayouts) },
                    onNotifications = { open(VoiceCloudRoutes.CreatorNotifications) },
                    onVerification = { open(VoiceCloudRoutes.CreatorVerification) },
                    onSwitchToVoiceCloud = authViewModel::switchToUserPortal,
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorProfile) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorProfileScreen(
                    state = creatorState,
                    onLoad = creatorViewModel::loadProfile,
                    onSave = creatorViewModel::saveProfile,
                    onDashboard = { open(VoiceCloudRoutes.CreatorDashboard) },
                    onLive = { open(VoiceCloudRoutes.CreatorLive) },
                    onProfile = { open(VoiceCloudRoutes.CreatorProfile) },
                    onSettings = { open(VoiceCloudRoutes.CreatorSettings) },
                    onHelp = { open(VoiceCloudRoutes.CreatorHelp) },
                    onAnalytics = { open(VoiceCloudRoutes.CreatorAnalytics) },
                    onSwitchToVoiceCloud = authViewModel::switchToUserPortal,
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorSettings) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorSettingsScreen(
                    state = creatorState,
                    onLoad = creatorViewModel::loadSettings,
                    onSave = creatorViewModel::saveSettings,
                    onDashboard = { open(VoiceCloudRoutes.CreatorDashboard) },
                    onLive = { open(VoiceCloudRoutes.CreatorLive) },
                    onProfile = { open(VoiceCloudRoutes.CreatorProfile) },
                    onSettings = { open(VoiceCloudRoutes.CreatorSettings) },
                    onHelp = { open(VoiceCloudRoutes.CreatorHelp) },
                    onAnalytics = { open(VoiceCloudRoutes.CreatorAnalytics) },
                    onSwitchToVoiceCloud = authViewModel::switchToUserPortal,
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorHelp) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorHelpScreen(
                    state = creatorState,
                    onLoad = creatorViewModel::loadCmsPages,
                    onOpenPage = { open(VoiceCloudRoutes.creatorCmsContent(it)) },
                    onContact = { open(VoiceCloudRoutes.CreatorContactSupport) },
                    onDashboard = { open(VoiceCloudRoutes.CreatorDashboard) },
                    onLive = { open(VoiceCloudRoutes.CreatorLive) },
                    onProfile = { open(VoiceCloudRoutes.CreatorProfile) },
                    onSettings = { open(VoiceCloudRoutes.CreatorSettings) },
                    onHelp = { open(VoiceCloudRoutes.CreatorHelp) },
                    onAnalytics = { open(VoiceCloudRoutes.CreatorAnalytics) },
                    onSwitchToVoiceCloud = authViewModel::switchToUserPortal,
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorCmsContent) { entry ->
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                val slug = Uri.decode(entry.arguments?.getString("slug").orEmpty())
                DisposableEffect(slug) { onDispose { creatorViewModel.clearCmsPage() } }
                CreatorCmsContentScreen(
                    state = creatorState,
                    slug = slug,
                    onLoad = { creatorViewModel.loadCmsPage(slug) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorContactSupport) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorContactSupportScreen(
                    state = creatorState,
                    defaultName = authState.user?.displayName.orEmpty(),
                    defaultEmail = authState.user?.email.orEmpty(),
                    onSubmit = creatorViewModel::contact,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorAudience) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorAudienceScreen(
                    state = creatorState,
                    onLoad = { creatorViewModel.loadAudience(authState.user?.id, authState.user?.username) },
                    onFollowers = { open(VoiceCloudRoutes.CreatorFollowers) },
                    onMessages = { open(VoiceCloudRoutes.CreatorMessages) },
                    onSubscribers = { open(VoiceCloudRoutes.CreatorSubscribers) },
                    onPlans = { open(VoiceCloudRoutes.CreatorPlans) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorFollowers) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorFollowersScreen(
                    state = creatorState,
                    onLoad = { search, sort -> creatorViewModel.loadFollowers(authState.user?.id, authState.user?.username, search, sort) },
                    onToggleFollowBack = { userId, follow -> creatorViewModel.setFollowBack(userId, follow, authState.user?.id, authState.user?.username) },
                    onMessage = { userId -> engagementViewModel.startDirectConversation(userId) { conversation -> open(VoiceCloudRoutes.creatorConversation(conversation.id)) } },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorSubscribers) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorSubscribersScreen(
                    state = creatorState,
                    onLoad = creatorViewModel::loadSubscribers,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorPlans) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorPlansScreen(
                    state = creatorState,
                    onLoad = creatorViewModel::loadPlans,
                    onCreate = creatorViewModel::createPlan,
                    onUpdate = creatorViewModel::updatePlan,
                    onArchive = creatorViewModel::archivePlan,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorMessages) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                MessagesScreen(
                    state = engagementState,
                    onLoad = engagementViewModel::loadCreatorDirectConversations,
                    onOpen = { open(VoiceCloudRoutes.creatorConversation(it)) },
                    onDelete = engagementViewModel::deleteCreatorDirectConversation,
                    onDeleteMany = engagementViewModel::deleteCreatorDirectConversations,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorConversation) { entry ->
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                val conversationId = Uri.decode(entry.arguments?.getString("conversationId").orEmpty())
                VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                    ConversationScreen(
                        state = engagementState, viewerId = authState.user?.id,
                        onLoad = { engagementViewModel.loadCreatorDirectConversation(conversationId) },
                        onRefresh = { engagementViewModel.refreshCreatorDirectConversationSilently(conversationId) },
                        onSend = engagementViewModel::sendMessage,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }

        composable(VoiceCloudRoutes.CreatorAnalytics) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else CreatorAnalyticsScreen(creatorState, creatorViewModel::loadAnalytics) { navController.popBackStack() }
        }
        composable(VoiceCloudRoutes.CreatorWallet) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else CreatorWalletScreen(creatorState, creatorViewModel::loadWallet) { navController.popBackStack() }
        }
        composable(VoiceCloudRoutes.CreatorEarnings) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else CreatorEarningsScreen(creatorState, creatorViewModel::loadEarnings) { navController.popBackStack() }
        }
        composable(VoiceCloudRoutes.CreatorGifts) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else CreatorGiftsScreen(creatorState, creatorViewModel::loadGifts) { navController.popBackStack() }
        }
        composable(VoiceCloudRoutes.CreatorPayouts) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else CreatorPayoutsScreen(
                state = creatorState,
                onLoad = { creatorViewModel.loadPayouts() },
                onCreate = creatorViewModel::createPayout,
                onOpen = { open(VoiceCloudRoutes.creatorPayout(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.CreatorPayoutDetail) { entry ->
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                val payoutId = Uri.decode(entry.arguments?.getString("payoutId").orEmpty())
                CreatorPayoutDetailScreen(creatorState, payoutId, { creatorViewModel.loadPayout(payoutId) }) { navController.popBackStack() }
            }
        }
        composable(VoiceCloudRoutes.CreatorNotifications) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                NotificationsScreen(
                    state = engagementState,
                    onLoad = { engagementViewModel.loadNotifications() },
                    onOpen = { route -> open(route) },
                    onRead = engagementViewModel::markNotificationRead,
                    onReadAll = engagementViewModel::markAllNotificationsRead,
                    onDelete = engagementViewModel::deleteNotification,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorVerification) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                HostVerificationScreen(
                    state = hostingState,
                    onLoad = hostingViewModel::loadVerification,
                    onApply = hostingViewModel::applyForHost,
                    onUpload = hostingViewModel::uploadVerification,
                    onReplace = hostingViewModel::replaceVerification,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable(VoiceCloudRoutes.CreatorLive) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                CreatorLiveStudioScreen(
                    state = hostingState,
                    onLoad = { hostingViewModel.loadCreatorStudio(authState.user?.id) },
                    onCreateRoom = { open(VoiceCloudRoutes.CreatorRoomCreate) },
                    onScheduleRoom = { open(VoiceCloudRoutes.CreatorScheduleCreate) },
                    onRoom = { open(VoiceCloudRoutes.creatorRoom(it)) },
                    onSchedule = { open(VoiceCloudRoutes.creatorSchedule(it)) },
                    onStartScheduled = { schedule -> hostingViewModel.startScheduled(schedule) { room -> open(VoiceCloudRoutes.creatorConsole(room.id)) } },
                    onDashboard = { open(VoiceCloudRoutes.CreatorDashboard) },
                    onLive = { open(VoiceCloudRoutes.CreatorLive) },
                    onProfile = { open(VoiceCloudRoutes.CreatorProfile) },
                    onSettings = { open(VoiceCloudRoutes.CreatorSettings) },
                    onHelp = { open(VoiceCloudRoutes.CreatorHelp) },
                    onAnalytics = { open(VoiceCloudRoutes.CreatorAnalytics) },
                    onVerification = { open(VoiceCloudRoutes.CreatorVerification) },
                    onSwitchToVoiceCloud = authViewModel::switchToUserPortal,
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorRoomCreate) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                RoomEditorScreen(
                    state = hostingState, roomId = null, onLoad = {},
                    onSave = { input -> hostingViewModel.createRoom(input) { room -> open(VoiceCloudRoutes.creatorRoom(room.id)) } },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorRoomSettings) { entry ->
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
                VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                    RoomEditorScreen(
                        state = hostingState, roomId = roomId, onLoad = hostingViewModel::loadRoom,
                        onSave = { input -> hostingViewModel.updateRoom(roomId, input) { navController.popBackStack() } },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
        composable(VoiceCloudRoutes.CreatorRoomManage) { entry ->
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
                VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                    HostRoomManageScreen(
                        state = hostingState, roomId = roomId, onLoad = { hostingViewModel.loadRoom(roomId) },
                        onSettings = { open(VoiceCloudRoutes.creatorRoomSettings(roomId)) },
                        onStart = { hostingViewModel.startRoom(roomId) { open(VoiceCloudRoutes.creatorConsole(it.id)) } },
                        onOpenConsole = { open(VoiceCloudRoutes.creatorConsole(roomId)) },
                        onPause = { hostingViewModel.pauseRoom(roomId) },
                        onResume = { hostingViewModel.resumeRoom(roomId) },
                        onEnd = { hostingViewModel.endRoom(roomId) },
                        onDelete = { hostingViewModel.deleteRoom(roomId) { navController.popBackStack() } },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
        composable(VoiceCloudRoutes.CreatorScheduleCreate) {
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                ScheduleEditorScreen(
                    state = hostingState, scheduleId = null, onLoad = {},
                    onSave = { input -> hostingViewModel.createSchedule(input) { navController.popBackStack() } },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(VoiceCloudRoutes.CreatorScheduleEdit) { entry ->
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                val scheduleId = Uri.decode(entry.arguments?.getString("scheduleId").orEmpty())
                VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
                    ScheduleEditorScreen(
                        state = hostingState, scheduleId = scheduleId, onLoad = hostingViewModel::loadSchedule,
                        onSave = { input -> hostingViewModel.updateSchedule(scheduleId, input) },
                        onDelete = { hostingViewModel.deleteSchedule(scheduleId) { navController.popBackStack() } },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
        composable(VoiceCloudRoutes.CreatorLiveConsole) { entry ->
            if (authState.user?.normalizedRole != VoiceCloudRole.CREATOR) {
                LaunchedEffect(authState.user?.role) { open(VoiceCloudRoutes.Restricted) }
            } else {
                val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
                VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = true) {
                    HostLiveConsoleScreen(
                        state = hostingState, roomId = roomId, viewerId = authState.user?.id,
                        onEnter = { hostingViewModel.enterHostConsole(roomId) },
                        onLeave = { hostingViewModel.leaveHostConsole(roomId) },
                        onMicrophone = hostingViewModel::setMicrophoneEnabled,
                        onPause = { hostingViewModel.pauseRoom(roomId) },
                        onResume = { hostingViewModel.resumeRoom(roomId) },
                        onEnd = { hostingViewModel.endRoom(roomId) { navController.popBackStack() } },
                        onApprove = { hostingViewModel.approve(roomId, it) },
                        onReject = { hostingViewModel.reject(roomId, it) },
                        onInviteSpeaker = { hostingViewModel.inviteSpeaker(roomId, it) },
                        onRemoveSpeaker = { hostingViewModel.removeSpeaker(roomId, it) },
                        onMuteSpeaker = { userId, muted -> hostingViewModel.muteSpeaker(roomId, userId, muted) },
                        onSearchInvite = hostingViewModel::searchInviteCandidates,
                        onInviteParticipant = { hostingViewModel.inviteParticipant(roomId, it) },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
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
                onRoom = { open(VoiceCloudRoutes.roomExperience(it)) },
                onPeople = { open(VoiceCloudRoutes.People) },
                onCreators = { open(VoiceCloudRoutes.Creators) },
                onProfile = ::openProfile,
                onUpgrade = { open(VoiceCloudRoutes.GuestUpgrade) },
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onLive = { open(VoiceCloudRoutes.Rooms) },
                onFriends = { open(VoiceCloudRoutes.Messages) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
                onCommunities = { open(VoiceCloudRoutes.Communities) },
                onMessages = { open(VoiceCloudRoutes.Messages) },
                onNotifications = { open(VoiceCloudRoutes.Notifications) },
                onHostStudio = { if (viewer?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade) else open(VoiceCloudRoutes.HostStudio) },
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
                onRoom = { open(VoiceCloudRoutes.roomExperience(it)) },
                onPeople = { open(VoiceCloudRoutes.People) },
                onCreators = { open(VoiceCloudRoutes.Creators) },
                onProfile = ::openProfile,
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onLive = { open(VoiceCloudRoutes.Rooms) },
                onFriends = { open(VoiceCloudRoutes.Messages) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
                onCommunities = { open(VoiceCloudRoutes.Communities) },
                onEvents = { open(VoiceCloudRoutes.Events) },
            )
        }
        composable(VoiceCloudRoutes.Rooms) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            RoomsScreen(state, { vm.loadRooms() }, { open(VoiceCloudRoutes.roomExperience(it)) }, { navController.popBackStack() })
        }
        composable(VoiceCloudRoutes.RoomPreview) { entry ->
            val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
            val vm: LiveRoomViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            LaunchedEffect(viewer?.id, viewer?.username) { vm.setViewer(viewer?.id, viewer?.username) }
            RoomPreviewScreen(
                state = state,
                roomId = roomId,
                onLoad = { vm.loadPreview(roomId) },
                onJoin = { open(VoiceCloudRoutes.roomExperience(roomId)) },
                onToggleSave = vm::toggleSave,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.RoomExperience) { entry ->
            val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
            val vm: LiveRoomViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            LaunchedEffect(viewer?.id, viewer?.username) { vm.setViewer(viewer?.id, viewer?.username) }
            LiveRoomScreen(
                state = state,
                roomId = roomId,
                viewerId = viewer?.id,
                onEnter = { vm.enter(roomId) },
                onLeave = vm::leave,
                onRetryAudio = vm::retryAudio,
                onToggleSave = vm::toggleSave,
                onToggleHand = vm::toggleHand,
                onSendMessage = vm::sendMessage,
                onMessageReaction = vm::reactToMessage,
                onReaction = vm::sendReaction,
                onGift = vm::sendGift,
                onAcceptInvitation = vm::acceptSpeakerInvitation,
                onRejectInvitation = vm::rejectSpeakerInvitation,
                onBackground = vm::onBackground,
                onForeground = vm::onForeground,
                onReport = { id, label -> open(VoiceCloudRoutes.contextualReport(ReportTargetType.ROOM, id, label)) },
                onBack = { navController.popBackStack() },
            )
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
                communities = engagementState.communities.map { community ->
                    CommunitySearchItem(community.id, community.name, community.handle, community.memberCount)
                },
                communitiesLoading = engagementState.loading,
                onLoadDefaults = {
                    vm.setViewer(viewer?.id, viewer?.username)
                    vm.loadSearchLanding()
                    engagementViewModel.loadCommunities("")
                },
                onSubmit = { query ->
                    vm.setViewer(viewer?.id, viewer?.username)
                    vm.search(query)
                    engagementViewModel.loadCommunities(query)
                },
                onProfile = ::openProfile,
                onRoom = { open(VoiceCloudRoutes.roomExperience(it)) },
                onCommunity = { open(VoiceCloudRoutes.community(it)) },
                onPeopleViewAll = { open(VoiceCloudRoutes.People) },
                onCreatorsViewAll = { open(VoiceCloudRoutes.Creators) },
                onRoomsViewAll = { open(VoiceCloudRoutes.Rooms) },
                onCommunitiesViewAll = { open(VoiceCloudRoutes.Communities) },
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onLive = { open(VoiceCloudRoutes.Rooms) },
                onFriends = { open(VoiceCloudRoutes.Messages) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
            )
        }
        composable(VoiceCloudRoutes.CommunitySearch) {
            val vm: DiscoveryViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val viewer = authState.user
            SearchScreen(
                state = state,
                communities = engagementState.communities.map { community ->
                    CommunitySearchItem(community.id, community.name, community.handle, community.memberCount)
                },
                communitiesLoading = engagementState.loading,
                initialTab = "Communities",
                onLoadDefaults = {
                    vm.setViewer(viewer?.id, viewer?.username)
                    vm.loadSearchLanding()
                    engagementViewModel.loadCommunities("")
                },
                onSubmit = { query ->
                    vm.setViewer(viewer?.id, viewer?.username)
                    vm.search(query)
                    engagementViewModel.loadCommunities(query)
                },
                onProfile = ::openProfile,
                onRoom = { open(VoiceCloudRoutes.roomExperience(it)) },
                onCommunity = { open(VoiceCloudRoutes.community(it)) },
                onPeopleViewAll = { open(VoiceCloudRoutes.People) },
                onCreatorsViewAll = { open(VoiceCloudRoutes.Creators) },
                onRoomsViewAll = { open(VoiceCloudRoutes.Rooms) },
                onCommunitiesViewAll = { open(VoiceCloudRoutes.Communities) },
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onLive = { open(VoiceCloudRoutes.Rooms) },
                onFriends = { open(VoiceCloudRoutes.Messages) },
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
                onReport = { userId, label -> open(VoiceCloudRoutes.contextualReport(ReportTargetType.USER, userId, label)) },
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
                onEconomy = { if (authState.user?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade) else open(VoiceCloudRoutes.Economy) },
                onEditProfile = { if (authState.user?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade) else open(VoiceCloudRoutes.EditProfile) },
                onProfileTools = { if (authState.user?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade) else open(VoiceCloudRoutes.ProfileTools) },
                onSettings = { if (authState.user?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade) else open(VoiceCloudRoutes.Settings) },
                onSafety = { if (authState.user?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade) else open(VoiceCloudRoutes.SafetyCenter) },
                onHelp = { open(VoiceCloudRoutes.HelpCenter) },
                onContactSupport = { open(VoiceCloudRoutes.ContactSupport) },
                onAbout = { open(VoiceCloudRoutes.About) },
                canSwitchToCreator = authState.user?.normalizedRole == VoiceCloudRole.CREATOR,
                onSwitchToCreator = authViewModel::switchToCreatorPortal,
                onUpgrade = { open(VoiceCloudRoutes.GuestUpgrade) },
                onLogout = { authViewModel.logout() },
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onLive = { open(VoiceCloudRoutes.Rooms) },
                onFriends = { open(VoiceCloudRoutes.Messages) },
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
                onCancel = vm::cancelFriendRequest,
                onRemove = vm::removeFriend,
                onHome = { open(VoiceCloudRoutes.Home) },
                onExplore = { open(VoiceCloudRoutes.Explore) },
                onSearch = { open(VoiceCloudRoutes.Search) },
                onLive = { open(VoiceCloudRoutes.Rooms) },
                onFriends = { open(VoiceCloudRoutes.Messages) },
                onMe = { open(VoiceCloudRoutes.MyProfile) },
            )
        }

        composable(VoiceCloudRoutes.Communities) {
            CommunitiesScreen(
                state = engagementState,
                onLoad = engagementViewModel::loadCommunities,
                onOpen = { open(VoiceCloudRoutes.community(it)) },
                onCreate = { if (authState.user?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade) else open(VoiceCloudRoutes.CommunityCreate) },
                onSearch = { open(VoiceCloudRoutes.CommunitySearch) },
                onEvents = { open(VoiceCloudRoutes.Events) },
                onMessages = { open(VoiceCloudRoutes.Messages) },
                onNotifications = { open(VoiceCloudRoutes.Notifications) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.CommunityCreate) {
            CommunityEditorScreen(
                state = engagementState,
                onSubmit = { input ->
                    engagementViewModel.createCommunity(input) { created ->
                        navController.navigate(VoiceCloudRoutes.community(created.handle.ifBlank { created.id })) {
                            launchSingleTop = true
                            popUpTo(VoiceCloudRoutes.CommunityCreate) { inclusive = true }
                        }
                    }
                },
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
                onDeleteMany = engagementViewModel::deleteConversations,
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
        composable(VoiceCloudRoutes.HostStudio) {
            HostStudioScreen(
                state = hostingState,
                onLoad = hostingViewModel::loadStudio,
                onCreateRoom = { open(VoiceCloudRoutes.HostRoomCreate) },
                onScheduleRoom = { open(VoiceCloudRoutes.HostScheduleCreate) },
                onRoom = { open(VoiceCloudRoutes.hostRoom(it)) },
                onSchedule = { open(VoiceCloudRoutes.hostSchedule(it)) },
                onStartScheduled = { schedule -> hostingViewModel.startScheduled(schedule) { room -> open(VoiceCloudRoutes.hostConsole(room.id)) } },
                onVerification = { open(VoiceCloudRoutes.HostVerification) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HostVerification) {
            HostVerificationScreen(
                state = hostingState,
                onLoad = hostingViewModel::loadVerification,
                onApply = hostingViewModel::applyForHost,
                onUpload = hostingViewModel::uploadVerification,
                onReplace = hostingViewModel::replaceVerification,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HostRoomCreate) {
            RoomEditorScreen(
                state = hostingState, roomId = null, onLoad = {},
                onSave = { input -> hostingViewModel.createRoom(input) { room -> open(VoiceCloudRoutes.hostRoom(room.id)) } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HostRoomSettings) { entry ->
            val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
            RoomEditorScreen(
                state = hostingState, roomId = roomId, onLoad = hostingViewModel::loadRoom,
                onSave = { input -> hostingViewModel.updateRoom(roomId, input) { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HostRoomManage) { entry ->
            val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
            HostRoomManageScreen(
                state = hostingState, roomId = roomId, onLoad = { hostingViewModel.loadRoom(roomId) },
                onSettings = { open(VoiceCloudRoutes.hostRoomSettings(roomId)) },
                onStart = { hostingViewModel.startRoom(roomId) { open(VoiceCloudRoutes.hostConsole(it.id)) } },
                onOpenConsole = { open(VoiceCloudRoutes.hostConsole(roomId)) },
                onInteractive = { open(VoiceCloudRoutes.hostInteractive(roomId)) },
                onPause = { hostingViewModel.pauseRoom(roomId) },
                onResume = { hostingViewModel.resumeRoom(roomId) },
                onEnd = { hostingViewModel.endRoom(roomId) },
                onDelete = { hostingViewModel.deleteRoom(roomId) { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HostScheduleCreate) {
            ScheduleEditorScreen(
                state = hostingState, scheduleId = null, onLoad = {},
                onSave = { input -> hostingViewModel.createSchedule(input) { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HostScheduleEdit) { entry ->
            val scheduleId = Uri.decode(entry.arguments?.getString("scheduleId").orEmpty())
            ScheduleEditorScreen(
                state = hostingState, scheduleId = scheduleId, onLoad = hostingViewModel::loadSchedule,
                onSave = { input -> hostingViewModel.updateSchedule(scheduleId, input) },
                onDelete = { hostingViewModel.deleteSchedule(scheduleId) { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HostLiveConsole) { entry ->
            val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
            HostLiveConsoleScreen(
                state = hostingState, roomId = roomId, viewerId = authState.user?.id,
                onEnter = { hostingViewModel.enterHostConsole(roomId) },
                onLeave = { hostingViewModel.leaveHostConsole(roomId) },
                onMicrophone = hostingViewModel::setMicrophoneEnabled,
                onPause = { hostingViewModel.pauseRoom(roomId) },
                onResume = { hostingViewModel.resumeRoom(roomId) },
                onEnd = { hostingViewModel.endRoom(roomId) { navController.popBackStack() } },
                onApprove = { hostingViewModel.approve(roomId, it) },
                onReject = { hostingViewModel.reject(roomId, it) },
                onInviteSpeaker = { hostingViewModel.inviteSpeaker(roomId, it) },
                onRemoveSpeaker = { hostingViewModel.removeSpeaker(roomId, it) },
                onMuteSpeaker = { userId, muted -> hostingViewModel.muteSpeaker(roomId, userId, muted) },
                onSearchInvite = hostingViewModel::searchInviteCandidates,
                onInviteParticipant = { hostingViewModel.inviteParticipant(roomId, it) },
                onInteractive = { open(VoiceCloudRoutes.hostInteractive(roomId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HostInteractive) { entry ->
            val roomId = Uri.decode(entry.arguments?.getString("roomId").orEmpty())
            PollsQuizScreen(
                state = hostingState, roomId = roomId, onLoad = { hostingViewModel.loadInteractive(roomId) },
                onCreatePoll = hostingViewModel::createPoll,
                onStartPoll = { hostingViewModel.startPoll(it, roomId) },
                onStopPoll = { hostingViewModel.stopPoll(it, roomId) },
                onDeletePoll = { hostingViewModel.deletePoll(it, roomId) },
                onCreateQuiz = hostingViewModel::createQuiz,
                onStartQuiz = { hostingViewModel.startQuiz(it, roomId) },
                onNextQuiz = { hostingViewModel.nextQuizRound(it, roomId) },
                onStopQuiz = { hostingViewModel.stopQuiz(it, roomId) },
                onBack = { navController.popBackStack() },
            )
        }

        // PH09 — Preferences, security, safety, CMS and support.
        composable(VoiceCloudRoutes.Settings) {
            SettingsOverviewScreen(
                onNotifications = { open(VoiceCloudRoutes.NotificationSettings) },
                onPrivacy = { open(VoiceCloudRoutes.PrivacySettings) },
                onVoiceAppearance = { open(VoiceCloudRoutes.VoiceAppearance) },
                onSecurity = { open(VoiceCloudRoutes.Security) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.NotificationSettings) {
            NotificationPreferencesScreen(
                state = settingsState,
                onLoad = settingsViewModel::loadSettings,
                onSave = settingsViewModel::saveNotifications,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.PrivacySettings) {
            PrivacySettingsScreen(
                state = settingsState,
                onLoad = settingsViewModel::loadPrivacy,
                onSave = settingsViewModel::savePrivacy,
                onBlocked = { open(VoiceCloudRoutes.BlockedUsers) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.VoiceAppearance) {
            VoiceAppearanceScreen(
                state = settingsState,
                onLoad = settingsViewModel::loadSettings,
                onSaveVoice = settingsViewModel::saveVoice,
                onSaveTheme = settingsViewModel::saveTheme,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.Security) {
            SecurityOverviewScreen(
                state = settingsState,
                onLoad = settingsViewModel::loadSecurity,
                onSessionsDevices = { open(VoiceCloudRoutes.SessionsDevices) },
                onLoginActivity = { open(VoiceCloudRoutes.LoginActivity) },
                onSignOutAll = { authViewModel.logout(allDevices = true) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.SessionsDevices) {
            SessionsDevicesScreen(
                state = settingsState,
                onLoad = settingsViewModel::loadSecurity,
                onSession = { open(VoiceCloudRoutes.securitySession(it)) },
                onDevice = { open(VoiceCloudRoutes.securityDevice(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.SessionDetail) { entry ->
            val id = Uri.decode(entry.arguments?.getString("sessionId").orEmpty())
            SessionDetailScreen(
                state = settingsState,
                id = id,
                onLoad = { settingsViewModel.loadSession(id) },
                onRevoke = { settingsViewModel.revokeSession(id) { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.DeviceDetail) { entry ->
            val id = Uri.decode(entry.arguments?.getString("deviceId").orEmpty())
            DeviceDetailScreen(
                state = settingsState,
                id = id,
                onLoad = { settingsViewModel.loadDevice(id) },
                onRevoke = { settingsViewModel.revokeDevice(id) { navController.popBackStack() } },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.LoginActivity) {
            LoginActivityScreen(
                state = settingsState,
                onLoad = settingsViewModel::loadSecurity,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.HelpCenter) {
            HelpCenterScreen(
                state = settingsState,
                onLoad = settingsViewModel::loadCmsPages,
                onPage = { open(VoiceCloudRoutes.cmsContent(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.CmsContent) { entry ->
            val slug = Uri.decode(entry.arguments?.getString("slug").orEmpty())
            CmsContentScreen(
                state = settingsState,
                slug = slug,
                onLoad = { settingsViewModel.loadCmsPage(slug) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.SafetyCenter) {
            SafetyCenterScreen(
                state = settingsState,
                onLoad = settingsViewModel::loadCmsPages,
                onReport = { open(VoiceCloudRoutes.Report) },
                onBlocked = { open(VoiceCloudRoutes.BlockedUsers) },
                onPage = { open(VoiceCloudRoutes.cmsContent(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.Report) {
            ReportScreen(
                state = settingsState,
                contextualType = null,
                contextualId = null,
                contextualLabel = null,
                onLoadContext = settingsViewModel::prepareReport,
                onSearch = settingsViewModel::searchReportTargets,
                onSelect = settingsViewModel::selectReportTarget,
                onSubmit = { reason, description -> settingsViewModel.submitReport(reason, description) },
                onReloadReports = settingsViewModel::loadReports,
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.ContextReport) { entry ->
            val type = runCatching { ReportTargetType.valueOf(Uri.decode(entry.arguments?.getString("targetType").orEmpty()).uppercase()) }.getOrNull()
            val id = Uri.decode(entry.arguments?.getString("targetId").orEmpty())
            val label = Uri.decode(entry.arguments?.getString("targetLabel").orEmpty())
            ReportScreen(
                state = settingsState,
                contextualType = type,
                contextualId = id,
                contextualLabel = label,
                onLoadContext = { if (type != null && id.isNotBlank()) settingsViewModel.loadReporting(type, id, label) },
                onSearch = settingsViewModel::searchReportTargets,
                onSelect = settingsViewModel::selectReportTarget,
                onSubmit = { reason, description -> settingsViewModel.submitReport(reason, description) },
                onReloadReports = {},
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.ContactSupport) {
            val user = authState.user
            ContactSupportScreen(
                state = settingsState,
                defaultName = user?.displayName.orEmpty(),
                defaultEmail = user?.email.orEmpty(),
                defaultPhone = user?.phoneNumber,
                onSend = { name, email, phone, description -> settingsViewModel.sendSupport(name, email, phone, description) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.About) {
            AboutScreen(
                versionName = app.voicecloud.android.BuildConfig.VERSION_NAME,
                onBack = { navController.popBackStack() },
            )
        }

        composable(VoiceCloudRoutes.ProfileTools) {
            ProfileToolsScreen(
                state = profileState,
                onLoad = { profileViewModel.loadHub() },
                onReplays = { open(VoiceCloudRoutes.ReplayLibrary) },
                onActivity = { open(VoiceCloudRoutes.ActivityHistory) },
                onVisitors = { open(VoiceCloudRoutes.ProfileVisitors) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.EditProfile) {
            EditProfileScreen(
                state = profileState,
                identityUsername = authState.user?.username.orEmpty(),
                identityEmail = authState.user?.email,
                identityPhone = authState.user?.phoneNumber,
                onLoad = { profileViewModel.loadProfile() },
                onSave = { body -> profileViewModel.saveProfile(body) },
                onUploadAvatar = { bytes, fileName, mimeType -> profileViewModel.uploadAvatar(bytes, fileName, mimeType) },
                onDeleteAvatar = { profileViewModel.deleteAvatar() },
                onUploadCover = { bytes, fileName, mimeType -> profileViewModel.uploadCover(bytes, fileName, mimeType) },
                onDeleteCover = { profileViewModel.deleteCover() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.ReplayLibrary) {
            ReplayLibraryScreen(
                state = profileState,
                onLoad = { profileViewModel.loadReplays() },
                onReplay = { open(VoiceCloudRoutes.replay(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.ReplayPlayer) { entry ->
            val replayId = Uri.decode(entry.arguments?.getString("replayId").orEmpty())
            ReplayPlayerScreen(
                state = profileState,
                replayId = replayId,
                onLoad = { replayIdValue -> profileViewModel.loadReplay(replayIdValue) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.ActivityHistory) {
            ActivityHistoryScreen(profileState, { profileViewModel.loadActivity() }) { navController.popBackStack() }
        }
        composable(VoiceCloudRoutes.ProfileVisitors) {
            ProfileVisitorsScreen(profileState, { profileViewModel.loadVisitors() }) { navController.popBackStack() }
        }
        composable(VoiceCloudRoutes.BlockedUsers) {
            BlockedUsersScreen(
                state = profileState,
                onLoad = { profileViewModel.loadBlockedUsers() },
                onUnblock = { userId -> profileViewModel.unblock(userId) },
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
        composable(VoiceCloudRoutes.Economy) {
            EconomyHubScreen(
                onOpen = { open(VoiceCloudRoutes.economySection(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(VoiceCloudRoutes.EconomySection) { entry ->
            val sectionName = Uri.decode(entry.arguments?.getString("section").orEmpty())
            val section = runCatching { EconomySection.valueOf(sectionName) }.getOrDefault(EconomySection.WALLET)
            EconomySectionScreen(
                section = section,
                state = economyState,
                onLoad = { economyViewModel.load(section) },
                onBack = { navController.popBackStack() },
                onClaimCheckIn = economyViewModel::claimCheckIn,
                onClaimTask = economyViewModel::claimTask,
                onBuyItem = economyViewModel::buyItem,
                onEquip = economyViewModel::equip,
                onUnequip = economyViewModel::unequip,
                onBuyTicket = economyViewModel::buyTicket,
                paymentRail = economyViewModel.paymentRail,
                onBuyWalletCredits = economyViewModel::buyWalletCredits,
                onBuyVip = economyViewModel::buyVip,
                onRestoreWallet = economyViewModel::restoreGooglePlayWallet,
                onRestoreVip = economyViewModel::restoreGooglePlayVip,
                onPaymentResume = economyViewModel::refreshAfterExternalCheckout,
            )
        }

    }
}


private val consumerTabs = listOf(
    VoiceCloudRoutes.Home, VoiceCloudRoutes.Explore, VoiceCloudRoutes.Search,
    VoiceCloudRoutes.Friends, VoiceCloudRoutes.MyProfile,
)

private fun tabIndex(route: String?): Int = consumerTabs.indexOf(if (route == VoiceCloudRoutes.CommunitySearch) VoiceCloudRoutes.Search else route)

private fun tabEnterTransition(fromRoute: String?, toRoute: String?): EnterTransition {
    val from = tabIndex(fromRoute)
    val to = tabIndex(toRoute)
    if (from >= 0 && to >= 0 && from != to) {
        val direction = if (to > from) 1 else -1
        return slideInHorizontally(animationSpec = tween(220)) { fullWidth -> direction * (fullWidth / 6) } + fadeIn(animationSpec = tween(180))
    }
    if (fromRoute == toRoute) return EnterTransition.None
    return slideInHorizontally(animationSpec = tween(220)) { fullWidth -> fullWidth / 8 } + fadeIn(animationSpec = tween(180))
}

private fun tabExitTransition(fromRoute: String?, toRoute: String?): ExitTransition {
    val from = tabIndex(fromRoute)
    val to = tabIndex(toRoute)
    if (from >= 0 && to >= 0 && from != to) {
        val direction = if (to > from) -1 else 1
        return slideOutHorizontally(animationSpec = tween(220)) { fullWidth -> direction * (fullWidth / 6) } + fadeOut(animationSpec = tween(150))
    }
    if (fromRoute == toRoute) return ExitTransition.None
    return slideOutHorizontally(animationSpec = tween(200)) { fullWidth -> -(fullWidth / 12) } + fadeOut(animationSpec = tween(140))
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
    AuthScreen.CREATOR_READY -> VoiceCloudRoutes.CreatorDashboard
    AuthScreen.MAINTENANCE -> VoiceCloudRoutes.Maintenance
}
