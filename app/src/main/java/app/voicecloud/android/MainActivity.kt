package app.voicecloud.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.voicecloud.android.ui.VoiceCloudRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceCloudRoot(initialPasswordResetToken = extractResetToken(intent))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractResetToken(intent: Intent?): String? {
        val data: Uri = intent?.data ?: return null
        val path = data.path.orEmpty()
        val isResetPath = data.host.equals("reset-password", ignoreCase = true) ||
            path.contains("reset-password", ignoreCase = true)
        if (isResetPath) {
            return data.getQueryParameter("token")?.trim()?.takeIf { it.isNotEmpty() }
        }
        return null
    }
}
