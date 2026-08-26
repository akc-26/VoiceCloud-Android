package app.voicecloud.feature.bootstrap

import app.voicecloud.core.model.MobileConfig

sealed interface BootstrapState {
    data object Loading : BootstrapState
    data class Maintenance(val message: String) : BootstrapState
    data class ForceUpdate(val latest: String?, val minimum: String?, val message: String?, val downloadUrl: String?) : BootstrapState
    data class Ready(val config: MobileConfig) : BootstrapState
    data class Error(val message: String, val canRetry: Boolean = true) : BootstrapState
}
