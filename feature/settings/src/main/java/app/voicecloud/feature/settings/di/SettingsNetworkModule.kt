package app.voicecloud.feature.settings.di

import app.voicecloud.core.network.ApiClientFactory
import app.voicecloud.core.network.TokenRefreshCoordinator
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.settings.data.SettingsApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsNetworkModule {
    @Provides
    @Singleton
    fun settingsApi(
        @Named("voicecloudApiBaseUrl") baseUrl: String,
        moshi: Moshi,
        tokenVault: TokenVault,
        refreshCoordinator: TokenRefreshCoordinator,
    ): SettingsApi = ApiClientFactory
        .createAuthenticatedRetrofit(baseUrl, moshi, tokenVault, refreshCoordinator)
        .create(SettingsApi::class.java)
}
