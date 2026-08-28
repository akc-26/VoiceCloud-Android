@file:Suppress("DEPRECATION") // Backend-compatible Google/Firebase token exchange retained until Credential Manager migration is coordinated.

package app.voicecloud.feature.auth.ui

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.feature.auth.model.AuthUser
import app.voicecloud.feature.auth.model.CreatorAccessApplicationRequest
import app.voicecloud.feature.auth.model.FirebaseClientConfig
import app.voicecloud.feature.auth.data.FirebaseGoogleTokenExchange
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
private fun AuthPage(
    title: String,
    subtitle: String,
    state: AuthUiState,
    creator: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    VoiceCloudTheme(portal = if (creator) PortalTheme.Creator else PortalTheme.User, darkTheme = creator) {
        Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()
                .padding(horizontal = 20.dp),
        ) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(12.dp))
                VoiceCloudBrandMark(62.dp)
                Spacer(Modifier.height(16.dp))
                Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(7.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(22.dp))
                Card(
                    Modifier.fillMaxWidth().widthIn(max = 560.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        state.error?.let { AlertBox(it, error = true) }
                        state.notice?.let { AlertBox(it, error = false) }
                        content()
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun AlertBox(message: String, error: Boolean) {
    val container = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(16.dp)) {
        Text(message, Modifier.fillMaxWidth().padding(14.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BusyButton(text: String, busy: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled && !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text(text)
    }
}

@Composable
private fun SecondaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text(text) }
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        visualTransformation = if (password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (password) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(if (passwordVisible) R.drawable.vc_icon_visibility_off else R.drawable.vc_icon_visibility),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
fun AuthGateScreen() {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            VoiceCloudBrandMark(64.dp)
            CircularProgressIndicator()
            Text("Restoring your secure ${VoiceCloudBrand.name} session…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PortalSelectorScreen(
    state: AuthUiState,
    onUser: () -> Unit,
    onCreator: () -> Unit,
) {
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = false) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))
                Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface) {
                    VoiceCloudBrandMark(72.dp)
                }
                Spacer(Modifier.height(22.dp))
                Text("Choose your portal", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text("One account. Two experiences.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                Column(Modifier.fillMaxWidth().widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    state.error?.let { AlertBox(it, true) }
                    state.notice?.let { AlertBox(it, false) }
                    PortalChoice(
                        title = "User Portal",
                        body = "Listen · Join · Connect",
                        iconRes = R.drawable.vc_portal_user,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = onUser,
                    )
                    PortalChoice(
                        title = "Creator Portal",
                        body = "Host · Create · Grow",
                        iconRes = R.drawable.vc_portal_creator,
                        accent = app.voicecloud.core.designsystem.theme.CreatorColors.PrimaryDark,
                        onClick = onCreator,
                    )
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun PortalChoice(
    title: String,
    body: String,
    iconRes: Int,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = .12f)) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(13.dp).size(30.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = accent)
        }
    }
}

@Composable
fun UserSignInScreen(
    state: AuthUiState,
    supportedLoginMethods: List<String>,
    firebaseClientConfig: FirebaseClientConfig,
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    onPhone: () -> Unit,
    onGoogleToken: (String) -> Unit,
    onGuest: () -> Unit,
    onForgot: () -> Unit,
    onBack: () -> Unit,
) {
    var identifier by rememberSaveableCompat("")
    var password by rememberSaveableCompat("")
    val methods = remember(supportedLoginMethods) {
        supportedLoginMethods.map { it.lowercase() }.toSet().ifEmpty { setOf("email") }
    }
    fun enabled(vararg aliases: String) = aliases.any { alias -> methods.any { it.contains(alias) } }

    AuthPage("User Portal", "Welcome back", state) {
        if (enabled("email", "password", "username")) {
            Field(identifier, { identifier = it }, "Email or username", keyboardType = KeyboardType.Email)
            Field(password, { password = it }, "Password", password = true)
            BusyButton("Sign In", state.busy) { onLogin(identifier, password) }
            TextAction("Forgot password?", onForgot)
        }
        if (enabled("phone")) SecondaryButton("Continue with phone", !state.busy, onPhone)
        if (enabled("google")) GoogleAuthButton(
            text = "Continue with Google",
            firebaseClientConfig = firebaseClientConfig,
            enabled = !state.busy,
            onToken = onGoogleToken,
        )
        if (enabled("guest")) SecondaryButton("Continue as guest", !state.busy, onGuest)
        HorizontalDivider()
        TextAction("Signup", onRegister)
        SecondaryButton("Creator Portal", !state.busy, onBack)
    }
}

@Composable
fun RegisterScreen(state: AuthUiState, onSubmit: (String, String, String, String) -> Unit, onBack: () -> Unit) {
    var username by rememberSaveableCompat("")
    var displayName by rememberSaveableCompat("")
    var email by rememberSaveableCompat("")
    var password by rememberSaveableCompat("")
    AuthPage("Create your account", "Your email and username become part of your ${VoiceCloudBrand.name} account identity.", state) {
        Field(displayName, { displayName = it }, "Display name")
        Field(username, { username = it }, "Username")
        Field(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Field(password, { password = it }, "Password (8+ characters)", password = true)
        BusyButton("Create Account", state.busy) { onSubmit(username, displayName, email, password) }
        TextAction("Back to sign in", onBack)
    }
}

@Composable
fun PhoneSignInScreen(state: AuthUiState, onSend: (String, String) -> Unit, onBack: () -> Unit) {
    var phone by rememberSaveableCompat("")
    var referral by rememberSaveableCompat("")
    AuthPage("Sign in with phone", "Use international E.164 format. ${VoiceCloudBrand.name} will send a six-digit verification code.", state) {
        Field(phone, { phone = it }, "Phone number, e.g. +919876543210", keyboardType = KeyboardType.Phone)
        Field(referral, { referral = it }, "Referral code (optional)")
        BusyButton("Send Verification Code", state.busy) { onSend(phone, referral) }
        TextAction("Back to sign in", onBack)
    }
}

@Composable
fun OtpVerifyScreen(state: AuthUiState, onVerify: (String) -> Unit, onResend: () -> Unit, onBack: () -> Unit) {
    var code by rememberSaveableCompat("")
    var cooldown by remember(state.otpCooldownSeconds, state.pendingPhone) { mutableIntStateOf(state.otpCooldownSeconds) }
    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            kotlinx.coroutines.delay(1000)
            cooldown -= 1
        }
    }
    AuthPage("Verify your phone", "Enter the six-digit code sent to ${state.pendingPhone}.", state) {
        Field(code, { value -> code = value.filter(Char::isDigit).take(6) }, "6-digit code", keyboardType = KeyboardType.Number)
        state.developmentOtp?.let {
            AlertBox("Development OTP: $it", error = false)
        }
        BusyButton("Verify & Continue", state.busy, code.length == 6) { onVerify(code) }
        SecondaryButton(if (cooldown > 0) "Resend in ${cooldown}s" else "Resend code", enabled = !state.busy && cooldown == 0) {
            cooldown = state.otpCooldownSeconds
            onResend()
        }
        TextAction("Change phone number", onBack)
    }
}

@Composable
fun ForgotPasswordScreen(state: AuthUiState, onSubmit: (String) -> Unit, onBack: () -> Unit) {
    var email by rememberSaveableCompat("")
    AuthPage("Reset your password", "We’ll send a secure, one-time recovery link to your account email.", state) {
        Field(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        BusyButton("Send Recovery Link", state.busy) { onSubmit(email) }
        TextAction("Back to sign in", onBack)
    }
}

@Composable
fun ResetPasswordScreen(state: AuthUiState, token: String, onSubmit: (String) -> Unit, onBack: () -> Unit) {
    var password by rememberSaveableCompat("")
    var confirm by rememberSaveableCompat("")
    var localError by remember { mutableStateOf<String?>(null) }
    AuthPage("Choose a new password", "The recovery link is single-use and time-limited.", state.copy(error = localError ?: state.error)) {
        if (token.isBlank()) AlertBox("This recovery link is missing its one-time token. Request a new link.", true)
        Field(password, { password = it }, "New password", password = true)
        Field(confirm, { confirm = it }, "Confirm password", password = true)
        BusyButton("Reset Password", state.busy, token.isNotBlank()) {
            localError = when {
                password.length < 8 -> "Your new password must be at least 8 characters."
                password != confirm -> "The password confirmation does not match."
                else -> null
            }
            if (localError == null) onSubmit(password)
        }
        TextAction("Back to sign in", onBack)
    }
}

private val onboardingInterests = listOf("Deep Talks", "Music", "Mindfulness", "Culture", "Books", "Comedy", "Night Rooms", "Learning")

@Composable
fun OnboardingScreen(state: AuthUiState, onFinish: (String, String, List<String>, Boolean) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var bio by rememberSaveableCompat(state.user?.bio.orEmpty())
    var country by rememberSaveableCompat(state.user?.country.orEmpty())
    var interests by remember { mutableStateOf(state.user?.interests.orEmpty()) }
    var reminders by remember { mutableStateOf(true) }
    AuthPage("Make ${VoiceCloudBrand.name} feel like yours", "First-time setup adds optional profile and discovery preferences.", state) {
        LinearProgressIndicator(progress = { step / 3f }, modifier = Modifier.fillMaxWidth())
        Text("Step $step of 3", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        when (step) {
            1 -> {
                Field(bio, { bio = it }, "Short bio (optional)", singleLine = false, minLines = 3)
                Field(country, { country = it }, "Country (optional)")
            }
            2 -> {
                Text("Choose topics you enjoy", style = MaterialTheme.typography.titleMedium)
                onboardingInterests.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { interest ->
                            FilterChip(
                                selected = interest in interests,
                                onClick = { interests = if (interest in interests) interests - interest else interests + interest },
                                label = { Text(interest) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            else -> {
                Text("Notifications", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Room and activity reminders", fontWeight = FontWeight.SemiBold)
                        Text("You can change this later.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = reminders, onCheckedChange = { reminders = it })
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 1) OutlinedButton(onClick = { step-- }, Modifier.weight(1f)) { Text("Previous") }
            Button(
                onClick = { if (step < 3) step++ else onFinish(bio, country, interests, reminders) },
                enabled = !state.busy,
                modifier = Modifier.weight(1f),
            ) { Text(if (step < 3) "Continue" else "Finish Setup") }
        }
    }
}

@Composable
fun UserReadyScreen(state: AuthUiState, onUpgrade: () -> Unit, onLogout: () -> Unit, onLogoutAll: () -> Unit) {
    val user = state.user
    AuthPage("You're signed in", "PH02 authentication is complete. Consumer Home and discovery begin in PH03.", state) {
        IdentityCard(user)
        if (user?.isGuest == true) {
            AlertBox("You’re using a temporary Guest account. Upgrade it to keep a permanent account identity.", false)
            BusyButton("Upgrade Guest Account", state.busy, onClick = onUpgrade)
        }
        SecondaryButton("Sign Out", !state.busy, onLogout)
        TextAction("Sign out on all devices", onLogoutAll)
    }
}

@Composable
fun GuestUpgradeScreen(
    state: AuthUiState,
    firebaseClientConfig: FirebaseClientConfig,
    onEmailUpgrade: (String, String, String) -> Unit,
    onSendPhoneOtp: (String) -> Unit,
    onPhoneUpgrade: (String, String, String) -> Unit,
    onGoogleUpgrade: (String?, String) -> Unit,
    onBack: () -> Unit,
) {
    var displayName by rememberSaveableCompat(state.user?.displayName.orEmpty())
    var email by rememberSaveableCompat("")
    var password by rememberSaveableCompat("")
    var phone by rememberSaveableCompat("")
    var otpCode by rememberSaveableCompat("")
    AuthPage("Keep your ${VoiceCloudBrand.name} account", "Upgrade this active Guest account without losing the server-side account identity.", state) {
        Field(displayName, { displayName = it }, "Display name")
        Text("Upgrade with email", style = MaterialTheme.typography.titleMedium)
        Field(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Field(password, { password = it }, "Password (8+ characters)", password = true)
        BusyButton("Upgrade with Email", state.busy) { onEmailUpgrade(displayName, email, password) }
        HorizontalDivider()
        Text("Upgrade with phone", style = MaterialTheme.typography.titleMedium)
        Field(phone, { phone = it }, "Phone number, e.g. +919876543210", keyboardType = KeyboardType.Phone)
        SecondaryButton("Send Verification Code", enabled = !state.busy) { onSendPhoneOtp(phone) }
        Field(otpCode, { otpCode = it.filter(Char::isDigit).take(6) }, "6-digit code", keyboardType = KeyboardType.Number)
        state.developmentOtp?.let { AlertBox("Development OTP: $it", false) }
        BusyButton("Upgrade with Phone", state.busy, otpCode.length == 6) { onPhoneUpgrade(displayName, phone, otpCode) }
        HorizontalDivider()
        GoogleAuthButton("Upgrade with Google", firebaseClientConfig, !state.busy) { token -> onGoogleUpgrade(displayName, token) }
        TextAction("Not now", onBack)
    }
}

@Composable
fun CreatorSignInScreen(
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit,
) {
    var identifier by rememberSaveableCompat("")
    var password by rememberSaveableCompat("")
    AuthPage(
        "Creator Portal",
        "Create. Host. Grow.",
        state,
        creator = true,
    ) {
        Field(identifier, { identifier = it }, "Creator email", keyboardType = KeyboardType.Email)
        Field(password, { password = it }, "Password", password = true)
        BusyButton("Enter Creator Studio", state.busy) { onLogin(identifier, password) }
        HorizontalDivider()
        SecondaryButton("Creator Access", !state.busy, onApply)
        SecondaryButton("User Portal", !state.busy, onBack)
    }
}

@Composable
fun CreatorAccessScreen(state: AuthUiState, onSubmit: (CreatorAccessApplicationRequest) -> Unit, onBack: () -> Unit) {
    var fullName by rememberSaveableCompat("")
    var email by rememberSaveableCompat("")
    var phone by rememberSaveableCompat("")
    var country by rememberSaveableCompat("")
    var category by rememberSaveableCompat("")
    var years by rememberSaveableCompat("0")
    var audience by rememberSaveableCompat("")
    var experience by rememberSaveableCompat("")
    var motivation by rememberSaveableCompat("")
    var portfolio by rememberSaveableCompat("")
    AuthPage("Apply for Creator access", "Submit your Creator background for ${VoiceCloudBrand.name} review.", state, creator = true) {
        Field(fullName, { fullName = it }, "Full name")
        Field(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Field(phone, { phone = it }, "Phone number", keyboardType = KeyboardType.Phone)
        Field(country, { country = it }, "Country")
        Field(category, { category = it }, "Creator category (optional)")
        Field(years, { years = it.filter(Char::isDigit).take(2) }, "Years of experience", keyboardType = KeyboardType.Number)
        Field(audience, { audience = it }, "Current audience (optional)")
        Field(experience, { experience = it }, "Creator experience", singleLine = false, minLines = 4)
        Field(motivation, { motivation = it }, "Why do you want Creator access?", singleLine = false, minLines = 4)
        Field(portfolio, { portfolio = it }, "Portfolio URL (optional)", keyboardType = KeyboardType.Uri)
        BusyButton("Submit Application", state.busy) {
            onSubmit(CreatorAccessApplicationRequest(
                fullName = fullName,
                email = email,
                phoneNumber = phone,
                country = country,
                creatorCategory = category,
                experienceYears = years.toIntOrNull()?.coerceIn(0, 60) ?: 0,
                currentAudience = audience,
                experience = experience,
                motivation = motivation,
                portfolioUrl = portfolio,
            ))
        }
        TextAction("Back to Creator sign in", onBack)
    }
}

@Composable
fun CreatorReadyScreen(state: AuthUiState, onLogout: () -> Unit, onLogoutAll: () -> Unit) {
    AuthPage("Creator access verified", "Your Creator access was validated by ${VoiceCloudBrand.name}.", state, creator = true) {
        IdentityCard(state.user)
        AlertBox("PH02 intentionally stops at the authenticated Creator handoff. It does not expose room, dashboard, RTC or earnings controls early.", false)
        SecondaryButton("Sign Out", !state.busy, onLogout)
        TextAction("Sign out on all devices", onLogoutAll)
    }
}

@Composable
private fun IdentityCard(user: AuthUser?) {
    if (user == null) return
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(user.displayName, style = MaterialTheme.typography.titleLarge)
            Text("@${user.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Role: ${user.role.uppercase()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RestrictedScreen(state: AuthUiState, onSignIn: () -> Unit, onPortal: () -> Unit) {
    AuthPage("Access is temporarily restricted", state.restrictedMessage ?: "VoiceCloud could not authorize this account for the requested action.", state.copy(error = null)) {
        AlertBox("This state reflects backend authorization. The Android app does not bypass account restrictions.", false)
        BusyButton("Return to Sign In", false, onClick = onSignIn)
        SecondaryButton("Choose Portal", onClick = onPortal)
    }
}

@Composable
fun SessionExpiredScreen(state: AuthUiState, onSignIn: () -> Unit, onPortal: () -> Unit) {
    AuthPage("Your session has expired", "Sign in again to continue. Your VoiceCloud account and server-side data remain unchanged.", state.copy(error = null)) {
        BusyButton("Sign In Again", false, onClick = onSignIn)
        SecondaryButton("Choose Portal", onClick = onPortal)
    }
}

@Composable
fun MaintenanceScreen(state: AuthUiState, onPortal: () -> Unit) {
    AuthPage("${VoiceCloudBrand.name} is under maintenance", state.maintenanceMessage ?: "${VoiceCloudBrand.name} is temporarily unavailable.", state.copy(error = null)) {
        AlertBox("Maintenance access is controlled by the backend. Creator/User authentication will not bypass it.", false)
        SecondaryButton("Back", onClick = onPortal)
    }
}

@Composable
private fun GoogleAuthButton(
    text: String,
    firebaseClientConfig: FirebaseClientConfig,
    enabled: Boolean,
    onToken: (String) -> Unit,
) {
    var localError by remember { mutableStateOf<String?>(null) }
    var exchanging by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val client = remember(firebaseClientConfig.googleWebClientId, activity) {
        if (activity == null || !firebaseClientConfig.isConfigured) null else {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(firebaseClientConfig.googleWebClientId)
                .build()
            GoogleSignIn.getClient(activity, options)
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (data == null) {
            localError = "Google Sign-In was cancelled."
        } else {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                val googleIdToken = account.idToken.orEmpty()
                if (googleIdToken.isBlank()) {
                    localError = "Google Sign-In did not return an ID token."
                } else {
                    exchanging = true
                    localError = null
                    scope.launch {
                        runCatching { FirebaseGoogleTokenExchange.exchange(context, googleIdToken, firebaseClientConfig) }
                            .onSuccess(onToken)
                            .onFailure { localError = it.message ?: "Firebase Google authentication could not complete." }
                        exchanging = false
                    }
                }
            } catch (e: ApiException) {
                localError = "Google Sign-In could not complete (${e.statusCode})."
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = {
                if (client == null) localError = "Google Sign-In is not configured for this Android build."
                else launcher.launch(client.signInIntent)
            },
            enabled = enabled && !exchanging,
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        ) {
            if (exchanging) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(text)
        }
        localError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
    }
}

/** Tiny saveable-like state helper without forcing feature UI to own navigation arguments. */
@Composable
private fun rememberSaveableCompat(initial: String): MutableState<String> = androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(initial) }
