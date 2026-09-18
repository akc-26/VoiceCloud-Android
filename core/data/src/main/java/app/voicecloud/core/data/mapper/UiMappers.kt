package app.voicecloud.core.data.mapper

import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.model.ChatConversationDto
import app.voicecloud.core.model.ChatMessageDto
import app.voicecloud.core.model.DiscoveryRoomDto
import app.voicecloud.core.model.DiscoveryUserDto
import app.voicecloud.core.model.UserProfileDto
import app.voicecloud.core.model.WalletBalanceDto
import app.voicecloud.core.model.WalletTransactionDto

fun DiscoveryRoomDto.toRoomUiModel(): VCRoomUiModel? {
    val roomId = resolvedId ?: return null
    val meta = buildList {
        hostDisplayName?.let { add("Host · $it") }
        hostName?.let { add("Host · $it") }
        listenerCount?.let { add("$it listening") }
        participantCount?.let { add("$it in room") }
    }.joinToString(" · ").ifBlank { null }
    return VCRoomUiModel(
        id = roomId,
        title = resolvedTitle,
        subtitle = description,
        metaLabel = meta,
        hostLabel = hostDisplayName ?: hostName,
        live = resolvedLive,
    )
}

fun DiscoveryUserDto.toPersonUiModel(): VCPersonUiModel? {
    val personId = resolvedId ?: username ?: return null
    return VCPersonUiModel(
        userId = resolvedId,
        name = resolvedName,
        subtitle = username?.let { "@$it" },
        metaLabel = if (isOnline == true) "Online now" else null,
    )
}

fun UserProfileDto.toDisplayName(): String = displayName ?: username ?: "Your profile"

fun ChatConversationDto.toConversationUi(id: String = resolvedId): app.voicecloud.core.data.model.ConversationSummary {
    return app.voicecloud.core.data.model.ConversationSummary(
        id = id,
        title = resolvedTitle,
        preview = lastMessagePreview,
        unreadCount = unreadCount ?: 0,
    )
}

fun ChatMessageDto.toMessageUi(currentUserId: String?): app.voicecloud.core.data.model.MessageItem {
    val outgoing = resolvedOutgoing || (currentUserId != null && senderId == currentUserId)
    return app.voicecloud.core.data.model.MessageItem(
        id = resolvedId,
        text = resolvedText,
        outgoing = outgoing,
        metaLabel = createdAt,
    )
}

fun WalletBalanceDto.toBalanceLabel(): String {
    val coins = coins ?: balance
    return when {
        coins != null -> coins.toString()
        diamonds != null -> "$diamonds diamonds"
        else -> "—"
    }
}

fun WalletTransactionDto.toLineLabel(): String =
    listOfNotNull(description, amount?.let { if (it >= 0) "+$it" else "$it" }, createdAt).joinToString(" · ")
