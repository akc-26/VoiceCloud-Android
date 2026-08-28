package app.voicecloud.feature.discovery.ui

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.theme.ConsumerBrushes
import app.voicecloud.core.designsystem.theme.CommonColors
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.VoiceCloudPageMetrics
import app.voicecloud.feature.discovery.model.*

@Composable
private fun ConsumerScaffold(
    selected: String,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onProfile: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val metrics = VoiceCloudPageMetrics.current()
    val items = listOf(
        ConsumerNavItem("home", R.drawable.vc_nav_home, R.drawable.vc_nav_home_selected, "Home"),
        ConsumerNavItem("explore", R.drawable.vc_nav_explore, R.drawable.vc_nav_explore_selected, "Explore"),
        ConsumerNavItem("search", R.drawable.vc_nav_search, R.drawable.vc_nav_search_selected, "Search"),
        ConsumerNavItem("friends", R.drawable.vc_nav_friends, R.drawable.vc_nav_friends_selected, "Friends"),
        ConsumerNavItem("profile", R.drawable.vc_nav_profile, R.drawable.vc_nav_profile_selected, "Profile"),
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(metrics.bottomBarHeight),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                items.forEach { item ->
                    val action = when (item.key) {
                        "home" -> onHome
                        "explore" -> onExplore
                        "search" -> onSearch
                        "friends" -> onFriends
                        else -> onProfile
                    }
                    val isSelected = selected == item.key
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = action,
                        icon = {
                            Icon(
                                painter = painterResource(if (isSelected) item.selectedIcon else item.outlineIcon),
                                contentDescription = item.label,
                                modifier = Modifier.size(metrics.bottomIconSize),
                            )
                        },
                        label = { Text(item.label, maxLines = 1, softWrap = false) },
                    )
                }
            }
        },
        content = content,
    )
}

private data class ConsumerNavItem(
    val key: String,
    val outlineIcon: Int,
    val selectedIcon: Int,
    val label: String,
)

@Composable
private fun ScreenHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    val metrics = VoiceCloudPageMetrics.current()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = metrics.horizontalPadding, vertical = metrics.contentTopSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (!subtitle.isNullOrBlank()) Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        action?.invoke()
    }
}

@Composable
private fun SecondaryPageLayout(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { VoiceCloudPageTopBar(title = title, subtitle = subtitle, onBack = onBack) },
    ) { innerPadding ->
        content(Modifier.fillMaxSize().padding(innerPadding))
    }
}

@Composable
private fun adaptivePagePadding(): PaddingValues {
    val metrics = VoiceCloudPageMetrics.current()
    return PaddingValues(
        start = metrics.horizontalPadding,
        top = metrics.contentTopSpacing,
        end = metrics.horizontalPadding,
        bottom = metrics.contentBottomSpacing,
    )
}

@Composable
private fun RowScope.HomeShortcut(label: String, iconRes: Int, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.weight(1f).heightIn(min = 64.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = ConsumerColors.SapphireSoft) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = ConsumerColors.SapphireDeep,
                    modifier = Modifier.padding(7.dp).size(20.dp),
                )
            }
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun StatusBlock(state: DiscoveryUiState, onRetry: (() -> Unit)? = null) {
    when {
        state.loading -> Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.error != null -> Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
                if (onRetry != null) OutlinedButton(onClick = onRetry) { Text("Try again") }
            }
        }
        state.notice != null -> Text(
            state.notice,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            color = ConsumerColors.SapphireDeep,
        )
    }
}

@Composable
private fun SectionTitle(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
private fun UserAvatar(user: VoiceCloudUser, size: Int = 48) {
    val initial = (user.displayName.ifBlank { user.username }.firstOrNull() ?: 'V').uppercaseChar().toString()
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(
            ConsumerBrushes.Primary
        ),
        contentAlignment = Alignment.Center,
    ) { Text(initial, color = Color.White, fontWeight = FontWeight.Bold) }
}

@Composable
private fun UserCard(
    user: VoiceCloudUser,
    onOpen: () -> Unit,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: (() -> Unit)? = null,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(user)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.displayName.ifBlank { user.username }, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (user.isVerified) Text("  ✓", color = ConsumerColors.Sapphire)
                }
                Text("@${user.username}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                val descriptor = when {
                    user.role?.uppercase() == "CREATOR" -> "Creator"
                    user.isOnline -> "Online now"
                    !user.bio.isNullOrBlank() -> user.bio
                    else -> "${VoiceCloudBrand.name} member"
                }
                Text(descriptor ?: "${VoiceCloudBrand.name} member", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            }
            if (actionLabel != null && onAction != null) {
                OutlinedButton(enabled = actionEnabled, onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun RoomCard(room: VoiceCloudRoom, onOpen: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().clickable(onClick = onOpen), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(if (room.isLive) CommonColors.Error else MaterialTheme.colorScheme.outline))
                Spacer(Modifier.width(8.dp))
                Text(if (room.isLive) "LIVE NOW" else room.status.ifBlank { "ROOM" }, color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                if (room.isLocked) Text("Locked", style = MaterialTheme.typography.labelLarge)
            }
            Text(room.title.ifBlank { "${VoiceCloudBrand.name} Room" }, style = MaterialTheme.typography.titleLarge)
            if (!room.description.isNullOrBlank()) Text(room.description, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${room.listenerCount} listening", style = MaterialTheme.typography.bodyMedium)
                Text("${room.speakerCount} speakers", style = MaterialTheme.typography.bodyMedium)
                if (room.category.isNotBlank()) Text(room.category, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EmptyBlock(title: String, body: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HomeScreen(
    state: DiscoveryUiState,
    isGuest: Boolean,
    onLoad: () -> Unit,
    onRooms: () -> Unit,
    onRoom: (String) -> Unit,
    onPeople: () -> Unit,
    onCreators: () -> Unit,
    onProfile: (String) -> Unit,
    onUpgrade: () -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
    onCommunities: () -> Unit,
    onMessages: () -> Unit,
    onNotifications: () -> Unit,
    onHostStudio: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    ConsumerScaffold("home", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(20.dp).clip(RoundedCornerShape(28.dp)).background(
                        ConsumerBrushes.Hero
                    ).padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("LIVE AUDIO", color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text("Join the conversation", color = ConsumerColors.Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                        Text("Live rooms, creators and communities.", color = ConsumerColors.Text)
                        Button(onClick = onRooms) { Text("Explore rooms", maxLines = 1, softWrap = false) }
                    }
                }
            }
            item {
                val metrics = VoiceCloudPageMetrics.current()
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = metrics.horizontalPadding, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HomeShortcut("Communities", R.drawable.vc_icon_community, onCommunities)
                        HomeShortcut("Messages", R.drawable.vc_icon_message, onMessages)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HomeShortcut("Alerts", R.drawable.vc_icon_bell, onNotifications)
                        HomeShortcut("Host Studio", R.drawable.vc_icon_host, onHostStudio)
                    }
                }
            }
            if (isGuest) item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("Keep your profile", fontWeight = FontWeight.Bold); Text("Upgrade your guest account.", style = MaterialTheme.typography.bodyMedium) }
                        TextButton(onClick = onUpgrade) { Text("Upgrade") }
                    }
                }
            }
            item { StatusBlock(state, onLoad) }
            item { Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { SectionTitle("Live now", "View all", onRooms) } }
            if (state.home.rooms.isEmpty() && !state.loading) item { Box(Modifier.padding(horizontal = 20.dp)) { EmptyBlock("No live rooms", "Check again soon.") } }
            itemsIndexed(state.home.rooms.distinctBy { it.id }, key = { index, room -> "home-room:${room.id}:$index" }) { _, room -> Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) { RoomCard(room) { onRoom(room.id) } } }
            item { Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { SectionTitle("People to discover", "See people", onPeople) } }
            itemsIndexed(state.home.people.distinctBy { it.id }.take(5), key = { index, user -> "home-person:${user.id}:$index" }) { _, user -> Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) { UserCard(user, { onProfile(user.username) }) } }
            item { Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { SectionTitle("Creators", "See creators", onCreators) } }
            if (state.home.creators.isEmpty() && !state.loading) item { Box(Modifier.padding(horizontal = 20.dp)) { EmptyBlock("No creators yet", "Discover more soon.") } }
            itemsIndexed(state.home.creators.distinctBy { it.id }.take(5), key = { index, user -> "home-creator:${user.id}:$index" }) { _, user -> Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) { UserCard(user, { onProfile(user.username) }) } }
        }
    }
}

@Composable
fun ExploreScreen(
    state: DiscoveryUiState,
    onLoad: () -> Unit,
    onRooms: () -> Unit,
    onRoom: (String) -> Unit,
    onPeople: () -> Unit,
    onCreators: () -> Unit,
    onProfile: (String) -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    ConsumerScaffold("explore", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader("Explore", "Discover live conversations, creators, and people across ${VoiceCloudBrand.name}.") }
            item { StatusBlock(state, onLoad) }
            item { SectionTitle("Live rooms", "All rooms", onRooms) }
            itemsIndexed(state.explore.liveRooms.distinctBy { it.id }.take(6), key = { index, room -> "explore-live:${room.id}:$index" }) { _, room -> RoomCard(room) { onRoom(room.id) } }
            item { SectionTitle("Trending rooms") }
            itemsIndexed(state.explore.trendingRooms.distinctBy { it.id }.take(5), key = { index, room -> "explore-trend:${room.id}:$index" }) { _, room -> RoomCard(room) { onRoom(room.id) } }
            item { SectionTitle("People", "View all", onPeople) }
            itemsIndexed(state.explore.people.distinctBy { it.id }.take(6), key = { index, user -> "explore-person:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
            item { SectionTitle("Creators", "View all", onCreators) }
            itemsIndexed(state.explore.creators.distinctBy { it.id }.take(6), key = { index, user -> "explore-creator:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
        }
    }
}

@Composable
fun RoomsScreen(state: DiscoveryUiState, onLoad: () -> Unit, onRoom: (String) -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    SecondaryPageLayout(
        title = "Live Rooms",
        subtitle = "Discover conversations happening live across ${VoiceCloudBrand.name}.",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = adaptivePagePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { StatusBlock(state, onLoad) }
            if (state.rooms.isEmpty() && !state.loading) item { EmptyBlock("No live rooms", "There are no discoverable live rooms at the moment.") }
            itemsIndexed(state.rooms.distinctBy { it.id }, key = { index, room -> "rooms:${room.id}:$index" }) { _, room -> RoomCard(room) { onRoom(room.id) } }
        }
    }
}

@Composable
fun PeopleScreen(
    state: DiscoveryUiState,
    creatorsOnly: Boolean,
    onLoad: () -> Unit,
    onProfile: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(creatorsOnly) { onLoad() }
    SecondaryPageLayout(
        title = if (creatorsOnly) "Creators" else "People",
        subtitle = if (creatorsOnly) "Discover creators and voices worth following." else "Discover people to follow and connect with.",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = adaptivePagePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { StatusBlock(state, onLoad) }
            if (state.people.isEmpty() && !state.loading) item { EmptyBlock("No profiles found", "Try again later as the ${VoiceCloudBrand.name} community grows.") }
            itemsIndexed(state.people.distinctBy { it.id }, key = { index, user -> "people:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
        }
    }
}

@Immutable
data class CommunitySearchItem(
    val id: String,
    val name: String,
    val handle: String,
    val memberCount: Int,
)

@Composable
fun SearchScreen(
    state: DiscoveryUiState,
    communities: List<CommunitySearchItem>,
    communitiesLoading: Boolean,
    initialTab: String = "All",
    onSubmit: (String) -> Unit,
    onProfile: (String) -> Unit,
    onRoom: (String) -> Unit,
    onCommunity: (String) -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf(state.search.query) }
    var selectedTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    val tabs = listOf("All", "People", "Creators", "Rooms", "Communities")
    val people = state.search.users.filter { it.role?.uppercase() != "CREATOR" }
    val creators = state.search.users.filter { it.role?.uppercase() == "CREATOR" }
    ConsumerScaffold("search", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ScreenHeader("Search") }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search VoiceCloud") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(painterResource(R.drawable.vc_icon_search), contentDescription = null) },
                    trailingIcon = {
                        FilledIconButton(enabled = query.trim().isNotEmpty(), onClick = { onSubmit(query.trim()) }) {
                            Icon(painterResource(R.drawable.vc_nav_search), contentDescription = "Search")
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                )
            }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab, maxLines = 1, softWrap = false) },
                        )
                    }
                }
            }
            item { StatusBlock(state) }
            if (communitiesLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }

            if (state.search.query.isNotBlank() && selectedTab in setOf("All", "People")) {
                if (selectedTab == "All") item { SectionTitle("People") }
                itemsIndexed(people.distinctBy { it.id }, key = { index, user -> "search-person:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
            }
            if (state.search.query.isNotBlank() && selectedTab in setOf("All", "Creators")) {
                if (selectedTab == "All") item { SectionTitle("Creators") }
                itemsIndexed(creators.distinctBy { it.id }, key = { index, user -> "search-creator:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
            }
            if (state.search.query.isNotBlank() && selectedTab in setOf("All", "Rooms")) {
                if (selectedTab == "All") item { SectionTitle("Rooms") }
                itemsIndexed(state.search.rooms.distinctBy { it.id }, key = { index, room -> "search-room:${room.id}:$index" }) { _, room -> RoomCard(room) { onRoom(room.id) } }
            }
            if (state.search.query.isNotBlank() && selectedTab in setOf("All", "Communities")) {
                if (selectedTab == "All") item { SectionTitle("Communities") }
                itemsIndexed(communities.distinctBy { it.id }, key = { index, community -> "search-community:${community.id}:$index" }) { _, community ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().clickable { onCommunity(community.handle.ifBlank { community.id }) },
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(14.dp), color = ConsumerColors.SapphireSoft) {
                                Icon(
                                    painter = painterResource(R.drawable.vc_icon_community),
                                    contentDescription = null,
                                    tint = ConsumerColors.SapphireDeep,
                                    modifier = Modifier.padding(9.dp).size(22.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(community.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("@${community.handle} · ${community.memberCount} members", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            val noResults = state.search.query.isNotBlank() &&
                state.search.users.isEmpty() && state.search.rooms.isEmpty() && communities.isEmpty() &&
                !state.loading && !communitiesLoading
            if (noResults) item { EmptyBlock("No results", "Try another search.") }
        }
    }
}

@Composable
fun PublicProfileScreen(
    state: DiscoveryUiState,
    username: String,
    isSelf: Boolean,
    onLoad: () -> Unit,
    onFollow: () -> Unit,
    onMessage: (String) -> Unit,
    onMyProfile: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(username) { onLoad() }
    val profile = state.profile
    SecondaryPageLayout(title = "Profile", subtitle = "@${username}", onBack = onBack) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = PaddingValues(bottom = 28.dp)) {
        item { StatusBlock(state, onLoad) }
        if (profile != null) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(28.dp)).background(
                        ConsumerBrushes.Hero
                    ).padding(22.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        UserAvatar(profile.asUser(), 72)
                        Spacer(Modifier.height(12.dp))
                        Text(profile.displayName.ifBlank { profile.username }, color = ConsumerColors.Ink, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text("@${profile.username}", color = ConsumerColors.SapphireDeep)
                        if (profile.isVerified) Text("Verified ${VoiceCloudBrand.name} profile", color = ConsumerColors.SapphireDeep)
                        Spacer(Modifier.height(12.dp))
                        Text(profile.bio ?: profile.statusMessage ?: "${VoiceCloudBrand.name} member", color = ConsumerColors.Text, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(14.dp))
                        if (isSelf) Button(onClick = onMyProfile) { Text("My profile") }
                        else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(enabled = !state.mutationBusy, onClick = onFollow) { Text(if (profile.relationship?.isFollowing == true) "Following" else "Follow") }
                            OutlinedButton(enabled = !state.mutationBusy, onClick = { onMessage(profile.id) }) { Text("Message") }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ProfileMetric((profile.stats?.followersCount ?: profile.followersCount).toString(), "Followers")
                    ProfileMetric((profile.stats?.followingCount ?: profile.followingCount).toString(), "Following")
                    ProfileMetric((profile.wealthLevel).toString(), "Wealth")
                    ProfileMetric((profile.charmLevel).toString(), "Charm")
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("About", style = MaterialTheme.typography.titleLarge)
                    if (!profile.country.isNullOrBlank()) Text("Country · ${profile.country}")
                    if (!profile.preferredLanguage.isNullOrBlank()) Text("Language · ${profile.preferredLanguage}")
                    val interests = profile.interests.orEmpty().ifEmpty { profile.customTags.orEmpty() }
                    if (interests.isNotEmpty()) Text("Interests · ${interests.joinToString(" · ")}")
                }
            }
        }
    }
}
}

@Composable
private fun ProfileMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.Bold, fontSize = 19.sp); Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
fun MyProfileScreen(
    state: DiscoveryUiState,
    isGuest: Boolean,
    onLoad: () -> Unit,
    onFollowers: () -> Unit,
    onFollowing: () -> Unit,
    onEconomy: () -> Unit,
    onUpgrade: () -> Unit,
    onLogout: () -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val profile = state.myProfile
    ConsumerScaffold("profile", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("My Profile", "Your ${VoiceCloudBrand.name} profile and connections.") }
            item { StatusBlock(state, onLoad) }
            if (profile != null) {
                item {
                    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            UserAvatar(profile.asUser(), 72)
                            Spacer(Modifier.height(10.dp))
                            Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.headlineMedium)
                            Text("@${profile.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            profile.bio?.takeIf { it.isNotBlank() }?.let { bio -> Text(bio, modifier = Modifier.padding(top = 12.dp)) }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onFollowers, modifier = Modifier.weight(1f)) { Text("${profile.stats?.followersCount ?: profile.followersCount}\nFollowers") }
                        OutlinedButton(onClick = onFollowing, modifier = Modifier.weight(1f)) { Text("${profile.stats?.followingCount ?: profile.followingCount}\nFollowing") }
                    }
                }
                item { Text("Profile complete · ${profile.profileCompletionPercentage}%", fontWeight = FontWeight.SemiBold) }
                item { Button(onClick = onEconomy, modifier = Modifier.fillMaxWidth()) { Text("Economy & progression") } }
            }
            if (isGuest) item { Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Text("Upgrade guest account") } }
            item { OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Sign out") } }
        }
    }
}

@Composable
fun SocialListScreen(
    state: DiscoveryUiState,
    mode: String,
    onLoad: (String) -> Unit,
    onProfile: (String) -> Unit,
    onUnfollow: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(mode) { onLoad("") }
    SecondaryPageLayout(
        title = if (mode == "followers") "Followers" else "Following",
        subtitle = if (mode == "followers") "People who follow your profile." else "People you follow.",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = adaptivePagePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search ${mode}") },
                    trailingIcon = { TextButton(onClick = { onLoad(search) }) { Text("Search") } },
                )
            }
            item { StatusBlock(state) }
            if (state.socialUsers.isEmpty() && !state.loading) item { EmptyBlock("No ${mode} found", "Matching ${VoiceCloudBrand.name} profiles will appear here.") }
            itemsIndexed(state.socialUsers.distinctBy { it.id }, key = { index, user -> "social:${user.id}:$index" }) { _, user ->
                UserCard(
                    user = user,
                    onOpen = { onProfile(user.username) },
                    actionLabel = if (mode == "following") "Unfollow" else null,
                    actionEnabled = !state.mutationBusy,
                    onAction = if (mode == "following") ({ onUnfollow(user.id, search) }) else null,
                )
            }
        }
    }
}

@Composable
fun FriendsScreen(
    state: DiscoveryUiState,
    onLoad: () -> Unit,
    onProfile: (String) -> Unit,
    onSend: (String) -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onRemove: (String) -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    ConsumerScaffold("friends", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader("Friends", "Manage requests, connections, and people you may know.") }
            item { StatusBlock(state, onLoad) }
            item { SectionTitle("Incoming requests") }
            if (state.pending.incoming.isEmpty() && !state.loading) item { Text("No incoming requests.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            itemsIndexed(state.pending.incoming.distinctBy { it.id }, key = { index, request -> "friend-in:${request.id}:$index" }) { _, request ->
                request.sender?.let { user ->
                    UserCard(user, { onProfile(user.username) }, "Accept", !state.mutationBusy) { onAccept(request.id) }
                    TextButton(enabled = !state.mutationBusy, onClick = { onReject(request.id) }) { Text("Decline request") }
                }
            }
            item { SectionTitle("Friends") }
            if (state.friends.isEmpty() && !state.loading) item { Text("No friends yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            itemsIndexed(state.friends.distinctBy { it.friendshipId.ifBlank { it.user.id } }, key = { index, friend -> "friend:${friend.friendshipId.ifBlank { friend.user.id }}:$index" }) { _, friend ->
                UserCard(friend.user, { onProfile(friend.user.username) }, "Remove", !state.mutationBusy) { onRemove(friend.user.id) }
            }
            item { SectionTitle("Suggested people") }
            itemsIndexed(state.friendSuggestions.distinctBy { it.id }, key = { index, user -> "friend-suggest:${user.id}:$index" }) { _, user ->
                UserCard(user, { onProfile(user.username) }, "Add", !state.mutationBusy) { onSend(user.id) }
            }
            item { SectionTitle("Sent requests") }
            if (state.pending.outgoing.isEmpty() && !state.loading) item { Text("No pending sent requests.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            itemsIndexed(state.pending.outgoing.distinctBy { it.id }, key = { index, request -> "friend-out:${request.id}:$index" }) { _, request ->
                request.receiver?.let { user -> UserCard(user, { onProfile(user.username) }, "Pending", false, null) }
            }
        }
    }
}

private fun VoiceCloudProfile.asUser() = VoiceCloudUser(
    id = id,
    username = username,
    displayName = displayName,
    role = role,
    isGuest = isGuest,
    avatarUrl = avatarUrl,
    coverUrl = coverUrl,
    bio = bio,
    statusMessage = statusMessage,
    country = country,
    preferredLanguage = preferredLanguage,
    interests = interests,
    customTags = customTags,
    hostBadge = hostBadge,
    vipBadge = vipBadge,
    isOnline = isOnline,
    isVerified = isVerified,
    isVip = isVip,
    followersCount = followersCount,
    followingCount = followingCount,
    popularityScore = popularityScore,
    wealthLevel = wealthLevel,
    charmLevel = charmLevel,
    createdAt = createdAt,
)
