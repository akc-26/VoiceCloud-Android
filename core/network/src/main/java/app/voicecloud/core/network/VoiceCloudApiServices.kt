package app.voicecloud.core.network

import app.voicecloud.core.security.TokenVault
import com.squareup.moshi.Moshi
import retrofit2.Retrofit

/** Shared authenticated Retrofit services (single OkHttp client + refresh authenticator). */
data class VoiceCloudApiServices(
    val bootstrap: BootstrapApi,
    val auth: AuthApi,
    val discovery: DiscoveryApi,
    val search: SearchApi,
    val users: UsersApi,
    val chat: ChatApi,
    val wallet: WalletApi,
    val rooms: RoomsApi,
    val rtc: RtcApi,
    val session: SessionApi,
    val creatorAccess: CreatorAccessApi,
) {
    companion object {
        fun create(baseUrl: String, moshi: Moshi, tokenVault: TokenVault): VoiceCloudApiServices {
            val authenticator = TokenRefreshAuthenticator(baseUrl, moshi, tokenVault)
            val client = ApiClientFactory.createOkHttpClient(tokenVault, authenticator)
            val retrofit: Retrofit = ApiClientFactory.createRetrofit(baseUrl, moshi, client)
            return VoiceCloudApiServices(
                bootstrap = retrofit.create(BootstrapApi::class.java),
                auth = retrofit.create(AuthApi::class.java),
                discovery = retrofit.create(DiscoveryApi::class.java),
                search = retrofit.create(SearchApi::class.java),
                users = retrofit.create(UsersApi::class.java),
                chat = retrofit.create(ChatApi::class.java),
                wallet = retrofit.create(WalletApi::class.java),
                rooms = retrofit.create(RoomsApi::class.java),
                rtc = retrofit.create(RtcApi::class.java),
                session = retrofit.create(SessionApi::class.java),
                creatorAccess = retrofit.create(CreatorAccessApi::class.java),
            )
        }
    }
}
