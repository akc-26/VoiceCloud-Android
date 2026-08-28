package app.voicecloud.feature.creator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.feature.creator.data.CreatorRepository
import app.voicecloud.feature.creator.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CreatorViewModel @Inject constructor(private val repository: CreatorRepository) : ViewModel() {
    private val _state = MutableStateFlow(CreatorUiState())
    val state: StateFlow<CreatorUiState> = _state.asStateFlow()
    private val _events = Channel<CreatorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var activeLoad: Job? = null
    private var loadGeneration: Long = 0

    fun loadDashboard() = load {
        val maintenance = repository.maintenance()
        if (maintenance.enabled) {
            _events.send(CreatorEvent.AuthFailure(503, maintenance.message))
            return@load
        }
        _state.value = _state.value.copy(dashboard = repository.dashboard())
    }

    fun loadProfile() = load { _state.value = _state.value.copy(profile = repository.profile()) }

    fun saveProfile(bio: String?, country: String?, interests: List<String>) = save("Creator Profile Updated.", "We Couldn’t Update Your Creator Profile. Try Again.") {
        _state.value = _state.value.copy(profile = repository.updateProfile(bio, country, interests))
    }

    fun loadSettings() = load { _state.value = _state.value.copy(settings = repository.settings()) }

    fun saveSettings(value: CreatorSettings) = save("Creator Settings Updated.", "We Couldn’t Save Your Creator Settings. Try Again.") {
        _state.value = _state.value.copy(settings = repository.updateSettings(value))
    }

    fun loadCmsPages() = load { _state.value = _state.value.copy(cmsPages = repository.cmsPages()) }

    fun loadCmsPage(slug: String) = load { _state.value = _state.value.copy(cmsPage = repository.cmsPage(slug)) }

    fun contact(name: String, email: String, phone: String?, description: String) = save("Your Message Was Sent To VoiceCloud Support.", "We Couldn’t Send Your Message. Check The Details And Try Again.") {
        repository.contact(name, email, phone, description)
    }

    fun clearCmsPage() { _state.value = _state.value.copy(cmsPage = null) }
    fun clearFeedback() { _state.value = _state.value.copy(error = null, notice = null) }

    private fun load(block: suspend () -> Unit) {
        val generation = ++loadGeneration
        activeLoad?.cancel()
        activeLoad = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, notice = null)
            try { block() } catch (error: Throwable) { handle(error) }
            finally {
                if (generation == loadGeneration) _state.value = _state.value.copy(loading = false)
            }
        }
    }

    private fun save(notice: String, fallback: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null, notice = null)
            try {
                block()
                _state.value = _state.value.copy(notice = notice)
            } catch (error: Throwable) { handle(error, fallback) }
            finally { _state.value = _state.value.copy(saving = false) }
        }
    }

    private suspend fun handle(error: Throwable, fallback: String = "We Couldn’t Complete This Creator Request. Try Again.") {
        if (error is HttpException && error.code() in setOf(401, 403, 503)) {
            _events.send(CreatorEvent.AuthFailure(error.code(), safeMessage(error, fallback)))
            return
        }
        _state.value = _state.value.copy(error = safeMessage(error, fallback))
    }

    private fun safeMessage(error: Throwable, fallback: String): String {
        val message = error.message.orEmpty().trim()
        val unsafe = listOf("sql", "postgres", "typeorm", "constraint", "stack trace", "exception at", "relation ", "column ", "http 4", "http 5")
        return when {
            error is HttpException && error.code() == 401 -> "Your Session Has Expired. Sign In Again."
            error is HttpException && error.code() == 403 -> "This Creator Action Isn’t Available For Your Account."
            error is HttpException && error.code() == 429 -> "Too Many Requests. Try Again In A Moment."
            error is HttpException && error.code() >= 500 -> "VoiceCloud Is Temporarily Unavailable. Try Again Soon."
            error is HttpException -> fallback
            message.isBlank() || unsafe.any { message.contains(it, ignoreCase = true) } -> fallback
            else -> message
        }
    }
}
