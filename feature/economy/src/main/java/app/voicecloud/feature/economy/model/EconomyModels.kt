package app.voicecloud.feature.economy.model

data class EconomyUiState(val loading:Boolean=false,val busy:Boolean=false,val title:String="Economy & Progression",val payload:Any?=null,val error:String?=null,val notice:String?=null)
enum class EconomySection(val label:String,val subtitle:String){
 WALLET("Wallet","Balance, transactions, coin packages and purchase history"), VIP("VIP","Google Play-backed membership authority"), REFERRALS("Referrals","Codes, history and rewards"), GIFTS("Gifts","Gift catalogue and history"), STORE("Store","Catalogue, inventory and equipped items"), TASKS("Tasks","Tasks and claimable rewards"), ACHIEVEMENTS("Achievements","Achievement progress"), PROGRESSION("XP & Check-in","XP, daily check-in and streaks"), RANKINGS("Rankings","Users, creators, hosts, rooms, gifts and VIP"), TICKETS("Tickets","Scheduled-room ticket purchases and history")
}
