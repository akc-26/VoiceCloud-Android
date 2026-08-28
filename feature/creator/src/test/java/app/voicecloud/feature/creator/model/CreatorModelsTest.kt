package app.voicecloud.feature.creator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatorModelsTest {
    @Test fun creatorSettingsRemainEnglishOnlyByDefault() {
        assertEquals("en", CreatorSettings().language)
    }

    @Test fun creatorPortalIncludesPh11LiveWithoutFinancialSections() {
        val sections = CreatorPortalSection.entries.map { it.name }
        assertEquals(listOf("DASHBOARD", "LIVE", "PROFILE", "SETTINGS", "HELP"), sections)
        assertTrue(sections.contains("LIVE"))
        assertFalse(sections.any { it.contains("WALLET") || it.contains("PAYOUT") || it.contains("EARNINGS") })
    }
}
