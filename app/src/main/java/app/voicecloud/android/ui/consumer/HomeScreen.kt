package app.voicecloud.android.ui.consumer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCCommunityCard
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCEventCard
import app.voicecloud.core.designsystem.component.VCIconButton
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomCard
import app.voicecloud.core.designsystem.component.VCRoomCompactCard
import app.voicecloud.core.designsystem.component.VCRoomHero
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.designsystem.component.VCSearchField
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun HomeScreen(
    state: ConsumerDiscoveryUiState,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    onRoomClick: (VCRoomUiModel) -> Unit = {},
    onPersonClick: (VCPersonUiModel) -> Unit = {},
    onCommunityClick: (VCCommunityUiModel) -> Unit = {},
    onEventClick: (VCEventUiModel) -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    val colors = VoiceCloud.colors
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = spacing.pageGutter, end = spacing.xs, top = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VoiceCloudBrandMark(size = spacing.xxxl, contentDescription = "VoiceCloud")
            VCPageHeader(
                title = "Home",
                subtitle = "Live audio, in one place",
                modifier = Modifier.weight(1f),
                actions = {
                    VCIconButton(
                        icon = VoiceCloudIcons.Search,
                        contentDescription = "Search VoiceCloud",
                        onClick = onOpenSearch,
                    )
                },
            )
        }
        VCSearchField(
            query = "",
            onQueryChange = { onOpenSearch() },
            onSearch = onOpenSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.pageGutter, vertical = spacing.xs),
            placeholder = "Search rooms and people",
        )
        Text(
            text = "Listen in whenever a room is live.",
            style = VoiceCloud.typography.bodySecondary,
            color = colors.textSecondary,
            modifier = Modifier
                .padding(horizontal = spacing.pageGutter, vertical = spacing.xs)
                .semantics { heading() },
        )
        when {
            state.errorMessage != null -> VCErrorState(
                title = "Couldn't load Home",
                message = state.errorMessage,
            )
            state.isLoading -> HomeLoading()
            else -> AnimatedVisibility(
                visible = true,
                enter = if (motionEnabled) fadeIn(tween(VoiceCloudMotion.EntranceMs)) else fadeIn(tween(0)),
            ) {
                HomeFeed(
                    state = state,
                    onRoomClick = onRoomClick,
                    onPersonClick = onPersonClick,
                    onCommunityClick = onCommunityClick,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}

@Composable
private fun HomeLoading() {
    val spacing = VoiceCloud.spacing
    Column(
        Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        VCSkeleton()
        VCSkeleton()
        VCSkeleton()
    }
}

@Composable
private fun HomeFeed(
    state: ConsumerDiscoveryUiState,
    onRoomClick: (VCRoomUiModel) -> Unit,
    onPersonClick: (VCPersonUiModel) -> Unit,
    onCommunityClick: (VCCommunityUiModel) -> Unit,
    onEventClick: (VCEventUiModel) -> Unit,
) {
    val spacing = VoiceCloud.spacing
    val empty = state.liveRooms.isEmpty() &&
        state.recommendedRooms.isEmpty() &&
        state.people.isEmpty() &&
        state.communities.isEmpty() &&
        state.events.isEmpty()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        item {
            VCSectionHeader(title = "Live now")
        }
        if (state.liveRooms.isEmpty()) {
            item { DiscoveryUnavailable("Live rooms will appear here from VoiceCloud when discovery is connected.") }
        } else {
            item {
                VCRoomHero(
                    room = state.liveRooms.first(),
                    onClick = { onRoomClick(state.liveRooms.first()) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
            items(state.liveRooms.drop(1), key = { it.title }) { room ->
                VCRoomCompactCard(
                    room = room,
                    onClick = { onRoomClick(room) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        item { VCSectionHeader(title = "Recommended rooms") }
        if (state.recommendedRooms.isEmpty()) {
            item { DiscoveryUnavailable("Recommended rooms will use your VoiceCloud listening when this experience is connected.") }
        } else {
            items(state.recommendedRooms, key = { "rec-${it.title}" }) { room ->
                VCRoomCard(
                    room = room,
                    onClick = { onRoomClick(room) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        item { VCSectionHeader(title = "People and creators") }
        if (state.people.isEmpty()) {
            item { DiscoveryUnavailable("People and creators will appear here from VoiceCloud when social discovery is connected.") }
        } else {
            items(state.people, key = { it.name }) { person ->
                VCPersonRow(person = person, onClick = { onPersonClick(person) })
            }
        }
        item { VCSectionHeader(title = "Communities") }
        if (state.communities.isEmpty()) {
            item { DiscoveryUnavailable("Communities will appear here from VoiceCloud when this experience is connected.") }
        } else {
            items(state.communities, key = { it.name }) { community ->
                VCCommunityCard(
                    community = community,
                    onClick = { onCommunityClick(community) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        item { VCSectionHeader(title = "Events") }
        if (state.events.isEmpty()) {
            item { DiscoveryUnavailable("Upcoming events will appear here from VoiceCloud when this experience is connected.") }
        } else {
            items(state.events, key = { it.title }) { event ->
                VCEventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        item { VCSectionHeader(title = "Replays") }
        item { DiscoveryUnavailable("Replays will appear here from VoiceCloud when this experience is connected.") }
        if (empty) {
            item {
                VCEmptyState(
                    title = "Nothing live yet",
                    message = "Home is ready. Rooms, people, and communities will fill these sections from VoiceCloud when discovery is connected.",
                )
            }
        }
    }
}

@Composable
internal fun DiscoveryUnavailable(message: String) {
    Text(
        text = message,
        style = VoiceCloud.typography.bodySecondary,
        color = VoiceCloud.colors.textMuted,
        modifier = Modifier.padding(horizontal = VoiceCloud.spacing.pageGutter, vertical = VoiceCloud.spacing.xxs),
    )
}
