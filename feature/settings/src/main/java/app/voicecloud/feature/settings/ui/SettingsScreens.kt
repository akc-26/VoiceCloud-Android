package app.voicecloud.feature.settings.ui

import android.text.Html
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudEmptyVisual
import app.voicecloud.core.designsystem.component.VoiceCloudGlossCard
import app.voicecloud.core.designsystem.component.VoiceCloudMetricTile
import app.voicecloud.core.designsystem.component.VoiceCloudPageHero
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.voiceCloudVisualFor
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.preferences.ThemePreference
import app.voicecloud.feature.settings.model.*

@Composable
private fun PageScaffold(title: String, subtitle: String? = null, onBack: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        containerColor = ConsumerColors.Cloud,
        topBar = { VoiceCloudPageTopBar(title = title, subtitle = subtitle, onBack = onBack) },
        content = content,
    )
}

@Composable
private fun Feedback(state: SettingsUiState, onRetry: (() -> Unit)? = null) {
    VoiceCloudToastEffect(state.error, state.notice)
    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
private fun SettingsVisualHero(title: String, subtitle: String, kind: VoiceCloudVisualKind = voiceCloudVisualFor(title)) {
    VoiceCloudPageHero(
        title = title,
        subtitle = subtitle,
        kind = kind,
        badge = "VoiceCloud · Secure & personal",
    )
}

@Composable
private fun NavigationCard(title: String, subtitle: String, onClick: () -> Unit) {
    VoiceCloudGlossCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        contentPadding = 15.dp,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VoiceCloudPictogram(voiceCloudVisualFor(title), size = 48.dp)
            Column(Modifier.weight(1f)) {
                Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = CircleShape, color = ConsumerColors.SapphireSoft) {
                Text(voiceCloudTitleCase("›"), Modifier.padding(horizontal = 11.dp, vertical = 5.dp), style = MaterialTheme.typography.headlineSmall, color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsOverviewScreen(
    onNotifications: () -> Unit,
    onPrivacy: () -> Unit,
    onVoiceAppearance: () -> Unit,
    onSecurity: () -> Unit,
    onBack: () -> Unit,
) {
    PageScaffold("Settings", "Your Personal Preferences And Security.", onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { VoiceCloudPageHero("Your VoiceCloud, Your Way", "Personalize notifications, privacy, audio, appearance and account security without losing the premium VoiceCloud experience.", VoiceCloudVisualKind.SETTINGS, badge = "Preferences & control") }
            item { NavigationCard("Notifications", "Choose How VoiceCloud Reaches You", onNotifications) }
            item { NavigationCard("Privacy", "Control Visibility And Interactions", onPrivacy) }
            item { NavigationCard("Voice & Appearance", "Tune Audio And App Appearance", onVoiceAppearance) }
            item { NavigationCard("Security & Devices", "Manage Sessions, Devices And Sign-In Activity", onSecurity) }
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
            SettingsVisualHero("Stay In The Conversation", "Choose the alerts that matter while keeping your VoiceCloud experience calm and intentional.", VoiceCloudVisualKind.NOTIFICATION)
            ToggleRow("Email Notifications", "Account and product messages delivered by email", value.email) { value = value.copy(email = it) }
            ToggleRow("Push Notifications", "Alerts delivered to this device", value.push) { value = value.copy(push = it) }
            ToggleRow("In-App Notifications", "Activity alerts while using VoiceCloud", value.inApp) { value = value.copy(inApp = it) }
            ToggleRow("Notification Sounds", "Play a sound for supported alerts", value.sound) { value = value.copy(sound = it) }
            Button(onClick = { onSave(value) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase(if (state.saving) "Saving…" else "Save Notification Preferences")) }
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            VoiceCloudPictogram(voiceCloudVisualFor(title), size = 42.dp)
            Column(Modifier.weight(1f)) {
                Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onChecked)
        }
    }
}

@Composable
fun PrivacySettingsScreen(
    state: SettingsUiState,
    onLoad: () -> Unit,
    onSave: (PrivacyPreferences) -> Unit,
    onBlocked: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    var value by remember { mutableStateOf(PrivacyPreferences()) }
    LaunchedEffect(state.privacy) { state.privacy?.let { value = it } }
    PageScaffold("Privacy", "Choose What Others Can See And Do.", onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { Feedback(state, onLoad) }
            item { SettingsVisualHero("Privacy, On Your Terms", "Control presence, direct messages and gift visibility with clear account-level safeguards.", VoiceCloudVisualKind.SECURITY) }
            item { ToggleRow("Show Online Status", "Let Others See When You’re Online", value.showOnlineStatus) { value = value.copy(showOnlineStatus = it) } }
            item { ToggleRow("Show Last Seen", "Let Others See Your Recent Activity", value.showLastSeen) { value = value.copy(showLastSeen = it) } }
            item { ToggleRow("Allow Direct Messages", "Allow Other Members To Message You", value.allowDirectMessages) { value = value.copy(allowDirectMessages = it) } }
            item { ToggleRow("Show Gifts", "Show Gift Activity On Your Profile", value.showGifts) { value = value.copy(showGifts = it) } }
            item { OutlinedButton(onClick = onBlocked, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Blocked Users")) } }
            item {
                Button(
                    onClick = { onSave(value) },
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(voiceCloudTitleCase(if (state.saving) "Saving…" else "Save Privacy Settings")) }
            }
        }
    }
}

@Composable
private fun PermissionPicker(title: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    VoiceCloudGlossCard(Modifier.fillMaxWidth()) {
        Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(selected = selected.equals(option, true), onClick = { onSelected(option) }, label = { Text(voiceCloudTitleCase(option.replaceFirstChar { it.uppercase() })) })
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
    PageScaffold("Voice & Appearance", "Audio processing and visual preferences.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Feedback(state, onLoad) }
            item { SettingsVisualHero("Sound & Style", "Fine-tune your audio processing and visual mode for a comfortable premium listening experience.", VoiceCloudVisualKind.AUDIO) }
            item {
                VoiceCloudGlossCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(voiceCloudTitleCase("Appearance"), style = MaterialTheme.typography.titleLarge)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePreference.entries.forEach { option ->
                                FilterChip(selected = theme == option, onClick = { theme = option }, label = { Text(voiceCloudTitleCase(option.name.lowercase().replaceFirstChar { it.uppercase() })) })
                            }
                        }
                        Text(voiceCloudTitleCase("Language"), style = MaterialTheme.typography.titleMedium)
                        Text(voiceCloudTitleCase("English"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(voiceCloudTitleCase("English Is The Currently Supported Product Language."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { onSaveTheme(theme) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Apply Appearance")) }
                    }
                }
            }
            item { ToggleRow("Noise suppression", "Reduce supported background noise during voice sessions", voice.noiseSuppression) { voice = voice.copy(noiseSuppression = it) } }
            item { ToggleRow("Echo cancellation", "Reduce supported speaker and room echo", voice.echoCancellation) { voice = voice.copy(echoCancellation = it) } }
            item { ToggleRow("Automatic gain control", "Let supported audio sessions balance microphone gain", voice.agc) { voice = voice.copy(agc = it) } }
            if (voice.audioPreset.isNotBlank()) item {
                ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(voiceCloudTitleCase("Audio Preset"), style = MaterialTheme.typography.titleMedium); Text(voice.audioPreset, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            item { Button(onClick = { onSaveVoice(voice) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase(if (state.saving) "Saving…" else "Save voice preferences")) } }
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
            item { SettingsVisualHero("Protected By Design", "Review sessions, devices and recent sign-ins without exposing credentials or sensitive tokens.", VoiceCloudVisualKind.SECURITY) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("${snapshot?.sessions?.size ?: 0}", "Sessions", Modifier.weight(1f))
                    MetricCard("${snapshot?.devices?.size ?: 0}", "Devices", Modifier.weight(1f))
                    MetricCard("${snapshot?.history?.size ?: 0}", "Recent", Modifier.weight(1f))
                }
            }
            item { NavigationCard("Sessions & devices", "Inspect current account access and revoke individual entries", onSessionsDevices) }
            item { NavigationCard("Login Activity", "Review up to 100 recent authentication events", onLoginActivity) }
            item {
                OutlinedButton(onClick = onSignOutAll, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(voiceCloudTitleCase("Sign Out All Sessions"))
                }
            }
            item { Text(voiceCloudTitleCase("Password Change, Two-Factor Authentication And Trusted-Device Controls Are Not Shown Because The Current Mobile Backend Contract Does Not Provide Those Mutations."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    VoiceCloudMetricTile(value = value, label = label, modifier = modifier, kind = VoiceCloudVisualKind.SECURITY)
}

@Composable
fun SessionsDevicesScreen(state: SettingsUiState, onLoad: () -> Unit, onSession: (String) -> Unit, onDevice: (String) -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    PageScaffold("Sessions & devices", "Only safe account-facing metadata is displayed.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Feedback(state, onLoad) }
            item { SettingsVisualHero("Trusted Sessions & Devices", "See where your account is active and revoke access from devices you no longer use.", VoiceCloudVisualKind.SECURITY) }
            item { Text(voiceCloudTitleCase("Active Sessions"), style = MaterialTheme.typography.titleLarge) }
            val sessions = state.security?.sessions.orEmpty()
            if (!state.loading && sessions.isEmpty()) item { EmptyCard("No active sessions were returned.") }
            items(sessions, key = { it.id }) { session ->
                NavigationCard(
                    if (session.isCurrent) "${session.deviceName ?: session.deviceType ?: "Current session"} · Current" else session.deviceName ?: session.deviceType ?: "Session",
                    listOfNotNull(session.status, session.lastActiveAt).joinToString(" · ").ifBlank { "Session details" },
                ) { onSession(session.id) }
            }
            item { Text(voiceCloudTitleCase("Registered Devices"), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
            val devices = state.security?.devices.orEmpty()
            if (!state.loading && devices.isEmpty()) item { EmptyCard("No registered devices were returned.") }
            items(devices, key = { it.id }) { device ->
                NavigationCard(
                    if (device.isCurrent) "${device.deviceName ?: device.model ?: device.deviceType ?: "Current device"} · Current" else device.deviceName ?: device.model ?: device.deviceType ?: "Device",
                    listOfNotNull(device.status, device.lastUsedAt).joinToString(" · ").ifBlank { "Device details" },
                ) { onDevice(device.deviceId ?: device.id) }
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String) { VoiceCloudEmptyVisual("Nothing Here Yet", message, Modifier.fillMaxWidth(), VoiceCloudVisualKind.SECURITY) }

@Composable
fun SessionDetailScreen(state: SettingsUiState, id: String, onLoad: () -> Unit, onRevoke: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(id) { onLoad() }
    val session = state.session
    PageScaffold("Session detail", if (session?.isCurrent == true) "Current session" else null, onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Feedback(state, onLoad)
            SettingsVisualHero("Session Details", "Review safe session metadata and revoke access when something does not look familiar.", VoiceCloudVisualKind.SECURITY)
            session?.let {
                DetailRows(listOf(
                    "Device" to (it.deviceName ?: it.deviceType),
                    "Status" to it.status,
                    "IP address" to it.ipAddress,
                    "Last active" to it.lastActiveAt,
                    "Expires" to it.expiresAt,
                    "User agent" to it.userAgent,
                ))
                if (!it.isCurrent) Button(onClick = onRevoke, enabled = !state.saving, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(voiceCloudTitleCase("Revoke This Session")) }
                else Text(voiceCloudTitleCase("The Current Session Cannot Be Revoked From Its Own Detail Page. Use Sign Out Or Sign Out All Sessions Instead."), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            SettingsVisualHero("Device Details", "Review this device’s safe account metadata and access state.", VoiceCloudVisualKind.SECURITY)
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
                if (!it.isCurrent) Button(onClick = onRevoke, enabled = !state.saving, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(voiceCloudTitleCase("Remove This Device")) }
                else Text(voiceCloudTitleCase("This Is The Current Device. Removing It Here Is Disabled To Avoid Invalidating The Active Session Unexpectedly."), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailRows(rows: List<Pair<String, String?>>) {
    rows.filter { !it.second.isNullOrBlank() }.forEach { (label, value) ->
        VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 13.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VoiceCloudPictogram(voiceCloudVisualFor(label), size = 38.dp)
                Column(Modifier.weight(1f)) {
                    Text(voiceCloudTitleCase(label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value.orEmpty(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun LoginActivityScreen(state: SettingsUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    PageScaffold("Login Activity", "Recent server-authoritative account authentication events.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Feedback(state, onLoad) }
            item { SettingsVisualHero("Recent Sign-Ins", "A clear history of server-authoritative authentication activity for your account.", VoiceCloudVisualKind.ACTIVITY) }
            val history = state.security?.history.orEmpty()
            if (!state.loading && history.isEmpty()) item { EmptyCard("No login activity was returned.") }
            items(history, key = { it.id.ifBlank { "${it.action}:${it.createdAt}:${it.ipAddress}" } }) { item ->
                VoiceCloudGlossCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.ACTIVITY, size = 42.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.action.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val meta = listOfNotNull(item.loginMethod, item.platform, item.country, item.ipAddress).filter(String::isNotBlank)
                        if (meta.isNotEmpty()) Text(meta.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        item.createdAt?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpCenterScreen(state: SettingsUiState, onLoad: () -> Unit, onPage: (String) -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    PageScaffold("Help & Legal", "Published content is managed by VoiceCloud Admin.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Feedback(state, onLoad) }
            item { SettingsVisualHero("Help, Policies & Guidance", "Explore VoiceCloud support content, safety guidance and published legal information.", VoiceCloudVisualKind.HELP) }
            if (!state.loading && state.cmsPages.isEmpty()) item { EmptyCard("No published help or policy pages are available right now.") }
            items(state.cmsPages, key = { it.slug }) { page -> NavigationCard(page.title, page.excerpt ?: page.category ?: "Published information") { onPage(page.slug) } }
        }
    }
}

@Composable
fun CmsContentScreen(state: SettingsUiState, slug: String, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(slug) { onLoad() }
    PageScaffold(state.cmsPage?.title ?: "Information", null, onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Feedback(state, onLoad)
            SettingsVisualHero(state.cmsPage?.title ?: "VoiceCloud Information", "Official information published and maintained by VoiceCloud.", VoiceCloudVisualKind.HELP)
            state.cmsPage?.let { page ->
                Text(plainText(page.content), style = MaterialTheme.typography.bodyLarge)
                page.updatedAt?.let { Text(voiceCloudTitleCase("Updated $it"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
    onPage: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val safetyPages = state.cmsPages.filter { page -> listOf(page.slug, page.title, page.category.orEmpty()).joinToString(" ").contains(Regex("safety|guideline|privacy|moderation", RegexOption.IGNORE_CASE)) }
    PageScaffold("Safety Center", "Safety controls and Admin-published guidance in one place.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Feedback(state, onLoad) }
            item { SettingsVisualHero("Safe Conversations", "Report concerns, manage blocked users and review community safety guidance in one trusted place.", VoiceCloudVisualKind.SECURITY) }
            item { NavigationCard("Report A Person Or Room", "Search by human-readable identity and submit a supported reason", onReport) }
            item { NavigationCard("Blocked Users", "Review and unblock accounts you have blocked", onBlocked) }
            if (safetyPages.isNotEmpty()) item { Text(voiceCloudTitleCase("Safety Guidance"), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
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
    PageScaffold("Report", "Tell Us What Happened. Internal IDs Stay Hidden.", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Feedback(state) }
            item { SettingsVisualHero("Report With Confidence", "Choose the person or room, select a supported reason and add clear details for review.", VoiceCloudVisualKind.SECURITY) }
            item {
                VoiceCloudGlossCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(voiceCloudTitleCase("Reported Item"), style = MaterialTheme.typography.titleLarge)
                        if (!contextual) {
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ReportTargetType.entries.forEach { option -> FilterChip(selected = type == option, onClick = { type = option; query = ""; onSearch("", option) }, label = { Text(voiceCloudTitleCase(if (option == ReportTargetType.USER) "Person" else "Room")) }) }
                            }
                            OutlinedTextField(value = query, onValueChange = { query = it; onSearch(it, type) }, modifier = Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase(if (type == ReportTargetType.USER) "Search people" else "Search rooms")) }, placeholder = { Text(voiceCloudTitleCase("Enter A Name, Username Or Room Title")) }, singleLine = true)
                            state.reportTargets.forEach { target ->
                                OutlinedCard(onClick = { onSelect(target); query = target.label }, modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp)) { Text(target.label, fontWeight = FontWeight.SemiBold); target.secondaryLabel?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                                }
                            }
                        }
                        state.reportTarget?.let { target ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp)) { Text(target.label, fontWeight = FontWeight.Bold); Text(voiceCloudTitleCase(if (target.type == ReportTargetType.USER) "Person" else "Room"), color = MaterialTheme.colorScheme.onPrimaryContainer); target.secondaryLabel?.let { Text(it, color = MaterialTheme.colorScheme.onPrimaryContainer) } }
                            }
                        } ?: contextualLabel?.takeIf(String::isNotBlank)?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            item {
                VoiceCloudGlossCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.SECURITY, size = 44.dp)
                        Column(Modifier.weight(1f)) {
                            Text(voiceCloudTitleCase("Reason"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Text(voiceCloudTitleCase("Select the closest reason and add helpful context."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReportReason.entries.forEach { option -> FilterChip(selected = reason == option, onClick = { reason = option }, label = { Text(voiceCloudTitleCase(option.name.replace('_', ' ').lowercase())) }) }
                    }
                    OutlinedTextField(value = description, onValueChange = { description = it.take(1000) }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), label = { Text(voiceCloudTitleCase("Details (Optional)")) }, supportingText = { Text(voiceCloudTitleCase("${description.length}/1000")) })
                    Button(onClick = { onSubmit(reason, description) }, enabled = !state.saving && state.reportTarget != null, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase(if (state.saving) "Submitting…" else "Submit report")) }
                    Text(voiceCloudTitleCase("Add Clear Details To Help Our Safety Team Review Your Report."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.reports.isNotEmpty()) item { Text(voiceCloudTitleCase("Your Reports"), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 6.dp)) }
            items(state.reports, key = { it.id }) { report ->
                VoiceCloudGlossCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VoiceCloudPictogram(VoiceCloudVisualKind.SECURITY, size = 42.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(report.targetLabel, fontWeight = FontWeight.ExtraBold)
                            Text(voiceCloudTitleCase("${report.reason.replace('_', ' ')} · ${report.status}"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            report.createdAt?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
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
    PageScaffold("Contact Support", "Tell Us How We Can Help.", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Feedback(state)
            SettingsVisualHero("We’re Here To Help", "Send a support request directly to VoiceCloud with the details our team needs to respond.", VoiceCloudVisualKind.HELP)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(voiceCloudTitleCase("Full Name")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(voiceCloudTitleCase("Email Address")) }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(voiceCloudTitleCase("Phone (Optional)")) }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(value = message, onValueChange = { message = it.take(1000) }, label = { Text(voiceCloudTitleCase("Message")) }, modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp), supportingText = { Text(voiceCloudTitleCase("${message.length}/1000")) })
            Button(onClick = { onSend(name, email, phone.takeIf(String::isNotBlank), message) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase(if (state.saving) "Sending…" else "Send Message")) }
            Text(voiceCloudTitleCase("Your Message Goes Directly To VoiceCloud Support."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AboutScreen(versionName: String, onBack: () -> Unit) {
    PageScaffold("About ${VoiceCloudBrand.name}", "Live Voice. Real Connections.", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            VoiceCloudPageHero(
                title = VoiceCloudBrand.name,
                subtitle = "A live voice community built for listeners and creators. Android $versionName",
                kind = VoiceCloudVisualKind.AUDIO,
                badge = "Live audio · Real connections",
            )
        }
    }
}

private fun plainText(value: String): String = Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().trim()
