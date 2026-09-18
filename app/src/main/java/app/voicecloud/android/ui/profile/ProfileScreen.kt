package app.voicecloud.android.ui.profile

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
import app.voicecloud.core.designsystem.component.VCCommunityCard
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCEventCard
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCProfileHeader
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSettingsRow
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCStatCard
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun ProfileScreen(
    state: ConsumerProfileUiState,
    onOpenCreatorWorkspace: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenWallet: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onPersonClick: (app.voicecloud.core.designsystem.component.VCPersonUiModel) -> Unit = {},
    onCommunityClick: (app.voicecloud.core.designsystem.component.VCCommunityUiModel) -> Unit = {},
    onEventClick: (app.voicecloud.core.designsystem.component.VCEventUiModel) -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Profile", subtitle = "Your VoiceCloud identity")
        when {
            state.errorMessage != null -> VCErrorState(
                title = "Couldn't load profile",
                message = state.errorMessage,
            )
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
                ProfileContent(
                    state = state,
                    onOpenCreatorWorkspace = onOpenCreatorWorkspace,
                    onNotificationsClick = onNotificationsClick,
                    onOpenWallet = onOpenWallet,
                    onPersonClick = onPersonClick,
                    onCommunityClick = onCommunityClick,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ConsumerProfileUiState,
    onOpenCreatorWorkspace: () -> Unit,
    onNotificationsClick: (() -> Unit)?,
    onOpenWallet: (() -> Unit)?,
    onPersonClick: (app.voicecloud.core.designsystem.component.VCPersonUiModel) -> Unit,
    onCommunityClick: (app.voicecloud.core.designsystem.component.VCCommunityUiModel) -> Unit,
    onEventClick: (app.voicecloud.core.designsystem.component.VCEventUiModel) -> Unit,
) {
    val spacing = VoiceCloud.spacing
    val identityName = state.displayName ?: "Your profile"
    val hasIdentity = state.displayName != null || state.handle != null || state.bio != null
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        item {
            VCProfileHeader(
                name = identityName,
                handle = state.handle,
                bio = state.bio ?: if (!hasIdentity) {
                    "Your profile will use your VoiceCloud account when authentication is connected."
                } else {
                    null
                },
            )
        }
        if (state.stats.isNotEmpty()) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.pageGutter),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    state.stats.forEach { stat ->
                        VCStatCard(
                            label = stat.label,
                            value = stat.value,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        item { VCSectionHeader(title = "Social") }
        if (state.people.isEmpty()) {
            item { ProfileMuted("People and creators you follow will appear here when social is connected.") }
        } else {
            items(state.people, key = { it.name }) { person ->
                VCPersonRow(person = person, onClick = { onPersonClick(person) })
            }
        }
        if (state.communities.isEmpty()) {
            item { ProfileMuted("Communities will appear here when this experience is connected.") }
        } else {
            items(state.communities, key = { it.name }) { community ->
                VCCommunityCard(
                    community = community,
                    onClick = { onCommunityClick(community) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        if (state.events.isEmpty()) {
            item { ProfileMuted("Events will appear here when this experience is connected.") }
        } else {
            items(state.events, key = { it.title }) { event ->
                VCEventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        if (state.notificationsAvailable && onNotificationsClick != null) {
            item {
                VCSettingsRow(
                    title = "Notifications",
                    subtitle = "Social and room alerts",
                    icon = VoiceCloudIcons.Notifications,
                    onClick = onNotificationsClick,
                )
            }
        }
        if (onOpenWallet != null) {
            item { VCSectionHeader(title = "Economy") }
            item {
                VCSettingsRow(
                    title = "Wallet & gifts",
                    subtitle = "Balance, gifts, and history",
                    icon = VoiceCloudIcons.Wallet,
                    onClick = onOpenWallet,
                )
            }
        }
        item { VCSectionHeader(title = "Workspace") }
        item {
            VCSettingsRow(
                title = "Creator workspace",
                subtitle = "Manage your creator tools",
                icon = VoiceCloudIcons.CreatorTools,
                onClick = onOpenCreatorWorkspace,
            )
        }
        if (!hasIdentity && state.stats.isEmpty() && state.people.isEmpty()) {
            item {
                VCEmptyState(
                    title = "Profile is ready",
                    message = "Identity, social connections, and stats will appear from VoiceCloud when your account is connected.",
                )
            }
        }
    }
}

@Composable
private fun ProfileMuted(message: String) {
    Text(
        text = message,
        style = VoiceCloud.typography.bodySecondary,
        color = VoiceCloud.colors.textMuted,
        modifier = Modifier.padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.xxs),
    )
}
