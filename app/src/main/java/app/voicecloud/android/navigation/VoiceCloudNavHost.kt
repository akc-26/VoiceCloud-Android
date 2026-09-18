package app.voicecloud.android.navigation

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.voicecloud.android.ui.shell.ConsumerShell
import app.voicecloud.android.ui.shell.CreatorShell
import app.voicecloud.core.model.MobileConfig
import app.voicecloud.core.model.VoiceCloudAccountRole
import app.voicecloud.core.preferences.LastPortal
import app.voicecloud.feature.auth.AuthGateViewModel
import app.voicecloud.feature.auth.AuthSessionState
import app.voicecloud.feature.auth.AuthSessionController
import app.voicecloud.feature.auth.CreatorLoginViewModel
import app.voicecloud.feature.auth.UserLoginViewModel
import app.voicecloud.feature.auth.ui.AuthEntryScreen
import app.voicecloud.feature.auth.ui.CreatorLoginScreen
import app.voicecloud.feature.auth.ui.PortalSelectionScreen
import app.voicecloud.feature.auth.ui.UserLoginScreen
import app.voicecloud.feature.bootstrap.BootstrapRoute
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
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
    const val RoomPreview = "user/room/preview"
    const val Live = "user/live"
    const val Messages = "user/messages"
    const val MessageThread = "user/messages/thread"
    const val Profile = "user/profile"
    const val Wallet = "user/wallet"
    const val Settings = "user/settings"
}

@Composable
fun VoiceCloudNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val scope = rememberCoroutineScope()
    var mobileConfig by remember { mutableStateOf<MobileConfig?>(null) }
    val authGateViewModel: AuthGateViewModel = hiltViewModel()
    val sessionController = authGateViewModel.sessionController
    val sessionState by authGateViewModel.sessionState.collectAsStateWithLifecycle()
    var sessionRestoreStarted by remember { mutableStateOf(false) }

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
                    if (!sessionRestoreStarted) {
                        sessionRestoreStarted = true
                        authGateViewModel.restore(onComplete = {})
                    }
                },
            )
        }
        composable(AuthDestinations.Entry) {
            val config = mobileConfig
            AuthEntryScreen(
                supportedLoginMethods = config?.supportedLoginMethods.orEmpty(),
                onUserSignIn = { navController.navigate(AuthDestinations.UserLogin) },
                onCreatorSignIn = { navController.navigate(AuthDestinations.CreatorLogin) },
                onContinueAsGuest = null,
            )
        }
        composable(AuthDestinations.UserLogin) {
            UserLoginScreen(
                viewModel = hiltViewModel<UserLoginViewModel>(),
                onBack = { navController.popBackStack() },
                onAuthenticated = {
                    navController.navigate(AuthDestinations.Portal) {
                        popUpTo(AuthDestinations.Entry) { inclusive = false }
                    }
                },
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
                        navController.navigate(VoiceCloudRoutes.UserPortal) {
                            popUpTo(VoiceCloudRoutes.AuthGraph) { inclusive = true }
                        }
                    }
                },
                onOpenCreatorPortal = {
                    scope.launch {
                        sessionController.persistPortal(LastPortal.CREATOR)
                        navController.navigate(VoiceCloudRoutes.CreatorPortal) {
                            popUpTo(VoiceCloudRoutes.AuthGraph) { inclusive = true }
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
        composable(VoiceCloudRoutes.AuthGraph) {
            LaunchedEffect(sessionState) {
                when (val state = sessionState) {
                    is AuthSessionState.Authenticated -> {
                        if (state.user.accountRole == VoiceCloudAccountRole.CREATOR) {
                            navController.navigate(AuthDestinations.Portal) {
                                popUpTo(VoiceCloudRoutes.AuthGraph) { inclusive = true }
                            }
                        } else {
                            navController.navigate(VoiceCloudRoutes.UserPortal) {
                                popUpTo(VoiceCloudRoutes.AuthGraph) { inclusive = true }
                            }
                        }
                    }
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
