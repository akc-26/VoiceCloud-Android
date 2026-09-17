package app.voicecloud.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
import kotlin.math.sin

@Composable
fun VCLiveBadge(
    modifier: Modifier = Modifier,
    label: String = "LIVE",
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Row(
        modifier = modifier
            .background(colors.live, VoiceCloud.shapes.pill)
            .padding(horizontal = spacing.xs, vertical = spacing.xxs)
            .semantics {
                contentDescription = label
                liveRegion = LiveRegionMode.Polite
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        VCLivePulse(color = colors.textOnDark, size = spacing.xs)
        Text(label, style = VoiceCloud.typography.metadata, color = colors.textOnDark)
    }
}

@Composable
fun VCLivePulse(
    modifier: Modifier = Modifier,
    color: Color = VoiceCloud.colors.live,
    size: Dp = VoiceCloud.spacing.xs,
    active: Boolean = true,
) {
    val scale = rememberPulseScale(active)
    Box(
        modifier
            .size(size)
            .scale(scale)
            .background(color, VoiceCloud.shapes.avatar)
            .semantics { contentDescription = if (active) "Live" else "Idle" },
    )
}

@Composable
fun VCAudioWaveform(
    modifier: Modifier = Modifier,
    amplitudes: List<Float>? = null,
    active: Boolean = false,
    barCount: Int = 5,
    color: Color = VoiceCloud.colors.accent,
) {
    val spacing = VoiceCloud.spacing
    val phase = rememberWavePhase(active && amplitudes == null)
    val bars = amplitudes ?: List(barCount) { index ->
        if (active) {
            val wave = (sin(phase + index * 0.9f) + 1f) / 2f
            0.28f + wave * 0.72f
        } else {
            0.22f
        }
    }
    Row(
        modifier = modifier
            .height(spacing.xl)
            .semantics { contentDescription = if (active) "Audio active" else "Audio idle" },
        horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        bars.take(barCount).forEach { level ->
            Box(
                Modifier
                    .width(spacing.xxs)
                    .height(spacing.xl * level.coerceIn(0.15f, 1f))
                    .background(color, VoiceCloud.shapes.pill),
            )
        }
    }
}

@Composable
internal fun rememberPulseScale(active: Boolean): Float {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val transition = rememberInfiniteTransition(label = "vcLivePulse")
    val animated by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(VoiceCloudMotion.PulseMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    return if (active && motionEnabled) animated else 1f
}

@Composable
internal fun rememberWavePhase(active: Boolean): Float {
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val transition = rememberInfiniteTransition(label = "vcWaveform")
    val animated by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(VoiceCloudMotion.WaveformMs, easing = LinearEasing),
        ),
        label = "wavePhase",
    )
    return if (active && motionEnabled) animated else 0f
}
