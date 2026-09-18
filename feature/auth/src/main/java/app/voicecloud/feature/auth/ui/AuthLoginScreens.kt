package app.voicecloud.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import app.voicecloud.core.designsystem.component.VCTextButton
import app.voicecloud.feature.auth.CreatorLoginViewModel
import app.voicecloud.feature.auth.UserLoginViewModel

@Composable
fun UserLoginScreen(
    viewModel: UserLoginViewModel,
    onBack: () -> Unit,
    onAuthenticated: () -> Unit,
    onRegister: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onPhoneSignIn: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginForm(
        title = "User Sign In",
        subtitle = "Access VoiceCloud with your account",
        identifierLabel = if (state.useEmail) "Email" else "Username",
        state = state,
        onBack = onBack,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onToggleIdentifierMode = viewModel::toggleUseEmail,
        onSubmit = { viewModel.submit { onAuthenticated() } },
        footer = {
            VCTextButton(text = "Create account", onClick = onRegister)
            VCTextButton(text = "Forgot password", onClick = onForgotPassword)
            VCTextButton(text = "Phone sign-in", onClick = onPhoneSignIn)
        },
        modifier = modifier,
    )
}

@Composable
fun CreatorLoginScreen(
    viewModel: CreatorLoginViewModel,
    onBack: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginForm(
        title = "Creator Sign In",
        subtitle = "Creator Studio requires a creator account",
        identifierLabel = if (state.useEmail) "Email" else "Username",
        state = state,
        onBack = onBack,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onToggleIdentifierMode = viewModel::toggleUseEmail,
        onSubmit = { viewModel.submit(onSuccess = onAuthenticated, onNotCreator = {}) },
        modifier = modifier,
    )
}

@Composable
private fun LoginForm(
    title: String,
    subtitle: String,
    identifierLabel: String,
    state: app.voicecloud.feature.auth.LoginUiState,
    onBack: () -> Unit,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleIdentifierMode: () -> Unit,
    onSubmit: () -> Unit,
    footer: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val spacing = VoiceCloud.spacing
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = spacing.pageGutter, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        VCPageHeader(title = title, subtitle = subtitle, onBack = onBack, applyStatusBarPadding = true)
        OutlinedTextField(
            value = state.identifier,
            onValueChange = onIdentifierChange,
            label = { Text(identifierLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (state.useEmail) KeyboardType.Email else KeyboardType.Text,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        VCTextButton(
            text = if (state.useEmail) "Use username instead" else "Use email instead",
            onClick = onToggleIdentifierMode,
        )
        footer?.invoke()
        state.errorMessage?.let { message ->
            VCErrorState(title = "Sign in failed", message = message)
        }
        VCPrimaryButton(
            text = if (state.isSubmitting) "Signing in…" else "Sign in",
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
