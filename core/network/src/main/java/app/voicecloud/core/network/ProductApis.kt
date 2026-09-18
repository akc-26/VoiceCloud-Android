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
import app.voicecloud.core.model.FollowStatsDto
import app.voicecloud.core.model.ReferralSummaryDto
import app.voicecloud.core.model.TaskItemDto
import app.voicecloud.core.model.AchievementItemDto
import app.voicecloud.core.model.CreateClubRequest
import app.voicecloud.core.model.CreateConversationRequest
import app.voicecloud.core.model.ClubDto
import app.voicecloud.core.model.FriendRequestBody
import app.voicecloud.core.model.FriendRequestDto
import app.voicecloud.core.model.ProfileVisitorDto
import app.voicecloud.core.model.ProfileVisitorStatsDto
import app.voicecloud.core.model.RoomActivityItemDto
import app.voicecloud.core.model.ScheduledRoomDto
import app.voicecloud.core.model.UpdateClubRequest
import app.voicecloud.core.model.UpdateUserProfileRequest
import app.voicecloud.core.model.UpdateUserSettingsRequest
import app.voicecloud.core.model.UserSettingsDto
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

    @GET("users/{userId}/follow-stats")
    suspend fun followStats(@Path("userId") userId: String): Response<DataEnvelope<FollowStatsDto>>
}

interface BlocksApi {
    @GET("blocks")
    suspend fun blockedUsers(): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @POST("blocks/{userId}")
    suspend fun block(@Path("userId") userId: String): Response<Unit>

    @DELETE("blocks/{userId}")
    suspend fun unblock(@Path("userId") userId: String): Response<Unit>
}

interface RankingsApi {
    @GET("rankings/leaderboard/users")
    suspend fun userLeaderboard(): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @GET("rankings/trending/users")
    suspend fun trendingUsers(): Response<DataEnvelope<List<DiscoveryUserDto>>>
}

interface ReferralsApi {
    @GET("referrals/summary")
    suspend fun summary(): Response<DataEnvelope<ReferralSummaryDto>>

    @GET("referrals/history")
    suspend fun history(): Response<DataEnvelope<List<Any>>>
}

interface TasksAchievementsApi {
    @GET("tasks-achievements/tasks")
    suspend fun tasks(): Response<DataEnvelope<List<TaskItemDto>>>

    @GET("tasks-achievements/achievements")
    suspend fun achievements(): Response<DataEnvelope<List<AchievementItemDto>>>
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

    @POST("chat/conversations")
    suspend fun createConversation(@Body body: CreateConversationRequest): Response<DataEnvelope<ChatConversationDto>>
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

interface FriendsApi {
    @GET("users/friends")
    suspend fun friends(): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @GET("users/friends/requests/pending")
    suspend fun pendingRequests(): Response<DataEnvelope<List<FriendRequestDto>>>

    @GET("users/friends/suggested")
    suspend fun suggested(): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @POST("users/friends/request")
    suspend fun sendRequest(@Body body: FriendRequestBody): Response<Unit>

    @POST("users/friends/request/{requestId}/accept")
    suspend fun acceptRequest(@Path("requestId") requestId: String): Response<Unit>

    @POST("users/friends/request/{requestId}/reject")
    suspend fun rejectRequest(@Path("requestId") requestId: String): Response<Unit>

    @DELETE("users/friends/request/{requestId}/cancel")
    suspend fun cancelRequest(@Path("requestId") requestId: String): Response<Unit>

    @DELETE("users/friends/{userId}")
    suspend fun removeFriend(@Path("userId") userId: String): Response<Unit>
}

interface VisitorsApi {
    @GET("users/visitors")
    suspend fun visitors(): Response<DataEnvelope<List<ProfileVisitorDto>>>

    @GET("users/visitors/stats")
    suspend fun stats(): Response<DataEnvelope<ProfileVisitorStatsDto>>

    @POST("users/visitors/{userId}")
    suspend fun recordVisit(@Path("userId") userId: String): Response<Unit>
}

interface UserSettingsApi {
    @GET("users/settings")
    suspend fun settings(): Response<DataEnvelope<UserSettingsDto>>

    @PATCH("users/settings")
    suspend fun updateSettings(@Body body: UpdateUserSettingsRequest): Response<DataEnvelope<UserSettingsDto>>
}

interface RoomActivityApi {
    @GET("room-activity/me")
    suspend fun myActivity(): Response<DataEnvelope<List<RoomActivityItemDto>>>
}

interface ClubsApi {
    @GET("clubs")
    suspend fun clubs(): Response<DataEnvelope<List<ClubDto>>>

    @POST("clubs")
    suspend fun createClub(@Body body: CreateClubRequest): Response<DataEnvelope<ClubDto>>

    @GET("clubs/{clubId}")
    suspend fun club(@Path("clubId") clubId: String): Response<DataEnvelope<ClubDto>>

    @PATCH("clubs/{clubId}")
    suspend fun updateClub(@Path("clubId") clubId: String, @Body body: UpdateClubRequest): Response<DataEnvelope<ClubDto>>

    @POST("clubs/{clubId}/join")
    suspend fun join(@Path("clubId") clubId: String): Response<Unit>

    @POST("clubs/{clubId}/leave")
    suspend fun leave(@Path("clubId") clubId: String): Response<Unit>

    @GET("clubs/{clubId}/members")
    suspend fun members(@Path("clubId") clubId: String): Response<DataEnvelope<List<DiscoveryUserDto>>>

    @GET("clubs/{clubId}/scheduled-rooms")
    suspend fun scheduledRooms(@Path("clubId") clubId: String): Response<DataEnvelope<List<ScheduledRoomDto>>>
}

interface ScheduledRoomsApi {
    @GET("scheduled-rooms")
    suspend fun events(): Response<DataEnvelope<List<ScheduledRoomDto>>>

    @GET("scheduled-rooms/{eventId}")
    suspend fun event(@Path("eventId") eventId: String): Response<DataEnvelope<ScheduledRoomDto>>
}
