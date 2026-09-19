package app.voicecloud.feature.creator.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatorModelsTest {
    @Test fun creatorSettingsRemainEnglishOnlyByDefault() {
        assertEquals("en", CreatorSettings().language)
    }

    @Test fun creatorPortalIncludesPh12AudienceWithoutFinancialSections() {
        val sections = CreatorPortalSection.entries.map { it.name }
        assertEquals(listOf("DASHBOARD", "LIVE", "AUDIENCE", "PROFILE", "SETTINGS", "HELP"), sections)
        assertTrue(sections.contains("LIVE"))
        assertTrue(sections.contains("AUDIENCE"))
        assertFalse(sections.any { it.contains("WALLET") || it.contains("PAYOUT") || it.contains("EARNINGS") })
    }

    @Test fun creatorPlanDefaultsNeverFabricateActiveOrPaidState() {
        val plan = CreatorPlan()
        assertEquals("DRAFT", plan.status)
        assertEquals(0.0, plan.monthlyPrice, 0.0)
        assertEquals(null, plan.subscriberCount)
    }

    @Test fun subscriberTotalMayRemainUnknownWhenBackendOmitsPaginationMetadata() {
        assertEquals(null, CreatorSubscriberPage().total)
    }
    @Test fun ph13FinancialModelsDefaultToUnknownOrEmptyBackendState() {
        assertTrue(CreatorWallet().balances.isEmpty())
        assertTrue(CreatorEarnings().metrics.isEmpty())
        assertTrue(CreatorPayoutPage().items.isEmpty())
        assertEquals("", CreatorPayoutRequest(id = "request-1").status)
    }

}
