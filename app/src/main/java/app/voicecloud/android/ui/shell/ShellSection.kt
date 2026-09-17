package app.voicecloud.android.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCPageHeader

@Composable
internal fun ShellSection(
    title: String,
    subtitle: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxSize()) {
        VCPageHeader(title = title, subtitle = subtitle)
        VCEmptyState(
            title = title,
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
        )
    }
}

internal fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
