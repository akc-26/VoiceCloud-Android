package app.voicecloud.core.network

import app.voicecloud.core.model.MobileConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BootstrapApi {
    @GET("config/mobile")
    suspend fun getMobileConfig(@Query("platform") platform: String = "android"): Response<MobileConfig>
}
