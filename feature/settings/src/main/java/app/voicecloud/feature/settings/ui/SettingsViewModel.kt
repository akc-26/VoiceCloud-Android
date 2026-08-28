package app.voicecloud.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.preferences.ThemePreference
import app.voicecloud.core.preferences.VoiceCloudPreferences
import app.voicecloud.feature.settings.data.SettingsRepository
import app.voicecloud.feature.settings.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

data class SettingsUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val preferences: UserPreferences? = null,
    val privacy: PrivacyPreferences? = null,
    val security: SecuritySnapshot? = null,
    val session: SafeSession? = null,
    val device: SafeDevice? = null,
    val cmsPages: List<CmsPageSummary> = emptyList(),
    val cmsPage: CmsPageDetail? = null,
    val reportTarget: ReportTarget? = null,
    val reportTargets: List<ReportTarget> = emptyList(),
    val reports: List<MyReport> = emptyList(),
    val maintenance: MaintenanceState? = null,
)

sealed interface SettingsEvent {
    data object SessionExpired : SettingsEvent
    data class Maintenance(val message: String?) : SettingsEvent
}

@HiltViewModel
class AppearanceViewModel @Inject constructor(preferences: VoiceCloudPreferences) : ViewModel() {
    val theme: StateFlow<ThemePreference> = preferences.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ThemePreference.LIGHT,
    )
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()
    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun clearFeedback() { _state.update { it.copy(error = null, notice = null) } }
    fun prepareReport() { _state.update { it.copy(reportTarget = null, reportTargets = emptyList(), error = null, notice = null) } }

    fun loadSettings() = read("We Couldn’t Load Your Settings. Try Again.") { copy(preferences = repository.preferences()) }
    fun loadPrivacy() = read("We Couldn’t Load Your Privacy Settings. Try Again.") { copy(privacy = repository.privacy()) }
    fun loadSecurity() = read("We Couldn’t Load Your Security Information. Try Again.") { copy(security = repository.securitySnapshot()) }
    fun loadSession(id: String) = read("We Couldn’t Load This Session. It May No Longer Be Active.") { copy(session = repository.session(id), device = null) }
    fun loadDevice(id: String) = read("We Couldn’t Load This Device. It May No Longer Be Registered.") { copy(device = repository.device(id), session = null) }
    fun loadCmsPages() = read("We Couldn’t Load Help And Legal Information. Try Again.") { copy(cmsPages = repository.cmsPages()) }
    fun loadCmsPage(slug: String) = read("We Couldn’t Load This Page. Try Again.") { copy(cmsPage = repository.cmsPage(slug)) }
    fun loadReports() = read("We Couldn’t Load Your Reports. Try Again.") { copy(reports = repository.myReports()) }
    fun loadReporting(type: ReportTargetType, id: String, fallbackLabel: String?) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, notice = null, reportTargets = emptyList()) }
            try {
                val target = repository.resolveReportTarget(type, id, fallbackLabel)
                val reports = repository.myReports()
                _state.update { it.copy(loading = false, reportTarget = target, reports = reports) }
            } catch (t: Throwable) {
                handleFailure(t, "We Couldn’t Load The Report Details. Try Again.", loading = false)
            }
        }
    }
    fun loadMaintenance() = read("We Couldn’t Check Service Availability. Try Again.") { copy(maintenance = repository.maintenance()) }

    fun saveNotifications(value: NotificationPreferences) = mutate("Notification Preferences Saved.", "We Couldn’t Save Your Notification Preferences. Try Again.") {
        copy(preferences = repository.updateNotifications(value))
    }

    fun saveVoice(value: VoicePreferences) = mutate("Voice Preferences Saved.", "We Couldn’t Save Your Voice Preferences. Try Again.") {
        copy(preferences = repository.updateVoice(value))
    }

    fun saveTheme(value: ThemePreference) = mutate("Appearance Updated.", "We Couldn’t Update Your Appearance. Try Again.") {
        copy(preferences = repository.updateTheme(value))
    }

    fun savePrivacy(value: PrivacyPreferences) = mutate("Privacy Settings Saved.", "We Couldn’t Save Your Privacy Settings. Try Again.") {
        copy(privacy = repository.updatePrivacy(value))
    }

    fun revokeSession(id: String, onSuccess: () -> Unit = {}) = mutate("Session Access Revoked.", "We Couldn’t Revoke This Session. Try Again.", onSuccess) {
        repository.revokeSession(id)
        copy(session = null, security = repository.securitySnapshot())
    }

    fun revokeDevice(id: String, onSuccess: () -> Unit = {}) = mutate("Device Access Revoked.", "We Couldn’t Revoke This Device. Try Again.", onSuccess) {
        repository.revokeDevice(id)
        copy(device = null, security = repository.securitySnapshot())
    }

    fun resolveReportTarget(type: ReportTargetType, id: String, fallbackLabel: String?) = read("We Couldn’t Load The Report Details. Try Again.") {
        copy(reportTarget = repository.resolveReportTarget(type, id, fallbackLabel), reportTargets = emptyList())
    }

    fun searchReportTargets(query: String, type: ReportTargetType?) {
        _state.update { it.copy(reportTarget = null, error = null) }
        if (query.trim().length < 2) {
            _state.update { it.copy(reportTargets = emptyList()) }
            return
        }
        read("We Couldn’t Search Right Now. Try Again.") { copy(reportTargets = repository.searchReportTargets(query, type), reportTarget = null) }
    }

    fun selectReportTarget(target: ReportTarget) {
        _state.update { it.copy(reportTarget = target, reportTargets = emptyList(), error = null) }
    }

    fun submitReport(reason: ReportReason, description: String?, onSuccess: () -> Unit = {}) {
        val target = _state.value.reportTarget
        if (target == null) {
            _state.update { it.copy(error = "Choose the person or room you want to report.") }
            return
        }
        mutate("Report Submitted.", "We Couldn’t Submit Your Report. Try Again.", onSuccess) {
            repository.submitReport(target, reason, description)
            copy(reports = repository.myReports())
        }
    }

    fun sendSupport(name: String, email: String, phone: String?, description: String, onSuccess: () -> Unit = {}) =
        mutate("Message Sent To VoiceCloud Support.", "We Couldn’t Send Your Message. Check The Details And Try Again.", onSuccess) {
            repository.contact(name, email, phone, description)
            this
        }

    private fun read(fallback: String, transform: suspend SettingsUiState.() -> SettingsUiState) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, notice = null) }
            try {
                _state.value = _state.value.transform().copy(loading = false)
            } catch (t: Throwable) {
                handleFailure(t, fallback, loading = false)
            }
        }
    }

    private fun mutate(
        notice: String,
        fallback: String,
        onSuccess: () -> Unit = {},
        transform: suspend SettingsUiState.() -> SettingsUiState,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, notice = null) }
            try {
                _state.value = _state.value.transform().copy(saving = false, notice = notice)
                onSuccess()
            } catch (t: Throwable) {
                handleFailure(t, fallback, saving = false)
            }
        }
    }

    private suspend fun handleFailure(
        t: Throwable,
        fallback: String,
        loading: Boolean = _state.value.loading,
        saving: Boolean = _state.value.saving,
    ) {
        val http = t as? HttpException
        when (http?.code()) {
            401 -> _events.send(SettingsEvent.SessionExpired)
            503 -> _events.send(SettingsEvent.Maintenance(safeMessage(t, fallback)))
        }
        _state.update { it.copy(loading = loading, saving = saving, error = safeMessage(t, fallback)) }
    }

    private fun safeMessage(t: Throwable, fallback: String): String {
        val raw = t.message.orEmpty()
        val unsafe = listOf(
            "relation ", "sql", "postgres", "typeorm", "select ", "insert ", "constraint", "stack trace",
            "http 400", "http 404", "http 409", "http 422", "http 429", "http 500", "http 502", "http 503",
        ).any { raw.contains(it, ignoreCase = true) }
        return when {
            t is IllegalArgumentException && raw.isNotBlank() -> raw
            t is HttpException && t.code() == 401 -> "Your Session Has Expired. Sign In Again."
            t is HttpException && t.code() == 403 -> "This Action Isn’t Available For Your Account."
            t is HttpException && t.code() == 429 -> "Too Many Requests. Try Again In A Moment."
            t is HttpException && t.code() >= 500 -> "VoiceCloud Is Temporarily Unavailable. Try Again Soon."
            t is HttpException && t.code() in listOf(400, 404, 409, 422) -> fallback
            unsafe -> fallback
            raw.isNotBlank() && raw.length <= 140 && !raw.contains("http", ignoreCase = true) -> raw
            else -> fallback
        }
    }
}
