package app.voicecloud.feature.profile.data

import app.voicecloud.feature.profile.model.ExtendedProfile
import app.voicecloud.feature.profile.model.UpdateExtendedProfileBody
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApi {
    @GET("replays")
    suspend fun replays(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): Any

    @GET("replays/{replayId}")
    suspend fun replay(@Path("replayId") replayId: String): Any

    @GET("room-activity/me")
    suspend fun myActivity(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): Any

    @GET("users/profile/me")
    suspend fun myProfile(): ExtendedProfile

    @PATCH("users/profile")
    suspend fun updateProfile(@Body body: UpdateExtendedProfileBody): Any

    @Multipart
    @POST("users/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Any

    @Multipart
    @PUT("users/avatar")
    suspend fun replaceAvatar(@Part avatar: MultipartBody.Part): Any

    @DELETE("users/avatar")
    suspend fun deleteAvatar(): Any

    @Multipart
    @POST("users/cover")
    suspend fun uploadCover(@Part cover: MultipartBody.Part): Any

    @Multipart
    @PUT("users/cover")
    suspend fun replaceCover(@Part cover: MultipartBody.Part): Any

    @DELETE("users/cover")
    suspend fun deleteCover(): Any

    @GET("blocks")
    suspend fun blockedUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): Any

    @DELETE("blocks/{userId}")
    suspend fun unblock(@Path("userId") userId: String): Any

    @GET("users/visitors")
    suspend fun visitors(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): Any

    @GET("users/visitors/stats")
    suspend fun visitorStats(): Any

    @GET("cms/pages")
    suspend fun helpPages(): Any

    @GET("cms/pages/{slug}")
    suspend fun helpPage(@Path("slug") slug: String): Any
}
