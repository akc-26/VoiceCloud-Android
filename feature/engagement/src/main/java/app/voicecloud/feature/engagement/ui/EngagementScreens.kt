package app.voicecloud.feature.engagement.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.theme.ConsumerBrushes
import app.voicecloud.core.designsystem.theme.ConsumerColors
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
            )
        },
    ) { innerPadding ->
        content(Modifier.fillMaxSize().padding(innerPadding))
    }
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
private fun RowScope.QuickAction(label: String, glyph: String, onClick: () -> Unit) {
    ElevatedCard(
        Modifier.weight(1f).clickable(onClick = onClick).heightIn(min = 76.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(glyph, fontSize = 20.sp, color = ConsumerColors.SapphireDeep)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
fun Ph04QuickActions(onCommunities: () -> Unit, onMessages: () -> Unit, onNotifications: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction("Communities", "◎", onCommunities)
        QuickAction("Messages", "✉", onMessages)
        QuickAction("Alerts", "◌", onNotifications)
    }
}

@Composable
fun CommunitiesScreen(
    state: EngagementUiState,
    onLoad: (String) -> Unit,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onEvents: () -> Unit,
    onMessages: () -> Unit,
    onNotifications: () -> Unit,
    onBack: () -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) { onLoad("") }
    SecondaryPageLayout(
        title = "Communities",
        subtitle = "Find groups built around the conversations you care about.",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(ConsumerBrushes.Hero).padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Find your people.", color = ConsumerColors.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Join conversations that continue beyond a single live room.", color = ConsumerColors.Text)
                    Button(onClick = onCreate) { Text("Create community", maxLines = 1, softWrap = false) }
                }
            }
        }
        item { Ph04QuickActions(onCommunities = { onLoad(search) }, onMessages = onMessages, onNotifications = onNotifications) }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search communities") },
                singleLine = true,
                trailingIcon = { TextButton(onClick = { onLoad(search) }) { Text("Search") } },
            )
        }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onEvents) { Text("Upcoming events →") } } }
        item { Feedback(state) { onLoad(search) } }
        if (state.communities.isEmpty() && !state.loading) item { Empty("No communities found", "Try another search or create a community.") }
        itemsIndexed(state.communities.distinctBy { it.id }, key = { i, c -> "community:${c.id}:$i" }) { _, community ->
            ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(community.handle.ifBlank { community.id }) }, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(community.name.ifBlank { "VoiceCloud community" }, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        if (community.isVerified) Text("✓", color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold)
                    }
                    Text("@${community.handle} · ${community.visibility.uppercase()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(community.description.ifBlank { "No description yet." }, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${community.memberCount} members · ${community.upcomingRoomsCount} upcoming · ${community.category}", style = MaterialTheme.typography.bodyMedium)
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
    SecondaryPageLayout(
        title = community?.name ?: "Community",
        subtitle = community?.let { "@${it.handle} · ${it.category}" },
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Feedback(state, onLoad) }
        if (community != null) {
            item {
                ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(community.description.ifBlank { "No community description has been added yet." })
                        Text("${community.memberCount} members · ${community.hostCount} hosts · ${community.upcomingRoomsCount} upcoming sessions", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (community.rules.isNotEmpty()) {
                            Text("Community rules", fontWeight = FontWeight.Bold)
                            community.rules.take(6).forEachIndexed { index, rule -> Text("${index + 1}. $rule") }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isGuest) {
                        Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Text("Upgrade account to join") }
                    } else if (membership?.member == true) {
                        if (canManage) Button(onClick = onManage, modifier = Modifier.fillMaxWidth()) { Text("Manage community · ${membership.role}") }
                        if (membership.role?.uppercase() != "OWNER") OutlinedButton(onClick = onLeave, enabled = !state.mutationBusy, modifier = Modifier.fillMaxWidth()) { Text("Leave community") }
                    } else {
                        if (isPrivate) OutlinedTextField(inviteCode, { inviteCode = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Private invitation code") }, singleLine = true)
                        Button(onClick = { onJoin(inviteCode.ifBlank { null }) }, enabled = !state.mutationBusy && (!isPrivate || inviteCode.isNotBlank()), modifier = Modifier.fillMaxWidth()) { Text(if (isPrivate) "Join with invitation code" else "Join community") }
                    }
                }
            }
            if (!isPrivate || membership?.member == true) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onMembers,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("Members", maxLines = 1, softWrap = false) }
                        OutlinedButton(
                            onClick = onEvents,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("Rooms & events", maxLines = 1, softWrap = false, fontSize = 12.sp) }
                    }
                }
                if (state.communityEvents.isNotEmpty()) item { Text("Upcoming", style = MaterialTheme.typography.titleLarge) }
                itemsIndexed(state.communityEvents.take(4).distinctBy { it.id }, key = { i, event -> "community-event:${event.id}:$i" }) { _, event -> EventCard(event, onClick = onEvents) }
            } else item { Empty("Private community", "Join with the current invitation code before browsing members and scheduled sessions.") }
        }
    }
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
        subtitle = "Set your community details, privacy, and member experience.",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Feedback(state) }
        item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true) }
        item { OutlinedTextField(handle, { handle = it }, Modifier.fillMaxWidth(), label = { Text("Handle") }, singleLine = true) }
        item { OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") }, minLines = 3) }
        item { OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Category") }, singleLine = true) }
        item { OutlinedTextField(rules, { rules = it }, Modifier.fillMaxWidth(), label = { Text("Rules · one per line") }, minLines = 3) }
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
        LazyColumn(pageModifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Feedback(state) }
        if (state.members.isEmpty() && !state.loading) item { Empty("No members", "No member identities are available.") }
        itemsIndexed(state.members.distinctBy { it.id.ifBlank { it.userId } }, key = { i, m -> "member:${m.id.ifBlank { m.userId }}:$i" }) { _, member ->
            val user = member.user
            val memberName = user?.displayName?.takeIf { it.isNotBlank() }
                ?: user?.username?.takeIf { it.isNotBlank() }
                ?: "VoiceCloud member"
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
            Text(event.title.ifBlank { "VoiceCloud session" }, style = MaterialTheme.typography.titleLarge)
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
        subtitle = "Scheduled VoiceCloud sessions and reminders.",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        LazyColumn(pageModifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
fun MessagesScreen(state: EngagementUiState, onLoad: (String) -> Unit, onOpen: (String) -> Unit, onDelete: (String) -> Unit, onBack: () -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) { onLoad("") }
    SecondaryPageLayout(
        title = "Messages",
        subtitle = "Your VoiceCloud conversations, all in one place.",
        onBack = onBack,
    ) { pageModifier ->
        LazyColumn(pageModifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), label = { Text("Search conversations") }, singleLine = true, trailingIcon = { TextButton(onClick = { onLoad(search) }) { Text("Search") } }) }
        item { Feedback(state) { onLoad(search) } }
        if (state.conversations.isEmpty() && !state.loading) item { Empty("No conversations yet", "When you start or join a conversation it will appear here.") }
        itemsIndexed(state.conversations.distinctBy { it.id }, key = { i, c -> "conversation:${c.id}:$i" }) { _, conversation ->
            ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().clickable { onOpen(conversation.id) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(ConsumerColors.SapphireSoft), contentAlignment = Alignment.Center) { Text((conversation.name ?: conversation.peer?.displayName ?: "V").firstOrNull()?.uppercaseChar()?.toString() ?: "V", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        val title = conversation.name ?: conversation.peer?.displayName ?: conversation.peer?.username ?: conversation.type.replaceFirstChar(Char::uppercase)
                        Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(conversation.lastMessage?.content ?: "No messages yet", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (conversation.unreadCount > 0) Badge { Text(conversation.unreadCount.toString()) }
                }
                TextButton(onClick = { onDelete(conversation.id) }, enabled = !state.mutationBusy, modifier = Modifier.align(Alignment.End)) { Text("Remove") }
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
                                if (!mine && !message.sender?.displayName.isNullOrBlank()) Text(message.sender?.displayName.orEmpty(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
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
        LazyColumn(pageModifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Feedback(state, onLoad) }
        if (!pushPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Enable push alerts", fontWeight = FontWeight.Bold); Text("Allow Android to show VoiceCloud messages, reminders and community notifications.", style = MaterialTheme.typography.bodyMedium) }
                    TextButton(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Enable") }
                }
            }
        }
        if (state.notifications.isEmpty() && !state.loading) item { Empty("You're all caught up", "New VoiceCloud notifications will appear here.") }
        itemsIndexed(state.notifications.distinctBy { it.id }, key = { i, n -> "notification:${n.id}:$i" }) { _, item ->
            ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else ConsumerColors.SapphireSoft), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().clickable {
                    if (!item.isRead) onRead(item.id)
                    onOpen(EngagementRouteResolver.fromNotification(item))
                }.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(item.title.ifBlank { "VoiceCloud notification" }, fontWeight = FontWeight.Bold)
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
