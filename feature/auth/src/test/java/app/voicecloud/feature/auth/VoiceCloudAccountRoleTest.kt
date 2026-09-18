package app.voicecloud.feature.auth

import app.voicecloud.core.model.VoiceCloudAccountRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCloudAccountRoleTest {
    @Test
    fun creatorPortalRequiresCreatorRole() {
        assertTrue(VoiceCloudAccountRole.CREATOR.canAccessCreatorPortal())
        assertFalse(VoiceCloudAccountRole.USER.canAccessCreatorPortal())
        assertTrue(VoiceCloudAccountRole.USER.canAccessUserPortal())
        assertTrue(VoiceCloudAccountRole.GUEST.canAccessUserPortal())
    }
}
