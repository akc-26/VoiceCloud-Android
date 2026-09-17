package app.voicecloud.android.ui.consumer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCRoomCard
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.designsystem.component.VCSearchField
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton

private val SearchScopes = listOf("Rooms", "People", "Communities")

@Composable
fun SearchScreen(
    state: ConsumerSearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onRoomClick: (VCRoomUiModel) -> Unit = {},
    onPersonClick: (VCPersonUiModel) -> Unit = {},
    onCommunityClick: (VCCommunityUiModel) -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    var scope by rememberSaveable { mutableStateOf(SearchScopes.first()) }
    Column(modifier.fillMaxSize()) {
        VCPageHeader(
            title = "Search",
            subtitle = "Rooms, people, communities",
            onBack = onBack,
        )
        VCSearchField(
            query = state.query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.pageGutter, vertical = spacing.xs),
            placeholder = "Search VoiceCloud",
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = spacing.pageGutter, vertical = spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            items(SearchScopes) { label ->
                VCChip(
                    label = label,
                    selected = label == scope,
                    onClick = { scope = label },
                )
            }
        }
        when {
            state.errorMessage != null -> VCErrorState(
                title = "Search unavailable",
                message = state.errorMessage,
            )
            state.isLoading -> SearchLoading()
            state.isIdle -> VCEmptyState(
                title = "Search VoiceCloud",
                message = "Find rooms, people, and communities. Results will use VoiceCloud search when that experience is connected.",
            )
            state.hasResults -> SearchResults(
                state = state,
                scope = scope,
                onRoomClick = onRoomClick,
                onPersonClick = onPersonClick,
                onCommunityClick = onCommunityClick,
            )
            state.isSearching && state.isLoading -> SearchLoading()
            state.isSearching -> VCEmptyState(
                title = "No results",
                message = "No $scope matches for \"${state.query}\". Search is presentation-only until VoiceCloud search is connected.",
            )
            state.query.isNotBlank() -> VCEmptyState(
                title = "Ready to search",
                message = "Press search to look for $scope matching \"${state.query}\".",
            )
            else -> VCEmptyState(
                title = "Search VoiceCloud",
                message = "Find rooms, people, and communities. Results will use VoiceCloud search when that experience is connected.",
            )
        }
    }
}

@Composable
private fun SearchLoading() {
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
private fun SearchResults(
    state: ConsumerSearchUiState,
    scope: String,
    onRoomClick: (VCRoomUiModel) -> Unit,
    onPersonClick: (VCPersonUiModel) -> Unit,
    onCommunityClick: (VCCommunityUiModel) -> Unit,
) {
    val spacing = VoiceCloud.spacing
    val scopedEmpty = when (scope) {
        "Rooms" -> state.rooms.isEmpty()
        "People" -> state.people.isEmpty()
        else -> state.communities.isEmpty()
    }
    if (scopedEmpty) {
        VCEmptyState(
            title = "No results",
            message = "No $scope matches for \"${state.query}\".",
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(bottom = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        if (scope == "Rooms") {
            item { VCSectionHeader(title = "Rooms") }
            items(state.rooms, key = { it.title }) { room ->
                VCRoomCard(
                    room = room,
                    onClick = { onRoomClick(room) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
        if (scope == "People") {
            item { VCSectionHeader(title = "People") }
            items(state.people, key = { it.name }) { person ->
                VCPersonRow(person = person, onClick = { onPersonClick(person) })
            }
        }
        if (scope == "Communities") {
            item { VCSectionHeader(title = "Communities") }
            items(state.communities, key = { it.name }) { community ->
                VCCommunityCard(
                    community = community,
                    onClick = { onCommunityClick(community) },
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        }
    }
}
