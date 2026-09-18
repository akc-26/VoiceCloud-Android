package app.voicecloud.core.network

import app.voicecloud.core.security.TokenVault
import okhttp3.Interceptor
import okhttp3.Response

/**
 * When an authenticated request still returns 401 after refresh retry, end the VoiceCloud session.
 */
class SessionExpiryResponseInterceptor(
    private val tokenVault: TokenVault,
    private val sessionExpiredListener: SessionExpiredListener?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val hadAuth = !request.header("Authorization").isNullOrBlank()
        val response = chain.proceed(request)
        if (response.code == 401 && hadAuth && shouldTreatAsSessionExpired(request.url.encodedPath)) {
            if (!tokenVault.accessToken().isNullOrBlank()) {
                tokenVault.clear()
            }
            sessionExpiredListener?.onSessionExpired()
        }
        return response
    }

    private fun shouldTreatAsSessionExpired(path: String): Boolean {
        val lower = path.lowercase()
        if (lower.contains("/auth/login") || lower.contains("/auth/register") ||
            lower.contains("/auth/refresh") || lower.contains("/auth/guest/login")
        ) {
            return false
        }
        return true
    }
}
