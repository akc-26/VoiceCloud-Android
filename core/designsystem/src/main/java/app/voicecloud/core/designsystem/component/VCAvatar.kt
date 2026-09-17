package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.VoiceCloud

@Composable
fun VCAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = VoiceCloud.spacing.xxxl,
    image: Painter? = null,
    contentDescription: String? = name,
) {
    val colors = VoiceCloud.colors
    val initials = name.trim().split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
    Box(
        modifier = modifier
            .size(size)
            .clip(VoiceCloud.shapes.avatar)
            .background(colors.primaryContainer)
            .semantics { this.contentDescription = contentDescription ?: name },
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(image, contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        } else {
            Text(
                text = initials.ifBlank { "•" },
                style = VoiceCloud.typography.actionLabel,
                color = colors.primary,
            )
        }
    }
}

@Composable
fun VCSpeakingAvatar(
    name: String,
    speaking: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = VoiceCloud.spacing.xxxl,
    image: Painter? = null,
    muted: Boolean = false,
) {
    val colors = VoiceCloud.colors
    val ring = if (speaking) colors.accent else colors.outline
    val description = buildString {
        append(name)
        if (speaking) append(", speaking")
        if (muted) append(", muted")
    }
    Box(
        modifier = modifier.semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (speaking) {
            VCLivePulse(color = colors.accent, size = size + VoiceCloud.spacing.sm)
        }
        Box(
            Modifier
                .size(size + VoiceCloud.spacing.xxs)
                .border(2.dp, ring, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            VCAvatar(name = name, size = size, image = image, contentDescription = null)
        }
    }
}
