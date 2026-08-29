package app.voicecloud.feature.creator.data

import app.voicecloud.feature.creator.model.CreatorContactRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    // PH12 creator audience/subscription authority.
    @GET("creator/plans") suspend fun plans(): Any
    @POST("creator/plans") suspend fun createPlan(@Body body: Map<String, @JvmSuppressWildcards Any?>): Any
    @PATCH("creator/plans/{id}") suspend fun updatePlan(@Path("id") id: String, @Body body: Map<String, @JvmSuppressWildcards Any?>): Any
    @DELETE("creator/plans/{id}") suspend fun archivePlan(@Path("id") id: String): Any
    @GET("creator/subscribers") suspend fun subscribers(
        @Query("status") status: String? = null,
        @Query("sortOrder") sortOrder: String = "DESC",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Any

    // PH13 creator analytics/economy authority. Streaming credential endpoints are intentionally forbidden on Android.
    @GET("analytics") suspend fun analytics(): Any
    @GET("wallet/balance") suspend fun walletBalance(): Any
    @GET("wallet/summary") suspend fun walletSummary(): Any
    @GET("wallet/transactions") suspend fun walletTransactions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Any
    @GET("gifts/history") suspend fun giftHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Any
    @GET("creator/earnings") suspend fun earnings(
        @Query("status") status: String? = null,
        @Query("sortOrder") sortOrder: String = "DESC",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Any
    @POST("creator/payout-request") suspend fun createPayoutRequest(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): Any
    @GET("creator/payout-requests") suspend fun payoutRequests(
        @Query("status") status: String? = null,
        @Query("sortOrder") sortOrder: String = "DESC",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Any
    @GET("creator/payout-requests/{id}") suspend fun payoutRequest(@Path("id") id: String): Any

}
