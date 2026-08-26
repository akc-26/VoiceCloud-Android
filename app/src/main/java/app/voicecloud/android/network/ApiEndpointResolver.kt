package app.voicecloud.android.network

import java.net.URI

data class VoiceCloudEndpoints(
    val apiBaseUrl: String,
    val socketBaseUrl: String,
    val webBaseUrl: String,
)

/**
 * Resolves the three VoiceCloud service authorities from the configured server URLs.
 *
 * Android development from PH01-R08 onward uses the deployed Raspberry Pi server,
 * not emulator/ADB loopback defaults. The API property may be supplied as either
 * the server origin (https://host) or the full API base (https://host/api/v1/).
 */
object ApiEndpointResolver {
    fun resolve(
        configuredApiBaseUrl: String,
        configuredSocketBaseUrl: String,
        configuredWebBaseUrl: String,
        debug: Boolean,
    ): VoiceCloudEndpoints {
        // `debug` is intentionally retained in the contract so future environment
        // policy can remain build-type aware without changing call sites.
        @Suppress("UNUSED_VARIABLE")
        val buildIsDebug = debug

        return VoiceCloudEndpoints(
            apiBaseUrl = normalizeApiBaseUrl(configuredApiBaseUrl),
            socketBaseUrl = normalizeSocketBaseUrl(configuredSocketBaseUrl),
            webBaseUrl = normalizeWebBaseUrl(configuredWebBaseUrl),
        )
    }

    internal fun normalizeApiBaseUrl(configured: String): String {
        val clean = normalizeConfiguredUrl(configured, "API")
        val withoutTrailingSlash = clean.trimEnd('/')
        return when {
            withoutTrailingSlash.endsWith("/api/v1", ignoreCase = true) -> "$withoutTrailingSlash/"
            else -> "$withoutTrailingSlash/api/v1/"
        }
    }

    internal fun normalizeSocketBaseUrl(configured: String): String =
        normalizeConfiguredUrl(configured, "Socket.IO").trimEnd('/')

    internal fun normalizeWebBaseUrl(configured: String): String =
        normalizeConfiguredUrl(configured, "Web").trimEnd('/') + "/"

    private fun normalizeConfiguredUrl(configured: String, label: String): String {
        val value = configured.trim()
        require(value.isNotBlank()) { "VoiceCloud $label URL is required" }
        val uri = runCatching { URI.create(value) }
            .getOrElse { throw IllegalArgumentException("VoiceCloud $label URL is invalid", it) }
        require(uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) {
            "VoiceCloud $label URL must use http or https"
        }
        require(!uri.host.isNullOrBlank()) { "VoiceCloud $label URL must contain a host" }
        require(uri.query == null && uri.fragment == null) { "VoiceCloud $label URL must not contain query or fragment data" }
        return value
    }
}
