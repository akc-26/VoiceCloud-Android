package app.voicecloud.core.model

data class ApiError(
    val httpStatus: Int? = null,
    val code: String? = null,
    val message: String,
    val requestId: String? = null,
    val retryable: Boolean = false,
)
