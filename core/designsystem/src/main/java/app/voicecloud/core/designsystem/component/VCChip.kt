package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud

@Composable
fun VCChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = VoiceCloud.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = VoiceCloud.spacing.touch),
        label = { Text(label, style = VoiceCloud.typography.actionLabel) },
        shape = VoiceCloud.shapes.pill,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surfaceSoft,
            labelColor = colors.textSecondary,
            selectedContainerColor = colors.primaryContainer,
            selectedLabelColor = colors.primary,
        ),
    )
}
