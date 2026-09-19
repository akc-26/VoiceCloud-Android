package app.voicecloud.feature.discovery.di

import app.voicecloud.core.network.ApiClientFactory
import app.voicecloud.core.network.TokenRefreshCoordinator
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.discovery.data.DiscoveryApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiscoveryNetworkModule {
    @Provides
    @Singleton
    fun discoveryApi(
        @Named("voicecloudApiBaseUrl") baseUrl: String,
        moshi: Moshi,
        tokenVault: TokenVault,
        refreshCoordinator: TokenRefreshCoordinator,
    ): DiscoveryApi = ApiClientFactory.createAuthenticatedRetrofit(
        baseUrl = baseUrl,
        moshi = moshi,
        tokenVault = tokenVault,
        refreshCoordinator = refreshCoordinator,
    ).create(DiscoveryApi::class.java)
}
