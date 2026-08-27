package app.voicecloud.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Exact Android translation of finalized R06 Website presentation authority. */
object ConsumerColors {
    // Website premium light palette.
    val Sapphire = Color(0xFF0B7C86)
    val SapphireDeep = Color(0xFF075762)
    val SapphireSoft = Color(0xFFDDF2F4)
    val Ice = Color(0xFF22C7CF)
    val Indigo = Color(0xFF4352D4)
    val Violet = Color(0xFF5A49DF)
    val VioletDeep = Color(0xFF4033B6)
    val Lavender = Color(0xFFECEAFC)
    val LavenderStrong = Color(0xFFDDD9FA)
    val Sky = Color(0xFFD8EEF0)
    val Cloud = Color(0xFFF1F7F8)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSoft = Color(0xFFE8F4F5)
    val Ink = Color(0xFF071820)
    val Text = Color(0xFF10262E)
    val TextMuted = Color(0xFF50666E)
    val Border = Color(0xFFA9CED2)
    val DeepNavy = Color(0xFF081225)
    val Navy = Color(0xFF0F1D3A)
    val LiveSurface = Color(0xFF132750)
    val LiveSurfaceElevated = Color(0xFF1A3468)
    val TextOnDark = Color(0xFFF7FAFF)
    val TextOnDarkSecondary = Color(0xFFB7C5E1)
    val VipGold = Color(0xFFF6C85F)

    // Website presentation gradients. These intentionally do not use Indigo.
    val PrimaryGradientStart = Color(0xFF087E8A)
    val PrimaryGradientEnd = Color(0xFF075F70)
    val HeroGradientStart = Color(0xFFF5FCFD)
    val HeroGradientMiddle = Color(0xFFECF9FA)
    val HeroGradientEnd = Color(0xFFDEF5F7)

    val DarkSapphire = Color(0xFF20E0E5)
    val DarkSapphireDeep = Color(0xFF56EDF0)
    val DarkSurface = Color(0xFF07141D)
    val DarkSurfaceSoft = Color(0xFF0A1D27)
    val DarkBackground = Color(0xFF020910)
    val DarkText = Color(0xFFDDECEF)
    val DarkMuted = Color(0xFF8FA8B2)
    val DarkBorder = Color(0xFF163946)
}

object ConsumerBrushes {
    val Primary: Brush
        get() = Brush.linearGradient(listOf(ConsumerColors.PrimaryGradientStart, ConsumerColors.PrimaryGradientEnd))
    val Hero: Brush
        get() = Brush.linearGradient(listOf(ConsumerColors.HeroGradientStart, ConsumerColors.HeroGradientMiddle, ConsumerColors.HeroGradientEnd))
}

object CreatorColors {
    val Primary = Color(0xFF22C55E)
    val PrimaryLight = Color(0xFF4ADE80)
    val PrimaryDark = Color(0xFF15803D)
    val Secondary = Color(0xFF0F766E)
    val SecondaryLight = Color(0xFF2DD4BF)
    val Accent = Color(0xFF5EEAD4)
    val LightBackground = Color(0xFFE7ECEB)
    val LightSurface = Color(0xFFF3F7F5)
    val Elevated = Color(0xFFFFFFFF)
    val Navigation = Color(0xFF123A32)
    val Text = Color(0xFF10231F)
    val TextMuted = Color(0xFF64756F)
    val Border = Color(0xFFD8E3DE)
    val DarkBackground = Color(0xFF0B1512)
    val DarkSurface = Color(0xFF12211C)
    val DarkElevated = Color(0xFF192B24)
    val DarkText = Color(0xFFF3FAF6)
    val DarkMuted = Color(0xFFA7BBB2)
    val DarkBorder = Color(0xFF2B453B)
}

object CommonColors {
    val Success = Color(0xFF16A34A)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF38BDF8)
    val LogoBlue = Color(0xFF2563EB)
}
