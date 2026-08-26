package app.voicecloud.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import app.voicecloud.android.ui.VoiceCloudRoot
import dagger.hilt.android.AndroidEntryPoint
import java.net.URI

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val resetToken = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetToken.value = extractResetToken(intent)
        enableEdgeToEdge()
        setContent { VoiceCloudRoot(initialResetToken = resetToken.value) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resetToken.value = extractResetToken(intent)
    }

    private fun extractResetToken(intent: Intent?): String? {
        val expectedHost = runCatching { URI.create(BuildConfig.WEB_BASE_URL).host }.getOrNull() ?: return null
        return intent?.data
            ?.takeIf { uri ->
                uri.scheme.equals("https", ignoreCase = true) &&
                    uri.host.equals(expectedHost, ignoreCase = true) &&
                    uri.path == "/auth/reset-password"
            }
            ?.getQueryParameter("token")
            ?.trim()
            ?.takeIf { it.length >= 32 }
    }
}
