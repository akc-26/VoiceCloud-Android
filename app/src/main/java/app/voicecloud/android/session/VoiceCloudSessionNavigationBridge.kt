package app.voicecloud.android.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class VoiceCloudSessionNavEvent {
    SessionExpired,
}

@Singleton
class VoiceCloudSessionNavigationBridge @Inject constructor() {
    private val _events = MutableSharedFlow<VoiceCloudSessionNavEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<VoiceCloudSessionNavEvent> = _events.asSharedFlow()

    fun notifySessionExpired() {
        _events.tryEmit(VoiceCloudSessionNavEvent.SessionExpired)
    }
}
