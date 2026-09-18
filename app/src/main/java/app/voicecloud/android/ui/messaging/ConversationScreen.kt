package app.voicecloud.android.ui.messaging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCComposer
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCMessageBubble
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCSkeleton

@Composable
fun ConversationScreen(
    state: ConsumerConversationUiState,
    onBack: () -> Unit,
    onComposerChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = VoiceCloud.spacing
    Column(modifier.fillMaxSize()) {
        VCPageHeader(
            title = state.title,
            subtitle = "Conversation",
            onBack = onBack,
        )
        Box(Modifier.weight(1f).fillMaxSize()) {
            when {
                state.errorMessage != null -> VCErrorState(
                    title = "Couldn't load conversation",
                    message = state.errorMessage,
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
                state.isLoading -> Column(
                    Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    VCSkeleton()
                    VCSkeleton()
                }
                state.messages.isEmpty() -> VCEmptyState(
                    title = "No messages yet",
                    message = "Messages will appear here when this conversation is connected.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.pageGutter,
                        end = spacing.pageGutter,
                        top = spacing.sm,
                        bottom = spacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        VCMessageBubble(
                            text = message.text,
                            outgoing = message.outgoing,
                            metaLabel = message.metaLabel,
                        )
                    }
                }
            }
        }
        VCComposer(
            value = state.composerText,
            onValueChange = onComposerChange,
            onSend = onSend,
        )
    }
}
