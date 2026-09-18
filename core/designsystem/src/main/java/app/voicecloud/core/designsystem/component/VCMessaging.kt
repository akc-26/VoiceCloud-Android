package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons

@Composable
fun VCMessageBubble(
    text: String,
    modifier: Modifier = Modifier,
    outgoing: Boolean = false,
    metaLabel: String? = null,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    val background = if (outgoing) colors.primary else colors.surfaceSoft
    val foreground = if (outgoing) colors.onPrimary else colors.textPrimary
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (outgoing) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = text,
            style = VoiceCloud.typography.body,
            color = foreground,
            modifier = Modifier
                .background(background, VoiceCloud.shapes.standard)
                .padding(horizontal = spacing.md, vertical = spacing.sm),
        )
        metaLabel?.let {
            Text(
                it,
                style = VoiceCloud.typography.caption,
                color = colors.textMuted,
                modifier = Modifier.padding(top = spacing.xxs, start = spacing.xs, end = spacing.xs),
            )
        }
    }
}

@Composable
fun VCComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Message",
    enabled: Boolean = true,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            .padding(horizontal = spacing.pageGutter, vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = VoiceCloud.typography.body.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = spacing.touch)
                .background(colors.surfaceSoft, VoiceCloud.shapes.pill)
                .padding(PaddingValues(horizontal = spacing.md, vertical = spacing.sm)),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, style = VoiceCloud.typography.bodySecondary, color = colors.textMuted)
                }
                inner()
            },
        )
        VCIconButton(
            icon = VoiceCloudIcons.Send,
            contentDescription = "Send message",
            onClick = onSend,
            enabled = enabled && value.isNotBlank(),
        )
    }
}
