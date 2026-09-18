package app.voicecloud.android.ui.settings

import androidx.compose.runtime.Immutable
import app.voicecloud.core.preferences.ThemePreference

@Immutable
data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val accountDisplayName: String? = null,
    val accountHandle: String? = null,
    val themePreference: ThemePreference = ThemePreference.LIGHT,
    val pushNotificationsConfigured: Boolean = false,
    val pushNotificationsEnabled: Boolean = false,
    val helpAvailable: Boolean = false,
    val legalLinksAvailable: Boolean = false,
    val isGuestAccount: Boolean = false,
)
