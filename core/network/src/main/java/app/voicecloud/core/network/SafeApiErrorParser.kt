package app.voicecloud.core.network

import app.voicecloud.core.model.ApiError
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import retrofit2.Response

@JsonClass(generateAdapter = false)
internal data class ErrorEnvelope(val statusCode: Int? = null, val code: String? = null, val message: String? = null, val error: String? = null, val requestId: String? = null)

class SafeApiErrorParser(moshi: Moshi) {
    private val adapter = moshi.adapter(ErrorEnvelope::class.java)
    fun parse(response: Response<*>): ApiError {
        val envelope = runCatching { response.errorBody()?.string()?.let(adapter::fromJson) }.getOrNull()
        val status = envelope?.statusCode ?: response.code()
        return ApiError(
            httpStatus = status,
            code = envelope?.code,
            message = envelope?.message ?: envelope?.error ?: "VoiceCloud could not complete this request.",
            requestId = envelope?.requestId,
            retryable = status == 408 || status == 429 || status >= 500,
        )
    }
}
