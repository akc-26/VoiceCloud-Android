package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.foundation.vcTouchTarget

@Composable
fun VCPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    applyStatusBarPadding: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    val type = VoiceCloud.typography
    val colors = VoiceCloud.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (applyStatusBarPadding) {
                    Modifier.windowInsetsPadding(WindowInsets.statusBars)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = spacing.pageGutter, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        if (onBack != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Navigate back",
                tint = colors.textPrimary,
                modifier = Modifier
                    .vcTouchTarget()
                    .clickable(role = Role.Button, onClick = onBack)
                    .padding(spacing.xs),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = type.screenTitle,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            if (subtitle != null) {
                Text(text = subtitle, style = type.bodySecondary, color = colors.textSecondary)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

@Composable
fun VCSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val spacing = VoiceCloud.spacing
    val type = VoiceCloud.typography
    val colors = VoiceCloud.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.pageGutter, vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = type.sectionTitle,
            color = colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = type.actionLabel,
                color = colors.primary,
                modifier = Modifier
                    .vcTouchTarget()
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(spacing.xs),
            )
        }
    }
}
