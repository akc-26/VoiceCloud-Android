package app.voicecloud.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class VoiceCloudTypography(
    val display: TextStyle,
    val screenTitle: TextStyle,
    val sectionTitle: TextStyle,
    val body: TextStyle,
    val bodySecondary: TextStyle,
    val metadata: TextStyle,
    val actionLabel: TextStyle,
    val caption: TextStyle,
)

val LocalVoiceCloudTypography = staticCompositionLocalOf { voiceCloudTypography() }

internal fun voiceCloudTypography(): VoiceCloudTypography {
    val sans = FontFamily.SansSerif
    return VoiceCloudTypography(
        display = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.4).sp),
        screenTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.2).sp),
        sectionTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
        body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodySecondary = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
        metadata = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
        actionLabel = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        caption = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    )
}

internal fun voiceCloudMaterialTypography(tokens: VoiceCloudTypography): Typography = Typography(
    displaySmall = tokens.display,
    headlineMedium = tokens.screenTitle,
    titleLarge = tokens.sectionTitle,
    titleMedium = tokens.actionLabel.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = tokens.body,
    bodyMedium = tokens.bodySecondary,
    labelLarge = tokens.actionLabel,
    labelMedium = tokens.metadata,
    labelSmall = tokens.caption,
    bodySmall = tokens.caption,
)
