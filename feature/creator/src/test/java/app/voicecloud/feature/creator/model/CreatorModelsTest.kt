package app.voicecloud.feature.creator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CreatorModelsTest {
    @Test fun creatorSettingsRemainEnglishOnlyByDefault() {
        assertEquals("en", CreatorSettings().language)
    }

    @Test fun creatorPortalCoreDoesNotContainFinancialOrRtcSections() {
        val sections = CreatorPortalSection.entries.map { it.name }
        assertEquals(listOf("DASHBOARD", "PROFILE", "SETTINGS", "HELP"), sections)
        assertFalse(sections.any { it.contains("WALLET") || it.contains("LIVE") || it.contains("PAYOUT") })
    }
}
