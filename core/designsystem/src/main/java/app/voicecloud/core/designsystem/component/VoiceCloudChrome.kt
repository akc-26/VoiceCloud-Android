package app.voicecloud.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.CreatorColors

data class VoiceCloudNavDestination(
    val id: String,
    val label: String,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int = icon,
)

@Composable
fun VoiceCloudConsumerBottomBar(
    selectedId: String,
    onHome: () -> Unit,
    onDiscover: () -> Unit,
    onLive: () -> Unit,
    onMessages: () -> Unit,
    onProfile: () -> Unit,
) {
    VoiceCloudBottomBar(
        destinations = listOf(
            VoiceCloudNavDestination("home", "Home", R.drawable.vc_nav_home, R.drawable.vc_nav_home_selected),
            VoiceCloudNavDestination("explore", "Discover", R.drawable.vc_nav_explore, R.drawable.vc_nav_explore_selected),
            VoiceCloudNavDestination("messages", "Messages", R.drawable.vc_icon_message),
            VoiceCloudNavDestination("profile", "Profile", R.drawable.vc_nav_profile, R.drawable.vc_nav_profile_selected),
        ),
        selectedId = selectedId,
        onSelect = { id ->
            when (id) {
                "home" -> onHome()
                "explore" -> onDiscover()
                "messages" -> onMessages()
                "profile" -> onProfile()
            }
        },
        onLive = onLive,
        selectedColor = ConsumerColors.Sapphire,
        mutedColor = ConsumerColors.TextMuted,
        liveColor = ConsumerColors.Sapphire,
        barColor = Color.White,
    )
}

@Composable
fun VoiceCloudCreatorBottomBar(
    selectedId: String,
    onHome: () -> Unit,
    onStudio: () -> Unit,
    onLive: () -> Unit,
    onAnalytics: () -> Unit,
    onProfile: () -> Unit,
) {
    VoiceCloudBottomBar(
        destinations = listOf(
            VoiceCloudNavDestination("home", "Home", R.drawable.vc_nav_home, R.drawable.vc_nav_home_selected),
            VoiceCloudNavDestination("studio", "Studio", R.drawable.vc_icon_host),
            VoiceCloudNavDestination("analytics", "Analytics", R.drawable.vc_icon_calendar),
            VoiceCloudNavDestination("profile", "Profile", R.drawable.vc_nav_profile, R.drawable.vc_nav_profile_selected),
        ),
        selectedId = selectedId,
        onSelect = { id ->
            when (id) {
                "home" -> onHome()
                "studio" -> onStudio()
                "analytics" -> onAnalytics()
                "profile" -> onProfile()
            }
        },
        onLive = onLive,
        selectedColor = CreatorColors.Primary,
        mutedColor = CreatorColors.TextMuted,
        liveColor = CreatorColors.Primary,
        barColor = CreatorColors.LightSurface,
        liveContentDescription = "Go live",
    )
}

@Composable
fun VoiceCloudBottomBar(
    destinations: List<VoiceCloudNavDestination>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onLive: () -> Unit,
    selectedColor: Color,
    mutedColor: Color,
    liveColor: Color,
    barColor: Color,
    liveContentDescription: String = "Live",
) {
    val left = destinations.take(2)
    val right = destinations.drop(2)
    Surface(
        color = barColor,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            left.forEach { destination ->
                VoiceCloudBottomNavItem(destination, selectedId == destination.id, selectedColor, mutedColor) { onSelect(destination.id) }
            }
            VoiceCloudLiveAction(onClick = onLive, color = liveColor, contentDescription = liveContentDescription)
            right.forEach { destination ->
                VoiceCloudBottomNavItem(destination, selectedId == destination.id, selectedColor, mutedColor) { onSelect(destination.id) }
            }
        }
    }
}

@Composable
private fun RowScope.VoiceCloudBottomNavItem(
    destination: VoiceCloudNavDestination,
    selected: Boolean,
    selectedColor: Color,
    mutedColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.weight(1f).fillMaxHeight(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(if (selected) destination.selectedIcon else destination.icon),
                contentDescription = destination.label,
                tint = if (selected) selectedColor else mutedColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                destination.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) selectedColor else mutedColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun VoiceCloudLiveAction(
    onClick: () -> Unit,
    color: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        shadowElevation = 8.dp,
        modifier = modifier.size(56.dp).offset(y = (-10).dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painterResource(R.drawable.vc_nav_live),
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}
