package app.voicecloud.feature.discovery.data

import app.voicecloud.feature.discovery.model.ViewerIdentity
import app.voicecloud.feature.discovery.model.VoiceCloudUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsumerIdentityPolicyTest {
    private val viewer = ViewerIdentity(id = "self-id", username = "Ashok")

    @Test fun consumerRolesRemainVisible() {
        assertTrue(ConsumerIdentityPolicy.isDiscoverable(VoiceCloudUser(id = "u1", username = "user1", role = "USER"), viewer))
        assertTrue(ConsumerIdentityPolicy.isDiscoverable(VoiceCloudUser(id = "u2", username = "creator1", role = "CREATOR"), viewer))
    }

    @Test fun privilegedAndGuestIdentitiesAreHidden() {
        assertFalse(ConsumerIdentityPolicy.isDiscoverable(VoiceCloudUser(id = "a", username = "admin", role = "ADMIN"), viewer))
        assertFalse(ConsumerIdentityPolicy.isDiscoverable(VoiceCloudUser(id = "s", username = "super", role = "SUPER_ADMIN"), viewer))
        assertFalse(ConsumerIdentityPolicy.isDiscoverable(VoiceCloudUser(id = "g", username = "guest", role = "GUEST", isGuest = true), viewer))
    }

    @Test fun duplicateConsumerIdentitiesAreCollapsedBeforeRendering() {
        val duplicated = listOf(
            VoiceCloudUser(id = "creator-id", username = "creator", role = "CREATOR"),
            VoiceCloudUser(id = "creator-id", username = "creator", role = "CREATOR"),
            VoiceCloudUser(id = "user-id", username = "user", role = "USER"),
        )
        val filtered = ConsumerIdentityPolicy.filter(duplicated, viewer)
        assertEquals(listOf("creator-id", "user-id"), filtered.map { it.id })
    }

    @Test fun selfIsExcludedByIdAndNormalizedUsername() {
        assertFalse(ConsumerIdentityPolicy.isDiscoverable(VoiceCloudUser(id = "self-id", username = "different", role = "USER"), viewer))
        assertFalse(ConsumerIdentityPolicy.isDiscoverable(VoiceCloudUser(id = "other", username = "  ASHOK  ", role = "CREATOR"), viewer))
    }
}
