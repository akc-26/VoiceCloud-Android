package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.theme.CommonColors

@Composable
fun VoiceCloudBrandMark(size: Dp = 58.dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(RoundedCornerShape(size * .28f)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(CommonColors.LogoBlue)
            val c = center
            val unit = this.size.minDimension / 64f
            drawCircle(Color.White, 5f * unit, c)
            fun arc(radius: Float, sweep: Float, start: Float) = drawArc(Color.White, start, sweep, false, topLeft = Offset(c.x-radius,c.y-radius), size = androidx.compose.ui.geometry.Size(radius*2,radius*2), style = Stroke(4f*unit, cap=StrokeCap.Round))
            arc(11f*unit, 84f, 138f); arc(11f*unit, 84f, -42f)
            arc(20f*unit, 76f, 142f); arc(20f*unit, 76f, -38f)
        }
    }
}
