package app.voicecloud.feature.creator.data

import app.voicecloud.feature.creator.model.CreatorContactRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CreatorApi {
    @GET("creator/dashboard") suspend fun dashboard(): Any
    @GET("users/profile/me") suspend fun profile(): Any
    @PATCH("users/profile") suspend fun updateProfile(@Body body: Map<String, @JvmSuppressWildcards Any?>): Any
    @GET("users/settings") suspend fun settings(): Any
    @PATCH("users/settings") suspend fun updateSettings(@Body body: Map<String, @JvmSuppressWildcards Any?>): Any
    @GET("cms/creator/pages") suspend fun cmsPages(): Any
    @GET("cms/creator/pages/{slug}") suspend fun cmsPage(@Path("slug") slug: String): Any
    @POST("contact") suspend fun contact(@Body request: CreatorContactRequest): Any
    @GET("config/maintenance") suspend fun maintenance(): Any
}
