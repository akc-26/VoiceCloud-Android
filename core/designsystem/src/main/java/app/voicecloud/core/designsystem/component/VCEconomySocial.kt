package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons

@Composable
fun VCWalletBalance(
    amountLabel: String,
    modifier: Modifier = Modifier,
    caption: String = "Balance",
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = VoiceCloud.shapes.standard,
        color = colors.surfaceDark,
        shadowElevation = VoiceCloud.elevation.card,
    ) {
        Column(Modifier.padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Text(caption, style = VoiceCloud.typography.metadata, color = colors.textOnDark.copy(alpha = 0.72f))
            Text(amountLabel, style = VoiceCloud.typography.display, color = colors.coin)
        }
    }
}

@Immutable
data class VCGiftUiModel(
    val name: String,
    val costLabel: String,
    val selected: Boolean = false,
)

@Composable
fun VCGiftTile(
    gift: VCGiftUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Surface(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
        shape = VoiceCloud.shapes.compactCard,
        color = if (gift.selected) colors.primaryContainer else colors.surfaceSoft,
        tonalElevation = if (gift.selected) VoiceCloud.elevation.card else VoiceCloud.elevation.none,
    ) {
        Column(
            Modifier.padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Icon(VoiceCloudIcons.Gift, contentDescription = gift.name, tint = colors.coin, modifier = Modifier.size(spacing.xl))
            Text(gift.name, style = VoiceCloud.typography.actionLabel, color = colors.textPrimary)
            Text(gift.costLabel, style = VoiceCloud.typography.metadata, color = colors.coin)
        }
    }
}

@Composable
fun VCRankingRow(
    rankLabel: String,
    title: String,
    valueLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.pageGutter, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(rankLabel, style = VoiceCloud.typography.sectionTitle, color = colors.premium, modifier = Modifier.padding(end = spacing.xs))
        Text(title, style = VoiceCloud.typography.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(valueLabel, style = VoiceCloud.typography.actionLabel, color = colors.textSecondary)
    }
}

@Immutable
data class VCEventUiModel(
    val title: String,
    val timeLabel: String,
    val placeLabel: String? = null,
)

@Composable
fun VCEventCard(
    event: VCEventUiModel,
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
    ) {
        Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            Text(event.title, style = VoiceCloud.typography.sectionTitle, color = colors.textPrimary)
            Text(event.timeLabel, style = VoiceCloud.typography.metadata, color = colors.secondary)
            event.placeLabel?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textSecondary) }
        }
    }
}

@Immutable
data class VCCommunityUiModel(
    val name: String,
    val metaLabel: String? = null,
    val description: String? = null,
)

@Composable
fun VCCommunityCard(
    community: VCCommunityUiModel,
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
    ) {
        Row(
            Modifier.padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VoiceCloudBrandMark(size = spacing.xxxl, contentDescription = community.name)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(community.name, style = VoiceCloud.typography.sectionTitle, color = colors.textPrimary)
                community.metaLabel?.let { Text(it, style = VoiceCloud.typography.metadata, color = colors.textMuted) }
                community.description?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textSecondary) }
            }
        }
    }
}
