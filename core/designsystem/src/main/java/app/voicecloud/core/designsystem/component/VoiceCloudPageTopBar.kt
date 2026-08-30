package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.theme.VoiceCloudPageMetrics
import app.voicecloud.core.designsystem.theme.ConsumerColors

/**
 * Shared secondary-page chrome for the User portal.
 *
 * System insets are consumed once by the app root. This component owns the
 * adaptive navigation row, icon-only back affordance, centered title and
 * optional compact actions used across product pages.
 */
@Composable
fun VoiceCloudPageTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val metrics = VoiceCloudPageMetrics.current()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ConsumerColors.Surface.copy(alpha = .985f),
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(
                start = (metrics.horizontalPadding - 10.dp).coerceAtLeast(6.dp),
                end = (metrics.horizontalPadding - 10.dp).coerceAtLeast(6.dp),
                top = 5.dp,
                bottom = if (subtitle.isNullOrBlank()) 5.dp else 8.dp,
            )
        ) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = metrics.topBarMinHeight - 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.semantics { contentDescription = "Back" },
                ) {
                    val arrowColor = MaterialTheme.colorScheme.onSurface
                    Canvas(Modifier.size(22.dp)) {
                        val stroke = 2.2.dp.toPx()
                        val midY = size.height * 0.5f
                        val leftX = size.width * 0.24f
                        val rightX = size.width * 0.78f
                        drawLine(arrowColor, Offset(rightX, midY), Offset(leftX, midY), stroke, StrokeCap.Round)
                        drawLine(arrowColor, Offset(leftX, midY), Offset(size.width * 0.48f, size.height * 0.25f), stroke, StrokeCap.Round)
                        drawLine(arrowColor, Offset(leftX, midY), Offset(size.width * 0.48f, size.height * 0.75f), stroke, StrokeCap.Round)
                    }
                }
                Box(
                    Modifier.padding(start = 2.dp, end = 10.dp).size(width = 4.dp, height = 30.dp)
                        .background(ConsumerColors.Sapphire, RoundedCornerShape(50))
                )
                Column(Modifier.weight(1f), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = voiceCloudTitleCase(title),
                        style = MaterialTheme.typography.titleLarge,
                        color = ConsumerColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = voiceCloudTitleCase(subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (actions != null || (actionLabel != null && onAction != null)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        actions?.invoke(this)
                        if (actionLabel != null && onAction != null) {
                            TextButton(onClick = onAction, enabled = actionEnabled) {
                                Text(voiceCloudTitleCase(actionLabel), maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceCloudTopBarIconAction(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = tint,
        ),
        modifier = Modifier.size(42.dp),
    ) {
        Surface(color = Color.Transparent, shape = CircleShape) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.padding(9.dp).size(24.dp),
            )
        }
    }
}
