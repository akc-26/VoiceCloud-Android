package app.voicecloud.feature.bootstrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.voicecloud.core.model.AppIdentity
import app.voicecloud.core.model.MobileConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BootstrapViewModel @Inject constructor(
    private val repository: BootstrapRepository,
    private val appIdentity: AppIdentity,
) : ViewModel() {
    private val _state = MutableStateFlow<BootstrapState>(BootstrapState.Loading)
    val state: StateFlow<BootstrapState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = BootstrapState.Loading
            _state.value = when (val result = repository.load()) {
                is BootstrapResult.Failure -> BootstrapState.Error(result.error.message, result.error.retryable)
                is BootstrapResult.Success -> decide(result.config)
            }
        }
    }

    private fun decide(config: MobileConfig): BootstrapState {
        if (config.maintenanceMode) return BootstrapState.Maintenance(config.maintenanceMessage)
        val version = config.appVersion
        if (version != null && VersionPolicy.requiresForceUpdate(appIdentity.versionName, version.minSupportedVersion, version.forceUpdate)) {
            return BootstrapState.ForceUpdate(
                latest = version.latestVersion,
                minimum = version.minSupportedVersion,
                message = version.releaseNotes,
                downloadUrl = version.downloadUrl,
            )
        }
        return BootstrapState.Ready(config)
    }
}
