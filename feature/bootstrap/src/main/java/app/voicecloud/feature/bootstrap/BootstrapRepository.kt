package app.voicecloud.feature.bootstrap

import app.voicecloud.core.logging.VoiceCloudLogger
import app.voicecloud.core.model.ApiError
import app.voicecloud.core.model.MobileConfig
import app.voicecloud.core.network.BootstrapApi
import app.voicecloud.core.network.SafeApiErrorParser

sealed interface BootstrapResult {
    data class Success(val config: MobileConfig) : BootstrapResult
    data class Failure(val error: ApiError) : BootstrapResult
}

interface BootstrapRepository { suspend fun load(): BootstrapResult }

class DefaultBootstrapRepository(
    private val api: BootstrapApi,
    private val errorParser: SafeApiErrorParser,
    private val logger: VoiceCloudLogger,
) : BootstrapRepository {
    override suspend fun load(): BootstrapResult = try {
        val response = api.getMobileConfig()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            logger.debug("Bootstrap", "Mobile configuration loaded (HTTP ${response.code()})")
            BootstrapResult.Success(body)
        } else {
            val error = errorParser.parse(response)
            logger.warn("Bootstrap", "Mobile configuration failed (HTTP ${error.httpStatus ?: 0}, requestId=${error.requestId ?: "none"})")
            BootstrapResult.Failure(error)
        }
    } catch (t: Throwable) {
        logger.error("Bootstrap", "Mobile configuration transport failure (${t::class.java.simpleName})", t)
        BootstrapResult.Failure(
            ApiError(
                message = "VoiceCloud is currently unreachable. Check your connection and try again.",
                retryable = true,
            ),
        )
    }
}
