package app.voicecloud.android.deeplink

import android.net.Uri
import app.voicecloud.core.model.PasswordRecoveryConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PasswordResetDeepLinkParserTest {
    @Test
    fun customScheme_extractsToken() {
        val uri = Uri.parse("voicecloud://reset-password?token=abc123")
        assertEquals("abc123", PasswordResetDeepLinkParser.extractToken(uri, null, "https://example.com"))
    }

    @Test
    fun https_defaultPath_whenCompatibleNotSet() {
        val uri = Uri.parse("https://voicecloud.example/reset-password?token=tok")
        assertEquals(
            "tok",
            PasswordResetDeepLinkParser.extractToken(
                uri,
                null,
                "https://voicecloud.example",
            ),
        )
    }

    @Test
    fun https_usesConfiguredPathWhenCompatible() {
        val uri = Uri.parse("https://voicecloud.example/app/auth/reset?token=tok")
        val config = app.voicecloud.core.model.MobileConfig(
            passwordRecovery = PasswordRecoveryConfig(
                httpsAppLinkPath = "/app/auth/reset",
                androidAppLinkCompatible = true,
            ),
        )
        assertEquals(
            "tok",
            PasswordResetDeepLinkParser.extractToken(uri, config, "https://voicecloud.example"),
        )
    }

    @Test
    fun rejectsWrongHost() {
        val uri = Uri.parse("https://other.example/reset-password?token=tok")
        assertNull(PasswordResetDeepLinkParser.extractToken(uri, null, "https://voicecloud.example"))
    }

    @Test
    fun matchesCustomScheme() {
        assertTrue(
            PasswordResetDeepLinkParser.matchesResetDestination(
                Uri.parse("voicecloud://reset-password"),
                null,
                "https://x.com",
            ),
        )
    }
}
