package app.voicecloud.android.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiEndpointResolverTest {
    private val live = "https://voicecloud.tailfca77b.ts.net"

    @Test fun liveServerRootExpandsToVersionedApiBase() {
        val endpoints = ApiEndpointResolver.resolve(live, live, live, debug = true)
        assertEquals("$live/api/v1/", endpoints.apiBaseUrl)
    }

    @Test fun fullApiBaseIsNotDuplicated() {
        val endpoints = ApiEndpointResolver.resolve("$live/api/v1/", live, live, debug = true)
        assertEquals("$live/api/v1/", endpoints.apiBaseUrl)
    }

    @Test fun socketUsesServerOriginAndWebUsesTrailingSlash() {
        val endpoints = ApiEndpointResolver.resolve(live, "$live/", live, debug = true)
        assertEquals(live, endpoints.socketBaseUrl)
        assertEquals("$live/", endpoints.webBaseUrl)
    }

    @Test fun releaseUsesConfiguredLiveAuthoritiesWithoutDeviceRewrite() {
        val endpoints = ApiEndpointResolver.resolve(live, live, live, debug = false)
        assertEquals("$live/api/v1/", endpoints.apiBaseUrl)
        assertEquals(live, endpoints.socketBaseUrl)
        assertEquals("$live/", endpoints.webBaseUrl)
    }
}
