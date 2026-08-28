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
    WALLET("Wallet", "Balance & Transactions"),
    VIP("VIP", "Membership & Benefits"),
    STORE("Store", "Frames, Effects & More"),
    GIFTS("Gifts", "Send, Receive & Celebrate"),
    TASKS("Tasks", "Complete & Claim Rewards"),
    ACHIEVEMENTS("Achievements", "Milestones & Rewards"),
    PROGRESSION("XP & Check-In", "Level, Streak & Daily Reward"),
    RANKINGS("Rankings", "Top Voices & Rooms"),
    TICKETS("Tickets", "Upcoming Access"),
    REFERRALS("Referrals", "Invite & Earn"),
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
