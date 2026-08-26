package app.voicecloud.android.navigation

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.voicecloud.core.model.MobileConfig
import app.voicecloud.feature.auth.ui.*
import app.voicecloud.feature.auth.model.FirebaseClientConfig
import app.voicecloud.feature.bootstrap.BootstrapRoute

object VoiceCloudRoutes {
    const val Bootstrap = "bootstrap"
    // PH01 compatibility aliases retained while PH02 owns the actual auth graph routes.
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
                            if (event.clearAuthStack) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
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
                if (resetToken.isNotBlank()) {
                    navController.navigate(VoiceCloudRoutes.ResetPassword) { launchSingleTop = true }
                } else {
                    navController.navigate(VoiceCloudRoutes.AuthGate) { launchSingleTop = true }
                }
            }
        }
        composable(VoiceCloudRoutes.AuthGate) {
            LaunchedEffect(Unit) { authViewModel.restoreSession() }
            AuthGateScreen()
        }
        composable(VoiceCloudRoutes.PortalSelector) {
            PortalSelectorScreen(
                state = authState,
                onUser = { navController.navigate(VoiceCloudRoutes.UserSignIn) },
                onCreator = { navController.navigate(VoiceCloudRoutes.CreatorSignIn) },
            )
        }
        composable(VoiceCloudRoutes.UserSignIn) {
            UserSignInScreen(
                state = authState,
                supportedLoginMethods = mobileConfig?.supportedLoginMethods.orEmpty(),
                firebaseClientConfig = firebaseClientConfig,
                onLogin = { id, password -> authViewModel.login(id, password, creatorPortal = false) },
                onRegister = { navController.navigate(VoiceCloudRoutes.Register) },
                onPhone = { navController.navigate(VoiceCloudRoutes.PhoneSignIn) },
                onGoogleToken = { token -> authViewModel.googleLogin(token) },
                onGuest = { authViewModel.guestLogin() },
                onForgot = { navController.navigate(VoiceCloudRoutes.ForgotPassword) },
                onBack = { navController.navigate(VoiceCloudRoutes.PortalSelector) },
            )
        }
        composable(VoiceCloudRoutes.Register) {
            RegisterScreen(
                state = authState,
                onSubmit = authViewModel::register,
                onBack = { navController.navigate(VoiceCloudRoutes.UserSignIn) },
            )
        }
        composable(VoiceCloudRoutes.PhoneSignIn) {
            PhoneSignInScreen(
                state = authState,
                onSend = authViewModel::sendPhoneOtp,
                onBack = { navController.navigate(VoiceCloudRoutes.UserSignIn) },
            )
        }
        composable(VoiceCloudRoutes.OtpVerify) {
            OtpVerifyScreen(
                state = authState,
                onVerify = authViewModel::verifyPhoneOtp,
                onResend = authViewModel::resendPhoneOtp,
                onBack = { navController.navigate(VoiceCloudRoutes.PhoneSignIn) },
            )
        }
        composable(VoiceCloudRoutes.ForgotPassword) {
            ForgotPasswordScreen(
                state = authState,
                onSubmit = authViewModel::forgotPassword,
                onBack = { navController.navigate(VoiceCloudRoutes.UserSignIn) },
            )
        }
        composable(VoiceCloudRoutes.ResetPassword) {
            ResetPasswordScreen(
                state = authState,
                token = resetToken,
                onSubmit = { password -> authViewModel.resetPassword(resetToken, password) },
                onBack = {
                    resetToken = ""
                    navController.navigate(VoiceCloudRoutes.UserSignIn)
                },
            )
        }
        composable(VoiceCloudRoutes.Onboarding) {
            OnboardingScreen(state = authState, onFinish = authViewModel::completeOnboarding)
        }
        composable(VoiceCloudRoutes.UserReady) {
            UserReadyScreen(
                state = authState,
                onUpgrade = { navController.navigate(VoiceCloudRoutes.GuestUpgrade) },
                onLogout = { authViewModel.logout() },
                onLogoutAll = { authViewModel.logout(allDevices = true) },
            )
        }
        composable(VoiceCloudRoutes.GuestUpgrade) {
            GuestUpgradeScreen(
                state = authState,
                firebaseClientConfig = firebaseClientConfig,
                onEmailUpgrade = authViewModel::upgradeGuestEmail,
                onSendPhoneOtp = authViewModel::sendGuestUpgradePhoneOtp,
                onPhoneUpgrade = authViewModel::upgradeGuestPhone,
                onGoogleUpgrade = authViewModel::upgradeGuestGoogle,
                onBack = { navController.navigate(VoiceCloudRoutes.UserReady) },
            )
        }
        composable(VoiceCloudRoutes.CreatorSignIn) {
            CreatorSignInScreen(
                state = authState,
                onLogin = { id, password -> authViewModel.login(id, password, creatorPortal = true) },
                onApply = { navController.navigate(VoiceCloudRoutes.CreatorAccess) },
                onBack = { navController.navigate(VoiceCloudRoutes.PortalSelector) },
            )
        }
        composable(VoiceCloudRoutes.CreatorAccess) {
            CreatorAccessScreen(
                state = authState,
                onSubmit = authViewModel::submitCreatorAccess,
                onBack = { navController.navigate(VoiceCloudRoutes.CreatorSignIn) },
            )
        }
        composable(VoiceCloudRoutes.CreatorReady) {
            CreatorReadyScreen(
                state = authState,
                onLogout = { authViewModel.logout() },
                onLogoutAll = { authViewModel.logout(allDevices = true) },
            )
        }
        composable(VoiceCloudRoutes.Restricted) {
            RestrictedScreen(
                state = authState,
                onSignIn = { navController.navigate(VoiceCloudRoutes.UserSignIn) },
                onPortal = { navController.navigate(VoiceCloudRoutes.PortalSelector) },
            )
        }
        composable(VoiceCloudRoutes.SessionExpired) {
            SessionExpiredScreen(
                state = authState,
                onSignIn = { navController.navigate(VoiceCloudRoutes.UserSignIn) },
                onPortal = { navController.navigate(VoiceCloudRoutes.PortalSelector) },
            )
        }
        composable(VoiceCloudRoutes.Maintenance) {
            MaintenanceScreen(state = authState, onPortal = { navController.navigate(VoiceCloudRoutes.PortalSelector) })
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

