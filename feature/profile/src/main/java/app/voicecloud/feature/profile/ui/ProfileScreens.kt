package app.voicecloud.feature.profile.ui

import android.media.MediaPlayer
import android.net.Uri
import android.text.Html
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.VoiceCloudBrand
import app.voicecloud.core.designsystem.theme.VoiceCloudPageMetrics
import app.voicecloud.feature.profile.model.*
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun ProfileToolsScreen(
    state: ProfileUiState,
    onLoad: () -> Unit,
    onReplays: () -> Unit,
    onActivity: () -> Unit,
    onVisitors: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    ProfilePage(title = "Activity & Replays", subtitle = "Your Replays, Room Activity And Profile Visitors.", onBack = onBack) {
        StatusCards(state)
        state.profile?.let { profile ->
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    RemoteMedia(
                        url = profile.coverUrl,
                        contentDescription = "Profile cover",
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        fallbackLabel = VoiceCloudBrand.name,
                    )
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        RemoteMedia(
                            url = profile.avatarUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            fallbackLabel = profile.displayName.ifBlank { profile.username },
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(profile.displayName.ifBlank { profile.username }, style = MaterialTheme.typography.titleLarge)
                            if (profile.username.isNotBlank()) Text(voiceCloudTitleCase("@${profile.username}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(voiceCloudTitleCase("Profile Complete · ${profile.profileCompletionPercentage}%"), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        ProfileToolButton("Replays", "Listen to recordings you are authorized to access.", onReplays)
        ProfileToolButton("My activity", "Review your recent room participation.", onActivity)
        ProfileToolButton("Profile visitors", "See backend-authorized visitor history and statistics.", onVisitors)
    }
}

@Composable
fun EditProfileScreen(
    state: ProfileUiState,
    identityUsername: String,
    identityEmail: String?,
    identityPhone: String?,
    onLoad: () -> Unit,
    onSave: (UpdateExtendedProfileBody) -> Unit,
    onUploadAvatar: (ByteArray, String, String) -> Unit,
    onDeleteAvatar: () -> Unit,
    onUploadCover: (ByteArray, String, String) -> Unit,
    onDeleteCover: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    val profile = state.profile
    var bio by remember(profile?.bio) { mutableStateOf(profile?.bio.orEmpty()) }
    var country by remember(profile?.country) { mutableStateOf(profile?.country.orEmpty()) }
    var interests by remember(profile?.interests) { mutableStateOf(profile?.interests.orEmpty().joinToString(", ")) }
    val context = LocalContext.current
    var pendingAvatar by remember { mutableStateOf<Uri?>(null) }
    var pendingCover by remember { mutableStateOf<Uri?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> pendingAvatar = uri }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> pendingCover = uri }

    pendingAvatar?.let { uri ->
        ProfileImageCropDialog(
            context = context,
            uri = uri,
            kind = ProfileCropKind.AVATAR,
            onDismiss = { pendingAvatar = null },
            onConfirm = { media ->
                pendingAvatar = null
                onUploadAvatar(media.bytes, media.fileName, media.mimeType)
            },
        )
    }
    pendingCover?.let { uri ->
        ProfileImageCropDialog(
            context = context,
            uri = uri,
            kind = ProfileCropKind.COVER,
            onDismiss = { pendingCover = null },
            onConfirm = { media ->
                pendingCover = null
                onUploadCover(media.bytes, media.fileName, media.mimeType)
            },
        )
    }

    ProfilePage(title = "Edit profile", subtitle = "Only backend-supported profile fields are editable here.", onBack = onBack) {
        StatusCards(state)
        if (profile == null && !state.loading) {
            EmptyCard("Profile unavailable", "Your profile could not be loaded from VoiceCloud.")
        }
        if (profile != null) {
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    RemoteMedia(profile.coverUrl, "Profile cover", Modifier.fillMaxWidth().height(170.dp), profile.displayName)
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        RemoteMedia(profile.avatarUrl, "Profile photo", Modifier.size(72.dp).clip(CircleShape), profile.displayName.ifBlank { identityUsername })
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                enabled = !state.mutating,
                            ) { Text(voiceCloudTitleCase("Change Photo")) }
                            Text(voiceCloudTitleCase("Preview And Adjust Any Image Before Uploading."), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!profile.avatarUrl.isNullOrBlank()) {
                                TextButton(onClick = onDeleteAvatar, enabled = !state.mutating) { Text(voiceCloudTitleCase("Remove Photo")) }
                            }
                        }
                    }
                    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                enabled = !state.mutating,
                                modifier = Modifier.weight(1f),
                            ) { Text(voiceCloudTitleCase("Change Cover")) }
                            if (!profile.coverUrl.isNullOrBlank()) {
                                TextButton(onClick = onDeleteCover, enabled = !state.mutating, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Remove Cover")) }
                            }
                        }
                        Text(voiceCloudTitleCase("Any Image Size Is Accepted And Resized After Your Crop."), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            ReadOnlyIdentityField("Username", identityUsername.ifBlank { profile.username })
            identityEmail?.takeIf { it.isNotBlank() }?.let { ReadOnlyIdentityField("Email", it) }
            identityPhone?.takeIf { it.isNotBlank() }?.let { ReadOnlyIdentityField("Phone", it) }

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it.take(300) },
                label = { Text(voiceCloudTitleCase("Bio")) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
            )
            OutlinedTextField(
                value = country,
                onValueChange = { country = it.take(80) },
                label = { Text(voiceCloudTitleCase("Country")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = interests,
                onValueChange = { interests = it.take(240) },
                label = { Text(voiceCloudTitleCase("Interests")) },
                supportingText = { Text(voiceCloudTitleCase("Separate Interests With Commas")) },
                modifier = Modifier.fillMaxWidth(),
            )
            ReadOnlyIdentityField("Language", profile.preferredLanguage.orEmpty().ifBlank { "en" })
            Button(
                onClick = {
                    onSave(
                        UpdateExtendedProfileBody(
                            bio = bio,
                            country = country,
                            preferredLanguage = profile.preferredLanguage?.ifBlank { "en" } ?: "en",
                            interests = interests.split(',').map(String::trim).filter(String::isNotBlank),
                        )
                    )
                },
                enabled = !state.mutating,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(voiceCloudTitleCase(if (state.mutating) "Saving…" else "Save profile")) }
        }
    }
}

@Composable
fun ReplayLibraryScreen(
    state: ProfileUiState,
    onLoad: () -> Unit,
    onReplay: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    ProfilePage(title = "Replays", subtitle = "Recordings are shown only as authorized by VoiceCloud.", onBack = onBack) {
        StatusCards(state)
        if (!state.loading && state.replays.isEmpty()) EmptyCard("No replays", "Available room recordings will appear here.")
        state.replays.forEach { replay ->
            ElevatedCard(onClick = { onReplay(replay.id) }, shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    RemoteMedia(replay.coverUrl, "Replay artwork", Modifier.size(82.dp).clip(RoundedCornerShape(18.dp)), replay.title)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(replay.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        replay.hostName?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Text(replayStatusLabel(replay), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ReplayPlayerScreen(
    state: ProfileUiState,
    replayId: String,
    onLoad: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(replayId) { onLoad(replayId) }
    val replay = state.selectedReplay
    ProfilePage(title = "Replay", subtitle = replay?.title, onBack = onBack) {
        StatusCards(state)
        if (replay != null) {
            RemoteMedia(replay.coverUrl, "Replay artwork", Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)), replay.title)
            Text(replay.title, style = MaterialTheme.typography.headlineMedium)
            replay.hostName?.let { Text(voiceCloudTitleCase("Hosted By $it"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            val status = replay.status.uppercase()
            val explicitlyDenied = replay.accessAllowed == false
            val processing = status in setOf("PROCESSING", "PENDING", "QUEUED")
            val failed = status in setOf("FAILED", "ERROR", "UNAVAILABLE")
            when {
                explicitlyDenied -> EmptyCard("Replay access restricted", replay.accessReason ?: "Your account is not authorized to play this recording.")
                processing -> EmptyCard("Replay processing", "This recording is still being prepared.")
                failed -> EmptyCard("Replay unavailable", replay.accessReason ?: "VoiceCloud could not make this recording available.")
                replay.playbackUrl.isNullOrBlank() -> EmptyCard("Replay unavailable", "No playable recording is available from the server.")
                else -> ReplayAudioControls(url = replay.playbackUrl)
            }
            if (replay.durationSeconds > 0) Text(voiceCloudTitleCase("Duration · ${formatDuration(replay.durationSeconds)}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            replay.createdAt?.let { Text(voiceCloudTitleCase("Recorded · ${formatTimestamp(it)}"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else if (!state.loading) {
            EmptyCard("Replay unavailable", "This recording could not be loaded.")
        }
    }
}

@Composable
fun ActivityHistoryScreen(state: ProfileUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    ProfilePage(title = "My activity", subtitle = "Recent room participation from the VoiceCloud activity ledger.", onBack = onBack) {
        StatusCards(state)
        if (!state.loading && state.activity.isEmpty()) EmptyCard("No recent activity", "Your room activity will appear here after you participate.")
        state.activity.forEach { item ->
            ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(item.roomTitle ?: item.title, style = MaterialTheme.typography.titleMedium)
                    item.hostName?.let { Text(voiceCloudTitleCase("Host · $it"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text(item.type.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    val time = item.joinedAt ?: item.createdAt
                    time?.let { Text(formatTimestamp(it), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (item.durationSeconds > 0) Text(voiceCloudTitleCase("Duration · ${formatDuration(item.durationSeconds)}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun BlockedUsersScreen(
    state: ProfileUiState,
    onLoad: () -> Unit,
    onUnblock: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    ProfilePage(title = "Blocked users", subtitle = "Unblocking is applied by VoiceCloud immediately.", onBack = onBack) {
        StatusCards(state)
        if (!state.loading && state.blockedUsers.isEmpty()) EmptyCard("No blocked users", "Accounts you block will appear here.")
        state.blockedUsers.forEach { entry ->
            PersonCard(entry.person, trailing = {
                OutlinedButton(
                    onClick = { onUnblock(entry.blockedUserId.ifBlank { entry.person.id }) },
                    enabled = !state.mutating,
                ) { Text(voiceCloudTitleCase("Unblock")) }
            })
        }
    }
}

@Composable
fun ProfileVisitorsScreen(state: ProfileUiState, onLoad: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) { onLoad() }
    ProfilePage(title = "Profile visitors", subtitle = "Visitor visibility follows server privacy rules.", onBack = onBack) {
        StatusCards(state)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(state.visitorStats.total.toString(), "Total", Modifier.weight(1f))
            MetricCard(state.visitorStats.today.toString(), "Today", Modifier.weight(1f))
            MetricCard(state.visitorStats.thisWeek.toString(), "Week", Modifier.weight(1f))
        }
        if (!state.loading && state.visitors.isEmpty()) EmptyCard("No visitors yet", "Authorized profile visits will appear here.")
        state.visitors.forEach { visit ->
            PersonCard(visit.person, subtitle = visit.visitedAt?.let(::formatTimestamp))
        }
    }
}

@Composable
fun HelpPagesScreen(
    state: ProfileUiState,
    onLoad: () -> Unit,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    ProfilePage(title = "Help & information", subtitle = "Terms, help and information published by VoiceCloud.", onBack = onBack) {
        StatusCards(state)
        if (!state.loading && state.helpPages.isEmpty()) {
            EmptyCard("No help pages available", "Published help and legal pages will appear here.")
        }
        state.helpPages.forEach { page ->
            ElevatedCard(onClick = { onOpen(page.slug) }, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(page.title, style = MaterialTheme.typography.titleMedium)
                    page.category?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium) }
                    page.excerpt?.takeIf { it.isNotBlank() }?.let { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
fun HelpPageScreen(
    state: ProfileUiState,
    slug: String,
    onLoad: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(slug) { onLoad(slug) }
    val page = state.selectedHelpPage
    ProfilePage(title = page?.title ?: "Help", onBack = onBack) {
        StatusCards(state)
        if (page != null) {
            val readable = remember(page.content) {
                Html.fromHtml(page.content, Html.FROM_HTML_MODE_LEGACY).toString().trim()
            }
            if (readable.isBlank()) EmptyCard("Content unavailable", "This page does not currently contain published content.")
            else Text(readable, style = MaterialTheme.typography.bodyLarge)
            page.updatedAt?.let { Text(voiceCloudTitleCase("Updated · ${formatTimestamp(it)}"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else if (!state.loading) {
            EmptyCard("Page unavailable", "This page could not be loaded.")
        }
    }
}

@Composable
private fun ProfilePage(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val metrics = VoiceCloudPageMetrics.current()
    Scaffold(topBar = { VoiceCloudPageTopBar(title, subtitle, onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                start = metrics.horizontalPadding,
                end = metrics.horizontalPadding,
                top = metrics.contentTopSpacing,
                bottom = metrics.contentBottomSpacing,
            ),
            verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing), content = content)
            }
        }
    }
}

@Composable
private fun ProfileToolButton(title: String, subtitle: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleMedium)
            Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusCards(state: ProfileUiState) {
    VoiceCloudToastEffect(state.error, state.notice)
    if (state.loading || state.mutating) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
private fun EmptyCard(title: String, body: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleMedium)
            Text(voiceCloudTitleCase(body), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReadOnlyIdentityField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(voiceCloudTitleCase(label)) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PersonCard(person: PersonSummary, subtitle: String? = null, trailing: (@Composable () -> Unit)? = null) {
    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RemoteMedia(
                url = person.avatarUrl,
                contentDescription = "Profile photo",
                modifier = Modifier.size(52.dp).clip(CircleShape),
                fallbackLabel = person.displayName.ifBlank { person.username.ifBlank { VoiceCloudBrand.name } },
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(person.displayName.ifBlank { person.username.ifBlank { "VoiceCloud member" } }, style = MaterialTheme.typography.titleMedium)
                if (person.username.isNotBlank()) Text(voiceCloudTitleCase("@${person.username}"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(voiceCloudTitleCase(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RemoteMedia(
    url: String?,
    contentDescription: String,
    modifier: Modifier,
    fallbackLabel: String,
) {
    val fallback = fallbackLabel.trim().take(1).uppercase().ifBlank { "V" }
    if (url.isNullOrBlank()) {
        MediaFallback(fallback, modifier)
        return
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        contentScale = ContentScale.Crop,
    ) {
        val mediaState by painter.state.collectAsState()
        when (mediaState) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            is AsyncImagePainter.State.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp)) }
            else -> MediaFallback(fallback, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun MediaFallback(label: String, modifier: Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReplayAudioControls(url: String) {
    var prepared by remember(url) { mutableStateOf(false) }
    var playing by remember(url) { mutableStateOf(false) }
    var error by remember(url) { mutableStateOf<String?>(null) }
    val playerResult = remember(url) {
        runCatching {
            MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener {
                    prepared = true
                    error = null
                }
                setOnCompletionListener { playing = false }
                setOnErrorListener { _, _, _ ->
                    playing = false
                    error = "Replay playback is unavailable."
                    true
                }
                prepareAsync()
            }
        }
    }
    val player = playerResult.getOrNull()
    VoiceCloudToastEffect(error ?: if (player == null) "Replay Playback Is Unavailable" else null, null)
    player?.let { mediaPlayer ->
        DisposableEffect(mediaPlayer) {
            onDispose { runCatching { mediaPlayer.release() } }
        }
    }

    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(voiceCloudTitleCase("Audio Replay"), style = MaterialTheme.typography.titleMedium)
            if (player == null) {
                Text(voiceCloudTitleCase("Replay Unavailable"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                if (!prepared && error == null) LinearProgressIndicator(Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        if (!prepared) return@Button
                        if (playing) {
                            runCatching { player.pause() }
                            playing = false
                        } else {
                            runCatching { player.start() }
                                .onSuccess { playing = true }
                                .onFailure { error = "Replay playback is unavailable." }
                        }
                    },
                    enabled = prepared && error == null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(voiceCloudTitleCase(if (playing) "Pause replay" else "Play replay")) }
            }
        }
    }
}

private fun replayStatusLabel(replay: ReplayItem): String {
    val parts = mutableListOf<String>()
    replay.status.takeIf { it.isNotBlank() }?.let { parts += it.lowercase().replaceFirstChar { it.titlecase() } }
    if (replay.durationSeconds > 0) parts += formatDuration(replay.durationSeconds)
    replay.createdAt?.let { parts += formatTimestamp(it) }
    return parts.joinToString(" · ").ifBlank { "Replay" }
}

private fun formatDuration(totalSeconds: Long): String {
    val seconds = max(totalSeconds, 0)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remaining = seconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, remaining) else "%d:%02d".format(minutes, remaining)
}

private fun formatTimestamp(raw: String): String = runCatching {
    OffsetDateTime.parse(raw).format(DateTimeFormatter.ofPattern("dd MMM yyyy · hh:mm a"))
}.getOrDefault(raw)
