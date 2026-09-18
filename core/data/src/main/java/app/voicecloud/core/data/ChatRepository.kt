package app.voicecloud.core.data

import app.voicecloud.core.data.mapper.toConversationUi
import app.voicecloud.core.data.mapper.toMessageUi
import app.voicecloud.core.data.model.ConversationSummary
import app.voicecloud.core.data.model.MessageItem
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.model.VoiceCloudUser
import app.voicecloud.core.network.ChatApi
import app.voicecloud.core.network.SendMessageRequest
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.UsersApi
import app.voicecloud.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ChatApi,
    private val usersApi: UsersApi,
    private val parser: SafeApiErrorParser,
) {
    private var cachedUserId: String? = null

    suspend fun conversations(): ApiResult<List<ConversationSummary>> = when (
        val result = apiCall({ api.conversations() }, parser)
    ) {
        is ApiResult.Success -> ApiResult.Success(
            result.data.data.orEmpty().map { it.toConversationUi() }.filter { it.id.isNotBlank() },
        )
        is ApiResult.Failure -> result
    }

    suspend fun messages(conversationId: String): ApiResult<List<MessageItem>> {
        val userId = currentUserId()
        return when (val result = apiCall({ api.messages(conversationId) }, parser)) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data.orEmpty().map { it.toMessageUi(userId) },
            )
            is ApiResult.Failure -> result
        }
    }

    suspend fun sendMessage(conversationId: String, text: String): ApiResult<MessageItem> {
        val userId = currentUserId()
        return when (
            val result = apiCall({ api.sendMessage(conversationId, SendMessageRequest(text = text, content = text)) }, parser)
        ) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) {
                    ApiResult.Success(dto.toMessageUi(userId))
                } else {
                    ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Message was not returned by VoiceCloud."))
                }
            }
            is ApiResult.Failure -> result
        }
    }

    suspend fun markRead(conversationId: String): ApiResult<Unit> = when (
        val result = apiCall({ api.markRead(conversationId) }, parser)
    ) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    private suspend fun currentUserId(): String? {
        cachedUserId?.let { return it }
        val me = apiCall({ usersApi.myProfile() }, parser)
        if (me is ApiResult.Success) {
            cachedUserId = me.data.data?.resolvedId
        }
        return cachedUserId
    }
}

@Singleton
class ProfileRepository @Inject constructor(
    private val api: UsersApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun loadMyProfile(): ApiResult<VoiceCloudUser> = when (val result = apiCall({ api.myProfile() }, parser)) {
        is ApiResult.Success -> {
            val profile = result.data.data
            if (profile == null) {
                ApiResult.Failure(app.voicecloud.core.model.ApiError(message = "Profile unavailable."))
            } else {
                ApiResult.Success(
                    VoiceCloudUser(
                        id = profile.resolvedId.orEmpty(),
                        username = profile.username,
                        displayName = profile.displayName,
                        bio = profile.bio,
                        avatarUrl = profile.avatarUrl,
                        role = profile.role,
                    ),
                )
            }
        }
        is ApiResult.Failure -> result
    }

    suspend fun follow(userId: String): ApiResult<Unit> = when (val result = apiCall({ api.follow(userId) }, parser)) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    suspend fun unfollow(userId: String): ApiResult<Unit> = when (val result = apiCall({ api.unfollow(userId) }, parser)) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }
}
