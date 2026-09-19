package app.voicecloud.core.designsystem.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.R as GoogleFontsR

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = GoogleFontsR.array.com_google_android_gms_fonts_certs,
)

private val outfit = GoogleFont("Outfit")
private val inter = GoogleFont("Inter")

val VoiceCloudDisplayFont = FontFamily(
    Font(googleFont = outfit, fontProvider = googleFontProvider, weight = FontWeight.Light),
    Font(googleFont = outfit, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = outfit, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = outfit, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = outfit, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = outfit, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
)

val VoiceCloudBodyFont = FontFamily(
    Font(googleFont = inter, fontProvider = googleFontProvider, weight = FontWeight.Light),
    Font(googleFont = inter, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = inter, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = inter, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = inter, fontProvider = googleFontProvider, weight = FontWeight.Bold),
)
