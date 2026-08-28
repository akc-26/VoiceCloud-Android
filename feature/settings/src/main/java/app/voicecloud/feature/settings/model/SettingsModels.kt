package app.voicecloud.feature.settings.model

import app.voicecloud.core.preferences.ThemePreference

data class NotificationPreferences(
    val email: Boolean = true,
    val push: Boolean = true,
    val inApp: Boolean = true,
    val sound: Boolean = true,
)

data class VoicePreferences(
    val audioPreset: String = "",
    val noiseSuppression: Boolean = true,
    val echoCancellation: Boolean = true,
    val agc: Boolean = true,
)

data class UserPreferences(
    val notifications: NotificationPreferences = NotificationPreferences(),
    val voice: VoicePreferences = VoicePreferences(),
    val theme: ThemePreference = ThemePreference.LIGHT,
    val language: String = "en",
    val timezone: String = "",
)

data class PrivacyPreferences(
    val messagingPermission: String = "everyone",
    val followPermission: String = "everyone",
    val invitationPermission: String = "everyone",
    val visitorPermission: String = "everyone",
    val allowVisitorTracking: Boolean = true,
    val anonymousVisiting: Boolean = false,
)

data class SafeSession(
    val id: String,
    val deviceId: String? = null,
    val deviceType: String? = null,
    val deviceName: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val isOnline: Boolean = false,
    val status: String? = null,
    val lastActiveAt: String? = null,
    val expiresAt: String? = null,
    val createdAt: String? = null,
    val isCurrent: Boolean = false,
)

data class SafeDevice(
    val id: String,
    val deviceId: String? = null,
    val deviceType: String? = null,
    val deviceName: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null,
    val lastIp: String? = null,
    val status: String? = null,
    val lastUsedAt: String? = null,
    val createdAt: String? = null,
    val isCurrent: Boolean = false,
)

data class LoginActivity(
    val id: String,
    val action: String,
    val loginMethod: String? = null,
    val ipAddress: String? = null,
    val country: String? = null,
    val platform: String? = null,
    val userAgent: String? = null,
    val createdAt: String? = null,
)

data class CmsPageSummary(
    val id: String = "",
    val slug: String,
    val title: String,
    val excerpt: String? = null,
    val category: String? = null,
    val sortOrder: Int = 0,
)

data class CmsPageDetail(
    val slug: String,
    val title: String,
    val content: String,
    val updatedAt: String? = null,
)

enum class ReportTargetType { USER, ROOM }
enum class ReportReason { SPAM, ABUSE, HARASSMENT, FAKE_PROFILE, SEXUAL_CONTENT, VIOLENCE, OTHER }

data class ReportTarget(
    val id: String,
    val type: ReportTargetType,
    val label: String,
    val secondaryLabel: String? = null,
)

data class ReportRequest(
    val targetType: String,
    val targetId: String,
    val reason: String,
    val description: String? = null,
)

data class MyReport(
    val id: String,
    val targetType: String,
    val targetLabel: String,
    val reason: String,
    val status: String,
    val description: String? = null,
    val createdAt: String? = null,
)

data class ContactSupportRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val description: String,
)

data class MaintenanceState(
    val enabled: Boolean = false,
    val message: String? = null,
)

data class SecuritySnapshot(
    val sessions: List<SafeSession> = emptyList(),
    val devices: List<SafeDevice> = emptyList(),
    val history: List<LoginActivity> = emptyList(),
)
