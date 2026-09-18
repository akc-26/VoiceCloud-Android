package app.voicecloud.android.ui.profile

import androidx.compose.runtime.Immutable
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel

@Immutable
data class ConsumerProfileStat(
    val label: String,
    val value: String,
)

/** Presentation-only profile and social surfaces. Fields populate when account data is connected. */
@Immutable
data class ConsumerProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val displayName: String? = null,
    val handle: String? = null,
    val bio: String? = null,
    val stats: List<ConsumerProfileStat> = emptyList(),
    val people: List<VCPersonUiModel> = emptyList(),
    val communities: List<VCCommunityUiModel> = emptyList(),
    val events: List<VCEventUiModel> = emptyList(),
    val notificationsAvailable: Boolean = false,
)
