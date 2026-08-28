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

    fun loadSettings() = read { copy(preferences = repository.preferences()) }
    fun loadPrivacy() = read { copy(privacy = repository.privacy()) }
    fun loadSecurity() = read { copy(security = repository.securitySnapshot()) }
    fun loadSession(id: String) = read { copy(session = repository.session(id), device = null) }
    fun loadDevice(id: String) = read { copy(device = repository.device(id), session = null) }
    fun loadCmsPages() = read { copy(cmsPages = repository.cmsPages()) }
    fun loadCmsPage(slug: String) = read { copy(cmsPage = repository.cmsPage(slug)) }
    fun loadReports() = read { copy(reports = repository.myReports()) }
    fun loadReporting(type: ReportTargetType, id: String, fallbackLabel: String?) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, notice = null, reportTargets = emptyList()) }
            try {
                val target = repository.resolveReportTarget(type, id, fallbackLabel)
                val reports = repository.myReports()
                _state.update { it.copy(loading = false, reportTarget = target, reports = reports) }
            } catch (t: Throwable) {
                handleFailure(t, loading = false)
            }
        }
    }
    fun loadMaintenance() = read { copy(maintenance = repository.maintenance()) }

    fun saveNotifications(value: NotificationPreferences) = mutate("Notification preferences saved.") {
        copy(preferences = repository.updateNotifications(value))
    }

    fun saveVoice(value: VoicePreferences) = mutate("Voice preferences saved.") {
        copy(preferences = repository.updateVoice(value))
    }

    fun saveTheme(value: ThemePreference) = mutate("Appearance updated.") {
        copy(preferences = repository.updateTheme(value))
    }

    fun savePrivacy(value: PrivacyPreferences) = mutate("Privacy preferences saved.") {
        copy(privacy = repository.updatePrivacy(value))
    }

    fun revokeSession(id: String, onSuccess: () -> Unit = {}) = mutate("Session access revoked.", onSuccess) {
        repository.revokeSession(id)
        copy(session = null, security = repository.securitySnapshot())
    }

    fun revokeDevice(id: String, onSuccess: () -> Unit = {}) = mutate("Device access revoked.", onSuccess) {
        repository.revokeDevice(id)
        copy(device = null, security = repository.securitySnapshot())
    }

    fun resolveReportTarget(type: ReportTargetType, id: String, fallbackLabel: String?) = read {
        copy(reportTarget = repository.resolveReportTarget(type, id, fallbackLabel), reportTargets = emptyList())
    }

    fun searchReportTargets(query: String, type: ReportTargetType?) {
        _state.update { it.copy(reportTarget = null, error = null) }
        if (query.trim().length < 2) {
            _state.update { it.copy(reportTargets = emptyList()) }
            return
        }
        read { copy(reportTargets = repository.searchReportTargets(query, type), reportTarget = null) }
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
        mutate("Report submitted.", onSuccess) {
            repository.submitReport(target, reason, description)
            copy(reports = repository.myReports())
        }
    }

    fun sendSupport(name: String, email: String, phone: String?, description: String, onSuccess: () -> Unit = {}) =
        mutate("Your message was sent to VoiceCloud support.", onSuccess) {
            repository.contact(name, email, phone, description)
            this
        }

    private fun read(transform: suspend SettingsUiState.() -> SettingsUiState) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, notice = null) }
            try {
                _state.value = _state.value.transform().copy(loading = false)
            } catch (t: Throwable) {
                handleFailure(t, loading = false)
            }
        }
    }

    private fun mutate(notice: String, onSuccess: () -> Unit = {}, transform: suspend SettingsUiState.() -> SettingsUiState) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, notice = null) }
            try {
                _state.value = _state.value.transform().copy(saving = false, notice = notice)
                onSuccess()
            } catch (t: Throwable) {
                handleFailure(t, saving = false)
            }
        }
    }

    private suspend fun handleFailure(t: Throwable, loading: Boolean = _state.value.loading, saving: Boolean = _state.value.saving) {
        val http = t as? HttpException
        when (http?.code()) {
            401 -> _events.send(SettingsEvent.SessionExpired)
            503 -> _events.send(SettingsEvent.Maintenance(safeMessage(t)))
        }
        _state.update { it.copy(loading = loading, saving = saving, error = safeMessage(t)) }
    }

    private fun safeMessage(t: Throwable): String {
        val raw = t.message.orEmpty()
        val unsafe = listOf("relation ", "sql", "postgres", "typeorm", "select ", "insert ", "constraint", "stack trace")
            .any { raw.contains(it, ignoreCase = true) }
        return when {
            t is IllegalArgumentException && raw.isNotBlank() -> raw
            unsafe -> "VoiceCloud could not complete that request. Please try again."
            t is HttpException && t.code() == 401 -> "Your session has expired. Sign in again."
            t is HttpException && t.code() == 403 -> "This action is not available for your account."
            t is HttpException && t.code() >= 500 -> "VoiceCloud is temporarily unavailable. Please try again."
            raw.isNotBlank() && raw.length <= 180 -> raw
            else -> "VoiceCloud could not complete that request. Please try again."
        }
    }
}
