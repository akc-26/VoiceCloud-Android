package app.voicecloud.feature.auth

import app.voicecloud.feature.auth.google.AndroidCredentialGoogleSignInGateway
import app.voicecloud.feature.auth.google.GoogleSignInGateway
import dagger.Binds
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

@Module
@InstallIn(SingletonComponent::class)
abstract class GoogleSignInModule {
    @Binds @Singleton
    abstract fun bindGoogleSignInGateway(impl: AndroidCredentialGoogleSignInGateway): GoogleSignInGateway
}
