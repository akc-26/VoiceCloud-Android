package app.voicecloud.core.designsystem.component

import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.theme.CommonColors
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import kotlin.math.PI
import kotlin.math.sin

/**
 * Premium visual primitives for the approved VoiceCloud Android presentation.
 * These are intentionally presentation-only: callers continue to own all state,
 * navigation, API requests and business rules.
 */
@Composable
fun VoiceCloudPremiumBackdrop(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val top = if (dark) ConsumerColors.DeepNavy else ConsumerColors.Surface
    val bottom = if (dark) ConsumerColors.LiveSurface else ConsumerColors.Cloud
    val accent = if (dark) ConsumerColors.Ice else ConsumerColors.VipGold
    Box(
        modifier
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .drawBehind {
                val radius = size.minDimension * .48f
                drawCircle(
                    color = accent.copy(alpha = if (dark) .06f else .07f),
                    radius = radius,
                    center = Offset(size.width * .93f, size.height * .05f),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
                drawCircle(
                    color = accent.copy(alpha = if (dark) .04f else .055f),
                    radius = radius * .72f,
                    center = Offset(size.width * .04f, size.height * .88f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
        content = content,
    )
}

@Composable
fun VoiceCloudPremiumCard(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    VoiceCloudGlossCard(
        modifier = modifier,
        dark = dark,
        contentPadding = 18.dp,
        content = content,
    )
}

@Composable
fun VoiceCloudHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    badge: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val brush = if (dark) {
        Brush.linearGradient(listOf(ConsumerColors.LiveSurface, ConsumerColors.DeepNavy))
    } else {
        Brush.linearGradient(listOf(ConsumerColors.Surface, ConsumerColors.SapphireSoft, ConsumerColors.Lavender))
    }
    val titleColor = if (dark) ConsumerColors.TextOnDark else ConsumerColors.Ink
    val bodyColor = if (dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted
    Box(
        modifier
            .animateContentSize()
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
            .border(1.dp, (if (dark) ConsumerColors.DarkBorder else ConsumerColors.VipGold).copy(alpha = .45f), RoundedCornerShape(28.dp))
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (!badge.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = (if (dark) ConsumerColors.Ice else ConsumerColors.VipGold).copy(alpha = .16f),
                ) {
                    Text(
                        voiceCloudTitleCase(badge),
                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (dark) ConsumerColors.Ice else ConsumerColors.VioletDeep,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, color = titleColor, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = bodyColor, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                if (trailing != null) trailing.invoke(this)
                else Box(
                    Modifier.padding(start = 12.dp).size(width = 92.dp, height = 78.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, ConsumerColors.VipGold.copy(alpha=.28f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    VoiceCloudPosterArtwork(
                        kind = voiceCloudVisualFor(title),
                        modifier = Modifier.fillMaxSize(),
                        dark = dark,
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceCloudSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    meta: String? = null,
    dark: Boolean = false,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            voiceCloudTitleCase(title),
            style = MaterialTheme.typography.titleLarge,
            color = if (dark) ConsumerColors.TextOnDark else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (!meta.isNullOrBlank()) {
            Text(meta, style = MaterialTheme.typography.labelLarge, color = if (dark) ConsumerColors.TextOnDarkSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun VoiceCloudAnimatedWaveform(
    modifier: Modifier = Modifier,
    color: Color = ConsumerColors.Sapphire,
    active: Boolean = true,
    barCount: Int = 22,
) {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val transition = rememberInfiniteTransition(label = "voicecloud-wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(if (active && motionEnabled) 1500 else 100000), RepeatMode.Restart),
        label = "phase",
    )
    Canvas(modifier.height(56.dp).fillMaxWidth()) {
        val gap = size.width / (barCount * 1.7f)
        val barWidth = gap * .62f
        val centerY = size.height / 2f
        repeat(barCount) { i ->
            val x = (i + .5f) * size.width / barCount
            val wave = (sin((i * .58f) + phase) + 1f) / 2f
            val envelope = 1f - kotlin.math.abs((i - (barCount - 1) / 2f) / (barCount / 2f)) * .48f
            val half = (size.height * (.13f + wave * .34f) * envelope).coerceAtLeast(size.height * .08f)
            drawLine(color = color.copy(alpha = .62f + wave * .34f), start = Offset(x, centerY - half), end = Offset(x, centerY + half), strokeWidth = barWidth, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun VoiceCloudLiveBadge(
    modifier: Modifier = Modifier,
    label: String = "LIVE",
) {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val transition = rememberInfiniteTransition(label = "live-pulse")
    val alpha by transition.animateFloat(
        initialValue = .72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (motionEnabled) 900 else 100000), RepeatMode.Reverse),
        label = "live-alpha",
    )
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = CommonColors.Error.copy(alpha = alpha)) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White))
            Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun VoiceCloudSpeakingAvatar(
    label: String,
    modifier: Modifier = Modifier,
    speaking: Boolean = false,
    size: Dp = 58.dp,
    dark: Boolean = false,
) {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val transition = rememberInfiniteTransition(label = "speaker-ring")
    val pulse by transition.animateFloat(
        initialValue = .25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (speaking && motionEnabled) 850 else 100000), RepeatMode.Reverse),
        label = "speaker-pulse",
    )
    val avatarDiameter = size
    Box(modifier.size(avatarDiameter + 14.dp), contentAlignment = Alignment.Center) {
        if (speaking) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(ConsumerColors.Ice.copy(alpha = .13f + pulse * .16f), radius = avatarDiameter.toPx() * (.53f + pulse * .08f))
                drawCircle(ConsumerColors.Ice.copy(alpha = .45f + pulse * .45f), radius = avatarDiameter.toPx() * .55f, style = Stroke(2.dp.toPx()))
            }
        }
        Box(
            Modifier.size(avatarDiameter).clip(CircleShape)
                .background(Brush.linearGradient(listOf(ConsumerColors.SapphireSoft, ConsumerColors.SurfaceSoft)))
                .border(2.dp, if (speaking) ConsumerColors.Ice else ConsumerColors.VipGold.copy(alpha = .72f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize().padding(avatarDiameter * .18f)) {
                val canvasSize = this.size
                val center = Offset(canvasSize.width / 2f, canvasSize.height * .36f)
                drawCircle(ConsumerColors.SapphireDeep, radius = canvasSize.minDimension * .19f, center = center)
                drawArc(
                    color = ConsumerColors.SapphireDeep,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(canvasSize.width * .16f, canvasSize.height * .43f),
                    size = Size(canvasSize.width * .68f, canvasSize.height * .48f),
                    style = Stroke(width = canvasSize.minDimension * .14f, cap = StrokeCap.Round),
                )
            }
        }
    }
}

@Composable
fun VoiceCloudShimmer(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val transition = rememberInfiniteTransition(label = "premium-shimmer")
    val offset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(if (motionEnabled) VoiceCloudMotion.SlowMs * 3 else 100000), RepeatMode.Restart),
        label = "shimmer-offset",
    )
    val base = if (dark) ConsumerColors.LiveSurfaceElevated else ConsumerColors.SurfaceSoft
    val shine = if (dark) ConsumerColors.Ice.copy(alpha = .18f) else Color.White.copy(alpha = .85f)
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(base, shine, base),
                    start = Offset(offset * 500f, 0f),
                    end = Offset((offset + 1f) * 500f, 220f),
                )
            ),
    )
}

@Composable
private fun rememberVoiceCloudMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }
}
