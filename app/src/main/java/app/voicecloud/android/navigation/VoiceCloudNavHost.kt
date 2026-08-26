package app.voicecloud.android.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.voicecloud.core.model.MobileConfig
import app.voicecloud.feature.auth.model.FirebaseClientConfig
import app.voicecloud.feature.auth.ui.*
import app.voicecloud.feature.bootstrap.BootstrapRoute
import app.voicecloud.feature.discovery.ui.*

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

    fun profile(username: String): String = "profile/${Uri.encode(username.trim())}"
}

@Composable
fun VoiceCloudNavHost(
    navController: NavHostController,
    initialResetToken: String? = null,
    firebaseClientConfig: FirebaseClientConfig = FirebaseClientConfig(),
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    var mobileConfig by remember { mutableStateOf<MobileConfig?>(null) }
    var resetToken by remember(initialResetToken) { mutableStateOf(initialResetToken.orEmpty()) }

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
