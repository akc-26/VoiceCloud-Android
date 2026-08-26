package app.voicecloud.core.network

import app.voicecloud.core.security.TokenVault
import okhttp3.Interceptor
import okhttp3.Response

/**
 * PH01 authenticated HTTP foundation. PH02 adds refresh/authentication retry authority.
 * Public endpoints remain valid because the header is added only when a real token exists.
 */
internal class AuthTokenInterceptor(private val tokenVault: TokenVault) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenVault.accessToken()?.takeIf { it.isNotBlank() }
        val request = if (token == null) chain.request() else chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
