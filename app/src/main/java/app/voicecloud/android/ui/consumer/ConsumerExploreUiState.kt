package app.voicecloud.android.ui.consumer

import androidx.compose.runtime.Immutable
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel

/** Presentation-only Explore feed. Lists stay empty until a real discovery source is connected. */
@Immutable
data class ConsumerExploreUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val liveRooms: List<VCRoomUiModel> = emptyList(),
    val trendingRooms: List<VCRoomUiModel> = emptyList(),
    val people: List<VCPersonUiModel> = emptyList(),
    val communities: List<VCCommunityUiModel> = emptyList(),
    val events: List<VCEventUiModel> = emptyList(),
)
