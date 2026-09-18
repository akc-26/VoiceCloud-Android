package app.voicecloud.android.navigation

import app.voicecloud.core.data.VoiceCloudNotificationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRoutingTest {
    @Test
    fun resolvesConversationFromExplicitField() {
        val item = VoiceCloudNotificationItem(
            id = "1",
            title = "Message",
            body = null,
            isRead = false,
            createdAt = null,
            type = null,
            targetType = null,
            targetId = null,
            actionUrl = null,
            conversationId = "conv-1",
            roomId = null,
            userId = null,
            clubId = null,
        )
        assertTrue(item.resolveDestination() is NotificationDestination.Conversation)
        assertEquals("conv-1", (item.resolveDestination() as NotificationDestination.Conversation).conversationId)
    }

    @Test
    fun resolvesProfileFromTargetType() {
        val item = VoiceCloudNotificationItem(
            id = "2",
            title = "Follow",
            body = null,
            isRead = false,
            createdAt = null,
            type = "follow",
            targetType = "user",
            targetId = "user-9",
            actionUrl = null,
            conversationId = null,
            roomId = null,
            userId = null,
            clubId = null,
        )
        val destination = item.resolveDestination()
        assertTrue(destination is NotificationDestination.PublicProfile)
        assertEquals("user-9", (destination as NotificationDestination.PublicProfile).userId)
    }
}
