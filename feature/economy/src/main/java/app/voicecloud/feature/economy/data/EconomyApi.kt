package app.voicecloud.feature.economy.data

import retrofit2.http.*

typealias JsonMap = Map<String, Any?>
interface EconomyApi {
    @GET("wallet/summary") suspend fun walletSummary(): JsonMap
    @GET("wallet/transactions") suspend fun walletTransactions(@Query("page") page:Int=1,@Query("limit") limit:Int=50): JsonMap
    @GET("wallet/packages") suspend fun walletPackages(): List<JsonMap>
    @GET("wallet/purchases/history") suspend fun purchaseHistory(@Query("page") page:Int=1,@Query("limit") limit:Int=50): JsonMap
    @POST("wallet/purchase/validate") suspend fun validatePurchase(@Body body: JsonMap): JsonMap

    @GET("android/billing/vip/catalog") suspend fun androidVipCatalog(): JsonMap
    @POST("android/billing/vip/verify") suspend fun verifyAndroidVip(@Body body: JsonMap): JsonMap
    @GET("vip/membership") suspend fun vipMembership(): JsonMap
    @GET("vip/history") suspend fun vipHistory(): JsonMap
    @POST("vip/cancel") suspend fun cancelVip(): JsonMap

    @GET("referrals/summary") suspend fun referralSummary(): JsonMap
    @GET("referrals/history") suspend fun referralHistory(): JsonMap
    @GET("referrals/rewards") suspend fun referralRewards(): JsonMap
    @POST("referrals/generate-code") suspend fun generateReferralCode(): JsonMap
    @POST("referrals/apply-code") suspend fun applyReferralCode(@Body body: JsonMap): JsonMap

    @GET("gifts/catalog") suspend fun giftsCatalog(): JsonMap
    @GET("gifts/history") suspend fun giftHistory(@Query("page") page:Int=1,@Query("limit") limit:Int=50): JsonMap

    @GET("store/catalog") suspend fun storeCatalog(): JsonMap
    @GET("store/inventory") suspend fun inventory(): JsonMap
    @GET("store/inventory/equipped") suspend fun equipped(): JsonMap
    @POST("store/purchase") suspend fun purchaseStoreItem(@Body body: JsonMap): JsonMap
    @POST("store/equip") suspend fun equip(@Body body: JsonMap): JsonMap
    @POST("store/unequip") suspend fun unequip(@Body body: JsonMap): JsonMap

    @GET("tasks-achievements/tasks") suspend fun tasks(): JsonMap
    @POST("tasks-achievements/tasks/{taskId}/claim") suspend fun claimTask(@Path("taskId") taskId:String): JsonMap
    @GET("tasks-achievements/achievements") suspend fun achievements(): JsonMap
    @GET("tasks-achievements/xp/progress") suspend fun xpProgress(): JsonMap
    @GET("tasks-achievements/check-in") suspend fun checkIn(): JsonMap
    @POST("tasks-achievements/check-in/claim") suspend fun claimCheckIn(): JsonMap
    @GET("tasks-achievements/streaks") suspend fun streaks(): JsonMap

    @GET("rankings/leaderboard/users") suspend fun userRankings(): JsonMap
    @GET("rankings/leaderboard/creators") suspend fun creatorRankings(): JsonMap
    @GET("rankings/leaderboard/hosts") suspend fun hostRankings(): JsonMap
    @GET("rankings/leaderboard/rooms") suspend fun roomRankings(): JsonMap
    @GET("rankings/leaderboard/gift-senders") suspend fun giftSenderRankings(): JsonMap
    @GET("rankings/leaderboard/gift-receivers") suspend fun giftReceiverRankings(): JsonMap
    @GET("rankings/leaderboard/vip") suspend fun vipRankings(): JsonMap

    @GET("scheduled-rooms/my-tickets") suspend fun myTickets(): JsonMap
    @POST("scheduled-rooms/{roomId}/buy-ticket") suspend fun buyTicket(@Path("roomId") roomId:String): JsonMap
}
