package app.voicecloud.feature.auth.di

import app.voicecloud.core.network.ApiClientFactory
import app.voicecloud.core.network.TokenRefreshCoordinator
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.auth.data.AuthenticatedAuthApi
import app.voicecloud.feature.auth.data.PublicAuthApi
import app.voicecloud.feature.auth.data.SingleFlightTokenRefreshCoordinator
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {
    @Provides
    @Singleton
    fun publicAuthApi(
        @Named("voicecloudApiBaseUrl") baseUrl: String,
        moshi: Moshi,
    ): PublicAuthApi = ApiClientFactory.createPublicRetrofit(baseUrl, moshi).create(PublicAuthApi::class.java)

    @Provides
    @Singleton
    fun tokenRefreshCoordinator(coordinator: SingleFlightTokenRefreshCoordinator): TokenRefreshCoordinator = coordinator

    @Provides
    @Singleton
    fun authenticatedAuthApi(
        @Named("voicecloudApiBaseUrl") baseUrl: String,
        moshi: Moshi,
        tokenVault: TokenVault,
        refreshCoordinator: TokenRefreshCoordinator,
    ): AuthenticatedAuthApi = ApiClientFactory.createAuthenticatedRetrofit(
        baseUrl = baseUrl,
        moshi = moshi,
        tokenVault = tokenVault,
        refreshCoordinator = refreshCoordinator,
    ).create(AuthenticatedAuthApi::class.java)
}
