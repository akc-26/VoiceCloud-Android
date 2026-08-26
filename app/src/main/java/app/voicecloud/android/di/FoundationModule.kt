package app.voicecloud.android.di

import android.content.Context
import app.voicecloud.android.BuildConfig
import app.voicecloud.android.network.ApiEndpointResolver
import app.voicecloud.core.database.VoiceCloudDatabase
import app.voicecloud.core.logging.AndroidVoiceCloudLogger
import app.voicecloud.core.logging.VoiceCloudLogger
import app.voicecloud.core.model.AppIdentity
import app.voicecloud.core.network.ApiClientFactory
import app.voicecloud.core.network.BootstrapApi
import app.voicecloud.core.network.SafeApiErrorParser
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
    fun bootstrapApi(
        endpoints: app.voicecloud.android.network.VoiceCloudEndpoints,
        moshi: Moshi,
        tokenVault: TokenVault,
    ): BootstrapApi = ApiClientFactory.create(endpoints.apiBaseUrl, moshi, tokenVault)

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
