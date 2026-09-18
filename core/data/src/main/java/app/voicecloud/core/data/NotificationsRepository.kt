package app.voicecloud.core.data

import app.voicecloud.core.data.mapper.toPersonUiModel
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.model.NotificationDto
import app.voicecloud.core.network.NotificationsApi
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

data class VoiceCloudNotificationItem(
    val id: String,
    val title: String,
    val body: String?,
    val isRead: Boolean,
    val createdAt: String?,
    val type: String?,
    val targetType: String?,
    val targetId: String?,
    val actionUrl: String?,
    val conversationId: String?,
    val roomId: String?,
    val userId: String?,
    val clubId: String?,
)

@Singleton
class NotificationsRepository @Inject constructor(
    private val api: NotificationsApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun load(): ApiResult<List<VoiceCloudNotificationItem>> = when (val result = apiCall({ api.notifications() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().mapNotNull { it.toItem() })
        is ApiResult.Failure -> result
    }

    suspend fun unreadCount(): ApiResult<Int> = when (val result = apiCall({ api.unreadCount() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data?.resolvedCount ?: 0)
        is ApiResult.Failure -> result
    }

    suspend fun markRead(notificationId: String): ApiResult<Unit> = when (
        val result = apiCall({ api.markRead(notificationId) }, parser)
    ) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    suspend fun markAllRead(): ApiResult<Unit> = when (val result = apiCall({ api.readAll() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    private fun NotificationDto.toItem(): VoiceCloudNotificationItem? {
        val id = resolvedId ?: return null
        return VoiceCloudNotificationItem(
            id = id,
            title = resolvedTitle,
            body = resolvedBody,
            isRead = resolvedRead,
            createdAt = createdAt,
            type = type,
            targetType = targetType,
            targetId = targetId,
            actionUrl = actionUrl ?: link,
            conversationId = conversationId,
            roomId = roomId,
            userId = userId,
            clubId = clubId,
        )
    }
}

@Singleton
class SocialGraphRepository @Inject constructor(
    private val usersApi: app.voicecloud.core.network.UsersApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun followers(userId: String): ApiResult<List<app.voicecloud.core.designsystem.component.VCPersonUiModel>> =
        loadPeople { usersApi.followers(userId) }

    suspend fun following(userId: String): ApiResult<List<app.voicecloud.core.designsystem.component.VCPersonUiModel>> =
        loadPeople { usersApi.following(userId) }

    private suspend fun loadPeople(
        call: suspend () -> retrofit2.Response<app.voicecloud.core.model.DataEnvelope<List<app.voicecloud.core.model.DiscoveryUserDto>>>,
    ): ApiResult<List<app.voicecloud.core.designsystem.component.VCPersonUiModel>> = when (val result = apiCall(call, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().mapNotNull { it.toPersonUiModel() })
        is ApiResult.Failure -> result
    }
}
