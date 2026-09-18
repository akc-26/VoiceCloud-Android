package app.voicecloud.android.ui.creator

import androidx.compose.runtime.Immutable
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.android.ui.live.HostSessionStatus

@Immutable
data class CreatorStatItem(
    val label: String,
    val value: String,
)

@Immutable
data class CreatorDashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sessionStatus: HostSessionStatus = HostSessionStatus.Idle,
    val liveRoomTitle: String? = null,
    val upcomingEvents: List<VCEventUiModel> = emptyList(),
    val stats: List<CreatorStatItem> = emptyList(),
    val activitySummary: String? = null,
    val earningsLabel: String? = null,
    val messagesSummary: String? = null,
)

@Immutable
data class CreatorAudienceUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val followers: List<VCPersonUiModel> = emptyList(),
    val subscribers: List<VCPersonUiModel> = emptyList(),
    val plansAvailable: Boolean = false,
)

@Immutable
data class CreatorAnalyticsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val stats: List<CreatorStatItem> = emptyList(),
    val rankings: List<CreatorRankingItem> = emptyList(),
)

@Immutable
data class CreatorRankingItem(
    val title: String,
    val valueLabel: String,
)

@Immutable
data class CreatorWorkspaceToolsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val walletBalanceLabel: String? = null,
    val earningsLabel: String? = null,
    val giftsAvailable: Boolean = false,
    val payoutsAvailable: Boolean = false,
    val notificationsAvailable: Boolean = false,
)
