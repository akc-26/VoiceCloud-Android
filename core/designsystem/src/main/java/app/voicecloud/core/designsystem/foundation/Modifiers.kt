package app.voicecloud.core.designsystem.foundation

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud

@Composable
internal fun Modifier.vcTouchTarget(): Modifier {
    val touch = VoiceCloud.spacing.touch
    return defaultMinSize(minWidth = touch, minHeight = touch)
}
