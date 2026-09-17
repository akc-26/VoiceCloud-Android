package app.voicecloud.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

sealed interface PortalTheme {
    data object User : PortalTheme
    data object Creator : PortalTheme
}

val LocalPortalTheme = staticCompositionLocalOf<PortalTheme> { PortalTheme.User }

@Composable
fun VoiceCloudTheme(
    portal: PortalTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val semantic = semanticColors(portal, darkTheme)
    val radii = VoiceCloudRadii()
    val typography = voiceCloudTypography()
    CompositionLocalProvider(
        LocalPortalTheme provides portal,
        LocalVoiceCloudColors provides semantic,
        LocalVoiceCloudTypography provides typography,
        LocalVoiceCloudSpacing provides VoiceCloudSpacing(),
        LocalVoiceCloudRadii provides radii,
        LocalVoiceCloudShapes provides voiceCloudShapes(radii),
        LocalVoiceCloudElevation provides VoiceCloudElevation(),
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme(portal, darkTheme, semantic),
            typography = voiceCloudMaterialTypography(typography),
            content = content,
        )
    }
}

internal fun semanticColors(portal: PortalTheme, darkTheme: Boolean): VoiceCloudSemanticColors = when (portal) {
    PortalTheme.User -> if (darkTheme) consumerDarkColors() else consumerLightColors()
    PortalTheme.Creator -> if (darkTheme) creatorDarkColors() else creatorLightColors()
}

private fun materialColorScheme(
    portal: PortalTheme,
    darkTheme: Boolean,
    colors: VoiceCloudSemanticColors,
): ColorScheme {
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.textPrimary,
            secondary = colors.secondary,
            onSecondary = colors.textOnDark,
            tertiary = colors.accent,
            onTertiary = colors.textOnDark,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceSoft,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.outline,
            error = colors.error,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.textPrimary,
            secondary = colors.secondary,
            onSecondary = colors.textOnDark,
            tertiary = colors.accent,
            onTertiary = colors.textOnDark,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceSoft,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.outline,
            error = colors.error,
        )
    }
    return if (portal == PortalTheme.Creator && !darkTheme) {
        scheme.copy(onPrimary = colors.onPrimary)
    } else {
        scheme
    }
}
