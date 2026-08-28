package app.voicecloud.android

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.toArgb
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.voicecloud.core.designsystem.theme.CommonColors
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.CreatorColors
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceCloudFoundationInstrumentedTest {
    @Test
    fun packageIdentityMatchesGeneratedApplicationId() {
        assertEquals(
            BuildConfig.APPLICATION_ID,
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.packageName,
        )
    }

    @Test
    fun ph06DeclaresMicrophonePermissionForExplicitHostPublishing() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        val permissions = info.requestedPermissions?.toSet().orEmpty()
        assertEquals(true, Manifest.permission.RECORD_AUDIO in permissions)
    }

    /**
     * Runtime guard for centralized white-label colors.
     *
     * Compose's Color(ULong) constructor expects its internal packed color-space format.
     * Branding values are normal 32-bit ARGB and must therefore be created through Color(Int).
     * Calling toArgb() exercises the same ColorSpace conversion path used by Text rendering.
     */
    @Test
    fun centralizedBrandPaletteUsesValidArgbColors() {
        val palette = listOf(
            ConsumerColors.Sapphire,
            ConsumerColors.SapphireDeep,
            ConsumerColors.SapphireSoft,
            ConsumerColors.Ice,
            ConsumerColors.Indigo,
            ConsumerColors.Violet,
            ConsumerColors.VioletDeep,
            ConsumerColors.Lavender,
            ConsumerColors.LavenderStrong,
            ConsumerColors.Sky,
            ConsumerColors.Cloud,
            ConsumerColors.Surface,
            ConsumerColors.SurfaceSoft,
            ConsumerColors.Ink,
            ConsumerColors.Text,
            ConsumerColors.TextMuted,
            ConsumerColors.Border,
            ConsumerColors.DeepNavy,
            ConsumerColors.Navy,
            ConsumerColors.LiveSurface,
            ConsumerColors.LiveSurfaceElevated,
            ConsumerColors.TextOnDark,
            ConsumerColors.TextOnDarkSecondary,
            ConsumerColors.VipGold,
            ConsumerColors.PrimaryGradientStart,
            ConsumerColors.PrimaryGradientEnd,
            ConsumerColors.HeroGradientStart,
            ConsumerColors.HeroGradientMiddle,
            ConsumerColors.HeroGradientEnd,
            ConsumerColors.DarkSapphire,
            ConsumerColors.DarkSapphireDeep,
            ConsumerColors.DarkSurface,
            ConsumerColors.DarkSurfaceSoft,
            ConsumerColors.DarkBackground,
            ConsumerColors.DarkText,
            ConsumerColors.DarkMuted,
            ConsumerColors.DarkBorder,
            CreatorColors.Primary,
            CreatorColors.PrimaryLight,
            CreatorColors.PrimaryDark,
            CreatorColors.Secondary,
            CreatorColors.SecondaryLight,
            CreatorColors.Accent,
            CreatorColors.LightBackground,
            CreatorColors.LightSurface,
            CreatorColors.Elevated,
            CreatorColors.Navigation,
            CreatorColors.Text,
            CreatorColors.TextMuted,
            CreatorColors.Border,
            CreatorColors.DarkBackground,
            CreatorColors.DarkSurface,
            CreatorColors.DarkElevated,
            CreatorColors.DarkText,
            CreatorColors.DarkMuted,
            CreatorColors.DarkBorder,
            CommonColors.Success,
            CommonColors.Warning,
            CommonColors.Error,
            CommonColors.Info,
            CommonColors.LogoBlue,
        )

        val renderedArgb = palette.map { it.toArgb() }
        assertEquals(palette.size, renderedArgb.size)
        assertEquals(0xFF10262E.toInt(), ConsumerColors.Text.toArgb())
        assertEquals(0xFF0B7C86.toInt(), ConsumerColors.Sapphire.toArgb())
    }
}
