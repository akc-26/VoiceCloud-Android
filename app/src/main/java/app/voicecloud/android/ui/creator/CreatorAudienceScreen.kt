package app.voicecloud.android.ui.creator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCChip
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

private val AudienceScopes = listOf("Followers", "Subscribers", "Plans")

@Composable
fun CreatorAudienceScreen(
    state: CreatorAudienceUiState,
    modifier: Modifier = Modifier,
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    var scope by rememberSaveable { mutableStateOf(AudienceScopes.first()) }
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Audience", subtitle = "Followers, subscribers, and plans")
        when {
            state.errorMessage != null -> VCErrorState(title = "Couldn't load audience", message = state.errorMessage)
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
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = spacing.pageGutter, vertical = spacing.xs),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            items(AudienceScopes) { label ->
                                VCChip(label = label, selected = label == scope, onClick = { scope = label })
                            }
                        }
                    }
                    when (scope) {
                        "Followers" -> {
                            item { VCSectionHeader(title = "Followers") }
                            if (state.followers.isEmpty()) {
                                item {
                                    CreatorMuted(
                                        "Followers will appear here when audience data is connected.",
                                        Modifier.padding(horizontal = spacing.pageGutter),
                                    )
                                }
                            } else {
                                items(state.followers, key = { it.name }) { person ->
                                    VCPersonRow(person = person)
                                }
                            }
                        }
                        "Subscribers" -> {
                            item { VCSectionHeader(title = "Subscribers") }
                            if (state.subscribers.isEmpty()) {
                                item {
                                    CreatorMuted(
                                        "Subscribers will appear here when subscription data is connected.",
                                        Modifier.padding(horizontal = spacing.pageGutter),
                                    )
                                }
                            } else {
                                items(state.subscribers, key = { it.name }) { person ->
                                    VCPersonRow(person = person)
                                }
                            }
                        }
                        else -> {
                            item { VCSectionHeader(title = "Plans") }
                            item {
                                CreatorMuted(
                                    if (state.plansAvailable) {
                                        "Plan details will load from VoiceCloud when connected."
                                    } else {
                                        "Creator plans will appear here when that capability is connected."
                                    },
                                    Modifier.padding(horizontal = spacing.pageGutter),
                                )
                            }
                        }
                    }
                    if (state.followers.isEmpty() && state.subscribers.isEmpty() && !state.plansAvailable) {
                        item {
                            VCEmptyState(
                                title = "Audience is ready",
                                message = "Followers, subscribers, and plans will use VoiceCloud creator data when connected.",
                            )
                        }
                    }
                }
            }
        }
    }
}
