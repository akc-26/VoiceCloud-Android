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
    val hasTrailingAction = actions != null || (actionLabel != null && onAction != null)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ConsumerColors.Surface.copy(alpha = .995f),
        tonalElevation = 0.dp,
        shadowElevation = 5.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = metrics.topBarMinHeight)
                    .padding(horizontal = (metrics.horizontalPadding - 12.dp).coerceAtLeast(4.dp)),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .semantics { contentDescription = "Back" },
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

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = if (hasTrailingAction) 108.dp else 58.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    VoiceCloudPictogram(
                        kind = voiceCloudVisualFor(title),
                        size = 32.dp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = voiceCloudTitleCase(title),
                        style = MaterialTheme.typography.titleLarge,
                        color = ConsumerColors.Ink,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (hasTrailingAction) {
                    Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                        actions?.invoke(this)
                        if (actionLabel != null && onAction != null) {
                            TextButton(onClick = onAction, enabled = actionEnabled) {
                                Text(voiceCloudTitleCase(actionLabel), maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }

            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = voiceCloudTitleCase(subtitle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = metrics.horizontalPadding, end = metrics.horizontalPadding, bottom = metrics.contentTopSpacing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = metrics.horizontalPadding).background(ConsumerColors.VipGold.copy(alpha = .28f)))
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
