@file:Suppress("DEPRECATION") // Backend-compatible Google/Firebase token exchange retained until Credential Manager migration is coordinated.

package app.voicecloud.feature.auth.ui

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.component.VoiceCloudAnimatedWaveform
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedCard
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedChip
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedMetrics
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedPrimaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSecondaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedTextField
import app.voicecloud.core.designsystem.component.VoiceCloudPremiumBackdrop
import app.voicecloud.core.designsystem.component.VoiceCloudPremiumCard
import app.voicecloud.core.designsystem.component.VoiceCloudGlossCard
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudPosterArtwork
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.voiceCloudVisualFor
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
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
    VoiceCloudTheme(portal = if (creator) PortalTheme.Creator else PortalTheme.User, darkTheme = false) {
        Box(Modifier.fillMaxSize().background(ConsumerColors.Surface).imePadding()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = VoiceCloudApprovedMetrics.pagePadding, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                VoiceCloudToastEffect(state.error, state.notice)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    VoiceCloudBrandMark(34.dp)
                    Column {
                        Text(VoiceCloudBrand.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                        Text(voiceCloudTitleCase(VoiceCloudBrand.tagline), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.VioletDeep)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                    Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodyMedium, color = ConsumerColors.TextMuted)
                }
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(message: String) {
    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VoiceCloudPictogram(VoiceCloudVisualKind.HELP, size = 32.dp)
            Text(voiceCloudTitleCase(message), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
        }
    }
}

@Composable
private fun BusyButton(text: String, busy: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    VoiceCloudApprovedPrimaryButton(text = text, enabled = enabled && !busy, onClick = onClick)
}

@Composable
private fun SecondaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    VoiceCloudApprovedSecondaryButton(text = text, enabled = enabled, onClick = onClick)
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(voiceCloudTitleCase(text), style = MaterialTheme.typography.labelMedium, color = ConsumerColors.SapphireDeep)
    }
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
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(voiceCloudTitleCase(label), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyMedium,
            visualTransformation = if (password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (password) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(if (passwordVisible) R.drawable.vc_icon_visibility_off else R.drawable.vc_icon_visibility),
                            contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                        )
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ConsumerColors.Sapphire,
                unfocusedBorderColor = ConsumerColors.Border,
                focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
            ),
        )
    }
}

@Composable
fun AuthGateScreen() {
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = false) {
        Box(Modifier.fillMaxSize().background(ConsumerColors.Surface), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceCloudBrandMark(86.dp)
                Text(VoiceCloudBrand.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                Text("LIVE AUDIO. REAL CONNECTIONS.", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.VipGold, fontWeight = FontWeight.Bold)
                VoiceCloudAnimatedWaveform(Modifier.width(220.dp).height(74.dp), color = ConsumerColors.Sapphire, active = true)
                Spacer(Modifier.height(8.dp))
                Text(voiceCloudTitleCase("Connecting voices..."), style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                Text(voiceCloudTitleCase("Please wait"), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
                Box(Modifier.width(140.dp).height(2.dp).background(ConsumerColors.Border)) {
                    Box(Modifier.fillMaxWidth(.36f).height(2.dp).background(ConsumerColors.Sapphire))
                }
            }
        }
    }
}

@Composable
fun PortalSelectorScreen(
    state: AuthUiState,
    onUser: () -> Unit,
    onCreator: () -> Unit,
    onGuest: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(0) }
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = false) {
        Box(Modifier.fillMaxSize().background(ConsumerColors.Surface)) {
            VoiceCloudToastEffect(state.error, state.notice)
            if (step == 0) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        VoiceCloudBrandMark(40.dp)
                        Spacer(Modifier.width(9.dp))
                        Text(VoiceCloudBrand.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Speak. Connect. Belong.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = ConsumerColors.Ink,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "Join live conversations, share ideas and build communities around what matters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ConsumerColors.TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 330.dp),
                    )
                    Image(
                        painter = painterResource(R.drawable.vc_onboarding_conversation),
                        contentDescription = "VoiceCloud live conversation",
                        modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) { index ->
                            Box(Modifier.padding(horizontal = 3.dp).size(if (index == 0) 8.dp else 6.dp).clip(CircleShape).background(if (index == 0) ConsumerColors.Sapphire else ConsumerColors.Border))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    VoiceCloudApprovedPrimaryButton("Get Started", onClick = { step = 1 })
                    VoiceCloudApprovedSecondaryButton("Explore as Guest", onClick = onGuest)
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VoiceCloudBrandMark(34.dp)
                        Text(VoiceCloudBrand.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("How do you want\nto continue?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                    Text("Choose how you want to experience VoiceCloud.", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                    Spacer(Modifier.height(4.dp))
                    PortalChoice("Listener", "Join rooms, listen and connect with amazing people.", R.drawable.vc_portal_user, ConsumerColors.Sapphire, onUser)
                    PortalChoice("Speaker", "Host rooms and share your voice with the world.", R.drawable.vc_icon_host, ConsumerColors.Indigo, onUser)
                    PortalChoice("Creator", "Build your audience, manage creator tools and grow your community.", R.drawable.vc_portal_creator, ConsumerColors.SapphireDeep, onCreator)
                    Spacer(Modifier.weight(1f))
                    VoiceCloudApprovedPrimaryButton("Continue as Listener", onClick = onUser)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("Already have an account?", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                        TextButton(onClick = onUser) { Text("Sign In", style = MaterialTheme.typography.labelMedium, color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold) }
                    }
                    TextButton(onClick = { step = 0 }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Back", style = MaterialTheme.typography.labelSmall) }
                }
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
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = androidx.compose.ui.graphics.Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = .32f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Surface(shape = CircleShape, color = accent.copy(alpha = .10f), modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(painterResource(iconRes), null, tint = accent, modifier = Modifier.size(21.dp)) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = ConsumerColors.Ink)
                Text(voiceCloudTitleCase(body), style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted, maxLines = 2)
            }
            Surface(shape = CircleShape, color = accent, modifier = Modifier.size(18.dp)) { Box(Modifier.padding(5.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color.White)) }
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

    AuthPage("Welcome back 👋", "Sign in to continue", state) {
        if (enabled("email", "password", "username")) {
            Field(identifier, { identifier = it }, "Email or username", keyboardType = KeyboardType.Email)
            Field(password, { password = it }, "Password", password = true)
            TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) { Text("Forgot password?", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.SapphireDeep) }
            BusyButton("Sign In", state.busy) { onLogin(identifier, password) }
        }
        if (enabled("phone")) SecondaryButton("Continue with phone", !state.busy, onPhone)
        if (enabled("google")) GoogleAuthButton(
            text = "Continue with Google",
            firebaseClientConfig = firebaseClientConfig,
            enabled = !state.busy,
            onToken = onGoogleToken,
        )
        if (enabled("guest")) SecondaryButton("Continue as Guest", !state.busy, onGuest)
        HorizontalDivider()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Don’t have an account?", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
            TextButton(onClick = onRegister) { Text("Sign Up", style = MaterialTheme.typography.labelMedium, color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold) }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Creator Portal", style = MaterialTheme.typography.labelMedium, color = ConsumerColors.TextMuted) }
    }
}

@Composable
fun RegisterScreen(state: AuthUiState, onSubmit: (String, String, String, String) -> Unit, onBack: () -> Unit) {
    var username by rememberSaveableCompat("")
    var displayName by rememberSaveableCompat("")
    var email by rememberSaveableCompat("")
    var password by rememberSaveableCompat("")
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }
    AuthPage("Create your VoiceCloud account", "Let’s get you started", state) {
        Field(displayName, { displayName = it }, "Full name")
        Field(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Field(username, { username = it }, "Username")
        Field(password, { password = it }, "Password", password = true)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it }, modifier = Modifier.size(20.dp))
            Text("I agree to the Terms of Service and Privacy Policy", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted, modifier = Modifier.weight(1f))
        }
        BusyButton("Create Account", state.busy, acceptedTerms && username.isNotBlank() && email.isNotBlank() && password.length >= 8) { onSubmit(username, displayName, email, password) }
        TextAction("Already have an account? Sign In", onBack)
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
private fun VoiceCloudOtpDigitsField(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(6)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.fillMaxWidth().height(58.dp),
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(6) { index ->
                        val digit = value.getOrNull(index)?.toString().orEmpty()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = androidx.compose.ui.graphics.Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (index == value.length.coerceAtMost(5)) ConsumerColors.Sapphire.copy(alpha = .55f) else ConsumerColors.Border),
                            modifier = Modifier.weight(1f).height(50.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(digit, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                            }
                        }
                    }
                }
                Box(Modifier.size(1.dp)) { inner() }
            }
        },
    )
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
        Text("We’ve sent a 6-digit code to ${state.pendingPhone}.", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
        VoiceCloudOtpDigitsField(code) { code = it }
        Text(
            if (cooldown > 0) "Resend code in 00:${cooldown.toString().padStart(2, '0')}" else "You can request a new code now.",
            style = MaterialTheme.typography.labelSmall,
            color = ConsumerColors.TextMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        state.developmentOtp?.let { InfoCard("Development OTP: $it") }
        BusyButton("Verify & Continue", state.busy, code.length == 6) { onVerify(code) }
        VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Didn’t receive the code?", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = ConsumerColors.Ink)
                Text("Check your SMS spam folder or request a new code.", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
                TextButton(onClick = { cooldown = state.otpCooldownSeconds; onResend() }, enabled = !state.busy && cooldown == 0) { Text("Resend Code", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.SapphireDeep) }
            }
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
        if (token.isBlank()) InfoCard("This Recovery Link Is Missing Its One-Time Token. Request A New Link.")
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

private val onboardingInterests = listOf("Music", "Podcast", "Gaming", "Technology", "Motivation", "Business", "Sports", "Health", "Lifestyle")

@Composable
fun OnboardingScreen(state: AuthUiState, onFinish: (String, String, List<String>, Boolean) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var bio by rememberSaveableCompat(state.user?.bio.orEmpty())
    var country by rememberSaveableCompat(state.user?.country.orEmpty())
    var interests by remember { mutableStateOf(state.user?.interests.orEmpty()) }
    var reminders by remember { mutableStateOf(true) }
    AuthPage("Make ${VoiceCloudBrand.name} feel like yours", "First-time setup adds optional profile and discovery preferences.", state) {
        LinearProgressIndicator(progress = { step / 3f }, modifier = Modifier.fillMaxWidth())
        Text(voiceCloudTitleCase("Step $step Of 3"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        when (step) {
            1 -> {
                Field(bio, { bio = it }, "Short bio (optional)", singleLine = false, minLines = 3)
                Field(country, { country = it }, "Country (optional)")
            }
            2 -> {
                Text(voiceCloudTitleCase("Choose Topics You Enjoy"), style = MaterialTheme.typography.titleMedium)
                onboardingInterests.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { interest ->
                            VoiceCloudApprovedChip(
                                text = interest,
                                selected = interest in interests,
                                modifier = Modifier.weight(1f),
                                onClick = { interests = if (interest in interests) interests - interest else interests + interest },
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            else -> {
                Text(voiceCloudTitleCase("Notifications"), style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(voiceCloudTitleCase("Room And Activity Reminders"), fontWeight = FontWeight.SemiBold)
                        Text(voiceCloudTitleCase("You can change this later."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = reminders, onCheckedChange = { reminders = it })
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 1) OutlinedButton(onClick = { step-- }, Modifier.weight(1f)) { Text(voiceCloudTitleCase("Previous")) }
            Button(
                onClick = { if (step < 3) step++ else onFinish(bio, country, interests, reminders) },
                enabled = !state.busy,
                modifier = Modifier.weight(1f),
            ) { Text(voiceCloudTitleCase(if (step < 3) "Continue" else "Finish Setup")) }
        }
    }
}

@Composable
fun UserReadyScreen(state: AuthUiState, onUpgrade: () -> Unit, onLogout: () -> Unit, onLogoutAll: () -> Unit) {
    val user = state.user
    AuthPage("You're signed in", "PH02 authentication is complete. Consumer Home and discovery begin in PH03.", state) {
        IdentityCard(user)
        if (user?.isGuest == true) {
            InfoCard("You’re Using A Temporary Guest Account. Upgrade It To Keep A Permanent Account Identity.")
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
        Text(voiceCloudTitleCase("Upgrade With Email"), style = MaterialTheme.typography.titleMedium)
        Field(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Field(password, { password = it }, "Password (8+ characters)", password = true)
        BusyButton("Upgrade with Email", state.busy) { onEmailUpgrade(displayName, email, password) }
        HorizontalDivider()
        Text(voiceCloudTitleCase("Upgrade With Phone"), style = MaterialTheme.typography.titleMedium)
        Field(phone, { phone = it }, "Phone number, e.g. +919876543210", keyboardType = KeyboardType.Phone)
        SecondaryButton("Send Verification Code", enabled = !state.busy) { onSendPhoneOtp(phone) }
        Field(otpCode, { otpCode = it.filter(Char::isDigit).take(6) }, "6-digit code", keyboardType = KeyboardType.Number)
        state.developmentOtp?.let { InfoCard("Development OTP: $it") }
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
        InfoCard("Creator Access Is Verified. Open Creator Studio To Continue.")
        SecondaryButton("Sign Out", !state.busy, onLogout)
        TextAction("Sign out on all devices", onLogoutAll)
    }
}

@Composable
private fun IdentityCard(user: AuthUser?) {
    if (user == null) return
    VoiceCloudGlossCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VoiceCloudPictogram(VoiceCloudVisualKind.PROFILE, size = 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(user.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(voiceCloudTitleCase("@${user.username}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(voiceCloudTitleCase("Role: ${user.role.uppercase()}"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RestrictedScreen(state: AuthUiState, onSignIn: () -> Unit, onPortal: () -> Unit) {
    AuthPage("Access is temporarily restricted", state.restrictedMessage ?: "VoiceCloud could not authorize this account for the requested action.", state.copy(error = null)) {
        InfoCard("Account Access Is Controlled By VoiceCloud Security.")
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
        InfoCard("VoiceCloud Access Will Resume When Maintenance Is Complete.")
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
                localError = "Google Sign-In Couldn’t Complete. Try Again."
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
            else Text(voiceCloudTitleCase(text))
        }
        VoiceCloudToastEffect(localError, null)
    }
}

/** Tiny saveable-like state helper without forcing feature UI to own navigation arguments. */
@Composable
private fun rememberSaveableCompat(initial: String): MutableState<String> = androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(initial) }
