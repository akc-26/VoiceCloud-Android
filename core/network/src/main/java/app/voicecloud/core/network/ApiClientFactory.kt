package app.voicecloud.core.network

import app.voicecloud.core.security.TokenVault
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central Retrofit/OkHttp authority.
 *
 * Public and authenticated clients are deliberately separate. Public bootstrap/auth endpoints never
 * inherit a stale bearer token; authenticated clients always use TokenVault plus the single-flight
 * refresh coordinator supplied by PH02.
 */
object ApiClientFactory {
    fun createMoshi(): Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    fun createPublicRetrofit(baseUrl: String, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(requireCanonicalBaseUrl(baseUrl))
            .client(baseClient().build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    fun createAuthenticatedRetrofit(
        baseUrl: String,
        moshi: Moshi,
        tokenVault: TokenVault,
        refreshCoordinator: TokenRefreshCoordinator,
    ): Retrofit {
        val client = baseClient()
            .addInterceptor(AuthTokenInterceptor(tokenVault))
            .authenticator(RefreshingAuthenticator(tokenVault, refreshCoordinator))
            .build()
        return Retrofit.Builder()
            .baseUrl(requireCanonicalBaseUrl(baseUrl))
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /** Backwards-compatible PH01 bootstrap factory, now correctly public/no-bearer. */
    fun create(baseUrl: String, moshi: Moshi, tokenVault: TokenVault): BootstrapApi =
        createPublicRetrofit(baseUrl, moshi).create(BootstrapApi::class.java)

    private fun baseClient(): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)

    private fun requireCanonicalBaseUrl(baseUrl: String): String {
        require(baseUrl.startsWith("https://")) { "VoiceCloud API must use HTTPS" }
        require(baseUrl.endsWith('/')) { "VoiceCloud API base URL must end with /" }
        return baseUrl
    }
}

/** Blocking bridge used only by OkHttp's 401 authenticator. Implementations must be single-flight. */
interface TokenRefreshCoordinator {
    fun refreshBlocking(failedAccessToken: String?): String?
}

private class RefreshingAuthenticator(
    private val tokenVault: TokenVault,
    private val coordinator: TokenRefreshCoordinator,
) : Authenticator {
    override fun authenticate(route: okhttp3.Route?, response: okhttp3.Response): okhttp3.Request? {
        if (responseCount(response) >= 2) return null
        val failed = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val refreshed = coordinator.refreshBlocking(failed)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return response.request.newBuilder().header("Authorization", "Bearer $refreshed").build()
    }

    private fun responseCount(response: okhttp3.Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count += 1
            prior = prior.priorResponse
        }
        return count
    }
}
