package app.voicecloud.android.navigation

import android.net.Uri
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.voicecloud.android.ui.shell.ConsumerShell
import app.voicecloud.android.ui.shell.CreatorShell
import app.voicecloud.core.model.MobileConfig
import app.voicecloud.core.model.VoiceCloudAccountRole
import app.voicecloud.core.preferences.LastPortal
import app.voicecloud.core.preferences.VoiceCloudPreferences
import app.voicecloud.feature.auth.AuthEntryViewModel
import app.voicecloud.feature.auth.AuthGateViewModel
import app.voicecloud.feature.auth.AuthSessionState
import app.voicecloud.feature.auth.AuthSessionController
import app.voicecloud.feature.auth.CreatorAccessViewModel
import app.voicecloud.feature.auth.CreatorLoginViewModel
import app.voicecloud.feature.auth.ForgotPasswordViewModel
import app.voicecloud.feature.auth.GoogleSignInViewModel
import app.voicecloud.feature.auth.GuestUpgradeViewModel
import app.voicecloud.feature.auth.PhoneAuthViewModel
import app.voicecloud.feature.auth.RegisterViewModel
import app.voicecloud.feature.auth.ResetPasswordViewModel
import app.voicecloud.feature.auth.SessionListViewModel
import app.voicecloud.feature.auth.UserLoginViewModel
import app.voicecloud.feature.auth.ui.AuthEntryScreen
import app.voicecloud.feature.auth.ui.AuthStatusScreen
import app.voicecloud.feature.auth.ui.CreatorAccessApplicationScreen
import app.voicecloud.feature.auth.ui.CreatorLoginScreen
import app.voicecloud.feature.auth.ui.ForgotPasswordScreen
import app.voicecloud.feature.auth.ui.GoogleSignInScreen
import app.voicecloud.feature.auth.ui.GuestUpgradeScreen
import app.voicecloud.feature.auth.ui.OnboardingScreen
import app.voicecloud.feature.auth.ui.PhoneSignInScreen
import app.voicecloud.feature.auth.ui.PortalSelectionScreen
import app.voicecloud.feature.auth.ui.ResetPasswordScreen
import app.voicecloud.feature.auth.ui.UserRegisterScreen
import app.voicecloud.feature.auth.ui.UserLoginScreen
import app.voicecloud.feature.bootstrap.BootstrapRoute
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import app.voicecloud.android.di.VoiceCloudMobileConfigEntryPoint
import app.voicecloud.android.di.VoiceCloudSessionBridgeEntryPoint
import app.voicecloud.android.session.VoiceCloudSessionNavEvent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

/** PH01 start remains bootstrap. PH02 adds auth gates before portal graphs. */
object VoiceCloudRoutes {
    const val Bootstrap = "bootstrap"
    const val AuthGraph = "auth"
    const val UserPortal = "user"
    const val CreatorPortal = "creator"
}

object CreatorDestinations {
    const val Dashboard = "creator/dashboard"
    const val Audience = "creator/audience"
    const val LiveStudio = "creator/live"
    const val Analytics = "creator/analytics"
    const val Workspace = "creator/workspace"
    const val Settings = "creator/settings"
}

object ConsumerDestinations {
    const val Home = "user/home"
    const val Discover = "user/discover"
    const val Search = "user/search"
    const val People = "user/people"
    const val RoomPreview = "user/room/preview"
    const val Live = "user/live"
    const val Messages = "user/messages"
    const val MessageThread = "user/messages/thread"
    const val Profile = "user/profile"
    const val EditProfile = "user/profile/edit"
    const val Notifications = "user/notifications"
    const val Wallet = "user/wallet"
    const val Settings = "user/settings"
    const val PublicProfile = "user/profile/user/{userId}"
    const val SocialList = "user/profile/user/{userId}/social/{listType}"
    const val SavedRooms = "user/saved-rooms"
    const val Rankings = "user/rankings"
    const val BlockedUsers = "user/blocked"
    const val Referrals = "user/referrals"
    const val TasksHub = "user/tasks"

    const val UserIdArg = "userId"
    const val SocialListTypeArg = "listType"
}

@Composable
fun VoiceCloudNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialPasswordResetToken: String? = null,
) {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val preferences = remember { VoiceCloudPreferences(context.applicationContext) }
    var mobileConfig by remember { mutableStateOf<MobileConfig?>(null) }
    val authGateViewModel: AuthGateViewModel = hiltViewModel()
    val sessionController = authGateViewModel.sessionController
    val sessionState by authGateViewModel.sessionState.collectAsStateWithLifecycle()
    var sessionRestoreStarted by remember { mutableStateOf(false) }
    var handledResetDeepLink by remember { mutableStateOf(false) }

    val openPortalSelection: () -> Unit = {
        navController.navigate(AuthDestinations.Portal) {
            popUpTo(AuthDestinations.Entry) { inclusive = false }
        }
    }
    val openUserPortal: () -> Unit = {
        navController.navigate(VoiceCloudRoutes.UserPortal) {
            popUpTo(VoiceCloudRoutes.Bootstrap) { inclusive = true }
        }
    }
    val openCreatorPortal: () -> Unit = {
        navController.navigate(VoiceCloudRoutes.CreatorPortal) {
            popUpTo(VoiceCloudRoutes.Bootstrap) { inclusive = true }
        }
    }
    val routeAuthenticatedUser: suspend (app.voicecloud.core.model.VoiceCloudUser) -> Unit = { user ->
        navController.navigateAfterAuthentication(
            user = user,
            preferences = preferences,
            sessionController = sessionController,
            openPortalSelection = openPortalSelection,
            openUserPortal = openUserPortal,
            openCreatorPortal = openCreatorPortal,
        )
    }

    LaunchedEffect(initialPasswordResetToken, handledResetDeepLink) {
        val token = initialPasswordResetToken?.trim().orEmpty()
        if (token.isNotBlank() && !handledResetDeepLink) {
            handledResetDeepLink = true
            navController.navigate(
                "${AuthDestinations.ResetPasswordRoute}?${AuthDestinations.ResetTokenArg}=${Uri.encode(token)}",
            ) {
                launchSingleTop = true
            }
        }
    }

    val sessionBridge = remember(context.applicationContext) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            VoiceCloudSessionBridgeEntryPoint::class.java,
        ).sessionNavigationBridge()
    }
    LaunchedEffect(sessionBridge) {
        sessionBridge.events.collect { event ->
            when (event) {
                VoiceCloudSessionNavEvent.SessionExpired -> {
                    sessionController.markSessionExpired()
                    navController.navigate(AuthDestinations.SessionExpired) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = VoiceCloudRoutes.Bootstrap,
        modifier = modifier,
        enterTransition = { if (motionEnabled) fadeIn(tween(VoiceCloudMotion.EntranceMs)) else EnterTransition.None },
        exitTransition = { if (motionEnabled) fadeOut(tween(VoiceCloudMotion.ExitMs)) else ExitTransition.None },
    ) {
        composable(VoiceCloudRoutes.Bootstrap) {
            BootstrapRoute(
                onContinueToConsumer = {
                    when (sessionState) {
                        is AuthSessionState.Authenticated -> {
                            navController.navigate(VoiceCloudRoutes.AuthGraph) {
                                popUpTo(VoiceCloudRoutes.Bootstrap) { inclusive = true }
                            }
                        }
                        AuthSessionState.LoggedOut -> {
                            navController.navigate(AuthDestinations.Entry) {
                                popUpTo(VoiceCloudRoutes.Bootstrap) { inclusive = true }
                            }
                        }
                        AuthSessionState.Unknown -> Unit
                    }
                },
                onOpenCreatorWorkspace = {
                    navController.navigate(AuthDestinations.CreatorLogin) {
                        popUpTo(VoiceCloudRoutes.Bootstrap) { inclusive = true }
                    }
                },
                onConfigReady = { config ->
                    mobileConfig = config
                    EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        VoiceCloudMobileConfigEntryPoint::class.java,
                    ).mobileConfigStore().update(config)
                    if (!sessionRestoreStarted) {
                        sessionRestoreStarted = true
                        authGateViewModel.restore(onComplete = {})
                    }
                },
            )
        }
        composable(AuthDestinations.Entry) {
            val config = mobileConfig
            val entryViewModel: AuthEntryViewModel = hiltViewModel()
            val entryState by entryViewModel.state.collectAsStateWithLifecycle()
            val methods = config?.supportedLoginMethods.orEmpty()
            AuthEntryScreen(
                supportedLoginMethods = methods,
                onUserSignIn = { navController.navigate(AuthDestinations.UserLogin) },
                onCreatorSignIn = { navController.navigate(AuthDestinations.CreatorLogin) },
                onRegister = { navController.navigate(AuthDestinations.UserRegister) },
                onPhoneSignIn = if (methods.any { it.equals("phone", ignoreCase = true) }) {
                    { navController.navigate(AuthDestinations.PhoneSignIn) }
                } else {
                    null
                },
                onGoogleSignIn = if (methods.any { it.equals("google", ignoreCase = true) }) {
                    { navController.navigate(AuthDestinations.GoogleSignIn) }
                } else {
                    null
                },
                onContinueAsGuest = if (methods.any { it.equals("guest", ignoreCase = true) }) {
                    {
                        entryViewModel.continueAsGuest { user ->
                            scope.launch { routeAuthenticatedUser(user) }
                        }
                    }
                } else {
                    null
                },
                guestSubmitting = entryState.isSubmitting,
                guestErrorMessage = entryState.errorMessage,
            )
        }
        composable(AuthDestinations.UserLogin) {
            UserLoginScreen(
                viewModel = hiltViewModel<UserLoginViewModel>(),
                onBack = { navController.popBackStack() },
                onRegister = { navController.navigate(AuthDestinations.UserRegister) },
                onForgotPassword = { navController.navigate(AuthDestinations.ForgotPassword) },
                onPhoneSignIn = { navController.navigate(AuthDestinations.PhoneSignIn) },
                onAuthenticated = {
                    val user = (sessionState as? AuthSessionState.Authenticated)?.user ?: return@UserLoginScreen
                    scope.launch { routeAuthenticatedUser(user) }
                },
            )
        }
        composable(AuthDestinations.UserRegister) {
            UserRegisterScreen(
                viewModel = hiltViewModel<RegisterViewModel>(),
                onBack = { navController.popBackStack() },
                onRegistered = {
                    val user = (sessionState as? AuthSessionState.Authenticated)?.user ?: return@UserRegisterScreen
                    scope.launch { routeAuthenticatedUser(user) }
                },
            )
        }
        composable(AuthDestinations.ForgotPassword) {
            ForgotPasswordScreen(
                viewModel = hiltViewModel<ForgotPasswordViewModel>(),
                onBack = { navController.popBackStack() },
                onSent = { navController.popBackStack() },
            )
        }
        composable(
            route = AuthDestinations.ResetPassword,
            arguments = listOf(navArgument(AuthDestinations.ResetTokenArg) { type = NavType.StringType; defaultValue = "" }),
        ) { entry ->
            val token = entry.arguments?.getString(AuthDestinations.ResetTokenArg).orEmpty()
            ResetPasswordScreen(
                viewModel = hiltViewModel<ResetPasswordViewModel>(),
                token = token,
                onBack = { navController.navigate(AuthDestinations.UserLogin) { popUpTo(AuthDestinations.Entry) } },
                onComplete = {
                    navController.navigate(AuthDestinations.UserLogin) {
                        popUpTo(AuthDestinations.Entry) { inclusive = false }
                    }
                },
            )
        }
        composable(AuthDestinations.PhoneSignIn) {
            PhoneSignInScreen(
                viewModel = hiltViewModel<PhoneAuthViewModel>(),
                onBack = { navController.popBackStack() },
                onAuthenticated = {
                    val user = (sessionState as? AuthSessionState.Authenticated)?.user ?: return@PhoneSignInScreen
                    scope.launch { routeAuthenticatedUser(user) }
                },
            )
        }
        composable(AuthDestinations.GoogleSignIn) {
            GoogleSignInScreen(
                viewModel = hiltViewModel<GoogleSignInViewModel>(),
                onBack = { navController.popBackStack() },
                onAuthenticated = {
                    val user = (sessionState as? AuthSessionState.Authenticated)?.user ?: return@GoogleSignInScreen
                    scope.launch { routeAuthenticatedUser(user) }
                },
            )
        }
        composable(AuthDestinations.GuestUpgrade) {
            GuestUpgradeScreen(
                viewModel = hiltViewModel<GuestUpgradeViewModel>(),
                onBack = { navController.popBackStack() },
                onUpgraded = {
                    val user = (sessionState as? AuthSessionState.Authenticated)?.user ?: return@GuestUpgradeScreen
                    scope.launch { routeAuthenticatedUser(user) }
                },
            )
        }
        composable(AuthDestinations.Onboarding) {
            OnboardingScreen(
                onContinue = {
                    scope.launch {
                        preferences.setOnboardingComplete(true)
                        val user = (sessionState as? AuthSessionState.Authenticated)?.user ?: return@launch
                        routeAuthenticatedUser(user)
                    }
                },
            )
        }
        composable(AuthDestinations.RestrictedAccount) {
            AuthStatusScreen(
                title = "Account restricted",
                message = "Your VoiceCloud account is restricted or suspended. Contact support if you believe this is a mistake.",
                onBack = { /* blocked */ },
                primaryAction = "Sign out",
                onPrimary = {
                    scope.launch {
                        sessionController.logout()
                        navController.navigate(AuthDestinations.Entry) { popUpTo(0) { inclusive = true } }
                    }
                },
            )
        }
        composable(AuthDestinations.SessionExpired) {
            AuthStatusScreen(
                title = "Session expired",
                message = "Sign in again to continue using VoiceCloud.",
                onBack = { navController.navigate(AuthDestinations.Entry) { popUpTo(0) { inclusive = true } } },
                primaryAction = "Sign in",
                onPrimary = {
                    navController.navigate(AuthDestinations.Entry) { popUpTo(0) { inclusive = true } }
                },
            )
        }
        composable(AuthDestinations.Sessions) {
            app.voicecloud.feature.auth.ui.SessionListScreen(
                title = "Active sessions",
                viewModel = hiltViewModel<SessionListViewModel>(),
                onBack = { navController.popBackStack() },
                onLogoutAll = {
                    scope.launch {
                        sessionController.clearLoggedOut()
                        navController.navigate(AuthDestinations.Entry) { popUpTo(0) { inclusive = true } }
                    }
                },
            )
        }
        composable(AuthDestinations.CreatorAccess) {
            CreatorAccessApplicationScreen(
                viewModel = hiltViewModel<CreatorAccessViewModel>(),
                onBack = { navController.popBackStack() },
                onSubmitted = { navController.popBackStack() },
            )
        }
        composable(AuthDestinations.CreatorLogin) {
            CreatorLoginScreen(
                viewModel = hiltViewModel<CreatorLoginViewModel>(),
                onBack = { navController.popBackStack() },
                onAuthenticated = {
                    scope.launch {
                        sessionController.persistPortal(LastPortal.CREATOR)
                        navController.navigate(VoiceCloudRoutes.CreatorPortal) {
                            popUpTo(VoiceCloudRoutes.Bootstrap) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(AuthDestinations.Portal) {
            val user = (sessionState as? AuthSessionState.Authenticated)?.user
            PortalSelectionScreen(
                displayName = user?.displayName ?: user?.username,
                canOpenCreator = user?.accountRole?.canAccessCreatorPortal() == true,
                onOpenUserPortal = {
                    scope.launch {
                        sessionController.persistPortal(LastPortal.USER)
                        openUserPortal()
                    }
                },
                onOpenCreatorPortal = {
                    scope.launch {
                        sessionController.persistPortal(LastPortal.CREATOR)
                        openCreatorPortal()
                    }
                },
                onApplyForCreatorAccess = if (user?.accountRole == VoiceCloudAccountRole.USER) {
                    { navController.navigate(AuthDestinations.CreatorAccess) }
                } else {
                    null
                },
                onSignOut = {
                    scope.launch {
                        sessionController.logout()
                        navController.navigate(AuthDestinations.Entry) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(VoiceCloudRoutes.AuthGraph) {
            LaunchedEffect(sessionState) {
                when (val state = sessionState) {
                    is AuthSessionState.Authenticated -> scope.launch { routeAuthenticatedUser(state.user) }
                    AuthSessionState.LoggedOut -> {
                        navController.navigate(AuthDestinations.Entry) {
                            popUpTo(VoiceCloudRoutes.AuthGraph) { inclusive = true }
                        }
                    }
                    AuthSessionState.Unknown -> Unit
                }
            }
        }
        composable(VoiceCloudRoutes.UserPortal) {
            ConsumerShell(
                onOpenCreatorWorkspace = {
                    if (sessionController.role().canAccessCreatorPortal()) {
                        navController.navigate(VoiceCloudRoutes.CreatorPortal) { launchSingleTop = true }
                    } else {
                        navController.navigate(AuthDestinations.CreatorAccess)
                    }
                },
                onOpenGuestUpgrade = {
                    navController.navigate(AuthDestinations.GuestUpgrade)
                },
                onSignOut = {
                    scope.launch {
                        sessionController.logout()
                        navController.navigate(AuthDestinations.Entry) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(VoiceCloudRoutes.CreatorPortal) {
            CreatorShell(
                onLeaveWorkspace = {
                    scope.launch { sessionController.persistPortal(LastPortal.USER) }
                    if (!navController.popBackStack(VoiceCloudRoutes.UserPortal, inclusive = false)) {
                        navController.navigate(VoiceCloudRoutes.UserPortal) {
                            popUpTo(VoiceCloudRoutes.CreatorPortal) { inclusive = true }
                        }
                    }
                },
                onSignOut = {
                    scope.launch {
                        sessionController.logout()
                        navController.navigate(AuthDestinations.Entry) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }
    }
}
