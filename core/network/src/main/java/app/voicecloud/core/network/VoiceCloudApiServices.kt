package app.voicecloud.core.network

import app.voicecloud.core.security.TokenVault
import com.squareup.moshi.Moshi
import retrofit2.Retrofit

/** Shared authenticated Retrofit services (single OkHttp client + refresh authenticator). */
data class VoiceCloudApiServices(
    val bootstrap: BootstrapApi,
    val auth: AuthApi,
) {
    companion object {
        fun create(baseUrl: String, moshi: Moshi, tokenVault: TokenVault): VoiceCloudApiServices {
            val authenticator = TokenRefreshAuthenticator(baseUrl, moshi, tokenVault)
            val client = ApiClientFactory.createOkHttpClient(tokenVault, authenticator)
            val retrofit: Retrofit = ApiClientFactory.createRetrofit(baseUrl, moshi, client)
            return VoiceCloudApiServices(
                bootstrap = retrofit.create(BootstrapApi::class.java),
                auth = retrofit.create(AuthApi::class.java),
            )
        }
    }
}
