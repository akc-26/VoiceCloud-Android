package app.voicecloud.core.network

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class UserFacingErrorsTest {
    @Test fun `technical database text is never exposed`() {
        val error = IllegalStateException("relation users does not exist SQL constraint")
        assertEquals("Profile Couldn’t Be Updated. Try Again.", error.toVoiceCloudUserMessage("Profile Couldn’t Be Updated. Try Again."))
    }

    @Test fun `safe local validation text can be preserved`() {
        val error = IllegalArgumentException("Enter A Valid Email Address")
        assertEquals("Enter A Valid Email Address", error.toVoiceCloudUserMessage())
    }

    @Test fun `http 400 uses contextual fallback instead of status text`() {
        val error = HttpException(Response.error<Any>(400, "bad request".toResponseBody()))
        assertEquals("Privacy Settings Couldn’t Be Saved. Try Again.", error.toVoiceCloudUserMessage("Privacy Settings Couldn’t Be Saved. Try Again."))
    }

    @Test fun `http 500 is mapped to common availability text`() {
        val error = HttpException(Response.error<Any>(500, "internal".toResponseBody()))
        assertEquals("VoiceCloud Is Temporarily Unavailable. Try Again Soon.", error.toVoiceCloudUserMessage())
    }
}
