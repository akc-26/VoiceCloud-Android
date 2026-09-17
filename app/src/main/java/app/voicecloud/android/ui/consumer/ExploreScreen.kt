package app.voicecloud.android.ui.consumer

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
import app.voicecloud.core.designsystem.component.VCCommunityCard
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCEventCard
import app.voicecloud.core.designsystem.component.VCIconButton
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCRoomCard
import app.voicecloud.core.designsystem.component.VCRoomHero
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

private val ExploreTopics = listOf("All", "Music", "Talk", "Wellness", "Comedy", "News")

@Composable
fun ExploreScreen(
    state: ConsumerDiscoveryUiState,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    onRoomClick: (VCRoomUiModel) -> Unit = {},
    onPersonClick: (app.voicecloud.core.designsystem.component.VCPersonUiModel) -> Unit = {},
    onCommunityClick: (app.voicecloud.core.designsystem.component.VCCommunityUiModel) -> Unit = {},
    onEventClick: (app.voicecloud.core.designsystem.component.VCEventUiModel) -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    var selectedTopic by rememberSaveable { mutableStateOf(ExploreTopics.first()) }
    Column(modifier.fillMaxSize()) {
        VCPageHeader(
            title = "Explore",
            subtitle = "Editorial discovery",
            actions = {
                VCIconButton(
                    icon = VoiceCloudIcons.Search,
                    contentDescription = "Search VoiceCloud",
                    onClick = onOpenSearch,
                )
            },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = spacing.pageGutter),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            items(ExploreTopics) { topic ->
                VCChip(
                    label = topic,
                    selected = topic == selectedTopic,
                    onClick = { selectedTopic = topic },
                )
            }
        }
        when {
            state.errorMessage != null -> VCErrorState(
                title = "Couldn't load Explore",
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
                ExploreFeed(
                    state = state,
                    selectedTopic = selectedTopic,
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
private fun ExploreFeed(
    state: ConsumerDiscoveryUiState,
    selectedTopic: String,
    onRoomClick: (VCRoomUiModel) -> Unit,
    onPersonClick: (app.voicecloud.core.designsystem.component.VCPersonUiModel) -> Unit,
    onCommunityClick: (app.voicecloud.core.designsystem.component.VCCommunityUiModel) -> Unit,
    onEventClick: (app.voicecloud.core.designsystem.component.VCEventUiModel) -> Unit,
) {
    val spacing = VoiceCloud.spacing
    val rooms = (state.liveRooms + state.trendingRooms + state.recommendedRooms)
        .distinctBy { it.title }
        .filter { selectedTopic == "All" || it.metaLabel.equals(selectedTopic, ignoreCase = true) || it.subtitle.equals(selectedTopic, ignoreCase = true) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        item { VCSectionHeader(title = "Featured live") }
        if (state.liveRooms.isEmpty()) {
            item { DiscoveryUnavailable("Featured live rooms will appear here from VoiceCloud when discovery is connected.") }
        } else {
            item {
                VCRoomHero(
                    room = state.liveRooms.first(),
                    onClick = { onRoomClick(state.liveRooms.first()) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        item { VCSectionHeader(title = "Trending rooms") }
        if (rooms.isEmpty()) {
            item { DiscoveryUnavailable("Trending rooms will appear here from VoiceCloud when this experience is connected.") }
        } else {
            items(rooms, key = { "trend-${it.title}" }) { room ->
                VCRoomCard(
                    room = room,
                    onClick = { onRoomClick(room) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        item { VCSectionHeader(title = "Rising creators") }
        if (state.people.isEmpty()) {
            item { DiscoveryUnavailable("Rising creators will appear here from VoiceCloud when social discovery is connected.") }
        } else {
            items(state.people, key = { it.name }) { person ->
                VCPersonRow(person = person, onClick = { onPersonClick(person) })
            }
        }
        item { VCSectionHeader(title = "Active communities") }
        if (state.communities.isEmpty()) {
            item { DiscoveryUnavailable("Active communities will appear here from VoiceCloud when this experience is connected.") }
        } else {
            items(state.communities, key = { it.name }) { community ->
                VCCommunityCard(
                    community = community,
                    onClick = { onCommunityClick(community) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        item { VCSectionHeader(title = "Upcoming events") }
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
        if (rooms.isEmpty() && state.people.isEmpty() && state.communities.isEmpty() && state.events.isEmpty()) {
            item {
                VCEmptyState(
                    title = "Explore is ready",
                    message = "Category chips are presentation-only until VoiceCloud discovery data is connected. No sample rooms or audience counts are shown.",
                )
            }
        }
    }
}
