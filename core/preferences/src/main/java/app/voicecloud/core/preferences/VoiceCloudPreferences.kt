package app.voicecloud.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

enum class ThemePreference { LIGHT, DARK, SYSTEM }
enum class LastPortal { USER, CREATOR }

private val Context.voiceCloudDataStore by preferencesDataStore(name = "voicecloud_preferences")

class VoiceCloudPreferences(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("appearance_theme")
        val lastPortal = stringPreferencesKey("last_portal")
        val sessionId = stringPreferencesKey("session_id")
        val deviceId = stringPreferencesKey("device_id")
        val onboardingCompletedUsers = stringSetPreferencesKey("onboarding_completed_users")
    }

    /** Fresh/public VoiceCloud launches are Light-first until an authenticated preference is applied. */
    val theme: Flow<ThemePreference> = context.voiceCloudDataStore.data.map { prefs ->
        prefs[Keys.theme]?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() } ?: ThemePreference.LIGHT
    }

    val lastPortal: Flow<LastPortal?> = context.voiceCloudDataStore.data.map { prefs ->
        prefs[Keys.lastPortal]?.let { runCatching { LastPortal.valueOf(it) }.getOrNull() }
    }

    val sessionId: Flow<String?> = context.voiceCloudDataStore.data.map { it[Keys.sessionId] }
    val deviceId: Flow<String?> = context.voiceCloudDataStore.data.map { it[Keys.deviceId] }
    suspend fun setTheme(value: ThemePreference) {
        context.voiceCloudDataStore.edit { it[Keys.theme] = value.name }
    }
    suspend fun setLastPortal(value: LastPortal) {
        context.voiceCloudDataStore.edit { it[Keys.lastPortal] = value.name }
    }
    suspend fun setSessionMetadata(sessionId: String?, deviceId: String?) {
        context.voiceCloudDataStore.edit {
            if (sessionId == null) it.remove(Keys.sessionId) else it[Keys.sessionId] = sessionId
            if (deviceId == null) it.remove(Keys.deviceId) else it[Keys.deviceId] = deviceId
        }
    }
    suspend fun hasCompletedOnboarding(userId: String): Boolean =
        context.voiceCloudDataStore.data.first()[Keys.onboardingCompletedUsers]?.contains(userId) == true

    suspend fun markOnboardingCompleted(userId: String) {
        context.voiceCloudDataStore.edit { prefs ->
            val completed = prefs[Keys.onboardingCompletedUsers].orEmpty().toMutableSet()
            completed += userId
            prefs[Keys.onboardingCompletedUsers] = completed
        }
    }
    suspend fun clearAccountPreferences() {
        context.voiceCloudDataStore.edit {
            it.remove(Keys.theme)
            it.remove(Keys.lastPortal)
            it.remove(Keys.sessionId)
            it.remove(Keys.deviceId)
        }
    }
}
