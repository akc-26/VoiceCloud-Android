package app.voicecloud.feature.discovery.ui

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedCard
import app.voicecloud.core.designsystem.component.VcGreetingCard
import app.voicecloud.core.designsystem.component.VcLiveBadge
import app.voicecloud.core.designsystem.component.VcPersonSuggestCard
import app.voicecloud.core.designsystem.component.VcRoomCard
import app.voicecloud.core.designsystem.component.VcSearchField
import app.voicecloud.core.designsystem.component.VcSectionHeader
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedChip
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedFeaturedRoom
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedMetricRow
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedPersonRow
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedPrimaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedRoomRow
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSearchBar
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSectionTitle
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.component.VoiceCloudAnimatedWaveform
import app.voicecloud.core.designsystem.component.VoiceCloudRemoteMedia
import app.voicecloud.core.designsystem.component.VoiceCloudAvatar
import app.voicecloud.core.designsystem.component.VoiceCloudAudioArtwork
import app.voicecloud.core.designsystem.component.VoiceCloudEmptyVisual
import app.voicecloud.core.designsystem.component.VoiceCloudGlossCard
import app.voicecloud.core.designsystem.component.VoiceCloudPageHero
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudPosterArtwork
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.voiceCloudVisualFor
import app.voicecloud.core.designsystem.component.VoiceCloudHeroCard
import app.voicecloud.core.designsystem.component.VoiceCloudLiveBadge
import app.voicecloud.core.designsystem.component.VoiceCloudPremiumCard
import app.voicecloud.core.designsystem.component.VoiceCloudShimmer
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
    onLive: () -> Unit,
    onFriends: () -> Unit,
    onProfile: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = ConsumerColors.Cloud,
        bottomBar = {
            Surface(
                color = ConsumerColors.Surface,
                tonalElevation = 0.dp,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    ConsumerBoardNavItem(
                        label = "Home",
                        icon = R.drawable.vc_nav_home,
                        selectedIcon = R.drawable.vc_nav_home_selected,
                        selected = selected == "home",
                        onClick = onHome,
                    )
                    ConsumerBoardNavItem(
                        label = "Discover",
                        icon = R.drawable.vc_nav_explore,
                        selectedIcon = R.drawable.vc_nav_explore_selected,
                        selected = selected == "explore",
                        onClick = onExplore,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 8.dp)) {
                        Surface(
                            onClick = onLive,
                            shape = CircleShape,
                            color = ConsumerColors.Sapphire,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(painterResource(R.drawable.vc_nav_live), contentDescription = "Live", tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                        }
                    }
                    ConsumerBoardNavItem(
                        label = "Messages",
                        icon = R.drawable.vc_icon_message,
                        selectedIcon = R.drawable.vc_icon_message,
                        selected = selected == "friends",
                        onClick = onFriends,
                    )
                    ConsumerBoardNavItem(
                        label = "Profile",
                        icon = R.drawable.vc_nav_profile,
                        selectedIcon = R.drawable.vc_nav_profile_selected,
                        selected = selected == "profile",
                        onClick = onProfile,
                    )
                }
            }
        },
        content = content,
    )
}

@Composable
private fun RowScope.ConsumerBoardNavItem(
    label: String,
    icon: Int,
    selectedIcon: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.weight(1f).fillMaxHeight(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(if (selected) selectedIcon else icon),
                contentDescription = label,
                tint = if (selected) ConsumerColors.Sapphire else ConsumerColors.TextMuted,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) ConsumerColors.Sapphire else ConsumerColors.TextMuted,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    val metrics = VoiceCloudPageMetrics.current()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = metrics.horizontalPadding, vertical = metrics.contentTopSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.headlineMedium)
            if (!subtitle.isNullOrBlank()) Text(voiceCloudTitleCase(subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val metrics = VoiceCloudPageMetrics.current()
    Scaffold(
        containerColor = ConsumerColors.Cloud,
        topBar = { Row(Modifier.fillMaxWidth().padding(horizontal = metrics.horizontalPadding)) { VoiceCloudApprovedTopBar(title = title, onBack = onBack) } },
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
private fun StatusBlock(state: DiscoveryUiState, onRetry: (() -> Unit)? = null) {
    VoiceCloudToastEffect(state.error, state.notice)
    if (state.loading) Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        VoiceCloudShimmer(Modifier.fillMaxWidth().height(72.dp))
        VoiceCloudShimmer(Modifier.fillMaxWidth(.78f).height(18.dp))
    }
}

@Composable
private fun SectionTitle(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    VoiceCloudApprovedSectionTitle(title = title, action = actionLabel, onAction = onAction)
}

@Composable
private fun UserAvatar(user: VoiceCloudUser, size: Int = 48) {
    VoiceCloudAvatar(
        url = user.avatarUrl,
        label = user.displayName.ifBlank { user.username },
        size = size.dp,
        online = user.isOnline,
        verified = user.isVerified,
    )
}

@Composable
private fun UserCard(
    user: VoiceCloudUser,
    onOpen: () -> Unit,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: (() -> Unit)? = null,
) {
    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 8.dp, onClick = onOpen) {
        VoiceCloudApprovedPersonRow(
            name = user.displayName.ifBlank { user.username },
            subtitle = when {
                user.followersCount > 0 -> "${user.followersCount} followers"
                user.role?.uppercase() == "CREATOR" -> "Creator"
                user.isOnline -> "Online now"
                else -> "@${user.username}"
            },
            avatarUrl = user.avatarUrl,
            actionLabel = actionLabel,
            online = user.isOnline,
            verified = user.isVerified,
            onAction = if (actionEnabled) onAction else null,
        )
    }
}

@Composable
private fun RoomCard(room: VoiceCloudRoom, onOpen: () -> Unit) {
    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 8.dp) {
        VoiceCloudApprovedRoomRow(
            title = room.title.ifBlank { "VoiceCloud Room" },
            subtitle = listOf(room.category, room.language).filter(String::isNotBlank).joinToString(" · ").ifBlank { "Live audio" },
            imageUrl = room.coverUrl,
            meta = "${room.listenerCount}",
            live = room.isLive,
            onClick = onOpen,
        )
    }
}

@Composable
private fun EmptyBlock(title: String, body: String) {
    VoiceCloudEmptyVisual(title, body, Modifier.fillMaxWidth(), voiceCloudVisualFor(title))
}

@Composable
private fun RowScope.HomeShortcut(label: String, iconRes: Int, onClick: () -> Unit) {
    VoiceCloudApprovedCard(Modifier.weight(1f).height(64.dp), onClick = onClick, contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(painterResource(iconRes), contentDescription = null, tint = ConsumerColors.Sapphire, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = ConsumerColors.Text, maxLines = 1)
        }
    }
}

@Composable
private fun CompactPeopleRow(users: List<VoiceCloudUser>, onProfile: (String) -> Unit) {
    if (users.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { Text(voiceCloudTitleCase("Nothing to show right now."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(users.distinctBy { it.id }.take(4), key = { it.id.ifBlank { it.username } }) { user -> CompactUserCard(user, onProfile) }
    }
}

@Composable
private fun CompactUserCard(user: VoiceCloudUser, onProfile: (String) -> Unit) {
    VoiceCloudApprovedCard(Modifier.width(158.dp).clickable { onProfile(user.username) }, contentPadding = 12.dp) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { UserAvatar(user, 58) }
        Text(user.displayName.ifBlank { user.username }, Modifier.fillMaxWidth(), textAlign=TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.ExtraBold)
        Text(if (user.isOnline) voiceCloudTitleCase("Active now") else "@${user.username}", Modifier.fillMaxWidth(), textAlign=TextAlign.Center, maxLines = 1, color = if (user.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SearchCompactRowUsers(users: List<VoiceCloudUser>, keyPrefix: String, onProfile: (String) -> Unit, searched: Boolean, emptyLabel: String) {
    if (users.isEmpty()) { Text(voiceCloudTitleCase(if (searched) emptyLabel else "No profiles available right now."), color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(users, key = { "$keyPrefix:${it.id}" }) { CompactUserCard(it, onProfile) } }
}

@Composable
private fun SearchCompactRowRooms(rooms: List<VoiceCloudRoom>, onRoom: (String) -> Unit, searched: Boolean) {
    if (rooms.isEmpty()) { Text(voiceCloudTitleCase(if (searched) "No rooms found" else "No live rooms available right now."), color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(rooms, key = { it.id }) { room ->
            ElevatedCard(onClick = { onRoom(room.id) }, modifier = Modifier.width(236.dp), shape = RoundedCornerShape(20.dp)) {
                Column {
                    VoiceCloudRemoteMedia(room.coverUrl, room.title, Modifier.fillMaxWidth().height(96.dp), VoiceCloudVisualKind.LIVE, dark = room.isLive)
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(room.title.ifBlank { "VoiceCloud Room" }, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(voiceCloudTitleCase("${room.listenerCount} listening · ${room.category.ifBlank { "Live" }}"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCompactRowCommunities(communities: List<CommunitySearchItem>, onCommunity: (String) -> Unit, searched: Boolean) {
    if (communities.isEmpty()) { Text(voiceCloudTitleCase(if (searched) "No communities found" else "No communities available right now."), color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(communities, key = { it.id }) { CommunityCompactCard(it, onCommunity) } }
}

@Composable
private fun CommunityCompactCard(community: CommunitySearchItem, onCommunity: (String) -> Unit) {
    ElevatedCard(onClick = { onCommunity(community.handle.ifBlank { community.id }) }, modifier = Modifier.width(220.dp), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(community.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(voiceCloudTitleCase("@${community.handle} · ${community.memberCount} Members"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun HomeRoomVisualCard(room: VoiceCloudRoom, onOpen: () -> Unit) {
    val dark = room.isLive || room.listenerCount > 0
    VoiceCloudApprovedCard(
        modifier = Modifier.width(226.dp).heightIn(min = 252.dp).clickable(onClick = onOpen),
        dark = dark,
        contentPadding = 0.dp,
    ) {
        Box(Modifier.fillMaxWidth().height(148.dp)) {
            VoiceCloudRemoteMedia(
                url = room.coverUrl,
                contentDescription = room.title.ifBlank { "Live room artwork" },
                modifier = Modifier.fillMaxSize(),
                kind = if (room.category.contains("music", true)) VoiceCloudVisualKind.AUDIO else if (room.category.contains("talk", true)) VoiceCloudVisualKind.EVENT else VoiceCloudVisualKind.LIVE,
                dark = dark,
            )
            Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, ConsumerColors.DeepNavy.copy(alpha=.72f)))))
            Row(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (room.isLive) VoiceCloudLiveBadge() else Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .88f)) {
                    Text(voiceCloudTitleCase(room.status.ifBlank { "Room" }), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.Ink)
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(alpha=.28f)) {
                    Text(voiceCloudTitleCase("${room.listenerCount} listening"), Modifier.padding(horizontal=8.dp, vertical=4.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(room.title.ifBlank { "VoiceCloud Room" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = if (dark) Color.White else ConsumerColors.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(voiceCloudTitleCase(room.category.ifBlank { "Live audio" }), color = if (dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceCloudPictogram(VoiceCloudVisualKind.AUDIENCE, size = 30.dp, dark = dark)
                Text(voiceCloudTitleCase("${room.speakerCount} speakers"), color = if (dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted, style = MaterialTheme.typography.labelMedium)
                if (room.isPremium) {
                    Surface(shape = RoundedCornerShape(50), color = ConsumerColors.VipGold.copy(alpha=.16f)) {
                        Text(voiceCloudTitleCase("VIP"), Modifier.padding(horizontal=7.dp, vertical=3.dp), color = ConsumerColors.VipGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeRankedHost(user: VoiceCloudUser, rank: Int, onProfile: (String) -> Unit) {
    VoiceCloudApprovedCard(
        modifier = Modifier.width(148.dp).clickable { onProfile(user.username) },
        contentPadding = 12.dp,
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            VoiceCloudAvatar(user.avatarUrl, user.displayName.ifBlank { user.username }, size = 68.dp, online = user.isOnline, verified = user.isVerified)
            Surface(Modifier.align(Alignment.TopStart), shape = CircleShape, color = if (rank == 1) ConsumerColors.VipGold else ConsumerColors.SurfaceSoft) {
                Text(rank.toString(), Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = ConsumerColors.Ink)
            }
        }
        Text(user.displayName.ifBlank { user.username }, Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(voiceCloudTitleCase(user.role ?: if (user.isVerified) "Verified Creator" else "VoiceCloud Creator"), Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = ConsumerColors.TextMuted, maxLines = 1)
        Text(voiceCloudTitleCase("${user.followersCount} followers"), Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = ConsumerColors.VioletDeep, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HomeScreen(
    state: DiscoveryUiState,
    isGuest: Boolean,
    greetingName: String,
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
    onLive: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
    onCommunities: () -> Unit,
    onEvents: () -> Unit,
    onMessages: () -> Unit,
    onNotifications: () -> Unit,
    onHostStudio: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val rooms = state.home.rooms.distinctBy { it.id }
    val liveRooms = rooms.filter { it.isLive }
    val popularRooms = rooms.filterNot { it.isLive }.ifEmpty { rooms.drop(1) }
    val featuredLive = liveRooms.firstOrNull() ?: rooms.firstOrNull()

    ConsumerScaffold("home", onHome, onExplore, onLive, onFriends, onMe) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).background(ConsumerColors.Cloud),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                    VoiceCloudBrandMark(28.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("VoiceCloud", style = MaterialTheme.typography.titleLarge, color = ConsumerColors.Text, modifier = Modifier.weight(1f))
                    IconButton(onClick = onNotifications, modifier = Modifier.size(36.dp)) {
                        Icon(painterResource(R.drawable.vc_icon_bell), "Notifications", tint = ConsumerColors.Ink, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onMe, modifier = Modifier.size(36.dp)) {
                        Icon(painterResource(R.drawable.vc_nav_profile), "Profile", tint = ConsumerColors.Ink, modifier = Modifier.size(20.dp))
                    }
                }
            }
            item { VcSearchField("Search rooms, people, communities...", onSearch) }
            item { Spacer(Modifier.height(8.dp)) }
            item {
                VcGreetingCard(
                    name = greetingName.ifBlank { "there" },
                    subtitle = "Great conversations are waiting for you!",
                    onJoinLive = featuredLive?.let { { onRoom(it.id) } } ?: onLive,
                )
            }
            item { StatusBlock(state, onLoad) }
            item { VcSectionHeader("Live now", "See all", onRooms) }
            if (liveRooms.isEmpty() && !state.loading) {
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth()) {
                        Text("No live rooms right now.", style = MaterialTheme.typography.bodyLarge, color = ConsumerColors.TextMuted)
                    }
                }
            } else {
                items(liveRooms.take(4), key = { "home-live:${it.id}" }) { room ->
                    VcRoomCard(
                        title = room.title.ifBlank { "VoiceCloud Room" },
                        host = room.category.ifBlank { "Host" },
                        topic = room.language.ifBlank { "Live audio" },
                        listeners = "${room.listenerCount}",
                        live = true,
                        coverUrl = room.coverUrl,
                        onClick = { onRoom(room.id) },
                        onJoin = { onRoom(room.id) },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            item { VcSectionHeader("Popular rooms", "See all", onRooms) }
            items(popularRooms.take(3), key = { "home-popular:${it.id}" }) { room ->
                VcRoomCard(
                    title = room.title.ifBlank { "VoiceCloud Room" },
                    host = room.category.ifBlank { "Host" },
                    topic = room.language.ifBlank { "Live audio" },
                    listeners = "${room.listenerCount}",
                    live = room.isLive,
                    coverUrl = room.coverUrl,
                    onClick = { onRoom(room.id) },
                    onJoin = if (room.isLive) ({ onRoom(room.id) }) else null,
                )
                Spacer(Modifier.height(12.dp))
            }
            val recommendedPeople = (state.home.creators + state.home.people).distinctBy { it.id }.take(8)
            item { VcSectionHeader("People you may like", "See all", onPeople) }
            if (recommendedPeople.isEmpty() && !state.loading) {
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth()) {
                        Text("People recommendations will appear as your VoiceCloud network grows.", style = MaterialTheme.typography.bodyLarge, color = ConsumerColors.TextMuted)
                    }
                }
            } else {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(recommendedPeople, key = { it.id }) { user ->
                            VcPersonSuggestCard(
                                name = user.displayName.ifBlank { user.username },
                                subtitle = "${user.followersCount} followers",
                                photo = user.avatarUrl,
                                onClick = { onProfile(user.username) },
                            )
                        }
                    }
                }
            }
            item { VcSectionHeader("Upcoming events", "See all", onEvents) }
            item {
                VoiceCloudApprovedCard(Modifier.fillMaxWidth(), onClick = onEvents) {
                    Text("Events", style = MaterialTheme.typography.titleSmall, color = ConsumerColors.Text)
                    Text("See live and upcoming VoiceCloud events.", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                }
            }
            item { VcSectionHeader("Communities", "See all", onCommunities) }
            item {
                VoiceCloudApprovedCard(Modifier.fillMaxWidth(), onClick = onCommunities) {
                    Text("Communities", style = MaterialTheme.typography.titleSmall, color = ConsumerColors.Text)
                    Text("Find groups and rooms that match your interests.", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                }
            }
            item { VcSectionHeader("Shortcuts") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeShortcut("Discover", R.drawable.vc_nav_explore, onExplore)
                    HomeShortcut("Host Studio", R.drawable.vc_icon_host, onHostStudio)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeShortcut("Creators", R.drawable.vc_icon_host, onCreators)
                    HomeShortcut("Messages", R.drawable.vc_icon_message, onMessages)
                }
            }
            if (isGuest) item {
                Surface(shape = RoundedCornerShape(16.dp), color = ConsumerColors.SapphireSoft, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Create an account to save rooms and follow creators.", style = MaterialTheme.typography.bodyLarge, color = ConsumerColors.SapphireDeep, modifier = Modifier.weight(1f))
                        TextButton(onClick = onUpgrade) { Text("Upgrade", style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
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
    onLive: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
    onCommunities: () -> Unit,
    onEvents: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val liveRooms = state.explore.liveRooms.distinctBy { it.id }
    val trendingRooms = state.explore.trendingRooms.distinctBy { it.id }
    val creators = (state.explore.creators + state.explore.people).distinctBy { it.id }
    val categories = (trendingRooms + liveRooms)
        .map { it.category.trim() }.filter(String::isNotBlank).distinct().take(9)
    var tab by rememberSaveable { mutableStateOf("All") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    val featured = liveRooms.firstOrNull() ?: trendingRooms.firstOrNull()
    val visibleRooms = when (tab) {
        "Live" -> liveRooms
        else -> (trendingRooms + liveRooms).distinctBy { it.id }
    }.let { list ->
        if (selectedCategory.isBlank()) list else list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    ConsumerScaffold("explore", onHome, onExplore, onLive, onFriends, onMe) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).background(ConsumerColors.Cloud),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Explore", style = MaterialTheme.typography.headlineMedium, color = ConsumerColors.Text, modifier = Modifier.weight(1f))
                    IconButton(onClick = onSearch) {
                        Icon(painterResource(R.drawable.vc_icon_search), "Search", tint = ConsumerColors.Ink)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Live", "Events", "Communities").forEach { label ->
                        VoiceCloudApprovedChip(label, tab == label) {
                            when (label) {
                                "Events" -> onEvents()
                                "Communities" -> onCommunities()
                                else -> tab = label
                            }
                        }
                    }
                }
            }
            item { StatusBlock(state, onLoad) }
            if (featured != null) {
                item {
                    Box(
                        Modifier.fillMaxWidth().height(168.dp).clip(RoundedCornerShape(20.dp)).clickable { onRoom(featured.id) },
                    ) {
                        VoiceCloudRemoteMedia(featured.coverUrl, featured.title, Modifier.fillMaxSize(), VoiceCloudVisualKind.LIVE, dark = true, contentScale = ContentScale.Crop)
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            VcLiveBadge(small = true)
                            Text("Featured Live Rooms", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            Text(featured.title.ifBlank { "Live room" }, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Surface(onClick = { onRoom(featured.id) }, shape = RoundedCornerShape(20.dp), color = ConsumerColors.Sapphire) {
                                Text("Join", Modifier.padding(horizontal = 16.dp, vertical = 7.dp), color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
            if (categories.isNotEmpty()) {
                item { VcSectionHeader("Categories") }
                item {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { category ->
                            val active = selectedCategory.equals(category, ignoreCase = true)
                            Surface(
                                onClick = { selectedCategory = if (active) "" else category },
                            shape = RoundedCornerShape(14.dp),
                            color = if (active) ConsumerColors.SapphireSoft else ConsumerColors.SurfaceSoft,
                            border = BorderStroke(1.dp, if (active) ConsumerColors.Sapphire else ConsumerColors.Border),
                        ) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp).width(72.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(category.take(1), style = MaterialTheme.typography.titleMedium)
                                Text(category, style = MaterialTheme.typography.labelSmall, color = ConsumerColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        }
                    }
                }
            }
            item { VcSectionHeader("Trending", "See all", onRooms) }
            if (visibleRooms.isEmpty() && !state.loading) {
                item { VoiceCloudApprovedCard(Modifier.fillMaxWidth()) { Text("Live and trending rooms will appear here.", style = MaterialTheme.typography.bodyLarge, color = ConsumerColors.TextMuted) } }
            } else {
                items(visibleRooms.take(6), key = { "discover-room:${it.id}" }) { room ->
                    VcRoomCard(
                        title = room.title.ifBlank { "VoiceCloud Room" },
                        host = room.category.ifBlank { "Host" },
                        topic = room.language.ifBlank { "Live audio" },
                        listeners = "${room.listenerCount}",
                        live = room.isLive,
                        coverUrl = room.coverUrl,
                        onClick = { onRoom(room.id) },
                        onJoin = if (room.isLive) ({ onRoom(room.id) }) else null,
                    )
                }
            }
            item { VcSectionHeader("Rising creators", "See all", onCreators) }
            if (creators.isEmpty() && !state.loading) {
                item { VoiceCloudApprovedCard(Modifier.fillMaxWidth()) { Text("Creator recommendations will appear as the community grows.", style = MaterialTheme.typography.bodyLarge, color = ConsumerColors.TextMuted) } }
            } else {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(creators.take(8), key = { "discover-creator:${it.id}" }) { creator ->
                            VcPersonSuggestCard(
                                name = creator.displayName.ifBlank { creator.username },
                                subtitle = "${creator.followersCount} followers",
                                photo = creator.avatarUrl,
                                onClick = { onProfile(creator.username) },
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
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
            item { VoiceCloudApprovedSectionTitle("Live now") }
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
            item { VoiceCloudApprovedSectionTitle(if (creatorsOnly) "Suggested creators" else "People to connect with") }
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
    onLive: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    val tabs = listOf("All", "Rooms", "People", "Communities", "Topics")
    LaunchedEffect(Unit) { onLoadDefaults() }
    val hasQuery = state.search.query.isNotBlank()
    val resultPeople = state.search.users.filter { it.role?.uppercase() != "CREATOR" }
    val resultCreators = state.search.users.filter { it.role?.uppercase() == "CREATOR" }
    val people = if (hasQuery) resultPeople else state.searchLanding.people
    val creators = if (hasQuery) resultCreators else state.searchLanding.creators
    val rooms = if (hasQuery) state.search.rooms else state.searchLanding.rooms

    ConsumerScaffold("explore", onHome, onExplore, onLive, onFriends, onMe) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).background(ConsumerColors.Cloud),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { VoiceCloudApprovedTopBar(title = "Search") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    VoiceCloudApprovedSearchBar(query, { query = it }, placeholder = "Search VoiceCloud")
                    if (query.isNotBlank()) {
                        VoiceCloudApprovedPrimaryButton("Search", enabled = !state.loading) { onSubmit(query.trim()) }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    tabs.forEach { tab -> VoiceCloudApprovedChip(tab, selectedTab == tab) { selectedTab = tab } }
                }
            }
            item { StatusBlock(state) }
            if (communitiesLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }

            if (selectedTab == "All") {
                if (hasQuery && people.isEmpty() && creators.isEmpty() && rooms.isEmpty() && communities.isEmpty() && !state.loading && !communitiesLoading) {
                    item { VoiceCloudEmptyVisual("No Results Found", "No matches for “${state.search.query}”. Try another name, room or topic.", Modifier.fillMaxWidth(), VoiceCloudVisualKind.SEARCH) }
                } else {
                    item { SectionTitle("Top Rooms", "View All", onRoomsViewAll) }
                    items(rooms.take(3), key = { "search-room:${it.id}" }) { room ->
                        VoiceCloudApprovedRoomRow(room.title.ifBlank { "VoiceCloud Room" }, room.category.ifBlank { "Live audio" }, room.coverUrl, "${room.listenerCount}", live = room.isLive) { onRoom(room.id) }
                    }
                    item { SectionTitle("People", "View All", onPeopleViewAll) }
                    items((people + creators).distinctBy { it.id }.take(4), key = { "search-person:${it.id}" }) { user ->
                        VoiceCloudApprovedPersonRow(user.displayName.ifBlank { user.username }, if (user.role?.uppercase() == "CREATOR") "Creator · ${user.followersCount} followers" else "@${user.username}", user.avatarUrl, actionLabel = "View", online = user.isOnline, verified = user.isVerified, onClick = { onProfile(user.username) }, onAction = { onProfile(user.username) })
                    }
                    item { SectionTitle("Communities", "View All", onCommunitiesViewAll) }
                    items(communities.take(3), key = { "search-community:${it.id}" }) { community -> CommunityCompactCard(community, onCommunity) }
                }
            } else when (selectedTab) {
                "Rooms" -> items(rooms.distinctBy { it.id }, key = { "search-rooms:${it.id}" }) { room ->
                    VoiceCloudApprovedRoomRow(room.title.ifBlank { "VoiceCloud Room" }, room.category.ifBlank { "Live audio" }, room.coverUrl, "${room.listenerCount}", live = room.isLive) { onRoom(room.id) }
                }
                "People" -> items((people + creators).distinctBy { it.id }, key = { "search-users:${it.id}" }) { user ->
                    UserCard(user, { onProfile(user.username) })
                }
                "Communities" -> items(communities.distinctBy { it.id }, key = { "search-communities:${it.id}" }) { community -> CommunityCompactCard(community, onCommunity) }
                else -> {
                    val topics = rooms.map { it.category }.filter(String::isNotBlank).distinct()
                    items(topics, key = { "topic:$it" }) { topic ->
                        VoiceCloudApprovedCard(Modifier.fillMaxWidth(), onClick = onExplore) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                VoiceCloudPictogram(voiceCloudVisualFor(topic), size = 38.dp)
                                Text(voiceCloudTitleCase(topic), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ConsumerColors.Ink)
                            }
                        }
                    }
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
    SecondaryPageLayout(title = "Profile", onBack = onBack) { pageModifier ->
        LazyColumn(
            pageModifier.background(ConsumerColors.Cloud),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { StatusBlock(state, onLoad) }
            if (profile != null) {
                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        UserAvatar(profile.asUser(), 78)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                            if (profile.isVerified) Text("✓", color = ConsumerColors.Sapphire, fontWeight = FontWeight.Bold)
                        }
                        Text("@${profile.username}", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                        VoiceCloudApprovedMetricRow(
                            listOf(
                                (profile.stats?.followersCount ?: profile.followersCount).toString() to "Followers",
                                (profile.stats?.followingCount ?: profile.followingCount).toString() to "Following",
                                (profile.stats?.badgesCount ?: 0).toString() to "Badges",
                            ),
                            Modifier.padding(top = 5.dp),
                        )
                    }
                }
                item {
                    if (isSelf) VoiceCloudApprovedPrimaryButton("My Profile", onClick = onMyProfile)
                    else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onFollow,
                            enabled = !state.mutationBusy,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ConsumerColors.Sapphire),
                        ) { Text(if (profile.relationship?.isFollowing == true) "Following" else "Follow", style = MaterialTheme.typography.labelMedium) }
                        OutlinedButton(
                            onClick = { onMessage(profile.id) },
                            enabled = !state.mutationBusy,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) { Text("Message", style = MaterialTheme.typography.labelMedium) }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Rooms", "About", "Clips", "Community").forEachIndexed { index, tab -> VoiceCloudApprovedChip(tab, index == 1) { } }
                    }
                }
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                            Text(profile.bio ?: profile.statusMessage ?: "VoiceCloud member", style = MaterialTheme.typography.bodyMedium, color = ConsumerColors.TextMuted)
                            if (!profile.country.isNullOrBlank()) Text("◉  ${profile.country}", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                            val interests = profile.interests.orEmpty().ifEmpty { profile.customTags.orEmpty() }
                            if (interests.isNotEmpty()) {
                                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    interests.take(6).forEach { interest -> VoiceCloudApprovedChip(interest, false) { } }
                                }
                            }
                        }
                    }
                }
                if (!isSelf) item {
                    TextButton(onClick = { onReport(profile.id, profile.displayName.ifBlank { "@${profile.username}" }) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Report Profile", style = MaterialTheme.typography.labelSmall, color = CommonColors.Error)
                    }
                }
            }
        }
    }
}

@Composable
fun MyProfileScreen(
    state: DiscoveryUiState,
    isGuest: Boolean,
    onLoad: () -> Unit,
    onFollowers: () -> Unit,
    onFollowing: () -> Unit,
    onEconomy: () -> Unit,
    onEditProfile: () -> Unit,
    onProfileTools: () -> Unit,
    onSettings: () -> Unit,
    onSafety: () -> Unit,
    onHelp: () -> Unit,
    onContactSupport: () -> Unit,
    onAbout: () -> Unit,
    canSwitchToCreator: Boolean,
    onSwitchToCreator: () -> Unit,
    onUpgrade: () -> Unit,
    onLogout: () -> Unit,
    onHome: () -> Unit,
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onLive: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val profile = state.myProfile
    ConsumerScaffold("profile", onHome, onExplore, onLive, onFriends, onMe) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).background(ConsumerColors.Cloud),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item { VoiceCloudApprovedTopBar(title = "Profile") }
            item { StatusBlock(state, onLoad) }
            if (profile != null) {
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.fillMaxWidth().height(146.dp)) {
                                VoiceCloudRemoteMedia(
                                    url = profile.coverUrl,
                                    contentDescription = "${profile.displayName.ifBlank { profile.username }} cover",
                                    modifier = Modifier.fillMaxSize(),
                                    kind = VoiceCloudVisualKind.PROFILE,
                                    dark = true,
                                    contentScale = ContentScale.Crop,
                                )
                                Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, ConsumerColors.DeepNavy.copy(alpha = .58f)))))
                                VoiceCloudAvatar(
                                    url = profile.avatarUrl,
                                    label = profile.displayName.ifBlank { profile.username },
                                    size = 82.dp,
                                    online = profile.isOnline,
                                    verified = profile.isVerified,
                                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = 34.dp),
                                )
                            }
                            Spacer(Modifier.height(42.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                                if (profile.isVip) Surface(shape = RoundedCornerShape(50), color = ConsumerColors.VipGold.copy(alpha = .22f)) { Text("VIP", Modifier.padding(horizontal = 7.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.VioletDeep, fontWeight = FontWeight.Bold) }
                            }
                            if (profile.username.isNotBlank()) Text("@${profile.username}", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                            VoiceCloudApprovedMetricRow(
                                listOf(
                                    (profile.stats?.badgesCount ?: 0).toString() to "Rooms",
                                    (profile.stats?.followersCount ?: profile.followersCount).toString() to "Followers",
                                    (profile.stats?.followingCount ?: profile.followingCount).toString() to "Following",
                                ),
                                Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(profile.bio?.takeIf(String::isNotBlank) ?: "Share your voice and connect with your community.", style = MaterialTheme.typography.bodyMedium, color = ConsumerColors.Text)
                            if (!profile.statusMessage.isNullOrBlank()) Text(profile.statusMessage, style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                            Text("VoiceCloud ID  ·  ${profile.username.ifBlank { profile.id.take(8) }}", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VoiceCloudApprovedCard(Modifier.weight(1f), contentPadding = 10.dp, onClick = onEditProfile) {
                            VoiceCloudPictogram(VoiceCloudVisualKind.PROFILE, size = 34.dp)
                            Text("Edit Profile", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                            Text("Photo, bio & identity", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
                        }
                        VoiceCloudApprovedCard(Modifier.weight(1f), contentPadding = 10.dp, onClick = onEconomy) {
                            VoiceCloudPictogram(VoiceCloudVisualKind.WALLET, size = 34.dp)
                            Text("Wallet & Rewards", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                            Text("Balance, gifts & VIP", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
                        }
                    }
                }
                item { ProfileSettingsRow("Activity & Replays", "Rooms, history & visitors", onProfileTools) }
                item { ProfileSettingsRow("Settings & Privacy", "Notifications, privacy & security", onSettings) }
                item { ProfileSettingsRow("Safety Center", "Reports & blocked users", onSafety) }
                item { ProfileSettingsRow("Help & Support", "FAQ, legal & contact", onHelp) }
                item { ProfileSettingsRow("Contact Support", "Send a message to VoiceCloud", onContactSupport) }
                item { ProfileSettingsRow("About VoiceCloud", "App information & version", onAbout) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onFollowers, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Followers", style = MaterialTheme.typography.labelSmall) }
                        OutlinedButton(onClick = onFollowing, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Following", style = MaterialTheme.typography.labelSmall) }
                    }
                }
                if (canSwitchToCreator) item { VoiceCloudApprovedPrimaryButton("Switch to Creator", onClick = onSwitchToCreator) }
            }
            if (isGuest) item { VoiceCloudApprovedPrimaryButton("Upgrade Guest Account", onClick = onUpgrade) }
            item { OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Sign Out", style = MaterialTheme.typography.labelMedium) } }
        }
    }
}

@Composable
private fun ProfileSettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, ConsumerColors.Border.copy(alpha = .72f)), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VoiceCloudPictogram(voiceCloudVisualFor(title), size = 34.dp)
            Column(Modifier.weight(1f)) {
                Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ConsumerColors.Ink)
                Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
            }
            Text("›", style = MaterialTheme.typography.titleMedium, color = ConsumerColors.TextMuted)
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
            item { VoiceCloudApprovedSearchBar(search, { search = it }, placeholder = "Search ${mode}") }
            if (search.isNotBlank()) item { VoiceCloudApprovedPrimaryButton("Search", onClick = { onLoad(search) }) }
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
    onLive: () -> Unit,
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    var selected by rememberSaveable { mutableStateOf("Friends") }
    val tabs = listOf("Friends", "Requests", "Suggestions")
    ConsumerScaffold("friends", onHome, onExplore, onLive, onFriends, onMe) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).background(ConsumerColors.Cloud), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { VoiceCloudApprovedTopBar(title = "Friends & Connections") }
            item { StatusBlock(state, onLoad) }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEach { tab -> VoiceCloudApprovedChip(if (tab == "Requests" && state.pending.incoming.isNotEmpty()) "$tab (${state.pending.incoming.size})" else tab, selected == tab) { selected = tab } }
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
                    if (state.pending.incoming.isEmpty() && !state.loading) item { Text(voiceCloudTitleCase("No Incoming Requests."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    itemsIndexed(state.pending.incoming.distinctBy { it.id }, key = { i, r -> "friend-in:${r.id}:$i" }) { _, request ->
                        request.sender?.let { user ->
                            UserCard(user, { onProfile(user.username) }, "Accept", !state.mutationBusy) { onAccept(request.id) }
                            TextButton(enabled = !state.mutationBusy, onClick = { onReject(request.id) }) { Text(voiceCloudTitleCase("Decline")) }
                        }
                    }
                    item { SectionTitle("Sent") }
                    if (state.pending.outgoing.isEmpty() && !state.loading) item { Text(voiceCloudTitleCase("No Pending Sent Requests."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
