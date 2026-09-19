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

/**
 * Normalizes application-authored UI copy without damaging whitespace, URLs,
 * e-mail addresses, handles or backend-provided identifiers.
 *
 * Short control labels are presented in a restrained title case; descriptive
 * sentences keep their authored casing so body copy remains natural and easy
 * to read on a phone. Whitespace is intentionally preserved.
 */
fun voiceCloudTitleCase(value: String): String {
    if (value.isBlank()) return value

    val trimmed = value.trim()
    val words = Regex("\\S+").findAll(trimmed).map { it.value }.toList()
    val isSentence = words.size > 5 || trimmed.any { it == '.' || it == '!' || it == '?' }

    if (isSentence) {
        val firstLetter = value.indexOfFirst(Char::isLetter)
        if (firstLetter < 0) return value
        return buildString(value.length) {
            append(value, 0, firstLetter)
            append(value[firstLetter].titlecase())
            append(value, firstLetter + 1, value.length)
        }
    }

    val minorWords = setOf("a", "an", "and", "as", "at", "by", "for", "in", "of", "on", "or", "the", "to", "with")
    var wordIndex = 0
    return Regex("\\s+|\\S+").findAll(value).joinToString(separator = "") { match ->
        val token = match.value
        if (token.firstOrNull()?.isWhitespace() == true) return@joinToString token

        val lead = token.takeWhile { !it.isLetterOrDigit() && it != '@' }
        val tail = token.drop(lead.length)
        if (tail.isBlank()) return@joinToString token
        val core = tail.trimEnd('.', ',', ':', ';', '!', '?', ')', ']', '}')
        val suffix = tail.drop(core.length)
        val preserve = core.startsWith("@") || core.contains("@") || core.contains("://") ||
            core.matches(Regex("[A-Z0-9_./-]{2,}")) || core.drop(1).any(Char::isUpperCase) ||
            (core.any(Char::isDigit) && core.none(Char::isLetter))
        val current = wordIndex++
        if (preserve || core.isBlank()) token
        else {
            val lower = core.lowercase()
            val titled = if (current > 0 && lower in minorWords) lower
            else lower.replaceFirstChar { if (it.isLetter()) it.titlecase() else it.toString() }
            lead + titled + suffix
        }
    }
}
