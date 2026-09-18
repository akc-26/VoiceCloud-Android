package app.voicecloud.android.ui.economy

import androidx.compose.runtime.Immutable
import app.voicecloud.core.designsystem.component.VCGiftUiModel

@Immutable
data class EconomyTransactionUiModel(
    val id: String,
    val title: String,
    val amountLabel: String,
    val metaLabel: String? = null,
)

/** Presentation-only economy state. Values populate when wallet and gifts are connected. */
@Immutable
data class ConsumerEconomyUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val balanceLabel: String? = null,
    val balanceCaption: String = "Balance",
    val statusMessage: String? = null,
    val progressLabel: String? = null,
    val progressValueLabel: String? = null,
    val gifts: List<VCGiftUiModel> = emptyList(),
    val transactions: List<EconomyTransactionUiModel> = emptyList(),
)
