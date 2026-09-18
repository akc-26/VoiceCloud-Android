package app.voicecloud.core.network

import app.voicecloud.core.model.ApiError
import app.voicecloud.core.model.ApiResult
import retrofit2.Response

fun <T> Response<T>.toApiResult(parser: SafeApiErrorParser): ApiResult<T> {
    val body = body()
    return if (isSuccessful && body != null) {
        ApiResult.Success(body)
    } else {
        ApiResult.Failure(parser.parse(this))
    }
}

suspend fun <T> apiCall(block: suspend () -> Response<T>, parser: SafeApiErrorParser): ApiResult<T> = try {
    block().toApiResult(parser)
} catch (_: Throwable) {
    ApiResult.Failure(ApiError(message = "VoiceCloud is unreachable.", retryable = true))
}
