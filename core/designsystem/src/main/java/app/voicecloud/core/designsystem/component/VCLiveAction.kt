package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons

@Composable
fun VCLiveAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Go live",
    icon: ImageVector = VoiceCloudIcons.Live,
    size: Dp = 64.dp,
    enabled: Boolean = true,
) {
    val colors = VoiceCloud.colors
    val elevation = VoiceCloud.elevation.floating
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation, CircleShape, clip = false)
            .background(colors.live, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textOnDark,
            modifier = Modifier.size(VoiceCloud.spacing.xl),
        )
    }
}
