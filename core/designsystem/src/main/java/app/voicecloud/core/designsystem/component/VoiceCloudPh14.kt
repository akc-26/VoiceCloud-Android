package app.voicecloud.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.theme.CommonColors
import app.voicecloud.core.designsystem.theme.ConsumerColors

/** PH14-R11 shared presentation primitives matching VC_Android_Design/src/ds.tsx. */

@Composable
fun VcLiveBadge(count: String? = null, small: Boolean = false) {
    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "live-dot",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(CommonColors.Error.copy(alpha = 0.10f))
            .padding(horizontal = if (small) 8.dp else 10.dp, vertical = if (small) 3.dp else 5.dp),
    ) {
        Box(Modifier.size(if (small) 6.dp else 8.dp).clip(CircleShape).background(CommonColors.Error.copy(alpha = pulse)))
        Text(
            "LIVE",
            color = CommonColors.Error,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
        )
        if (!count.isNullOrBlank()) {
            Text(count, color = ConsumerColors.TextMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun VcSectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = ConsumerColors.Text, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelMedium,
                color = ConsumerColors.Sapphire,
                modifier = Modifier.clickable(onClick = onAction).padding(4.dp),
            )
        }
    }
}

@Composable
fun VcSearchField(placeholder: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = ConsumerColors.Surface,
        modifier = modifier.fillMaxWidth().height(48.dp).border(1.dp, ConsumerColors.Border, RoundedCornerShape(16.dp)),
        shadowElevation = 0.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(painterResource(R.drawable.vc_icon_search), null, tint = ConsumerColors.TextMuted, modifier = Modifier.size(18.dp))
            Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = ConsumerColors.TextMuted)
        }
    }
}

@Composable
fun VcGreetingCard(
    name: String,
    subtitle: String,
    onJoinLive: (() -> Unit)?,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = ConsumerColors.Sapphire.copy(alpha = 0.18f), spotColor = ConsumerColors.Sapphire.copy(alpha = 0.22f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(ConsumerColors.Sapphire, ConsumerColors.Indigo, ConsumerColors.Ice)),
                    RoundedCornerShape(20.dp),
                )
                .padding(18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Good to hear you, $name", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.86f))
                    if (onJoinLive != null) {
                        Spacer(Modifier.height(6.dp))
                        Surface(onClick = onJoinLive, shape = RoundedCornerShape(50), color = Color.White) {
                            Text("Join Live", Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = ConsumerColors.Sapphire, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                VoiceCloudAnimatedWaveform(modifier = Modifier.size(72.dp), color = Color.White, barCount = 8)
            }
        }
    }
}

@Composable
fun VcRoomCard(
    title: String,
    host: String,
    topic: String,
    listeners: String,
    live: Boolean,
    coverUrl: String?,
    onClick: () -> Unit,
    onJoin: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.06f), spotColor = Color.Black.copy(alpha = 0.08f))
            .clip(shape)
            .background(ConsumerColors.Surface)
            .clickable(onClick = onClick)
            .border(1.dp, ConsumerColors.Border, shape),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(132.dp)
                .background(Brush.linearGradient(listOf(ConsumerColors.Sapphire.copy(alpha = 0.55f), ConsumerColors.Ice.copy(alpha = 0.35f)))),
        ) {
            VoiceCloudRemoteMedia(coverUrl, title, Modifier.fillMaxSize(), VoiceCloudVisualKind.LIVE, dark = true, contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))))
            Row(Modifier.align(Alignment.TopStart).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (live) VcLiveBadge(small = true)
            }
            Text(
                listeners,
                Modifier.align(Alignment.BottomEnd).padding(10.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = ConsumerColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$host · $topic", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (live && onJoin != null) {
                Surface(onClick = onJoin, shape = RoundedCornerShape(20.dp), color = ConsumerColors.Sapphire) {
                    Text("Join", Modifier.padding(horizontal = 14.dp, vertical = 7.dp), color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun VcPersonSuggestCard(
    name: String,
    subtitle: String,
    photo: String?,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ConsumerColors.Surface)
            .border(1.dp, ConsumerColors.Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VoiceCloudAvatar(photo, name, size = 56.dp, verified = false)
        Text(name, style = MaterialTheme.typography.labelLarge, color = ConsumerColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun VcConnectionBanner(
    message: String,
    color: Color,
    background: Color,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(background).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(message, color = color, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(action, color = color, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}
