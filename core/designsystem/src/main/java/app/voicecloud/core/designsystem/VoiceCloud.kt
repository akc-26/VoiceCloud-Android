package app.voicecloud.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import app.voicecloud.core.designsystem.theme.LocalPortalTheme
import app.voicecloud.core.designsystem.theme.LocalVoiceCloudColors
import app.voicecloud.core.designsystem.theme.LocalVoiceCloudElevation
import app.voicecloud.core.designsystem.theme.LocalVoiceCloudRadii
import app.voicecloud.core.designsystem.theme.LocalVoiceCloudShapes
import app.voicecloud.core.designsystem.theme.LocalVoiceCloudSpacing
import app.voicecloud.core.designsystem.theme.LocalVoiceCloudTypography
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudElevation
import app.voicecloud.core.designsystem.theme.VoiceCloudRadii
import app.voicecloud.core.designsystem.theme.VoiceCloudSemanticColors
import app.voicecloud.core.designsystem.theme.VoiceCloudShapes
import app.voicecloud.core.designsystem.theme.VoiceCloudSpacing
import app.voicecloud.core.designsystem.theme.VoiceCloudTypography

/** Semantic token accessors. Feature screens should read tokens through this object. */
object VoiceCloud {
    val colors: VoiceCloudSemanticColors
        @Composable @ReadOnlyComposable get() = LocalVoiceCloudColors.current

    val typography: VoiceCloudTypography
        @Composable @ReadOnlyComposable get() = LocalVoiceCloudTypography.current

    val spacing: VoiceCloudSpacing
        @Composable @ReadOnlyComposable get() = LocalVoiceCloudSpacing.current

    val radii: VoiceCloudRadii
        @Composable @ReadOnlyComposable get() = LocalVoiceCloudRadii.current

    val shapes: VoiceCloudShapes
        @Composable @ReadOnlyComposable get() = LocalVoiceCloudShapes.current

    val elevation: VoiceCloudElevation
        @Composable @ReadOnlyComposable get() = LocalVoiceCloudElevation.current

    val portal: PortalTheme
        @Composable @ReadOnlyComposable get() = LocalPortalTheme.current
}
