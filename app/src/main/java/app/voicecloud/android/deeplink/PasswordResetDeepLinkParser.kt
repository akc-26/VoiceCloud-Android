package app.voicecloud.android.deeplink

import android.net.Uri
import app.voicecloud.core.model.MobileConfig
import app.voicecloud.core.model.PasswordRecoveryConfig

object PasswordResetDeepLinkParser {
    fun extractToken(data: Uri?, mobileConfig: MobileConfig?, webBaseUrl: String): String? {
        if (data == null) return null
        if (!matchesResetDestination(data, mobileConfig?.passwordRecovery, webBaseUrl)) return null
        return data.getQueryParameter("token")?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun matchesResetDestination(
        data: Uri,
        recovery: PasswordRecoveryConfig?,
        webBaseUrl: String,
    ): Boolean {
        if (data.scheme.equals("voicecloud", ignoreCase = true) &&
            data.host.equals("reset-password", ignoreCase = true)
        ) {
            return true
        }
        if (!data.scheme.equals("https", ignoreCase = true)) return false
        val recoveryConfig = recovery ?: PasswordRecoveryConfig()
        if (recoveryConfig.androidAppLinkCompatible != true) {
            return matchesDefaultHttpsPath(data, webBaseUrl)
        }
        val configuredPath = recoveryConfig.httpsAppLinkPath?.trim().orEmpty()
        if (configuredPath.isNotEmpty()) {
            return pathMatches(data, configuredPath)
        }
        return matchesDefaultHttpsPath(data, webBaseUrl)
    }

    private fun matchesDefaultHttpsPath(data: Uri, webBaseUrl: String): Boolean {
        val webHost = runCatching { Uri.parse(webBaseUrl).host?.lowercase() }.getOrNull()
        val dataHost = data.host?.lowercase()
        if (webHost != null && dataHost != null && webHost != dataHost) return false
        return data.path.orEmpty().contains("reset-password", ignoreCase = true)
    }

    private fun pathMatches(data: Uri, configuredPath: String): Boolean {
        val normalized = if (configuredPath.startsWith("/")) configuredPath else "/$configuredPath"
        val path = data.path.orEmpty()
        return path.equals(normalized, ignoreCase = true) || path.startsWith("$normalized/", ignoreCase = true)
    }
}
