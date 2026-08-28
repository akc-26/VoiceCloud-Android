package app.voicecloud.feature.creator.data

import app.voicecloud.feature.creator.model.*
import java.text.DecimalFormat
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
        for (key in keys) {
            val nested = root.value(key)
            if (nested is List<*>) return nested.mapNotNull { it as? Map<*, *> }
            if (nested is Map<*, *>) {
                val nestedList = nested.value("items", "data", "pages")
                if (nestedList is List<*>) return nestedList.mapNotNull { it as? Map<*, *> }
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
    private fun Map<*, *>.bool(default: Boolean, vararg keys: String): Boolean = when (val value = value(*keys)) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> when (value.trim().lowercase()) { "true", "1", "yes", "on" -> true; "false", "0", "no", "off" -> false; else -> default }
        else -> default
    }
}
