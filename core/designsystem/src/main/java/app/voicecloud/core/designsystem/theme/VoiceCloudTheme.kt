package app.voicecloud.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed interface PortalTheme { data object User : PortalTheme; data object Creator : PortalTheme }

@Immutable
data class VoiceCloudRadii(
    val small: androidx.compose.ui.unit.Dp = app.voicecloud.core.designsystem.BuildConfig.RADIUS_SMALL.dp,
    val medium: androidx.compose.ui.unit.Dp = app.voicecloud.core.designsystem.BuildConfig.RADIUS_MEDIUM.dp,
    val large: androidx.compose.ui.unit.Dp = app.voicecloud.core.designsystem.BuildConfig.RADIUS_LARGE.dp,
    val extraLarge: androidx.compose.ui.unit.Dp = app.voicecloud.core.designsystem.BuildConfig.RADIUS_EXTRA_LARGE.dp,
)
object VoiceCloudMotion {
    val FastMs: Int get() = app.voicecloud.core.designsystem.BuildConfig.MOTION_FAST_MS
    val StandardMs: Int get() = app.voicecloud.core.designsystem.BuildConfig.MOTION_STANDARD_MS
    val SlowMs: Int get() = app.voicecloud.core.designsystem.BuildConfig.MOTION_SLOW_MS
}
val LocalVoiceCloudRadii = staticCompositionLocalOf { VoiceCloudRadii() }

private val consumerLight = lightColorScheme(
    primary = ConsumerColors.Sapphire, onPrimary = Color.White,
    primaryContainer = ConsumerColors.SapphireSoft, onPrimaryContainer = ConsumerColors.Ink,
    inversePrimary = ConsumerColors.Ice,
    secondary = ConsumerColors.Indigo, onSecondary = Color.White,
    secondaryContainer = ConsumerColors.SapphireSoft, onSecondaryContainer = ConsumerColors.Ink,
    tertiary = ConsumerColors.Violet, onTertiary = Color.White,
    tertiaryContainer = ConsumerColors.Lavender, onTertiaryContainer = ConsumerColors.Ink,
    background = ConsumerColors.Cloud, onBackground = ConsumerColors.Text,
    surface = ConsumerColors.Surface, onSurface = ConsumerColors.Text,
    surfaceVariant = ConsumerColors.SurfaceSoft, onSurfaceVariant = ConsumerColors.TextMuted,
    surfaceTint = ConsumerColors.Sapphire,
    inverseSurface = ConsumerColors.DeepNavy, inverseOnSurface = ConsumerColors.TextOnDark,
    outline = ConsumerColors.Border, outlineVariant = ConsumerColors.Sky,
    surfaceBright = ConsumerColors.Surface,
    surfaceDim = ConsumerColors.SurfaceSoft,
    surfaceContainerLowest = ConsumerColors.Surface,
    surfaceContainerLow = ConsumerColors.Surface,
    surfaceContainer = ConsumerColors.Cloud,
    surfaceContainerHigh = ConsumerColors.SurfaceSoft,
    surfaceContainerHighest = ConsumerColors.SapphireSoft,
    scrim = ConsumerColors.DeepNavy,
    error = CommonColors.Error,
)
private val consumerDark = darkColorScheme(
    primary = ConsumerColors.DarkSapphire, onPrimary = ConsumerColors.DarkBackground,
    primaryContainer = ConsumerColors.DarkSurfaceSoft, onPrimaryContainer = ConsumerColors.DarkText,
    inversePrimary = ConsumerColors.SapphireDeep,
    secondary = ConsumerColors.DarkSapphireDeep, onSecondary = ConsumerColors.DarkBackground,
    secondaryContainer = ConsumerColors.LiveSurface, onSecondaryContainer = ConsumerColors.DarkText,
    tertiary = ConsumerColors.Ice, onTertiary = ConsumerColors.DarkBackground,
    tertiaryContainer = ConsumerColors.LiveSurfaceElevated, onTertiaryContainer = ConsumerColors.DarkText,
    background = ConsumerColors.DarkBackground, onBackground = ConsumerColors.DarkText,
    surface = ConsumerColors.DarkSurface, onSurface = ConsumerColors.DarkText,
    surfaceVariant = ConsumerColors.DarkSurfaceSoft, onSurfaceVariant = ConsumerColors.DarkMuted,
    surfaceTint = ConsumerColors.DarkSapphire,
    inverseSurface = ConsumerColors.Surface, inverseOnSurface = ConsumerColors.Ink,
    outline = ConsumerColors.DarkBorder, outlineVariant = ConsumerColors.Navy,
    surfaceBright = ConsumerColors.DarkSurfaceSoft,
    surfaceDim = ConsumerColors.DarkBackground,
    surfaceContainerLowest = ConsumerColors.DarkBackground,
    surfaceContainerLow = ConsumerColors.DarkSurface,
    surfaceContainer = ConsumerColors.DarkSurfaceSoft,
    surfaceContainerHigh = ConsumerColors.Navy,
    surfaceContainerHighest = ConsumerColors.LiveSurfaceElevated,
    scrim = Color.Black,
    error = CommonColors.Error,
)
private val creatorLight = lightColorScheme(
    primary = CreatorColors.Primary, onPrimary = CreatorColors.DarkBackground,
    secondary = CreatorColors.Secondary, onSecondary = Color.White,
    tertiary = CreatorColors.Accent, onTertiary = CreatorColors.Navigation,
    background = CreatorColors.LightBackground, onBackground = CreatorColors.Text,
    surface = CreatorColors.LightSurface, onSurface = CreatorColors.Text,
    surfaceVariant = CreatorColors.Elevated, onSurfaceVariant = CreatorColors.TextMuted,
    outline = CreatorColors.Border, error = CommonColors.Error,
)
private val creatorDark = darkColorScheme(
    primary = CreatorColors.PrimaryLight, onPrimary = CreatorColors.DarkBackground,
    secondary = CreatorColors.SecondaryLight, onSecondary = CreatorColors.Navigation,
    background = CreatorColors.DarkBackground, onBackground = CreatorColors.DarkText,
    surface = CreatorColors.DarkSurface, onSurface = CreatorColors.DarkText,
    surfaceVariant = CreatorColors.DarkElevated, onSurfaceVariant = CreatorColors.DarkMuted,
    outline = CreatorColors.DarkBorder, error = CommonColors.Error,
)


private val voiceCloudShapes = Shapes(
    extraSmall = RoundedCornerShape(app.voicecloud.core.designsystem.BuildConfig.RADIUS_SMALL.dp),
    small = RoundedCornerShape(app.voicecloud.core.designsystem.BuildConfig.RADIUS_SMALL.dp),
    medium = RoundedCornerShape(app.voicecloud.core.designsystem.BuildConfig.RADIUS_MEDIUM.dp),
    large = RoundedCornerShape(app.voicecloud.core.designsystem.BuildConfig.RADIUS_LARGE.dp),
    extraLarge = RoundedCornerShape(app.voicecloud.core.designsystem.BuildConfig.RADIUS_EXTRA_LARGE.dp),
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
        MaterialTheme(colorScheme = colors, typography = voiceCloudTypography, shapes = voiceCloudShapes, content = content)
    }
}
