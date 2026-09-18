package app.voicecloud.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.voicecloud.android.config.VoiceCloudMobileConfigStore
import app.voicecloud.android.deeplink.PasswordResetDeepLinkParser
import app.voicecloud.android.ui.VoiceCloudRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var mobileConfigStore: VoiceCloudMobileConfigStore

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

    fun extractResetToken(intent: Intent?): String? {
        return PasswordResetDeepLinkParser.extractToken(
            data = intent?.data,
            mobileConfig = mobileConfigStore.config,
            webBaseUrl = BuildConfig.WEB_BASE_URL,
        )
    }
}
