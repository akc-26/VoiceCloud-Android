package app.voicecloud.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun VCCommandBar(
    sideDestinations: List<VCNavDestination>,
    selectedRoute: String?,
    onSelect: (VCNavDestination) -> Unit,
    onLiveClick: () -> Unit,
    modifier: Modifier = Modifier,
    liveSelected: Boolean = false,
    liveContentDescription: String = "Live",
    liveIcon: ImageVector = VoiceCloudIcons.Live,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val liveScale by animateFloatAsState(
        targetValue = if (liveSelected) 1.06f else 1f,
        animationSpec = if (motionEnabled) VoiceCloudMotion.micro() else snap(),
        label = "liveActionScale",
    )
    val left = sideDestinations.take(2)
    val right = sideDestinations.drop(2)
    Box(modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = colors.surface,
            shadowElevation = VoiceCloud.elevation.elevated,
            shape = VoiceCloud.shapes.bottomSheet,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = spacing.xs, end = spacing.xs, top = spacing.sm, bottom = spacing.xs)
                    .height(spacing.touch + spacing.lg),
                verticalAlignment = Alignment.Bottom,
            ) {
                left.forEachIndexed { index, destination ->
                    CommandBarItem(
                        destination = destination,
                        selected = destination.route == selectedRoute && !liveSelected,
                        onClick = { onSelect(destination) },
                        modifier = Modifier.weight(1f).semantics { traversalIndex = index.toFloat() },
                    )
                }
                Spacer(Modifier.weight(1f))
                right.forEachIndexed { index, destination ->
                    CommandBarItem(
                        destination = destination,
                        selected = destination.route == selectedRoute && !liveSelected,
                        onClick = { onSelect(destination) },
                        modifier = Modifier.weight(1f).semantics { traversalIndex = (index + 3).toFloat() },
                    )
                }
            }
        }
        VCLiveAction(
            onClick = onLiveClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-spacing.md))
                .scale(liveScale)
                .semantics {
                    selected = liveSelected
                    traversalIndex = 2f
                },
            contentDescription = liveContentDescription,
            icon = liveIcon,
            size = spacing.touch + spacing.md,
        )
    }
}

@Composable
private fun CommandBarItem(
    destination: VCNavDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    val tint = if (selected) colors.primary else colors.textMuted
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = spacing.touch)
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        Icon(
            imageVector = if (selected) destination.selectedIcon else destination.icon,
            contentDescription = destination.contentDescription,
            tint = tint,
            modifier = Modifier.size(spacing.lg),
        )
        Text(
            text = destination.label,
            style = VoiceCloud.typography.caption,
            color = tint,
        )
    }
}
