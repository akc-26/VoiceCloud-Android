package app.voicecloud.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class VoiceCloudRadii(
    val small: Dp = 12.dp,
    val compactCard: Dp = 16.dp,
    val medium: Dp = 18.dp,
    val standard: Dp = 20.dp,
    val large: Dp = 24.dp,
    val hero: Dp = 28.dp,
    val extraLarge: Dp = 30.dp,
)

@Immutable
data class VoiceCloudShapes(
    val small: Shape,
    val compactCard: Shape,
    val standard: Shape,
    val hero: Shape,
    val bottomSheet: Shape,
    val pill: Shape,
    val avatar: Shape,
)

val LocalVoiceCloudRadii = staticCompositionLocalOf { VoiceCloudRadii() }
val LocalVoiceCloudShapes = staticCompositionLocalOf { voiceCloudShapes(VoiceCloudRadii()) }

internal fun voiceCloudShapes(radii: VoiceCloudRadii) = VoiceCloudShapes(
    small = RoundedCornerShape(radii.small),
    compactCard = RoundedCornerShape(radii.compactCard),
    standard = RoundedCornerShape(radii.standard),
    hero = RoundedCornerShape(radii.hero),
    bottomSheet = RoundedCornerShape(topStart = radii.extraLarge, topEnd = radii.extraLarge),
    pill = RoundedCornerShape(percent = 50),
    avatar = CircleShape,
)
