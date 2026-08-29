package app.voicecloud.feature.creator.data

import app.voicecloud.feature.creator.model.*
import java.text.DecimalFormat
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatorRepository @Inject constructor(private val api: CreatorApi) {
    suspend fun maintenance(): CreatorMaintenance {
        val raw = unwrap(api.maintenance(), "maintenance", "config")
        return CreatorMaintenance(
            enabled = raw.bool(false, "enabled", "maintenanceMode", "isMaintenanceMode", "isEnabled"),
            message = raw.string("message", "maintenanceMessage").takeIf(String::isNotBlank),
        )
    }

    suspend fun dashboard(): CreatorDashboard {
        val raw = unwrap(api.dashboard(), "dashboard", "data", "summary")
        val metrics = dashboardMetrics(raw)
        return CreatorDashboard(
            metrics = metrics,
            generatedAt = raw.string("generatedAt", "updatedAt", "timestamp").takeIf(String::isNotBlank),
        )
    }

    suspend fun profile(): CreatorProfile = profileFrom(unwrap(api.profile(), "profile", "user", "data"))

    suspend fun updateProfile(bio: String?, country: String?, interests: List<String>): CreatorProfile {
        api.updateProfile(mapOf(
            "bio" to bio?.trim()?.takeIf(String::isNotBlank),
            "country" to country?.trim()?.takeIf(String::isNotBlank),
            "preferredLanguage" to "en",
            "interests" to interests.map(String::trim).filter(String::isNotBlank).distinct().take(20),
        ).filterValues { it != null })
        return profile()
    }

    suspend fun settings(): CreatorSettings {
        val raw = unwrap(api.settings(), "settings", "data")
        val notifications = raw.map("notificationPreferences", "notifications")
        return CreatorSettings(
            notifications = CreatorNotificationPreferences(
                email = notifications.bool(false, "email"),
                push = notifications.bool(true, "push"),
                inApp = notifications.bool(true, "inApp", "in_app"),
                sound = notifications.bool(true, "sound"),
            ),
            language = raw.string("language").ifBlank { "en" },
            timezone = raw.string("timezone", "timeZone"),
        )
    }

    suspend fun updateSettings(value: CreatorSettings): CreatorSettings {
        api.updateSettings(mapOf(
            "notificationPreferences" to mapOf(
                "email" to value.notifications.email,
                "push" to value.notifications.push,
                "inApp" to value.notifications.inApp,
                "sound" to value.notifications.sound,
            ),
            "language" to "en",
            "timezone" to value.timezone.trim().takeIf(String::isNotBlank),
        ).filterValues { it != null })
        return settings()
    }

    suspend fun cmsPages(): List<CreatorCmsPageSummary> = items(api.cmsPages(), "pages", "items", "data")
        .mapNotNull(::cmsSummaryFrom)
        .filter { it.slug.isNotBlank() && it.title.isNotBlank() }
        .distinctBy { it.slug.lowercase() }
        .sortedWith(compareBy<CreatorCmsPageSummary> { it.sortOrder }.thenBy { it.title.lowercase() })

    suspend fun cmsPage(slug: String): CreatorCmsPage {
        require(slug.trim().isNotBlank()) { "Creator content is unavailable." }
        val raw = unwrap(api.cmsPage(slug.trim()), "page", "data")
        return CreatorCmsPage(
            slug = raw.string("slug", "key").ifBlank { slug.trim() },
            title = raw.string("title", "name").ifBlank { "Creator information" },
            content = raw.string("content", "body", "html", "description"),
            updatedAt = raw.string("updatedAt", "publishedAt").takeIf(String::isNotBlank),
        )
    }

    suspend fun contact(name: String, email: String, phone: String?, description: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanDescription = description.trim()
        require(cleanName.length >= 2) { "Enter Your Name." }
        require('@' in cleanEmail) { "Enter A Valid Email Address." }
        require(cleanDescription.length >= 10) { "Enter At Least 10 Characters In Your Message." }
        api.contact(CreatorContactRequest(
            name = cleanName.take(120),
            email = cleanEmail,
            phoneNumber = phone?.trim()?.takeIf(String::isNotBlank)?.take(40),
            message = cleanDescription.take(4000),
        ))
    }

    suspend fun plans(): List<CreatorPlan> = items(api.plans(), "plans", "items", "data")
        .mapNotNull(::planFrom)
        .distinctBy { it.id }

    suspend fun createPlan(
        title: String, description: String?, monthlyPrice: Double, yearlyPrice: Double?,
        benefits: List<String>, visibility: String,
    ): List<CreatorPlan> {
        val cleanTitle = title.trim()
        require(cleanTitle.isNotBlank()) { "Plan title is required." }
        require(monthlyPrice >= 0) { "Monthly price cannot be negative." }
        require(yearlyPrice == null || yearlyPrice >= 0) { "Yearly price cannot be negative." }
        val cleanVisibility = visibility.trim().uppercase().takeIf { it in setOf("PUBLIC", "PRIVATE", "CLUB_ONLY", "LINK_ONLY") } ?: "PUBLIC"
        api.createPlan(mapOf(
            "title" to cleanTitle.take(120),
            "description" to description?.trim()?.takeIf(String::isNotBlank),
            "monthlyPrice" to monthlyPrice,
            "yearlyPrice" to yearlyPrice,
            "benefits" to benefits.map(String::trim).filter(String::isNotBlank).distinct(),
            "visibility" to cleanVisibility,
        ).filterValues { it != null })
        return plans()
    }

    suspend fun updatePlan(
        id: String, title: String, description: String?, monthlyPrice: Double, yearlyPrice: Double?,
        benefits: List<String>, visibility: String, status: String,
    ): List<CreatorPlan> {
        require(id.isNotBlank()) { "Plan is unavailable." }
        val cleanStatus = status.trim().uppercase().takeIf { it in setOf("DRAFT", "ACTIVE", "ARCHIVED") } ?: "DRAFT"
        val cleanVisibility = visibility.trim().uppercase().takeIf { it in setOf("PUBLIC", "PRIVATE", "CLUB_ONLY", "LINK_ONLY") } ?: "PUBLIC"
        api.updatePlan(id, mapOf(
            "title" to title.trim().take(120),
            "description" to description?.trim()?.takeIf(String::isNotBlank),
            "monthlyPrice" to monthlyPrice.coerceAtLeast(0.0),
            "yearlyPrice" to yearlyPrice?.coerceAtLeast(0.0),
            "benefits" to benefits.map(String::trim).filter(String::isNotBlank).distinct(),
            "visibility" to cleanVisibility,
            "status" to cleanStatus,
        ).filterValues { it != null })
        return plans()
    }

    suspend fun archivePlan(id: String): List<CreatorPlan> {
        require(id.isNotBlank()) { "Plan is unavailable." }
        api.archivePlan(id)
        return plans()
    }

    suspend fun subscribers(status: String? = null, page: Int = 1, limit: Int = 50): CreatorSubscriberPage {
        val cleanStatus = status?.trim()?.uppercase()?.takeIf { it in setOf("ACTIVE", "CANCELLED", "EXPIRED", "PENDING") }
        val rawValue = api.subscribers(status = cleanStatus, sortOrder = "DESC", page = page.coerceAtLeast(1), limit = limit.coerceIn(1, 100))
        val root = rawValue as? Map<*, *> ?: emptyMap<Any?, Any?>()
        val nested = root.map("data", "result", "subscribers").ifEmpty { root }
        val list = items(rawValue, "subscribers", "items", "data").mapNotNull(::subscriberFrom).distinctBy { it.id.ifBlank { "${it.userId}:${it.planId}:${it.subscribedAt}" } }
        return CreatorSubscriberPage(
            items = list,
            total = nested.nullableInt("total", "totalCount", "count") ?: root.nullableInt("total", "totalCount", "count"),
            page = nested.nullableInt("page", "currentPage") ?: page.coerceAtLeast(1),
            limit = nested.nullableInt("limit", "pageSize") ?: limit.coerceIn(1, 100),
            totalPages = nested.nullableInt("totalPages", "pages") ?: root.nullableInt("totalPages", "pages"),
        )
    }


    suspend fun analytics(): CreatorAnalytics {
        val raw = unwrap(api.analytics(), "analytics", "data", "summary")
        return CreatorAnalytics(
            metrics = scalarMetrics(raw, excluded = setOf("generatedAt", "updatedAt", "timestamp", "rooms", "series", "data")),
            generatedAt = raw.string("generatedAt", "updatedAt", "timestamp").takeIf(String::isNotBlank),
        )
    }

    suspend fun wallet(): CreatorWallet {
        val balanceRaw = unwrap(api.walletBalance(), "balance", "balances", "data", "wallet")
        val summaryRaw = unwrap(api.walletSummary(), "summary", "data", "wallet")
        val transactionsRaw = api.walletTransactions(page = 1, limit = 50)
        return CreatorWallet(
            balances = balanceEntries(balanceRaw),
            summary = scalarMetrics(summaryRaw, excluded = setOf("transactions", "items", "data", "balances")),
            transactions = items(transactionsRaw, "transactions", "items", "data")
                .mapNotNull(::walletTransactionFrom)
                .distinctBy { it.id },
        )
    }

    suspend fun earnings(): CreatorEarnings {
        val raw = unwrap(api.earnings(page = 1, limit = 50), "earnings", "summary", "data")
        return CreatorEarnings(
            metrics = scalarMetrics(raw, excluded = setOf("items", "data", "subscriptions", "payouts", "generatedAt", "updatedAt")),
            generatedAt = raw.string("generatedAt", "updatedAt", "timestamp").takeIf(String::isNotBlank),
        )
    }

    suspend fun gifts(): List<CreatorGiftRecord> = items(api.giftHistory(page = 1, limit = 50), "history", "gifts", "items", "data")
        .mapNotNull(::giftFrom)
        .distinctBy { it.id }

    suspend fun payoutRequests(status: String? = null, page: Int = 1, limit: Int = 50): CreatorPayoutPage {
        val cleanStatus = status?.trim()?.uppercase()?.takeIf { it in setOf("PENDING", "APPROVED", "REJECTED", "PROCESSED", "CANCELLED", "FAILED") }
        val rawValue = api.payoutRequests(cleanStatus, "DESC", page.coerceAtLeast(1), limit.coerceIn(1, 100))
        val root = rawValue as? Map<*, *> ?: emptyMap<Any?, Any?>()
        val nested = root.map("data", "result", "payouts").ifEmpty { root }
        val list = items(rawValue, "payoutRequests", "payouts", "items", "data").mapNotNull(::payoutFrom).distinctBy { it.id }
        return CreatorPayoutPage(
            items = list,
            total = nested.nullableInt("total", "totalCount", "count") ?: root.nullableInt("total", "totalCount", "count"),
            page = nested.nullableInt("page", "currentPage") ?: page.coerceAtLeast(1),
            limit = nested.nullableInt("limit", "pageSize") ?: limit.coerceIn(1, 100),
            totalPages = nested.nullableInt("totalPages", "pages") ?: root.nullableInt("totalPages", "pages"),
        )
    }

    suspend fun payoutRequest(id: String): CreatorPayoutRequest {
        require(id.trim().isNotBlank()) { "Payout request is unavailable." }
        val raw = unwrap(api.payoutRequest(id.trim()), "payout", "payoutRequest", "data")
        return payoutFrom(raw) ?: error("Payout request is unavailable.")
    }

    suspend fun createPayoutRequest(diamondAmount: Int, payoutMethod: String): CreatorPayoutPage {
        require(diamondAmount >= 100) { "Payout requests require at least 100 diamonds." }
        val method = payoutMethod.trim().uppercase()
        require(method in setOf("BANK_TRANSFER", "PAYPAL", "STRIPE", "CRYPTO")) { "Choose a supported payout method." }
        api.createPayoutRequest(mapOf(
            "diamondAmount" to diamondAmount,
            "payoutMethod" to method,
            "operationKey" to "android-payout-${UUID.randomUUID()}",
        ))
        return payoutRequests()
    }

    private fun balanceEntries(raw: Map<*, *>): List<CreatorBalance> {
        val nested = raw.map("balances")
        val source = if (nested.isNotEmpty()) nested else raw
        return source.entries.mapNotNull { (key, value) ->
            val label = key?.toString()?.trim()?.takeIf(String::isNotBlank) ?: return@mapNotNull null
            if (value !is Number && value !is String) return@mapNotNull null
            value.toString().toDoubleOrNull() ?: return@mapNotNull null
            CreatorBalance(label.uppercase(), displayValue(value))
        }.filter { it.type !in setOf("ID", "USERID") }.sortedBy { it.type }
    }

    private fun walletTransactionFrom(raw: Map<*, *>): CreatorWalletTransaction? {
        val id = raw.string("id", "transactionId")
        if (id.isBlank()) return null
        return CreatorWalletTransaction(
            id = id,
            type = raw.string("type", "transactionType").uppercase(),
            status = raw.string("status").uppercase(),
            amount = raw.value("amount", "value")?.let(::displayValue).orEmpty(),
            currency = raw.string("currency", "balanceType").uppercase(),
            description = raw.string("description", "note", "reason").takeIf(String::isNotBlank),
            createdAt = raw.string("createdAt", "timestamp").takeIf(String::isNotBlank),
        )
    }

    private fun giftFrom(raw: Map<*, *>): CreatorGiftRecord? {
        val id = raw.string("id", "transactionId", "giftTransactionId")
        if (id.isBlank()) return null
        val gift = raw.map("gift", "giftItem")
        val sender = raw.map("sender", "fromUser")
        val receiver = raw.map("receiver", "recipient", "toUser")
        val direction = raw.string("direction", "type", "transactionType").uppercase()
        val counterparty = when {
            "RECEIV" in direction -> sender.string("displayName", "username", "name")
            "SENT" in direction || "SEND" in direction -> receiver.string("displayName", "username", "name")
            else -> raw.string("counterpartyName").ifBlank { sender.string("displayName", "username") }
        }.takeIf(String::isNotBlank)
        return CreatorGiftRecord(
            id = id,
            giftName = gift.string("name", "title").ifBlank { raw.string("giftName", "name").ifBlank { "Gift" } },
            direction = direction,
            counterparty = counterparty,
            quantity = raw.nullableInt("quantity", "count"),
            amount = raw.value("amount", "diamondAmount", "coinAmount", "totalAmount")?.let(::displayValue),
            currency = raw.string("currency", "balanceType").takeIf(String::isNotBlank),
            createdAt = raw.string("createdAt", "timestamp", "sentAt").takeIf(String::isNotBlank),
        )
    }

    private fun payoutFrom(raw: Map<*, *>): CreatorPayoutRequest? {
        val id = raw.string("id", "payoutRequestId", "requestId")
        if (id.isBlank()) return null
        return CreatorPayoutRequest(
            id = id,
            diamondAmount = raw.value("diamondAmount", "diamonds", "amount")?.let(::displayValue).orEmpty(),
            payoutMethod = raw.string("payoutMethod", "method").uppercase(),
            status = raw.string("status").uppercase(),
            createdAt = raw.string("createdAt", "requestedAt").takeIf(String::isNotBlank),
            updatedAt = raw.string("updatedAt", "processedAt").takeIf(String::isNotBlank),
            rejectionReason = raw.string("rejectionReason", "reason").takeIf(String::isNotBlank),
        )
    }

    private fun scalarMetrics(raw: Map<*, *>, excluded: Set<String> = emptySet()): List<CreatorMetric> = raw.entries.mapNotNull { (key, value) ->
        val rawKey = key?.toString()?.trim()?.takeIf(String::isNotBlank) ?: return@mapNotNull null
        if (excluded.any { it.equals(rawKey, ignoreCase = true) }) return@mapNotNull null
        if (value !is Number && value !is String && value !is Boolean) return@mapNotNull null
        CreatorMetric(rawKey, humanize(rawKey), displayValue(value))
    }.sortedBy { it.label }

    private fun humanize(value: String): String = value
        .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
        .replace('_', ' ').replace('-', ' ').trim()
        .split(Regex("\\s+")).filter(String::isNotBlank).joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) }

    private fun planFrom(raw: Map<*, *>): CreatorPlan? {
        val id = raw.string("id", "planId")
        val title = raw.string("title", "name")
        if (id.isBlank() || title.isBlank()) return null
        return CreatorPlan(
            id = id, title = title,
            description = raw.string("description").takeIf(String::isNotBlank),
            monthlyPrice = raw.double("monthlyPrice", "monthly_price", "price"),
            yearlyPrice = raw.nullableDouble("yearlyPrice", "yearly_price"),
            benefits = raw.list("benefits", "perks").mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) },
            visibility = raw.string("visibility").ifBlank { "PUBLIC" }.uppercase(),
            status = raw.string("status").ifBlank { "DRAFT" }.uppercase(),
            subscriberCount = raw.nullableInt("subscriberCount", "subscribersCount", "activeSubscribers"),
            createdAt = raw.string("createdAt").takeIf(String::isNotBlank),
            updatedAt = raw.string("updatedAt").takeIf(String::isNotBlank),
        )
    }

    private fun subscriberFrom(raw: Map<*, *>): CreatorSubscriber? {
        val user = raw.map("user", "subscriber")
        val plan = raw.map("plan", "creatorPlan")
        val userId = raw.string("userId", "subscriberId").ifBlank { user.string("id", "userId") }
        val planId = raw.string("planId").ifBlank { plan.string("id", "planId") }
        val id = raw.string("id", "subscriptionId")
        if (id.isBlank() && userId.isBlank()) return null
        return CreatorSubscriber(
            id = id, userId = userId, planId = planId,
            status = raw.string("status").uppercase(),
            displayName = user.string("displayName", "name").ifBlank { user.string("username") },
            username = user.string("username"),
            avatarUrl = user.string("avatarUrl", "avatar").takeIf(String::isNotBlank),
            planTitle = plan.string("title", "name").takeIf(String::isNotBlank) ?: raw.string("planTitle").takeIf(String::isNotBlank),
            subscribedAt = raw.string("subscribedAt", "startedAt", "createdAt").takeIf(String::isNotBlank),
            expiresAt = raw.string("expiresAt", "endDate", "currentPeriodEnd").takeIf(String::isNotBlank),
            autoRenew = raw.nullableBool("autoRenew", "auto_renew"),
        )
    }

    private fun profileFrom(raw: Map<*, *>): CreatorProfile = CreatorProfile(
        id = raw.string("id", "userId"),
        username = raw.string("username"),
        displayName = raw.string("displayName", "name").ifBlank { raw.string("username") },
        email = raw.string("email").takeIf(String::isNotBlank),
        phoneNumber = raw.string("phoneNumber", "phone").takeIf(String::isNotBlank),
        avatarUrl = raw.string("avatarUrl", "avatar").takeIf(String::isNotBlank),
        bio = raw.string("bio", "about").takeIf(String::isNotBlank),
        country = raw.string("country").takeIf(String::isNotBlank),
        preferredLanguage = raw.string("preferredLanguage", "language").ifBlank { "en" },
        interests = raw.list("interests").mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }.distinct(),
        role = raw.string("role").ifBlank { "CREATOR" },
    )

    private fun cmsSummaryFrom(raw: Map<*, *>): CreatorCmsPageSummary? {
        val slug = raw.string("slug", "key", "id")
        val title = raw.string("title", "name")
        if (slug.isBlank() || title.isBlank()) return null
        return CreatorCmsPageSummary(
            slug = slug,
            title = title,
            excerpt = raw.string("excerpt", "summary", "description").takeIf(String::isNotBlank),
            sortOrder = raw.int("sortOrder", "order", "position"),
        )
    }

    private fun dashboardMetrics(raw: Map<*, *>): List<CreatorDashboardMetric> {
        val summary = raw.map("summary", "metrics", "stats").ifEmpty { raw }
        val specs = listOf(
            MetricSpec("followers", "Followers", listOf("followers", "followersCount", "totalFollowers")),
            MetricSpec("subscribers", "Subscribers", listOf("subscribers", "subscribersCount", "totalSubscribers")),
            MetricSpec("rooms", "Rooms", listOf("roomsCount", "totalRooms", "roomsHosted", "completedRooms")),
            MetricSpec("listeners", "Listeners", listOf("listeners", "totalListeners", "listenerCount")),
            MetricSpec("earnings", "Earnings", listOf("earnings", "totalEarnings", "creatorEarnings")),
            MetricSpec("payouts", "Payouts", listOf("payouts", "payoutRequests", "payoutCount")),
        )
        return specs.mapNotNull { spec ->
            val entry = spec.keys.firstNotNullOfOrNull { key -> summary.value(key)?.let { key to it } } ?: return@mapNotNull null
            val displayed = displayValue(entry.second).takeIf(String::isNotBlank) ?: return@mapNotNull null
            CreatorDashboardMetric(spec.key, spec.label, displayed)
        }
    }

    private data class MetricSpec(val key: String, val label: String, val keys: List<String>)

    private fun displayValue(value: Any?): String = when (value) {
        null -> ""
        is Number -> DecimalFormat("#,##0.##").format(value)
        is Boolean -> if (value) "Yes" else "No"
        is String -> value.trim()
        is Map<*, *> -> {
            val amount = value.value("amount", "value", "total")
            val currency = value.string("currency", "currencyCode")
            if (amount != null) listOf(currency, displayValue(amount)).filter(String::isNotBlank).joinToString(" ") else ""
        }
        else -> value.toString().trim()
    }

    private fun unwrap(value: Any?, vararg keys: String): Map<*, *> {
        val root = value as? Map<*, *> ?: return emptyMap<Any?, Any?>()
        for (key in keys) {
            val nested = root.value(key) as? Map<*, *>
            if (nested != null) return nested
        }
        return root
    }

    private fun items(value: Any?, vararg keys: String): List<Map<*, *>> {
        if (value is List<*>) return value.mapNotNull { it as? Map<*, *> }
        val root = value as? Map<*, *> ?: return emptyList()
        val candidates = (keys.toList() + listOf("items", "data", "pages", "subscribers", "plans")).distinct()
        for (key in candidates) {
            val nested = root.value(key)
            if (nested is List<*>) return nested.mapNotNull { it as? Map<*, *> }
            if (nested is Map<*, *>) {
                for (childKey in candidates) {
                    val nestedList = nested.value(childKey)
                    if (nestedList is List<*>) return nestedList.mapNotNull { it as? Map<*, *> }
                }
            }
        }
        return emptyList()
    }

    private fun Map<*, *>.value(vararg keys: String): Any? {
        for (key in keys) entries.firstOrNull { it.key?.toString()?.equals(key, ignoreCase = true) == true }?.value?.let { return it }
        return null
    }
    private fun Map<*, *>.string(vararg keys: String): String = value(*keys)?.toString()?.trim().orEmpty()
    private fun Map<*, *>.map(vararg keys: String): Map<*, *> = value(*keys) as? Map<*, *> ?: emptyMap<Any?, Any?>()
    private fun Map<*, *>.list(vararg keys: String): List<*> = value(*keys) as? List<*> ?: emptyList<Any?>()
    private fun Map<*, *>.int(vararg keys: String): Int = when (val value = value(*keys)) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: 0
        else -> 0
    }
    private fun Map<*, *>.nullableInt(vararg keys: String): Int? = when (val value = value(*keys)) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }
    private fun Map<*, *>.double(vararg keys: String): Double = when (val value = value(*keys)) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
    private fun Map<*, *>.nullableDouble(vararg keys: String): Double? = when (val value = value(*keys)) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        else -> null
    }
    private fun Map<*, *>.nullableBool(vararg keys: String): Boolean? = when (val value = value(*keys)) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> when (value.trim().lowercase()) { "true", "1", "yes", "on" -> true; "false", "0", "no", "off" -> false; else -> null }
        else -> null
    }
    private fun Map<*, *>.bool(default: Boolean, vararg keys: String): Boolean = when (val value = value(*keys)) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> when (value.trim().lowercase()) { "true", "1", "yes", "on" -> true; "false", "0", "no", "off" -> false; else -> default }
        else -> default
    }
}
