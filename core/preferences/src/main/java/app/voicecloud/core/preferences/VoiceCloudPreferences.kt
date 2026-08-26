package app.voicecloud.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemePreference { LIGHT, DARK, SYSTEM }
enum class LastPortal { USER, CREATOR }

private val Context.voiceCloudDataStore by preferencesDataStore(name = "voicecloud_preferences")

class VoiceCloudPreferences(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("appearance_theme")
        val lastPortal = stringPreferencesKey("last_portal")
    }

    /** Fresh/public VoiceCloud launches are Light-first until an authenticated preference is applied. */
    val theme: Flow<ThemePreference> = context.voiceCloudDataStore.data.map { prefs ->
        prefs[Keys.theme]?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() } ?: ThemePreference.LIGHT
    }

    val lastPortal: Flow<LastPortal?> = context.voiceCloudDataStore.data.map { prefs ->
        prefs[Keys.lastPortal]?.let { runCatching { LastPortal.valueOf(it) }.getOrNull() }
    }

    suspend fun setTheme(value: ThemePreference) = context.voiceCloudDataStore.edit { it[Keys.theme] = value.name }
    suspend fun setLastPortal(value: LastPortal) = context.voiceCloudDataStore.edit { it[Keys.lastPortal] = value.name }
    suspend fun clearAccountPreferences() = context.voiceCloudDataStore.edit {
        it.remove(Keys.theme)
        it.remove(Keys.lastPortal)
    }
}
