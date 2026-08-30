package app.voicecloud.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared adaptive page chrome metrics. System insets are consumed once by the app root; this object
 * owns page/header/footer rhythm so compact and large devices do not need per-screen magic numbers.
 */
@Immutable
data class VoiceCloudAdaptiveMetrics(
    val horizontalPadding: Dp,
    val contentTopSpacing: Dp,
    val contentBottomSpacing: Dp,
    val topBarMinHeight: Dp,
    val bottomBarHeight: Dp,
    val bottomIconSize: Dp,
    val sectionSpacing: Dp,
)

object VoiceCloudPageMetrics {
    @Composable
    fun current(): VoiceCloudAdaptiveMetrics {
        val px = LocalWindowInfo.current.containerSize.width
        val widthDp = with(LocalDensity.current) { px.toDp() }
        return when {
            widthDp < 360.dp -> VoiceCloudAdaptiveMetrics(14.dp, 8.dp, 14.dp, 50.dp, 62.dp, 21.dp, 12.dp)
            widthDp < 600.dp -> VoiceCloudAdaptiveMetrics(16.dp, 8.dp, 16.dp, 52.dp, 64.dp, 22.dp, 14.dp)
            else -> VoiceCloudAdaptiveMetrics(24.dp, 12.dp, 24.dp, 58.dp, 70.dp, 24.dp, 18.dp)
        }
    }
}
