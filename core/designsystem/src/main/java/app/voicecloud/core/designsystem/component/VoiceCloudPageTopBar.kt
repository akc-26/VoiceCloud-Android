package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Shared secondary-page chrome for the User portal.
 *
 * The parent app owns system-window insets, while this component owns a stable
 * 64dp navigation row, a real icon-only back affordance, centered title, and
 * optional action/subtitle. Keeping this in the design system prevents every
 * feature from inventing its own header geometry.
 */
@Composable
fun VoiceCloudPageTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 8.dp),
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
                        drawLine(
                            color = arrowColor,
                            start = Offset(rightX, midY),
                            end = Offset(leftX, midY),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = arrowColor,
                            start = Offset(leftX, midY),
                            end = Offset(size.width * 0.48f, size.height * 0.25f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = arrowColor,
                            start = Offset(leftX, midY),
                            end = Offset(size.width * 0.48f, size.height * 0.75f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round,
                        )
                    }
                }

                Text(
                    text = title,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = if (actionLabel != null && onAction != null) 88.dp else 58.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (actionLabel != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        enabled = actionEnabled,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    ) {
                        Text(actionLabel, maxLines = 1, softWrap = false)
                    }
                }
            }

            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
