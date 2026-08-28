package app.voicecloud.feature.economy.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EconomyRepository @Inject constructor(private val api: EconomyApi) {
    /**
     * Read-side economy pages are independently fault-tolerant. One unavailable endpoint must not
     * blank an entire Wallet/VIP/Store page; available server-authoritative sections still render.
     */
    suspend fun load(section: String): Any = when (section) {
        "WALLET" -> mapOf(
            "summary" to safe { api.walletSummary() },
            "transactions" to safe { api.walletTransactions() },
            "packages" to safe { api.walletPackages() },
            "purchase history" to safe { api.purchaseHistory() },
        )
        "VIP" -> mapOf(
            "membership" to safe { api.vipMembership() },
            "plans" to safe { api.androidVipCatalog() },
            "history" to safe { api.vipHistory() },
        )
        "REFERRALS" -> mapOf(
            "summary" to safe { api.referralSummary() },
            "history" to safe { api.referralHistory() },
            "rewards" to safe { api.referralRewards() },
        )
        "GIFTS" -> mapOf(
            "catalog" to safe { api.giftsCatalog() },
            "history" to safe { api.giftHistory() },
        )
        "STORE" -> mapOf(
            "catalog" to safe { api.storeCatalog() },
            "inventory" to safe { api.inventory() },
            "equipped" to safe { api.equipped() },
        )
        "TASKS" -> safe { api.tasks() }
        "ACHIEVEMENTS" -> safe { api.achievements() }
        "PROGRESSION" -> mapOf(
            "xp" to safe { api.xpProgress() },
            "daily check-in" to safe { api.checkIn() },
            "streaks" to safe { api.streaks() },
        )
        "RANKINGS" -> mapOf(
            "users" to safe { api.userRankings() },
            "creators" to safe { api.creatorRankings() },
            "hosts" to safe { api.hostRankings() },
            "rooms" to safe { api.roomRankings() },
            "gift senders" to safe { api.giftSenderRankings() },
            "gift receivers" to safe { api.giftReceiverRankings() },
            "vip" to safe { api.vipRankings() },
        )
        "TICKETS" -> safe { api.myTickets() }
        else -> emptyMap<String, Any>()
    }

    suspend fun initiateGooglePlayWallet(packageId: String) = api.initiatePlayPurchase(
        mapOf("packageId" to packageId),
    )

    suspend fun initiateHostedWallet(packageId: String): HostedCheckout = api.initiateWalletWebCheckout(
        mapOf("packageId" to packageId),
    ).toHostedCheckout()

    suspend fun completeHostedWallet(body: JsonMap) = api.completeWalletWebCheckout(body)
    suspend fun cancelHostedWallet(checkoutSessionId: String) = api.cancelWalletWebCheckout(
        mapOf("checkoutSessionId" to checkoutSessionId),
    )

    suspend fun initiateHostedVip(tierId: String, cycle: String?): HostedCheckout = api.initiateVipWebCheckout(
        buildMap<String, Any?> {
            put("tierId", tierId)
            if (!cycle.isNullOrBlank()) put("cycle", cycle.uppercase())
        },
    ).toHostedCheckout()

    suspend fun completeHostedVip(body: JsonMap) = api.completeVipWebCheckout(body)
    suspend fun cancelHostedVip(checkoutSessionId: String) = api.cancelVipWebCheckout(
        mapOf("checkoutSessionId" to checkoutSessionId),
    )

    suspend fun validateCoinPurchase(productId: String, token: String, orderId: String?) = api.validatePurchase(
        mapOf("provider" to "GOOGLE_PLAY", "productId" to productId, "purchaseToken" to token, "orderId" to orderId)
    )
    suspend fun verifyVip(productId: String, token: String) = api.verifyAndroidVip(mapOf("productId" to productId, "purchaseToken" to token))
    suspend fun claimTask(id: String) = api.claimTask(id)
    suspend fun claimCheckIn() = api.claimCheckIn()
    suspend fun buyTicket(id: String) = api.buyTicket(id)
    suspend fun equip(id: String) = api.equip(mapOf("itemId" to id))
    suspend fun unequip(id: String) = api.unequip(mapOf("itemId" to id))
    suspend fun buyItem(id: String) = api.purchaseStoreItem(mapOf("itemId" to id))

    data class HostedCheckout(val checkoutSessionId: String, val checkoutUrl: String)

    private fun JsonMap.toHostedCheckout(): HostedCheckout {
        val checkoutSessionId = findString("checkoutSessionId", "checkoutId", "id")
        val checkoutUrl = findString("checkoutUrl", "url")
        require(checkoutSessionId.isNotBlank()) { "VoiceCloud did not return a checkout session." }
        require(checkoutUrl.startsWith("https://")) { "VoiceCloud did not return a secure checkout URL." }
        return HostedCheckout(checkoutSessionId, checkoutUrl)
    }

    private fun Any?.findString(vararg keys: String): String {
        val map = this as? Map<*, *> ?: return ""
        keys.forEach { key -> map[key]?.toString()?.takeIf { it.isNotBlank() }?.let { return it } }
        map.values.forEach { value ->
            if (value is Map<*, *>) value.findString(*keys).takeIf { it.isNotBlank() }?.let { return it }
        }
        return ""
    }

    private suspend fun safe(block: suspend () -> Any): Any = runCatching { block() }.getOrElse {
        mapOf("status" to "Unavailable", "message" to "This information could not be loaded right now.")
    }
}
