package app.voicecloud.feature.discovery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.voicecloud.core.designsystem.theme.ConsumerColors
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
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                listOf(
                    Triple("home", "●", "Home"),
                    Triple("explore", "◇", "Explore"),
                    Triple("search", "⌕", "Search"),
                    Triple("friends", "◎", "Friends"),
                    Triple("profile", "◉", "Profile"),
                ).forEach { (key, glyph, label) ->
                    val action = when (key) {
                        "home" -> onHome
                        "explore" -> onExplore
                        "search" -> onSearch
                        "friends" -> onFriends
                        else -> onProfile
                    }
                    NavigationBarItem(
                        selected = selected == key,
                        onClick = action,
                        icon = { Text(glyph, fontSize = 18.sp) },
                        label = { Text(label) },
                    )
                }
            }
        },
        content = content,
    )
}

@Composable
private fun ScreenHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
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
            Brush.linearGradient(listOf(ConsumerColors.Sapphire, ConsumerColors.Indigo))
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
                    else -> "VoiceCloud member"
                }
                Text(descriptor ?: "VoiceCloud member", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            }
            if (actionLabel != null && onAction != null) {
                OutlinedButton(enabled = actionEnabled, onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun RoomCard(room: VoiceCloudRoom) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(if (room.isLive) Color(0xFFEF4444) else MaterialTheme.colorScheme.outline))
                Spacer(Modifier.width(8.dp))
                Text(if (room.isLive) "LIVE NOW" else room.status.ifBlank { "ROOM" }, color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                if (room.isLocked) Text("Locked", style = MaterialTheme.typography.labelLarge)
            }
            Text(room.title.ifBlank { "VoiceCloud Room" }, style = MaterialTheme.typography.titleLarge)
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
    onPeople: () -> Unit,
    onCreators: () -> Unit,
    onProfile: (String) -> Unit,
    onUpgrade: () -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    ConsumerScaffold("home", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(20.dp).clip(RoundedCornerShape(28.dp)).background(
                        Brush.linearGradient(listOf(ConsumerColors.SapphireDeep, ConsumerColors.Indigo))
                    ).padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("VOICECLOUD", color = Color.White.copy(alpha = .8f), fontWeight = FontWeight.Bold)
                        Text("Listen. Connect. Be heard.", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                        Text("Join live conversations and discover creators and people across VoiceCloud.", color = Color.White.copy(alpha = .9f))
                        Button(onClick = onRooms, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ConsumerColors.SapphireDeep)) { Text("Explore live rooms") }
                    }
                }
            }
            if (isGuest) item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("Save your VoiceCloud identity", fontWeight = FontWeight.Bold); Text("Upgrade your guest account to follow, connect and keep your profile.") }
                        TextButton(onClick = onUpgrade) { Text("Upgrade") }
                    }
                }
            }
            item { StatusBlock(state, onLoad) }
            item { Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { SectionTitle("Live now", "View all", onRooms) } }
            if (state.home.rooms.isEmpty() && !state.loading) item { Box(Modifier.padding(horizontal = 20.dp)) { EmptyBlock("No rooms are live right now", "New live conversations will appear here automatically.") } }
            itemsIndexed(state.home.rooms.distinctBy { it.id }, key = { index, room -> "home-room:${room.id}:$index" }) { _, room -> Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) { RoomCard(room) } }
            item { Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { SectionTitle("People to discover", "See people", onPeople) } }
            itemsIndexed(state.home.people.distinctBy { it.id }.take(5), key = { index, user -> "home-person:${user.id}:$index" }) { _, user -> Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) { UserCard(user, { onProfile(user.username) }) } }
            item { Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { SectionTitle("Creators", "See creators", onCreators) } }
            if (state.home.creators.isEmpty() && !state.loading) item { Box(Modifier.padding(horizontal = 20.dp)) { EmptyBlock("Creators are building their audience", "Verified Creator profiles will appear here as discovery data becomes available.") } }
            itemsIndexed(state.home.creators.distinctBy { it.id }.take(5), key = { index, user -> "home-creator:${user.id}:$index" }) { _, user -> Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) { UserCard(user, { onProfile(user.username) }) } }
        }
    }
}

@Composable
fun ExploreScreen(
    state: DiscoveryUiState,
    onLoad: () -> Unit,
    onRooms: () -> Unit,
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
            item { ScreenHeader("Explore", "Live conversations and consumer-visible people from the finalized VoiceCloud discovery APIs.") }
            item { StatusBlock(state, onLoad) }
            item { SectionTitle("Live rooms", "All rooms", onRooms) }
            itemsIndexed(state.explore.liveRooms.distinctBy { it.id }.take(6), key = { index, room -> "explore-live:${room.id}:$index" }) { _, room -> RoomCard(room) }
            item { SectionTitle("Trending rooms") }
            itemsIndexed(state.explore.trendingRooms.distinctBy { it.id }.take(5), key = { index, room -> "explore-trend:${room.id}:$index" }) { _, room -> RoomCard(room) }
            item { SectionTitle("People", "View all", onPeople) }
            itemsIndexed(state.explore.people.distinctBy { it.id }.take(6), key = { index, user -> "explore-person:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
            item { SectionTitle("Creators", "View all", onCreators) }
            itemsIndexed(state.explore.creators.distinctBy { it.id }.take(6), key = { index, user -> "explore-creator:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
        }
    }
}

@Composable
fun RoomsScreen(state: DiscoveryUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Live Rooms", "Discovery and listing only in PH03. Room detail/join is introduced in PH05.", action = { TextButton(onClick = onBack) { Text("Back") } }) }
        item { StatusBlock(state, onLoad) }
        if (state.rooms.isEmpty() && !state.loading) item { EmptyBlock("No live rooms", "There are no discoverable live rooms at the moment.") }
        itemsIndexed(state.rooms.distinctBy { it.id }, key = { index, room -> "rooms:${room.id}:$index" }) { _, room -> RoomCard(room) }
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
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader(if (creatorsOnly) "Creators" else "People", if (creatorsOnly) "Creator accounts visible to VoiceCloud consumers." else "Consumer-visible VoiceCloud people. Administrative and guest identities are excluded.", action = { TextButton(onClick = onBack) { Text("Back") } }) }
        item { StatusBlock(state, onLoad) }
        if (state.people.isEmpty() && !state.loading) item { EmptyBlock("No profiles found", "VoiceCloud discovery did not return matching consumer profiles.") }
        itemsIndexed(state.people.distinctBy { it.id }, key = { index, user -> "people:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
    }
}

@Composable
fun SearchScreen(
    state: DiscoveryUiState,
    onSubmit: (String) -> Unit,
    onProfile: (String) -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf(state.search.query) }
    ConsumerScaffold("search", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader("Search", "Find live rooms and people by human-readable VoiceCloud identity.") }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search VoiceCloud") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { TextButton(enabled = query.trim().isNotEmpty(), onClick = { onSubmit(query) }) { Text("Search") } },
                )
            }
            item { StatusBlock(state) }
            if (state.search.query.isNotBlank()) item { SectionTitle("People") }
            itemsIndexed(state.search.users.distinctBy { it.id }, key = { index, user -> "search-user:${user.id}:$index" }) { _, user -> UserCard(user, { onProfile(user.username) }) }
            if (state.search.query.isNotBlank()) item { SectionTitle("Rooms") }
            itemsIndexed(state.search.rooms.distinctBy { it.id }, key = { index, room -> "search-room:${room.id}:$index" }) { _, room -> RoomCard(room) }
            if (state.search.query.isNotBlank() && state.search.users.isEmpty() && state.search.rooms.isEmpty() && !state.loading) item { EmptyBlock("No results", "Try another username, display name, room title or category.") }
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
    onMyProfile: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(username) { onLoad() }
    val profile = state.profile
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item { ScreenHeader("Profile", "@${username}", action = { TextButton(onClick = onBack) { Text("Back") } }) }
        item { StatusBlock(state, onLoad) }
        if (profile != null) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(28.dp)).background(
                        Brush.linearGradient(listOf(ConsumerColors.SapphireDeep, ConsumerColors.Indigo))
                    ).padding(22.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        UserAvatar(profile.asUser(), 72)
                        Spacer(Modifier.height(12.dp))
                        Text(profile.displayName.ifBlank { profile.username }, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text("@${profile.username}", color = Color.White.copy(alpha = .8f))
                        if (profile.isVerified) Text("Verified VoiceCloud profile", color = Color.White.copy(alpha = .9f))
                        Spacer(Modifier.height(12.dp))
                        Text(profile.bio ?: profile.statusMessage ?: "VoiceCloud member", color = Color.White, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(14.dp))
                        if (isSelf) Button(onClick = onMyProfile) { Text("My profile") }
                        else Button(enabled = !state.mutationBusy, onClick = onFollow) { Text(if (profile.relationship?.isFollowing == true) "Following" else "Follow") }
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
            item { ScreenHeader("My Profile", "Your authenticated VoiceCloud identity.") }
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
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader(if (mode == "followers") "Followers" else "Following", if (mode == "followers") "People who follow your profile." else "People you follow.", action = { TextButton(onClick = onBack) { Text("Back") } }) }
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
        if (state.socialUsers.isEmpty() && !state.loading) item { EmptyBlock("No ${mode} found", "Matching consumer-visible profiles will appear here.") }
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
            item { ScreenHeader("Friends", "Requests, connections and suggestions from the finalized VoiceCloud social APIs.") }
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
