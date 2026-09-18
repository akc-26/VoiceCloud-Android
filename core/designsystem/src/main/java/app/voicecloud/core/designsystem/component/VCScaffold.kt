package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud

@Composable
fun VCScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.Center,
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = VoiceCloud.colors
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}
