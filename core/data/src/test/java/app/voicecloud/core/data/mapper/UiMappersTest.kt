package app.voicecloud.core.data.mapper

import app.voicecloud.core.model.DiscoveryRoomDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UiMappersTest {
    @Test
    fun mapsDiscoveryRoomToUiModel() {
        val dto = DiscoveryRoomDto(
            id = "room-1",
            title = "Night Talk",
            hostDisplayName = "Alex",
            listenerCount = 42,
            isLive = true,
        )
        val ui = dto.toRoomUiModel()
        assertNotNull(ui)
        assertEquals("room-1", ui?.id)
        assertEquals("Night Talk", ui?.title)
        assertEquals(true, ui?.live)
    }
}
