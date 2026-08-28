package app.voicecloud.core.network

import retrofit2.HttpException

private val technicalMarkers = listOf(
    "http ", "http/", "sql", "postgres", "typeorm", "constraint", "relation ", "column ",
    "stack trace", "exception", "retrofit", "okhttp", "socket", "jwt", "token hash", "api key",
    "secret", "database", "query failed", "java.", "kotlin.", "at app.", "localhost", "127.0.0.1",
)

/**
 * Converts internal/network failures into concise product-safe copy.
 * HTTP status text, provider diagnostics and database/framework details never reach end users.
 */
fun Throwable.toVoiceCloudUserMessage(
    fallback: String = "VoiceCloud Couldn’t Complete This Action. Try Again.",
    forbidden: String = "This Action Isn’t Available For Your Account.",
    unavailable: String = "VoiceCloud Is Temporarily Unavailable. Try Again Soon.",
): String {
    val http = this as? HttpException
    if (http != null) {
        return when {
            http.code() == 401 -> "Your Session Has Expired. Sign In Again."
            http.code() == 403 -> forbidden
            http.code() == 429 -> "Too Many Requests. Try Again In A Moment."
            http.code() >= 500 -> unavailable
            else -> fallback
        }
    }
    val raw = message.orEmpty().trim()
    val unsafe = raw.isBlank() || raw.length > 180 || technicalMarkers.any { raw.contains(it, ignoreCase = true) }
    return if (unsafe) fallback else raw
}

fun Throwable.toVoiceCloudPaymentMessage(): String = toVoiceCloudUserMessage(
    fallback = "Secure Payment Is Unavailable Right Now. Try Again.",
    forbidden = "This Purchase Isn’t Available For Your Account.",
    unavailable = "Secure Payment Is Temporarily Unavailable. Try Again Soon.",
)
