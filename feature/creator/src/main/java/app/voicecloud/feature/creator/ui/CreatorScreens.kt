package app.voicecloud.feature.creator.ui

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.VoiceCloudRemoteMedia
import app.voicecloud.core.designsystem.component.VoiceCloudAvatar
import app.voicecloud.core.designsystem.component.VoiceCloudEmptyVisual
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.voiceCloudVisualFor
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedCard
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedPrimaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSecondaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSectionTitle
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedStatCard
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedTopBar
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.CreatorColors
import app.voicecloud.feature.creator.model.*
import app.voicecloud.feature.hosting.model.HostingUiState
import app.voicecloud.feature.hosting.model.HostRoom
import app.voicecloud.feature.hosting.model.ScheduledHostRoom
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
private fun CreatorPortalFrame(
    title: String,
    selected: CreatorPortalSection,
    onDashboard: () -> Unit,
    onLive: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onAnalytics: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(
            containerColor = CreatorColors.LightBackground,
            topBar = {
                if (selected != CreatorPortalSection.DASHBOARD) {
                    Surface(color = CreatorColors.LightSurface, shadowElevation = 1.dp) {
                        Row(
                            Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                voiceCloudTitleCase(title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CreatorColors.Text,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(color = CreatorColors.LightSurface, shadowElevation = 5.dp) {
                    Row(
                        Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        CreatorBoardNavItem("Home", VoiceCloudVisualKind.DISCOVER, selected == CreatorPortalSection.DASHBOARD, onDashboard)
                        CreatorBoardNavItem("Studio", VoiceCloudVisualKind.LIVE, selected == CreatorPortalSection.LIVE, onLive)
                        Surface(onClick = onLive, shape = CircleShape, color = CreatorColors.Primary, shadowElevation = 5.dp, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.vc_nav_live), contentDescription = "Live", tint = Color.White, modifier = Modifier.size(24.dp)) }
                        }
                        CreatorBoardNavItem("Analytics", VoiceCloudVisualKind.ANALYTICS, false, onAnalytics)
                        CreatorBoardNavItem("Profile", VoiceCloudVisualKind.PROFILE, selected == CreatorPortalSection.PROFILE, onProfile)
                    }
                }
            },
            content = content,
        )
    }
}

@Composable
private fun RowScope.CreatorBoardNavItem(
    label: String,
    kind: VoiceCloudVisualKind,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.weight(1f).fillMaxHeight()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            VoiceCloudPictogram(kind, size = 24.dp, dark = false, accent = if (selected) CreatorColors.Primary else CreatorColors.TextMuted)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) CreatorColors.PrimaryDark else CreatorColors.TextMuted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun CreatorStatus(state: CreatorUiState, onRetry: (() -> Unit)? = null) {
    VoiceCloudToastEffect(state.error, state.notice)
    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
fun CreatorDashboardScreen(
    state: CreatorUiState,
    creatorName: String,
    onLoad: () -> Unit,
    onDashboard: () -> Unit,
    onLive: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onAudience: () -> Unit,
    onMessages: () -> Unit,
    onAnalytics: () -> Unit,
    onWallet: () -> Unit,
    onEarnings: () -> Unit,
    onGifts: () -> Unit,
    onPayouts: () -> Unit,
    onNotifications: () -> Unit,
    onVerification: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPortalFrame("Dashboard", CreatorPortalSection.DASHBOARD, onDashboard, onLive, onProfile, onSettings, onHelp, onAnalytics, onSwitchToVoiceCloud) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VoiceCloudAvatar(state.profile?.avatarUrl, state.profile?.displayName?.ifBlank { creatorName } ?: creatorName, size = 46.dp, verified = true)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Welcome back, ${creatorName.ifBlank { "Creator" }}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CreatorColors.Text)
                        Text("Manage your audience, rooms and creator performance from one place.", style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted)
                    }
                }
            }
            item { CreatorStatus(state, onLoad) }
            state.dashboard?.metrics?.takeIf { it.isNotEmpty() }?.let { metrics ->
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), dark = true, contentPadding = 0.dp) {
                        Box(Modifier.fillMaxWidth().height(112.dp)) {
                            VoiceCloudRemoteMedia(
                                url = null,
                                contentDescription = "Creator dashboard",
                                modifier = Modifier.fillMaxSize(),
                                kind = VoiceCloudVisualKind.CREATOR,
                                dark = true,
                                fallbackDrawable = app.voicecloud.core.designsystem.R.drawable.vc_ref_creator_mic,
                            )
                            Box(
                                Modifier.fillMaxSize().background(
                                    Brush.horizontalGradient(
                                        listOf(ConsumerColors.LiveSurface.copy(alpha = .96f), ConsumerColors.LiveSurface.copy(alpha = .72f), Color.Transparent)
                                    )
                                )
                            )
                            Column(
                                Modifier.align(Alignment.CenterStart).padding(14.dp).fillMaxWidth(.68f),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Text("Creator overview", color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelSmall)
                                Text(metrics.first().value, color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text(voiceCloudTitleCase(metrics.first().label), color = ConsumerColors.VipGold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                items(metrics.drop(1).take(4).chunked(2), key = { row -> row.joinToString(":") { it.key } }) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { metric -> VoiceCloudApprovedStatCard(metric.value, metric.label, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            if (state.dashboard != null && state.dashboard.metrics.isEmpty() && !state.loading) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VoiceCloudApprovedStatCard("—", "Rooms", Modifier.weight(1f))
                        VoiceCloudApprovedStatCard("—", "Audience", Modifier.weight(1f))
                        VoiceCloudApprovedStatCard("—", "Earnings", Modifier.weight(1f))
                    }
                }
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            VoiceCloudPictogram(VoiceCloudVisualKind.ANALYTICS, size = 38.dp)
                            Column(Modifier.weight(1f)) {
                                Text("Creator insights are ready for real activity", fontWeight = FontWeight.Bold, color = CreatorColors.Text)
                                Text("Metrics will populate from VoiceCloud as your rooms, audience and earnings activity grows.", style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted)
                            }
                        }
                    }
                }
            }
            item { VoiceCloudApprovedSectionTitle("Quick actions") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CreatorQuickAction("Live Studio", VoiceCloudVisualKind.LIVE, Modifier.weight(1f), onLive)
                    CreatorQuickAction("Audience", VoiceCloudVisualKind.AUDIENCE, Modifier.weight(1f), onAudience)
                    CreatorQuickAction("Analytics", VoiceCloudVisualKind.ANALYTICS, Modifier.weight(1f), onAnalytics)
                    CreatorQuickAction("Messages", VoiceCloudVisualKind.MESSAGE, Modifier.weight(1f), onMessages)
                }
            }
            item { VoiceCloudApprovedSectionTitle("Creator tools") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CreatorToolRow("Wallet", "Balances and transactions", VoiceCloudVisualKind.WALLET, onWallet)
                    CreatorToolRow("Earnings", "Revenue and settled activity", VoiceCloudVisualKind.ANALYTICS, onEarnings)
                    CreatorToolRow("Gifts", "Community gift history", VoiceCloudVisualKind.GIFT, onGifts)
                    CreatorToolRow("Payouts", "Withdraw and review requests", VoiceCloudVisualKind.PAYOUT, onPayouts)
                    CreatorToolRow("Notifications", "Creator updates and reminders", VoiceCloudVisualKind.NOTIFICATION, onNotifications)
                    CreatorToolRow("Host verification", "Verification and host access", VoiceCloudVisualKind.SECURITY, onVerification)
                }
            }
        }
    }
}

@Composable
private fun CreatorQuickAction(title: String, kind: VoiceCloudVisualKind, modifier: Modifier = Modifier, onClick: () -> Unit) {
    VoiceCloudApprovedCard(modifier.height(82.dp), contentPadding = 8.dp, onClick = onClick) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            VoiceCloudPictogram(kind, size = 30.dp)
            Spacer(Modifier.height(4.dp))
            Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun CreatorToolRow(title: String, subtitle: String, kind: VoiceCloudVisualKind, onClick: () -> Unit) {
    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp, onClick = onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            VoiceCloudPictogram(kind, size = 34.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.labelSmall, color = CreatorColors.TextMuted)
            }
            Text("›", color = CreatorColors.PrimaryDark, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun CreatorLiveStudioScreen(
    state: HostingUiState,
    onLoad: () -> Unit,
    onCreateRoom: () -> Unit,
    onScheduleRoom: () -> Unit,
    onRoom: (String) -> Unit,
    onSchedule: (String) -> Unit,
    onStartScheduled: (ScheduledHostRoom) -> Unit,
    onDashboard: () -> Unit,
    onLive: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onAnalytics: () -> Unit,
    onVerification: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val hostApproved = state.hostProfile?.status.equals("APPROVED", true)
    CreatorPortalFrame("Live Studio", CreatorPortalSection.LIVE, onDashboard, onLive, onProfile, onSettings, onHelp, onAnalytics, onSwitchToVoiceCloud) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { VoiceCloudApprovedSectionTitle("Content planner") }
            item { VoiceCloudToastEffect(state.error, state.notice) }
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (state.eligibilityChecked && !hostApproved && !state.loading) {
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            VoiceCloudPictogram(VoiceCloudVisualKind.SECURITY, size = 44.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Host access required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CreatorColors.Text)
                                Text(state.eligibility?.reasons?.firstOrNull() ?: "Creator tools are available, but creating or broadcasting rooms requires an approved Host profile.", style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        VoiceCloudApprovedPrimaryButton("Review Host Access", onClick = onVerification)
                    }
                }
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth()) {
                        Text("Creator Studio remains available", fontWeight = FontWeight.Bold, color = CreatorColors.Text)
                        Text("You can continue using audience, analytics, messages, wallet and profile tools while Host approval is pending.", style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted)
                    }
                }
            } else if (hostApproved) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VoiceCloudApprovedPrimaryButton("Go Live", modifier = Modifier.weight(1f), enabled = !state.loading, onClick = onCreateRoom)
                        VoiceCloudApprovedSecondaryButton("Schedule", modifier = Modifier.weight(1f), enabled = !state.loading, onClick = onScheduleRoom)
                    }
                }
                item { Text(voiceCloudTitleCase("My Rooms"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                if (state.rooms.isEmpty() && !state.loading) {
                    item { CreatorEmptyState("No Rooms Yet", "Create A Room When You’re Ready To Go Live.") }
                }
                items(state.rooms.distinctBy(HostRoom::id), key = { "creator-room:${it.id}" }) { room ->
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp, onClick = { onRoom(room.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))) {
                                VoiceCloudRemoteMedia(url = null, contentDescription = room.title, modifier = Modifier.fillMaxSize(), kind = VoiceCloudVisualKind.LIVE, dark = true, fallbackDrawable = app.voicecloud.core.designsystem.R.drawable.vc_ref_creator_mic)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(room.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(voiceCloudTitleCase(room.status.replaceFirstChar { it.titlecase() }), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(voiceCloudTitleCase("Host room controls and live audience tools"), color = CreatorColors.TextMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            Text(voiceCloudTitleCase("Open"), color = CreatorColors.PrimaryDark, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                item { Text(voiceCloudTitleCase("Upcoming"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                if (state.schedules.isEmpty() && !state.loading) {
                    item { CreatorEmptyState("Nothing Scheduled", "Plan Your Next Room From Here.") }
                }
                items(state.schedules.distinctBy(ScheduledHostRoom::id), key = { "creator-schedule:${it.id}" }) { schedule ->
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp, onClick = { onSchedule(schedule.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))) {
                                VoiceCloudRemoteMedia(url = null, contentDescription = schedule.title, modifier = Modifier.fillMaxSize(), kind = VoiceCloudVisualKind.SCHEDULE, dark = false, fallbackDrawable = app.voicecloud.core.designsystem.R.drawable.vc_ref_live_forest)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(schedule.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatCreatorSchedule(schedule.scheduledStartTime, schedule.timeZone), color = CreatorColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Button(onClick = { onStartScheduled(schedule) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Start Room")) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatorEmptyState(title: String, body: String) {
    VoiceCloudEmptyVisual(title, body, Modifier.fillMaxWidth(), voiceCloudVisualFor(title))
}

private fun formatCreatorSchedule(raw: String, timeZone: String?): String = runCatching {
    val parsed = OffsetDateTime.parse(raw)
    val zone = runCatching { ZoneId.of(timeZone?.takeIf(String::isNotBlank) ?: ZoneId.systemDefault().id) }.getOrDefault(ZoneId.systemDefault())
    parsed.atZoneSameInstant(zone).format(DateTimeFormatter.ofPattern("EEE, dd MMM · hh:mm a"))
}.getOrDefault(raw)

@Composable
fun CreatorProfileScreen(
    state: CreatorUiState,
    onLoad: () -> Unit,
    onSave: (String?, String?, List<String>) -> Unit,
    onDashboard: () -> Unit,
    onLive: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onAnalytics: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val profile = state.profile
    var bio by remember(profile?.id) { mutableStateOf(profile?.bio.orEmpty()) }
    var country by remember(profile?.id) { mutableStateOf(profile?.country.orEmpty()) }
    var interests by remember(profile?.id) { mutableStateOf(profile?.interests?.joinToString(", ").orEmpty()) }
    var editing by rememberSaveable(profile?.id) { mutableStateOf(false) }
    CreatorPortalFrame("Profile", CreatorPortalSection.PROFILE, onDashboard, onLive, onProfile, onSettings, onHelp, onAnalytics, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { CreatorStatus(state, onLoad) }
            if (profile != null) {
                item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 0.dp, dark = true) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.fillMaxWidth().height(116.dp).background(CreatorColors.PrimaryDark)) {
                                VoiceCloudRemoteMedia(
                                    url = null,
                                    contentDescription = profile.displayName.ifBlank { profile.username },
                                    modifier = Modifier.fillMaxSize(),
                                    kind = VoiceCloudVisualKind.CREATOR,
                                    dark = true,
                                    fallbackDrawable = app.voicecloud.core.designsystem.R.drawable.vc_ref_creator_mic,
                                )
                                VoiceCloudAvatar(profile.avatarUrl, profile.displayName.ifBlank { profile.username }, size = 78.dp, verified = true, modifier = Modifier.align(Alignment.BottomCenter).offset(y = 34.dp))
                            }
                            Spacer(Modifier.height(40.dp))
                            Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ConsumerColors.TextOnDark)
                            Text("@${profile.username}", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextOnDarkSecondary)
                            Surface(shape = RoundedCornerShape(50), color = ConsumerColors.VipGold.copy(alpha = .18f), modifier = Modifier.padding(vertical = 8.dp)) { Text("Creator · Verified", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = ConsumerColors.VipGold, style = MaterialTheme.typography.labelSmall) }
                            profile.bio?.takeIf(String::isNotBlank)?.let { Text(it, Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextOnDarkSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis) }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
                if (!editing) {
                    item {
                        VoiceCloudApprovedCard(Modifier.fillMaxWidth()) {
                            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CreatorColors.Text)
                            Text(profile.bio?.takeIf(String::isNotBlank) ?: "Add a bio to tell listeners what you create on VoiceCloud.", style = MaterialTheme.typography.bodyMedium, color = CreatorColors.TextMuted)
                            if (!profile.country.isNullOrBlank()) Text("Country · ${profile.country}", style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted)
                            if (profile.interests.isNotEmpty()) Text("Interests · ${profile.interests.joinToString(" · ")}", style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted)
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VoiceCloudApprovedPrimaryButton("Edit Profile", modifier = Modifier.weight(1f)) { editing = true }
                            VoiceCloudApprovedSecondaryButton("Help", modifier = Modifier.weight(1f), onClick = onHelp)
                        }
                    }
                    item { VoiceCloudApprovedSecondaryButton("Switch to Listener", onClick = onSwitchToVoiceCloud) }
                } else {
                    item { OutlinedTextField(bio, { bio = it }, Modifier.fillMaxWidth(), label = { Text("Bio") }, minLines = 3, maxLines = 6, shape = RoundedCornerShape(11.dp)) }
                    item { OutlinedTextField(country, { country = it }, Modifier.fillMaxWidth(), label = { Text("Country") }, shape = RoundedCornerShape(11.dp)) }
                    item { OutlinedTextField(interests, { interests = it }, Modifier.fillMaxWidth(), label = { Text("Interests") }, supportingText = { Text("Separate interests with commas") }, shape = RoundedCornerShape(11.dp)) }
                    item { VoiceCloudApprovedPrimaryButton(if (state.saving) "Saving…" else "Save profile", enabled = !state.saving) { onSave(bio, country, interests.split(',').map(String::trim).filter(String::isNotBlank)); editing = false } }
                    item { VoiceCloudApprovedSecondaryButton("Cancel", onClick = { editing = false }) }
                }
            }
        }
    }
}

@Composable
fun CreatorSettingsScreen(
    state: CreatorUiState,
    onLoad: () -> Unit,
    onSave: (CreatorSettings) -> Unit,
    onDashboard: () -> Unit,
    onLive: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onAnalytics: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val remote = state.settings
    var value by remember(remote) { mutableStateOf(remote ?: CreatorSettings()) }
    CreatorPortalFrame("Creator Settings", CreatorPortalSection.SETTINGS, onDashboard, onLive, onProfile, onSettings, onHelp, onAnalytics, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { VoiceCloudApprovedSectionTitle("Settings & privacy") }
            item { CreatorStatus(state, onLoad) }
            if (remote != null) {
                item { Text(voiceCloudTitleCase("Notifications"), style = MaterialTheme.typography.titleLarge) }
                item { CreatorToggle("Email notifications", value.notifications.email) { value = value.copy(notifications = value.notifications.copy(email = it)) } }
                item { CreatorToggle("Push notifications", value.notifications.push) { value = value.copy(notifications = value.notifications.copy(push = it)) } }
                item { CreatorToggle("In-app notifications", value.notifications.inApp) { value = value.copy(notifications = value.notifications.copy(inApp = it)) } }
                item { CreatorToggle("Notification sounds", value.notifications.sound) { value = value.copy(notifications = value.notifications.copy(sound = it)) } }
                item { OutlinedTextField("English", {}, Modifier.fillMaxWidth(), enabled = false, label = { Text(voiceCloudTitleCase("Language")) }) }
                item {
                    OutlinedTextField(
                        value.timezone,
                        { value = value.copy(timezone = it) },
                        Modifier.fillMaxWidth(),
                        label = { Text(voiceCloudTitleCase("Timezone")) },
                        supportingText = { Text(voiceCloudTitleCase("Use a time zone such as Asia/Kolkata")) },
                    )
                }
                item { VoiceCloudApprovedPrimaryButton(if (state.saving) "Saving…" else "Save settings", enabled = !state.saving) { onSave(value.copy(language = "en")) } }

            }
        }
    }
}

@Composable
private fun CreatorToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            VoiceCloudPictogram(voiceCloudVisualFor(label), size = 32.dp)
            Text(voiceCloudTitleCase(label), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Switch(checked, onCheckedChange)
        }
    }
}

@Composable
fun CreatorHelpScreen(
    state: CreatorUiState,
    onLoad: () -> Unit,
    onOpenPage: (String) -> Unit,
    onContact: () -> Unit,
    onDashboard: () -> Unit,
    onLive: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onAnalytics: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPortalFrame("Creator Help", CreatorPortalSection.HELP, onDashboard, onLive, onProfile, onSettings, onHelp, onAnalytics, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { VoiceCloudApprovedSectionTitle("Help & support") }
            item { CreatorStatus(state, onLoad) }
            item {
                VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 10.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.HELP, size = 34.dp)
                        Column(Modifier.weight(1f)) {
                            Text(voiceCloudTitleCase("Creator Access Active"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(voiceCloudTitleCase("Manage profile, rooms and support from one place"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { VoiceCloudApprovedPrimaryButton("Send a Support Ticket", onClick = onContact) }
            if (state.cmsPages.isEmpty() && !state.loading) item { Text(voiceCloudTitleCase("No Creator CMS pages are currently published for this account."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(state.cmsPages, key = { it.slug }) { page ->
                VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 10.dp, onClick = { onOpenPage(page.slug) }) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(page.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        page.excerpt?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    }
                }
            }
        }
    }
}


@Composable
fun CreatorAudienceScreen(
    state: CreatorUiState,
    onLoad: () -> Unit,
    onFollowers: () -> Unit,
    onMessages: () -> Unit,
    onSubscribers: () -> Unit,
    onPlans: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(topBar = { CreatorSecondaryTopBar("Audience", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { VoiceCloudApprovedSectionTitle("Audience growth") }
                item { CreatorStatus(state, onLoad) }
                state.audience?.let { audience ->
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CreatorMetricCard("Followers", audience.followerTotal.toString(), Modifier.weight(1f))
                            CreatorMetricCard("Following", audience.followingTotal.toString(), Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CreatorMetricCard("Subscribers", audience.subscriberTotal?.toString() ?: "—", Modifier.weight(1f))
                            CreatorMetricCard("Listeners", audience.listeners ?: "—", Modifier.weight(1f))
                        }
                    }
                }
                item { VoiceCloudApprovedPrimaryButton("Followers", onClick = onFollowers) }
                item { VoiceCloudApprovedSecondaryButton("Subscribers", onClick = onSubscribers) }
                item { VoiceCloudApprovedSecondaryButton("Subscription Plans", onClick = onPlans) }
                item { VoiceCloudApprovedSecondaryButton("Direct Messages", onClick = onMessages) }
                item {
                    Text(
                        voiceCloudTitleCase("Audience and subscription metrics are read from VoiceCloud. A dash means the backend did not provide a global total."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatorMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    VoiceCloudApprovedStatCard(value, label, modifier)
}

@Composable
fun CreatorFollowersScreen(
    state: CreatorUiState,
    onLoad: (String, CreatorFollowerSort) -> Unit,
    onToggleFollowBack: (String, Boolean) -> Unit,
    onMessage: (String) -> Unit,
    onBack: () -> Unit,
) {
    var search by rememberSaveable { mutableStateOf(state.followerSearch) }
    var sort by remember { mutableStateOf(state.followerSort) }
    LaunchedEffect(Unit) { onLoad(search, sort) }
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(topBar = { CreatorSecondaryTopBar("Followers", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { VoiceCloudApprovedSectionTitle("Followers") }
                item { CreatorStatus(state) { onLoad(search, sort) } }
                item {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(voiceCloudTitleCase("Search Followers")) },
                        singleLine = true,
                        trailingIcon = { TextButton(onClick = { onLoad(search, sort) }) { Text(voiceCloudTitleCase("Search")) } },
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CreatorFollowerSort.entries, key = { it.name }) { option ->
                            FilterChip(
                                selected = sort == option,
                                onClick = { sort = option; onLoad(search, option) },
                                label = { Text(voiceCloudTitleCase(option.name.lowercase())) },
                            )
                        }
                    }
                }
                if (state.followers.isEmpty() && !state.loading) item { CreatorEmptyState("No Followers Found", "Followers Matching This Search Will Appear Here.") }
                items(state.followers, key = { it.user.id }) { follower ->
                    ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(follower.user.displayName.ifBlank { follower.user.username }, style = MaterialTheme.typography.titleMedium)
                                    Text(voiceCloudTitleCase("@${follower.user.username}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (follower.user.isOnline) Text(voiceCloudTitleCase("Online"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                                }
                                Text(follower.user.followersCount.toString(), style = MaterialTheme.typography.titleMedium)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onToggleFollowBack(follower.user.id, !follower.followingBack) },
                                    enabled = !state.saving,
                                    modifier = Modifier.weight(1f),
                                ) { Text(voiceCloudTitleCase(if (follower.followingBack) "Following" else "Follow Back")) }
                                OutlinedButton(onClick = { onMessage(follower.user.id) }, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Message")) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorSubscribersScreen(
    state: CreatorUiState,
    onLoad: (String?) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad(null) }
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(topBar = { CreatorSecondaryTopBar("Subscribers", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { VoiceCloudApprovedSectionTitle("Subscribers") }
                item { CreatorStatus(state) { onLoad(state.subscriberStatus) } }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val statuses = listOf<String?>(null, "ACTIVE", "PENDING", "CANCELLED", "EXPIRED")
                        items(statuses, key = { it ?: "ALL" }) { status ->
                            FilterChip(
                                selected = state.subscriberStatus == status,
                                onClick = { onLoad(status) },
                                label = { Text(voiceCloudTitleCase(status?.lowercase() ?: "all")) },
                            )
                        }
                    }
                }
                state.subscribers?.let { page ->
                    item {
                        Text(
                            voiceCloudTitleCase(page.total?.let { "$it Subscribers" } ?: "Subscriber Total Not Published By Backend"),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    if (page.items.isEmpty() && !state.loading) item { CreatorEmptyState("No Subscribers", "No Subscribers Match This Filter.") }
                    items(page.items, key = { it.id.ifBlank { "${it.userId}:${it.planId}:${it.subscribedAt}" } }) { subscriber ->
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(subscriber.displayName.ifBlank { subscriber.username.ifBlank { "VoiceCloud Subscriber" } }, style = MaterialTheme.typography.titleMedium)
                                if (subscriber.username.isNotBlank()) Text(voiceCloudTitleCase("@${subscriber.username}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                subscriber.planTitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                if (subscriber.status.isNotBlank()) Text(voiceCloudTitleCase(subscriber.status.lowercase()), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                subscriber.subscribedAt?.let { Text(voiceCloudTitleCase("Subscribed $it"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                subscriber.expiresAt?.let { Text(voiceCloudTitleCase("Expires $it"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                    }
                }
                item {
                    Text(
                        voiceCloudTitleCase("This screen shows authoritative subscription records only. It does not infer payment completion."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun CreatorPlansScreen(
    state: CreatorUiState,
    onLoad: () -> Unit,
    onCreate: (String, String?, Double, Double?, List<String>, String) -> Unit,
    onUpdate: (String, String, String?, Double, Double?, List<String>, String, String) -> Unit,
    onArchive: (String) -> Unit,
    onBack: () -> Unit,
) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var monthly by rememberSaveable { mutableStateOf("") }
    var yearly by rememberSaveable { mutableStateOf("") }
    var benefits by rememberSaveable { mutableStateOf("") }
    var visibility by rememberSaveable { mutableStateOf("PUBLIC") }
    var status by rememberSaveable { mutableStateOf("DRAFT") }
    LaunchedEffect(Unit) { onLoad() }

    fun loadEditor(plan: CreatorPlan?) {
        editingId = plan?.id
        title = plan?.title.orEmpty()
        description = plan?.description.orEmpty()
        monthly = plan?.monthlyPrice?.toString().orEmpty()
        yearly = plan?.yearlyPrice?.toString().orEmpty()
        benefits = plan?.benefits?.joinToString(", ").orEmpty()
        visibility = plan?.visibility ?: "PUBLIC"
        status = plan?.status ?: "DRAFT"
    }

    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(topBar = { CreatorSecondaryTopBar("Subscription Plans", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { VoiceCloudApprovedSectionTitle("Subscription plans") }
                item { CreatorStatus(state, onLoad) }
                item { Text(voiceCloudTitleCase(if (editingId == null) "Create Plan" else "Edit Plan"), style = MaterialTheme.typography.titleLarge) }
                item { OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Plan Title")) }) }
                item { OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Description")) }, minLines = 2) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(monthly, { monthly = it }, Modifier.weight(1f), label = { Text(voiceCloudTitleCase("Monthly USD")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        OutlinedTextField(yearly, { yearly = it }, Modifier.weight(1f), label = { Text(voiceCloudTitleCase("Yearly USD")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    }
                }
                item { OutlinedTextField(benefits, { benefits = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Benefits")) }, supportingText = { Text(voiceCloudTitleCase("Separate Benefits With Commas")) }) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("PUBLIC", "PRIVATE", "CLUB_ONLY", "LINK_ONLY"), key = { it }) { option ->
                            FilterChip(selected = visibility == option, onClick = { visibility = option }, label = { Text(voiceCloudTitleCase(option.lowercase().replace('_', ' '))) })
                        }
                    }
                }
                if (editingId != null) item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("DRAFT", "ACTIVE"), key = { it }) { option ->
                            FilterChip(selected = status == option, onClick = { status = option }, label = { Text(voiceCloudTitleCase(option.lowercase())) })
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            val m = monthly.toDoubleOrNull()
                            if (m != null) {
                                val perkList = benefits.split(',').map(String::trim).filter(String::isNotBlank)
                                val y = yearly.toDoubleOrNull()
                                val id = editingId
                                if (id == null) onCreate(title, description, m, y, perkList, visibility)
                                else onUpdate(id, title, description, m, y, perkList, visibility, status)
                            }
                        },
                        enabled = !state.saving && title.isNotBlank() && monthly.toDoubleOrNull() != null,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(voiceCloudTitleCase(if (editingId == null) "Create Plan" else "Save Plan")) }
                }
                if (editingId != null) item { OutlinedButton(onClick = { loadEditor(null) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Cancel Editing")) } }
                item { HorizontalDivider() }
                item { Text(voiceCloudTitleCase("Your Plans"), style = MaterialTheme.typography.titleLarge) }
                if (state.plans.isEmpty() && !state.loading) item { CreatorEmptyState("No Subscription Plans", "Create A Plan To Define Subscriber Access." ) }
                items(state.plans, key = { it.id }) { plan ->
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            VoiceCloudPictogram(VoiceCloudVisualKind.VIP, size = 50.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Text(voiceCloudTitleCase("${plan.status.lowercase()} · ${plan.visibility.lowercase().replace('_', ' ')}"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                                Text(voiceCloudTitleCase("USD ${"%.2f".format(java.util.Locale.US, plan.monthlyPrice)} / month"), color = CreatorColors.PrimaryDark, fontWeight = FontWeight.Bold)
                                plan.yearlyPrice?.let { Text(voiceCloudTitleCase("USD ${"%.2f".format(java.util.Locale.US, it)} / year"), color = CreatorColors.TextMuted) }
                                plan.subscriberCount?.let { Text(voiceCloudTitleCase("$it Subscribers"), color = CreatorColors.TextMuted) }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { loadEditor(plan) }, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Edit")) }
                            if (!plan.status.equals("ARCHIVED", true)) Button(onClick = { onArchive(plan.id) }, enabled = !state.saving, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Archive")) }
                        }
                    }
                }
                item { Text(voiceCloudTitleCase("Archiving uses VoiceCloud’s archive/deactivate plan operation. No client-side payment state is created."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
fun CreatorCmsContentScreen(state: CreatorUiState, slug: String, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(slug) { onLoad() }
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(topBar = { CreatorSecondaryTopBar(state.cmsPage?.title ?: "Creator information", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { VoiceCloudApprovedSectionTitle(state.cmsPage?.title ?: "Information") }
                item { CreatorStatus(state, onLoad) }
                state.cmsPage?.let { page ->
                    item { Text(plainText(page.content), style = MaterialTheme.typography.bodyLarge) }
                    page.updatedAt?.let { item { Text(voiceCloudTitleCase("Updated $it"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                }
            }
        }
    }
}

@Composable
fun CreatorContactSupportScreen(
    state: CreatorUiState,
    defaultName: String,
    defaultEmail: String,
    onSubmit: (String, String, String?, String) -> Unit,
    onBack: () -> Unit,
) {
    var name by rememberSaveable(defaultName) { mutableStateOf(defaultName) }
    var email by rememberSaveable(defaultEmail) { mutableStateOf(defaultEmail) }
    var phone by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(topBar = { CreatorSecondaryTopBar("Contact support", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { VoiceCloudApprovedSectionTitle("Contact admin") }
                item { CreatorStatus(state) }
                item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Name")) }) }
                item { OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Email")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)) }
                item { OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Phone (Optional)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)) }
                item { OutlinedTextField(message, { message = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("How Can We Help?")) }, minLines = 5, maxLines = 10) }
                item { VoiceCloudApprovedPrimaryButton(if (state.saving) "Sending…" else "Send support ticket", enabled = !state.saving) { onSubmit(name, email, phone, message) } }
                item { Text(voiceCloudTitleCase("Your message is sent securely to VoiceCloud Support"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun CreatorSecondaryTopBar(title: String, onBack: () -> Unit) {
    Surface(color = CreatorColors.LightSurface, shadowElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { VoiceCloudApprovedTopBar(title, onBack = onBack) }
    }
}

private fun plainText(value: String): String = if (value.contains('<') && value.contains('>')) {
    Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().trim()
} else value.trim()

// PH13 Creator analytics/economy surfaces. All displayed values originate from backend responses.
@Composable
private fun CreatorPhase13Page(title: String, subtitle: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(
            containerColor = CreatorColors.LightBackground,
            topBar = { Surface(color = CreatorColors.LightSurface, shadowElevation = 1.dp) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { VoiceCloudApprovedTopBar(title, onBack = onBack) } } },
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 10.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted)
                content()
            }
        }
    }
}

@Composable
private fun CreatorMetricCards(metrics: List<CreatorMetric>) {
    metrics.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { metric -> VoiceCloudApprovedStatCard(metric.value, metric.label, Modifier.weight(1f)) }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun CreatorAnalyticsScreen(state: CreatorUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPhase13Page("Analytics", "Backend-computed creator performance.", onBack) {
        CreatorStatus(state, onLoad)
        val analytics = state.analytics
        if (analytics != null) {
            if (analytics.metrics.isEmpty() && !state.loading) CreatorEmptyState("No Analytics Yet", "Creator performance will appear when the backend has activity to report.")
            CreatorMetricCards(analytics.metrics)
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VoiceCloudApprovedSectionTitle("Listener trend")
                    Text(
                        voiceCloudTitleCase("Trend chart will appear when VoiceCloud returns time-series analytics."),
                        style = MaterialTheme.typography.bodySmall,
                        color = CreatorColors.TextMuted,
                    )
                }
            }
            analytics.generatedAt?.let { Text(voiceCloudTitleCase("Updated $it"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun CreatorWalletScreen(state: CreatorUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPhase13Page("Creator Wallet", "Server-authoritative balances and ledger.", onBack) {
        CreatorStatus(state, onLoad)
        state.wallet?.let { wallet ->
            Text(voiceCloudTitleCase("Balances"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (wallet.balances.isEmpty() && !state.loading) CreatorEmptyState("No Balance Data", "Wallet balances will appear when returned by VoiceCloud.")
            wallet.balances.forEach { balance ->
                Card(
                    Modifier.fillMaxWidth().border(1.dp, ConsumerColors.VipGold.copy(alpha = .45f), RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CreatorColors.Primary),
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(voiceCloudTitleCase(balance.type.replace('_', ' ')), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelLarge)
                            Text(balance.amount, fontWeight = FontWeight.ExtraBold, color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.headlineSmall)
                        }
                        Text(voiceCloudTitleCase("◈"), color = ConsumerColors.VipGold, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            if (wallet.summary.isNotEmpty()) {
                Text(voiceCloudTitleCase("Summary"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                CreatorMetricCards(wallet.summary)
            }
            Text(voiceCloudTitleCase("Recent Transactions"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            wallet.transactions.forEach { tx ->
                VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.WALLET, size = 44.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(voiceCloudTitleCase(tx.type.replace('_', ' ')).ifBlank { voiceCloudTitleCase("Wallet Transaction") }, fontWeight = FontWeight.ExtraBold)
                            Text(listOf(tx.amount, tx.currency).filter(String::isNotBlank).joinToString(" "), color = CreatorColors.PrimaryDark, fontWeight = FontWeight.Bold)
                            if (tx.status.isNotBlank()) Text(voiceCloudTitleCase(tx.status), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            tx.description?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                            tx.createdAt?.let { Text(it.replace('T', ' ').take(19), style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorEarningsScreen(state: CreatorUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPhase13Page("Earnings", "Read-only creator earnings reported by VoiceCloud.", onBack) {
        CreatorStatus(state, onLoad)
        val earnings = state.earnings
        if (earnings != null) {
            if (earnings.metrics.isEmpty() && !state.loading) CreatorEmptyState("No Earnings Yet", "Earnings appear only when the backend reports settled creator activity.")
            earnings.metrics.firstOrNull()?.let { primary ->
                VoiceCloudApprovedCard(Modifier.fillMaxWidth(), dark = true, contentPadding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Total earnings", color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelSmall)
                        Text(primary.value, color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(voiceCloudTitleCase(primary.label), color = ConsumerColors.VipGold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            CreatorMetricCards(earnings.metrics.drop(1))
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                Text(
                    voiceCloudTitleCase("Earnings trend will appear when VoiceCloud returns time-series settlement data."),
                    style = MaterialTheme.typography.bodySmall,
                    color = CreatorColors.TextMuted,
                )
            }
            earnings.generatedAt?.let { Text(voiceCloudTitleCase("Updated $it"), style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun CreatorGiftsScreen(state: CreatorUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPhase13Page("Gifts", "Authoritative gift history and settlement records.", onBack) {
        CreatorStatus(state, onLoad)
        if (state.gifts.isEmpty() && !state.loading) CreatorEmptyState("No Gift History", "Gift transactions will appear when returned by the backend.")
        state.gifts.forEach { gift ->
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VoiceCloudPictogram(VoiceCloudVisualKind.GIFT, size = 48.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(gift.giftName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        if (gift.direction.isNotBlank()) Text(voiceCloudTitleCase(gift.direction.replace('_', ' ')), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        gift.counterparty?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        val amount = listOfNotNull(gift.quantity?.let { "Qty $it" }, gift.amount, gift.currency).filter(String::isNotBlank).joinToString(" · ")
                        if (amount.isNotBlank()) Text(amount, color = CreatorColors.PrimaryDark, fontWeight = FontWeight.Bold)
                        gift.createdAt?.let { Text(it.replace('T', ' ').take(19), style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted) }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorPayoutsScreen(
    state: CreatorUiState,
    onLoad: () -> Unit,
    onCreate: (Int, String) -> Unit,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    var amount by rememberSaveable { mutableStateOf("") }
    var method by rememberSaveable { mutableStateOf("BANK_TRANSFER") }
    val methods = listOf("BANK_TRANSFER", "PAYPAL", "STRIPE", "CRYPTO")
    CreatorPhase13Page("Payout Requests", "Create and review backend-authoritative payout requests.", onBack) {
        CreatorStatus(state, onLoad)
        OutlinedTextField(
            value = amount, onValueChange = { amount = it.filter(Char::isDigit).take(12) },
            modifier = Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Diamond Amount")) },
            supportingText = { Text(voiceCloudTitleCase("Minimum 100 Diamonds")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(methods) { item ->
                FilterChip(selected = method == item, onClick = { method = item }, label = { Text(voiceCloudTitleCase(item.replace('_', ' '))) })
            }
        }
        val parsed = amount.toIntOrNull()
        VoiceCloudApprovedPrimaryButton(if (state.saving) "Submitting…" else "Withdraw now", enabled = parsed != null && parsed >= 100 && !state.saving) { parsed?.let { onCreate(it, method) } }
        HorizontalDivider()
        val page = state.payouts
        if (page != null && page.items.isEmpty() && !state.loading) CreatorEmptyState("No Payout Requests", "Submitted payout requests will appear here.")
        page?.total?.let { Text(voiceCloudTitleCase("Total Requests: $it"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        page?.items.orEmpty().forEach { payout ->
            VoiceCloudApprovedCard(Modifier.fillMaxWidth().clickable { onOpen(payout.id) }, contentPadding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VoiceCloudPictogram(VoiceCloudVisualKind.PAYOUT, size = 46.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(listOf(payout.diamondAmount, "Diamonds").filter(String::isNotBlank).joinToString(" "), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Text(voiceCloudTitleCase(payout.payoutMethod.replace('_', ' ')), color = CreatorColors.TextMuted)
                        Text(voiceCloudTitleCase(payout.status.ifBlank { "Status Pending From Backend" }), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        payout.createdAt?.let { Text(it.replace('T', ' ').take(19), style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted) }
                    }
                    Text(voiceCloudTitleCase("Open"), color = CreatorColors.PrimaryDark, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun CreatorPayoutDetailScreen(state: CreatorUiState, payoutId: String, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(payoutId) { onLoad() }
    CreatorPhase13Page("Payout Detail", "VoiceCloud payout status and settlement information.", onBack) {
        CreatorStatus(state, onLoad)
        state.selectedPayout?.takeIf { it.id == payoutId }?.let { payout ->
            CreatorMetricCards(listOf(
                CreatorMetric("diamonds", "Diamond Amount", payout.diamondAmount),
                CreatorMetric("method", "Payout Method", payout.payoutMethod.replace('_', ' ')),
                CreatorMetric("status", "Status", payout.status),
            ))
            payout.rejectionReason?.let {
                VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.PAYOUT, size = 42.dp, accent = MaterialTheme.colorScheme.error)
                        Column(Modifier.weight(1f)) {
                            Text(voiceCloudTitleCase("Payout Update"), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            payout.createdAt?.let { Text(voiceCloudTitleCase("Requested: $it")) }
            payout.updatedAt?.let { Text(voiceCloudTitleCase("Updated: $it")) }
        }
    }
}
