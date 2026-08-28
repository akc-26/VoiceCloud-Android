package app.voicecloud.core.designsystem.component

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Application-wide transient feedback presentation.
 *
 * Product success/failure feedback is shown as a toast rather than occupying
 * permanent page space. The supplied state owner must clear the message in
 * [onConsumed] so navigation/recomposition cannot replay stale feedback.
 */
@Composable
fun VoiceCloudToastEffect(
    error: String?,
    notice: String?,
    onConsumed: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(error, notice) {
        val message = error?.takeIf(String::isNotBlank) ?: notice?.takeIf(String::isNotBlank)
        if (message != null) {
            Toast.makeText(
                context,
                voiceCloudTitleCase(message),
                if (error != null) Toast.LENGTH_LONG else Toast.LENGTH_SHORT,
            ).show()
            onConsumed()
        }
    }
}


@Composable
fun VoiceCloudToastEffect(error: String?, notice: String?) {
    val context = LocalContext.current
    var lastShown by remember { mutableStateOf<String?>(null) }
    val message = error?.takeIf(String::isNotBlank) ?: notice?.takeIf(String::isNotBlank)
    LaunchedEffect(message) {
        if (message == null) {
            lastShown = null
        } else if (message != lastShown) {
            Toast.makeText(
                context,
                voiceCloudTitleCase(message),
                if (error != null) Toast.LENGTH_LONG else Toast.LENGTH_SHORT,
            ).show()
            lastShown = message
        }
    }
}

/** Title-case application-authored UI copy without altering URLs, e-mails or handles. */
fun voiceCloudTitleCase(value: String): String {
    if (value.isBlank()) return value
    return value.split(Regex("(\\s+)"))
        .joinToString("") { token ->
            if (token.isBlank()) return@joinToString token
            val lead = token.takeWhile { !it.isLetterOrDigit() && it != '@' }
            val tail = token.drop(lead.length)
            if (tail.isBlank()) return@joinToString token
            val core = tail.trimEnd('.', ',', ':', ';', '!', '?', ')', ']', '}')
            val suffix = tail.drop(core.length)
            val preserve = core.startsWith("@") || core.contains("@") || core.contains("://") ||
                core.matches(Regex("[A-Z0-9_./-]{2,}")) || core.any(Char::isDigit) && core.none(Char::isLetter)
            if (preserve || core.isBlank()) token
            else lead + core.replaceFirstChar { if (it.isLetter()) it.titlecase() else it.toString() } + suffix
        }
}
