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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCRankingRow
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCStatCard
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun CreatorAnalyticsScreen(
    state: CreatorAnalyticsUiState,
    modifier: Modifier = Modifier,
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Analytics", subtitle = "How your rooms perform")
        when {
            state.errorMessage != null -> VCErrorState(title = "Couldn't load analytics", message = state.errorMessage)
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
                    if (state.stats.isNotEmpty()) {
                        item { VCSectionHeader(title = "Overview") }
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
                    } else {
                        item {
                            CreatorMuted(
                                "Performance metrics will appear here when creator analytics is connected.",
                                Modifier.padding(horizontal = spacing.pageGutter),
                            )
                        }
                    }
                    item { VCSectionHeader(title = "Highlights") }
                    if (state.rankings.isEmpty()) {
                        item {
                            CreatorMuted(
                                "Rankings and trends will appear here when analytics data is connected.",
                                Modifier.padding(horizontal = spacing.pageGutter),
                            )
                        }
                    } else {
                        items(state.rankings, key = { it.title }) { row ->
                            VCRankingRow(rankLabel = "•", title = row.title, valueLabel = row.valueLabel)
                        }
                    }
                    if (state.stats.isEmpty() && state.rankings.isEmpty()) {
                        item {
                            VCEmptyState(
                                title = "Analytics is ready",
                                message = "Reach, listening, and earnings insights will use VoiceCloud when analytics is connected.",
                            )
                        }
                    }
                }
            }
        }
    }
}
