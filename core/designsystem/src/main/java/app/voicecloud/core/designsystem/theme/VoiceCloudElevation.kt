package app.voicecloud.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class VoiceCloudElevation(
    val none: Dp = 0.dp,
    val subtle: Dp = 1.dp,
    val card: Dp = 2.dp,
    val elevated: Dp = 4.dp,
    val floating: Dp = 6.dp,
)

val LocalVoiceCloudElevation = staticCompositionLocalOf { VoiceCloudElevation() }
