package app.voicecloud.feature.engagement.ui

import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudEmptyVisual
import app.voicecloud.core.designsystem.component.VoiceCloudGlossCard
import app.voicecloud.core.designsystem.component.VoiceCloudPageHero
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.voiceCloudVisualFor
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
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
        containerColor = ConsumerColors.Cloud,
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
    VoiceCloudToastEffect(state.error, state.notice)
    if (state.loading) Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun Empty(title: String, body: String) {
    VoiceCloudEmptyVisual(title, body, Modifier.fillMaxWidth(), voiceCloudVisualFor(title))
}

@Composable
private fun RowScope.QuickAction(label: String, iconRes: Int, onClick: () -> Unit) {
    VoiceCloudGlossCard(
        modifier = Modifier.weight(1f).heightIn(min = 72.dp).clickable(onClick = onClick),
        contentPadding = 11.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VoiceCloudPictogram(voiceCloudVisualFor(label), size = 38.dp)
            Text(voiceCloudTitleCase(label), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, softWrap = false)
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
                                Text(voiceCloudTitleCase("Build Your Circle"), color = ConsumerColors.Ink, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                                Text(voiceCloudTitleCase("Create A Place For Your Audience."), color = ConsumerColors.Text, style = MaterialTheme.typography.bodyMedium)
                            }
                            FilledIconButton(onClick = onCreate, modifier = Modifier.size(52.dp)) {
                                Text(voiceCloudTitleCase("+"), fontSize = 27.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            item { Ph04QuickActions(onCommunities = { onLoad("") }, onMessages = onMessages, onNotifications = onNotifications) }
            item { Feedback(state) { onLoad("") } }
            if (state.communities.isEmpty() && !state.loading) item { Empty("No communities", "Create the first one.") }
            itemsIndexed(state.communities.distinctBy { it.id }, key = { i, c -> "community:${c.id}:$i" }) { _, community ->
                VoiceCloudGlossCard(Modifier.fillMaxWidth().clickable { onOpen(community.handle.ifBlank { community.id }) }, contentPadding = 15.dp) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.COMMUNITY, size = 50.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(community.name.ifBlank { "Community" }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (community.isVerified) Text(voiceCloudTitleCase("✓"), color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold)
                            }
                            Text(voiceCloudTitleCase("@${community.handle} · ${community.memberCount} Members"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (community.description.isNotBlank()) Text(community.description, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(voiceCloudTitleCase("›"), fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            item { VoiceCloudPageHero(community?.name ?: "Community", community?.description?.takeIf { it.isNotBlank() } ?: "A place for people, hosts and conversations to grow together.", VoiceCloudVisualKind.COMMUNITY, badge = community?.category ?: "Community") }
            if (community != null) {
                item {
                    VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 20.dp) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VoiceCloudPictogram(VoiceCloudVisualKind.COMMUNITY, size = 66.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(community.name, style = MaterialTheme.typography.titleLarge)
                                    Text(voiceCloudTitleCase("${community.category} · ${community.visibility.lowercase().replaceFirstChar(Char::uppercase)}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Upgrade To Join")) }
                        } else if (membership?.member == true) {
                            if (canManage) Button(onClick = onManage, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Manage Community")) }
                            if (membership.role?.uppercase() != "OWNER") OutlinedButton(onClick = onLeave, enabled = !state.mutationBusy, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Leave Community")) }
                        } else {
                            if (isPrivate) OutlinedTextField(inviteCode, { inviteCode = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(voiceCloudTitleCase("Invitation Code")) }, singleLine = true, shape = RoundedCornerShape(18.dp))
                            Button(onClick = { onJoin(inviteCode.ifBlank { null }) }, enabled = !state.mutationBusy && (!isPrivate || inviteCode.isNotBlank()), modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase(if (isPrivate) "Join with code" else "Join community")) }
                        }
                    }
                }
                if (!isPrivate || membership?.member == true) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onMembers, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Members"), maxLines = 1, softWrap = false) }
                            OutlinedButton(onClick = onEvents, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Events"), maxLines = 1, softWrap = false) }
                        }
                    }
                    if (community.rules.isNotEmpty()) item {
                        VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(voiceCloudTitleCase("Community Rules"), fontWeight = FontWeight.Bold)
                                community.rules.take(4).forEachIndexed { index, rule -> Text(voiceCloudTitleCase("${index + 1}. $rule"), style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    }
                    if (state.communityEvents.isNotEmpty()) item { Text(voiceCloudTitleCase("Upcoming"), style = MaterialTheme.typography.titleLarge) }
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
        Text(voiceCloudTitleCase(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        item { VoiceCloudPageHero(if (existing == null) "Create Community" else "Manage Community", if (existing == null) "Create a polished home for your audience, rules and upcoming conversations." else "Refine community details, membership and access while keeping the same community identity.", VoiceCloudVisualKind.COMMUNITY, badge = if (existing == null) "Build your circle" else "Community tools") }
        if (existing == null) item {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(ConsumerBrushes.Hero).padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .8f)) {
                        Icon(painterResource(R.drawable.vc_icon_community), contentDescription = null, tint = ConsumerColors.SapphireDeep, modifier = Modifier.padding(12.dp).size(28.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(voiceCloudTitleCase("Start A Community"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                        Text(voiceCloudTitleCase("Name It. Shape It. Grow It."), style = MaterialTheme.typography.bodyMedium, color = ConsumerColors.Text)
                    }
                }
            }
        }
        item { Text(voiceCloudTitleCase("Basics"), style = MaterialTheme.typography.titleMedium, color = ConsumerColors.SapphireDeep) }
        item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Community Name")) }, singleLine = true, shape = RoundedCornerShape(18.dp)) }
        item { OutlinedTextField(handle, { handle = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Handle")) }, singleLine = true, shape = RoundedCornerShape(18.dp)) }
        item { OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Description")) }, minLines = 3, shape = RoundedCornerShape(18.dp)) }
        item { OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Category")) }, singleLine = true, shape = RoundedCornerShape(18.dp)) }
        item { OutlinedTextField(rules, { rules = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Rules · One Per Line")) }, minLines = 3, shape = RoundedCornerShape(18.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(voiceCloudTitleCase("Private Community"), fontWeight = FontWeight.Bold); Text(voiceCloudTitleCase("Requires The Secure Invitation Code To Join."), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Switch(checked = privateCommunity, onCheckedChange = { privateCommunity = it })
            }
        }
        if (existing != null && privateCommunity && onRotateInvite != null) item {
            OutlinedButton(onClick = onRotateInvite, modifier = Modifier.fillMaxWidth(), enabled = !state.mutationBusy) { Text(voiceCloudTitleCase("Rotate Invitation Code")) }
        }
        if (!state.inviteCode.isNullOrBlank()) item {
            Card(colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) { Text(voiceCloudTitleCase("Current One-Time Shareable Invitation Code"), fontWeight = FontWeight.Bold); Text(state.inviteCode, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
        if (existing != null && onMembers != null) item { OutlinedButton(onClick = onMembers, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Manage Members And Roles")) } }
        if (existing != null && onDelete != null) item {
            OutlinedButton(onClick = onDelete, enabled = !state.mutationBusy, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Delete Community"), color = MaterialTheme.colorScheme.error) }
        }
        item {
            Button(
                onClick = { onSubmit(CommunityInput(name = name, handle = handle, description = description, category = category, rules = rules.lines(), visibility = if (privateCommunity) "PRIVATE" else "PUBLIC")) },
                enabled = name.isNotBlank() && handle.isNotBlank() && !state.mutationBusy,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(voiceCloudTitleCase(if (existing == null) "Create community" else "Save changes")) }
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
        item { VoiceCloudPageHero("Community Members", "See the people who make this community active and manage roles when your permissions allow it.", VoiceCloudVisualKind.AUDIENCE, badge = "People & roles") }
        if (state.members.isEmpty() && !state.loading) item { Empty("No members", "No member identities are available.") }
        itemsIndexed(state.members.distinctBy { it.id.ifBlank { it.userId } }, key = { i, m -> "member:${m.id.ifBlank { m.userId }}:$i" }) { _, member ->
            val user = member.user
            val memberName = user?.displayName?.takeIf { it.isNotBlank() }
                ?: user?.username?.takeIf { it.isNotBlank() }
                ?: "${VoiceCloudBrand.name} member"
            VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    VoiceCloudPictogram(VoiceCloudVisualKind.PROFILE, size = 44.dp)
                    Column(Modifier.weight(1f)) {
                        Text(memberName, fontWeight = FontWeight.Bold)
                        Text(voiceCloudTitleCase("@${user?.username ?: member.userId.take(8)} · ${member.role}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                    if (canManage && member.userId != viewerId && member.role.uppercase() != "OWNER") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val assignableRoles = if (viewerRole?.uppercase() == "OWNER") listOf("OWNER", "ADMIN", "MODERATOR", "MEMBER") else listOf("MODERATOR", "MEMBER")
                            assignableRoles.forEach { role ->
                                TextButton(onClick = { onRole(member.userId, role) }, enabled = !state.mutationBusy) {
                                    Text(voiceCloudTitleCase(role.lowercase().replaceFirstChar(Char::uppercase)))
                                }
                            }
                        }
                        TextButton(onClick = { onRemove(member.userId) }, enabled = !state.mutationBusy) { Text(voiceCloudTitleCase("Remove Member"), color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: ScheduledEvent, onClick: () -> Unit) {
    VoiceCloudGlossCard(Modifier.fillMaxWidth().clickable(onClick = onClick), contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VoiceCloudPictogram(VoiceCloudVisualKind.EVENT, size = 58.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = ConsumerColors.SapphireSoft) { Text(voiceCloudTitleCase(event.status), Modifier.padding(horizontal=8.dp,vertical=4.dp), color=ConsumerColors.SapphireDeep, fontWeight=FontWeight.Bold, style=MaterialTheme.typography.labelSmall) }
                    if(event.isPremium) Surface(shape=RoundedCornerShape(50),color=ConsumerColors.Lavender){Text(voiceCloudTitleCase("VIP"),Modifier.padding(horizontal=8.dp,vertical=4.dp),color=ConsumerColors.VioletDeep,fontWeight=FontWeight.ExtraBold,style=MaterialTheme.typography.labelSmall)}
                }
                Text(event.title.ifBlank { "${VoiceCloudBrand.name} Session" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines=2, overflow=TextOverflow.Ellipsis)
                Text(event.scheduledStartTime.replace('T', ' ').take(16) + " · ${event.timeZone}", color = MaterialTheme.colorScheme.onSurfaceVariant, style=MaterialTheme.typography.bodyMedium)
            }
        }
        if (!event.description.isNullOrBlank()) Text(event.description, maxLines = 2, overflow = TextOverflow.Ellipsis, color=ConsumerColors.TextMuted)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Surface(shape=RoundedCornerShape(12.dp),color=ConsumerColors.SurfaceSoft){Text(voiceCloudTitleCase("${event.rsvpCount} reminders"),Modifier.padding(horizontal=9.dp,vertical=5.dp),style=MaterialTheme.typography.labelSmall)}
            Surface(shape=RoundedCornerShape(12.dp),color=ConsumerColors.SurfaceSoft){Text(voiceCloudTitleCase("${event.durationMinutes} min"),Modifier.padding(horizontal=9.dp,vertical=5.dp),style=MaterialTheme.typography.labelSmall)}
            if(event.category.isNotBlank()) Surface(shape=RoundedCornerShape(12.dp),color=ConsumerColors.Lavender.copy(alpha=.7f)){Text(voiceCloudTitleCase(event.category),Modifier.padding(horizontal=9.dp,vertical=5.dp),style=MaterialTheme.typography.labelSmall,color=ConsumerColors.VioletDeep)}
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
        item { VoiceCloudPageHero("Upcoming Events", "Plan ahead for scheduled VoiceCloud conversations, community sessions and reminders.", VoiceCloudVisualKind.EVENT, badge = "Live audio calendar") }
        item { OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Search Scheduled Sessions")) }, singleLine = true, trailingIcon = { TextButton(onClick = { onLoad(search) }) { Text(voiceCloudTitleCase("Search")) } }) }
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
        item { VoiceCloudPageHero(event?.title ?: "Scheduled Event", event?.description?.takeIf { it.isNotBlank() } ?: "Review the session, host context and reminder details before it begins.", VoiceCloudVisualKind.EVENT, badge = event?.status ?: "Upcoming") }
        if (event != null) {
            item { EventCard(event) {} }
            item { Button(onClick = onReminder, enabled = !state.mutationBusy, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Remind Me")) } }
            event.club?.let { club -> item { OutlinedButton(onClick = { onCommunity(club.handle.ifBlank { club.id }) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("View ${club.name}")) } } }
            if (event.isPremium) item { Text(voiceCloudTitleCase("Premium Session · ${event.ticketPriceAmount ?: 0} ${event.currency}"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
            title = { Text(voiceCloudTitleCase(if (ids.size == 1) "Delete conversation?" else "Delete ${ids.size} conversations?")) },
            text = { Text(voiceCloudTitleCase("This Removes The Selected Conversation${if (ids.size == 1) "" else "s"} From Your Inbox.")) },
            confirmButton = {
                TextButton(onClick = {
                    if (ids.size == 1) onDelete(ids.first()) else onDeleteMany(ids)
                    selected = selected - ids
                    if (selected.isEmpty()) selectionMode = false
                    pendingDelete = null
                }) { Text(voiceCloudTitleCase("Delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(voiceCloudTitleCase("Cancel")) } },
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
                    placeholder = { Text(voiceCloudTitleCase("Search Conversations")) },
                    singleLine = true,
                    leadingIcon = { Icon(painterResource(R.drawable.vc_icon_search), contentDescription = null) },
                    trailingIcon = { TextButton(onClick = { onLoad(search.trim()) }) { Text(voiceCloudTitleCase("Go")) } },
                    shape = RoundedCornerShape(20.dp),
                )
            }
            if (!selectionMode) item { VoiceCloudPageHero("Your Conversations", "Direct messages and community connections, presented in one polished inbox.", VoiceCloudVisualKind.MESSAGE, badge = "Private & connected") }
            item { Feedback(state) { onLoad(search) } }
            if (state.conversations.isEmpty() && !state.loading) item { Empty("No conversations", "Start a new conversation.") }
            itemsIndexed(state.conversations.distinctBy { it.id }, key = { i, c -> "conversation:${c.id}:$i" }) { _, conversation ->
                val checked = conversation.id in selected
                VoiceCloudGlossCard(
                    modifier = Modifier.fillMaxWidth().border(1.dp, if (checked) ConsumerColors.Sapphire else ConsumerColors.Border.copy(alpha = .62f), RoundedCornerShape(20.dp)),
                    contentPadding = 10.dp,
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                            VoiceCloudPictogram(VoiceCloudVisualKind.MESSAGE, size = 46.dp)
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
        VoiceCloudToastEffect(state.error, state.notice)
        Box(Modifier.weight(1f)) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { VoiceCloudPageHero(conversation?.name ?: conversation?.peer?.displayName ?: "Conversation", "Private, polished messaging that stays focused on the people you connect with.", VoiceCloudVisualKind.MESSAGE, badge = "Direct conversation") }
                if (state.loading && state.messages.isEmpty()) item { Feedback(state) }
                if (state.messages.isEmpty() && !state.loading) item { Empty("Start the conversation", "Send a message to start the conversation.") }
                itemsIndexed(state.messages.distinctBy { it.id }, key = { i, m -> "message:${m.id}:$i" }) { _, message ->
                    val mine = message.senderId == viewerId
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                        Surface(
                            color = if (mine) ConsumerColors.Sapphire else ConsumerColors.Surface,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.widthIn(max = 300.dp).border(
                                1.dp,
                                if (mine) ConsumerColors.Sapphire else ConsumerColors.Border.copy(alpha = .7f),
                                RoundedCornerShape(20.dp),
                            ),
                            shadowElevation = if (mine) 2.dp else 1.dp,
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                val senderName = message.sender?.displayName.orEmpty()
                                if (!mine && senderName.isNotBlank()) Text(senderName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = ConsumerColors.SapphireDeep)
                                Text(message.content ?: "[${message.type}]", color = if (mine) Color.White else MaterialTheme.colorScheme.onSurface)
                                Text(
                                    voiceCloudTitleCase(message.createdAt.replace('T', ' ').take(16) + if (message.isEdited) " · edited" else ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (mine) Color.White.copy(alpha = .74f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                draft, { draft = it }, Modifier.weight(1f),
                label = { Text(voiceCloudTitleCase("Message")) }, maxLines = 4,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    val value = draft.trim()
                    if (value.isNotEmpty() && !state.mutationBusy) { onSend(value); draft = "" }
                }),
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { val value = draft.trim(); if (value.isNotEmpty()) { onSend(value); draft = "" } }, enabled = draft.isNotBlank() && !state.mutationBusy) { Text(voiceCloudTitleCase("Send")) }
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
        item { VoiceCloudPageHero("Notifications", "Stay on top of rooms, messages, community activity and important VoiceCloud updates.", VoiceCloudVisualKind.NOTIFICATION, badge = "Activity center") }
        item { Feedback(state, onLoad) }
        if (!pushPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(voiceCloudTitleCase("Enable Push Alerts"), fontWeight = FontWeight.Bold); Text(voiceCloudTitleCase("Allow Android To Show ${VoiceCloudBrand.name} Messages, Reminders And Community Notifications."), style = MaterialTheme.typography.bodyMedium) }
                    TextButton(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text(voiceCloudTitleCase("Enable")) }
                }
            }
        }
        if (state.notifications.isEmpty() && !state.loading) item { Empty("You're all caught up", "New ${VoiceCloudBrand.name} notifications will appear here.") }
        itemsIndexed(state.notifications.distinctBy { it.id }, key = { i, n -> "notification:${n.id}:$i" }) { _, item ->
            VoiceCloudGlossCard(
                modifier = Modifier.fillMaxWidth().border(1.dp, ConsumerColors.Border.copy(alpha = if (item.isRead) .55f else .9f), RoundedCornerShape(18.dp)),
                contentPadding = 0.dp,
            ) {
                Column(Modifier.fillMaxWidth().clickable {
                    if (!item.isRead) onRead(item.id)
                    onOpen(EngagementRouteResolver.fromNotification(item))
                }.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(item.title.ifBlank { "${VoiceCloudBrand.name} notification" }, fontWeight = FontWeight.Bold)
                    Text(item.message)
                    Text(voiceCloudTitleCase(item.type.replace('_', ' ') + " · " + item.createdAt.replace('T', ' ').take(16)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (!item.isRead) TextButton(onClick = { onRead(item.id) }) { Text(voiceCloudTitleCase("Mark Read")) }
                        TextButton(onClick = { onDelete(item.id) }) { Text(voiceCloudTitleCase("Delete"), color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}
}
