package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.foundation.vcTouchTarget

@Composable
fun VCPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = spacing.touch),
        shape = VoiceCloud.shapes.standard,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.surfaceSoft,
            disabledContentColor = colors.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(spacing.lg))
            Spacer(Modifier.width(spacing.xs))
        }
        Text(text, style = VoiceCloud.typography.actionLabel)
    }
}

@Composable
fun VCSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = spacing.touch),
        shape = VoiceCloud.shapes.standard,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
        border = BorderStroke(1.dp, colors.outline),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(spacing.lg))
            Spacer(Modifier.width(spacing.xs))
        }
        Text(text, style = VoiceCloud.typography.actionLabel)
    }
}

@Composable
fun VCTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.vcTouchTarget(),
        colors = ButtonDefaults.textButtonColors(contentColor = VoiceCloud.colors.primary),
    ) {
        Text(text, style = VoiceCloud.typography.actionLabel)
    }
}

@Composable
fun VCIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.vcTouchTarget(),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = VoiceCloud.colors.textPrimary)
    }
}
