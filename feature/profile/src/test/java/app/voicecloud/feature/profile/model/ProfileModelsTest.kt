package app.voicecloud.feature.profile.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileModelsTest {
    @Test fun replayDefaultsAreNonAuthoritative() {
        val replay = ReplayItem()
        assertEquals("", replay.id)
        assertNull(replay.accessAllowed)
    }

    @Test fun profileDoesNotFabricateInterests() {
        val profile = ExtendedProfile()
        assertFalse(profile.interests.orEmpty().any())
        assertFalse(profile.customTags.orEmpty().any())
    }
}
