package app.voicecloud.android.ui.creator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCEventCard
import app.voicecloud.core.designsystem.component.VCLiveBadge
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCStatCard
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCSettingsRow
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
import app.voicecloud.android.ui.live.HostLiveMode
import app.voicecloud.android.ui.live.HostSessionStatus
import app.voicecloud.android.ui.live.statusMessage

@Composable
fun CreatorDashboardScreen(
    state: CreatorDashboardUiState,
    modifier: Modifier = Modifier,
    onOpenLiveStudio: () -> Unit = {},
    onOpenAudience: () -> Unit = {},
    onOpenAnalytics: () -> Unit = {},
    onOpenWorkspace: () -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Dashboard", subtitle = "Creator workspace")
        when {
            state.errorMessage != null -> VCErrorState(title = "Couldn't load dashboard", message = state.errorMessage)
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
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    item { VCSectionHeader(title = "Current status") }
                    item {
                        Column(Modifier.padding(horizontal = spacing.pageGutter), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            if (state.sessionStatus == HostSessionStatus.Live) {
                                VCLiveBadge()
                            }
                            Text(
                                text = state.sessionStatus.statusMessage(HostLiveMode.Host),
                                style = VoiceCloud.typography.body,
                                color = VoiceCloud.colors.textPrimary,
                            )
                            state.liveRoomTitle?.let {
                                Text(it, style = VoiceCloud.typography.actionLabel, color = VoiceCloud.colors.textSecondary)
                            } ?: CreatorMuted("Live room details will appear when your studio session is connected.")
                        }
                    }
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.pageGutter),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            VCSecondaryButton(text = "Live Studio", onClick = onOpenLiveStudio, modifier = Modifier.weight(1f))
                        }
                    }
                    item { VCSectionHeader(title = "Upcoming") }
                    if (state.upcomingEvents.isEmpty()) {
                        item { CreatorMuted("Upcoming rooms and events will appear here when scheduling is connected.", Modifier.padding(horizontal = spacing.pageGutter)) }
                    } else {
                        items(state.upcomingEvents, key = { it.title }) { event ->
                            VCEventCard(event = event, onClick = {}, modifier = Modifier.padding(horizontal = spacing.pageGutter))
                        }
                    }
                    if (state.stats.isNotEmpty()) {
                        item { VCSectionHeader(title = "Key information") }
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.pageGutter),
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            ) {
                                state.stats.forEach { stat ->
                                    VCStatCard(label = stat.label, value = stat.value, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    item { VCSectionHeader(title = "Activity") }
                    item {
                        CreatorMuted(
                            state.activitySummary ?: "Audience activity will appear here when creator analytics is connected.",
                            Modifier.padding(horizontal = spacing.pageGutter),
                        )
                    }
                    item { VCSectionHeader(title = "Economy & messages") }
                    item {
                        Column(Modifier.padding(horizontal = spacing.pageGutter), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                            CreatorMuted(state.earningsLabel ?: "Earnings will appear here when creator payouts are connected.")
                            CreatorMuted(state.messagesSummary ?: "Creator messages will appear here when messaging is connected.")
                        }
                    }
                    item { VCSectionHeader(title = "Shortcuts") }
                    item {
                        VCSettingsRow(title = "Audience", subtitle = "Followers and subscribers", icon = VoiceCloudIcons.Audience, onClick = onOpenAudience)
                    }
                    item {
                        VCSettingsRow(title = "Analytics", subtitle = "Performance insights", icon = VoiceCloudIcons.Analytics, onClick = onOpenAnalytics)
                    }
                    item {
                        VCSettingsRow(title = "Workspace", subtitle = "Tools and settings", icon = VoiceCloudIcons.CreatorTools, onClick = onOpenWorkspace)
                    }
                    if (state.stats.isEmpty() && state.upcomingEvents.isEmpty() && state.liveRoomTitle == null) {
                        item {
                            VCEmptyState(
                                title = "Dashboard is ready",
                                message = "Creator status, schedule, and metrics will use VoiceCloud when the creator experience is connected.",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CreatorMuted(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = VoiceCloud.typography.bodySecondary,
        color = VoiceCloud.colors.textMuted,
        modifier = modifier.padding(vertical = VoiceCloud.spacing.xxs),
    )
}
