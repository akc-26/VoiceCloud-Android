package app.voicecloud.feature.creator.ui

import android.text.Html
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
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
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = true) {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 2.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(voiceCloudTitleCase("VC"), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(voiceCloudTitleCase("${VoiceCloudBrand.name} Creator"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onSwitchToVoiceCloud) { Text(voiceCloudTitleCase("VoiceCloud")) }
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    CreatorNavItem("Dashboard", selected == CreatorPortalSection.DASHBOARD, onDashboard)
                    CreatorNavItem("Live", selected == CreatorPortalSection.LIVE, onLive)
                    CreatorNavItem("Profile", selected == CreatorPortalSection.PROFILE, onProfile)
                    CreatorNavItem("Settings", selected == CreatorPortalSection.SETTINGS, onSettings)
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
        icon = { Text(voiceCloudTitleCase(label).take(1), fontWeight = FontWeight.Bold) },
        label = { Text(voiceCloudTitleCase(label), maxLines = 1) },
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
                Text(voiceCloudTitleCase("Welcome, ${creatorName.ifBlank { "Creator" }}"), style = MaterialTheme.typography.headlineMedium)
                Text(voiceCloudTitleCase("Your Creator Snapshot."), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                items(metrics, key = { it.key }) { metric ->
                    ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(voiceCloudTitleCase(metric.label), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(metric.value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            item {
                Button(onClick = onLive, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Open Live Studio")) }
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
                ElevatedCard(onClick = { onRoom(room.id) }, shape = RoundedCornerShape(22.dp)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(voiceCloudTitleCase("●"), color = MaterialTheme.colorScheme.primary) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(room.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(voiceCloudTitleCase(room.status.replaceFirstChar { it.titlecase() }), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(voiceCloudTitleCase("Open"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item { Text(voiceCloudTitleCase("Upcoming"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            if (state.schedules.isEmpty() && !state.loading) {
                item { CreatorEmptyState("Nothing Scheduled", "Plan Your Next Room From Here.") }
            }
            items(state.schedules.distinctBy(ScheduledHostRoom::id), key = { "creator-schedule:${it.id}" }) { schedule ->
                ElevatedCard(onClick = { onSchedule(schedule.id) }, shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(schedule.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(formatCreatorSchedule(schedule.scheduledStartTime, schedule.timeZone), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { onStartScheduled(schedule) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Start Room")) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatorEmptyState(title: String, body: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(voiceCloudTitleCase(body), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
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
    ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(voiceCloudTitleCase(label), modifier = Modifier.weight(1f))
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
fun CreatorCmsContentScreen(state: CreatorUiState, slug: String, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(slug) { onLoad() }
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = true) {
        Scaffold(topBar = { CreatorSecondaryTopBar(state.cmsPage?.title ?: "Creator information", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = true) {
        Scaffold(topBar = { CreatorSecondaryTopBar("Contact support", onBack) }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            Text(voiceCloudTitleCase(title), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun plainText(value: String): String = if (value.contains('<') && value.contains('>')) {
    Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().trim()
} else value.trim()
