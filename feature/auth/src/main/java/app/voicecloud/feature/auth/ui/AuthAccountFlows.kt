package app.voicecloud.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VCSettingsRow
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCTextButton
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.feature.auth.AuthSessionItem
import app.voicecloud.feature.auth.CreatorAccessViewModel
import app.voicecloud.feature.auth.ForgotPasswordViewModel
import app.voicecloud.feature.auth.PhoneAuthViewModel
import app.voicecloud.feature.auth.RegisterViewModel
import app.voicecloud.feature.auth.ResetPasswordViewModel
import app.voicecloud.feature.auth.SessionListViewModel
import app.voicecloud.feature.auth.GuestUpgradeViewModel
import app.voicecloud.feature.auth.LoginHistoryViewModel
import app.voicecloud.feature.auth.GoogleSignInViewModel

@Composable
fun GoogleSignInScreen(
    viewModel: GoogleSignInViewModel,
    onBack: () -> Unit,
    onAuthenticated: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(VoiceCloud.spacing.pageGutter), verticalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.md)) {
        VCPageHeader(
            title = "Google Sign-In",
            subtitle = "Use your Google account with VoiceCloud",
            onBack = onBack,
            applyStatusBarPadding = true,
        )
        Text(
            "Complete Google Sign-In from a build that includes the OAuth client configuration from VoiceCloud mobile config.",
            style = VoiceCloud.typography.bodySecondary,
            color = VoiceCloud.colors.textSecondary,
        )
        state.errorMessage?.let { VCErrorState(title = "Google Sign-In unavailable", message = it) }
        VCPrimaryButton(
            text = if (state.isSubmitting) "Please wait…" else "Continue with Google",
            onClick = { viewModel.signInWithIdToken(idToken = "", onAuthenticated = onAuthenticated) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun LoginHistoryScreen(
    viewModel: LoginHistoryViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        VCPageHeader(title = "Login activity", subtitle = "Recent sign-ins on your VoiceCloud account", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load activity", message = state.errorMessage)
            state.items.isEmpty() -> VCEmptyState(title = "No activity yet", message = "Sign-in history will appear after VoiceCloud records account activity.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                items(state.items, key = { it.id }) { item ->
                    SessionRow(item, canRevoke = false, onRevoke = {})
                }
            }
        }
    }
}

@Composable
fun UserRegisterScreen(
    viewModel: RegisterViewModel,
    onBack: () -> Unit,
    onRegistered: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = "Create account",
        subtitle = "Join VoiceCloud with email and username",
        onBack = onBack,
        errorMessage = state.errorMessage,
        isSubmitting = state.isSubmitting,
        submitLabel = "Create account",
        onSubmit = { viewModel.submit(onRegistered) },
    ) {
        OutlinedTextField(state.email, viewModel::onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.username, viewModel::onUsernameChange, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.displayName, viewModel::onDisplayNameChange, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.password, viewModel::onPasswordChange, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: ForgotPasswordViewModel, onBack: () -> Unit, onSent: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = "Forgot password",
        subtitle = "We will email reset instructions if the account exists",
        onBack = onBack,
        errorMessage = state.errorMessage,
        successMessage = state.successMessage,
        isSubmitting = state.isSubmitting,
        submitLabel = "Send reset email",
        onSubmit = { viewModel.submit(onSent) },
    ) {
        OutlinedTextField(state.email, viewModel::onEmailChange, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun ResetPasswordScreen(viewModel: ResetPasswordViewModel, token: String, onBack: () -> Unit, onComplete: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = "Reset password",
        subtitle = "Choose a new password for your VoiceCloud account",
        onBack = onBack,
        errorMessage = state.errorMessage,
        successMessage = state.successMessage,
        isSubmitting = state.isSubmitting,
        submitLabel = "Update password",
        onSubmit = { viewModel.submit(token, onComplete) },
    ) {
        OutlinedTextField(state.password, viewModel::onPasswordChange, label = { Text("New password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun PhoneSignInScreen(viewModel: PhoneAuthViewModel, onBack: () -> Unit, onAuthenticated: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = "Phone sign-in",
        subtitle = if (state.codeSent) "Enter the verification code" else "Enter your mobile number",
        onBack = onBack,
        errorMessage = state.errorMessage,
        isSubmitting = state.isSubmitting,
        submitLabel = if (state.codeSent) "Verify and sign in" else "Send code",
        onSubmit = { viewModel.submit(onAuthenticated) },
    ) {
        OutlinedTextField(state.phone, viewModel::onPhoneChange, label = { Text("Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
        if (state.codeSent) {
            OutlinedTextField(state.code, viewModel::onCodeChange, label = { Text("Verification code") }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun GuestUpgradeScreen(viewModel: GuestUpgradeViewModel, onBack: () -> Unit, onUpgraded: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = "Upgrade guest account",
        subtitle = "Add credentials to keep your VoiceCloud profile",
        onBack = onBack,
        errorMessage = state.errorMessage,
        isSubmitting = state.isSubmitting,
        submitLabel = "Upgrade account",
        onSubmit = { viewModel.submit(onUpgraded) },
    ) {
        OutlinedTextField(state.email, viewModel::onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.username, viewModel::onUsernameChange, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.password, viewModel::onPasswordChange, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun SessionListScreen(
    title: String,
    viewModel: SessionListViewModel,
    onBack: () -> Unit,
    revokeEnabled: Boolean = true,
    onLogoutAll: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        VCPageHeader(title = title, subtitle = "Manage your VoiceCloud account security", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load sessions", message = state.errorMessage)
            state.items.isEmpty() -> VCEmptyState(title = "No sessions", message = "You have no other active VoiceCloud sessions.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                items(state.items, key = { it.id }) { item ->
                    SessionRow(item, revokeEnabled && !item.isCurrent) { viewModel.revoke(item.id) }
                }
            }
        }
        if (onLogoutAll != null && !state.isLoading && state.errorMessage == null) {
            VCSecondaryButton(
                text = "Sign out everywhere",
                onClick = { viewModel.logoutAll(onLogoutAll) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.md),
            )
        }
    }
}

@Composable
private fun SessionRow(item: AuthSessionItem, canRevoke: Boolean, onRevoke: () -> Unit) {
    VCSettingsRow(
        title = item.label,
        subtitle = buildString {
            item.meta?.let { append(it) }
            if (item.isCurrent) {
                if (isNotEmpty()) append(" · ")
                append("Current session")
            }
        }.ifBlank { null },
        onClick = if (canRevoke) onRevoke else null,
    )
}

@Composable
fun CreatorAccessApplicationScreen(viewModel: CreatorAccessViewModel, onBack: () -> Unit, onSubmitted: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = "Creator access",
        subtitle = "Apply for Creator Studio access",
        onBack = onBack,
        errorMessage = state.errorMessage,
        successMessage = state.successMessage,
        isSubmitting = state.isSubmitting,
        submitLabel = "Submit application",
        onSubmit = { viewModel.submit(onSubmitted) },
    ) {
        OutlinedTextField(state.displayName, viewModel::onDisplayNameChange, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.bio, viewModel::onBioChange, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.reason, viewModel::onReasonChange, label = { Text("Why do you want to create?") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun AuthStatusScreen(title: String, message: String, onBack: () -> Unit, primaryAction: String, onPrimary: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(VoiceCloud.spacing.pageGutter), verticalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.md)) {
        VCPageHeader(title = title, onBack = onBack, applyStatusBarPadding = true)
        Text(message, style = VoiceCloud.typography.body, color = VoiceCloud.colors.textSecondary)
        VCPrimaryButton(text = primaryAction, onClick = onPrimary, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(VoiceCloud.spacing.pageGutter), verticalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.md)) {
        VCPageHeader(title = "Welcome to VoiceCloud", subtitle = "Discover live audio rooms, follow creators, and join conversations.", applyStatusBarPadding = true)
        VCPrimaryButton(text = "Continue to VoiceCloud", onClick = onContinue, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun AuthFormScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    errorMessage: String?,
    successMessage: String? = null,
    isSubmitting: Boolean,
    submitLabel: String,
    onSubmit: () -> Unit,
    fields: @Composable () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.md),
    ) {
        VCPageHeader(title = title, subtitle = subtitle, onBack = onBack, applyStatusBarPadding = true)
        fields()
        errorMessage?.let { VCErrorState(title = "Something went wrong", message = it) }
        successMessage?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = VoiceCloud.colors.textSecondary) }
        VCPrimaryButton(text = if (isSubmitting) "Please wait…" else submitLabel, onClick = onSubmit, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth())
    }
}
