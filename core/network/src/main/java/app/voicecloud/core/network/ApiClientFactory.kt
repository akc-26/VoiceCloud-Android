package app.voicecloud.core.network

import app.voicecloud.core.security.TokenVault
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClientFactory {
    fun createMoshi(): Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    fun createOkHttpClient(
        tokenVault: TokenVault,
        authenticator: TokenRefreshAuthenticator? = null,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(tokenVault))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        if (authenticator != null) {
            builder.authenticator(authenticator)
        }
        return builder.build()
    }

    fun createRetrofit(baseUrl: String, moshi: Moshi, client: OkHttpClient): Retrofit {
        require(baseUrl.endsWith('/')) { "VoiceCloud API base URL must end with /" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun create(baseUrl: String, moshi: Moshi, tokenVault: TokenVault): BootstrapApi {
        val authenticator = TokenRefreshAuthenticator(baseUrl, moshi, tokenVault)
        val client = createOkHttpClient(tokenVault, authenticator)
        return createRetrofit(baseUrl, moshi, client).create(BootstrapApi::class.java)
    }

    inline fun <reified T> createService(
        baseUrl: String,
        moshi: Moshi,
        tokenVault: TokenVault,
    ): T {
        val authenticator = TokenRefreshAuthenticator(baseUrl, moshi, tokenVault)
        val client = createOkHttpClient(tokenVault, authenticator)
        return createRetrofit(baseUrl, moshi, client).create(T::class.java)
    }
}
