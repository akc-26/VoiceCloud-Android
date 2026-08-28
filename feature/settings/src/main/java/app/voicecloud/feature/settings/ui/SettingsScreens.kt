package app.voicecloud.feature.settings.ui

import android.text.Html
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
import app.voicecloud.core.preferences.ThemePreference
import app.voicecloud.feature.settings.model.*

@Composable
private fun PageScaffold(title: String, subtitle: String? = null, onBack: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(topBar = { VoiceCloudPageTopBar(title = title, subtitle = subtitle, onBack = onBack) }, content = content)
}

@Composable
private fun Feedback(state: SettingsUiState, onRetry: (() -> Unit)? = null) {
    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    state.error?.let { message ->
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                if (onRetry != null) TextButton(onClick = onRetry) { Text("Retry") }
            }
        }
    }
    state.notice?.let { message ->
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(message, Modifier.fillMaxWidth().padding(14.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun NavigationCard(title: String, subtitle: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SettingsOverviewScreen(
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    onPrivacy: () -> Unit,
    onVoiceAppearance: () -> Unit,
    onSecurity: () -> Unit,
    onHelp: () -> Unit,
    onSafety: () -> Unit,
    onContact: () -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit,
) {
    PageScaffold("Settings", "Manage your account, preferences and ${VoiceCloudBrand.name} experience.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { NavigationCard("Profile", "Edit your public profile and media", onProfile) }
            item { NavigationCard("Notifications", "Email, push, in-app and sound alerts", onNotifications) }
            item { NavigationCard("Privacy", "Messaging, follows, invitations and visitors", onPrivacy) }
            item { NavigationCard("Voice & appearance", "Audio processing, theme and language", onVoiceAppearance) }
            item { NavigationCard("Security & devices", "Sessions, devices and login activity", onSecurity) }
            item { NavigationCard("Help, FAQ & legal", "Admin-published help and policy content", onHelp) }
            item { NavigationCard("Safety Center", "Reporting, blocked users and privacy shortcuts", onSafety) }
            item { NavigationCard("Contact Support", "Send a persisted support message", onContact) }
            item { NavigationCard("About ${VoiceCloudBrand.name}", "Application and product information", onAbout) }
        }
    }
}

@Composable
fun NotificationPreferencesScreen(state: SettingsUiState, onLoad: () -> Unit, onSave: (NotificationPreferences) -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    var value by remember { mutableStateOf(NotificationPreferences()) }
    LaunchedEffect(state.preferences) { state.preferences?.notifications?.let { value = it } }
    PageScaffold("Notifications", "Choose how ${VoiceCloudBrand.name} notifies you.", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Feedback(state, onLoad)
            ToggleRow("Email notifications", "Account and product messages delivered by email", value.email) { value = value.copy(email = it) }
            ToggleRow("Push notifications", "Alerts delivered to this device", value.push) { value = value.copy(push = it) }
            ToggleRow("In-app notifications", "Activity alerts while using VoiceCloud", value.inApp) { value = value.copy(inApp = it) }
            ToggleRow("Notification sounds", "Play a sound for supported alerts", value.sound) { value = value.copy(sound = it) }
            Button(onClick = { onSave(value) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Saving…" else "Save notification preferences") }
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onChecked)
        }
    }
}

@Composable
fun PrivacySettingsScreen(state: SettingsUiState, onLoad: () -> Unit, onSave: (PrivacyPreferences) -> Unit, onBlocked: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    var value by remember { mutableStateOf(PrivacyPreferences()) }
    LaunchedEffect(state.privacy) { state.privacy?.let { value = it } }
    PageScaffold("Privacy", "Control supported account visibility and interaction permissions.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Feedback(state, onLoad) }
            item { PermissionPicker("Who can message you", value.messagingPermission, listOf("everyone", "following", "friends", "none")) { value = value.copy(messagingPermission = it) } }
            item { PermissionPicker("Who can follow you", value.followPermission, listOf("everyone", "approval", "none")) { value = value.copy(followPermission = it) } }
            item { PermissionPicker("Who can invite you", value.invitationPermission, listOf("everyone", "friends", "none")) { value = value.copy(invitationPermission = it) } }
            item { PermissionPicker("Who can appear in visitor activity", value.visitorPermission, listOf("everyone", "friends", "none")) { value = value.copy(visitorPermission = it) } }
            item { ToggleRow("Profile visitor tracking", "Allow profile visits to be recorded according to your permission setting", value.allowVisitorTracking) { value = value.copy(allowVisitorTracking = it) } }
            item { ToggleRow("Anonymous visiting", "Visit other profiles anonymously by default when supported", value.anonymousVisiting) { value = value.copy(anonymousVisiting = it) } }
            item { OutlinedButton(onClick = onBlocked, modifier = Modifier.fillMaxWidth()) { Text("Blocked users") } }
            item { Button(onClick = { onSave(value) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Saving…" else "Save privacy preferences") } }
        }
    }
}

@Composable
private fun PermissionPicker(title: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    FilterChip(selected = selected.equals(option, true), onClick = { onSelected(option) }, label = { Text(option.replaceFirstChar { it.uppercase() }) })
                }
            }
        }
    }
}

@Composable
fun VoiceAppearanceScreen(
    state: SettingsUiState,
    onLoad: () -> Unit,
    onSaveVoice: (VoicePreferences) -> Unit,
    onSaveTheme: (ThemePreference) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    var voice by remember { mutableStateOf(VoicePreferences()) }
    var theme by remember { mutableStateOf(ThemePreference.LIGHT) }
    LaunchedEffect(state.preferences) {
        state.preferences?.let { prefs -> voice = prefs.voice; theme = prefs.theme }
    }
    PageScaffold("Voice & appearance", "Audio processing and visual preferences.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Feedback(state, onLoad) }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Appearance", style = MaterialTheme.typography.titleLarge)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePreference.entries.forEach { option ->
                                FilterChip(selected = theme == option, onClick = { theme = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) })
                            }
                        }
                        Text("Language", style = MaterialTheme.typography.titleMedium)
                        Text("English", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("English is the currently supported product language.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { onSaveTheme(theme) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text("Apply appearance") }
                    }
                }
            }
            item { ToggleRow("Noise suppression", "Reduce supported background noise during voice sessions", voice.noiseSuppression) { voice = voice.copy(noiseSuppression = it) } }
            item { ToggleRow("Echo cancellation", "Reduce supported speaker and room echo", voice.echoCancellation) { voice = voice.copy(echoCancellation = it) } }
            item { ToggleRow("Automatic gain control", "Let supported audio sessions balance microphone gain", voice.agc) { voice = voice.copy(agc = it) } }
            if (voice.audioPreset.isNotBlank()) item {
                ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Audio preset", style = MaterialTheme.typography.titleMedium); Text(voice.audioPreset, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            item { Button(onClick = { onSaveVoice(voice) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Saving…" else "Save voice preferences") } }
        }
    }
}

@Composable
fun SecurityOverviewScreen(
    state: SettingsUiState,
    onLoad: () -> Unit,
    onSessionsDevices: () -> Unit,
    onLoginActivity: () -> Unit,
    onSignOutAll: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val snapshot = state.security
    PageScaffold("Security", "Review current account access. ${VoiceCloudBrand.name} never displays credential tokens here.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Feedback(state, onLoad) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("${snapshot?.sessions?.size ?: 0}", "Sessions", Modifier.weight(1f))
                    MetricCard("${snapshot?.devices?.size ?: 0}", "Devices", Modifier.weight(1f))
                    MetricCard("${snapshot?.history?.size ?: 0}", "Recent", Modifier.weight(1f))
                }
            }
            item { NavigationCard("Sessions & devices", "Inspect current account access and revoke individual entries", onSessionsDevices) }
            item { NavigationCard("Login activity", "Review up to 100 recent authentication events", onLoginActivity) }
            item {
                OutlinedButton(onClick = onSignOutAll, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Sign out all sessions")
                }
            }
            item { Text("Password change, two-factor authentication and trusted-device controls are not shown because the current mobile backend contract does not provide those mutations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) { Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.headlineMedium); Text(label, style = MaterialTheme.typography.bodySmall) } }
}

@Composable
fun SessionsDevicesScreen(state: SettingsUiState, onLoad: () -> Unit, onSession: (String) -> Unit, onDevice: (String) -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    PageScaffold("Sessions & devices", "Only safe account-facing metadata is displayed.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Feedback(state, onLoad) }
            item { Text("Active sessions", style = MaterialTheme.typography.titleLarge) }
            val sessions = state.security?.sessions.orEmpty()
            if (!state.loading && sessions.isEmpty()) item { EmptyCard("No active sessions were returned.") }
            items(sessions, key = { it.id }) { session ->
                NavigationCard(
                    if (session.isCurrent) "${session.deviceName ?: session.deviceType ?: "Current session"} · Current" else session.deviceName ?: session.deviceType ?: "Session",
                    listOfNotNull(session.status, session.lastActiveAt).joinToString(" · ").ifBlank { "Session details" },
                ) { onSession(session.id) }
            }
            item { Text("Registered devices", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
            val devices = state.security?.devices.orEmpty()
            if (!state.loading && devices.isEmpty()) item { EmptyCard("No registered devices were returned.") }
            items(devices, key = { it.id }) { device ->
                NavigationCard(
                    if (device.isCurrent) "${device.deviceName ?: device.model ?: device.deviceType ?: "Current device"} · Current" else device.deviceName ?: device.model ?: device.deviceType ?: "Device",
                    listOfNotNull(device.status, device.lastUsedAt).joinToString(" · ").ifBlank { "Device details" },
                ) { onDevice(device.id) }
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String) { OutlinedCard(Modifier.fillMaxWidth()) { Text(message, Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable
fun SessionDetailScreen(state: SettingsUiState, id: String, onLoad: () -> Unit, onRevoke: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(id) { onLoad() }
    val session = state.session
    PageScaffold("Session detail", if (session?.isCurrent == true) "Current session" else null, onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Feedback(state, onLoad)
            session?.let {
                DetailRows(listOf(
                    "Device" to (it.deviceName ?: it.deviceType),
                    "Status" to it.status,
                    "IP address" to it.ipAddress,
                    "Last active" to it.lastActiveAt,
                    "Expires" to it.expiresAt,
                    "User agent" to it.userAgent,
                ))
                if (!it.isCurrent) Button(onClick = onRevoke, enabled = !state.saving, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Revoke this session") }
                else Text("The current session cannot be revoked from its own detail page. Use Sign out or Sign out all sessions instead.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DeviceDetailScreen(state: SettingsUiState, id: String, onLoad: () -> Unit, onRevoke: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(id) { onLoad() }
    val device = state.device
    PageScaffold("Device detail", if (device?.isCurrent == true) "Current device" else null, onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Feedback(state, onLoad)
            device?.let {
                DetailRows(listOf(
                    "Device" to (it.deviceName ?: it.model ?: it.deviceType),
                    "Status" to it.status,
                    "Manufacturer" to it.manufacturer,
                    "Model" to it.model,
                    "OS version" to it.osVersion,
                    "App version" to it.appVersion,
                    "Last IP" to it.lastIp,
                    "Last used" to it.lastUsedAt,
                ))
                if (!it.isCurrent) Button(onClick = onRevoke, enabled = !state.saving, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Remove this device") }
                else Text("This is the current device. Removing it here is disabled to avoid invalidating the active session unexpectedly.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailRows(rows: List<Pair<String, String?>>) {
    rows.filter { !it.second.isNullOrBlank() }.forEach { (label, value) ->
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(15.dp)) { Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value.orEmpty(), style = MaterialTheme.typography.bodyLarge) } }
    }
}

@Composable
fun LoginActivityScreen(state: SettingsUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    PageScaffold("Login activity", "Recent server-authoritative account authentication events.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Feedback(state, onLoad) }
            val history = state.security?.history.orEmpty()
            if (!state.loading && history.isEmpty()) item { EmptyCard("No login activity was returned.") }
            items(history, key = { it.id.ifBlank { "${it.action}:${it.createdAt}:${it.ipAddress}" } }) { item ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.action.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                        val meta = listOfNotNull(item.loginMethod, item.platform, item.country, item.ipAddress).filter(String::isNotBlank)
                        if (meta.isNotEmpty()) Text(meta.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        item.createdAt?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpCenterScreen(state: SettingsUiState, onLoad: () -> Unit, onPage: (String) -> Unit, onContact: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    PageScaffold("Help, FAQ & legal", "Published content is managed by VoiceCloud Admin.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Feedback(state, onLoad) }
            if (!state.loading && state.cmsPages.isEmpty()) item { EmptyCard("No published help or policy pages are available right now.") }
            items(state.cmsPages, key = { it.slug }) { page -> NavigationCard(page.title, page.excerpt ?: page.category ?: "Published information") { onPage(page.slug) } }
            item { OutlinedButton(onClick = onContact, modifier = Modifier.fillMaxWidth()) { Text("Contact Support") } }
            item { Text("Support availability and response times are not fabricated in the app; only server-published content and persisted contact submission are used.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun CmsContentScreen(state: SettingsUiState, slug: String, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(slug) { onLoad() }
    PageScaffold(state.cmsPage?.title ?: "Information", null, onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Feedback(state, onLoad)
            state.cmsPage?.let { page ->
                Text(plainText(page.content), style = MaterialTheme.typography.bodyLarge)
                page.updatedAt?.let { Text("Updated $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
fun SafetyCenterScreen(
    state: SettingsUiState,
    onLoad: () -> Unit,
    onReport: () -> Unit,
    onBlocked: () -> Unit,
    onPrivacy: () -> Unit,
    onCommunities: () -> Unit,
    onPage: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val safetyPages = state.cmsPages.filter { page -> listOf(page.slug, page.title, page.category.orEmpty()).joinToString(" ").contains(Regex("safety|community|guideline|privacy|moderation", RegexOption.IGNORE_CASE)) }
    PageScaffold("Safety Center", "Safety controls and Admin-published guidance in one place.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Feedback(state, onLoad) }
            item { NavigationCard("Report a person or room", "Search by human-readable identity and submit a supported reason", onReport) }
            item { NavigationCard("Blocked users", "Review and unblock accounts you have blocked", onBlocked) }
            item { NavigationCard("Privacy", "Manage messaging, follow, invitation and visitor permissions", onPrivacy) }
            item { NavigationCard("Communities", "Return to community discovery and membership", onCommunities) }
            if (safetyPages.isNotEmpty()) item { Text("Safety guidance", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
            items(safetyPages, key = { it.slug }) { page -> NavigationCard(page.title, page.excerpt ?: "Published guidance") { onPage(page.slug) } }
        }
    }
}

@Composable
fun ReportScreen(
    state: SettingsUiState,
    contextualType: ReportTargetType?,
    contextualId: String?,
    contextualLabel: String?,
    onLoadContext: () -> Unit,
    onSearch: (String, ReportTargetType?) -> Unit,
    onSelect: (ReportTarget) -> Unit,
    onSubmit: (ReportReason, String?) -> Unit,
    onReloadReports: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(contextualType, contextualId) {
        onLoadContext()
        onReloadReports()
    }
    var type by remember { mutableStateOf(contextualType ?: ReportTargetType.USER) }
    var query by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf(ReportReason.HARASSMENT) }
    var description by remember { mutableStateOf("") }
    val contextual = contextualType != null && !contextualId.isNullOrBlank()
    PageScaffold("Report", "Reports are submitted to VoiceCloud moderation using backend-authoritative target IDs.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Feedback(state) }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Reported item", style = MaterialTheme.typography.titleLarge)
                        if (!contextual) {
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ReportTargetType.entries.forEach { option -> FilterChip(selected = type == option, onClick = { type = option; query = ""; onSearch("", option) }, label = { Text(if (option == ReportTargetType.USER) "Person" else "Room") }) }
                            }
                            OutlinedTextField(value = query, onValueChange = { query = it; onSearch(it, type) }, modifier = Modifier.fillMaxWidth(), label = { Text(if (type == ReportTargetType.USER) "Search people" else "Search rooms") }, placeholder = { Text("Enter a name, username or room title") }, singleLine = true)
                            state.reportTargets.forEach { target ->
                                OutlinedCard(onClick = { onSelect(target); query = target.label }, modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp)) { Text(target.label, fontWeight = FontWeight.SemiBold); target.secondaryLabel?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                                }
                            }
                        }
                        state.reportTarget?.let { target ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp)) { Text(target.label, fontWeight = FontWeight.Bold); Text(if (target.type == ReportTargetType.USER) "Person" else "Room", color = MaterialTheme.colorScheme.onPrimaryContainer); target.secondaryLabel?.let { Text(it, color = MaterialTheme.colorScheme.onPrimaryContainer) } }
                            }
                        } ?: contextualLabel?.takeIf(String::isNotBlank)?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Reason", style = MaterialTheme.typography.titleLarge)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReportReason.entries.forEach { option -> FilterChip(selected = reason == option, onClick = { reason = option }, label = { Text(option.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) }) }
                        }
                        OutlinedTextField(value = description, onValueChange = { description = it.take(1000) }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), label = { Text("Details (optional)") }, supportingText = { Text("${description.length}/1000") })
                        Button(onClick = { onSubmit(reason, description) }, enabled = !state.saving && state.reportTarget != null, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Submitting…" else "Submit report") }
                        Text("Evidence uploads are not offered because the canonical report contract accepts target, reason and optional description only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (state.reports.isNotEmpty()) item { Text("Your reports", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 6.dp)) }
            items(state.reports, key = { it.id }) { report ->
                ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(15.dp)) { Text(report.targetLabel, fontWeight = FontWeight.SemiBold); Text("${report.reason.replace('_', ' ')} · ${report.status}", color = MaterialTheme.colorScheme.onSurfaceVariant); report.createdAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) } } }
            }
        }
    }
}

@Composable
fun ContactSupportScreen(state: SettingsUiState, defaultName: String, defaultEmail: String, defaultPhone: String?, onSend: (String, String, String?, String) -> Unit, onBack: () -> Unit) {
    var name by remember(defaultName) { mutableStateOf(defaultName) }
    var email by remember(defaultEmail) { mutableStateOf(defaultEmail) }
    var phone by remember(defaultPhone) { mutableStateOf(defaultPhone.orEmpty()) }
    var message by remember { mutableStateOf("") }
    PageScaffold("Contact Support", "Send a persisted message to VoiceCloud Admin support.", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Feedback(state)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email address") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(value = message, onValueChange = { message = it.take(1000) }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp), supportingText = { Text("${message.length}/1000") })
            Button(onClick = { onSend(name, email, phone.takeIf(String::isNotBlank), message) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Sending…" else "Send message") }
            Text("The app does not promise live chat or fixed response times. Your message uses the server's Contact submission workflow.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AboutScreen(versionName: String, onHelp: () -> Unit, onBack: () -> Unit) {
    PageScaffold("About ${VoiceCloudBrand.name}", "Connecting people through high-quality live voice experiences.", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(VoiceCloudBrand.name, style = MaterialTheme.typography.headlineMedium)
                    Text("Android $versionName", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Professional live audio, communities and creator experiences backed by VoiceCloud's server-authoritative platform.", style = MaterialTheme.typography.bodyLarge)
                }
            }
            NavigationCard("Policies & information", "Open Admin-published legal and product pages", onHelp)
        }
    }
}

private fun plainText(value: String): String = Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().trim()
