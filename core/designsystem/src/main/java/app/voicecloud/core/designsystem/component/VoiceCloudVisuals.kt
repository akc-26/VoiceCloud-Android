package app.voicecloud.core.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.theme.CommonColors
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.CreatorColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class VoiceCloudVisualKind {
    AUDIO, DISCOVER, SEARCH, COMMUNITY, EVENT, MESSAGE, NOTIFICATION,
    PROFILE, REPLAY, ACTIVITY, WALLET, GIFT, VIP, REWARD,
    SETTINGS, SECURITY, HELP, CREATOR, ANALYTICS, AUDIENCE,
    SCHEDULE, LIVE, PAYOUT, VERIFICATION
}

fun voiceCloudVisualFor(title: String): VoiceCloudVisualKind {
    val value = title.lowercase()
    return when {
        "wallet" in value || "transaction" in value || "credit" in value -> VoiceCloudVisualKind.WALLET
        "gift" in value || "store" in value -> VoiceCloudVisualKind.GIFT
        "vip" in value || "membership" in value -> VoiceCloudVisualKind.VIP
        "reward" in value || "task" in value || "achievement" in value -> VoiceCloudVisualKind.REWARD
        "analytic" in value || "earning" in value || "performance" in value -> VoiceCloudVisualKind.ANALYTICS
        "payout" in value || "withdraw" in value -> VoiceCloudVisualKind.PAYOUT
        "audience" in value || "follower" in value || "subscriber" in value || "people" in value || "friend" in value -> VoiceCloudVisualKind.AUDIENCE
        "creator" in value || "host" in value || "studio" in value -> VoiceCloudVisualKind.CREATOR
        "schedule" in value || "calendar" in value -> VoiceCloudVisualKind.SCHEDULE
        "room" in value || "live" in value || "audio" in value || "voice" in value -> VoiceCloudVisualKind.LIVE
        "community" in value -> VoiceCloudVisualKind.COMMUNITY
        "event" in value -> VoiceCloudVisualKind.EVENT
        "message" in value || "conversation" in value || "chat" in value -> VoiceCloudVisualKind.MESSAGE
        "notification" in value || "alert" in value -> VoiceCloudVisualKind.NOTIFICATION
        "profile" in value || "visitor" in value -> VoiceCloudVisualKind.PROFILE
        "replay" in value -> VoiceCloudVisualKind.REPLAY
        "activity" in value || "history" in value -> VoiceCloudVisualKind.ACTIVITY
        "search" in value || "explore" in value || "discover" in value -> VoiceCloudVisualKind.SEARCH
        "security" in value || "device" in value || "session" in value || "privacy" in value -> VoiceCloudVisualKind.SECURITY
        "verification" in value -> VoiceCloudVisualKind.VERIFICATION
        "setting" in value || "appearance" in value -> VoiceCloudVisualKind.SETTINGS
        "help" in value || "support" in value || "about" in value || "content" in value -> VoiceCloudVisualKind.HELP
        else -> VoiceCloudVisualKind.AUDIO
    }
}

@Composable
fun VoiceCloudGlossCard(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    contentPadding: Dp = 18.dp,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    val surface = if (dark) ConsumerColors.LiveSurfaceElevated else ConsumerColors.Surface
    val edge = if (dark) ConsumerColors.Ice.copy(alpha = .16f) else ConsumerColors.Border.copy(alpha = .88f)
    Card(
        modifier = modifier
            .animateContentSize()
            .shadow(if (dark) 5.dp else 8.dp, shape, ambientColor = ConsumerColors.DeepNavy.copy(alpha = .09f), spotColor = ConsumerColors.DeepNavy.copy(alpha = .06f))
            .border(1.dp, edge, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().background(
                if (dark) Brush.linearGradient(listOf(ConsumerColors.LiveSurfaceElevated, ConsumerColors.DeepNavy.copy(alpha = .85f)))
                else Brush.linearGradient(listOf(Color.White, ConsumerColors.Surface, ConsumerColors.SurfaceSoft.copy(alpha = .42f)))
            )
        ) {
            Column(Modifier.fillMaxWidth().padding(contentPadding), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
        }
    }
}

@Composable
fun VoiceCloudPictogram(
    kind: VoiceCloudVisualKind,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    dark: Boolean = false,
    accent: Color = if (dark) ConsumerColors.Ice else ConsumerColors.Sapphire,
) {
    val background = if (dark) ConsumerColors.LiveSurfaceElevated else ConsumerColors.SapphireSoft.copy(alpha = .72f)
    val gold = if (kind == VoiceCloudVisualKind.VIP || kind == VoiceCloudVisualKind.GIFT || kind == VoiceCloudVisualKind.REWARD) ConsumerColors.VipGold else accent.copy(alpha = .72f)
    Box(
        modifier.size(size).clip(RoundedCornerShape(size * .28f))
            .background(background)
            .border(1.dp, accent.copy(alpha = if (dark) .22f else .14f), RoundedCornerShape(size * .28f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size * .66f)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = (w * .065f).coerceAtLeast(2f)
            val thin = stroke * .72f
            fun line(a: Offset, b: Offset, color: Color = accent, width: Float = stroke) = drawLine(color, a, b, width, StrokeCap.Round)
            fun ring(center: Offset, radius: Float, color: Color = accent, width: Float = stroke) = drawCircle(color, radius, center, style = Stroke(width))
            when (kind) {
                VoiceCloudVisualKind.AUDIO, VoiceCloudVisualKind.LIVE, VoiceCloudVisualKind.REPLAY -> {
                    repeat(5) { i ->
                        val x = w * (.18f + i * .16f)
                        val amp = if (i == 2) .38f else if (i == 1 || i == 3) .28f else .18f
                        line(Offset(x, h * (.5f - amp)), Offset(x, h * (.5f + amp)), if (i == 2) gold else accent, if (i == 2) stroke * 1.15f else thin)
                    }
                    if (kind == VoiceCloudVisualKind.LIVE) ring(Offset(w * .5f, h * .5f), w * .43f, gold.copy(alpha = .82f), thin)
                    if (kind == VoiceCloudVisualKind.REPLAY) {
                        val p = Path().apply { moveTo(w*.46f,h*.34f); lineTo(w*.70f,h*.5f); lineTo(w*.46f,h*.66f); close() }
                        drawPath(p, gold)
                    }
                }
                VoiceCloudVisualKind.SEARCH, VoiceCloudVisualKind.DISCOVER -> {
                    ring(Offset(w*.43f,h*.43f), w*.25f)
                    line(Offset(w*.60f,h*.60f), Offset(w*.82f,h*.82f), gold)
                    repeat(3) { i -> ring(Offset(w*(.2f+i*.3f),h*.84f), w*.035f, accent, thin) }
                }
                VoiceCloudVisualKind.COMMUNITY, VoiceCloudVisualKind.AUDIENCE -> {
                    ring(Offset(w*.5f,h*.30f), w*.13f, gold)
                    ring(Offset(w*.24f,h*.45f), w*.10f, accent, thin)
                    ring(Offset(w*.76f,h*.45f), w*.10f, accent, thin)
                    drawArc(color = accent, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(w*.28f,h*.43f), size = Size(w*.44f,h*.42f), style = Stroke(stroke))
                    drawArc(color = accent.copy(alpha=.8f), startAngle = 205f, sweepAngle = 130f, useCenter = false, topLeft = Offset(w*.05f,h*.56f), size = Size(w*.32f,h*.28f), style = Stroke(thin))
                    drawArc(color = accent.copy(alpha=.8f), startAngle = 205f, sweepAngle = 130f, useCenter = false, topLeft = Offset(w*.63f,h*.56f), size = Size(w*.32f,h*.28f), style = Stroke(thin))
                }
                VoiceCloudVisualKind.EVENT, VoiceCloudVisualKind.SCHEDULE -> {
                    drawRoundRect(color = accent.copy(alpha=.12f), topLeft = Offset(w*.15f,h*.22f), size = Size(w*.70f,h*.60f), cornerRadius = CornerRadius(w*.08f,w*.08f))
                    drawRoundRect(color = accent, topLeft = Offset(w*.15f,h*.22f), size = Size(w*.70f,h*.60f), cornerRadius = CornerRadius(w*.08f,w*.08f), style=Stroke(stroke))
                    line(Offset(w*.15f,h*.38f), Offset(w*.85f,h*.38f), gold)
                    line(Offset(w*.32f,h*.12f), Offset(w*.32f,h*.30f), accent)
                    line(Offset(w*.68f,h*.12f), Offset(w*.68f,h*.30f), accent)
                    repeat(3){ i -> ring(Offset(w*(.32f+i*.18f),h*.57f), w*.035f, if(i==1) gold else accent, thin) }
                }
                VoiceCloudVisualKind.MESSAGE, VoiceCloudVisualKind.NOTIFICATION -> {
                    if (kind == VoiceCloudVisualKind.MESSAGE) {
                        drawRoundRect(color = accent, topLeft = Offset(w*.12f,h*.20f), size = Size(w*.76f,h*.52f), cornerRadius = CornerRadius(w*.12f,w*.12f), style=Stroke(stroke))
                        val p=Path().apply{moveTo(w*.32f,h*.71f);lineTo(w*.25f,h*.88f);lineTo(w*.48f,h*.72f);close()}; drawPath(p, gold)
                        repeat(3){i->drawCircle(if(i==1) gold else accent,w*.035f,Offset(w*(.35f+i*.15f),h*.47f))}
                    } else {
                        drawArc(color = accent, startAngle = 195f, sweepAngle = 150f, useCenter = false, topLeft = Offset(w*.25f,h*.20f), size = Size(w*.5f,h*.5f), style = Stroke(stroke))
                        line(Offset(w*.28f,h*.58f),Offset(w*.72f,h*.58f),accent)
                        ring(Offset(w*.5f,h*.72f),w*.055f,gold,thin)
                    }
                }
                VoiceCloudVisualKind.PROFILE, VoiceCloudVisualKind.CREATOR, VoiceCloudVisualKind.VERIFICATION -> {
                    ring(Offset(w*.5f,h*.30f), w*.16f, if(kind==VoiceCloudVisualKind.CREATOR) gold else accent)
                    drawArc(color = accent, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(w*.18f,h*.45f), size = Size(w*.64f,h*.42f), style = Stroke(stroke))
                    if (kind == VoiceCloudVisualKind.CREATOR) {
                        val p=Path().apply{moveTo(w*.30f,h*.12f);lineTo(w*.40f,h*.22f);lineTo(w*.50f,h*.10f);lineTo(w*.60f,h*.22f);lineTo(w*.70f,h*.12f);lineTo(w*.66f,h*.28f);lineTo(w*.34f,h*.28f);close()}; drawPath(p,gold.copy(alpha=.9f))
                    }
                    if (kind == VoiceCloudVisualKind.VERIFICATION) {
                        line(Offset(w*.65f,h*.65f),Offset(w*.73f,h*.74f),gold)
                        line(Offset(w*.73f,h*.74f),Offset(w*.88f,h*.55f),gold)
                    }
                }
                VoiceCloudVisualKind.WALLET, VoiceCloudVisualKind.PAYOUT -> {
                    drawRoundRect(color = accent, topLeft = Offset(w*.10f,h*.26f), size = Size(w*.80f,h*.52f), cornerRadius = CornerRadius(w*.10f,w*.10f), style=Stroke(stroke))
                    drawRoundRect(color = gold.copy(alpha=.16f), topLeft = Offset(w*.53f,h*.40f), size = Size(w*.33f,h*.22f), cornerRadius = CornerRadius(w*.07f,w*.07f))
                    ring(Offset(w*.70f,h*.51f),w*.045f,gold,thin)
                    if(kind==VoiceCloudVisualKind.PAYOUT){ line(Offset(w*.50f,h*.12f),Offset(w*.50f,h*.35f),gold); line(Offset(w*.38f,h*.24f),Offset(w*.50f,h*.35f),gold); line(Offset(w*.62f,h*.24f),Offset(w*.50f,h*.35f),gold) }
                }
                VoiceCloudVisualKind.GIFT, VoiceCloudVisualKind.REWARD, VoiceCloudVisualKind.VIP -> {
                    if(kind==VoiceCloudVisualKind.VIP){
                        val p=Path().apply{moveTo(w*.12f,h*.38f);lineTo(w*.27f,h*.22f);lineTo(w*.43f,h*.39f);lineTo(w*.56f,h*.18f);lineTo(w*.73f,h*.39f);lineTo(w*.88f,h*.22f);lineTo(w*.80f,h*.70f);lineTo(w*.20f,h*.70f);close()}; drawPath(p,gold); line(Offset(w*.25f,h*.78f),Offset(w*.75f,h*.78f),accent)
                    } else {
                        drawRoundRect(color = if(kind==VoiceCloudVisualKind.GIFT) gold.copy(alpha=.18f) else accent.copy(alpha=.12f), topLeft = Offset(w*.20f,h*.38f), size = Size(w*.60f,h*.46f), cornerRadius = CornerRadius(w*.05f,w*.05f))
                        drawRoundRect(color = accent, topLeft = Offset(w*.20f,h*.38f), size = Size(w*.60f,h*.46f), cornerRadius = CornerRadius(w*.05f,w*.05f), style=Stroke(stroke))
                        line(Offset(w*.50f,h*.38f),Offset(w*.50f,h*.84f),gold)
                        line(Offset(w*.18f,h*.52f),Offset(w*.82f,h*.52f),gold)
                        drawArc(color = gold, startAngle = 190f, sweepAngle = 155f, useCenter = false, topLeft = Offset(w*.27f,h*.12f), size = Size(w*.23f,h*.30f), style = Stroke(thin))
                        drawArc(color = gold, startAngle = 195f, sweepAngle = 150f, useCenter = false, topLeft = Offset(w*.50f,h*.12f), size = Size(w*.23f,h*.30f), style = Stroke(thin))
                    }
                }
                VoiceCloudVisualKind.SETTINGS -> {
                    ring(Offset(w*.5f,h*.5f),w*.18f,gold,stroke)
                    ring(Offset(w*.5f,h*.5f),w*.08f,accent,stroke)
                    repeat(8){i-> val a=(i*PI/4).toFloat(); line(Offset(w*.5f+cos(a)*w*.27f,h*.5f+sin(a)*w*.27f),Offset(w*.5f+cos(a)*w*.38f,h*.5f+sin(a)*w*.38f),accent,thin)}
                }
                VoiceCloudVisualKind.SECURITY -> {
                    val p=Path().apply{moveTo(w*.5f,h*.08f);lineTo(w*.82f,h*.20f);lineTo(w*.77f,h*.58f);quadraticTo(w*.68f,h*.80f,w*.5f,h*.90f);quadraticTo(w*.32f,h*.80f,w*.23f,h*.58f);lineTo(w*.18f,h*.20f);close()}; drawPath(p,accent.copy(alpha=.12f)); drawPath(p,accent,style=Stroke(stroke)); ring(Offset(w*.5f,h*.47f),w*.08f,gold,thin); line(Offset(w*.5f,h*.55f),Offset(w*.5f,h*.67f),gold,thin)
                }
                VoiceCloudVisualKind.HELP -> {
                    ring(Offset(w*.5f,h*.5f),w*.36f,accent,stroke)
                    drawArc(color = gold, startAngle = 195f, sweepAngle = 220f, useCenter = false, topLeft = Offset(w*.36f,h*.22f), size = Size(w*.28f,h*.31f), style = Stroke(stroke))
                    line(Offset(w*.50f,h*.51f),Offset(w*.50f,h*.62f),gold,stroke)
                    drawCircle(gold,w*.035f,Offset(w*.50f,h*.75f))
                }
                VoiceCloudVisualKind.ANALYTICS, VoiceCloudVisualKind.ACTIVITY -> {
                    val points=listOf(Offset(w*.10f,h*.72f),Offset(w*.28f,h*.57f),Offset(w*.43f,h*.64f),Offset(w*.61f,h*.35f),Offset(w*.78f,h*.43f),Offset(w*.90f,h*.20f))
                    points.zipWithNext().forEach { (a,b)-> line(a,b,accent,stroke) }
                    points.forEach{ drawCircle(gold,w*.035f,it) }
                    line(Offset(w*.10f,h*.86f),Offset(w*.90f,h*.86f),accent.copy(alpha=.45f),thin)
                }
            }
        }
    }
}

@Composable
fun VoiceCloudPosterArtwork(
    kind: VoiceCloudVisualKind,
    modifier: Modifier = Modifier,
    dark: Boolean = true,
) {
    val shape = RoundedCornerShape(22.dp)
    val background = if (dark) {
        Brush.linearGradient(listOf(ConsumerColors.DeepNavy, ConsumerColors.LiveSurface, ConsumerColors.SapphireDeep))
    } else {
        Brush.linearGradient(listOf(ConsumerColors.Surface, ConsumerColors.SapphireSoft, ConsumerColors.Lavender))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(1.dp, ConsumerColors.VipGold.copy(alpha = .48f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val ink = if (dark) ConsumerColors.Ice else ConsumerColors.SapphireDeep
            val gold = ConsumerColors.VipGold
            val soft = if (dark) Color.White else ConsumerColors.Sapphire
            val stroke = (w * .018f).coerceAtLeast(2f)
            val stars = listOf(.11f to .20f, .23f to .10f, .76f to .17f, .88f to .32f, .67f to .08f, .39f to .28f)
            stars.forEachIndexed { index, point ->
                drawCircle(
                    color = if (index % 3 == 0) gold.copy(alpha = .85f) else soft.copy(alpha = .45f),
                    radius = if (index % 2 == 0) 2.4.dp.toPx() else 1.5.dp.toPx(),
                    center = Offset(w * point.first, h * point.second),
                )
            }
            when (kind) {
                VoiceCloudVisualKind.AUDIO, VoiceCloudVisualKind.LIVE, VoiceCloudVisualKind.REPLAY -> {
                    val center = Offset(w * .67f, h * .50f)
                    repeat(3) { index ->
                        drawCircle(
                            color = ink.copy(alpha = .22f - index * .04f),
                            radius = w * (.16f + index * .055f),
                            center = center,
                            style = Stroke(width = (2.6f - index * .45f).dp.toPx()),
                        )
                    }
                    drawRoundRect(
                        color = gold,
                        topLeft = Offset(w * .61f, h * .24f),
                        size = Size(w * .12f, h * .38f),
                        cornerRadius = CornerRadius(w * .06f, w * .06f),
                    )
                    drawArc(
                        color = gold,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * .56f, h * .39f),
                        size = Size(w * .22f, h * .27f),
                        style = Stroke(width = stroke * 1.4f, cap = StrokeCap.Round),
                    )
                    drawLine(color = gold, start = Offset(w * .67f, h * .66f), end = Offset(w * .67f, h * .78f), strokeWidth = stroke * 1.35f, cap = StrokeCap.Round)
                    drawLine(color = gold, start = Offset(w * .61f, h * .79f), end = Offset(w * .73f, h * .79f), strokeWidth = stroke * 1.35f, cap = StrokeCap.Round)
                    repeat(7) { index ->
                        val x = w * (.08f + index * .055f)
                        val amp = h * (.08f + ((index * 3) % 5) * .018f)
                        drawLine(color = ink.copy(alpha = .72f), start = Offset(x, h*.53f-amp), end = Offset(x, h*.53f+amp), strokeWidth = stroke*.55f, cap = StrokeCap.Round)
                    }
                }
                VoiceCloudVisualKind.SCHEDULE, VoiceCloudVisualKind.EVENT -> {
                    drawCircle(color = gold.copy(alpha = .90f), radius = w*.10f, center = Offset(w*.72f,h*.30f))
                    val baseY = h*.70f
                    val mountain = Path().apply {
                        moveTo(0f, baseY)
                        lineTo(w*.23f,h*.42f)
                        lineTo(w*.41f,h*.64f)
                        lineTo(w*.61f,h*.35f)
                        lineTo(w*.88f,h*.64f)
                        lineTo(w, h*.48f)
                        lineTo(w,h)
                        lineTo(0f,h)
                        close()
                    }
                    drawPath(mountain, ink.copy(alpha = if (dark) .25f else .18f))
                    drawLine(color = gold.copy(alpha=.75f), start = Offset(w*.08f,baseY), end = Offset(w*.92f,baseY), strokeWidth = stroke)
                }
                VoiceCloudVisualKind.COMMUNITY, VoiceCloudVisualKind.AUDIENCE, VoiceCloudVisualKind.CREATOR, VoiceCloudVisualKind.PROFILE -> {
                    val centers = listOf(Offset(w*.38f,h*.48f),Offset(w*.62f,h*.43f),Offset(w*.51f,h*.62f))
                    centers.forEachIndexed { index, center ->
                        drawCircle(color = if(index==1) gold.copy(alpha=.88f) else ink.copy(alpha=.70f), radius = w*(.105f-index*.008f), center = center)
                        drawCircle(color = if(dark) ConsumerColors.DeepNavy else Color.White, radius = w*.066f, center = center)
                    }
                    drawArc(color = ink, startAngle = 195f, sweepAngle = 150f, useCenter = false, topLeft = Offset(w*.20f,h*.47f), size = Size(w*.60f,h*.42f), style = Stroke(width = stroke*1.2f, cap = StrokeCap.Round))
                }
                VoiceCloudVisualKind.WALLET, VoiceCloudVisualKind.GIFT, VoiceCloudVisualKind.VIP, VoiceCloudVisualKind.REWARD, VoiceCloudVisualKind.PAYOUT -> {
                    drawRoundRect(color = ink.copy(alpha=.20f), topLeft=Offset(w*.28f,h*.32f), size=Size(w*.47f,h*.38f), cornerRadius=CornerRadius(w*.06f,w*.06f))
                    drawRoundRect(color = ink, topLeft=Offset(w*.28f,h*.32f), size=Size(w*.47f,h*.38f), cornerRadius=CornerRadius(w*.06f,w*.06f), style=Stroke(width=stroke*1.2f))
                    repeat(4){index-> drawCircle(color=gold.copy(alpha=.95f-index*.09f), radius=w*(.075f-index*.008f), center=Offset(w*(.34f+index*.12f),h*(.29f-index*.03f)))}
                }
                else -> {
                    val moon = Offset(w*.72f,h*.28f)
                    drawCircle(color = gold.copy(alpha=.92f), radius=w*.09f, center=moon)
                    drawCircle(color = if(dark) ConsumerColors.LiveSurface else ConsumerColors.SapphireSoft, radius=w*.075f, center=Offset(moon.x+w*.035f,moon.y-w*.020f))
                    val path=Path().apply{moveTo(0f,h*.78f);lineTo(w*.20f,h*.55f);lineTo(w*.36f,h*.72f);lineTo(w*.58f,h*.46f);lineTo(w*.76f,h*.69f);lineTo(w,h*.52f);lineTo(w,h);lineTo(0f,h);close()}
                    drawPath(path, ink.copy(alpha=.25f))
                }
            }
        }
    }
}

@Composable
fun VoiceCloudPageHero(
    title: String,
    subtitle: String,
    kind: VoiceCloudVisualKind = voiceCloudVisualFor(title),
    modifier: Modifier = Modifier,
    badge: String? = null,
    dark: Boolean = false,
    creator: Boolean = false,
) {
    val shape = RoundedCornerShape(30.dp)
    val startColor = when { dark -> ConsumerColors.LiveSurface; creator -> CreatorColors.PrimaryDark; else -> ConsumerColors.Surface }
    val middleColor = when { dark -> ConsumerColors.LiveSurfaceElevated; creator -> CreatorColors.Primary; else -> ConsumerColors.SurfaceSoft }
    val endColor = when { dark -> ConsumerColors.DeepNavy; creator -> CreatorColors.PrimaryDark; else -> ConsumerColors.SapphireSoft.copy(alpha = .78f) }
    val titleColor = if (dark || creator) Color.White else ConsumerColors.Ink
    val bodyColor = if (dark || creator) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted

    Box(
        modifier.fillMaxWidth()
            .shadow(10.dp, shape, ambientColor = ConsumerColors.DeepNavy.copy(alpha=.10f), spotColor = ConsumerColors.DeepNavy.copy(alpha=.08f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(startColor, middleColor, endColor)))
            .border(1.dp, if (dark || creator) ConsumerColors.Ice.copy(alpha=.18f) else ConsumerColors.Border.copy(alpha=.82f), shape)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!badge.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (dark || creator) ConsumerColors.VipGold.copy(alpha=.17f) else ConsumerColors.VipGold.copy(alpha=.14f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ConsumerColors.VipGold.copy(alpha=.28f)),
                    ) {
                        Text(
                            voiceCloudTitleCase(badge),
                            Modifier.padding(horizontal=10.dp, vertical=5.dp),
                            color=if(dark||creator) ConsumerColors.VipGold else ConsumerColors.VioletDeep,
                            style=MaterialTheme.typography.labelSmall,
                            fontWeight=FontWeight.ExtraBold,
                            maxLines = 1,
                        )
                    }
                }
                Text(
                    voiceCloudTitleCase(title),
                    style=MaterialTheme.typography.headlineSmall,
                    color=titleColor,
                    fontWeight=FontWeight.ExtraBold,
                    maxLines=2,
                    overflow=TextOverflow.Ellipsis,
                )
                Text(
                    voiceCloudTitleCase(subtitle),
                    style=MaterialTheme.typography.bodyMedium,
                    color=bodyColor,
                    maxLines=4,
                    overflow=TextOverflow.Ellipsis,
                )
            }
            Box(
                Modifier.size(width = 104.dp, height = 94.dp).clip(RoundedCornerShape(24.dp))
                    .border(1.dp, ConsumerColors.VipGold.copy(alpha=.30f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                VoiceCloudPosterArtwork(kind = kind, modifier = Modifier.fillMaxSize(), dark = dark || creator)
            }
        }
    }
}

@Composable
fun VoiceCloudMetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    kind: VoiceCloudVisualKind = VoiceCloudVisualKind.ANALYTICS,
    dark: Boolean = false,
) {
    VoiceCloudGlossCard(modifier, dark, contentPadding=14.dp) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            VoiceCloudPictogram(kind,size=38.dp,dark=dark)
            Column(Modifier.weight(1f)) {
                Text(value,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.ExtraBold,color=if(dark) Color.White else ConsumerColors.SapphireDeep,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(voiceCloudTitleCase(label),style=MaterialTheme.typography.labelMedium,color=if(dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted,maxLines=2,overflow=TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun VoiceCloudEmptyVisual(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    kind: VoiceCloudVisualKind = voiceCloudVisualFor(title),
    dark: Boolean = false,
) {
    VoiceCloudGlossCard(modifier, dark) {
        Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
            VoiceCloudPictogram(kind,size=64.dp,dark=dark)
            Text(voiceCloudTitleCase(title),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium,color=if(dark)Color.White else ConsumerColors.Ink)
            Text(voiceCloudTitleCase(body),style=MaterialTheme.typography.bodyMedium,color=if(dark)ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted)
        }
    }
}

@Composable
fun VoiceCloudAudioArtwork(
    modifier: Modifier = Modifier,
    dark: Boolean = true,
) {
    val base = if (dark) ConsumerColors.DeepNavy else ConsumerColors.SapphireSoft
    Box(modifier.clip(CircleShape).background(Brush.radialGradient(listOf(ConsumerColors.Ice.copy(alpha=.22f), base))).border(2.dp,ConsumerColors.VipGold.copy(alpha=.60f),CircleShape),contentAlignment=Alignment.Center) {
        Canvas(Modifier.size(92.dp)) {
            val c=Offset(size.width/2,size.height/2)
            repeat(3){i->drawCircle(ConsumerColors.Ice.copy(alpha=.30f-i*.07f),size.width*(.34f+i*.09f),c,style=Stroke((2-i*.35f).dp.toPx()))}
            drawRoundRect(color = ConsumerColors.VipGold, topLeft = Offset(size.width*.41f,size.height*.18f), size = Size(size.width*.18f,size.height*.46f), cornerRadius = CornerRadius(size.width*.09f,size.width*.09f))
            drawArc(color = ConsumerColors.VipGold, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(size.width*.30f,size.height*.35f), size = Size(size.width*.40f,size.height*.32f), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
            drawLine(ConsumerColors.VipGold,Offset(size.width*.50f,size.height*.66f),Offset(size.width*.50f,size.height*.78f),4.dp.toPx(),StrokeCap.Round)
            drawLine(ConsumerColors.VipGold,Offset(size.width*.37f,size.height*.79f),Offset(size.width*.63f,size.height*.79f),4.dp.toPx(),StrokeCap.Round)
        }
    }
}

@Composable
fun VoiceCloudMiniChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    val clean = values.ifEmpty { listOf(.2f,.38f,.31f,.55f,.48f,.72f,.66f,.88f) }
    val max = clean.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val lineColor = if(dark) ConsumerColors.Ice else ConsumerColors.Sapphire
    Canvas(modifier.fillMaxWidth().height(96.dp)) {
        val step = if(clean.size<=1) size.width else size.width/(clean.size-1)
        val pts=clean.mapIndexed{i,v->Offset(i*step,size.height-(v/max)*size.height*.78f-size.height*.10f)}
        pts.zipWithNext().forEach{(a,b)->drawLine(lineColor,a,b,3.dp.toPx(),StrokeCap.Round)}
        pts.forEach{drawCircle(ConsumerColors.VipGold,4.dp.toPx(),it)}
        drawLine(lineColor.copy(alpha=.16f),Offset(0f,size.height*.90f),Offset(size.width,size.height*.90f),1.dp.toPx())
    }
}
