package app.voicecloud.feature.discovery.ui

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
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
                Text(descriptor, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
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
private fun CompactPeopleRow(users: List<VoiceCloudUser>, onProfile: (String) -> Unit) {
    if (users.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { Text("Nothing to show right now.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(users.distinctBy { it.id }.take(4), key = { it.id.ifBlank { it.username } }) { user -> CompactUserCard(user, onProfile) }
    }
}

@Composable
private fun CompactUserCard(user: VoiceCloudUser, onProfile: (String) -> Unit) {
    ElevatedCard(onClick = { onProfile(user.username) }, modifier = Modifier.width(154.dp), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            UserAvatar(user, 52)
            Text(user.displayName.ifBlank { user.username }, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(if (user.isOnline) "Active now" else "@${user.username}", maxLines = 1, color = if (user.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SearchCompactRowUsers(users: List<VoiceCloudUser>, keyPrefix: String, onProfile: (String) -> Unit, searched: Boolean, emptyLabel: String) {
    if (users.isEmpty()) { Text(if (searched) emptyLabel else "No profiles available right now.", color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(users, key = { "$keyPrefix:${it.id}" }) { CompactUserCard(it, onProfile) } }
}

@Composable
private fun SearchCompactRowRooms(rooms: List<VoiceCloudRoom>, onRoom: (String) -> Unit, searched: Boolean) {
    if (rooms.isEmpty()) { Text(if (searched) "No rooms found" else "No live rooms available right now.", color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(rooms, key = { it.id }) { room ->
            ElevatedCard(onClick = { onRoom(room.id) }, modifier = Modifier.width(230.dp), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(room.title.ifBlank { "VoiceCloud room" }, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${room.listenerCount} listening · ${room.category.ifBlank { "Live" }}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SearchCompactRowCommunities(communities: List<CommunitySearchItem>, onCommunity: (String) -> Unit, searched: Boolean) {
    if (communities.isEmpty()) { Text(if (searched) "No communities found" else "No communities available right now.", color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(communities, key = { it.id }) { CommunityCompactCard(it, onCommunity) } }
}

@Composable
private fun CommunityCompactCard(community: CommunitySearchItem, onCommunity: (String) -> Unit) {
    ElevatedCard(onClick = { onCommunity(community.handle.ifBlank { community.id }) }, modifier = Modifier.width(220.dp), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(community.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("@${community.handle} · ${community.memberCount} members", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VoiceCloudBrandMark(size = 44.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(VoiceCloudBrand.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Live conversations happening now", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = onNotifications) { Icon(painterResource(R.drawable.vc_icon_bell), contentDescription = "Notifications") }
                }
            }
            item { StatusBlock(state, onLoad) }
            if (isGuest) item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Save your profile and connections", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = onUpgrade) { Text("Upgrade") }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 2.dp)) {
                    SectionTitle("Live now", "View all", onRooms)
                    Text("Tap a room to enter the live experience directly.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.home.rooms.isEmpty() && !state.loading) item { Box(Modifier.padding(horizontal = 20.dp)) { EmptyBlock("No live rooms", "Check again soon.") } }
            itemsIndexed(state.home.rooms.distinctBy { it.id }, key = { index, room -> "home-room:${room.id}:$index" }) { _, room ->
                Box(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) { RoomCard(room) { onRoom(room.id) } }
            }
            item {
                val metrics = VoiceCloudPageMetrics.current()
                Column(Modifier.fillMaxWidth().padding(horizontal = metrics.horizontalPadding, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HomeShortcut("Communities", R.drawable.vc_icon_community, onCommunities)
                        HomeShortcut("Messages", R.drawable.vc_icon_message, onMessages)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HomeShortcut("Explore", R.drawable.vc_nav_explore, onExplore)
                        HomeShortcut("Host Studio", R.drawable.vc_icon_host, onHostStudio)
                    }
                }
            }
            item { Column(Modifier.padding(horizontal = 20.dp)) { SectionTitle("People to discover", "View all", onPeople) } }
            item { CompactPeopleRow(state.home.people, onProfile) }
            item { Column(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Creators", "View all", onCreators) } }
            item { CompactPeopleRow(state.home.creators, onProfile) }
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
    onCommunities: () -> Unit,
    onEvents: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val categories = (state.explore.trendingRooms + state.explore.liveRooms).map { it.category.trim() }.filter(String::isNotBlank).distinct().take(8)
    ConsumerScaffold("explore", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ScreenHeader("Explore", "Trends, categories, communities and voices beyond your live feed.") }
            item { StatusBlock(state, onLoad) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeShortcut("Communities", R.drawable.vc_icon_community, onCommunities)
                    HomeShortcut("Events", R.drawable.vc_icon_calendar, onEvents)
                }
            }
            if (categories.isNotEmpty()) item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Browse topics")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories, key = { it }) { category -> AssistChip(onClick = onRooms, label = { Text(category) }) }
                    }
                }
            }
            item { SectionTitle("Trending rooms", "All live rooms", onRooms) }
            if (state.explore.trendingRooms.isEmpty() && !state.loading) item { EmptyBlock("No trends yet", "Trending conversations will appear here as activity grows.") }
            itemsIndexed(state.explore.trendingRooms.distinctBy { it.id }.take(8), key = { index, room -> "explore-trend:${room.id}:$index" }) { _, room -> RoomCard(room) { onRoom(room.id) } }
            item { SectionTitle("Trending people", "View all", onPeople) }
            item { CompactPeopleRow(state.explore.people.filter { it.role?.uppercase() != "CREATOR" }.take(6), onProfile) }
            item { SectionTitle("Creators to discover", "View all", onCreators) }
            item { CompactPeopleRow(state.explore.creators.take(6), onProfile) }
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
    onLoadDefaults: () -> Unit,
    onSubmit: (String) -> Unit,
    onProfile: (String) -> Unit,
    onRoom: (String) -> Unit,
    onCommunity: (String) -> Unit,
    onPeopleViewAll: () -> Unit,
    onCreatorsViewAll: () -> Unit,
    onRoomsViewAll: () -> Unit,
    onCommunitiesViewAll: () -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    val tabs = listOf("All", "People", "Creators", "Rooms", "Communities")
    LaunchedEffect(Unit) { onLoadDefaults() }

    val hasQuery = state.search.query.isNotBlank()
    val resultPeople = state.search.users.filter { it.role?.uppercase() != "CREATOR" }
    val resultCreators = state.search.users.filter { it.role?.uppercase() == "CREATOR" }
    val people = if (hasQuery) resultPeople else state.searchLanding.people
    val creators = if (hasQuery) resultCreators else state.searchLanding.creators
    val rooms = if (hasQuery) state.search.rooms else state.searchLanding.rooms

    ConsumerScaffold("search", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ScreenHeader("Search", if (hasQuery) "Results for “${state.search.query}”" else "Discover active people, creators, rooms and communities.") }
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
                    tabs.forEach { tab -> FilterChip(selected = selectedTab == tab, onClick = { selectedTab = tab }, label = { Text(tab, maxLines = 1) }) }
                }
            }
            item { StatusBlock(state) }
            if (communitiesLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }

            if (selectedTab == "All") {
                val allEmpty = people.isEmpty() && creators.isEmpty() && rooms.isEmpty() && communities.isEmpty()
                if (hasQuery && allEmpty && !state.loading && !communitiesLoading) {
                    item {
                        Box(Modifier.fillParentMaxHeight(.55f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No results found for “${state.search.query}”.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    item { SectionTitle("People", "View all", onPeopleViewAll) }
                    item { SearchCompactRowUsers(people.take(4), "people", onProfile, hasQuery, "No people found") }
                    item { SectionTitle("Creators", "View all", onCreatorsViewAll) }
                    item { SearchCompactRowUsers(creators.take(4), "creators", onProfile, hasQuery, "No creators found") }
                    item { SectionTitle("Rooms", "View all", onRoomsViewAll) }
                    item { SearchCompactRowRooms(rooms.take(4), onRoom, hasQuery) }
                    item { SectionTitle("Communities", "View all", onCommunitiesViewAll) }
                    item { SearchCompactRowCommunities(communities.take(4), onCommunity, hasQuery) }
                }
            } else {
                val empty = when (selectedTab) {
                    "People" -> people.isEmpty()
                    "Creators" -> creators.isEmpty()
                    "Rooms" -> rooms.isEmpty()
                    else -> communities.isEmpty()
                }
                if (empty && !state.loading && !communitiesLoading) {
                    item {
                        Box(Modifier.fillParentMaxHeight(.55f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(if (hasQuery) "No ${selectedTab.lowercase()} found for “${state.search.query}”." else "No ${selectedTab.lowercase()} available right now.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else when (selectedTab) {
                    "People" -> itemsIndexed(people.distinctBy { it.id }, key = { i, u -> "search-people:${u.id}:$i" }) { _, u -> UserCard(u, { onProfile(u.username) }) }
                    "Creators" -> itemsIndexed(creators.distinctBy { it.id }, key = { i, u -> "search-creators:${u.id}:$i" }) { _, u -> UserCard(u, { onProfile(u.username) }) }
                    "Rooms" -> itemsIndexed(rooms.distinctBy { it.id }, key = { i, r -> "search-rooms:${r.id}:$i" }) { _, r -> RoomCard(r) { onRoom(r.id) } }
                    "Communities" -> itemsIndexed(communities.distinctBy { it.id }, key = { i, c -> "search-community:${c.id}:$i" }) { _, c -> CommunityCompactCard(c, onCommunity) }
                    else -> Unit
                }
            }
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
    onReport: (String, String) -> Unit,
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
                        else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(enabled = !state.mutationBusy, onClick = onFollow) { Text(if (profile.relationship?.isFollowing == true) "Following" else "Follow") }
                                OutlinedButton(enabled = !state.mutationBusy, onClick = { onMessage(profile.id) }) { Text("Message") }
                            }
                            TextButton(onClick = { onReport(profile.id, profile.displayName.ifBlank { "@${profile.username}" }) }) { Text("Report profile") }
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
    onEconomySection: (String) -> Unit,
    onEditProfile: () -> Unit,
    onProfileTools: () -> Unit,
    onSettings: () -> Unit,
    onSecurity: () -> Unit,
    onSafety: () -> Unit,
    onHelpPages: () -> Unit,
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
    val economy = listOf(
        "WALLET" to "Wallet",
        "VIP" to "VIP",
        "STORE" to "Store",
        "GIFTS" to "Gifts",
        "TASKS" to "Tasks",
        "ACHIEVEMENTS" to "Achievements",
        "PROGRESSION" to "XP & Check-in",
        "RANKINGS" to "Rankings",
        "TICKETS" to "Tickets",
        "REFERRALS" to "Referrals",
    )
    ConsumerScaffold("profile", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader("My Profile", "Your ${VoiceCloudBrand.name} identity, activity and account tools.") }
            item { StatusBlock(state, onLoad) }
            if (profile != null) {
                item {
                    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            UserAvatar(profile.asUser(), 72)
                            Spacer(Modifier.height(10.dp))
                            Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.headlineMedium)
                            Text("@${profile.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            profile.bio?.takeIf { it.isNotBlank() }?.let { Text(it, modifier = Modifier.padding(top = 10.dp)) }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onFollowers, modifier = Modifier.weight(1f)) { Text("${profile.stats?.followersCount ?: profile.followersCount}\nFollowers") }
                        OutlinedButton(onClick = onFollowing, modifier = Modifier.weight(1f)) { Text("${profile.stats?.followingCount ?: profile.followingCount}\nFollowing") }
                    }
                }
                item { Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) { Text("Edit profile") } }
                item { OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings") } }
                item { OutlinedButton(onClick = onSecurity, modifier = Modifier.fillMaxWidth()) { Text("Security & devices") } }
                item { OutlinedButton(onClick = onSafety, modifier = Modifier.fillMaxWidth()) { Text("Safety Center") } }
                item { OutlinedButton(onClick = onProfileTools, modifier = Modifier.fillMaxWidth()) { Text("Replays, activity & privacy") } }
                item { SectionTitle("Economy & progression") }
                items(economy, key = { it.first }) { (key, label) ->
                    ElevatedCard(onClick = { onEconomySection(key) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item { SectionTitle("Support & information") }
                item { OutlinedButton(onClick = onHelpPages, modifier = Modifier.fillMaxWidth()) { Text("Help, terms & information") } }
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
    onCancel: (String) -> Unit,
    onRemove: (String) -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    var selected by rememberSaveable { mutableStateOf("Friends") }
    val tabs = listOf("Friends", "Requests", "Suggestions")
    ConsumerScaffold("friends", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader("Friends", "Connections and friend requests from VoiceCloud.") }
            item { StatusBlock(state, onLoad) }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEach { tab -> FilterChip(selected == tab, { selected = tab }, label = { Text(if (tab == "Requests" && state.pending.incoming.isNotEmpty()) "$tab (${state.pending.incoming.size})" else tab) }) }
                }
            }
            when (selected) {
                "Friends" -> {
                    if (state.friends.isEmpty() && !state.loading) item { EmptyBlock("No friends yet", "Accepted friends will appear here. Open Suggestions to discover people.") }
                    itemsIndexed(state.friends.distinctBy { it.friendshipId.ifBlank { it.user.id } }, key = { i, f -> "friend:${f.friendshipId.ifBlank { f.user.id }}:$i" }) { _, friend ->
                        UserCard(friend.user, { onProfile(friend.user.username) }, "Remove", !state.mutationBusy) { onRemove(friend.user.id) }
                    }
                }
                "Requests" -> {
                    item { SectionTitle("Incoming") }
                    if (state.pending.incoming.isEmpty() && !state.loading) item { Text("No incoming requests.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    itemsIndexed(state.pending.incoming.distinctBy { it.id }, key = { i, r -> "friend-in:${r.id}:$i" }) { _, request ->
                        request.sender?.let { user ->
                            UserCard(user, { onProfile(user.username) }, "Accept", !state.mutationBusy) { onAccept(request.id) }
                            TextButton(enabled = !state.mutationBusy, onClick = { onReject(request.id) }) { Text("Decline") }
                        }
                    }
                    item { SectionTitle("Sent") }
                    if (state.pending.outgoing.isEmpty() && !state.loading) item { Text("No pending sent requests.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    itemsIndexed(state.pending.outgoing.distinctBy { it.id }, key = { i, r -> "friend-out:${r.id}:$i" }) { _, request ->
                        request.receiver?.let { user -> UserCard(user, { onProfile(user.username) }, "Cancel", !state.mutationBusy) { onCancel(request.id) } }
                    }
                }
                else -> {
                    if (state.friendSuggestions.isEmpty() && !state.loading) item { EmptyBlock("No suggestions right now", "VoiceCloud suggestions update as your activity and connections change.") }
                    itemsIndexed(state.friendSuggestions.distinctBy { it.id }, key = { i, u -> "friend-suggest:${u.id}:$i" }) { _, user ->
                        UserCard(user, { onProfile(user.username) }, "Add", !state.mutationBusy) { onSend(user.id) }
                    }
                }
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
