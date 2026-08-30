package app.voicecloud.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.theme.CommonColors
import app.voicecloud.core.designsystem.theme.ConsumerColors
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent

/** Data-driven media presentation shared by every VoiceCloud portal. */
@Composable
fun VoiceCloudRemoteMedia(
    url: String?,
    contentDescription: String?,
    modifier: Modifier,
    kind: VoiceCloudVisualKind = VoiceCloudVisualKind.AUDIO,
    dark: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes fallbackDrawable: Int? = null,
) {
    if (url.isNullOrBlank()) {
        VoiceCloudMediaFallback(modifier, kind, dark, fallbackDrawable)
        return
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    ) {
        val state by painter.state.collectAsState()
        when (state) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            is AsyncImagePainter.State.Loading -> Box(
                Modifier.fillMaxSize().background(if (dark) ConsumerColors.LiveSurface else ConsumerColors.SurfaceSoft),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }
            else -> VoiceCloudMediaFallback(Modifier.fillMaxSize(), kind, dark, fallbackDrawable)
        }
    }
}

@Composable
fun VoiceCloudAvatar(
    url: String?,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    online: Boolean = false,
    verified: Boolean = false,
    dark: Boolean = false,
) {
    Box(modifier.size(size + 6.dp), contentAlignment = Alignment.Center) {
        VoiceCloudRemoteMedia(
            url = url,
            contentDescription = if (label.isBlank()) "Profile photo" else "$label profile photo",
            modifier = Modifier.size(size).clip(CircleShape).border(2.dp, ConsumerColors.VipGold.copy(alpha = .72f), CircleShape),
            kind = VoiceCloudVisualKind.PROFILE,
            dark = dark,
        )
        if (online) {
            Box(
                Modifier.align(Alignment.BottomEnd).size((size.value * .24f).coerceAtLeast(12f).dp)
                    .clip(CircleShape).background(CommonColors.Success).border(2.dp, Color.White, CircleShape)
            )
        } else if (verified) {
            Box(
                Modifier.align(Alignment.BottomEnd).size((size.value * .22f).coerceAtLeast(11f).dp)
                    .clip(CircleShape).background(ConsumerColors.Sapphire).border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun VoiceCloudMediaFallback(
    modifier: Modifier,
    kind: VoiceCloudVisualKind,
    dark: Boolean,
    @DrawableRes fallbackDrawable: Int? = null,
) {
    val shape = RoundedCornerShape(22.dp)
    val colors = if (dark) {
        listOf(ConsumerColors.DeepNavy, ConsumerColors.LiveSurface, ConsumerColors.LiveSurfaceElevated)
    } else {
        listOf(ConsumerColors.SapphireSoft, ConsumerColors.Surface, ConsumerColors.Lavender.copy(alpha = .74f))
    }
    Box(
        modifier.clip(shape).background(Brush.linearGradient(colors))
            .border(1.dp, ConsumerColors.VipGold.copy(alpha = .30f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (fallbackDrawable != null) {
            Image(
                painter = painterResource(fallbackDrawable),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            VoiceCloudPosterArtwork(kind = kind, modifier = Modifier.fillMaxSize(), dark = dark)
        }
    }
}
