package app.voicecloud.android.di

import android.content.Context
import app.voicecloud.android.BuildConfig
import app.voicecloud.android.network.ApiEndpointResolver
import app.voicecloud.core.database.VoiceCloudDatabase
import app.voicecloud.core.logging.AndroidVoiceCloudLogger
import app.voicecloud.core.logging.VoiceCloudLogger
import app.voicecloud.core.model.AppIdentity
import app.voicecloud.core.network.ApiClientFactory
import app.voicecloud.core.network.AuthApi
import app.voicecloud.core.network.BootstrapApi
import app.voicecloud.core.network.ChatApi
import app.voicecloud.core.network.CreatorAccessApi
import app.voicecloud.core.network.DiscoveryApi
import app.voicecloud.core.network.RoomsApi
import app.voicecloud.core.network.RtcApi
import app.voicecloud.core.network.SearchApi
import app.voicecloud.core.network.SessionApi
import app.voicecloud.core.network.NotificationsApi
import app.voicecloud.core.network.BlocksApi
import app.voicecloud.core.network.RankingsApi
import app.voicecloud.core.network.ReferralsApi
import app.voicecloud.core.network.TasksAchievementsApi
import app.voicecloud.core.network.FriendsApi
import app.voicecloud.core.network.VisitorsApi
import app.voicecloud.core.network.UserSettingsApi
import app.voicecloud.core.network.RoomActivityApi
import app.voicecloud.core.network.ClubsApi
import app.voicecloud.core.network.ScheduledRoomsApi
import app.voicecloud.core.network.UsersApi
import app.voicecloud.core.network.WalletApi
import app.voicecloud.core.network.SafeApiErrorParser
import app.voicecloud.core.network.VoiceCloudApiServices
import app.voicecloud.core.preferences.VoiceCloudPreferences
import app.voicecloud.core.realtime.RealtimeClient
import app.voicecloud.core.realtime.RealtimeClientFactory
import app.voicecloud.core.security.AndroidKeyStoreTokenVault
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.bootstrap.BootstrapRepository
import app.voicecloud.feature.bootstrap.DefaultBootstrapRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FoundationModule {
    @Provides @Singleton fun moshi(): Moshi = ApiClientFactory.createMoshi()

    @Provides @Singleton
    fun endpoints(): app.voicecloud.android.network.VoiceCloudEndpoints = ApiEndpointResolver.resolve(
        configuredApiBaseUrl = BuildConfig.API_BASE_URL,
        configuredSocketBaseUrl = BuildConfig.SOCKET_BASE_URL,
        configuredWebBaseUrl = BuildConfig.WEB_BASE_URL,
        debug = BuildConfig.DEBUG,
    )

    @Provides @Singleton
    fun tokenVault(@ApplicationContext context: Context): TokenVault = AndroidKeyStoreTokenVault(context)

    @Provides @Singleton
    fun voiceCloudApiServices(
        endpoints: app.voicecloud.android.network.VoiceCloudEndpoints,
        moshi: Moshi,
        tokenVault: TokenVault,
        sessionExpiredListener: app.voicecloud.core.network.SessionExpiredListener,
    ): VoiceCloudApiServices = VoiceCloudApiServices.create(
        endpoints.apiBaseUrl,
        moshi,
        tokenVault,
        sessionExpiredListener,
    )

    @Provides @Singleton fun bootstrapApi(services: VoiceCloudApiServices): BootstrapApi = services.bootstrap

    @Provides @Singleton fun authApi(services: VoiceCloudApiServices): AuthApi = services.auth

    @Provides @Singleton fun discoveryApi(services: VoiceCloudApiServices): DiscoveryApi = services.discovery

    @Provides @Singleton fun searchApi(services: VoiceCloudApiServices): SearchApi = services.search

    @Provides @Singleton fun usersApi(services: VoiceCloudApiServices): UsersApi = services.users

    @Provides @Singleton fun chatApi(services: VoiceCloudApiServices): ChatApi = services.chat

    @Provides @Singleton fun walletApi(services: VoiceCloudApiServices): WalletApi = services.wallet

    @Provides @Singleton fun roomsApi(services: VoiceCloudApiServices): RoomsApi = services.rooms

    @Provides @Singleton fun rtcApi(services: VoiceCloudApiServices): RtcApi = services.rtc

    @Provides @Singleton fun sessionApi(services: VoiceCloudApiServices): SessionApi = services.session

    @Provides @Singleton fun notificationsApi(services: VoiceCloudApiServices): NotificationsApi = services.notifications

    @Provides @Singleton fun creatorAccessApi(services: VoiceCloudApiServices): CreatorAccessApi = services.creatorAccess

    @Provides @Singleton fun blocksApi(services: VoiceCloudApiServices): BlocksApi = services.blocks

    @Provides @Singleton fun rankingsApi(services: VoiceCloudApiServices): RankingsApi = services.rankings

    @Provides @Singleton fun referralsApi(services: VoiceCloudApiServices): ReferralsApi = services.referrals

    @Provides @Singleton fun tasksAchievementsApi(services: VoiceCloudApiServices): TasksAchievementsApi = services.tasksAchievements

    @Provides @Singleton fun friendsApi(services: VoiceCloudApiServices): FriendsApi = services.friends

    @Provides @Singleton fun visitorsApi(services: VoiceCloudApiServices): VisitorsApi = services.visitors

    @Provides @Singleton fun userSettingsApi(services: VoiceCloudApiServices): UserSettingsApi = services.userSettings

    @Provides @Singleton fun roomActivityApi(services: VoiceCloudApiServices): RoomActivityApi = services.roomActivity

    @Provides @Singleton fun clubsApi(services: VoiceCloudApiServices): ClubsApi = services.clubs

    @Provides @Singleton fun scheduledRoomsApi(services: VoiceCloudApiServices): ScheduledRoomsApi = services.scheduledRooms

    @Provides @Singleton
    fun realtimeClient(
        endpoints: app.voicecloud.android.network.VoiceCloudEndpoints,
        tokenVault: TokenVault,
    ): RealtimeClient = RealtimeClientFactory.create(endpoints.socketBaseUrl, tokenVault)

    @Provides @Singleton fun errorParser(moshi: Moshi) = SafeApiErrorParser(moshi)

    @Provides @Singleton
    fun logger(): VoiceCloudLogger = AndroidVoiceCloudLogger(debugEnabled = BuildConfig.DEBUG)

    @Provides @Singleton
    fun appIdentity(): AppIdentity = AppIdentity(
        versionName = BuildConfig.VERSION_NAME,
        environment = BuildConfig.ENVIRONMENT,
    )

    @Provides @Singleton
    fun bootstrapRepository(
        api: BootstrapApi,
        parser: SafeApiErrorParser,
        logger: VoiceCloudLogger,
    ): BootstrapRepository = DefaultBootstrapRepository(api, parser, logger)

    @Provides @Singleton
    fun preferences(@ApplicationContext context: Context): VoiceCloudPreferences = VoiceCloudPreferences(context)

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): VoiceCloudDatabase = VoiceCloudDatabase.create(context)
}
