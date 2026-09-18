package app.voicecloud.android.ui.live

import androidx.compose.runtime.Immutable
import app.voicecloud.core.designsystem.component.VCPersonUiModel

enum class HostLiveMode {
    Host,
    Audience,
}

enum class HostSessionStatus {
    Idle,
    Preparing,
    Connecting,
    Live,
    Reconnecting,
    Interrupted,
    Disconnected,
    Muted,
    Speaking,
    Paused,
    Ended,
    Error,
}

/** Presentation-only host/live session state. Populated when live hosting is connected. */
@Immutable
data class HostLiveUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sessionStatus: HostSessionStatus = HostSessionStatus.Idle,
    val roomTitle: String? = null,
    val topicLabel: String? = null,
    val audienceLabel: String? = null,
    val speakers: List<VCPersonUiModel> = emptyList(),
    val listeners: List<VCPersonUiModel> = emptyList(),
    val hostMuted: Boolean = false,
    val hostSpeaking: Boolean = false,
)

fun HostSessionStatus.statusMessage(mode: HostLiveMode): String = when (this) {
    HostSessionStatus.Idle -> if (mode == HostLiveMode.Host) {
        "Prepare your room, then go live when the realtime session is connected."
    } else {
        "Join a room from Home or Explore to listen in when live audio is connected."
    }
    HostSessionStatus.Preparing -> "Preparing your live room."
    HostSessionStatus.Connecting -> "Connecting to VoiceCloud realtime."
    HostSessionStatus.Live -> "You are live."
    HostSessionStatus.Reconnecting -> "Reconnecting to the live session."
    HostSessionStatus.Interrupted -> "Live session interrupted. Check your connection."
    HostSessionStatus.Disconnected -> "Not connected to a live session."
    HostSessionStatus.Muted -> "You are muted."
    HostSessionStatus.Speaking -> "You are speaking."
    HostSessionStatus.Paused -> "Room is paused."
    HostSessionStatus.Ended -> "This live session has ended."
    HostSessionStatus.Error -> "Live session unavailable."
}
