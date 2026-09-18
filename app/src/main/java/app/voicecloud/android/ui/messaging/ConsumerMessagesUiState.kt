package app.voicecloud.android.ui.messaging

import androidx.compose.runtime.Immutable

@Immutable
data class VCConversationUiModel(
    val id: String,
    val title: String,
    val preview: String? = null,
    val metaLabel: String? = null,
)

@Immutable
data class VCMessageUiModel(
    val id: String,
    val text: String,
    val outgoing: Boolean,
    val metaLabel: String? = null,
)

@Immutable
data class ConsumerMessagesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val conversations: List<VCConversationUiModel> = emptyList(),
)

@Immutable
data class ConsumerConversationUiState(
    val conversationId: String,
    val title: String,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<VCMessageUiModel> = emptyList(),
    val composerText: String = "",
)
