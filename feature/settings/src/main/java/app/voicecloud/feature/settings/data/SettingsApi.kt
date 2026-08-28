package app.voicecloud.feature.settings.data

import app.voicecloud.feature.settings.model.ContactSupportRequest
import app.voicecloud.feature.settings.model.ReportRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SettingsApi {
    @GET("users/settings") suspend fun settings(): Any
    @PATCH("users/settings") suspend fun updateSettings(@Body body: Map<String, @JvmSuppressWildcards Any?>): Any
    @GET("users/privacy") suspend fun privacy(): Any
    @PATCH("users/privacy") suspend fun updatePrivacy(@Body body: Map<String, @JvmSuppressWildcards Any?>): Any

    @GET("auth/sessions") suspend fun sessions(): Any
    @GET("auth/sessions/{sessionId}") suspend fun session(@Path("sessionId") sessionId: String): Any
    @DELETE("auth/sessions/{sessionId}") suspend fun revokeSession(@Path("sessionId") sessionId: String): Any
    @GET("auth/devices") suspend fun devices(): Any
    @GET("auth/devices/{deviceId}") suspend fun device(@Path("deviceId") deviceId: String): Any
    @DELETE("auth/devices/{deviceId}") suspend fun revokeDevice(@Path("deviceId") deviceId: String): Any
    @GET("auth/history") suspend fun history(@Query("limit") limit: Int = 100): Any

    @GET("cms/pages") suspend fun cmsPages(): Any
    @GET("cms/pages/{slug}") suspend fun cmsPage(@Path("slug") slug: String): Any

    @POST("reports") suspend fun submitReport(@Body request: ReportRequest): Any
    @GET("reports/my-reports") suspend fun myReports(@Query("page") page: Int = 1, @Query("limit") limit: Int = 100): Any
    @GET("reports/targets/search") suspend fun searchReportTargets(
        @Query("search") search: String,
        @Query("targetType") targetType: String? = null,
        @Query("limit") limit: Int = 20,
    ): Any
    @GET("reports/targets/{targetType}/{targetId}") suspend fun reportTarget(
        @Path("targetType") targetType: String,
        @Path("targetId") targetId: String,
    ): Any

    @GET("config/maintenance") suspend fun maintenance(): Any
    @POST("contact") suspend fun contact(@Body request: ContactSupportRequest): Any
}
