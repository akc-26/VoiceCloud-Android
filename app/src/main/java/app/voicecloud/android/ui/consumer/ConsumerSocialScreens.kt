package app.voicecloud.android.ui.consumer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.voicecloud.android.viewmodel.PeopleUiState
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton

@Composable
fun PeopleScreen(
    state: PeopleUiState,
    onBack: () -> Unit,
    onPersonClick: (VCPersonUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "People", subtitle = "Discover VoiceCloud members", onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { repeat(3) { VCSkeleton() } }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load people", message = state.errorMessage)
            state.suggested.isEmpty() && state.online.isEmpty() && state.trending.isEmpty() ->
                VCEmptyState(title = "No people to show", message = "VoiceCloud has no suggested members right now.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                if (state.suggested.isNotEmpty()) {
                    item { VCSectionHeader(title = "Suggested for you") }
                    items(state.suggested, key = { it.stableKey() }) { person ->
                        VCPersonRow(person = person, onClick = { onPersonClick(person) })
                    }
                }
                if (state.online.isNotEmpty()) {
                    item { VCSectionHeader(title = "Online now") }
                    items(state.online, key = { it.stableKey() }) { person ->
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

private fun VCPersonUiModel.stableKey(): String = listOfNotNull(userId, name, subtitle).joinToString(":")

@Composable
fun NotificationsScreen(
    state: app.voicecloud.android.viewmodel.NotificationsUiState,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onOpenNotification: (String, app.voicecloud.android.navigation.NotificationDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        VCPageHeader(
            title = "Notifications",
            subtitle = if (state.unreadCount > 0) "${state.unreadCount} unread" else "Stay up to date",
            onBack = onBack,
            applyStatusBarPadding = true,
        )
        when {
            state.isLoading -> Column(Modifier.padding(VoiceCloud.spacing.pageGutter)) { VCSkeleton(); VCSkeleton() }
            state.errorMessage != null -> VCErrorState(title = "Couldn't load notifications", message = state.errorMessage)
            state.items.isEmpty() -> VCEmptyState(title = "No notifications", message = "You're all caught up.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = VoiceCloud.spacing.xxl)) {
                if (state.unreadCount > 0) {
                    item {
                        Text(
                            "Mark all as read",
                            style = VoiceCloud.typography.actionLabel,
                            color = VoiceCloud.colors.accent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkAllRead() }
                                .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.sm),
                        )
                    }
                }
                items(state.items, key = { it.id }) { item ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                                .clickable {
                                    onOpenNotification(item.id, item.destination)
                                }
                            .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.sm),
                    ) {
                        Text(item.title, style = VoiceCloud.typography.actionLabel, color = VoiceCloud.colors.textPrimary)
                        item.body?.let {
                            Text(it, style = VoiceCloud.typography.bodySecondary, color = VoiceCloud.colors.textSecondary)
                        }
                        item.meta?.let {
                            Text(it, style = VoiceCloud.typography.caption, color = VoiceCloud.colors.textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileScreen(
    state: app.voicecloud.android.viewmodel.EditProfileUiState,
    onBack: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.lg),
    ) {
        VCPageHeader(title = "Edit profile", subtitle = state.handle, onBack = onBack, applyStatusBarPadding = true)
        when {
            state.isLoading -> VCSkeleton()
            state.errorMessage != null -> VCErrorState(title = "Couldn't load profile", message = state.errorMessage)
            else -> Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(VoiceCloud.spacing.md)) {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.bio,
                    onValueChange = onBioChange,
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                )
                VCPrimaryButton(
                    text = if (state.isSaving) "Saving…" else "Save profile",
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
