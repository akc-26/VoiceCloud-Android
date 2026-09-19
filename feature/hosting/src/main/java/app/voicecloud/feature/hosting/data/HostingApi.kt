package app.voicecloud.feature.hosting.data

import app.voicecloud.feature.hosting.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HostingApi {
    @GET("search") suspend fun searchUsers(@Query("q") query: String, @Query("type") type: String = "users", @Query("page") page: Int = 1, @Query("limit") limit: Int = 12): InviteSearchResponse
    @GET("hosts/profile") suspend fun hostProfile(): HostProfile
    @GET("hosts/eligibility") suspend fun hostEligibility(): HostEligibility
    @POST("hosts/apply") suspend fun applyForHost(@Body body: Map<String, @JvmSuppressWildcards Any?>): Any
    @GET("hosts/progression") suspend fun hostProgression(): Any
    @GET("hosts/verification/assets") suspend fun verificationAssets(): Any
    @Multipart @POST("hosts/verification/government-id") suspend fun uploadGovernmentId(@Part file: MultipartBody.Part): Any
    @Multipart @POST("hosts/verification/profile-photo") suspend fun uploadProfilePhoto(@Part file: MultipartBody.Part): Any
    @Multipart @POST("hosts/verification/documents") suspend fun uploadVerificationDocument(@Part file: MultipartBody.Part): Any
    @PUT("hosts/verification/assets/{assetId}/replacement") suspend fun replaceVerificationAsset(
        @Path("assetId") assetId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): Any

    @GET("rooms/mine") suspend fun myRooms(@Query("page") page: Int = 1, @Query("limit") limit: Int = 100): HostRoomPage
    @GET("rooms/{roomId}") suspend fun room(@Path("roomId") roomId: String): HostRoom
    @POST("rooms") suspend fun createRoom(@Body body: RoomEditorInput): HostRoom
    @PATCH("rooms/{roomId}") suspend fun updateRoom(@Path("roomId") roomId: String, @Body body: RoomEditorInput): HostRoom
    @DELETE("rooms/{roomId}") suspend fun deleteRoom(@Path("roomId") roomId: String): Map<String, Any?>
    @POST("rooms/{roomId}/start") suspend fun startRoom(@Path("roomId") roomId: String): HostRoom
    @POST("rooms/{roomId}/pause") suspend fun pauseRoom(@Path("roomId") roomId: String): HostRoom
    @POST("rooms/{roomId}/resume") suspend fun resumeRoom(@Path("roomId") roomId: String): HostRoom
    @POST("rooms/{roomId}/end") suspend fun endRoom(@Path("roomId") roomId: String): HostRoom

    @GET("scheduled-rooms") suspend fun schedules(@Query("hostId") hostId: String, @Query("page") page: Int = 1, @Query("limit") limit: Int = 100): ScheduledRoomPage
    @GET("scheduled-rooms/{scheduleId}") suspend fun schedule(@Path("scheduleId") scheduleId: String): ScheduledHostRoom
    @POST("scheduled-rooms") suspend fun createSchedule(@Body body: ScheduledRoomInput): ScheduledHostRoom
    @PATCH("scheduled-rooms/{scheduleId}") suspend fun updateSchedule(@Path("scheduleId") scheduleId: String, @Body body: ScheduledRoomInput): ScheduledHostRoom
    @DELETE("scheduled-rooms/{scheduleId}") suspend fun deleteSchedule(@Path("scheduleId") scheduleId: String): Map<String, Any?>

    @POST("rtc/rooms/join") suspend fun joinHostRoom(@Body body: JoinHostRoomBody): HostRtcJoinResult
    @POST("rtc/rooms/leave") suspend fun leaveHostRoom(@Body body: LeaveHostRoomBody): Map<String, Any?>
    @GET("rtc/rooms/{roomId}/stage") suspend fun stage(@Path("roomId") roomId: String): RoomStageState
    @POST("rtc/rooms/{roomId}/approve-speaker") suspend fun approveSpeaker(@Path("roomId") roomId: String, @Body body: SpeakerActionBody): Map<String, Any?>
    @POST("rtc/rooms/{roomId}/reject-speaker") suspend fun rejectSpeaker(@Path("roomId") roomId: String, @Body body: SpeakerActionBody): Map<String, Any?>
    @POST("rtc/rooms/{roomId}/invite-speaker") suspend fun inviteSpeaker(@Path("roomId") roomId: String, @Body body: SpeakerActionBody): Map<String, Any?>
    @POST("rtc/rooms/{roomId}/remove-speaker") suspend fun removeSpeaker(@Path("roomId") roomId: String, @Body body: SpeakerActionBody): Map<String, Any?>
    @POST("rtc/rooms/{roomId}/mute-user") suspend fun muteSpeaker(@Path("roomId") roomId: String, @Body body: MuteUserBody): Map<String, Any?>
    @POST("rtc/rooms/{roomId}/lock-seat") suspend fun lockSeat(@Path("roomId") roomId: String, @Body body: LockSeatBody): Map<String, Any?>

    @GET("polls/rooms/{roomId}") suspend fun polls(@Path("roomId") roomId: String): List<RoomPoll>
    @POST("polls") suspend fun createPoll(@Body body: CreatePollBody): RoomPoll
    @POST("polls/{pollId}/start") suspend fun startPoll(@Path("pollId") pollId: String): RoomPoll
    @POST("polls/{pollId}/stop") suspend fun stopPoll(@Path("pollId") pollId: String): RoomPoll
    @DELETE("polls/{pollId}") suspend fun deletePoll(@Path("pollId") pollId: String): Map<String, Any?>

    @GET("quizzes/rooms/{roomId}/active") suspend fun activeQuiz(@Path("roomId") roomId: String): RoomQuiz?
    @POST("quizzes") suspend fun createQuiz(@Body body: CreateQuizBody): RoomQuiz
    @POST("quizzes/{quizId}/start") suspend fun startQuiz(@Path("quizId") quizId: String): RoomQuiz
    @POST("quizzes/{quizId}/next-round") suspend fun nextQuizRound(@Path("quizId") quizId: String): RoomQuiz
    @POST("quizzes/{quizId}/stop") suspend fun stopQuiz(@Path("quizId") quizId: String): RoomQuiz
}
