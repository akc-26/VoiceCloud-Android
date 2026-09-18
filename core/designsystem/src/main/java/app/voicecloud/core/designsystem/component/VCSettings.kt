package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.foundation.vcTouchTarget

@Composable
fun VCSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    showDivider: Boolean = true,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.vcTouchTarget().clickable(role = Role.Button, onClick = onClick) else Modifier)
                .padding(horizontal = spacing.pageGutter, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.padding(end = spacing.sm))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = VoiceCloud.typography.body, color = colors.textPrimary)
                subtitle?.let { Text(it, style = VoiceCloud.typography.caption, color = colors.textMuted) }
            }
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.textMuted)
            }
        }
        if (showDivider) {
            HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun VCSelectionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = VoiceCloud.colors
    VCSettingsRow(
        title = title,
        subtitle = subtitle,
        modifier = modifier.semantics { this.selected = selected },
        onClick = onClick,
        trailing = {
            if (selected) {
                Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = colors.primary)
            }
        },
        showDivider = true,
    )
}
