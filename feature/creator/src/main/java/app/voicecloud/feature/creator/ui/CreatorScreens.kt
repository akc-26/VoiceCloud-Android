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
import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
import app.voicecloud.feature.creator.model.*

@Composable
private fun CreatorPortalFrame(
    title: String,
    selected: CreatorPortalSection,
    onDashboard: () -> Unit,
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
                                Text("VC", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${VoiceCloudBrand.name} Creator", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = onSwitchToVoiceCloud) { Text("VoiceCloud") }
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    CreatorNavItem("Dashboard", selected == CreatorPortalSection.DASHBOARD, onDashboard)
                    CreatorNavItem("Profile", selected == CreatorPortalSection.PROFILE, onProfile)
                    CreatorNavItem("Settings", selected == CreatorPortalSection.SETTINGS, onSettings)
                    CreatorNavItem("Help", selected == CreatorPortalSection.HELP, onHelp)
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
        icon = { Text(label.take(1), fontWeight = FontWeight.Bold) },
        label = { Text(label, maxLines = 1) },
    )
}

@Composable
private fun CreatorStatus(state: CreatorUiState, onRetry: (() -> Unit)? = null) {
    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    state.error?.let { message ->
        ElevatedCard(shape = RoundedCornerShape(18.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                onRetry?.let { TextButton(onClick = it) { Text("Try again") } }
            }
        }
    }
    state.notice?.let { message ->
        ElevatedCard(shape = RoundedCornerShape(18.dp)) {
            Text(message, modifier = Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun CreatorDashboardScreen(
    state: CreatorUiState,
    creatorName: String,
    onLoad: () -> Unit,
    onDashboard: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPortalFrame("Dashboard", CreatorPortalSection.DASHBOARD, onDashboard, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("Welcome, ${creatorName.ifBlank { "Creator" }}", style = MaterialTheme.typography.headlineMedium)
                Text("Server-authoritative Creator overview. Values shown here come from /creator/dashboard.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { CreatorStatus(state, onLoad) }
            val dashboard = state.dashboard
            if (dashboard != null && dashboard.metrics.isEmpty() && !state.loading) {
                item {
                    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                        Text("The Creator dashboard is connected, but the backend did not return any supported summary metrics for this account.", Modifier.padding(18.dp))
                    }
                }
            }
            dashboard?.metrics?.let { metrics ->
                items(metrics, key = { it.key }) { metric ->
                    ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(metric.label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(metric.value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            item {
                Text("Live rooms, schedule, audience, messaging, subscriptions, analytics and financial tools remain in their locked Creator phases and are not exposed early.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CreatorProfileScreen(
    state: CreatorUiState,
    onLoad: () -> Unit,
    onSave: (String?, String?, List<String>) -> Unit,
    onDashboard: () -> Unit,
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
    CreatorPortalFrame("Creator profile", CreatorPortalSection.PROFILE, onDashboard, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { CreatorStatus(state, onLoad) }
            if (profile != null) {
                item {
                    ElevatedCard(shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.titleLarge)
                            Text("@${profile.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Creator access", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            profile.email?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
                item { OutlinedTextField(bio, { bio = it }, Modifier.fillMaxWidth(), label = { Text("Bio") }, minLines = 3, maxLines = 6) }
                item { OutlinedTextField(country, { country = it }, Modifier.fillMaxWidth(), label = { Text("Country") }) }
                item { OutlinedTextField(interests, { interests = it }, Modifier.fillMaxWidth(), label = { Text("Interests") }, supportingText = { Text("Comma-separated. VoiceCloud stores the canonical list.") }) }
                item {
                    OutlinedTextField("English", {}, Modifier.fillMaxWidth(), enabled = false, label = { Text("Language") }, supportingText = { Text("English is the only language currently published by the backend.") })
                }
                item {
                    Button(
                        onClick = { onSave(bio, country, interests.split(',').map(String::trim).filter(String::isNotBlank)) },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (state.saving) "Saving…" else "Save creator profile") }
                }
                item { Text("Username, email, role and verification authority are read-only here. Media replacement continues to use the shared profile authority rather than local Creator-only state.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val remote = state.settings
    var value by remember(remote) { mutableStateOf(remote ?: CreatorSettings()) }
    CreatorPortalFrame("Creator settings", CreatorPortalSection.SETTINGS, onDashboard, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { CreatorStatus(state, onLoad) }
            if (remote != null) {
                item { Text("Notifications", style = MaterialTheme.typography.titleLarge) }
                item { CreatorToggle("Email notifications", value.notifications.email) { value = value.copy(notifications = value.notifications.copy(email = it)) } }
                item { CreatorToggle("Push notifications", value.notifications.push) { value = value.copy(notifications = value.notifications.copy(push = it)) } }
                item { CreatorToggle("In-app notifications", value.notifications.inApp) { value = value.copy(notifications = value.notifications.copy(inApp = it)) } }
                item { CreatorToggle("Notification sounds", value.notifications.sound) { value = value.copy(notifications = value.notifications.copy(sound = it)) } }
                item { OutlinedTextField("English", {}, Modifier.fillMaxWidth(), enabled = false, label = { Text("Language") }) }
                item {
                    OutlinedTextField(
                        value.timezone,
                        { value = value.copy(timezone = it) },
                        Modifier.fillMaxWidth(),
                        label = { Text("Timezone") },
                        supportingText = { Text("Use an IANA timezone such as Asia/Kolkata. Server validation remains authoritative.") },
                    )
                }
                item { Button(onClick = { onSave(value.copy(language = "en")) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Saving…" else "Save settings") } }
                item { Text("Streaming credentials are intentionally not exposed in PH10; they remain in the locked Creator financial/verification phase where the current Creator Studio authority is reconciled.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun CreatorToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f))
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
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onSwitchToVoiceCloud: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    CreatorPortalFrame("Creator help", CreatorPortalSection.HELP, onDashboard, onProfile, onSettings, onHelp, onSwitchToVoiceCloud) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { CreatorStatus(state, onLoad) }
            item {
                ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Creator access active", style = MaterialTheme.typography.titleMedium)
                        Text("This workspace is available only because the authenticated backend role is CREATOR. Creator access applications remain linked from Creator Sign In for accounts that do not yet have access.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { Button(onClick = onContact, modifier = Modifier.fillMaxWidth()) { Text("Contact VoiceCloud support") } }
            if (state.cmsPages.isEmpty() && !state.loading) item { Text("No Creator CMS pages are currently published for this account.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                    page.updatedAt?.let { item { Text("Updated $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
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
                item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }) }
                item { OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)) }
                item { OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Phone (optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)) }
                item { OutlinedTextField(message, { message = it }, Modifier.fillMaxWidth(), label = { Text("How can we help?") }, minLines = 5, maxLines = 10) }
                item { Button(onClick = { onSubmit(name, email, phone, message) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Sending…" else "Send to support") } }
                item { Text("This submits through VoiceCloud's persisted /contact authority. The Android app does not fall back to mailto: or promise a response time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun CreatorSecondaryTopBar(title: String, onBack: () -> Unit) {
    Surface(shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Back") }
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun plainText(value: String): String = if (value.contains('<') && value.contains('>')) {
    Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().trim()
} else value.trim()
