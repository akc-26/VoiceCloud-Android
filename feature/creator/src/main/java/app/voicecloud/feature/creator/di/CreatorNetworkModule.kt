package app.voicecloud.feature.creator.di

import app.voicecloud.core.network.ApiClientFactory
import app.voicecloud.core.network.TokenRefreshCoordinator
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.creator.data.CreatorApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CreatorNetworkModule {
    @Provides
    @Singleton
    fun creatorApi(
        @Named("voicecloudApiBaseUrl") baseUrl: String,
        moshi: Moshi,
        tokenVault: TokenVault,
        refreshCoordinator: TokenRefreshCoordinator,
    ): CreatorApi = ApiClientFactory
        .createAuthenticatedRetrofit(baseUrl, moshi, tokenVault, refreshCoordinator)
        .create(CreatorApi::class.java)
}
