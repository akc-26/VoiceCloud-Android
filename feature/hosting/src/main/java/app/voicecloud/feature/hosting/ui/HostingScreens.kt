package app.voicecloud.feature.hosting.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.theme.ConsumerBrushes
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.VoiceCloudPageMetrics
import app.voicecloud.feature.hosting.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
private fun HostPage(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val metrics = VoiceCloudPageMetrics.current()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { VoiceCloudPageTopBar(title, subtitle, onBack) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = metrics.horizontalPadding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing),
            content = content,
        )
    }
}

@Composable
private fun HostingStatus(state: HostingUiState) {
    when {
        state.loading -> Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.error != null -> Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Text(state.error, Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer)
        }
        state.notice != null -> Card(colors = CardDefaults.cardColors(containerColor = ConsumerColors.SapphireSoft)) {
            Text(state.notice, Modifier.padding(14.dp), color = ConsumerColors.Text)
        }
    }
}

@Composable
fun HostStudioScreen(
    state: HostingUiState,
    onLoad: () -> Unit,
    onCreateRoom: () -> Unit,
    onScheduleRoom: () -> Unit,
    onRoom: (String) -> Unit,
    onSchedule: (String) -> Unit,
    onStartScheduled: (ScheduledHostRoom) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    HostPage("Host Studio", "Create, schedule and manage your audio rooms.", onBack) {
        HostingStatus(state)
        if (state.eligibilityChecked) {
            val approved = state.hostProfile?.status.equals("APPROVED", true)
            if (!approved) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Host access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(state.eligibility?.reasons?.firstOrNull() ?: "An approved Host profile is required before you can create or broadcast rooms.")
                        state.eligibility?.reasons.orEmpty().drop(1).take(3).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                        state.eligibility?.let { eligibility ->
                            if (eligibility.applicationsEnabled && eligibility.eligible) {
                                Text("Your account currently meets the Host eligibility requirements.", color = ConsumerColors.SapphireDeep)
                            }
                        }
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onCreateRoom, modifier = Modifier.weight(1f)) { Text("Create room", maxLines = 1) }
                    OutlinedButton(onClick = onScheduleRoom, modifier = Modifier.weight(1f)) { Text("Schedule", maxLines = 1) }
                }
                Text("My rooms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (state.rooms.isEmpty() && !state.loading) Text("No rooms yet. Create your first room when you are ready to host.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.rooms.forEach { room -> HostRoomCard(room, { onRoom(room.id) }) }
                Text("Scheduled", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (state.schedules.isEmpty() && !state.loading) Text("No upcoming rooms.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.schedules.forEach { schedule ->
                    Card(Modifier.fillMaxWidth().clickable { onSchedule(schedule.id) }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(schedule.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatSchedule(schedule.scheduledStartTime, schedule.timeZone), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(schedule.visibility, style = MaterialTheme.typography.labelMedium)
                                if (schedule.isPremium) Text("Ticketed", style = MaterialTheme.typography.labelMedium, color = ConsumerColors.SapphireDeep)
                            }
                            Button(onClick = { onStartScheduled(schedule) }) { Text("Start room") }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HostRoomCard(room: HostRoom, onOpen: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(room.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(room.category + " · " + room.status.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (room.isPremium || room.isTicketRequired) Text("Ticket access enabled", style = MaterialTheme.typography.labelMedium, color = ConsumerColors.SapphireDeep)
            }
            Text("Open", color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RoomEditorScreen(
    state: HostingUiState,
    roomId: String?,
    onLoad: (String) -> Unit,
    onSave: (RoomEditorInput) -> Unit,
    onBack: () -> Unit,
) {
    if (roomId != null) LaunchedEffect(roomId) { onLoad(roomId) }
    val existing = if (roomId != null) state.selectedRoom?.takeIf { it.id == roomId } else null
    var title by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var description by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var category by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.category ?: "Audio Lounge") }
    var privateRoom by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.isPrivate ?: false) }
    var locked by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.isLocked ?: false) }
    var inviteOnly by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.isInviteOnly ?: false) }
    var subscriberOnly by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.isSubscriberOnly ?: false) }
    var verifiedOnly by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.isVerifiedOnly ?: false) }
    var ticketed by rememberSaveable(roomId, existing?.id) { mutableStateOf((existing?.isPremium == true) || (existing?.isTicketRequired == true)) }
    var price by rememberSaveable(roomId, existing?.id) { mutableStateOf(existing?.ticketPriceAmount?.toString()?.takeIf { it != "null" } ?: "0") }
    HostPage(if (roomId == null) "Create Room" else "Room Settings", "Room access and ticket settings stay server-authoritative.", onBack) {
        HostingStatus(state)
        OutlinedTextField(title, { title = it }, label = { Text("Room title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(category, { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        SwitchLine("Private room", privateRoom) { privateRoom = it; if (it) inviteOnly = true }
        SwitchLine("Lock room entry", locked) { locked = it }
        SwitchLine("Invite only", inviteOnly) { inviteOnly = it }
        SwitchLine("Subscribers only", subscriberOnly) { subscriberOnly = it }
        SwitchLine("Verified listeners only", verifiedOnly) { verifiedOnly = it }
        SwitchLine("Ticketed / premium access", ticketed) { ticketed = it }
        if (ticketed) OutlinedTextField(price, { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Ticket price") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(
            enabled = title.trim().length >= 3 && !state.loading,
            onClick = {
                onSave(RoomEditorInput(
                    title = title,
                    description = description,
                    category = category,
                    isPrivate = privateRoom,
                    isLocked = locked,
                    isInviteOnly = inviteOnly,
                    isPremium = ticketed,
                    isTicketRequired = ticketed,
                    ticketPriceAmount = price.toDoubleOrNull() ?: 0.0,
                    isSubscriberOnly = subscriberOnly,
                    isVerifiedOnly = verifiedOnly,
                    language = existing?.language ?: "en",
                    scheduledRoomId = existing?.scheduledRoomId,
                    clubId = existing?.clubId,
                ))
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (roomId == null) "Create room" else "Save settings") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SwitchLine(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked, onCheckedChange = onChecked)
    }
}

@Composable
fun ScheduleEditorScreen(
    state: HostingUiState,
    scheduleId: String?,
    onLoad: (String) -> Unit,
    onSave: (ScheduledRoomInput) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit,
) {
    if (scheduleId != null) LaunchedEffect(scheduleId) { onLoad(scheduleId) }
    val existing = if (scheduleId != null) state.selectedSchedule?.takeIf { it.id == scheduleId } else null
    val zone = remember { ZoneId.systemDefault() }
    val initialDateTime = remember(existing?.scheduledStartTime) {
        existing?.scheduledStartTime?.let { runCatching { OffsetDateTime.parse(it).atZoneSameInstant(zone).toLocalDateTime() }.getOrNull() }
            ?: LocalDateTime.now(zone).plusHours(1).withSecond(0).withNano(0)
    }
    var title by rememberSaveable(scheduleId, existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var description by rememberSaveable(scheduleId, existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var date by remember(scheduleId, existing?.id) { mutableStateOf(initialDateTime.toLocalDate()) }
    var time by remember(scheduleId, existing?.id) { mutableStateOf(initialDateTime.toLocalTime()) }
    var duration by rememberSaveable(scheduleId, existing?.id) { mutableStateOf((existing?.durationMinutes ?: 60).toString()) }
    var privateRoom by rememberSaveable(scheduleId, existing?.id) { mutableStateOf(existing?.visibility.equals("PRIVATE", true)) }
    var inviteOnly by rememberSaveable(scheduleId, existing?.id) { mutableStateOf(existing?.isInviteOnly ?: false) }
    var ticketed by rememberSaveable(scheduleId, existing?.id) { mutableStateOf(existing?.isPremium ?: false) }
    var price by rememberSaveable(scheduleId, existing?.id) { mutableStateOf(existing?.ticketPriceAmount?.toString()?.takeIf { it != "null" } ?: "0") }
    val context = LocalContext.current
    HostPage(if (scheduleId == null) "Schedule Room" else "Edit Schedule", "Times are shown in your device time zone (${zone.id}).", onBack) {
        HostingStatus(state)
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {
                    DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).apply {
                        datePicker.minDate = System.currentTimeMillis() - 1_000
                    }.show()
                }, modifier = Modifier.weight(1f)
            ) { Text(date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")), maxLines = 1) }
            OutlinedButton(
                onClick = { TimePickerDialog(context, { _, h, min -> time = LocalTime.of(h, min) }, time.hour, time.minute, false).show() },
                modifier = Modifier.weight(1f)
            ) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a")), maxLines = 1) }
        }
        OutlinedTextField(duration, { duration = it.filter(Char::isDigit) }, label = { Text("Duration (minutes)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        SwitchLine("Private schedule", privateRoom) { privateRoom = it; if (it) inviteOnly = true }
        SwitchLine("Invite only", inviteOnly) { inviteOnly = it }
        SwitchLine("Ticketed / premium", ticketed) { ticketed = it }
        if (ticketed) OutlinedTextField(price, { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Ticket price") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        val localDateTime = LocalDateTime.of(date, time)
        val offset = localDateTime.atZone(zone).toOffsetDateTime()
        val future = offset.isAfter(OffsetDateTime.now(zone))
        if (!future) Text("Choose a future date and time.", color = MaterialTheme.colorScheme.error)
        Button(
            enabled = title.trim().length >= 3 && future && !state.loading,
            onClick = {
                onSave(ScheduledRoomInput(
                    title = title,
                    description = description,
                    scheduledStartTime = offset.toString(),
                    durationMinutes = duration.toIntOrNull()?.coerceIn(15, 1440) ?: 60,
                    timeZone = zone.id,
                    visibility = if (privateRoom) "PRIVATE" else "PUBLIC",
                    isInviteOnly = inviteOnly,
                    isPremium = ticketed,
                    ticketPriceAmount = price.toDoubleOrNull() ?: 0.0,
                    category = existing?.category ?: "General",
                    language = existing?.language ?: "en",
                    currency = existing?.currency ?: "USD",
                    clubId = existing?.clubId,
                ))
            }, modifier = Modifier.fillMaxWidth()
        ) { Text(if (scheduleId == null) "Schedule room" else "Save schedule") }
        if (onDelete != null) OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Delete schedule") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun HostRoomManageScreen(
    state: HostingUiState,
    roomId: String,
    onLoad: () -> Unit,
    onSettings: () -> Unit,
    onStart: () -> Unit,
    onOpenConsole: () -> Unit,
    onInteractive: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(roomId) { onLoad() }
    val room = state.selectedRoom?.takeIf { it.id == roomId }
    HostPage("Room Management", room?.title, onBack) {
        HostingStatus(state)
        if (room != null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(room.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${room.status.replaceFirstChar { it.uppercase() }} · ${room.participantCount} participants")
                    if (room.isPrivate || room.isInviteOnly) Text("Restricted access", color = ConsumerColors.SapphireDeep)
                    if (room.isTicketRequired || room.isPremium) Text("Ticket access enabled", color = ConsumerColors.SapphireDeep)
                }
            }
            OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Room settings") }
            when {
                room.status.equals("paused", true) -> {
                    Button(onClick = onResume, modifier = Modifier.fillMaxWidth()) { Text("Resume room") }
                    OutlinedButton(onClick = onEnd, modifier = Modifier.fillMaxWidth()) { Text("End room") }
                }
                room.isLive || room.status.equals("live", true) -> {
                    Button(onClick = onOpenConsole, modifier = Modifier.fillMaxWidth()) { Text("Open host console") }
                    OutlinedButton(onClick = onInteractive, modifier = Modifier.fillMaxWidth()) { Text("Polls & quiz") }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f)) { Text("Pause") }
                        OutlinedButton(onClick = onEnd, modifier = Modifier.weight(1f)) { Text("End") }
                    }
                }
                else -> Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Start room") }
            }
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Delete room") }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun HostLiveConsoleScreen(
    state: HostingUiState,
    roomId: String,
    viewerId: String?,
    onEnter: () -> Unit,
    onLeave: () -> Unit,
    onMicrophone: (Boolean) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onInviteSpeaker: (String) -> Unit,
    onRemoveSpeaker: (String) -> Unit,
    onMuteSpeaker: (String, Boolean) -> Unit,
    onSearchInvite: (String) -> Unit,
    onInviteParticipant: (String) -> Unit,
    onInteractive: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(roomId) { onEnter() }
    DisposableEffect(roomId) { onDispose { onLeave() } }
    val context = LocalContext.current
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        if (granted) onMicrophone(true)
    }
    var inviteQuery by rememberSaveable { mutableStateOf("") }
    val room = state.selectedRoom
    val stage = state.stage
    HostPage("Host Console", room?.title ?: "Live room", onBack) {
        HostingStatus(state)
        Card(Modifier.fillMaxWidth().background(ConsumerBrushes.Hero)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (state.rtcConnected) "Live audio connected" else "Connecting live audio…", fontWeight = FontWeight.Bold)
                Button(
                    enabled = state.rtcConnected && !state.microphoneBusy,
                    onClick = {
                        if (state.microphoneEnabled) onMicrophone(false)
                        else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) onMicrophone(true)
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.microphoneEnabled) "Mute microphone" else "Go on mic") }
                if (permissionDenied) Text("Microphone permission is required to speak. You can still host and listen without it.", color = MaterialTheme.colorScheme.error)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f)) { Text("Pause") }
                    OutlinedButton(onClick = onResume, modifier = Modifier.weight(1f)) { Text("Resume") }
                    OutlinedButton(onClick = onEnd, modifier = Modifier.weight(1f)) { Text("End") }
                }
                OutlinedButton(onClick = onInteractive, modifier = Modifier.fillMaxWidth()) { Text("Polls & quiz") }
            }
        }
        Text("Raised hands", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (state.stage?.handQueue.isNullOrEmpty()) Text("No raised hands right now.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        stage?.handQueue.orEmpty().forEach { hand ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stageName(hand.userId, stage, viewerId), Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                TextButton(onClick = { onReject(hand.userId) }) { Text("Reject") }
                Button(onClick = { onApprove(hand.userId) }) { Text("Approve") }
            }
        }
        Text("Speakers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        stage?.speakers.orEmpty().forEach { speaker ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(speaker.username ?: stageName(speaker.userId, stage, viewerId), Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (speaker.userId != viewerId) {
                    TextButton(onClick = { onMuteSpeaker(speaker.userId, !speaker.isMuted) }) { Text(if (speaker.isMuted) "Unmute" else "Mute") }
                    TextButton(onClick = { onRemoveSpeaker(speaker.userId) }) { Text("Remove") }
                }
            }
        }
        Text("Audience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        val currentSpeakers = stage?.speakers.orEmpty()
        stage?.participants.orEmpty()
            .filter { participant -> currentSpeakers.none { speaker -> speaker.userId == participant.userId } }
            .take(30)
            .forEach { participant ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(participant.username ?: "Listener", Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (participant.userId != viewerId) TextButton(onClick = { onInviteSpeaker(participant.userId) }) { Text("Invite to stage") }
            }
        }
        Text("Invite participant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            inviteQuery, { inviteQuery = it; onSearchInvite(it) }, label = { Text("Search people") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        state.inviteCandidates.take(6).forEach { candidate ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(candidate.displayName.ifBlank { candidate.username }, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("@${candidate.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = { onInviteParticipant(candidate.id) }) { Text("Invite") }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

private fun stageName(userId: String, stage: RoomStageState?, viewerId: String?): String {
    if (userId == viewerId) return "You"
    return stage?.participants?.firstOrNull { it.userId == userId }?.username
        ?: stage?.speakers?.firstOrNull { it.userId == userId }?.username
        ?: "Listener"
}

@Composable
fun PollsQuizScreen(
    state: HostingUiState,
    roomId: String,
    onLoad: () -> Unit,
    onCreatePoll: (CreatePollBody) -> Unit,
    onStartPoll: (String) -> Unit,
    onStopPoll: (String) -> Unit,
    onDeletePoll: (String) -> Unit,
    onCreateQuiz: (CreateQuizBody) -> Unit,
    onStartQuiz: (String) -> Unit,
    onNextQuiz: (String) -> Unit,
    onStopQuiz: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(roomId) { onLoad() }
    var pollTitle by rememberSaveable { mutableStateOf("") }
    var pollOptions by rememberSaveable { mutableStateOf("") }
    var quizTitle by rememberSaveable { mutableStateOf("") }
    var question by rememberSaveable { mutableStateOf("") }
    var quizOptions by rememberSaveable { mutableStateOf("") }
    var correct by rememberSaveable { mutableStateOf("1") }
    HostPage("Polls & Quiz", "Create live interactions without leaving the room.", onBack) {
        HostingStatus(state)
        Text("Polls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(pollTitle, { pollTitle = it }, label = { Text("Poll question") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(pollOptions, { pollOptions = it }, label = { Text("Options, separated by commas") }, modifier = Modifier.fillMaxWidth())
        val parsedPollOptions = pollOptions.split(',').map(String::trim).filter(String::isNotBlank).distinct()
        Button(
            onClick = { onCreatePoll(CreatePollBody(roomId, pollTitle.trim(), options = parsedPollOptions, durationSeconds = 60)); pollTitle = ""; pollOptions = "" },
            enabled = pollTitle.trim().isNotBlank() && parsedPollOptions.size >= 2,
        ) { Text("Create poll") }
        state.polls.forEach { poll ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(poll.title, fontWeight = FontWeight.Bold)
                    Text(poll.status, style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!poll.status.equals("ACTIVE", true)) TextButton(onClick = { onStartPoll(poll.id) }) { Text("Start") }
                        else TextButton(onClick = { onStopPoll(poll.id) }) { Text("Stop") }
                        TextButton(onClick = { onDeletePoll(poll.id) }) { Text("Delete") }
                    }
                }
            }
        }
        HorizontalDivider()
        Text("Quiz", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        val active = state.activeQuiz
        if (active != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(active.title, fontWeight = FontWeight.Bold)
                    Text("${active.status} · Round ${active.currentRound ?: 0}/${active.totalRounds}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!active.status.equals("ACTIVE", true)) Button(onClick = { onStartQuiz(active.id) }) { Text("Start") }
                        else {
                            OutlinedButton(onClick = { onNextQuiz(active.id) }) { Text("Next round") }
                            OutlinedButton(onClick = { onStopQuiz(active.id) }) { Text("Stop") }
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(quizTitle, { quizTitle = it }, label = { Text("Quiz title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(question, { question = it }, label = { Text("Question") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(quizOptions, { quizOptions = it }, label = { Text("Options, separated by commas") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(correct, { correct = it.filter(Char::isDigit) }, label = { Text("Correct option number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            val opts = quizOptions.split(',').map(String::trim).filter(String::isNotBlank)
            val correctIndex = (correct.toIntOrNull() ?: 1) - 1
            Button(
                enabled = quizTitle.isNotBlank() && question.isNotBlank() && opts.size >= 2 && correctIndex in opts.indices,
                onClick = {
                    onCreateQuiz(CreateQuizBody(roomId, quizTitle.trim(), totalRounds = 1, questions = listOf(QuizQuestionInput(1, question.trim(), opts, correctIndex))))
                },
            ) { Text("Create quiz") }
        }
        Spacer(Modifier.height(30.dp))
    }
}

private fun formatSchedule(iso: String, zoneId: String): String = runCatching {
    val zone = runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())
    OffsetDateTime.parse(iso).atZoneSameInstant(zone).format(DateTimeFormatter.ofPattern("dd MMM yyyy · hh:mm a z"))
}.getOrDefault(iso)
