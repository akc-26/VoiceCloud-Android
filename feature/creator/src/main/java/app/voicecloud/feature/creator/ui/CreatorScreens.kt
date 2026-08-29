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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.component.VoiceCloudHeroCard
import app.voicecloud.core.designsystem.component.VoiceCloudEmptyVisual
import app.voicecloud.core.designsystem.component.VoiceCloudGlossCard
import app.voicecloud.core.designsystem.component.VoiceCloudMetricTile
import app.voicecloud.core.designsystem.component.VoiceCloudMiniChart
import app.voicecloud.core.designsystem.component.VoiceCloudPageHero
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.voiceCloudVisualFor
import app.voicecloud.core.designsystem.component.VoiceCloudPremiumCard
import app.voicecloud.core.designsystem.component.VoiceCloudSectionHeader
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
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
    onSwitchToVoiceCloud: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(
            containerColor = CreatorColors.LightBackground,
            topBar = {
                Surface(color = CreatorColors.LightSurface, shadowElevation = 3.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        VoiceCloudBrandMark(42.dp)
                        Column(Modifier.weight(1f)) {
                            Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CreatorColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(voiceCloudTitleCase("Creator Portal"), style = MaterialTheme.typography.labelMedium, color = CreatorColors.PrimaryDark)
                        }
                        VoiceCloudPictogram(voiceCloudVisualFor(title), size = 38.dp)
                        Surface(shape = RoundedCornerShape(50), color = ConsumerColors.Lavender, modifier = Modifier.border(1.dp, ConsumerColors.VipGold.copy(alpha=.42f), RoundedCornerShape(50))) {
                            Text(voiceCloudTitleCase("Creator"), Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = ConsumerColors.VioletDeep, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onSwitchToVoiceCloud) { Text(voiceCloudTitleCase("Listener"), maxLines = 1) }
                    }
                }
            },
            bottomBar = {
                Box(Modifier.fillMaxWidth().background(CreatorColors.LightBackground).padding(horizontal = 12.dp, vertical = 7.dp)) {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(25.dp))
                            .border(1.dp, CreatorColors.Border, RoundedCornerShape(25.dp)),
                        containerColor = CreatorColors.LightSurface,
                        tonalElevation = 4.dp,
                    ) {
                        CreatorNavItem("Dashboard", selected == CreatorPortalSection.DASHBOARD, onDashboard)
                        CreatorNavItem("Live", selected == CreatorPortalSection.LIVE, onLive)
                        CreatorNavItem("Profile", selected == CreatorPortalSection.PROFILE, onProfile)
                        CreatorNavItem("Settings", selected == CreatorPortalSection.SETTINGS, onSettings)
                    }
                }
            },
            content = content,
        )
    }
}

@Composable
private fun RowScope.CreatorNavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = androidx.compose.ui.graphics.Color.White,
            selectedTextColor = CreatorColors.PrimaryDark,
            indicatorColor = CreatorColors.Primary,
            unselectedIconColor = CreatorColors.TextMuted,
            unselectedTextColor = CreatorColors.TextMuted,
        ),
        icon = {
            VoiceCloudPictogram(
                kind = voiceCloudVisualFor(label),
                size = if (selected) 31.dp else 29.dp,
                dark = selected,
                accent = if (selected) CreatorColors.PrimaryLight else CreatorColors.PrimaryDark,
            )
        },
        label = { Text(voiceCloudTitleCase(label), maxLines = 1, style = MaterialTheme.typography.labelSmall) },
    )
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
    CreatorPortalFrame("Dashboard", CreatorPortalSection.DASHBOARD, onDashboard, onLive, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                VoiceCloudHeroCard(
                    title = "Good morning, ${creatorName.ifBlank { "Creator" }}",
                    subtitle = "Keep inspiring your community. Your live rooms, audience and creator performance are all here.",
                    badge = "Creator dashboard",
                ) { VoiceCloudBrandMark(48.dp, Modifier.padding(start = 8.dp)) }
            }
            item { CreatorStatus(state, onLoad) }
            val dashboard = state.dashboard
            if (dashboard != null && dashboard.metrics.isEmpty() && !state.loading) {
                item {
                    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                        Text(voiceCloudTitleCase("Your Creator Snapshot Will Appear Here When Activity Is Available"), Modifier.padding(18.dp))
                    }
                }
            }
            dashboard?.metrics?.let { metrics ->
                items(metrics.chunked(2), key = { row -> row.joinToString(":") { it.key } }) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { metric ->
                            VoiceCloudMetricTile(metric.value, metric.label, Modifier.weight(1f), VoiceCloudVisualKind.ANALYTICS)
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            item {
                VoiceCloudGlossCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(voiceCloudTitleCase("Audience Momentum"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            Text(voiceCloudTitleCase("A premium visual snapshot of your current creator activity."), color = CreatorColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        VoiceCloudPictogram(VoiceCloudVisualKind.ANALYTICS, size = 46.dp)
                    }
                    VoiceCloudMiniChart(listOf(.22f,.31f,.28f,.48f,.44f,.62f,.58f,.76f,.72f,.91f))
                }
            }
            item {
                Button(onClick = onLive, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Open Live Studio")) }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onAudience, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Audience")) }
                    OutlinedButton(onClick = onMessages, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Messages")) }
                }
            }
            item {
                VoiceCloudSectionHeader("Creator Center")
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onAnalytics, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Analytics")) }
                        OutlinedButton(onClick = onWallet, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Wallet")) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onEarnings, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Earnings")) }
                        OutlinedButton(onClick = onGifts, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Gifts")) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onPayouts, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Payouts")) }
                        OutlinedButton(onClick = onNotifications, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Notifications")) }
                    }
                    OutlinedButton(onClick = onVerification, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Host Verification")) }
                }
            }
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
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPortalFrame("Live Studio", CreatorPortalSection.LIVE, onDashboard, onLive, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { VoiceCloudPageHero("Live Studio", "Create, schedule and manage polished live audio experiences for your audience.", VoiceCloudVisualKind.LIVE, badge = "Creator live", creator = true) }
            item { VoiceCloudToastEffect(state.error, state.notice) }
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onCreateRoom, enabled = !state.loading, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Create Room")) }
                    OutlinedButton(onClick = onScheduleRoom, enabled = !state.loading, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Schedule")) }
                }
            }
            item { Text(voiceCloudTitleCase("My Rooms"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            if (state.rooms.isEmpty() && !state.loading) {
                item { CreatorEmptyState("No Rooms Yet", "Create A Room When You’re Ready To Go Live.") }
            }
            items(state.rooms.distinctBy(HostRoom::id), key = { "creator-room:${it.id}" }) { room ->
                VoiceCloudGlossCard(Modifier.fillMaxWidth().clickable { onRoom(room.id) }, contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.LIVE, size = 50.dp)
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
                VoiceCloudGlossCard(Modifier.fillMaxWidth().clickable { onSchedule(schedule.id) }, contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.SCHEDULE, size = 50.dp)
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
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val profile = state.profile
    var bio by remember(profile?.id) { mutableStateOf(profile?.bio.orEmpty()) }
    var country by remember(profile?.id) { mutableStateOf(profile?.country.orEmpty()) }
    var interests by remember(profile?.id) { mutableStateOf(profile?.interests?.joinToString(", ").orEmpty()) }
    CreatorPortalFrame("Creator Profile", CreatorPortalSection.PROFILE, onDashboard, onLive, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { VoiceCloudPageHero("Creator Profile", "Present your creator identity, bio and interests with a premium public-facing profile.", VoiceCloudVisualKind.CREATOR, badge = "Your creator identity", creator = true) }
            item { CreatorStatus(state, onLoad) }
            if (profile != null) {
                item {
                    ElevatedCard(shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.titleLarge)
                            Text(voiceCloudTitleCase("@${profile.username}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(voiceCloudTitleCase("Creator Access"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            profile.email?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
                item { OutlinedTextField(bio, { bio = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Bio")) }, minLines = 3, maxLines = 6) }
                item { OutlinedTextField(country, { country = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Country")) }) }
                item { OutlinedTextField(interests, { interests = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Interests")) }, supportingText = { Text(voiceCloudTitleCase("Separate Interests With Commas")) }) }
                item {
                    OutlinedTextField("English", {}, Modifier.fillMaxWidth(), enabled = false, label = { Text(voiceCloudTitleCase("Language")) }, supportingText = { Text(voiceCloudTitleCase("English Is Currently Supported")) })
                }
                item {
                    Button(
                        onClick = { onSave(bio, country, interests.split(',').map(String::trim).filter(String::isNotBlank)) },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(voiceCloudTitleCase(if (state.saving) "Saving…" else "Save creator profile")) }
                }
                item { OutlinedButton(onClick = onHelp, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Help & Support")) } }
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
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val remote = state.settings
    var value by remember(remote) { mutableStateOf(remote ?: CreatorSettings()) }
    CreatorPortalFrame("Creator Settings", CreatorPortalSection.SETTINGS, onDashboard, onLive, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { VoiceCloudPageHero("Creator Settings", "Control creator notifications, preferences and account-facing settings.", VoiceCloudVisualKind.SETTINGS, badge = "Creator preferences", creator = true) }
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
                        supportingText = { Text(voiceCloudTitleCase("Use A Time Zone Such As Asia/Kolkata")) },
                    )
                }
                item { Button(onClick = { onSave(value.copy(language = "en")) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase(if (state.saving) "Saving…" else "Save settings")) } }

            }
        }
    }
}

@Composable
private fun CreatorToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 13.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VoiceCloudPictogram(voiceCloudVisualFor(label), size = 40.dp)
            Text(voiceCloudTitleCase(label), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
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
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPortalFrame("Creator Help", CreatorPortalSection.HELP, onDashboard, onLive, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { VoiceCloudPageHero("Creator Help", "Guidance, support and creator information in one polished support center.", VoiceCloudVisualKind.HELP, badge = "Creator support", creator = true) }
            item { CreatorStatus(state, onLoad) }
            item {
                ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(voiceCloudTitleCase("Creator Access Active"), style = MaterialTheme.typography.titleMedium)
                        Text(voiceCloudTitleCase("Manage Your Creator Profile, Live Rooms And Support From One Place"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { Button(onClick = onContact, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Contact VoiceCloud Support")) } }
            if (state.cmsPages.isEmpty() && !state.loading) item { Text(voiceCloudTitleCase("No Creator CMS Pages Are Currently Published For This Account."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(state.cmsPages, key = { it.slug }) { page ->
                ElevatedCard(onClick = { onOpenPage(page.slug) }, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(page.title, style = MaterialTheme.typography.titleMedium)
                        page.excerpt?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis) }
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
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { VoiceCloudPageHero("Audience", "Understand followers, subscribers and listener relationships around your creator presence.", VoiceCloudVisualKind.AUDIENCE, badge = "Grow your community", creator = true) }
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
                item { Button(onClick = onFollowers, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Followers")) } }
                item { OutlinedButton(onClick = onSubscribers, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Subscribers")) } }
                item { OutlinedButton(onClick = onPlans, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Subscription Plans")) } }
                item { OutlinedButton(onClick = onMessages, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Direct Messages")) } }
                item {
                    Text(
                        voiceCloudTitleCase("Audience And Subscription Metrics Are Read From VoiceCloud. A Dash Means The Backend Did Not Provide A Global Total."),
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
    VoiceCloudMetricTile(value, label, modifier, VoiceCloudVisualKind.ANALYTICS)
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
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { VoiceCloudPageHero("Followers", "Explore the people following your creator journey and connect back when appropriate.", VoiceCloudVisualKind.AUDIENCE, badge = "Creator community", creator = true) }
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
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { VoiceCloudPageHero("Subscribers", "Your premium subscriber relationships and plan access, straight from VoiceCloud.", VoiceCloudVisualKind.VIP, badge = "Premium audience", creator = true) }
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
                        voiceCloudTitleCase("This Screen Shows Authoritative Subscription Records Only. It Does Not Infer Payment Completion."),
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
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { VoiceCloudPageHero("Subscription Plans", "Create polished creator memberships and manage subscriber access without changing payment authority.", VoiceCloudVisualKind.VIP, badge = "Membership", creator = true) }
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
                    VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
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
                item { Text(voiceCloudTitleCase("Archiving Uses VoiceCloud’s Archive/Deactivate Plan Operation. No Client-Side Payment State Is Created."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
fun CreatorCmsContentScreen(state: CreatorUiState, slug: String, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(slug) { onLoad() }
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) {
        Scaffold(topBar = { CreatorSecondaryTopBar(state.cmsPage?.title ?: "Creator information", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { VoiceCloudPageHero(state.cmsPage?.title ?: "Creator Information", "Creator guidance and information published by VoiceCloud.", VoiceCloudVisualKind.HELP, badge = "Information", creator = true) }
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
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { VoiceCloudPageHero("Contact Support", "Send a secure support request directly to VoiceCloud.", VoiceCloudVisualKind.HELP, badge = "Creator support", creator = true) }
                item { CreatorStatus(state) }
                item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Name")) }) }
                item { OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Email")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)) }
                item { OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Phone (Optional)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)) }
                item { OutlinedTextField(message, { message = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("How Can We Help?")) }, minLines = 5, maxLines = 10) }
                item { Button(onClick = { onSubmit(name, email, phone, message) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase(if (state.saving) "Sending…" else "Send to support")) } }
                item { Text(voiceCloudTitleCase("Your Message Is Sent Securely To VoiceCloud Support"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun CreatorSecondaryTopBar(title: String, onBack: () -> Unit) {
    Surface(shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(voiceCloudTitleCase("Back")) }
            VoiceCloudPictogram(voiceCloudVisualFor(title), size = 36.dp, modifier = Modifier.padding(end = 9.dp))
            Text(voiceCloudTitleCase(title), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        }
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
            topBar = {
                Surface(color = CreatorColors.LightSurface, shadowElevation = 2.dp) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onBack) { Text(voiceCloudTitleCase("Back")) }
                        Column(Modifier.weight(1f)) {
                            Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CreatorColors.Text)
                            Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodySmall, color = CreatorColors.TextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        VoiceCloudPictogram(voiceCloudVisualFor(title), size = 38.dp)
                    }
                }
            },
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(18.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                VoiceCloudPageHero(
                    title = title,
                    subtitle = subtitle,
                    kind = voiceCloudVisualFor(title),
                    badge = "Creator portal",
                    creator = true,
                )
                content()
            }
        }
    }
}

@Composable
private fun CreatorMetricCards(metrics: List<CreatorMetric>) {
    metrics.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { metric ->
                VoiceCloudMetricTile(metric.value, metric.label, Modifier.weight(1f), VoiceCloudVisualKind.ANALYTICS)
            }
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
                    colors = CardDefaults.cardColors(containerColor = ConsumerColors.LiveSurface),
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
                VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
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
            CreatorMetricCards(earnings.metrics)
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
            VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
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
        Button(
            onClick = { parsed?.let { onCreate(it, method) } }, enabled = parsed != null && parsed >= 100 && !state.saving,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(voiceCloudTitleCase(if (state.saving) "Submitting…" else "Submit Payout Request")) }
        HorizontalDivider()
        val page = state.payouts
        if (page != null && page.items.isEmpty() && !state.loading) CreatorEmptyState("No Payout Requests", "Submitted payout requests will appear here.")
        page?.total?.let { Text(voiceCloudTitleCase("Total Requests: $it"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        page?.items.orEmpty().forEach { payout ->
            VoiceCloudGlossCard(Modifier.fillMaxWidth().clickable { onOpen(payout.id) }, contentPadding = 14.dp) {
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
                VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
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
