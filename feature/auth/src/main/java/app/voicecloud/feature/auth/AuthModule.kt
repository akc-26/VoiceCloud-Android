package app.voicecloud.feature.auth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides @Singleton
    fun authRepository(
        api: app.voicecloud.core.network.AuthApi,
        tokenVault: app.voicecloud.core.security.TokenVault,
        parser: app.voicecloud.core.network.SafeApiErrorParser,
    ): AuthRepository = AuthRepository(api, tokenVault, parser)
}
