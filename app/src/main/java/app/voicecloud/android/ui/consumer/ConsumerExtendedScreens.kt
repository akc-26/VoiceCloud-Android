package app.voicecloud.android.ui.consumer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.android.viewmodel.ActivityHistoryUiState
import app.voicecloud.android.viewmodel.CommunitiesUiState
import app.voicecloud.android.viewmodel.CommunityDetailUiState
import app.voicecloud.android.viewmodel.CreateCommunityUiState
import app.voicecloud.android.viewmodel.EventDetailUiState
import app.voicecloud.android.viewmodel.EventsUiState
import app.voicecloud.android.viewmodel.FriendsUiState
import app.voicecloud.android.viewmodel.NotificationPreferencesUiState
import app.voicecloud.android.viewmodel.ProfileVisitorsUiState
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCCommunityCard
import app.voicecloud.core.designsystem.component.VCEventCard
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSettingsRow
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCTextButton
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCCommunityUiModel

@Composable
fun FriendsScreen(
    state: FriendsUiState,
    onBack: () -> Unit,
    onPersonClick: (VCPersonUiModel) -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onAddFriend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Friends", subtitle = "Your VoiceCloud connections", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { repeat(3) { VCSkeleton() } }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load friends", message = state.errorMessage)
            state.friends.isEmpty() && state.pending.isEmpty() && state.suggested.isEmpty() ->
                VCEmptyState(title = "No friends yet", message = "Send requests or accept invitations to grow your network.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                if (state.pending.isNotEmpty()) {
                    item { VCSectionHeader(title = "Pending requests") }
                    items(state.pending, key = { it.id }) { request ->
                        VCPersonRow(
                            person = request.person,
                            onClick = { onPersonClick(request.person) },
                            trailing = {
                                VCTextButton(text = "Accept", onClick = { onAccept(request.id) }, enabled = !state.actionInFlight)
                                VCTextButton(text = "Decline", onClick = { onReject(request.id) }, enabled = !state.actionInFlight)
                            },
                        )
                    }
                }
                if (state.friends.isNotEmpty()) {
                    item { VCSectionHeader(title = "Friends") }
                    items(state.friends, key = { personKey(it) }) { person ->
                        VCPersonRow(person = person, onClick = { onPersonClick(person) })
                    }
                }
                if (state.suggested.isNotEmpty()) {
                    item { VCSectionHeader(title = "Suggested") }
                    items(state.suggested, key = { personKey(it) }) { person ->
                        VCPersonRow(
                            person = person,
                            onClick = { onPersonClick(person) },
                            trailing = {
                                person.userId?.let { id ->
                                    VCTextButton(text = "Add", onClick = { onAddFriend(id) }, enabled = !state.actionInFlight)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileVisitorsScreen(
    state: ProfileVisitorsUiState,
    onBack: () -> Unit,
    onPersonClick: (VCPersonUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(
            title = "Profile visitors",
            subtitle = if (state.totalVisitors > 0) "${state.totalVisitors} total visits" else "See who viewed your profile",
            onBack = onBack,
            applyStatusBarPadding = true,
        )
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load visitors", message = state.errorMessage)
            state.visitors.isEmpty() -> VCEmptyState(title = "No visitors yet", message = "When members view your profile, they appear here.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                items(state.visitors, key = { personKey(it) }) { person ->
                    VCPersonRow(person = person, onClick = { onPersonClick(person) })
                }
            }
        }
    }
}

@Composable
fun ActivityHistoryScreen(
    state: ActivityHistoryUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Activity history", subtitle = "Rooms you joined on VoiceCloud", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load activity", message = state.errorMessage)
            state.items.isEmpty() -> VCEmptyState(title = "No activity yet", message = "Your room activity will appear after you join live rooms.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                items(state.items, key = { it.id }) { item ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.sm),
                    ) {
                        Text(item.title, style = VoiceCloud.typography.actionLabel, color = VoiceCloud.colors.textPrimary)
                        item.whenLabel?.let {
                            Text(it, style = VoiceCloud.typography.caption, color = VoiceCloud.colors.textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPreferencesScreen(
    state: NotificationPreferencesUiState,
    onBack: () -> Unit,
    onTogglePush: (Boolean) -> Unit,
    onToggleEmail: (Boolean) -> Unit,
    onToggleSocial: (Boolean) -> Unit,
    onToggleMessages: (Boolean) -> Unit,
    onToggleRooms: (Boolean) -> Unit,
    onToggleMarketing: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Notification preferences", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load preferences", message = state.errorMessage)
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                item { preferenceRow("Push notifications", state.pushEnabled, onTogglePush, state.isSaving) }
                item { preferenceRow("Email notifications", state.emailEnabled, onToggleEmail, state.isSaving) }
                item { preferenceRow("Social alerts", state.socialNotifications, onToggleSocial, state.isSaving) }
                item { preferenceRow("Messages", state.messageNotifications, onToggleMessages, state.isSaving) }
                item { preferenceRow("Live rooms", state.roomNotifications, onToggleRooms, state.isSaving) }
                item { preferenceRow("Product updates", state.marketingNotifications, onToggleMarketing, state.isSaving) }
            }
        }
    }
}

@Composable
private fun preferenceRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit, enabled: Boolean) {
    VCSettingsRow(
        title = label,
        trailing = {
            Switch(checked = checked, onCheckedChange = onToggle, enabled = !enabled)
        },
    )
}

@Composable
fun CommunitiesScreen(
    state: CommunitiesUiState,
    onBack: () -> Unit,
    onCommunityClick: (VCCommunityUiModel) -> Unit,
    onCreateCommunity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Communities", subtitle = "VoiceCloud clubs", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load communities", message = state.errorMessage)
            state.communities.isEmpty() -> {
                VCEmptyState(title = "No communities yet", message = "Join or create a VoiceCloud community to get started.")
                VCPrimaryButton(
                    text = "Create community",
                    onClick = onCreateCommunity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VoiceCloud.spacing.pageGutter),
                )
            }
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                item {
                    VCSecondaryButton(
                        text = "Create community",
                        onClick = onCreateCommunity,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.sm),
                    )
                }
                items(state.communities, key = { it.id ?: it.name }) { community ->
                    VCCommunityCard(
                        community = community,
                        onClick = { onCommunityClick(community) },
                        modifier = Modifier.padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.xxs),
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityDetailScreen(
    state: CommunityDetailUiState,
    onBack: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onPersonClick: (VCPersonUiModel) -> Unit,
    onEventClick: (VCEventUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = state.community?.name ?: "Community", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load community", message = state.errorMessage)
            state.community == null -> VCEmptyState(title = "Community unavailable", message = "This community could not be loaded.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                item {
                    state.community.description?.let {
                        Text(
                            it,
                            style = VoiceCloud.typography.bodySecondary,
                            color = VoiceCloud.colors.textSecondary,
                            modifier = Modifier.padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.sm),
                        )
                    }
                }
                item {
                    RowActions(
                        joinLabel = if (state.membershipActionInFlight) "Please wait…" else "Join community",
                        leaveLabel = "Leave community",
                        onJoin = onJoin,
                        onLeave = onLeave,
                        inFlight = state.membershipActionInFlight,
                    )
                }
                if (state.members.isNotEmpty()) {
                    item { VCSectionHeader(title = "Members") }
                    items(state.members.take(20), key = { personKey(it) }) { person ->
                        VCPersonRow(person = person, onClick = { onPersonClick(person) })
                    }
                }
                if (state.rooms.isNotEmpty()) {
                    item { VCSectionHeader(title = "Scheduled rooms") }
                    items(state.rooms, key = { it.id ?: it.title }) { event ->
                        VCEventCard(
                            event = event,
                            onClick = { onEventClick(event) },
                            modifier = Modifier.padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.xxs),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowActions(
    joinLabel: String,
    leaveLabel: String,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    inFlight: Boolean,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = VoiceCloud.spacing.pageGutter),
        verticalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.sm),
    ) {
        VCPrimaryButton(text = joinLabel, onClick = onJoin, enabled = !inFlight, modifier = Modifier.fillMaxWidth())
        VCSecondaryButton(text = leaveLabel, onClick = onLeave, enabled = !inFlight, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun CreateCommunityScreen(
    state: CreateCommunityUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.lg),
    ) {
        VCPageHeader(title = "Create community", onBack = onBack, applyStatusBarPadding = true)
        Column(verticalArrangement = Arrangement.spacedBy(VoiceCloud.spacing.md)) {
            OutlinedTextField(value = state.name, onValueChange = onNameChange, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.description, onValueChange = onDescriptionChange, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            state.errorMessage?.let { VCErrorState(title = "Could not create community", message = it) }
            VCPrimaryButton(
                text = if (state.isSubmitting) "Creating…" else "Create community",
                onClick = onSubmit,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun EventsScreen(
    state: EventsUiState,
    onBack: () -> Unit,
    onEventClick: (VCEventUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Events", subtitle = "Scheduled VoiceCloud rooms", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load events", message = state.errorMessage)
            state.events.isEmpty() -> VCEmptyState(title = "No upcoming events", message = "Scheduled events appear here when hosts publish them.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                items(state.events, key = { it.id ?: it.title }) { event ->
                    VCEventCard(
                        event = event,
                        onClick = { onEventClick(event) },
                        modifier = Modifier.padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.xxs),
                    )
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(
    state: EventDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = state.event?.title ?: "Event", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load event", message = state.errorMessage)
            state.event == null -> VCEmptyState(title = "Event unavailable", message = "This event could not be loaded.")
            else -> Column(Modifier.padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.md)) {
                Text(state.event.timeLabel, style = VoiceCloud.typography.metadata, color = VoiceCloud.colors.textMuted)
                state.event.placeLabel?.let {
                    Text(it, style = VoiceCloud.typography.bodySecondary, color = VoiceCloud.colors.textSecondary)
                }
                state.description?.let {
                    Text(it, style = VoiceCloud.typography.body, color = VoiceCloud.colors.textPrimary, modifier = Modifier.padding(top = VoiceCloud.spacing.md))
                }
            }
        }
    }
}

private fun personKey(person: VCPersonUiModel): String = listOfNotNull(person.userId, person.name, person.subtitle).joinToString(":")
