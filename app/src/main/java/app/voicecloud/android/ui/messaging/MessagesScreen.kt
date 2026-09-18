package app.voicecloud.android.ui.messaging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCSearchField
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.foundation.vcTouchTarget
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun MessagesScreen(
    state: ConsumerMessagesUiState,
    modifier: Modifier = Modifier,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onConversationClick: (VCConversationUiModel) -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Messages", subtitle = "Conversations")
        VCSearchField(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.pageGutter, vertical = spacing.xs),
            placeholder = "Search conversations",
            enabled = state.conversations.isNotEmpty(),
        )
        when {
            state.errorMessage != null -> VCErrorState(
                title = "Couldn't load messages",
                message = state.errorMessage,
            )
            state.isLoading -> Column(
                Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                VCSkeleton()
                VCSkeleton()
            }
            state.conversations.isEmpty() -> VCEmptyState(
                title = "No conversations yet",
                message = "Conversations will appear here from VoiceCloud when messaging is connected.",
            )
            else -> AnimatedVisibility(
                visible = true,
                enter = if (motionEnabled) fadeIn(tween(VoiceCloudMotion.EntranceMs)) else fadeIn(tween(0)),
            ) {
                val filtered = state.conversations.filter { conversation ->
                    query.isBlank() ||
                        conversation.title.contains(query, ignoreCase = true) ||
                        conversation.preview.orEmpty().contains(query, ignoreCase = true)
                }
                if (filtered.isEmpty()) {
                    VCEmptyState(
                        title = "No matches",
                        message = "Try a different search term.",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = spacing.xxl),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        item { VCSectionHeader(title = "Recent") }
                        items(filtered, key = { it.id }) { conversation ->
                            ConversationRow(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: VCConversationUiModel,
    onClick: () -> Unit,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .vcTouchTarget()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = spacing.pageGutter, vertical = spacing.xxs),
        shape = VoiceCloud.shapes.compactCard,
        color = colors.surface,
        tonalElevation = VoiceCloud.elevation.subtle,
    ) {
        Column(
            Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(conversation.title, style = VoiceCloud.typography.actionLabel, color = colors.textPrimary)
            conversation.preview?.let {
                Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textSecondary, maxLines = 2)
            }
            conversation.metaLabel?.let {
                Text(it, style = VoiceCloud.typography.caption, color = colors.textMuted)
            }
        }
    }
}
