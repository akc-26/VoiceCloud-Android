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
    onOpenSettings: (() -> Unit)? = null,
    onEditProfile: (() -> Unit)? = null,
    onOpenPeople: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onPersonClick: (app.voicecloud.core.designsystem.component.VCPersonUiModel) -> Unit = {},
    onCommunityClick: (app.voicecloud.core.designsystem.component.VCCommunityUiModel) -> Unit = {},
    onEventClick: (app.voicecloud.core.designsystem.component.VCEventUiModel) -> Unit = {},
    onOpenSavedRooms: (() -> Unit)? = null,
    onOpenRankings: (() -> Unit)? = null,
    onOpenBlockedUsers: (() -> Unit)? = null,
    onOpenReferrals: (() -> Unit)? = null,
    onOpenTasks: (() -> Unit)? = null,
    onOpenFriends: (() -> Unit)? = null,
    onOpenProfileVisitors: (() -> Unit)? = null,
    onOpenActivityHistory: (() -> Unit)? = null,
    onOpenCommunities: (() -> Unit)? = null,
    onOpenEvents: (() -> Unit)? = null,
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
                    onOpenSettings = onOpenSettings,
                    onEditProfile = onEditProfile,
                    onOpenPeople = onOpenPeople,
                    onPersonClick = onPersonClick,
                    onCommunityClick = onCommunityClick,
                    onEventClick = onEventClick,
                    onOpenSavedRooms = onOpenSavedRooms,
                    onOpenRankings = onOpenRankings,
                    onOpenBlockedUsers = onOpenBlockedUsers,
                    onOpenReferrals = onOpenReferrals,
                    onOpenTasks = onOpenTasks,
                    onOpenFriends = onOpenFriends,
                    onOpenProfileVisitors = onOpenProfileVisitors,
                    onOpenActivityHistory = onOpenActivityHistory,
                    onOpenCommunities = onOpenCommunities,
                    onOpenEvents = onOpenEvents,
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
    onOpenSettings: (() -> Unit)?,
    onEditProfile: (() -> Unit)?,
    onOpenPeople: (() -> Unit)?,
    onPersonClick: (app.voicecloud.core.designsystem.component.VCPersonUiModel) -> Unit,
    onCommunityClick: (app.voicecloud.core.designsystem.component.VCCommunityUiModel) -> Unit,
    onEventClick: (app.voicecloud.core.designsystem.component.VCEventUiModel) -> Unit,
    onOpenSavedRooms: (() -> Unit)?,
    onOpenRankings: (() -> Unit)?,
    onOpenBlockedUsers: (() -> Unit)?,
    onOpenReferrals: (() -> Unit)?,
    onOpenTasks: (() -> Unit)?,
    onOpenFriends: (() -> Unit)?,
    onOpenProfileVisitors: (() -> Unit)?,
    onOpenActivityHistory: (() -> Unit)?,
    onOpenCommunities: (() -> Unit)?,
    onOpenEvents: (() -> Unit)?,
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
                bio = state.bio,
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
            item {
                VCEmptyState(
                    title = "No connections yet",
                    message = "Follow creators and friends to see them here.",
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        } else {
            items(state.people, key = { it.name }) { person ->
                VCPersonRow(person = person, onClick = { onPersonClick(person) })
            }
        }
        if (state.communities.isEmpty()) {
            item { ProfileMuted("You are not in any communities yet.") }
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
            item { ProfileMuted("No upcoming events on your profile.") }
        } else {
            items(state.events, key = { it.title }) { event ->
                VCEventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        if (onEditProfile != null) {
            item {
                VCSettingsRow(
                    title = "Edit profile",
                    subtitle = "Display name and bio",
                    icon = VoiceCloudIcons.Profile,
                    onClick = onEditProfile,
                )
            }
        }
        if (onOpenPeople != null) {
            item {
                VCSettingsRow(
                    title = "Discover people",
                    subtitle = "Suggested and trending members",
                    icon = VoiceCloudIcons.Explore,
                    onClick = onOpenPeople,
                )
            }
        }
        onOpenSavedRooms?.let { open ->
            item {
                VCSettingsRow(title = "Saved rooms", subtitle = "Rooms you bookmarked", icon = VoiceCloudIcons.Explore, onClick = open)
            }
        }
        onOpenRankings?.let { open ->
            item {
                VCSettingsRow(title = "Rankings", subtitle = "Leaderboard and trending members", icon = VoiceCloudIcons.Explore, onClick = open)
            }
        }
        onOpenReferrals?.let { open ->
            item {
                VCSettingsRow(title = "Referrals", subtitle = "Invite friends to VoiceCloud", icon = VoiceCloudIcons.Profile, onClick = open)
            }
        }
        onOpenTasks?.let { open ->
            item {
                VCSettingsRow(title = "Tasks & achievements", subtitle = "Progress and milestones", icon = VoiceCloudIcons.Profile, onClick = open)
            }
        }
        onOpenBlockedUsers?.let { open ->
            item {
                VCSettingsRow(title = "Blocked users", subtitle = "Manage blocked members", icon = VoiceCloudIcons.Settings, onClick = open)
            }
        }
        onOpenFriends?.let { open ->
            item { VCSettingsRow(title = "Friends", subtitle = "Requests and connections", icon = VoiceCloudIcons.Profile, onClick = open) }
        }
        onOpenProfileVisitors?.let { open ->
            item { VCSettingsRow(title = "Profile visitors", subtitle = "See who viewed your profile", icon = VoiceCloudIcons.Profile, onClick = open) }
        }
        onOpenActivityHistory?.let { open ->
            item { VCSettingsRow(title = "Activity history", subtitle = "Rooms you joined", icon = VoiceCloudIcons.Explore, onClick = open) }
        }
        onOpenCommunities?.let { open ->
            item { VCSettingsRow(title = "Communities", subtitle = "VoiceCloud clubs", icon = VoiceCloudIcons.Explore, onClick = open) }
        }
        onOpenEvents?.let { open ->
            item { VCSettingsRow(title = "Events", subtitle = "Scheduled rooms and tickets", icon = VoiceCloudIcons.Live, onClick = open) }
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
        if (onOpenSettings != null) {
            item {
                VCSettingsRow(
                    title = "Settings",
                    subtitle = "Account, experience, and safety",
                    icon = VoiceCloudIcons.Settings,
                    onClick = onOpenSettings,
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
                    title = "Build your profile",
                    message = "Edit your profile to add a bio and help others find you on VoiceCloud.",
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
