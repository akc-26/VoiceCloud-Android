package app.voicecloud.android.navigation

object AuthDestinations {
    const val Entry = "auth/entry"
    const val UserLogin = "auth/user/login"
    const val UserRegister = "auth/user/register"
    const val CreatorLogin = "auth/creator/login"
    const val Portal = "auth/portal"
    const val PhoneSignIn = "auth/phone"
    const val ForgotPassword = "auth/forgot-password"
    const val ResetPassword = "auth/reset-password?token={token}"
    const val ResetPasswordRoute = "auth/reset-password"
    const val GuestUpgrade = "auth/guest/upgrade"
    const val Onboarding = "auth/onboarding"
    const val RestrictedAccount = "auth/restricted"
    const val SessionExpired = "auth/session-expired"
    const val Sessions = "auth/sessions"
    const val LoginHistory = "auth/login-history"
    const val CreatorAccess = "auth/creator/access"
    const val GoogleSignIn = "auth/google"

    const val ResetTokenArg = "token"
}

object SecurityDestinations {
    const val Sessions = "user/security/sessions"
    const val LoginHistory = "user/security/login-history"
}
