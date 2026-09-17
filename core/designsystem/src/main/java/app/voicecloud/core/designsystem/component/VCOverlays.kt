package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VCBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = VoiceCloud.shapes.bottomSheet,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        tonalElevation = VoiceCloud.elevation.elevated,
    ) {
        Column(Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.sm)) {
            title?.let {
                Text(
                    it,
                    style = VoiceCloud.typography.sectionTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = spacing.md),
                )
            }
            content()
        }
    }
}

@Composable
fun VCDialog(
    visible: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = null,
) {
    if (!visible) return
    val colors = VoiceCloud.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.surface,
        title = { Text(title, style = VoiceCloud.typography.sectionTitle, color = colors.textPrimary) },
        text = { Text(message, style = VoiceCloud.typography.body, color = colors.textSecondary) },
        confirmButton = { VCPrimaryButton(text = confirmLabel, onClick = onConfirm) },
        dismissButton = if (dismissLabel != null) {
            { VCTextButton(text = dismissLabel, onClick = onDismiss) }
        } else {
            null
        },
        shape = VoiceCloud.shapes.standard,
    )
}

@Composable
fun VCToast(
    message: String?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val host = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        if (message != null) {
            host.showSnackbar(message)
            onDismiss()
        }
    }
    SnackbarHost(hostState = host, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = VoiceCloud.shapes.standard,
            containerColor = VoiceCloud.colors.surfaceDark,
            contentColor = VoiceCloud.colors.textOnDark,
        )
    }
}
