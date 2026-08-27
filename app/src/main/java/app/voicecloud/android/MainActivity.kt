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
    private val navigationRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetToken.value = extractResetToken(intent)
        navigationRoute.value = extractNavigationRoute(intent)
        enableEdgeToEdge()
        setContent { VoiceCloudRoot(initialResetToken = resetToken.value, initialNavigationRoute = navigationRoute.value) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resetToken.value = extractResetToken(intent)
        navigationRoute.value = extractNavigationRoute(intent)
    }

    private fun extractNavigationRoute(intent: Intent?): String? {
        val route = intent?.getStringExtra(EXTRA_NAVIGATION_ROUTE)?.trim().orEmpty()
        return route.takeIf { value ->
            value == "notifications" || value == "communities" || value == "events" || value == "messages" || value == "rooms" ||
                value.matches(Regex("communities/[A-Za-z0-9._~-]+")) ||
                value.matches(Regex("events/[A-Za-z0-9._~-]+")) ||
                value.matches(Regex("messages/[A-Za-z0-9._~-]+"))
        }
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

    companion object { const val EXTRA_NAVIGATION_ROUTE = "voicecloud_navigation_route" }
}
