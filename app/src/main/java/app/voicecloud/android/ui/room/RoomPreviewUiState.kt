package app.voicecloud.android.ui.room

import androidx.compose.runtime.Immutable
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel

enum class RoomPreviewStatus {
    Loading,
    Ready,
    Live,
    Private,
    Locked,
    Unavailable,
    Ended,
    Error,
}

/** Presentation-only room preview. Extended fields populate when real room data is connected. */
@Immutable
data class RoomPreviewUiState(
    val room: VCRoomUiModel,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val status: RoomPreviewStatus = RoomPreviewStatus.Ready,
    val speakers: List<VCPersonUiModel> = emptyList(),
    val audienceLabel: String? = null,
    val accessLabel: String? = null,
) {
    val topic: String? get() = room.subtitle ?: room.metaLabel

    companion object {
        fun fromRoom(room: VCRoomUiModel): RoomPreviewUiState = RoomPreviewUiState(
            room = room,
            status = when {
                room.live -> RoomPreviewStatus.Live
                else -> RoomPreviewStatus.Ready
            },
        )
    }
}
