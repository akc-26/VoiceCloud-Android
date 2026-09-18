package app.voicecloud.feature.auth

import app.voicecloud.core.model.GoogleSignInConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleSignInConfigTest {
    @Test
    fun isConfigured_whenWebClientIdPresent() {
        assertTrue(GoogleSignInConfig(webClientId = "abc.apps.googleusercontent.com").isConfigured)
    }

    @Test
    fun isConfigured_falseWhenBlank() {
        assertFalse(GoogleSignInConfig(webClientId = "").isConfigured)
    }
}
