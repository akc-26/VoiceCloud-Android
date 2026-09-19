package app.voicecloud.feature.live.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomAccessPolicyTest {
    @Test
    fun `restriction labels reflect every supported listener gate without duplicates`() {
        val room = LiveRoomDetail(
            isLocked = true,
            isInviteOnly = true,
            isTicketRequired = true,
            isPremium = true,
            isSubscriberOnly = true,
            isVerifiedOnly = true,
            clubId = "club-1",
        )

        assertEquals(
            listOf(
                "Locked",
                "Invite only",
                "Ticket required",
                "Subscribers only",
                "Verified accounts",
                "Community room",
            ),
            room.restrictionLabels(),
        )
    }

    @Test
    fun `live active room is joinable presentation`() {
        assertTrue(LiveRoomDetail(isLive = true, status = "live", endedAt = null).isJoinablePresentation())
    }

    @Test
    fun `non live room is not joinable presentation`() {
        assertFalse(LiveRoomDetail(isLive = false, status = "scheduled", endedAt = null).isJoinablePresentation())
    }

    @Test
    fun `ended status is not joinable even if live flag is stale`() {
        assertFalse(LiveRoomDetail(isLive = true, status = "ended", endedAt = null).isJoinablePresentation())
    }

    @Test
    fun `ended timestamp is not joinable even if live flag is stale`() {
        assertFalse(LiveRoomDetail(isLive = true, status = "live", endedAt = "2026-08-27T00:00:00Z").isJoinablePresentation())
    }
}
