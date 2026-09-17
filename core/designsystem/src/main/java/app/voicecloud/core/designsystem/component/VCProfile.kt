package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import app.voicecloud.core.designsystem.VoiceCloud

@Composable
fun VCStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Surface(
        modifier = modifier,
        shape = VoiceCloud.shapes.compactCard,
        color = colors.surface,
        tonalElevation = VoiceCloud.elevation.card,
    ) {
        Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            Text(value, style = VoiceCloud.typography.screenTitle, color = colors.textPrimary)
            Text(label, style = VoiceCloud.typography.metadata, color = colors.textMuted)
        }
    }
}

@Composable
fun VCProfileHeader(
    name: String,
    modifier: Modifier = Modifier,
    handle: String? = null,
    bio: String? = null,
    avatar: Painter? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.pageGutter, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VCAvatar(name = name, size = spacing.xxxl + spacing.md, image = avatar)
        Spacer(Modifier.width(spacing.md))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            Text(name, style = VoiceCloud.typography.screenTitle, color = colors.textPrimary)
            handle?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textSecondary) }
            bio?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textMuted) }
        }
        trailing?.invoke()
    }
}
