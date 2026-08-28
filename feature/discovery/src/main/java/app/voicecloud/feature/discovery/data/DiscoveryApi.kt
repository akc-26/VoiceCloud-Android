package app.voicecloud.feature.discovery.data

import app.voicecloud.feature.discovery.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscoveryApi {
    @GET("discovery/rooms/live")
    suspend fun liveRooms(@Query("page") page: Int = 1, @Query("limit") limit: Int = 40, @Query("category") category: String? = null): PaginatedItems<VoiceCloudRoom>

    @GET("discovery/rooms/trending")
    suspend fun trendingRooms(@Query("page") page: Int = 1, @Query("limit") limit: Int = 12): PaginatedItems<VoiceCloudRoom>

    @GET("discovery/rooms/popular")
    suspend fun popularRooms(@Query("page") page: Int = 1, @Query("limit") limit: Int = 12, @Query("category") category: String? = null): PaginatedItems<VoiceCloudRoom>

    @GET("discovery/rooms/following")
    suspend fun followingRooms(@Query("page") page: Int = 1, @Query("limit") limit: Int = 40, @Query("category") category: String? = null): PaginatedItems<VoiceCloudRoom>

    @GET("discovery/users/trending")
    suspend fun trendingUsers(@Query("page") page: Int = 1, @Query("limit") limit: Int = 30): PaginatedItems<VoiceCloudUser>

    @GET("discovery/users/suggested")
    suspend fun suggestedUsers(@Query("page") page: Int = 1, @Query("limit") limit: Int = 30): PaginatedItems<VoiceCloudUser>

    @GET("discovery/users/online")
    suspend fun onlineUsers(@Query("page") page: Int = 1, @Query("limit") limit: Int = 30): PaginatedItems<VoiceCloudUser>

    @GET("search")
    suspend fun search(@Query("q") query: String, @Query("type") type: String, @Query("page") page: Int = 1, @Query("limit") limit: Int = 20): GlobalSearchResponse

    @GET("users/public/{username}")
    suspend fun publicProfile(@Path("username") username: String): VoiceCloudProfile

    @GET("users/{userId}/profile")
    suspend fun profileById(@Path("userId") userId: String): VoiceCloudProfile

    @GET("users/profile/me")
    suspend fun myProfile(): VoiceCloudProfile

    @POST("users/{userId}/follow")
    suspend fun follow(@Path("userId") userId: String): FollowMutationResult

    @DELETE("users/{userId}/follow")
    suspend fun unfollow(@Path("userId") userId: String): FollowMutationResult

    @GET("users/followers")
    suspend fun followers(@Query("page") page: Int = 1, @Query("limit") limit: Int = 50, @Query("search") search: String? = null): PaginatedData<VoiceCloudUser>

    @GET("users/following")
    suspend fun following(@Query("page") page: Int = 1, @Query("limit") limit: Int = 50, @Query("search") search: String? = null): PaginatedData<VoiceCloudUser>

    @GET("users/friends")
    suspend fun friends(@Query("page") page: Int = 1, @Query("limit") limit: Int = 50, @Query("category") category: String? = null): Any

    @GET("users/friends/requests/pending")
    suspend fun pendingFriendRequests(): Any

    @GET("users/friends/suggested")
    suspend fun suggestedFriends(@Query("page") page: Int = 1, @Query("limit") limit: Int = 20): Any

    @POST("users/friends/request")
    suspend fun sendFriendRequest(@Body body: FriendRequestBody): ApiMessage

    @POST("users/friends/request/{requestId}/accept")
    suspend fun acceptFriendRequest(@Path("requestId") requestId: String): ApiMessage

    @POST("users/friends/request/{requestId}/reject")
    suspend fun rejectFriendRequest(@Path("requestId") requestId: String): ApiMessage

    @DELETE("users/friends/request/{requestId}/cancel")
    suspend fun cancelFriendRequest(@Path("requestId") requestId: String): ApiMessage

    @DELETE("users/friends/{friendId}")
    suspend fun removeFriend(@Path("friendId") friendId: String): ApiMessage
}
