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
    val notifications: NotificationsApi,
    val blocks: BlocksApi,
    val rankings: RankingsApi,
    val referrals: ReferralsApi,
    val tasksAchievements: TasksAchievementsApi,
    val friends: FriendsApi,
    val visitors: VisitorsApi,
    val userSettings: UserSettingsApi,
    val roomActivity: RoomActivityApi,
    val clubs: ClubsApi,
    val scheduledRooms: ScheduledRoomsApi,
) {
    companion object {
        fun create(
            baseUrl: String,
            moshi: Moshi,
            tokenVault: TokenVault,
            sessionExpiredListener: SessionExpiredListener? = null,
        ): VoiceCloudApiServices {
            val authenticator = TokenRefreshAuthenticator(baseUrl, moshi, tokenVault, sessionExpiredListener)
            val client = ApiClientFactory.createOkHttpClient(tokenVault, authenticator, sessionExpiredListener)
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
                notifications = retrofit.create(NotificationsApi::class.java),
                blocks = retrofit.create(BlocksApi::class.java),
                rankings = retrofit.create(RankingsApi::class.java),
                referrals = retrofit.create(ReferralsApi::class.java),
                tasksAchievements = retrofit.create(TasksAchievementsApi::class.java),
                friends = retrofit.create(FriendsApi::class.java),
                visitors = retrofit.create(VisitorsApi::class.java),
                userSettings = retrofit.create(UserSettingsApi::class.java),
                roomActivity = retrofit.create(RoomActivityApi::class.java),
                clubs = retrofit.create(ClubsApi::class.java),
                scheduledRooms = retrofit.create(ScheduledRoomsApi::class.java),
            )
        }
    }
}
