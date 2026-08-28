package app.voicecloud.feature.hosting.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HostingModelTest {
    @Test fun defaultsAreFailClosed() {
        val state = HostingUiState()
        assertFalse(state.rtcConnected)
        assertFalse(state.microphoneEnabled)
        assertEquals("PUBLIC", ScheduledRoomInput("T", scheduledStartTime = "2026-08-28T10:00:00+05:30", timeZone = "Asia/Kolkata").visibility)
    }
}
