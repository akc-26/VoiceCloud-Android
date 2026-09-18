package app.voicecloud.android.di

import app.voicecloud.android.BuildConfig
import app.voicecloud.android.session.VoiceCloudSessionNavigationBridge
import app.voicecloud.core.model.GoogleSignInConfig
import app.voicecloud.core.network.SessionExpiredListener
import app.voicecloud.core.security.TokenVault
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppAuthModule {
    @Provides @Singleton
    fun googleSignInConfig(): GoogleSignInConfig = GoogleSignInConfig(
        webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
    )

    @Provides @Singleton
    fun sessionExpiredListener(
        tokenVault: TokenVault,
        navigationBridge: VoiceCloudSessionNavigationBridge,
    ): SessionExpiredListener = SessionExpiredListener {
        tokenVault.clear()
        navigationBridge.notifySessionExpired()
    }
}
