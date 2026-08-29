package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.theme.ConsumerColors

/** Premium cloud + live-wave VoiceCloud mark used throughout the approved light design. */
@Composable
fun VoiceCloudBrandMark(size: Dp = 58.dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val u = this.size.minDimension / 100f
            val teal = ConsumerColors.Sapphire
            val gold = ConsumerColors.VipGold
            val stroke = 5.2f * u
            val cloudStroke = Stroke(stroke, cap = StrokeCap.Round)

            // Cloud silhouette, intentionally open at the bottom centre so the waveform reads as the hero.
            drawArc(
                color = teal,
                startAngle = 155f,
                sweepAngle = 205f,
                useCenter = false,
                topLeft = Offset(8 * u, 28 * u),
                size = Size(44 * u, 44 * u),
                style = cloudStroke,
            )
            drawArc(
                color = teal,
                startAngle = 198f,
                sweepAngle = 205f,
                useCenter = false,
                topLeft = Offset(29 * u, 13 * u),
                size = Size(48 * u, 48 * u),
                style = cloudStroke,
            )
            drawArc(
                color = teal,
                startAngle = 250f,
                sweepAngle = 170f,
                useCenter = false,
                topLeft = Offset(61 * u, 31 * u),
                size = Size(32 * u, 32 * u),
                style = cloudStroke,
            )
            drawLine(color = teal, start = Offset(19 * u, 70 * u), end = Offset(34 * u, 70 * u), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = teal, start = Offset(67 * u, 70 * u), end = Offset(82 * u, 70 * u), strokeWidth = stroke, cap = StrokeCap.Round)

            val xs = listOf(39f, 46f, 53f, 60f)
            val halves = listOf(10f, 18f, 24f, 13f)
            xs.zip(halves).forEach { (x, h) ->
                drawLine(color = teal, start = Offset(x * u, (70 - h) * u), end = Offset(x * u, (70 + h) * u), strokeWidth = 4.4f * u, cap = StrokeCap.Round)
            }
            drawCircle(color = gold, radius = 4.2f * u, center = Offset(79 * u, 25 * u))
        }
    }
}
