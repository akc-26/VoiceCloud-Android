package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.Image
import app.voicecloud.core.designsystem.VoiceCloud

@Immutable
data class VCRoomUiModel(
    val id: String = "",
    val title: String,
    val subtitle: String? = null,
    val metaLabel: String? = null,
    val hostLabel: String? = null,
    val live: Boolean = false,
    val cover: Painter? = null,
)

@Composable
fun VCRoomCard(
    room: VCRoomUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
        shape = VoiceCloud.shapes.compactCard,
        color = colors.surface,
        tonalElevation = VoiceCloud.elevation.card,
        shadowElevation = VoiceCloud.elevation.subtle,
    ) {
        Column {
            RoomCover(room, Modifier.fillMaxWidth().aspectRatio(16f / 9f))
            Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (room.live) {
                        VCLiveBadge()
                        Spacer(Modifier.width(spacing.xs))
                    }
                    Text(room.title, style = VoiceCloud.typography.sectionTitle, color = colors.textPrimary)
                }
                room.subtitle?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textSecondary) }
                room.metaLabel?.let { Text(it, style = VoiceCloud.typography.metadata, color = colors.textMuted) }
            }
        }
    }
}

@Composable
fun VCRoomCompactCard(
    room: VCRoomUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
        shape = VoiceCloud.shapes.compactCard,
        color = colors.surface,
        tonalElevation = VoiceCloud.elevation.subtle,
    ) {
        Row(
            Modifier.padding(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            RoomCover(room, Modifier.size(spacing.xxxl + spacing.xl).clip(VoiceCloud.shapes.small))
            Column(Modifier.weight(1f)) {
                Text(room.title, style = VoiceCloud.typography.actionLabel, color = colors.textPrimary)
                room.subtitle?.let { Text(it, style = VoiceCloud.typography.caption, color = colors.textSecondary) }
            }
            if (room.live) VCLiveBadge()
        }
    }
}

@Composable
fun VCRoomHero(
    room: VCRoomUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
        shape = VoiceCloud.shapes.hero,
        color = colors.liveSurface,
        shadowElevation = VoiceCloud.elevation.elevated,
    ) {
        Box {
            RoomCover(room, Modifier.fillMaxWidth().height(spacing.xxxl * 4), dark = true)
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                if (room.live) VCLiveBadge()
                Text(room.title, style = VoiceCloud.typography.screenTitle, color = colors.textOnDark)
                room.hostLabel?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textOnDark.copy(alpha = 0.8f)) }
                room.metaLabel?.let { Text(it, style = VoiceCloud.typography.metadata, color = colors.textOnDark.copy(alpha = 0.7f)) }
            }
        }
    }
}

@Composable
private fun RoomCover(room: VCRoomUiModel, modifier: Modifier, dark: Boolean = false) {
    val colors = VoiceCloud.colors
    Box(
        modifier = modifier.background(if (dark) colors.liveSurface else colors.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (room.cover != null) {
            Image(room.cover, contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        }
    }
}
