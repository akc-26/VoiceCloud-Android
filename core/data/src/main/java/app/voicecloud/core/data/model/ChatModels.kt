package app.voicecloud.core.data.model

data class ConversationSummary(
    val id: String,
    val title: String,
    val preview: String? = null,
    val unreadCount: Int = 0,
)

data class MessageItem(
    val id: String,
    val text: String,
    val outgoing: Boolean,
    val metaLabel: String? = null,
)
