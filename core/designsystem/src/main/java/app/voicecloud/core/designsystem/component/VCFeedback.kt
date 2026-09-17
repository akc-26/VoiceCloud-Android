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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun VCEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    FeedbackState(title, message, actionLabel, onAction, modifier)
}

@Composable
fun VCErrorState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    FeedbackState(title, message, actionLabel, onAction, modifier, error = true)
}

@Composable
private fun FeedbackState(
    title: String,
    message: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier,
    error: Boolean = false,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xl, vertical = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            title,
            style = VoiceCloud.typography.sectionTitle,
            color = if (error) colors.error else colors.textPrimary,
        )
        message?.let { Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textSecondary) }
        if (actionLabel != null && onAction != null) {
            VCPrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun VCSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = VoiceCloud.spacing.xxxl,
) {
    val colors = VoiceCloud.colors
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    val transition = rememberInfiniteTransition(label = "vcSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(VoiceCloudMotion.SlowMs * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )
    val highlight = if (motionEnabled) colors.surfaceSoft.copy(alpha = alpha) else colors.surfaceSoft
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(VoiceCloud.shapes.compactCard)
            .background(Brush.horizontalGradient(listOf(colors.surfaceSoft, highlight, colors.surfaceSoft))),
    )
}
