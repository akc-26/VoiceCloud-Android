package app.voicecloud.feature.economy.data
import javax.inject.Inject; import javax.inject.Singleton
@Singleton class EconomyRepository @Inject constructor(private val api:EconomyApi){
 suspend fun load(section:String):Any = when(section){
  "WALLET" -> mapOf("summary" to api.walletSummary(),"transactions" to api.walletTransactions(),"packages" to api.walletPackages(),"purchases" to api.purchaseHistory())
  "VIP" -> mapOf("catalog" to api.androidVipCatalog(),"membership" to api.vipMembership(),"history" to api.vipHistory())
  "REFERRALS" -> mapOf("summary" to api.referralSummary(),"history" to api.referralHistory(),"rewards" to api.referralRewards())
  "GIFTS" -> mapOf("catalog" to api.giftsCatalog(),"history" to api.giftHistory())
  "STORE" -> mapOf("catalog" to api.storeCatalog(),"inventory" to api.inventory(),"equipped" to api.equipped())
  "TASKS" -> api.tasks(); "ACHIEVEMENTS" -> api.achievements()
  "PROGRESSION" -> mapOf("xp" to api.xpProgress(),"checkIn" to api.checkIn(),"streaks" to api.streaks())
  "RANKINGS" -> mapOf("users" to api.userRankings(),"creators" to api.creatorRankings(),"hosts" to api.hostRankings(),"rooms" to api.roomRankings(),"giftSenders" to api.giftSenderRankings(),"giftReceivers" to api.giftReceiverRankings(),"vip" to api.vipRankings())
  "TICKETS" -> api.myTickets(); else -> emptyMap<String,Any>()
 }
 suspend fun validateCoinPurchase(productId:String,token:String,orderId:String?)=api.validatePurchase(mapOf("provider" to "GOOGLE_PLAY","productId" to productId,"purchaseToken" to token,"orderId" to orderId))
 suspend fun verifyVip(productId:String,token:String)=api.verifyAndroidVip(mapOf("productId" to productId,"purchaseToken" to token))
 suspend fun claimTask(id:String)=api.claimTask(id); suspend fun claimCheckIn()=api.claimCheckIn(); suspend fun buyTicket(id:String)=api.buyTicket(id)
 suspend fun equip(id:String)=api.equip(mapOf("itemId" to id)); suspend fun unequip(id:String)=api.unequip(mapOf("itemId" to id)); suspend fun buyItem(id:String)=api.purchaseStoreItem(mapOf("itemId" to id))
}
