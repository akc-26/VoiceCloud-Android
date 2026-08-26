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

    fun create(baseUrl: String, moshi: Moshi, tokenVault: TokenVault): BootstrapApi {
        require(baseUrl.endsWith('/')) { "VoiceCloud API base URL must end with /" }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(tokenVault))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BootstrapApi::class.java)
    }
}
