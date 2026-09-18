package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.foundation.vcTouchTarget

@Immutable
data class VCPersonUiModel(
    val userId: String? = null,
    val name: String,
    val subtitle: String? = null,
    val metaLabel: String? = null,
    val avatar: Painter? = null,
    val speaking: Boolean = false,
)

@Composable
fun VCPersonRow(
    person: VCPersonUiModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    val click = if (onClick != null) {
        Modifier
            .vcTouchTarget()
            .clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(click)
            .padding(horizontal = spacing.pageGutter, vertical = spacing.xs)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        if (person.speaking) {
            VCSpeakingAvatar(name = person.name, speaking = true, image = person.avatar)
        } else {
            VCAvatar(name = person.name, image = person.avatar)
        }
        Column(Modifier.weight(1f)) {
            Text(person.name, style = VoiceCloud.typography.actionLabel, color = colors.textPrimary)
            person.subtitle?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textSecondary) }
            person.metaLabel?.let { Text(it, style = VoiceCloud.typography.metadata, color = colors.textMuted) }
        }
        trailing?.invoke()
    }
}
