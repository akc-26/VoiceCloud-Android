package app.voicecloud.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark

@Composable
fun AuthEntryScreen(
    supportedLoginMethods: List<String>,
    onUserSignIn: () -> Unit,
    onCreatorSignIn: () -> Unit,
    onContinueAsGuest: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = VoiceCloud.spacing
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = spacing.pageGutter, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        VoiceCloudBrandMark(size = spacing.xxxl, contentDescription = "VoiceCloud")
        VCPageHeader(
            title = "Welcome to VoiceCloud",
            subtitle = "Sign in to your account or open Creator Studio",
            applyStatusBarPadding = true,
        )
        if (supportedLoginMethods.isNotEmpty()) {
            Text(
                text = "Available sign-in methods: ${supportedLoginMethods.joinToString(" · ")}",
                style = VoiceCloud.typography.bodySecondary,
                color = VoiceCloud.colors.textSecondary,
            )
        }
        Spacer(Modifier.height(spacing.sm))
        VCPrimaryButton(text = "User Sign In", onClick = onUserSignIn, modifier = Modifier.fillMaxWidth())
        VCSecondaryButton(text = "Creator Sign In", onClick = onCreatorSignIn, modifier = Modifier.fillMaxWidth())
        if (onContinueAsGuest != null && supportedLoginMethods.any { it.equals("guest", ignoreCase = true) }) {
            VCSecondaryButton(
                text = "Continue as guest",
                onClick = onContinueAsGuest,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun PortalSelectionScreen(
    displayName: String?,
    canOpenCreator: Boolean,
    onOpenUserPortal: () -> Unit,
    onOpenCreatorPortal: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = VoiceCloud.spacing
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = spacing.pageGutter, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
        horizontalAlignment = Alignment.Start,
    ) {
        VCPageHeader(
            title = "Choose your workspace",
            subtitle = displayName?.let { "Signed in as $it" } ?: "Your VoiceCloud session is active",
            applyStatusBarPadding = true,
        )
        VCPrimaryButton(text = "VoiceCloud", onClick = onOpenUserPortal, modifier = Modifier.fillMaxWidth())
        if (canOpenCreator) {
            VCSecondaryButton(text = "Creator Studio", onClick = onOpenCreatorPortal, modifier = Modifier.fillMaxWidth())
        }
        VCSecondaryButton(text = "Sign out", onClick = onSignOut, modifier = Modifier.fillMaxWidth())
    }
}
