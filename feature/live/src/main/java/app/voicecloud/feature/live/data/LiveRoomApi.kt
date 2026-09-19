package app.voicecloud.feature.live.data

import app.voicecloud.feature.live.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LiveRoomApi {
    @GET("rooms/{roomId}") suspend fun room(@Path("roomId") roomId: String): LiveRoomDetail

    @POST("rtc/rooms/join") suspend fun join(@Body body: JoinRoomBody): RtcJoinResult
    @POST("rtc/rooms/rejoin") suspend fun rejoin(@Body body: RejoinRoomBody): RtcJoinResult
    @POST("rtc/rooms/leave") suspend fun leave(@Body body: LeaveRoomBody): Map<String, Any?>
    @GET("rtc/rooms/{roomId}/participants") suspend fun participants(@Path("roomId") roomId: String): RtcParticipantsResult
    @POST("rtc/rooms/{roomId}/raise-hand") suspend fun raiseHand(@Path("roomId") roomId: String, @Body body: RaiseHandBody): Map<String, Any?>
    @POST("rtc/rooms/{roomId}/cancel-raise-hand") suspend fun cancelRaiseHand(@Path("roomId") roomId: String): Map<String, Any?>

    @POST("chat/conversations") suspend fun roomConversation(@Body body: CreateRoomConversationBody): RoomConversation
    @GET("chat/conversations/{conversationId}/messages") suspend fun messages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): RoomMessagePage
    @POST("chat/conversations/{conversationId}/messages") suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body body: SendRoomMessageBody,
    ): RoomChatMessage
    @POST("chat/messages/{messageId}/reactions") suspend fun messageReaction(
        @Path("messageId") messageId: String,
        @Body body: MessageReactionBody,
    ): Map<String, Any?>

    @GET("rooms/saved/{roomId}/status") suspend fun savedStatus(@Path("roomId") roomId: String): SavedRoomStatus
    @POST("rooms/saved/{roomId}") suspend fun saveRoom(@Path("roomId") roomId: String): SavedRoomStatus
    @DELETE("rooms/saved/{roomId}") suspend fun unsaveRoom(@Path("roomId") roomId: String): SavedRoomStatus

    @POST("room-activity/{roomId}/join") suspend fun activityJoin(@Path("roomId") roomId: String, @Body body: ActivityJoinBody): Map<String, Any?>
    @POST("room-activity/{roomId}/leave") suspend fun activityLeave(@Path("roomId") roomId: String): Map<String, Any?>

    @GET("gifts/catalog") suspend fun giftCatalog(): List<GiftCatalogItem>
    @POST("gifts/send") suspend fun sendGift(@Body body: SendGiftBody): Map<String, Any?>
}
