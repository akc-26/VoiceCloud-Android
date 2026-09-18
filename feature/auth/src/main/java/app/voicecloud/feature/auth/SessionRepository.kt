package app.voicecloud.feature.auth

import app.voicecloud.core.model.ApiError
import app.voicecloud.core.model.AuthSessionDto
import app.voicecloud.core.model.CreatorAccessApplicationRequest
import app.voicecloud.core.model.ApiResult
import app.voicecloud.core.network.CreatorAccessApi
import app.voicecloud.core.network.SessionApi
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

data class AuthSessionItem(
    val id: String,
    val label: String,
    val meta: String?,
    val isCurrent: Boolean,
    val detailMeta: String? = null,
)

data class AuthDeviceItem(
    val id: String,
    val label: String,
    val meta: String?,
    val isCurrent: Boolean,
)

@Singleton
class SessionRepository @Inject constructor(
    private val api: SessionApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun sessions(): ApiResult<List<AuthSessionItem>> = when (val result = apiCall({ api.sessions() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(
            result.data.data.orEmpty().mapNotNull { it.toItem() },
        )
        is ApiResult.Failure -> result
    }

    suspend fun session(sessionId: String): ApiResult<AuthSessionItem> = when (
        val result = apiCall({ api.session(sessionId) }, parser)
    ) {
        is ApiResult.Success -> {
            val item = result.data.data?.toItem()
            if (item != null) ApiResult.Success(item) else ApiResult.Failure(ApiError(message = "Session not found", retryable = false))
        }
        is ApiResult.Failure -> result
    }

    suspend fun devices(): ApiResult<List<AuthDeviceItem>> = when (val result = apiCall({ api.devices() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty().mapNotNull { it.toDeviceItem() })
        is ApiResult.Failure -> result
    }

    suspend fun device(deviceId: String): ApiResult<AuthDeviceItem> = when (
        val result = apiCall({ api.device(deviceId) }, parser)
    ) {
        is ApiResult.Success -> {
            val item = result.data.data?.toDeviceItem()
            if (item != null) ApiResult.Success(item) else ApiResult.Failure(ApiError(message = "Device not found", retryable = false))
        }
        is ApiResult.Failure -> result
    }

    suspend fun revokeDevice(deviceId: String): ApiResult<Unit> = when (
        val result = apiCall({ api.revokeDevice(deviceId) }, parser)
    ) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    suspend fun revokeSession(sessionId: String): ApiResult<Unit> = when (
        val result = apiCall({ api.revokeSession(sessionId) }, parser)
    ) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }

    suspend fun loginHistory(): ApiResult<List<AuthSessionItem>> = when (val result = apiCall({ api.loginHistory() }, parser)) {
        is ApiResult.Success -> ApiResult.Success(
            result.data.data.orEmpty().mapNotNull { it.toHistoryItem() },
        )
        is ApiResult.Failure -> result
    }

    private fun AuthSessionDto.toItem(): AuthSessionItem? {
        val id = id ?: sessionId ?: return null
        val label = deviceName ?: "VoiceCloud session"
        val meta = listOfNotNull(ipAddress, lastActiveAt?.let { "Last active $it" }).joinToString(" · ")
        val detail = listOfNotNull(userAgent, createdAt?.let { "Started $it" }).joinToString(" · ")
        return AuthSessionItem(
            id = id,
            label = label,
            meta = meta.ifBlank { null },
            isCurrent = current == true,
            detailMeta = detail.ifBlank { null },
        )
    }

    private fun app.voicecloud.core.model.AuthDeviceDto.toDeviceItem(): AuthDeviceItem? {
        val id = resolvedId ?: return null
        val meta = listOfNotNull(platform, lastActiveAt?.let { "Last active $it" }).joinToString(" · ")
        return AuthDeviceItem(id = id, label = resolvedName, meta = meta.ifBlank { null }, isCurrent = current == true)
    }

    private fun AuthSessionDto.toHistoryItem(): AuthSessionItem? {
        val id = id ?: sessionId ?: lastActiveAt ?: return null
        return AuthSessionItem(
            id = id,
            label = deviceName ?: "Sign-in activity",
            meta = listOfNotNull(ipAddress, lastActiveAt).joinToString(" · "),
            isCurrent = false,
        )
    }
}

@Singleton
class CreatorAccessRepository @Inject constructor(
    private val api: CreatorAccessApi,
    private val parser: SafeApiErrorParser,
) {
    suspend fun submit(request: CreatorAccessApplicationRequest): ApiResult<Unit> = when (
        val result = apiCall({ api.apply(request) }, parser)
    ) {
        is ApiResult.Success -> ApiResult.Success(Unit)
        is ApiResult.Failure -> result
    }
}
