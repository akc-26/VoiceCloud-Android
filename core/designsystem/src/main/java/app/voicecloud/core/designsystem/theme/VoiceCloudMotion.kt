package app.voicecloud.core.designsystem.theme

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Central motion tokens. [FastMs]/[StandardMs]/[SlowMs] remain stable for PH01 bootstrap.
 */
object VoiceCloudMotion {
    const val FastMs = 160
    const val StandardMs = 220
    const val SlowMs = 420
    const val PulseMs = 1100
    const val WaveformMs = 900
    const val EntranceMs = 280
    const val ExitMs = 180
    const val MicroMs = 120

    fun <T> fast(): TweenSpec<T> = tween(FastMs, easing = FastOutSlowInEasing)
    fun <T> standard(): TweenSpec<T> = tween(StandardMs, easing = FastOutSlowInEasing)
    fun <T> slow(): TweenSpec<T> = tween(SlowMs, easing = FastOutSlowInEasing)
    fun <T> entrance(): AnimationSpec<T> = tween(EntranceMs, easing = FastOutSlowInEasing)
    fun <T> exit(): AnimationSpec<T> = tween(ExitMs, easing = FastOutSlowInEasing)
    fun <T> micro(): AnimationSpec<T> = tween(MicroMs, easing = FastOutSlowInEasing)
    fun <T> stateTransition(): AnimationSpec<T> = standard()
}

@Composable
fun rememberVoiceCloudMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale > 0f
    }
}
