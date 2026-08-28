package app.voicecloud.feature.hosting.di

import app.voicecloud.core.network.ApiClientFactory
import app.voicecloud.core.network.TokenRefreshCoordinator
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.hosting.data.HostingApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HostingNetworkModule {
    @Provides
    @Singleton
    fun hostingApi(
        @Named("voicecloudApiBaseUrl") baseUrl: String,
        moshi: Moshi,
        tokenVault: TokenVault,
        refreshCoordinator: TokenRefreshCoordinator,
    ): HostingApi = ApiClientFactory.createAuthenticatedRetrofit(
        baseUrl = baseUrl,
        moshi = moshi,
        tokenVault = tokenVault,
        refreshCoordinator = refreshCoordinator,
    ).create(HostingApi::class.java)
}
