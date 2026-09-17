package app.voicecloud.android.ui.consumer

import androidx.compose.runtime.Immutable
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomUiModel

/** Presentation-only search results. Lists stay empty until VoiceCloud search is connected. */
@Immutable
data class ConsumerSearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rooms: List<VCRoomUiModel> = emptyList(),
    val people: List<VCPersonUiModel> = emptyList(),
    val communities: List<VCCommunityUiModel> = emptyList(),
) {
    val isIdle: Boolean get() = query.isBlank() && !isLoading && errorMessage == null
    val hasResults: Boolean get() = rooms.isNotEmpty() || people.isNotEmpty() || communities.isNotEmpty()
}
