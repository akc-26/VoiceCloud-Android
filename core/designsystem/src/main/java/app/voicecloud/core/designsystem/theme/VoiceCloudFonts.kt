package app.voicecloud.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.voicecloud.core.designsystem.R

/**
 * Approved PH14-R11 type families from the VoiceCloud Android design boards:
 * Playfair Display for display/headings, Nunito for body/labels/navigation.
 */
object VoiceCloudFonts {
    val Display: FontFamily = FontFamily(
        Font(R.font.playfair_display, FontWeight.Normal),
        Font(R.font.playfair_display, FontWeight.Medium),
        Font(R.font.playfair_display, FontWeight.SemiBold),
        Font(R.font.playfair_display, FontWeight.Bold),
        Font(R.font.playfair_display, FontWeight.ExtraBold),
        Font(R.font.playfair_display, FontWeight.Black),
    )
    val Sans: FontFamily = FontFamily(
        Font(R.font.nunito, FontWeight.Light),
        Font(R.font.nunito, FontWeight.Normal),
        Font(R.font.nunito, FontWeight.Medium),
        Font(R.font.nunito, FontWeight.SemiBold),
        Font(R.font.nunito, FontWeight.Bold),
        Font(R.font.nunito, FontWeight.ExtraBold),
        Font(R.font.nunito, FontWeight.Black),
    )
}
