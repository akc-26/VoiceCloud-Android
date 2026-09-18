package app.voicecloud.android.ui.consumer

import androidx.compose.runtime.Immutable
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel

/** Home feed backed by VoiceCloud discovery APIs. */
@Immutable
data class ConsumerHomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val liveRooms: List<VCRoomUiModel> = emptyList(),
    val recommendedRooms: List<VCRoomUiModel> = emptyList(),
    val people: List<VCPersonUiModel> = emptyList(),
    val communities: List<VCCommunityUiModel> = emptyList(),
    val events: List<VCEventUiModel> = emptyList(),
)
