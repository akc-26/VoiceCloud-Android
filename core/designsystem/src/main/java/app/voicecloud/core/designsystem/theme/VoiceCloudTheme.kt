package app.voicecloud.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed interface PortalTheme { data object User : PortalTheme; data object Creator : PortalTheme }

@Immutable
data class VoiceCloudRadii(val small: androidx.compose.ui.unit.Dp = 12.dp, val medium: androidx.compose.ui.unit.Dp = 18.dp, val large: androidx.compose.ui.unit.Dp = 24.dp, val extraLarge: androidx.compose.ui.unit.Dp = 30.dp)
object VoiceCloudMotion { const val FastMs = 160; const val StandardMs = 220; const val SlowMs = 420 }
val LocalVoiceCloudRadii = staticCompositionLocalOf { VoiceCloudRadii() }

private val consumerLight = lightColorScheme(
    primary = ConsumerColors.Sapphire, onPrimary = Color.White,
    primaryContainer = ConsumerColors.SapphireSoft, onPrimaryContainer = ConsumerColors.Ink,
    secondary = ConsumerColors.Indigo, onSecondary = Color.White,
    tertiary = ConsumerColors.Violet, onTertiary = Color.White,
    background = ConsumerColors.Cloud, onBackground = ConsumerColors.Text,
    surface = ConsumerColors.Surface, onSurface = ConsumerColors.Text,
    surfaceVariant = ConsumerColors.SurfaceSoft, onSurfaceVariant = ConsumerColors.TextMuted,
    outline = ConsumerColors.Border, error = CommonColors.Error,
)
private val consumerDark = darkColorScheme(
    primary = ConsumerColors.DarkSapphire, onPrimary = ConsumerColors.DarkBackground,
    primaryContainer = Color(0xFF0A2A34), onPrimaryContainer = ConsumerColors.DarkText,
    secondary = Color(0xFF6171FF), onSecondary = Color.White,
    background = ConsumerColors.DarkBackground, onBackground = ConsumerColors.DarkText,
    surface = ConsumerColors.DarkSurface, onSurface = ConsumerColors.DarkText,
    surfaceVariant = ConsumerColors.DarkSurfaceSoft, onSurfaceVariant = ConsumerColors.DarkMuted,
    outline = ConsumerColors.DarkBorder, error = CommonColors.Error,
)
private val creatorLight = lightColorScheme(
    primary = CreatorColors.Primary, onPrimary = Color(0xFF07130D),
    secondary = CreatorColors.Secondary, onSecondary = Color.White,
    tertiary = CreatorColors.Accent, onTertiary = Color(0xFF062A25),
    background = CreatorColors.LightBackground, onBackground = CreatorColors.Text,
    surface = CreatorColors.LightSurface, onSurface = CreatorColors.Text,
    surfaceVariant = CreatorColors.Elevated, onSurfaceVariant = CreatorColors.TextMuted,
    outline = CreatorColors.Border, error = Color(0xFFDC2626),
)
private val creatorDark = darkColorScheme(
    primary = CreatorColors.PrimaryLight, onPrimary = Color(0xFF07130D),
    secondary = CreatorColors.SecondaryLight, onSecondary = Color(0xFF062A25),
    background = CreatorColors.DarkBackground, onBackground = CreatorColors.DarkText,
    surface = CreatorColors.DarkSurface, onSurface = CreatorColors.DarkText,
    surfaceVariant = CreatorColors.DarkElevated, onSurfaceVariant = CreatorColors.DarkMuted,
    outline = CreatorColors.DarkBorder, error = Color(0xFFEF4444),
)

private val voiceCloudTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
)

@Composable
fun VoiceCloudTheme(portal: PortalTheme, darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors: ColorScheme = when (portal) {
        PortalTheme.User -> if (darkTheme) consumerDark else consumerLight
        PortalTheme.Creator -> if (darkTheme) creatorDark else creatorLight
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalVoiceCloudRadii provides VoiceCloudRadii()) {
        MaterialTheme(colorScheme = colors, typography = voiceCloudTypography, content = content)
    }
}
