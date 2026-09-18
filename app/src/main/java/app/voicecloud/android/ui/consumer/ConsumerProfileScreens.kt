package app.voicecloud.android.ui.consumer

import androidx.compose.foundation.clickable
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
import app.voicecloud.android.viewmodel.BlockedUsersUiState
import app.voicecloud.android.viewmodel.PublicProfileUiState
import app.voicecloud.android.viewmodel.RankingsUiState
import app.voicecloud.android.viewmodel.ReferralsUiState
import app.voicecloud.android.viewmodel.SavedRoomsUiState
import app.voicecloud.android.viewmodel.SocialListUiState
import app.voicecloud.android.viewmodel.TasksHubUiState
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCProfileHeader
import app.voicecloud.core.designsystem.component.VCRoomCard
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCStatCard
import app.voicecloud.core.designsystem.component.VCTextButton

@Composable
fun PublicProfileScreen(
    state: PublicProfileUiState,
    onBack: () -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onBlock: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Profile", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load profile", message = state.errorMessage)
            state.profile == null -> VCEmptyState(title = "Profile unavailable", message = "This VoiceCloud member could not be found.")
            else -> {
                val profile = state.profile
                val user = profile.user
                LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                    item {
                        VCProfileHeader(
                            name = user.displayName ?: user.username ?: "VoiceCloud member",
                            handle = user.username?.let { "@$it" },
                            bio = user.bio,
                        )
                    }
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.sm),
                        ) {
                            VCStatCard(
                                label = "Followers",
                                value = profile.followerCount?.toString() ?: "—",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onFollowersClick(user.id) },
                            )
                            VCStatCard(
                                label = "Following",
                                value = profile.followingCount?.toString() ?: "—",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onFollowingClick(user.id) },
                            )
                        }
                    }
                    if (!state.isSelf) {
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = VoiceCloud.spacing.pageGutter),
                                verticalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.sm),
                            ) {
                                if (profile.isFollowing) {
                                    VCSecondaryButton(
                                        text = if (state.socialActionInFlight) "Updating…" else "Following",
                                        onClick = { onToggleFollow(user.id) },
                                        enabled = !state.socialActionInFlight,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                } else {
                                    VCPrimaryButton(
                                        text = if (state.socialActionInFlight) "Updating…" else "Follow",
                                        onClick = { onToggleFollow(user.id) },
                                        enabled = !state.socialActionInFlight,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                VCTextButton(
                                    text = "Block member",
                                    onClick = { onBlock(user.id) },
                                    enabled = !state.socialActionInFlight,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialListScreen(
    title: String,
    state: SocialListUiState,
    onBack: () -> Unit,
    onPersonClick: (VCPersonUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = title, onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load list", message = state.errorMessage)
            state.people.isEmpty() -> VCEmptyState(title = "No members yet", message = "This list is empty on VoiceCloud.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                items(state.people, key = { it.stableKey() }) { person ->
                    VCPersonRow(person = person, onClick = { onPersonClick(person) })
                }
            }
        }
    }
}

@Composable
fun SavedRoomsScreen(
    state: SavedRoomsUiState,
    onBack: () -> Unit,
    onRoomClick: (VCRoomUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Saved rooms", subtitle = "Rooms you saved on VoiceCloud", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load saved rooms", message = state.errorMessage)
            state.rooms.isEmpty() -> VCEmptyState(title = "No saved rooms", message = "Save rooms from discovery to find them here.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                items(state.rooms, key = { it.id }) { room ->
                    VCRoomCard(
                        room = room,
                        onClick = { onRoomClick(room) },
                        modifier = Modifier.padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.xxs),
                    )
                }
            }
        }
    }
}

@Composable
fun RankingsScreen(
    state: RankingsUiState,
    onBack: () -> Unit,
    onPersonClick: (VCPersonUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Rankings", subtitle = "Top VoiceCloud members", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load rankings", message = state.errorMessage)
            state.leaderboard.isEmpty() && state.trending.isEmpty() ->
                VCEmptyState(title = "No rankings yet", message = "Check back when VoiceCloud publishes leaderboard data.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                if (state.leaderboard.isNotEmpty()) {
                    item { VCSectionHeader(title = "Leaderboard") }
                    items(state.leaderboard, key = { it.stableKey() }) { person ->
                        VCPersonRow(person = person, onClick = { onPersonClick(person) })
                    }
                }
                if (state.trending.isNotEmpty()) {
                    item { VCSectionHeader(title = "Trending") }
                    items(state.trending, key = { it.stableKey() }) { person ->
                        VCPersonRow(person = person, onClick = { onPersonClick(person) })
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedUsersScreen(
    state: BlockedUsersUiState,
    onBack: () -> Unit,
    onUnblock: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Blocked users", subtitle = "Members you blocked on VoiceCloud", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load blocked list", message = state.errorMessage)
            state.people.isEmpty() -> VCEmptyState(title = "No blocked users", message = "You have not blocked anyone.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                items(state.people, key = { it.stableKey() }) { person ->
                    val userId = person.userId
                    VCPersonRow(
                        person = person,
                        trailing = {
                            if (userId != null) {
                                VCTextButton(
                                    text = if (state.actionInFlight) "…" else "Unblock",
                                    onClick = { onUnblock(userId) },
                                    enabled = !state.actionInFlight,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ReferralsScreen(
    state: ReferralsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = VoiceCloud.spacing.pageGutter),
    ) {
        VCPageHeader(title = "Referrals", subtitle = "Invite friends to VoiceCloud", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> VCSkeleton()
            state.errorMessage != null -> VCErrorState(title = "Couldn't load referrals", message = state.errorMessage)
            else -> Column(verticalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.md)) {
                state.code?.let {
                    Text("Your code", style = VoiceCloud.typography.caption, color = VoiceCloud.colors.textMuted)
                    Text(it, style = VoiceCloud.typography.screenTitle, color = VoiceCloud.colors.textPrimary)
                }
                state.inviteCount?.let {
                    Text("$it invites sent", style = VoiceCloud.typography.body, color = VoiceCloud.colors.textSecondary)
                }
                state.rewardSummary?.let {
                    Text(it, style = VoiceCloud.typography.bodySecondary, color = VoiceCloud.colors.textSecondary)
                }
                if (state.code == null && state.inviteCount == null && state.rewardSummary == null) {
                    VCEmptyState(title = "No referral activity", message = "Your referral summary will appear here when available.")
                }
            }
        }
    }
}

@Composable
fun TasksHubScreen(
    state: TasksHubUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Tasks & achievements", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load tasks", message = state.errorMessage)
            state.tasks.isEmpty() && state.achievements.isEmpty() ->
                VCEmptyState(title = "Nothing here yet", message = "Tasks and achievements will show when VoiceCloud assigns them to you.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                if (state.tasks.isNotEmpty()) {
                    item { VCSectionHeader(title = "Tasks") }
                    items(state.tasks, key = { it.resolvedId ?: it.title.orEmpty() }) { task ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.sm),
                        ) {
                            Text(task.title ?: "Task", style = VoiceCloud.typography.actionLabel, color = VoiceCloud.colors.textPrimary)
                            task.description?.let {
                                Text(it, style = VoiceCloud.typography.bodySecondary, color = VoiceCloud.colors.textSecondary)
                            }
                            val progress = task.progress
                            val target = task.target
                            if (progress != null && target != null) {
                                Text("$progress / $target", style = VoiceCloud.typography.caption, color = VoiceCloud.colors.textMuted)
                            }
                        }
                    }
                }
                if (state.achievements.isNotEmpty()) {
                    item { VCSectionHeader(title = "Achievements") }
                    items(state.achievements, key = { it.resolvedId ?: it.title.orEmpty() }) { item ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.sm),
                        ) {
                            Text(item.title ?: "Achievement", style = VoiceCloud.typography.actionLabel, color = VoiceCloud.colors.textPrimary)
                            item.description?.let {
                                Text(it, style = VoiceCloud.typography.bodySecondary, color = VoiceCloud.colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun VCPersonUiModel.stableKey(): String = listOfNotNull(userId, name, subtitle).joinToString(":")
