package app.voicecloud.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import app.voicecloud.core.designsystem.BuildConfig

/**
 * Single runtime authority for white-label identity and visual tokens.
 * Values are generated from /branding/voicecloud-brand.properties.
 */
object VoiceCloudBrand {
    val name: String = BuildConfig.BRAND_NAME
    val slug: String = BuildConfig.BRAND_SLUG
    val legalName: String = BuildConfig.BRAND_LEGALNAME
    val tagline: String = BuildConfig.BRAND_TAGLINE
    val websiteTagline: String = BuildConfig.BRAND_WEBSITETAGLINE

    internal fun color(value: String): Color {
        val raw = value.removePrefix("#")
        val argb = when (raw.length) {
            6 -> (0xFF000000L or raw.toLong(16))
            8 -> raw.toLong(16)
            else -> error("Invalid white-label color: $value")
        }
        return Color(argb.toInt())
    }
}
