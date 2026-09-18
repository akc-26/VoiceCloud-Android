package app.voicecloud.android.ui.creator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSettingsRow
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCWalletBalance
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun CreatorWorkspaceScreen(
    state: CreatorWorkspaceToolsUiState,
    onLeaveWorkspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Workspace", subtitle = "Creator tools and settings")
        when {
            state.errorMessage != null -> VCErrorState(title = "Couldn't load workspace", message = state.errorMessage)
            state.isLoading -> Column(
                Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                VCSkeleton()
                VCSkeleton()
            }
            else -> AnimatedVisibility(
                visible = true,
                enter = if (motionEnabled) fadeIn(tween(VoiceCloudMotion.EntranceMs)) else fadeIn(tween(0)),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    item { VCSectionHeader(title = "Wallet & earnings") }
                    item {
                        if (state.walletBalanceLabel != null) {
                            VCWalletBalance(
                                amountLabel = state.walletBalanceLabel,
                                caption = "Creator wallet",
                                modifier = Modifier.padding(horizontal = spacing.pageGutter),
                            )
                        } else {
                            CreatorMuted(
                                "Wallet balance will appear here when creator payouts are connected.",
                                Modifier.padding(horizontal = spacing.pageGutter),
                            )
                        }
                    }
                    item {
                        CreatorMuted(
                            state.earningsLabel ?: "Earnings summaries will appear here when that capability is connected.",
                            Modifier.padding(horizontal = spacing.pageGutter),
                        )
                    }
                    item { VCSectionHeader(title = "Economy") }
                    item {
                        VCSettingsRow(
                            title = "Gifts",
                            subtitle = if (state.giftsAvailable) "Manage gift presentation" else "Unavailable until gifts are connected",
                            icon = VoiceCloudIcons.Gift,
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Payouts",
                            subtitle = if (state.payoutsAvailable) "Payout settings" else "Unavailable until payouts are connected",
                            icon = VoiceCloudIcons.Wallet,
                            onClick = null,
                        )
                    }
                    item { VCSectionHeader(title = "Creator tools") }
                    item {
                        VCSettingsRow(
                            title = "Notifications",
                            subtitle = if (state.notificationsAvailable) "Creator alerts" else "Unavailable until notifications are connected",
                            icon = VoiceCloudIcons.Notifications,
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Creator settings",
                            subtitle = "Profile, safety, and studio preferences",
                            icon = VoiceCloudIcons.Settings,
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Messages",
                            subtitle = "Creator conversations",
                            icon = VoiceCloudIcons.Messages,
                            onClick = null,
                        )
                    }
                    if (state.walletBalanceLabel == null && !state.giftsAvailable && !state.payoutsAvailable) {
                        item {
                            VCEmptyState(
                                title = "Workspace is ready",
                                message = "Wallet, payouts, gifts, and creator settings will use your VoiceCloud creator account when connected.",
                            )
                        }
                    }
                    item {
                        VCPrimaryButton(
                            text = "Leave workspace",
                            onClick = onLeaveWorkspace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.pageGutter, vertical = spacing.md),
                        )
                    }
                }
            }
        }
    }
}
