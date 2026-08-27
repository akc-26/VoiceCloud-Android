package app.voicecloud.feature.engagement.data

import app.voicecloud.feature.engagement.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EngagementApi {
    @GET("clubs")
    suspend fun communities(@Query("search") search: String? = null, @Query("category") category: String? = null, @Query("page") page: Int = 1, @Query("limit") limit: Int = 30): Page<Community>

    @GET("clubs/{id}")
    suspend fun community(@Path("id") id: String): Community

    @POST("clubs")
    suspend fun createCommunity(@Body body: CommunityInput): Community

    @PATCH("clubs/{id}")
    suspend fun updateCommunity(@Path("id") id: String, @Body body: CommunityInput): Community

    @DELETE("clubs/{id}")
    suspend fun deleteCommunity(@Path("id") id: String): ApiMessage

    @POST("clubs/{id}/join")
    suspend fun joinCommunity(@Path("id") id: String, @Body body: JoinCommunityBody): CommunityMember

    @POST("clubs/{id}/leave")
    suspend fun leaveCommunity(@Path("id") id: String): ApiMessage

    @GET("clubs/{id}/membership/me")
    suspend fun membership(@Path("id") id: String): CommunityMembership

    @GET("clubs/{id}/members")
    suspend fun communityMembers(@Path("id") id: String, @Query("page") page: Int = 1, @Query("limit") limit: Int = 50, @Query("search") search: String? = null): Page<CommunityMember>

    @GET("clubs/{id}/scheduled-rooms")
    suspend fun communityEvents(@Path("id") id: String, @Query("page") page: Int = 1, @Query("limit") limit: Int = 30): Page<ScheduledEvent>

    @POST("clubs/{id}/invite-code")
    suspend fun rotateInviteCode(@Path("id") id: String): InviteCodeResult

    @PATCH("clubs/{id}/members/{userId}")
    suspend fun updateCommunityMemberRole(@Path("id") id: String, @Path("userId") userId: String, @Body body: UpdateCommunityRoleBody): CommunityMember

    @DELETE("clubs/{id}/members/{userId}")
    suspend fun removeCommunityMember(@Path("id") id: String, @Path("userId") userId: String): ApiMessage

    @GET("scheduled-rooms")
    suspend fun events(@Query("search") search: String? = null, @Query("page") page: Int = 1, @Query("limit") limit: Int = 30, @Query("status") status: String = "SCHEDULED"): Page<ScheduledEvent>

    @GET("scheduled-rooms/{id}")
    suspend fun event(@Path("id") id: String): ScheduledEvent

    @POST("scheduled-rooms/{id}/reminder")
    suspend fun registerReminder(@Path("id") id: String, @Body body: ReminderSettings = ReminderSettings()): ReminderResult

    @GET("chat/conversations")
    suspend fun conversations(@Query("page") page: Int = 1, @Query("limit") limit: Int = 50, @Query("search") search: String? = null, @Query("type") type: String? = null): ConversationList

    @GET("chat/conversations/{id}")
    suspend fun conversation(@Path("id") id: String): Conversation

    @POST("chat/conversations")
    suspend fun createConversation(@Body body: CreateConversationBody): Conversation

    @DELETE("chat/conversations/{id}")
    suspend fun deleteConversation(@Path("id") id: String): ApiMessage

    @GET("chat/conversations/{id}/messages")
    suspend fun messages(@Path("id") id: String, @Query("page") page: Int = 1, @Query("limit") limit: Int = 100): MessagePage

    @POST("chat/conversations/{id}/messages")
    suspend fun sendMessage(@Path("id") id: String, @Body body: SendMessageBody): ChatMessage

    @POST("chat/conversations/{id}/read")
    suspend fun markConversationRead(@Path("id") id: String, @Body body: ReadConversationBody): ApiMessage

    @GET("notifications")
    suspend fun notifications(@Query("page") page: Int = 1, @Query("limit") limit: Int = 40, @Query("type") type: String? = null, @Query("isRead") isRead: Boolean? = null): Page<VoiceCloudNotification>

    @GET("notifications/unread-count")
    suspend fun unreadCount(): UnreadCount

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): VoiceCloudNotification

    @PATCH("notifications/read-all")
    suspend fun markAllNotificationsRead(): ReadAllResult

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): SuccessResult
}
