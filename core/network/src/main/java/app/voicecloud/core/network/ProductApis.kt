package app.voicecloud.core.network

import app.voicecloud.core.model.ChatConversationDto
import app.voicecloud.core.model.ChatMessageDto
import app.voicecloud.core.model.CreatorAccessApplicationRequest
import app.voicecloud.core.model.DataEnvelope
import app.voicecloud.core.model.DiscoveryRoomDto
import app.voicecloud.core.model.DiscoveryUserDto
import app.voicecloud.core.model.AuthDeviceDto
import app.voicecloud.core.model.AuthSessionDto
import app.voicecloud.core.model.PaginatedItems
import app.voicecloud.core.model.PhoneSendOtpRequest
import app.voicecloud.core.model.PhoneLoginRequest
import app.voicecloud.core.model.RoomDetailDto
import app.voicecloud.core.model.RtcJoinRequest
import app.voicecloud.core.model.RtcJoinResponse
import app.voicecloud.core.model.SearchResultsDto
import app.voicecloud.core.model.NotificationDto
import app.voicecloud.core.model.NotificationUnreadDto
import app.voicecloud.core.model.UpdateUserProfileRequest
import app.voicecloud.core.model.UserProfileDto
import app.voicecloud.core.model.WalletBalanceDto
import app.voicecloud.core.model.WalletTransactionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscoveryApi {
    @GET("discovery/rooms/live")
    suspend fun liveRooms(): Response<DataEnvelope<List<DiscoveryRoomDto>>>

    @GET("discovery/rooms/trending")
    suspend fun trendingRooms(): Response<DataEnvelope<List<DiscoveryRoomDto>>>

    @GET("discovery/rooms/popular")
    suspend fun popularRooms(): Response<DataEnvelope<List<DiscoveryRoomDto>>>

    @GET("discovery/users/popular")
    suspend fun popularUsers(): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @GET("discovery/users/online")
    suspend fun onlineUsers(): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @GET("discovery/users/suggested")
    suspend fun suggestedUsers(): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @GET("discovery/users/trending")
    suspend fun trendingUsers(): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @GET("discovery/hosts/trending")
    suspend fun trendingHosts(): Response<DataEnvelope<List<DiscoveryUserDto>>>
}

interface SearchApi {
    @GET("search")
    suspend fun search(@Query("q") query: String): Response<DataEnvelope<SearchResultsDto>>

    @GET("search/rooms")
    suspend fun searchRooms(@Query("q") query: String): Response<DataEnvelope<List<DiscoveryRoomDto>>>

    @GET("search/users")
    suspend fun searchUsers(@Query("q") query: String): Response<DataEnvelope<List<DiscoveryUserDto>>>
}

interface UsersApi {
    @GET("users/profile/me")
    suspend fun myProfile(): Response<DataEnvelope<UserProfileDto>>

    @GET("users/public/{username}")
    suspend fun publicProfile(@Path("username") username: String): Response<DataEnvelope<UserProfileDto>>

    @GET("users/{userId}/profile")
    suspend fun profile(@Path("userId") userId: String): Response<DataEnvelope<UserProfileDto>>

    @POST("users/{userId}/follow")
    suspend fun follow(@Path("userId") userId: String): Response<Unit>

    @DELETE("users/{userId}/follow")
    suspend fun unfollow(@Path("userId") userId: String): Response<Unit>

    @PATCH("users/profile")
    suspend fun updateProfile(@Body body: UpdateUserProfileRequest): Response<DataEnvelope<UserProfileDto>>

    @GET("users/{userId}/followers")
    suspend fun followers(@Path("userId") userId: String): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @GET("users/{userId}/following")
    suspend fun following(@Path("userId") userId: String): Response<DataEnvelope<List<DiscoveryUserDto>>>
}

interface ChatApi {
    @GET("chat/conversations")
    suspend fun conversations(): Response<DataEnvelope<List<ChatConversationDto>>>

    @GET("chat/conversations/{conversationId}/messages")
    suspend fun messages(@Path("conversationId") conversationId: String): Response<DataEnvelope<List<ChatMessageDto>>>

    @POST("chat/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body body: SendMessageRequest,
    ): Response<DataEnvelope<ChatMessageDto>>

    @POST("chat/conversations/{conversationId}/typing")
    suspend fun typing(@Path("conversationId") conversationId: String): Response<Unit>

    @POST("chat/conversations/{conversationId}/read")
    suspend fun markRead(@Path("conversationId") conversationId: String): Response<Unit>
}

data class SendMessageRequest(val text: String, val content: String? = null)

interface WalletApi {
    @GET("wallet/balance")
    suspend fun balance(): Response<DataEnvelope<WalletBalanceDto>>

    @GET("wallet/history")
    suspend fun history(): Response<DataEnvelope<List<WalletTransactionDto>>>

    @GET("wallet/packages")
    suspend fun packages(): Response<DataEnvelope<List<Any>>>
}

interface RoomsApi {
    @GET("rooms/{roomId}")
    suspend fun room(@Path("roomId") roomId: String): Response<DataEnvelope<RoomDetailDto>>

    @GET("rooms/mine")
    suspend fun myRooms(): Response<DataEnvelope<List<DiscoveryRoomDto>>>

    @GET("rooms/saved")
    suspend fun savedRooms(): Response<DataEnvelope<List<DiscoveryRoomDto>>>
}

interface RtcApi {
    @POST("rtc/rooms/join")
    suspend fun joinRoom(@Body body: RtcJoinRequest): Response<DataEnvelope<RtcJoinResponse>>

    @POST("rtc/token")
    suspend fun token(@Body body: RtcJoinRequest): Response<DataEnvelope<RtcJoinResponse>>
}

interface SessionApi {
    @GET("auth/sessions")
    suspend fun sessions(): Response<DataEnvelope<List<AuthSessionDto>>>

    @DELETE("auth/sessions/{sessionId}")
    suspend fun revokeSession(@Path("sessionId") sessionId: String): Response<Unit>

    @GET("auth/sessions/{sessionId}")
    suspend fun session(@Path("sessionId") sessionId: String): Response<DataEnvelope<AuthSessionDto>>

    @GET("auth/devices")
    suspend fun devices(): Response<DataEnvelope<List<AuthDeviceDto>>>

    @GET("auth/devices/{deviceId}")
    suspend fun device(@Path("deviceId") deviceId: String): Response<DataEnvelope<AuthDeviceDto>>

    @DELETE("auth/devices/{deviceId}")
    suspend fun revokeDevice(@Path("deviceId") deviceId: String): Response<Unit>

    @GET("auth/history")
    suspend fun loginHistory(): Response<DataEnvelope<List<AuthSessionDto>>>
}

interface CreatorAccessApi {
    @POST("creator-access/applications")
    suspend fun apply(@Body body: CreatorAccessApplicationRequest): Response<Unit>
}

interface NotificationsApi {
    @GET("notifications")
    suspend fun notifications(): Response<DataEnvelope<List<NotificationDto>>>

    @GET("notifications/unread-count")
    suspend fun unreadCount(): Response<DataEnvelope<NotificationUnreadDto>>

    @POST("notifications/read-all")
    suspend fun readAll(): Response<Unit>

    @POST("notifications/{notificationId}/read")
    suspend fun markRead(@Path("notificationId") notificationId: String): Response<Unit>
}
