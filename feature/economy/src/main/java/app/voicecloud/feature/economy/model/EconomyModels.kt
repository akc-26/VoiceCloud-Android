package app.voicecloud.feature.economy.model

data class EconomyUiState(
    val loading: Boolean = false,
    val busy: Boolean = false,
    val title: String = "Economy & Progression",
    val payload: Any? = null,
    val error: String? = null,
    val notice: String? = null,
)

enum class EconomySection(val label: String, val subtitle: String) {
    WALLET("Wallet", "Balance, coin packages, transactions and purchases"),
    VIP("VIP", "Membership status and Google Play-backed plans"),
    STORE("Store", "Catalogue, inventory and equipped items"),
    GIFTS("Gifts", "Available gifts and gift history"),
    TASKS("Tasks", "Tasks, progress and claimable rewards"),
    ACHIEVEMENTS("Achievements", "Achievement progress and rewards"),
    PROGRESSION("XP & Check-in", "XP, daily check-in and streaks"),
    RANKINGS("Rankings", "Users, creators, hosts, rooms, gifts and VIP"),
    TICKETS("Tickets", "Scheduled-room tickets and purchase history"),
    REFERRALS("Referrals", "Referral code, history and rewards"),
}

val economyProfileOrder: List<EconomySection> = listOf(
    EconomySection.WALLET,
    EconomySection.VIP,
    EconomySection.STORE,
    EconomySection.GIFTS,
    EconomySection.TASKS,
    EconomySection.ACHIEVEMENTS,
    EconomySection.PROGRESSION,
    EconomySection.RANKINGS,
    EconomySection.TICKETS,
    EconomySection.REFERRALS,
)
