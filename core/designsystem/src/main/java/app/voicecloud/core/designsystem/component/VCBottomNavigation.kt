package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import app.voicecloud.core.designsystem.VoiceCloud

@Immutable
data class VCNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val contentDescription: String = label,
)

@Composable
fun VCBottomNavigation(
    destinations: List<VCNavDestination>,
    selectedRoute: String?,
    onSelect: (VCNavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    NavigationBar(
        modifier = modifier.heightIn(min = VoiceCloud.spacing.touch),
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        tonalElevation = VoiceCloud.elevation.subtle,
    ) {
        destinations.forEach { destination ->
            val selected = destination.route == selectedRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = destination.contentDescription,
                    )
                },
                label = { Text(destination.label, style = VoiceCloud.typography.caption) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.primaryContainer,
                    unselectedIconColor = colors.textMuted,
                    unselectedTextColor = colors.textMuted,
                ),
            )
        }
    }
}
