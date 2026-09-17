package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import app.voicecloud.core.designsystem.VoiceCloud

@Immutable
data class VCTab(
    val id: String,
    val label: String,
)

@Composable
fun VCTabBar(
    tabs: List<VCTab>,
    selectedId: String,
    onSelect: (VCTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = tabs.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    val colors = VoiceCloud.colors
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = colors.background,
        contentColor = colors.primary,
    ) {
        tabs.forEach { tab ->
            val selected = tab.id == selectedId
            Tab(
                selected = selected,
                onClick = { onSelect(tab) },
                modifier = Modifier.semantics { role = Role.Tab },
                text = {
                    Text(
                        text = tab.label,
                        style = VoiceCloud.typography.actionLabel,
                        color = if (selected) colors.primary else colors.textMuted,
                    )
                },
            )
        }
    }
}
