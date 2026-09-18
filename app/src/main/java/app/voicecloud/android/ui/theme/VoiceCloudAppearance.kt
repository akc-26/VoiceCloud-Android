package app.voicecloud.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import app.voicecloud.core.preferences.ThemePreference
import app.voicecloud.core.preferences.VoiceCloudPreferences

@Composable
fun rememberVoiceCloudDarkTheme(
    preferences: VoiceCloudPreferences? = null,
): Boolean {
    val context = LocalContext.current
    val prefs = preferences ?: remember(context) { VoiceCloudPreferences(context.applicationContext) }
    var themePreference by remember { mutableStateOf(ThemePreference.LIGHT) }
    LaunchedEffect(prefs) {
        prefs.theme.collect { themePreference = it }
    }
    val systemDark = isSystemInDarkTheme()
    return when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> systemDark
    }
}

@Composable
fun VoiceCloudSystemBarAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    LaunchedEffect(darkTheme) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
