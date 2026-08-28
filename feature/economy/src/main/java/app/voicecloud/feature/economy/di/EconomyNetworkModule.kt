package app.voicecloud.feature.economy.di
import app.voicecloud.core.network.*
import app.voicecloud.core.security.TokenVault
import app.voicecloud.feature.economy.data.EconomyApi
import com.squareup.moshi.Moshi
import dagger.Module; import dagger.Provides; import dagger.hilt.InstallIn; import dagger.hilt.components.SingletonComponent
import javax.inject.Named; import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
object EconomyNetworkModule {
 @Provides @Singleton fun api(@Named("voicecloudApiBaseUrl") baseUrl:String, moshi:Moshi, tokenVault:TokenVault, refreshCoordinator:TokenRefreshCoordinator):EconomyApi = ApiClientFactory.createAuthenticatedRetrofit(baseUrl,moshi,tokenVault,refreshCoordinator).create(EconomyApi::class.java)
}
