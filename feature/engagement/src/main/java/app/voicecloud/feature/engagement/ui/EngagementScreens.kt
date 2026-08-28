package app.voicecloud.feature.engagement.ui

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudTopBarIconAction
import app.voicecloud.core.designsystem.theme.ConsumerBrushes
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.VoiceCloudPageMetrics
import app.voicecloud.feature.engagement.data.EngagementRouteResolver
import app.voicecloud.feature.engagement.model.*
import kotlinx.coroutines.delay

@Composable
private fun SecondaryPageLayout(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VoiceCloudPageTopBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
                actionLabel = actionLabel,
                actionEnabled = actionEnabled,
                onAction = onAction,
                actions = actions,
            )
        },
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
private fun Feedback(state: EngagementUiState, retry: (() -> Unit)? = null) {
    when {
        state.loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.error != null -> Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
                if (retry != null) OutlinedButton(onClick = retry) { Text("Try again") }
            }
        }
        state.notice != null -> Text(state.notice, color = ConsumerColors.SapphireDeep, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Empty(title: String, body: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RowScope.QuickAction(label: String, iconRes: Int, onClick: () -> Unit) {
    ElevatedCard(
        Modifier.weight(1f).clickable(onClick = onClick).heightIn(min = 58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Surface(shape = RoundedCornerShape(11.dp), color = ConsumerColors.SapphireSoft) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = ConsumerColors.SapphireDeep,
                    modifier = Modifier.padding(6.dp).size(18.dp),
                )
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
fun Ph04QuickActions(onCommunities: () -> Unit, onMessages: () -> Unit, onNotifications: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickAction("Communities", R.drawable.vc_icon_community, onCommunities)
        QuickAction("Messages", R.drawable.vc_icon_message, onMessages)
        QuickAction("Alerts", R.drawable.vc_icon_bell, onNotifications)
    }
}

@Composable
fun CommunitiesScreen(
    state: EngagementUiState,
    onLoad: (String) -> Unit,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onSearch: () -> Unit,
    onEvents: () -> Unit,
    onMessages: () -> Unit,
    onNotifications: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad("") }
    val metrics = VoiceCloudPageMetrics.current()
    SecondaryPageLayout(
        title = "Communities",
        onBack = onBack,
        actions = {
            VoiceCloudTopBarIconAction(R.drawable.vc_icon_search, "Search communities", onClick = onSearch)
            Spacer(Modifier.width(6.dp))
            VoiceCloudTopBarIconAction(R.drawable.vc_icon_calendar, "Upcoming events", onClick = onEvents)
        },
    ) { pageModifier ->
        LazyColumn(
            pageModifier,
            contentPadding = PaddingValues(horizontal = metrics.horizontalPadding, vertical = metrics.contentTopSpacing),
            verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing),
        ) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Box(Modifier.fillMaxWidth().background(ConsumerBrushes.Hero).padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text("Build your circle", color = ConsumerColors.Ink, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                                Text("Create a place for your audience.", color = ConsumerColors.Text, style = MaterialTheme.typography.bodyMedium)
                            }
                            FilledIconButton(onClick = onCreate, modifier = Modifier.size(52.dp)) {
                                Text("+", fontSize = 27.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            item { Ph04QuickActions(onCommunities = { onLoad("") }, onMessages = onMessages, onNotifications = onNotifications) }
            item { Feedback(state) { onLoad("") } }
            if (state.communities.isEmpty() && !state.loading) item { Empty("No communities", "Create the first one.") }
            itemsIndexed(state.communities.distinctBy { it.id }, key = { i, c -> "community:${c.id}:$i" }) { _, community ->
                ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(community.handle.ifBlank { community.id }) }, shape = RoundedCornerShape(22.dp)) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(16.dp), color = ConsumerColors.SapphireSoft) {
                            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                Text(community.name.firstOrNull()?.uppercaseChar()?.toString() ?: "C", color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(community.name.ifBlank { "Community" }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (community.isVerified) Text("✓", color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold)
                            }
                            Text("@${community.handle} · ${community.memberCount} members", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (community.description.isNotBlank()) Text(community.description, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityDetailScreen(
    state: EngagementUiState,
    id: String,
    isGuest: Boolean,
    onLoad: () -> Unit,
    onJoin: (String?) -> Unit,
    onLeave: () -> Unit,
    onManage: () -> Unit,
    onMembers: () -> Unit,
    onEvents: () -> Unit,
    onUpgrade: () -> Unit,
    onBack: () -> Unit,
) {
    var inviteCode by rememberSaveable(id) { mutableStateOf("") }
    LaunchedEffect(id) { onLoad() }
    val community = state.community
    val membership = state.membership
    val isPrivate = community?.visibility?.uppercase() == "PRIVATE"
    val canManage = membership?.role?.uppercase() in setOf("OWNER", "ADMIN")
    val metrics = VoiceCloudPageMetrics.current()
    SecondaryPageLayout(
        title = community?.name ?: "Community",
        subtitle = community?.handle?.takeIf { it.isNotBlank() }?.let { "@$it" },
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(
            pageModifier,
            contentPadding = PaddingValues(horizontal = metrics.horizontalPadding, vertical = metrics.contentTopSpacing),
            verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing),
        ) {
            item { Feedback(state, onLoad) }
            if (community != null) {
                item {
                    ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(20.dp), color = ConsumerColors.SapphireSoft) {
                                    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                                        Text(community.name.firstOrNull()?.uppercaseChar()?.toString() ?: "C", fontSize = 25.sp, color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(community.name, style = MaterialTheme.typography.titleLarge)
                                    Text("${community.category} · ${community.visibility.lowercase().replaceFirstChar(Char::uppercase)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (community.description.isNotBlank()) Text(community.description, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                CommunityStat(community.memberCount.toString(), "Members")
                                CommunityStat(community.hostCount.toString(), "Hosts")
                                CommunityStat(community.upcomingRoomsCount.toString(), "Upcoming")
                            }
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        if (isGuest) {
                            Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Text("Upgrade to join") }
                        } else if (membership?.member == true) {
                            if (canManage) Button(onClick = onManage, modifier = Modifier.fillMaxWidth()) { Text("Manage community") }
                            if (membership.role?.uppercase() != "OWNER") OutlinedButton(onClick = onLeave, enabled = !state.mutationBusy, modifier = Modifier.fillMaxWidth()) { Text("Leave community") }
                        } else {
                            if (isPrivate) OutlinedTextField(inviteCode, { inviteCode = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Invitation code") }, singleLine = true, shape = RoundedCornerShape(18.dp))
                            Button(onClick = { onJoin(inviteCode.ifBlank { null }) }, enabled = !state.mutationBusy && (!isPrivate || inviteCode.isNotBlank()), modifier = Modifier.fillMaxWidth()) { Text(if (isPrivate) "Join with code" else "Join community") }
                        }
                    }
                }
                if (!isPrivate || membership?.member == true) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onMembers, modifier = Modifier.weight(1f)) { Text("Members", maxLines = 1, softWrap = false) }
                            OutlinedButton(onClick = onEvents, modifier = Modifier.weight(1f)) { Text("Events", maxLines = 1, softWrap = false) }
                        }
                    }
                    if (community.rules.isNotEmpty()) item {
                        ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Community rules", fontWeight = FontWeight.Bold)
                                community.rules.take(4).forEachIndexed { index, rule -> Text("${index + 1}. $rule", style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    }
                    if (state.communityEvents.isNotEmpty()) item { Text("Upcoming", style = MaterialTheme.typography.titleLarge) }
                    itemsIndexed(state.communityEvents.take(3).distinctBy { it.id }, key = { i, event -> "community-event:${event.id}:$i" }) { _, event -> EventCard(event, onClick = onEvents) }
                } else item { Empty("Private community", "Join to continue.") }
            }
        }
    }
}

@Composable
private fun CommunityStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = ConsumerColors.SapphireDeep)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CommunityEditorScreen(
    state: EngagementUiState,
    existing: Community? = null,
    onSubmit: (CommunityInput) -> Unit,
    onRotateInvite: (() -> Unit)? = null,
    onMembers: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit,
) {
    var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var handle by rememberSaveable(existing?.id) { mutableStateOf(existing?.handle.orEmpty()) }
    var description by rememberSaveable(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var category by rememberSaveable(existing?.id) { mutableStateOf(existing?.category ?: "General") }
    var rules by rememberSaveable(existing?.id) { mutableStateOf(existing?.rules?.joinToString("\n").orEmpty()) }
    var privateCommunity by rememberSaveable(existing?.id) { mutableStateOf(existing?.visibility?.uppercase() == "PRIVATE") }
    SecondaryPageLayout(
        title = if (existing == null) "Create Community" else "Manage Community",
        subtitle = if (existing == null) "Create your space" else null,
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = adaptivePagePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Feedback(state) }
        if (existing == null) item {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(ConsumerBrushes.Hero).padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .8f)) {
                        Icon(painterResource(R.drawable.vc_icon_community), contentDescription = null, tint = ConsumerColors.SapphireDeep, modifier = Modifier.padding(12.dp).size(28.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Start a community", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                        Text("Name it. Shape it. Grow it.", style = MaterialTheme.typography.bodyMedium, color = ConsumerColors.Text)
                    }
                }
            }
        }
        item { Text("Basics", style = MaterialTheme.typography.titleMedium, color = ConsumerColors.SapphireDeep) }
        item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Community name") }, singleLine = true, shape = RoundedCornerShape(18.dp)) }
        item { OutlinedTextField(handle, { handle = it }, Modifier.fillMaxWidth(), label = { Text("Handle") }, singleLine = true, shape = RoundedCornerShape(18.dp)) }
        item { OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") }, minLines = 3, shape = RoundedCornerShape(18.dp)) }
        item { OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Category") }, singleLine = true, shape = RoundedCornerShape(18.dp)) }
        item { OutlinedTextField(rules, { rules = it }, Modifier.fillMaxWidth(), label = { Text("Rules · one per line") }, minLines = 3, shape = RoundedCornerShape(18.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Private community", fontWeight = FontWeight.Bold); Text("Requires the secure invitation code to join.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Switch(checked = privateCommunity, onCheckedChange = { privateCommunity = it })
            }
        }
        if (existing != null && privateCommunity && onRotateInvite != null) item {
            OutlinedButton(onClick = onRotateInvite, modifier = Modifier.fillMaxWidth(), enabled = !state.mutationBusy) { Text("Rotate invitation code") }
        }
        if (!state.inviteCode.isNullOrBlank()) item {
            Card(colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) { Text("Current one-time shareable invitation code", fontWeight = FontWeight.Bold); Text(state.inviteCode, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
        if (existing != null && onMembers != null) item { OutlinedButton(onClick = onMembers, modifier = Modifier.fillMaxWidth()) { Text("Manage members and roles") } }
        if (existing != null && onDelete != null) item {
            OutlinedButton(onClick = onDelete, enabled = !state.mutationBusy, modifier = Modifier.fillMaxWidth()) { Text("Delete community", color = MaterialTheme.colorScheme.error) }
        }
        item {
            Button(
                onClick = { onSubmit(CommunityInput(name = name, handle = handle, description = description, category = category, rules = rules.lines(), visibility = if (privateCommunity) "PRIVATE" else "PUBLIC")) },
                enabled = name.isNotBlank() && handle.isNotBlank() && !state.mutationBusy,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (existing == null) "Create community" else "Save changes") }
        }
    }
}
}

@Composable
fun CommunityMembersScreen(
    state: EngagementUiState,
    canManage: Boolean,
    viewerRole: String?,
    viewerId: String?,
    onRole: (String, String) -> Unit,
    onRemove: (String) -> Unit,
    onBack: () -> Unit,
) {
    SecondaryPageLayout(
        title = "Community Members",
        subtitle = state.community?.name,
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = adaptivePagePadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Feedback(state) }
        if (state.members.isEmpty() && !state.loading) item { Empty("No members", "No member identities are available.") }
        itemsIndexed(state.members.distinctBy { it.id.ifBlank { it.userId } }, key = { i, m -> "member:${m.id.ifBlank { m.userId }}:$i" }) { _, member ->
            val user = member.user
            val memberName = user?.displayName?.takeIf { it.isNotBlank() }
                ?: user?.username?.takeIf { it.isNotBlank() }
                ?: "${VoiceCloudBrand.name} member"
            ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(memberName, fontWeight = FontWeight.Bold)
                    Text("@${user?.username ?: member.userId.take(8)} · ${member.role}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (canManage && member.userId != viewerId && member.role.uppercase() != "OWNER") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val assignableRoles = if (viewerRole?.uppercase() == "OWNER") listOf("OWNER", "ADMIN", "MODERATOR", "MEMBER") else listOf("MODERATOR", "MEMBER")
                            assignableRoles.forEach { role ->
                                TextButton(onClick = { onRole(member.userId, role) }, enabled = !state.mutationBusy) {
                                    Text(role.lowercase().replaceFirstChar(Char::uppercase))
                                }
                            }
                        }
                        TextButton(onClick = { onRemove(member.userId) }, enabled = !state.mutationBusy) { Text("Remove member", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun EventCard(event: ScheduledEvent, onClick: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(event.status, color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text(event.title.ifBlank { "${VoiceCloudBrand.name} session" }, style = MaterialTheme.typography.titleLarge)
            Text(event.scheduledStartTime.replace('T', ' ').take(16) + " · ${event.timeZone}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!event.description.isNullOrBlank()) Text(event.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${event.rsvpCount} reminders · ${event.durationMinutes} min · ${event.category}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EventsScreen(state: EngagementUiState, onLoad: (String) -> Unit, onOpen: (String) -> Unit, onBack: () -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) { onLoad("") }
    SecondaryPageLayout(
        title = "Upcoming Events",
        subtitle = "Scheduled ${VoiceCloudBrand.name} sessions and reminders.",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = adaptivePagePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), label = { Text("Search scheduled sessions") }, singleLine = true, trailingIcon = { TextButton(onClick = { onLoad(search) }) { Text("Search") } }) }
        item { Feedback(state) { onLoad(search) } }
        if (state.events.isEmpty() && !state.loading) item { Empty("No scheduled sessions", "Upcoming sessions will appear here.") }
        itemsIndexed(state.events.distinctBy { it.id }, key = { i, event -> "event:${event.id}:$i" }) { _, event -> EventCard(event) { onOpen(event.id) } }
        }
    }
}

@Composable
fun EventDetailScreen(state: EngagementUiState, onLoad: () -> Unit, onReminder: () -> Unit, onCommunity: (String) -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    val event = state.event
    SecondaryPageLayout(
        title = event?.title ?: "Event",
        subtitle = "Scheduled session details",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = adaptivePagePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Feedback(state, onLoad) }
        if (event != null) {
            item { EventCard(event) {} }
            item { Button(onClick = onReminder, enabled = !state.mutationBusy, modifier = Modifier.fillMaxWidth()) { Text("Remind me") } }
            event.club?.let { club -> item { OutlinedButton(onClick = { onCommunity(club.handle.ifBlank { club.id }) }, modifier = Modifier.fillMaxWidth()) { Text("View ${club.name}") } } }
            if (event.isPremium) item { Text("Premium session · ${event.ticketPriceAmount ?: 0} ${event.currency}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
}

@Composable
fun MessagesScreen(
    state: EngagementUiState,
    onLoad: (String) -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteMany: (Set<String>) -> Unit,
    onBack: () -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var pendingDelete by remember { mutableStateOf<Set<String>?>(null) }
    val metrics = VoiceCloudPageMetrics.current()
    LaunchedEffect(Unit) { onLoad("") }

    pendingDelete?.let { ids ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(if (ids.size == 1) "Delete conversation?" else "Delete ${ids.size} conversations?") },
            text = { Text("This removes the selected conversation${if (ids.size == 1) "" else "s"} from your inbox.") },
            confirmButton = {
                TextButton(onClick = {
                    if (ids.size == 1) onDelete(ids.first()) else onDeleteMany(ids)
                    selected = selected - ids
                    if (selected.isEmpty()) selectionMode = false
                    pendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }

    SecondaryPageLayout(
        title = if (selectionMode) "${selected.size} selected" else "Messages",
        onBack = { if (selectionMode) { selectionMode = false; selected = emptySet() } else onBack() },
        actions = {
            if (selectionMode) {
                VoiceCloudTopBarIconAction(
                    iconRes = R.drawable.vc_icon_delete,
                    contentDescription = "Delete selected",
                    onClick = { if (selected.isNotEmpty()) pendingDelete = selected },
                    enabled = selected.isNotEmpty() && !state.mutationBusy,
                    tint = MaterialTheme.colorScheme.error,
                )
            } else {
                VoiceCloudTopBarIconAction(R.drawable.vc_icon_search, "Search conversations", onClick = { searchVisible = !searchVisible })
                Spacer(Modifier.width(6.dp))
                VoiceCloudTopBarIconAction(R.drawable.vc_icon_check_all, "Select conversations", onClick = { selectionMode = true })
            }
        },
    ) { pageModifier ->
        LazyColumn(
            pageModifier,
            contentPadding = PaddingValues(horizontal = metrics.horizontalPadding, vertical = metrics.contentTopSpacing),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (searchVisible) item {
                OutlinedTextField(
                    search,
                    { search = it },
                    Modifier.fillMaxWidth(),
                    placeholder = { Text("Search conversations") },
                    singleLine = true,
                    leadingIcon = { Icon(painterResource(R.drawable.vc_icon_search), contentDescription = null) },
                    trailingIcon = { TextButton(onClick = { onLoad(search.trim()) }) { Text("Go") } },
                    shape = RoundedCornerShape(20.dp),
                )
            }
            item { Feedback(state) { onLoad(search) } }
            if (state.conversations.isEmpty() && !state.loading) item { Empty("No conversations", "Start a new conversation.") }
            itemsIndexed(state.conversations.distinctBy { it.id }, key = { i, c -> "conversation:${c.id}:$i" }) { _, conversation ->
                val checked = conversation.id in selected
                ElevatedCard(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = if (checked) ConsumerColors.SapphireSoft else MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (selectionMode) {
                            Checkbox(checked = checked, onCheckedChange = { isChecked -> selected = if (isChecked) selected + conversation.id else selected - conversation.id })
                        }
                        Row(
                            Modifier.weight(1f).clickable {
                                if (selectionMode) selected = if (checked) selected - conversation.id else selected + conversation.id
                                else onOpen(conversation.id)
                            }.padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(46.dp).clip(CircleShape).background(ConsumerColors.SapphireSoft), contentAlignment = Alignment.Center) {
                                Text((conversation.name ?: conversation.peer?.displayName ?: "V").firstOrNull()?.uppercaseChar()?.toString() ?: "V", fontWeight = FontWeight.Bold, color = ConsumerColors.SapphireDeep)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                val title = conversation.name ?: conversation.peer?.displayName ?: conversation.peer?.username ?: conversation.type.replaceFirstChar(Char::uppercase)
                                Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(conversation.lastMessage?.content ?: "No messages yet", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (conversation.unreadCount > 0) Badge { Text(conversation.unreadCount.toString()) }
                        }
                        if (!selectionMode) {
                            IconButton(onClick = { pendingDelete = setOf(conversation.id) }, enabled = !state.mutationBusy) {
                                Icon(painterResource(R.drawable.vc_icon_delete), contentDescription = "Delete conversation", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationScreen(state: EngagementUiState, viewerId: String?, onLoad: () -> Unit, onRefresh: () -> Unit, onSend: (String) -> Unit, onBack: () -> Unit) {
    var draft by rememberSaveable(state.conversation?.id) { mutableStateOf("") }
    LaunchedEffect(Unit) { onLoad() }
    LaunchedEffect(state.conversation?.id) {
        while (true) { delay(5_000); onRefresh() }
    }
    val conversation = state.conversation
    SecondaryPageLayout(
        title = conversation?.name ?: conversation?.peer?.displayName ?: "Conversation",
        subtitle = conversation?.type?.let { "${it.replaceFirstChar(Char::uppercase)} conversation" },
        onBack = onBack,
    ) { pageModifier ->
        Column(pageModifier) {
        Box(Modifier.weight(1f)) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.loading && state.messages.isEmpty()) item { Feedback(state) }
                if (state.messages.isEmpty() && !state.loading) item { Empty("Start the conversation", "Send a message to start the conversation.") }
                itemsIndexed(state.messages.distinctBy { it.id }, key = { i, m -> "message:${m.id}:$i" }) { _, message ->
                    val mine = message.senderId == viewerId
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                        Surface(color = if (mine) ConsumerColors.SapphireSoft else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp), modifier = Modifier.widthIn(max = 300.dp)) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                val senderName = message.sender?.displayName.orEmpty()
                                if (!mine && senderName.isNotBlank()) Text(senderName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                Text(message.content ?: "[${message.type}]")
                                Text(message.createdAt.replace('T', ' ').take(16) + if (message.isEdited) " · edited" else "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        if (state.error != null) Text(state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(draft, { draft = it }, Modifier.weight(1f), label = { Text("Message") }, maxLines = 4)
            Spacer(Modifier.width(8.dp))
            Button(onClick = { val value = draft.trim(); if (value.isNotEmpty()) { onSend(value); draft = "" } }, enabled = draft.isNotBlank() && !state.mutationBusy) { Text("Send") }
        }
    }
}
}

@Composable
fun NotificationsScreen(
    state: EngagementUiState,
    onLoad: () -> Unit,
    onOpen: (String) -> Unit,
    onRead: (String) -> Unit,
    onReadAll: () -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var pushPermissionGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pushPermissionGranted = granted
    }

    LaunchedEffect(Unit) { onLoad() }
    SecondaryPageLayout(
        title = "Notifications",
        subtitle = if (state.unreadNotifications == 1) "1 unread notification" else "${state.unreadNotifications} unread notifications",
        onBack = onBack,
        actionLabel = "Read all",
        actionEnabled = !state.mutationBusy,
        onAction = onReadAll,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = adaptivePagePadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Feedback(state, onLoad) }
        if (!pushPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Enable push alerts", fontWeight = FontWeight.Bold); Text("Allow Android to show ${VoiceCloudBrand.name} messages, reminders and community notifications.", style = MaterialTheme.typography.bodyMedium) }
                    TextButton(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Enable") }
                }
            }
        }
        if (state.notifications.isEmpty() && !state.loading) item { Empty("You're all caught up", "New ${VoiceCloudBrand.name} notifications will appear here.") }
        itemsIndexed(state.notifications.distinctBy { it.id }, key = { i, n -> "notification:${n.id}:$i" }) { _, item ->
            ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else ConsumerColors.SapphireSoft), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().clickable {
                    if (!item.isRead) onRead(item.id)
                    onOpen(EngagementRouteResolver.fromNotification(item))
                }.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(item.title.ifBlank { "${VoiceCloudBrand.name} notification" }, fontWeight = FontWeight.Bold)
                    Text(item.message)
                    Text(item.type.replace('_', ' ') + " · " + item.createdAt.replace('T', ' ').take(16), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (!item.isRead) TextButton(onClick = { onRead(item.id) }) { Text("Mark read") }
                        TextButton(onClick = { onDelete(item.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}
}
