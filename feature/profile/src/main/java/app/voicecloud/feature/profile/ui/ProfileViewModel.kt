package app.voicecloud.feature.profile.ui

import app.voicecloud.core.network.toVoiceCloudUserMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.feature.profile.data.ProfileRepository
import app.voicecloud.feature.profile.model.ProfileUiState
import app.voicecloud.feature.profile.model.UpdateExtendedProfileBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = mutableState.asStateFlow()

    fun loadHub() = viewModelScope.launch {
        mutateLoading(true)
        val profile = runCatching { repository.profile() }.getOrNull()
        val replays = runCatching { repository.replays() }.getOrDefault(emptyList())
        val activity = runCatching { repository.activity() }.getOrDefault(emptyList())
        val blocked = runCatching { repository.blockedUsers() }.getOrDefault(emptyList())
        val stats = runCatching { repository.visitorStats() }.getOrDefault(mutableState.value.visitorStats)
        mutableState.value = mutableState.value.copy(
            loading = false,
            profile = profile,
            replays = replays,
            activity = activity,
            blockedUsers = blocked,
            visitorStats = stats,
            error = if (profile == null) "Profile tools could not be fully loaded." else null,
        )
    }

    fun loadProfile() = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.profile() }
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, profile = it, error = null) }
            .onFailure { fail(it, "Profile could not be loaded.") }
    }

    fun saveProfile(body: UpdateExtendedProfileBody, onSaved: () -> Unit = {}) = viewModelScope.launch {
        mutateBusy(true)
        runCatching { repository.updateProfile(body) }
            .onSuccess {
                mutableState.value = mutableState.value.copy(mutating = false, profile = it, notice = "Profile updated.", error = null)
                onSaved()
            }
            .onFailure { failMutation(it, "Profile could not be updated.") }
    }

    fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String) = uploadMedia(
        success = "Profile photo updated.",
        fallback = "Profile photo could not be updated.",
    ) { repository.uploadAvatar(bytes, fileName, mimeType) }

    fun deleteAvatar() = uploadMedia(
        success = "Profile photo removed.",
        fallback = "Profile photo could not be removed.",
    ) { repository.deleteAvatar() }

    fun uploadCover(bytes: ByteArray, fileName: String, mimeType: String) = uploadMedia(
        success = "Cover photo updated.",
        fallback = "Cover photo could not be updated.",
    ) { repository.uploadCover(bytes, fileName, mimeType) }

    fun deleteCover() = uploadMedia(
        success = "Cover photo removed.",
        fallback = "Cover photo could not be removed.",
    ) { repository.deleteCover() }

    fun loadReplays() = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.replays() }
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, replays = it, error = null) }
            .onFailure { fail(it, "Replays could not be loaded.") }
    }

    fun loadReplay(id: String) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.replay(id) }
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, selectedReplay = it, error = null) }
            .onFailure { fail(it, "Replay is unavailable.") }
    }

    fun loadActivity() = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.activity() }
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, activity = it, error = null) }
            .onFailure { fail(it, "Activity could not be loaded.") }
    }

    fun loadBlockedUsers() = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.blockedUsers() }
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, blockedUsers = it, error = null) }
            .onFailure { fail(it, "Blocked users could not be loaded.") }
    }

    fun unblock(userId: String) = viewModelScope.launch {
        mutateBusy(true)
        runCatching { repository.unblock(userId) }
            .onSuccess {
                val refreshed = runCatching { repository.blockedUsers() }.getOrDefault(
                    mutableState.value.blockedUsers.filterNot { it.blockedUserId == userId || it.person.id == userId }
                )
                mutableState.value = mutableState.value.copy(mutating = false, blockedUsers = refreshed, notice = "User unblocked.", error = null)
            }
            .onFailure { failMutation(it, "User could not be unblocked.") }
    }

    fun loadVisitors() = viewModelScope.launch {
        mutateLoading(true)
        val visitors = runCatching { repository.visitors() }
        val stats = runCatching { repository.visitorStats() }.getOrDefault(mutableState.value.visitorStats)
        visitors
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, visitors = it, visitorStats = stats, error = null) }
            .onFailure { fail(it, "Profile visitors could not be loaded.") }
    }

    fun loadHelpPages() = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.helpPages() }
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, helpPages = it, error = null) }
            .onFailure { fail(it, "Help pages could not be loaded.") }
    }

    fun loadHelpPage(slug: String) = viewModelScope.launch {
        mutateLoading(true)
        runCatching { repository.helpPage(slug) }
            .onSuccess { mutableState.value = mutableState.value.copy(loading = false, selectedHelpPage = it, error = null) }
            .onFailure { fail(it, "This help page could not be loaded.") }
    }

    fun clearMessage() {
        mutableState.value = mutableState.value.copy(notice = null, error = null)
    }

    private fun uploadMedia(
        success: String,
        fallback: String,
        action: suspend () -> app.voicecloud.feature.profile.model.ExtendedProfile,
    ) = viewModelScope.launch {
        mutateBusy(true)
        runCatching { action() }
            .onSuccess { mutableState.value = mutableState.value.copy(mutating = false, profile = it, notice = success, error = null) }
            .onFailure { failMutation(it, fallback) }
    }

    private fun mutateLoading(value: Boolean) {
        mutableState.value = mutableState.value.copy(loading = value, error = null, notice = null)
    }

    private fun mutateBusy(value: Boolean) {
        mutableState.value = mutableState.value.copy(mutating = value, error = null, notice = null)
    }

    private fun fail(error: Throwable, fallback: String) {
        mutableState.value = mutableState.value.copy(loading = false, error = message(error, fallback))
    }

    private fun failMutation(error: Throwable, fallback: String) {
        mutableState.value = mutableState.value.copy(mutating = false, error = message(error, fallback))
    }

    private fun message(error: Throwable, fallback: String): String = error.toVoiceCloudUserMessage(fallback)
}
