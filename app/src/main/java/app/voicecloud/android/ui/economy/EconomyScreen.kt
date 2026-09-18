package app.voicecloud.android.ui.economy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCBottomSheet
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCGiftTile
import app.voicecloud.core.designsystem.component.VCGiftUiModel
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCRankingRow
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCStatCard
import app.voicecloud.core.designsystem.component.VCToast
import app.voicecloud.core.designsystem.component.VCWalletBalance
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun EconomyScreen(
    state: ConsumerEconomyUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    selectedGift: VCGiftUiModel? = null,
    onGiftSelect: (VCGiftUiModel) -> Unit = {},
    onGiftSheetDismiss: () -> Unit = {},
    onRecharge: (() -> Unit)? = null,
    onSendGift: (() -> Unit)? = null,
    toastMessage: String? = null,
    onToastDismiss: () -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    Column(modifier.fillMaxSize()) {
        VCPageHeader(
            title = "Wallet",
            subtitle = "Balance, gifts, and activity",
            onBack = onBack,
        )
        when {
            state.errorMessage != null -> VCErrorState(
                title = "Couldn't load wallet",
                message = state.errorMessage,
            )
            state.isLoading -> Column(
                Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                VCSkeleton()
                VCSkeleton()
                VCSkeleton()
            }
            else -> AnimatedVisibility(
                visible = true,
                enter = if (motionEnabled) fadeIn(tween(VoiceCloudMotion.EntranceMs)) else fadeIn(tween(0)),
            ) {
                EconomyContent(
                    state = state,
                    selectedGift = selectedGift,
                    onGiftSelect = onGiftSelect,
                    onGiftSheetDismiss = onGiftSheetDismiss,
                    onRecharge = onRecharge,
                    onSendGift = onSendGift,
                )
            }
        }
        VCToast(message = toastMessage, onDismiss = onToastDismiss)
    }
}

@Composable
private fun EconomyContent(
    state: ConsumerEconomyUiState,
    selectedGift: VCGiftUiModel?,
    onGiftSelect: (VCGiftUiModel) -> Unit,
    onGiftSheetDismiss: () -> Unit,
    onRecharge: (() -> Unit)?,
    onSendGift: (() -> Unit)?,
) {
    val spacing = VoiceCloud.spacing
    val hasEconomyData = state.balanceLabel != null ||
        state.gifts.isNotEmpty() ||
        state.transactions.isNotEmpty() ||
        state.progressLabel != null
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item {
            if (state.balanceLabel != null) {
                VCWalletBalance(
                    amountLabel = state.balanceLabel,
                    caption = state.balanceCaption,
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            } else {
                EconomyMuted(
                    "Your balance will appear here when the VoiceCloud wallet is connected.",
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        state.statusMessage?.let { status ->
            item { EconomyMuted(status, modifier = Modifier.padding(horizontal = spacing.pageGutter)) }
        }
        if (state.progressLabel != null && state.progressValueLabel != null) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.pageGutter),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    VCStatCard(
                        label = state.progressLabel,
                        value = state.progressValueLabel,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (onRecharge != null || onSendGift != null) {
            item { VCSectionHeader(title = "Actions") }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.pageGutter),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    onRecharge?.let { recharge ->
                        VCSecondaryButton(
                            text = "Add value",
                            onClick = recharge,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        item { VCSectionHeader(title = "Gifts") }
        if (state.gifts.isEmpty()) {
            item {
                VCEmptyState(
                    title = "No gifts to show",
                    message = "Gift browsing will use VoiceCloud economy data when gifts are connected.",
                )
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = spacing.pageGutter),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(state.gifts, key = { it.name }) { gift ->
                        VCGiftTile(
                            gift = gift.copy(selected = selectedGift?.name == gift.name),
                            onClick = { onGiftSelect(gift) },
                            modifier = Modifier.width(spacing.xxxl * 3),
                        )
                    }
                }
            }
        }
        item { VCSectionHeader(title = "History") }
        if (state.transactions.isEmpty()) {
            item {
                EconomyMuted(
                    "Transactions will appear here when wallet activity is connected.",
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        } else {
            items(state.transactions, key = { it.id }) { transaction ->
                VCRankingRow(
                    rankLabel = "•",
                    title = transaction.title,
                    valueLabel = transaction.amountLabel,
                )
                transaction.metaLabel?.let {
                    EconomyMuted(it, modifier = Modifier.padding(horizontal = spacing.pageGutter))
                }
            }
        }
        if (!hasEconomyData) {
            item {
                VCEmptyState(
                    title = "Economy is ready",
                    message = "Balance, gifts, and history will use VoiceCloud wallet and payment capabilities when they are connected.",
                )
            }
        }
    }
    VCBottomSheet(
        visible = selectedGift != null,
        onDismiss = onGiftSheetDismiss,
        title = selectedGift?.name,
    ) {
        selectedGift?.let { gift ->
            Text(
                text = gift.costLabel,
                style = VoiceCloud.typography.sectionTitle,
                color = VoiceCloud.colors.coin,
            )
            Text(
                text = "Gift details will use VoiceCloud economy when sending is connected.",
                style = VoiceCloud.typography.bodySecondary,
                color = VoiceCloud.colors.textSecondary,
                modifier = Modifier.padding(top = VoiceCloud.spacing.sm, bottom = VoiceCloud.spacing.md),
            )
            if (onSendGift != null) {
                VCPrimaryButton(
                    text = "Send gift",
                    onClick = onSendGift,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun EconomyMuted(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = VoiceCloud.typography.bodySecondary,
        color = VoiceCloud.colors.textMuted,
        modifier = modifier.padding(vertical = VoiceCloud.spacing.xxs),
    )
}
