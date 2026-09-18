package app.voicecloud.core.network

import app.voicecloud.core.model.RefreshTokenRequest
import app.voicecloud.core.model.RefreshTokenResponse
import app.voicecloud.core.security.TokenVault
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Single-flight refresh authenticator for `/auth/refresh`.
 * Uses a dedicated Retrofit client without this authenticator to avoid recursion.
 */
class TokenRefreshAuthenticator(
    private val baseUrl: String,
    private val moshi: Moshi,
    private val tokenVault: TokenVault,
    private val sessionExpiredListener: SessionExpiredListener? = null,
) : Authenticator {
    private val lock = ReentrantLock()
    private val refreshApi: AuthApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuthApi::class.java)
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val refreshToken = tokenVault.refreshToken()?.takeIf { it.isNotBlank() } ?: run {
            tokenVault.clear()
            sessionExpiredListener?.onSessionExpired()
            return null
        }
        return lock.withLock {
            val latestAccess = tokenVault.accessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()
            if (latestAccess != null && requestToken != null && latestAccess != requestToken) {
                return@withLock response.request.newBuilder()
                    .header("Authorization", "Bearer $latestAccess")
                    .build()
            }
            val refreshResponse = runBlocking {
                refreshApi.refresh(RefreshTokenRequest(refreshToken))
            }
            val body: RefreshTokenResponse? = refreshResponse.body()
            if (!refreshResponse.isSuccessful || body == null) {
                tokenVault.clear()
                sessionExpiredListener?.onSessionExpired()
                return@withLock null
            }
            tokenVault.save(body.accessToken, body.refreshToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer ${body.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
