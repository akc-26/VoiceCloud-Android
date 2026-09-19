package app.voicecloud.feature.settings.model

import app.voicecloud.core.preferences.ThemePreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsModelsTest {
    @Test fun `consumer preferences have safe defaults`() {
        val value = UserPreferences()
        assertEquals(ThemePreference.LIGHT, value.theme)
        assertEquals("en", value.language)
        assertTrue(value.notifications.push)
        assertTrue(value.voice.noiseSuppression)
    }

    @Test fun `consumer report scope exposes user and room only`() {
        assertEquals(listOf(ReportTargetType.USER, ReportTargetType.ROOM), ReportTargetType.entries)
        assertTrue(ReportReason.entries.contains(ReportReason.HARASSMENT))
        assertTrue(ReportReason.entries.contains(ReportReason.OTHER))
    }

    @Test fun `privacy model matches current backend booleans`() {
        val fields = PrivacyPreferences::class.java.declaredFields
            .filterNot { it.isSynthetic || it.name.startsWith("$") }
            .map { it.name }
            .toSet()
        assertEquals(setOf("showOnlineStatus", "showLastSeen", "allowDirectMessages", "showGifts"), fields)
    }

    @Test fun `contact support uses backend field names`() {
        val fields = ContactSupportRequest::class.java.declaredFields
            .filterNot { it.isSynthetic || it.name.startsWith("$") }
            .map { it.name }
            .toSet()
        assertEquals(setOf("name", "email", "phoneNumber", "message"), fields)
    }

    @Test fun `safe security projections contain no credential token fields`() {
        val names = SafeSession::class.java.declaredFields.map { it.name.lowercase() } + SafeDevice::class.java.declaredFields.map { it.name.lowercase() }
        assertTrue(names.none { it.contains("token") || it.contains("refreshhash") || it == "userid" || it == "pushtoken" })
    }
}
