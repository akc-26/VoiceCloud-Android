package app.voicecloud.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import app.voicecloud.core.designsystem.BuildConfig

/** White-label aliases generated from /branding/voicecloud-brand.properties. */
object ConsumerColors {
    val Sapphire = VoiceCloudBrand.color(BuildConfig.CONSUMER_SAPPHIRE)
    val SapphireDeep = VoiceCloudBrand.color(BuildConfig.CONSUMER_SAPPHIREDEEP)
    val SapphireSoft = VoiceCloudBrand.color(BuildConfig.CONSUMER_SAPPHIRESOFT)
    val Ice = VoiceCloudBrand.color(BuildConfig.CONSUMER_ICE)
    val Indigo = VoiceCloudBrand.color(BuildConfig.CONSUMER_INDIGO)
    val Violet = VoiceCloudBrand.color(BuildConfig.CONSUMER_VIOLET)
    val VioletDeep = VoiceCloudBrand.color(BuildConfig.CONSUMER_VIOLETDEEP)
    val Lavender = VoiceCloudBrand.color(BuildConfig.CONSUMER_LAVENDER)
    val LavenderStrong = VoiceCloudBrand.color(BuildConfig.CONSUMER_LAVENDERSTRONG)
    val Sky = VoiceCloudBrand.color(BuildConfig.CONSUMER_SKY)
    val Cloud = VoiceCloudBrand.color(BuildConfig.CONSUMER_CLOUD)
    val Surface = VoiceCloudBrand.color(BuildConfig.CONSUMER_SURFACE)
    val SurfaceSoft = VoiceCloudBrand.color(BuildConfig.CONSUMER_SURFACESOFT)
    val Ink = VoiceCloudBrand.color(BuildConfig.CONSUMER_INK)
    val Text = VoiceCloudBrand.color(BuildConfig.CONSUMER_TEXT)
    val TextMuted = VoiceCloudBrand.color(BuildConfig.CONSUMER_TEXTMUTED)
    val Border = VoiceCloudBrand.color(BuildConfig.CONSUMER_BORDER)
    val DeepNavy = VoiceCloudBrand.color(BuildConfig.CONSUMER_DEEPNAVY)
    val Navy = VoiceCloudBrand.color(BuildConfig.CONSUMER_NAVY)
    val LiveSurface = VoiceCloudBrand.color(BuildConfig.CONSUMER_LIVESURFACE)
    val LiveSurfaceElevated = VoiceCloudBrand.color(BuildConfig.CONSUMER_LIVESURFACEELEVATED)
    val TextOnDark = VoiceCloudBrand.color(BuildConfig.CONSUMER_TEXTONDARK)
    val TextOnDarkSecondary = VoiceCloudBrand.color(BuildConfig.CONSUMER_TEXTONDARKSECONDARY)
    val VipGold = VoiceCloudBrand.color(BuildConfig.CONSUMER_VIPGOLD)
    val PrimaryGradientStart = VoiceCloudBrand.color(BuildConfig.CONSUMER_PRIMARYGRADIENTSTART)
    val PrimaryGradientEnd = VoiceCloudBrand.color(BuildConfig.CONSUMER_PRIMARYGRADIENTEND)
    val HeroGradientStart = VoiceCloudBrand.color(BuildConfig.CONSUMER_HEROGRADIENTSTART)
    val HeroGradientMiddle = VoiceCloudBrand.color(BuildConfig.CONSUMER_HEROGRADIENTMIDDLE)
    val HeroGradientEnd = VoiceCloudBrand.color(BuildConfig.CONSUMER_HEROGRADIENTEND)
    val DarkSapphire = VoiceCloudBrand.color(BuildConfig.CONSUMER_DARKSAPPHIRE)
    val DarkSapphireDeep = VoiceCloudBrand.color(BuildConfig.CONSUMER_DARKSAPPHIREDEEP)
    val DarkSurface = VoiceCloudBrand.color(BuildConfig.CONSUMER_DARKSURFACE)
    val DarkSurfaceSoft = VoiceCloudBrand.color(BuildConfig.CONSUMER_DARKSURFACESOFT)
    val DarkBackground = VoiceCloudBrand.color(BuildConfig.CONSUMER_DARKBACKGROUND)
    val DarkText = VoiceCloudBrand.color(BuildConfig.CONSUMER_DARKTEXT)
    val DarkMuted = VoiceCloudBrand.color(BuildConfig.CONSUMER_DARKMUTED)
    val DarkBorder = VoiceCloudBrand.color(BuildConfig.CONSUMER_DARKBORDER)
}

object ConsumerBrushes {
    val Primary: Brush get() = Brush.linearGradient(listOf(ConsumerColors.PrimaryGradientStart, ConsumerColors.PrimaryGradientEnd))
    val Hero: Brush get() = Brush.linearGradient(listOf(ConsumerColors.HeroGradientStart, ConsumerColors.HeroGradientMiddle, ConsumerColors.HeroGradientEnd))
}

object CreatorColors {
    val Primary = VoiceCloudBrand.color(BuildConfig.CREATOR_PRIMARY)
    val PrimaryLight = VoiceCloudBrand.color(BuildConfig.CREATOR_PRIMARYLIGHT)
    val PrimaryDark = VoiceCloudBrand.color(BuildConfig.CREATOR_PRIMARYDARK)
    val Secondary = VoiceCloudBrand.color(BuildConfig.CREATOR_SECONDARY)
    val SecondaryLight = VoiceCloudBrand.color(BuildConfig.CREATOR_SECONDARYLIGHT)
    val Accent = VoiceCloudBrand.color(BuildConfig.CREATOR_ACCENT)
    val LightBackground = VoiceCloudBrand.color(BuildConfig.CREATOR_LIGHTBACKGROUND)
    val LightSurface = VoiceCloudBrand.color(BuildConfig.CREATOR_LIGHTSURFACE)
    val Elevated = VoiceCloudBrand.color(BuildConfig.CREATOR_ELEVATED)
    val Navigation = VoiceCloudBrand.color(BuildConfig.CREATOR_NAVIGATION)
    val Text = VoiceCloudBrand.color(BuildConfig.CREATOR_TEXT)
    val TextMuted = VoiceCloudBrand.color(BuildConfig.CREATOR_TEXTMUTED)
    val Border = VoiceCloudBrand.color(BuildConfig.CREATOR_BORDER)
    val DarkBackground = VoiceCloudBrand.color(BuildConfig.CREATOR_DARKBACKGROUND)
    val DarkSurface = VoiceCloudBrand.color(BuildConfig.CREATOR_DARKSURFACE)
    val DarkElevated = VoiceCloudBrand.color(BuildConfig.CREATOR_DARKELEVATED)
    val DarkText = VoiceCloudBrand.color(BuildConfig.CREATOR_DARKTEXT)
    val DarkMuted = VoiceCloudBrand.color(BuildConfig.CREATOR_DARKMUTED)
    val DarkBorder = VoiceCloudBrand.color(BuildConfig.CREATOR_DARKBORDER)
}

object CommonColors {
    val Success = VoiceCloudBrand.color(BuildConfig.COMMON_SUCCESS)
    val Warning = VoiceCloudBrand.color(BuildConfig.COMMON_WARNING)
    val Error = VoiceCloudBrand.color(BuildConfig.COMMON_ERROR)
    val Info = VoiceCloudBrand.color(BuildConfig.COMMON_INFO)
    val LogoBlue = VoiceCloudBrand.color(BuildConfig.COMMON_LOGO)
}
