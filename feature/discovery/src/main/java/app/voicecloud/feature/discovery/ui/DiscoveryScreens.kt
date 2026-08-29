package app.voicecloud.feature.discovery.ui

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.component.VoiceCloudAnimatedWaveform
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
            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 7.dp)) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth().height(metrics.bottomBarHeight - 8.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.dp, ConsumerColors.Border.copy(alpha = .72f), RoundedCornerShape(26.dp)),
                    containerColor = ConsumerColors.Surface,
                    tonalElevation = 4.dp,
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
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = ConsumerColors.SapphireDeep,
                                indicatorColor = ConsumerColors.Sapphire,
                                unselectedIconColor = ConsumerColors.TextMuted,
                                unselectedTextColor = ConsumerColors.TextMuted,
                            ),
                            icon = {
                                Icon(
                                    painter = painterResource(if (isSelected) item.selectedIcon else item.outlineIcon),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(metrics.bottomIconSize),
                                )
                            },
                            label = { Text(voiceCloudTitleCase(item.label), maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
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
            Text(voiceCloudTitleCase(label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        }
    }
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
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(voiceCloudTitleCase(actionLabel)) }
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
    VoiceCloudGlossCard(Modifier.fillMaxWidth().clickable(onClick = onOpen), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(contentAlignment = Alignment.Center) {
                UserAvatar(user, 56)
                if (user.isOnline) Box(Modifier.align(Alignment.BottomEnd).size(13.dp).clip(CircleShape).background(CommonColors.Success).border(2.dp, Color.White, CircleShape))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.displayName.ifBlank { user.username }, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (user.isVerified) Text(voiceCloudTitleCase("  ✓"), color = ConsumerColors.Sapphire)
                }
                Text(voiceCloudTitleCase("@${user.username}"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                val descriptor = voiceCloudTitleCase(when {
                    user.role?.uppercase() == "CREATOR" -> "Creator"
                    user.isOnline -> "Online Now"
                    !user.bio.isNullOrBlank() -> user.bio
                    else -> "${VoiceCloudBrand.name} Member"
                })
                Text(descriptor, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = ConsumerColors.TextMuted)
                if (user.followersCount > 0) Text(voiceCloudTitleCase("${user.followersCount} followers"), style=MaterialTheme.typography.labelSmall,color=ConsumerColors.VioletDeep,fontWeight=FontWeight.Bold)
            }
            VoiceCloudPictogram(if(user.role?.uppercase()=="CREATOR") VoiceCloudVisualKind.CREATOR else VoiceCloudVisualKind.PROFILE, size=40.dp)
            if (actionLabel != null && onAction != null) OutlinedButton(enabled = actionEnabled, onClick = onAction) { Text(voiceCloudTitleCase(actionLabel)) }
        }
    }
}

@Composable
private fun RoomCard(room: VoiceCloudRoom, onOpen: () -> Unit) {
    ElevatedCard(
        Modifier.fillMaxWidth().clickable(onClick = onOpen).border(1.dp, ConsumerColors.Border.copy(alpha = .65f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = ConsumerColors.Surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().background(
                    androidx.compose.ui.graphics.Brush.linearGradient(listOf(ConsumerColors.LiveSurface, ConsumerColors.DeepNavy))
                ).padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (room.isLive) VoiceCloudLiveBadge()
                        else Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .12f)) {
                            Text(voiceCloudTitleCase(room.status.ifBlank { "ROOM" }), Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.weight(1f))
                        if (room.isLocked) Text(voiceCloudTitleCase("Locked"), color = ConsumerColors.VipGold, style = MaterialTheme.typography.labelLarge)
                    }
                    Text(room.title.ifBlank { "${VoiceCloudBrand.name} Room" }, style = MaterialTheme.typography.titleLarge, color = ConsumerColors.TextOnDark, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    VoiceCloudAnimatedWaveform(Modifier.fillMaxWidth().height(34.dp), color = ConsumerColors.Ice, active = room.isLive)
                }
            }
            Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                if (!room.description.isNullOrBlank()) Text(room.description, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(voiceCloudTitleCase("${room.listenerCount} Listening"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ConsumerColors.SapphireDeep)
                    Text(voiceCloudTitleCase("${room.speakerCount} Speakers"), style = MaterialTheme.typography.bodyMedium)
                    if (room.category.isNotBlank()) Text(room.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun EmptyBlock(title: String, body: String) {
    VoiceCloudEmptyVisual(title, body, Modifier.fillMaxWidth(), voiceCloudVisualFor(title))
}

@Composable
private fun CompactPeopleRow(users: List<VoiceCloudUser>, onProfile: (String) -> Unit) {
    if (users.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { Text(voiceCloudTitleCase("Nothing To Show Right Now."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(users.distinctBy { it.id }.take(4), key = { it.id.ifBlank { it.username } }) { user -> CompactUserCard(user, onProfile) }
    }
}

@Composable
private fun CompactUserCard(user: VoiceCloudUser, onProfile: (String) -> Unit) {
    VoiceCloudGlossCard(Modifier.width(158.dp).clickable { onProfile(user.username) }, contentPadding = 12.dp) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            UserAvatar(user, 58)
            if (user.isOnline) Box(Modifier.align(Alignment.BottomEnd).size(12.dp).clip(CircleShape).background(CommonColors.Success).border(2.dp, Color.White, CircleShape))
        }
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
            ElevatedCard(onClick = { onRoom(room.id) }, modifier = Modifier.width(230.dp), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(room.title.ifBlank { "VoiceCloud room" }, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(voiceCloudTitleCase("${room.listenerCount} Listening · ${room.category.ifBlank { "Live" }}"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
    VoiceCloudGlossCard(
        modifier = Modifier.width(218.dp).heightIn(min = 238.dp).clickable(onClick = onOpen),
        dark = dark,
        contentPadding = 0.dp,
    ) {
        Box(
            Modifier.fillMaxWidth().height(132.dp).background(
                if (dark) androidx.compose.ui.graphics.Brush.linearGradient(listOf(ConsumerColors.DeepNavy, ConsumerColors.LiveSurface))
                else androidx.compose.ui.graphics.Brush.linearGradient(listOf(ConsumerColors.Lavender, ConsumerColors.Surface))
            ),
            contentAlignment = Alignment.Center,
        ) {
            VoiceCloudPosterArtwork(
                kind = if (room.category.contains("music", true)) VoiceCloudVisualKind.AUDIO else if (room.category.contains("well", true) || room.category.contains("talk", true)) VoiceCloudVisualKind.EVENT else VoiceCloudVisualKind.LIVE,
                modifier = Modifier.fillMaxSize(),
                dark = dark,
            )
            Row(Modifier.align(Alignment.TopStart).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (room.isLive) VoiceCloudLiveBadge() else Surface(shape = RoundedCornerShape(50), color = ConsumerColors.VipGold.copy(alpha = .18f)) {
                    Text(voiceCloudTitleCase(room.status.ifBlank { "Room" }), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (dark) ConsumerColors.TextOnDark else ConsumerColors.VioletDeep)
                }
            }
            Text(
                voiceCloudTitleCase("${room.listenerCount} listening"),
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                color = if (dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(room.title.ifBlank { "VoiceCloud Room" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = if (dark) Color.White else ConsumerColors.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(voiceCloudTitleCase(room.category.ifBlank { "Live Audio" }), color = if (dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(minOf(room.speakerCount.coerceAtLeast(1), 3)) { index ->
                    Box(Modifier.size(24.dp).clip(CircleShape).background(if (index % 2 == 0) ConsumerColors.Sapphire else ConsumerColors.VipGold), contentAlignment = Alignment.Center) {
                        Text((index + 1).toString(), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                if (room.speakerCount > 3) Text(voiceCloudTitleCase("+${room.speakerCount - 3}"), color = if (dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun HomeRankedHost(user: VoiceCloudUser, rank: Int, onProfile: (String) -> Unit) {
    VoiceCloudGlossCard(
        modifier = Modifier.width(148.dp).clickable { onProfile(user.username) },
        contentPadding = 12.dp,
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(68.dp).clip(CircleShape).background(ConsumerBrushes.Primary).border(2.dp, ConsumerColors.VipGold, CircleShape), contentAlignment = Alignment.Center) {
                Text((user.displayName.ifBlank { user.username }.firstOrNull() ?: 'V').uppercaseChar().toString(), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
            Surface(Modifier.align(Alignment.TopStart), shape = CircleShape, color = if (rank == 1) ConsumerColors.VipGold else ConsumerColors.SurfaceSoft) {
                Text(rank.toString(), Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = ConsumerColors.Ink)
            }
            if (user.isOnline) Box(Modifier.align(Alignment.BottomEnd).size(14.dp).clip(CircleShape).background(CommonColors.Success).border(2.dp, Color.White, CircleShape))
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
    val featuredRoom = state.home.rooms.firstOrNull()
    val hostPool = (state.home.creators + state.home.people).distinctBy { it.id.ifBlank { it.username } }.take(8)
    val topics = state.home.rooms.map { it.category.trim() }.filter { it.isNotBlank() }.distinct().take(8)
    ConsumerScaffold("home", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).background(ConsumerColors.Cloud),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VoiceCloudBrandMark(size = 48.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(VoiceCloudBrand.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = ConsumerColors.SapphireDeep, modifier = Modifier.weight(1f))
                    if (!isGuest) Surface(shape = RoundedCornerShape(50), color = ConsumerColors.Lavender, modifier = Modifier.border(1.dp, ConsumerColors.VipGold.copy(alpha=.45f), RoundedCornerShape(50))) {
                        Text(voiceCloudTitleCase("♛ VIP"), Modifier.padding(horizontal = 11.dp, vertical = 6.dp), color = ConsumerColors.VioletDeep, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = onNotifications) { Icon(painterResource(R.drawable.vc_icon_bell), contentDescription = "Notifications", tint = ConsumerColors.Ink) }
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onSearch).border(1.dp, ConsumerColors.Border, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    color = ConsumerColors.Surface,
                    shadowElevation = 5.dp,
                ) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(painterResource(R.drawable.vc_icon_search), contentDescription = null, tint = ConsumerColors.SapphireDeep, modifier = Modifier.size(22.dp))
                        Text(voiceCloudTitleCase("Search rooms, hosts, topics…"), modifier = Modifier.weight(1f), color = ConsumerColors.TextMuted)
                        VoiceCloudPictogram(VoiceCloudVisualKind.SEARCH, size = 34.dp)
                    }
                }
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    item { FilterChip(selected = true, onClick = {}, label = { Text(voiceCloudTitleCase("For You")) }) }
                    item { FilterChip(selected = false, onClick = onExplore, label = { Text(voiceCloudTitleCase("Trending")) }) }
                    item { FilterChip(selected = false, onClick = onRooms, label = { Text(voiceCloudTitleCase("Rooms")) }) }
                    item { FilterChip(selected = false, onClick = onCreators, label = { Text(voiceCloudTitleCase("Creators")) }) }
                    item { FilterChip(selected = false, onClick = onCommunities, label = { Text(voiceCloudTitleCase("Communities")) }) }
                }
            }
            item { StatusBlock(state, onLoad) }
            item {
                val title = featuredRoom?.title?.ifBlank { null } ?: "Where Voices Come Together"
                val subtitle = featuredRoom?.description?.takeIf { !it.isNullOrBlank() } ?: "Discover live rooms, inspiring creators and communities built around conversations that matter."
                Box(Modifier.padding(horizontal = 20.dp)) {
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 220.dp).clip(RoundedCornerShape(30.dp))
                            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(ConsumerColors.DeepNavy, ConsumerColors.LiveSurface)))
                            .border(1.dp, ConsumerColors.VipGold.copy(alpha=.60f), RoundedCornerShape(30.dp)).padding(20.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(voiceCloudTitleCase("✦"), color = ConsumerColors.VipGold, fontWeight = FontWeight.ExtraBold)
                                    Text(voiceCloudTitleCase("Highlight"), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    if (featuredRoom?.isLive == true) VoiceCloudLiveBadge()
                                }
                                Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(subtitle ?: "", color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.bodyLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { if (featuredRoom != null) onRoom(featuredRoom.id) else onRooms() },
                                        colors = ButtonDefaults.buttonColors(containerColor = ConsumerColors.Surface, contentColor = ConsumerColors.Ink),
                                    ) { Text(voiceCloudTitleCase(if (featuredRoom != null) "Join Room" else "Explore Rooms"), fontWeight = FontWeight.Bold) }
                                    if (featuredRoom != null) Text(voiceCloudTitleCase("${featuredRoom.listenerCount} listening"), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            VoiceCloudAudioArtwork(Modifier.size(126.dp), dark = true)
                        }
                    }
                }
            }
            if (isGuest) item {
                Box(Modifier.padding(horizontal = 20.dp)) {
                    VoiceCloudGlossCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            VoiceCloudPictogram(VoiceCloudVisualKind.PROFILE, size = 46.dp)
                            Column(Modifier.weight(1f)) {
                                Text(voiceCloudTitleCase("Keep Your Connections"), fontWeight = FontWeight.ExtraBold)
                                Text(voiceCloudTitleCase("Create an account to save rooms, follow hosts and keep your profile."), color = ConsumerColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = onUpgrade) { Text(voiceCloudTitleCase("Upgrade")) }
                        }
                    }
                }
            }
            item { Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Live Rooms", "See All", onRooms) } }
            if (state.home.rooms.isEmpty() && !state.loading) item { Box(Modifier.padding(horizontal = 20.dp)) { VoiceCloudEmptyVisual("No Live Rooms", "Check again soon for conversations going live.", kind = VoiceCloudVisualKind.LIVE) } }
            if (state.home.rooms.isNotEmpty()) item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.home.rooms.distinctBy { it.id }.take(10), key = { it.id }) { room -> HomeRoomVisualCard(room) { onRoom(room.id) } }
                }
            }
            item { Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Top Hosts", "See All", onCreators) } }
            if (hostPool.isNotEmpty()) item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(hostPool, key = { index, user -> "host:${user.id}:$index" }) { index, user -> HomeRankedHost(user, index + 1, onProfile) }
                }
            }
            item {
                val metrics = VoiceCloudPageMetrics.current()
                Column(Modifier.fillMaxWidth().padding(horizontal = metrics.horizontalPadding), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    SectionTitle("Quick Access")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        HomeShortcut("Communities", R.drawable.vc_icon_community, onCommunities)
                        HomeShortcut("Messages", R.drawable.vc_icon_message, onMessages)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        HomeShortcut("Explore", R.drawable.vc_nav_explore, onExplore)
                        HomeShortcut("Host Studio", R.drawable.vc_icon_host, onHostStudio)
                    }
                }
            }
            item { Box(Modifier.padding(horizontal = 20.dp)) { SectionTitle("Trending Topics", "Explore", onExplore) } }
            item {
                val displayTopics = if (topics.isEmpty()) listOf("Live Audio", "Communities", "Creators") else topics
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(displayTopics, key = { it }) { topic ->
                        VoiceCloudGlossCard(Modifier.width(170.dp).clickable(onClick = onExplore), contentPadding = 12.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                VoiceCloudPictogram(VoiceCloudVisualKind.AUDIO, size = 38.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(voiceCloudTitleCase("# $topic"), fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(voiceCloudTitleCase("Explore conversations"), color = ConsumerColors.TextMuted, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
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
            item { VoiceCloudPageHero("Explore VoiceCloud", "Trends, categories, communities and voices beyond your live feed.", VoiceCloudVisualKind.DISCOVER, badge = "Discover more") }
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
                        items(categories, key = { it }) { category -> AssistChip(onClick = onRooms, label = { Text(voiceCloudTitleCase(category)) }) }
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
            item { VoiceCloudPageHero("Live Rooms", "Browse conversations happening now and enter an immersive audio room in one tap.", VoiceCloudVisualKind.LIVE, badge = "Live now") }
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
            item { VoiceCloudPageHero(if (creatorsOnly) "Creators" else "People", if (creatorsOnly) "Discover creators and hosts with conversations worth following." else "Find people, voices and connections across VoiceCloud.", if (creatorsOnly) VoiceCloudVisualKind.CREATOR else VoiceCloudVisualKind.AUDIENCE, badge = if (creatorsOnly) "Featured voices" else "Real connections") }
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
            item { VoiceCloudPageHero("Search & Discover", if (hasQuery) "Results for “${state.search.query}”" else "Discover active people, creators, rooms and communities.", VoiceCloudVisualKind.SEARCH, badge = "Find your next conversation") }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(voiceCloudTitleCase("Search VoiceCloud")) },
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
                    tabs.forEach { tab -> FilterChip(selected = selectedTab == tab, onClick = { selectedTab = tab }, label = { Text(voiceCloudTitleCase(tab), maxLines = 1) }) }
                }
            }
            item { StatusBlock(state) }
            if (communitiesLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }

            if (selectedTab == "All") {
                val allEmpty = people.isEmpty() && creators.isEmpty() && rooms.isEmpty() && communities.isEmpty()
                if (hasQuery && allEmpty && !state.loading && !communitiesLoading) {
                    item {
                        VoiceCloudEmptyVisual("No Results Found", "No matches for “${state.search.query}”. Try another name, room or topic.", Modifier.fillMaxWidth(), VoiceCloudVisualKind.SEARCH)
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
                            Text(voiceCloudTitleCase(if (hasQuery) "No ${selectedTab.lowercase()} found for “${state.search.query}”." else "No ${selectedTab.lowercase()} available right now."), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text(voiceCloudTitleCase("@${profile.username}"), color = ConsumerColors.SapphireDeep)
                        if (profile.isVerified) Text(voiceCloudTitleCase("Verified ${VoiceCloudBrand.name} Profile"), color = ConsumerColors.SapphireDeep)
                        Spacer(Modifier.height(12.dp))
                        Text(profile.bio ?: profile.statusMessage ?: "${VoiceCloudBrand.name} member", color = ConsumerColors.Text, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(14.dp))
                        if (isSelf) Button(onClick = onMyProfile) { Text(voiceCloudTitleCase("My Profile")) }
                        else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(enabled = !state.mutationBusy, onClick = onFollow) { Text(voiceCloudTitleCase(if (profile.relationship?.isFollowing == true) "Following" else "Follow")) }
                                OutlinedButton(enabled = !state.mutationBusy, onClick = { onMessage(profile.id) }) { Text(voiceCloudTitleCase("Message")) }
                            }
                            TextButton(onClick = { onReport(profile.id, profile.displayName.ifBlank { "@${profile.username}" }) }) { Text(voiceCloudTitleCase("Report Profile")) }
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
                    Text(voiceCloudTitleCase("About"), style = MaterialTheme.typography.titleLarge)
                    if (!profile.country.isNullOrBlank()) Text(voiceCloudTitleCase("Country · ${profile.country}"))
                    if (!profile.preferredLanguage.isNullOrBlank()) Text(voiceCloudTitleCase("Language · ${profile.preferredLanguage}"))
                    val interests = profile.interests.orEmpty().ifEmpty { profile.customTags.orEmpty() }
                    if (interests.isNotEmpty()) Text(voiceCloudTitleCase("Interests · ${interests.joinToString(" · ")}"))
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
    onFriends: () -> Unit,
    onMe: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val profile = state.myProfile
    ConsumerScaffold("profile", onHome, onExplore, onSearch, onFriends, onMe) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { VoiceCloudPageHero("My Profile", "Your identity, activity, rewards, safety and creator access in one premium profile hub.", VoiceCloudVisualKind.PROFILE, badge = "Your VoiceCloud") }
            item { StatusBlock(state, onLoad) }
            if (profile != null) {
                item {
                    VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 20.dp) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            UserAvatar(profile.asUser(), 78)
                            Spacer(Modifier.height(10.dp))
                            Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (profile.username.isNotBlank()) Text(voiceCloudTitleCase("@${profile.username}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            profile.bio?.takeIf { it.isNotBlank() }?.let {
                                Spacer(Modifier.height(8.dp))
                                Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                ProfileMetricAction("${profile.stats?.followersCount ?: profile.followersCount}", "Followers", onFollowers, Modifier.weight(1f))
                                ProfileMetricAction("${profile.stats?.followingCount ?: profile.followingCount}", "Following", onFollowing, Modifier.weight(1f))
                            }
                        }
                    }
                }

                item { SectionTitle("Account") }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileHubTile("Edit Profile", "Photo & Bio", onEditProfile, Modifier.weight(1f))
                        ProfileHubTile("Settings", "Privacy & Security", onSettings, Modifier.weight(1f))
                    }
                }

                item { SectionTitle("Your VoiceCloud") }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileHubTile("Activity & Replays", "Rooms & Visitors", onProfileTools, Modifier.weight(1f))
                        ProfileHubTile("Wallet & Rewards", "VIP, Gifts & Store", onEconomy, Modifier.weight(1f))
                    }
                }

                item { SectionTitle("Safety & Support") }
                item { ProfileHubTile("Safety Center", "Reports & Blocked Users", onSafety, Modifier.fillMaxWidth()) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileHubTile("Help & Legal", "FAQ & Policies", onHelp, Modifier.weight(1f))
                        ProfileHubTile("Contact Support", "Send A Message", onContactSupport, Modifier.weight(1f))
                    }
                }
                item { ProfileHubTile("About VoiceCloud", "App & Version", onAbout, Modifier.fillMaxWidth()) }

                if (canSwitchToCreator) {
                    item {
                        Button(onClick = onSwitchToCreator, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                            Text(voiceCloudTitleCase("Switch To Creator"))
                        }
                    }
                }
            }
            if (isGuest) {
                item { Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Upgrade Guest Account")) } }
            }
            item { OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Sign Out")) } }
        }
    }
}

@Composable
private fun ProfileMetricAction(value: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    VoiceCloudGlossCard(modifier.clickable(onClick = onClick), contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VoiceCloudPictogram(VoiceCloudVisualKind.AUDIENCE, size = 38.dp)
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(voiceCloudTitleCase(label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileHubTile(title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    VoiceCloudGlossCard(modifier.heightIn(min = 104.dp).clickable(onClick = onClick), contentPadding = 13.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VoiceCloudPictogram(voiceCloudVisualFor(title), size = 44.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
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
            item { VoiceCloudPageHero(if (mode == "followers") "Followers" else "Following", if (mode == "followers") "People who choose to stay connected with your VoiceCloud journey." else "Voices and people you follow across VoiceCloud.", VoiceCloudVisualKind.AUDIENCE, badge = "Your connections") }
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(voiceCloudTitleCase("Search ${mode}")) },
                    trailingIcon = { TextButton(onClick = { onLoad(search) }) { Text(voiceCloudTitleCase("Search")) } },
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
            item { VoiceCloudPageHero("Friends & Connections", "Manage friends, incoming requests and suggestions in a visual community hub.", VoiceCloudVisualKind.AUDIENCE, badge = "Real connections") }
            item { StatusBlock(state, onLoad) }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEach { tab -> FilterChip(selected == tab, { selected = tab }, label = { Text(voiceCloudTitleCase(if (tab == "Requests" && state.pending.incoming.isNotEmpty()) "$tab (${state.pending.incoming.size})" else tab)) }) }
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
