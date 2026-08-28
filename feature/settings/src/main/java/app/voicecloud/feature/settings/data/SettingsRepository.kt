package app.voicecloud.feature.settings.data

import app.voicecloud.core.preferences.ThemePreference
import app.voicecloud.core.preferences.VoiceCloudPreferences
import app.voicecloud.feature.settings.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val api: SettingsApi,
    private val preferences: VoiceCloudPreferences,
) {
    suspend fun preferences(): UserPreferences {
        val raw = unwrap(api.settings(), "settings")
        val notifications = raw.map("notificationPreferences")
        val theme = raw.string("theme").toThemePreference()
        preferences.setTheme(theme)
        return UserPreferences(
            notifications = NotificationPreferences(
                email = notifications.bool(default = true, "email", "emailAlerts"),
                push = notifications.bool(default = true, "push", "pushNotifications"),
                inApp = notifications.bool(default = true, "inApp", "in_app", "inAppNotifications"),
                sound = notifications.bool(default = true, "sound", "sounds", "soundEffects"),
            ),
            voice = VoicePreferences(
                audioPreset = raw.string("audioPreset"),
                noiseSuppression = raw.bool(default = true, "noiseSuppression"),
                echoCancellation = raw.bool(default = true, "echoCancellation"),
                agc = raw.bool(default = true, "agc"),
            ),
            theme = theme,
            language = raw.string("language").ifBlank { "en" },
            timezone = raw.string("timezone"),
        )
    }

    suspend fun updateNotifications(value: NotificationPreferences): UserPreferences {
        api.updateSettings(mapOf(
            "notificationPreferences" to mapOf(
                "email" to value.email,
                "push" to value.push,
                "inApp" to value.inApp,
                "sound" to value.sound,
            ),
        ))
        return preferences()
    }

    suspend fun updateVoice(value: VoicePreferences): UserPreferences {
        api.updateSettings(mapOf(
            "audioPreset" to value.audioPreset.trim().takeIf(String::isNotBlank),
            "noiseSuppression" to value.noiseSuppression,
            "echoCancellation" to value.echoCancellation,
            "agc" to value.agc,
        ).filterValues { it != null })
        return preferences()
    }

    suspend fun updateTheme(value: ThemePreference): UserPreferences {
        val backendValue = value.name.lowercase()
        api.updateSettings(mapOf("theme" to backendValue, "language" to "en"))
        preferences.setTheme(value)
        return preferences()
    }

    suspend fun privacy(): PrivacyPreferences {
        val raw = unwrap(api.privacy(), "privacy", "settings")
        return PrivacyPreferences(
            messagingPermission = raw.string("messagingPermission").ifBlank { "everyone" },
            followPermission = raw.string("followPermission").ifBlank { "everyone" },
            invitationPermission = raw.string("invitationPermission").ifBlank { "everyone" },
            visitorPermission = raw.string("visitorPermission").ifBlank { "everyone" },
            allowVisitorTracking = raw.bool(default = true, "allowVisitorTracking"),
            anonymousVisiting = raw.bool(default = false, "anonymousVisiting"),
        )
    }

    suspend fun updatePrivacy(value: PrivacyPreferences): PrivacyPreferences {
        api.updatePrivacy(mapOf(
            "messagingPermission" to value.messagingPermission,
            "followPermission" to value.followPermission,
            "invitationPermission" to value.invitationPermission,
            "visitorPermission" to value.visitorPermission,
            "allowVisitorTracking" to value.allowVisitorTracking,
            "anonymousVisiting" to value.anonymousVisiting,
        ))
        return privacy()
    }

    suspend fun securitySnapshot(): SecuritySnapshot = SecuritySnapshot(
        sessions = sessions(),
        devices = devices(),
        history = history(),
    )

    suspend fun sessions(): List<SafeSession> {
        val currentSessionId = preferences.sessionId.first()
        return items(api.sessions(), "sessions").mapNotNull { raw -> sessionFrom(raw, currentSessionId) }
            .distinctBy { it.id }
            .sortedWith(compareByDescending<SafeSession> { it.isCurrent }.thenByDescending { it.lastActiveAt.orEmpty() })
    }

    suspend fun session(id: String): SafeSession {
        require(id.isNotBlank()) { "Session is unavailable." }
        return sessionFrom(unwrap(api.session(id), "session"), preferences.sessionId.first())
            ?: error("Session is unavailable.")
    }

    suspend fun revokeSession(id: String) {
        require(id.isNotBlank()) { "Session is unavailable." }
        api.revokeSession(id)
    }

    suspend fun devices(): List<SafeDevice> {
        val currentDeviceId = preferences.deviceId.first()
        return items(api.devices(), "devices").mapNotNull { raw -> deviceFrom(raw, currentDeviceId) }
            .distinctBy { it.id }
            .sortedWith(compareByDescending<SafeDevice> { it.isCurrent }.thenByDescending { it.lastUsedAt.orEmpty() })
    }

    suspend fun device(id: String): SafeDevice {
        require(id.isNotBlank()) { "Device is unavailable." }
        return deviceFrom(unwrap(api.device(id), "device"), preferences.deviceId.first())
            ?: error("Device is unavailable.")
    }

    suspend fun revokeDevice(id: String) {
        require(id.isNotBlank()) { "Device is unavailable." }
        api.revokeDevice(id)
    }

    suspend fun history(): List<LoginActivity> = items(api.history(100), "history", "activities", "items")
        .mapNotNull(::historyFrom)
        .distinctBy { it.id.ifBlank { "${it.action}:${it.createdAt}:${it.ipAddress}" } }
        .take(100)

    suspend fun cmsPages(): List<CmsPageSummary> = items(api.cmsPages(), "pages")
        .mapNotNull(::cmsSummaryFrom)
        .filter { it.slug.isNotBlank() && it.title.isNotBlank() }
        .distinctBy { it.slug.lowercase() }
        .sortedWith(compareBy<CmsPageSummary> { it.sortOrder }.thenBy { it.title.lowercase() })

    suspend fun cmsPage(slug: String): CmsPageDetail {
        require(slug.isNotBlank()) { "Content is unavailable." }
        val raw = unwrap(api.cmsPage(slug.trim()), "page")
        return CmsPageDetail(
            slug = raw.string("slug", "key").ifBlank { slug.trim() },
            title = raw.string("title", "name").ifBlank { "VoiceCloud" },
            content = raw.string("content", "body", "html", "description"),
            updatedAt = raw.string("updatedAt", "publishedAt").takeIf(String::isNotBlank),
        )
    }

    suspend fun searchReportTargets(query: String, type: ReportTargetType?): List<ReportTarget> {
        val clean = query.trim()
        if (clean.length < 2) return emptyList()
        return items(api.searchReportTargets(clean, type?.name, 20), "targets", "results", "items")
            .mapNotNull { reportTargetFrom(it, type) }
            .filter { it.type == ReportTargetType.USER || it.type == ReportTargetType.ROOM }
            .distinctBy { "${it.type}:${it.id}" }
    }

    suspend fun resolveReportTarget(type: ReportTargetType, id: String, fallbackLabel: String?): ReportTarget {
        val cleanId = id.trim()
        require(cleanId.isNotBlank()) { "Report target is unavailable." }
        val raw = unwrap(api.reportTarget(type.name, cleanId), "target")
        return reportTargetFrom(raw, type)?.copy(id = cleanId)
            ?: ReportTarget(cleanId, type, fallbackLabel?.trim().orEmpty().ifBlank { if (type == ReportTargetType.USER) "VoiceCloud member" else "VoiceCloud room" })
    }

    suspend fun submitReport(target: ReportTarget, reason: ReportReason, description: String?) {
        api.submitReport(ReportRequest(
            targetType = target.type.name,
            targetId = target.id,
            reason = reason.name,
            description = description?.trim()?.takeIf(String::isNotBlank),
        ))
    }

    suspend fun myReports(): List<MyReport> = items(api.myReports(), "reports", "items")
        .mapNotNull(::reportFrom)
        .distinctBy { it.id }

    suspend fun contact(name: String, email: String, phone: String?, description: String) {
        require(name.trim().isNotBlank()) { "Enter your name." }
        require(email.trim().contains('@')) { "Enter a valid email address." }
        require(description.trim().isNotBlank()) { "Enter your message." }
        api.contact(ContactSupportRequest(
            name = name.trim(),
            email = email.trim().lowercase(),
            phone = phone?.trim()?.takeIf(String::isNotBlank),
            description = description.trim(),
        ))
    }

    suspend fun maintenance(): MaintenanceState {
        val raw = unwrap(api.maintenance(), "maintenance", "config")
        return MaintenanceState(
            enabled = raw.bool(default = false, "enabled", "maintenanceMode", "isMaintenanceMode", "isEnabled"),
            message = raw.string("message", "maintenanceMessage").takeIf(String::isNotBlank),
        )
    }

    private fun sessionFrom(raw: Map<*, *>, currentSessionId: String?): SafeSession? {
        val id = raw.string("id", "sessionId")
        if (id.isBlank()) return null
        return SafeSession(
            id = id,
            deviceId = raw.string("deviceId").takeIf(String::isNotBlank),
            deviceType = raw.string("deviceType", "platform").takeIf(String::isNotBlank),
            deviceName = raw.string("deviceName", "name").takeIf(String::isNotBlank),
            ipAddress = raw.string("ipAddress", "ip").takeIf(String::isNotBlank),
            userAgent = raw.string("userAgent").takeIf(String::isNotBlank),
            isOnline = raw.bool(default = false, "isOnline", "online"),
            status = raw.string("status").takeIf(String::isNotBlank),
            lastActiveAt = raw.string("lastActiveAt", "lastUsedAt", "updatedAt").takeIf(String::isNotBlank),
            expiresAt = raw.string("expiresAt").takeIf(String::isNotBlank),
            createdAt = raw.string("createdAt").takeIf(String::isNotBlank),
            isCurrent = raw.bool(default = id == currentSessionId, "isCurrent") || id == currentSessionId,
        )
    }

    private fun deviceFrom(raw: Map<*, *>, currentDeviceId: String?): SafeDevice? {
        val id = raw.string("id", "deviceId")
        if (id.isBlank()) return null
        val canonicalDeviceId = raw.string("deviceId").ifBlank { id }
        return SafeDevice(
            id = id,
            deviceId = canonicalDeviceId,
            deviceType = raw.string("deviceType", "platform").takeIf(String::isNotBlank),
            deviceName = raw.string("deviceName", "name").takeIf(String::isNotBlank),
            manufacturer = raw.string("manufacturer").takeIf(String::isNotBlank),
            model = raw.string("model").takeIf(String::isNotBlank),
            osVersion = raw.string("osVersion").takeIf(String::isNotBlank),
            appVersion = raw.string("appVersion").takeIf(String::isNotBlank),
            lastIp = raw.string("lastIp", "ipAddress").takeIf(String::isNotBlank),
            status = raw.string("status").takeIf(String::isNotBlank),
            lastUsedAt = raw.string("lastUsedAt", "updatedAt").takeIf(String::isNotBlank),
            createdAt = raw.string("createdAt").takeIf(String::isNotBlank),
            isCurrent = raw.bool(default = canonicalDeviceId == currentDeviceId, "isCurrent") || canonicalDeviceId == currentDeviceId,
        )
    }

    private fun historyFrom(raw: Map<*, *>): LoginActivity? {
        val action = raw.string("action", "event", "type")
        val id = raw.string("id", "historyId", "eventId")
        if (action.isBlank() && id.isBlank()) return null
        return LoginActivity(
            id = id,
            action = action.ifBlank { "ACCOUNT_ACTIVITY" },
            loginMethod = raw.string("loginMethod", "method").takeIf(String::isNotBlank),
            ipAddress = raw.string("ipAddress", "ip").takeIf(String::isNotBlank),
            country = raw.string("country", "location").takeIf(String::isNotBlank),
            platform = raw.string("platform", "deviceType").takeIf(String::isNotBlank),
            userAgent = raw.string("userAgent").takeIf(String::isNotBlank),
            createdAt = raw.string("createdAt", "timestamp").takeIf(String::isNotBlank),
        )
    }

    private fun cmsSummaryFrom(raw: Map<*, *>): CmsPageSummary? {
        val slug = raw.string("slug", "key", "id")
        val title = raw.string("title", "name")
        if (slug.isBlank() || title.isBlank()) return null
        return CmsPageSummary(
            id = raw.string("id"),
            slug = slug,
            title = title,
            excerpt = raw.string("excerpt", "summary", "description").takeIf(String::isNotBlank),
            category = raw.string("category", "type").takeIf(String::isNotBlank),
            sortOrder = raw.int("sortOrder", "order", "position"),
        )
    }

    private fun reportTargetFrom(raw: Map<*, *>, fallbackType: ReportTargetType?): ReportTarget? {
        val nested = raw.map("target").ifEmpty { raw.map("user").ifEmpty { raw.map("room") } }
        val source = if (nested.isEmpty()) raw else nested
        val id = source.string("id", "targetId", "userId", "roomId").ifBlank { raw.string("targetId") }
        if (id.isBlank()) return null
        val typeRaw = raw.string("targetType", "type").ifBlank { source.string("targetType", "type") }
        val type = runCatching { ReportTargetType.valueOf(typeRaw.uppercase()) }.getOrNull() ?: fallbackType ?: return null
        val label = when (type) {
            ReportTargetType.USER -> source.string("displayName", "username", "name", "label")
            ReportTargetType.ROOM -> source.string("title", "name", "label")
        }.ifBlank { raw.string("label", "targetLabel") }
        val secondary = when (type) {
            ReportTargetType.USER -> source.string("username").takeIf { it.isNotBlank() && !it.equals(label, true) }?.let { "@$it" }
            ReportTargetType.ROOM -> source.map("host").string("displayName", "username").takeIf(String::isNotBlank)?.let { "Hosted by $it" }
        }
        return ReportTarget(id, type, label.ifBlank { if (type == ReportTargetType.USER) "VoiceCloud member" else "VoiceCloud room" }, secondary)
    }

    private fun reportFrom(raw: Map<*, *>): MyReport? {
        val id = raw.string("id", "reportId")
        if (id.isBlank()) return null
        val target = raw.map("target")
        val targetType = raw.string("targetType", "type")
        val label = raw.string("targetLabel", "resolvedTargetLabel").ifBlank {
            if (targetType.equals("ROOM", true)) target.string("title", "name") else target.string("displayName", "username", "name")
        }.ifBlank { targetType.lowercase().replaceFirstChar { it.uppercase() } }
        return MyReport(
            id = id,
            targetType = targetType,
            targetLabel = label,
            reason = raw.string("reason"),
            status = raw.string("status").ifBlank { "PENDING" },
            description = raw.string("description", "details").takeIf(String::isNotBlank),
            createdAt = raw.string("createdAt", "submittedAt").takeIf(String::isNotBlank),
        )
    }

    private fun String.toThemePreference(): ThemePreference = when (trim().lowercase()) {
        "dark" -> ThemePreference.DARK
        "system" -> ThemePreference.SYSTEM
        else -> ThemePreference.LIGHT
    }

    private fun items(raw: Any?, vararg preferred: String): List<Map<*, *>> {
        if (raw is List<*>) return raw.mapNotNull { it as? Map<*, *> }
        val root = raw as? Map<*, *> ?: return emptyList()
        val candidates = preferred.toList() + listOf("data", "items", "results")
        for (key in candidates.distinct()) {
            when (val value = root[key]) {
                is List<*> -> return value.mapNotNull { it as? Map<*, *> }
                is Map<*, *> -> {
                    for (nestedKey in candidates.distinct()) {
                        val nested = value[nestedKey]
                        if (nested is List<*>) return nested.mapNotNull { it as? Map<*, *> }
                    }
                }
            }
        }
        return emptyList()
    }

    private fun unwrap(raw: Any?, vararg preferred: String): Map<*, *> {
        val root = raw as? Map<*, *> ?: return emptyMap<Any?, Any?>()
        for (key in preferred.toList() + listOf("data", "result")) {
            val nested = root[key]
            if (nested is Map<*, *>) return nested
        }
        return root
    }

    private fun Map<*, *>.string(vararg keys: String): String {
        for (key in keys) when (val value = this[key]) {
            is String -> if (value.isNotBlank()) return value
            is Number, is Boolean -> return value.toString()
        }
        return ""
    }

    private fun Map<*, *>.bool(default: Boolean, vararg keys: String): Boolean {
        for (key in keys) when (val value = this[key]) {
            is Boolean -> return value
            is Number -> return value.toInt() != 0
            is String -> when (value.lowercase()) { "true", "1", "yes", "on" -> return true; "false", "0", "no", "off" -> return false }
        }
        return default
    }

    private fun Map<*, *>.int(vararg keys: String): Int {
        for (key in keys) when (val value = this[key]) {
            is Number -> return value.toInt()
            is String -> value.toIntOrNull()?.let { return it }
        }
        return 0
    }

    private fun Map<*, *>.map(key: String): Map<*, *> = this[key] as? Map<*, *> ?: emptyMap<Any?, Any?>()
}
