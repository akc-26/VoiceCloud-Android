package app.voicecloud.feature.hosting.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.VoiceCloudAnimatedWaveform
import app.voicecloud.core.designsystem.component.VoiceCloudEmptyVisual
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudRemoteMedia
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.VoiceCloudLiveBadge
import app.voicecloud.core.designsystem.component.VoiceCloudSectionHeader
import app.voicecloud.core.designsystem.component.VoiceCloudSpeakingAvatar
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedCard
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedPrimaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSecondaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSectionTitle
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedTopBar
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.VoiceCloudPageMetrics
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme
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
    dark: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = dark) {
        val metrics = VoiceCloudPageMetrics.current()
        Scaffold(
            containerColor = if (dark) ConsumerColors.DeepNavy else ConsumerColors.Surface,
            topBar = {
                if (!dark) {
                    Surface(color = ConsumerColors.Surface, shadowElevation = 1.dp) { Row(Modifier.fillMaxWidth().padding(horizontal = metrics.horizontalPadding)) { VoiceCloudApprovedTopBar(title, onBack = onBack) } }
                } else {
                    Surface(color = ConsumerColors.DeepNavy, shadowElevation = 1.dp) {
                        Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("‹", color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.headlineSmall) }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(voiceCloudTitleCase(title), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (!subtitle.isNullOrBlank()) Text(subtitle, color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            VoiceCloudLiveBadge()
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = metrics.horizontalPadding, vertical = 8.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!subtitle.isNullOrBlank() && !dark) Text(voiceCloudTitleCase(subtitle), style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                content()
            }
        }
    }
}

@Composable
private fun HostingStatus(state: HostingUiState) {
    VoiceCloudToastEffect(state.error, state.notice)
    if (state.loading) Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
    onVerification: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }
    HostPage("Host Studio", "Create, schedule and manage your audio rooms.", onBack) {
        HostingStatus(state)
        VoiceCloudApprovedSectionTitle("Content planner")
        VoiceCloudApprovedSecondaryButton("Host Verification & Progress", onClick = onVerification)
        if (state.eligibilityChecked) {
            val approved = state.hostProfile?.status.equals("APPROVED", true)
            if (!approved) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(voiceCloudTitleCase("Host Access"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(state.eligibility?.reasons?.firstOrNull() ?: "An approved Host profile is required before you can create or broadcast rooms.")
                        state.eligibility?.reasons.orEmpty().drop(1).take(3).forEach { Text(voiceCloudTitleCase("• $it"), style = MaterialTheme.typography.bodySmall) }
                        state.eligibility?.let { eligibility ->
                            if (eligibility.applicationsEnabled && eligibility.eligible) {
                                Text(voiceCloudTitleCase("Your account currently meets the Host eligibility requirements."), color = ConsumerColors.SapphireDeep)
                            }
                        }
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VoiceCloudApprovedPrimaryButton("Create Room", modifier = Modifier.weight(1f), onClick = onCreateRoom)
                    VoiceCloudApprovedSecondaryButton("Schedule", modifier = Modifier.weight(1f), onClick = onScheduleRoom)
                }
                Text(voiceCloudTitleCase("My Rooms"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (state.rooms.isEmpty() && !state.loading) VoiceCloudEmptyVisual("No Rooms Yet", "Create Your First Room When You Are Ready To Host.", Modifier.fillMaxWidth(), VoiceCloudVisualKind.LIVE)
                state.rooms.forEach { room -> HostRoomCard(room, { onRoom(room.id) }) }
                Text(voiceCloudTitleCase("Scheduled"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (state.schedules.isEmpty() && !state.loading) VoiceCloudEmptyVisual("No Upcoming Rooms", "Schedule a polished live session for your audience.", Modifier.fillMaxWidth(), VoiceCloudVisualKind.SCHEDULE)
                state.schedules.forEach { schedule ->
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp, onClick = { onSchedule(schedule.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))) {
                                VoiceCloudRemoteMedia(
                                    url = null,
                                    contentDescription = schedule.title,
                                    modifier = Modifier.fillMaxSize(),
                                    kind = VoiceCloudVisualKind.SCHEDULE,
                                    dark = false,
                                    fallbackDrawable = app.voicecloud.core.designsystem.R.drawable.vc_ref_live_forest,
                                )
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(schedule.title, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatSchedule(schedule.scheduledStartTime, schedule.timeZone), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(voiceCloudTitleCase(schedule.visibility), style = MaterialTheme.typography.labelMedium, color = ConsumerColors.SapphireDeep)
                                    if (schedule.isPremium) Text(voiceCloudTitleCase("Ticketed"), style = MaterialTheme.typography.labelMedium, color = ConsumerColors.VipGold)
                                }
                            }
                        }
                        Button(onClick = { onStartScheduled(schedule) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Start Room")) }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HostRoomCard(room: HostRoom, onOpen: () -> Unit) {
    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp, onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))) {
                VoiceCloudRemoteMedia(
                    url = null,
                    contentDescription = room.title,
                    modifier = Modifier.fillMaxSize(),
                    kind = VoiceCloudVisualKind.LIVE,
                    dark = room.isLive,
                    fallbackDrawable = app.voicecloud.core.designsystem.R.drawable.vc_ref_creator_mic,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(room.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(voiceCloudTitleCase(room.status), style = MaterialTheme.typography.labelSmall, color = if (room.isLive) ConsumerColors.Sapphire else ConsumerColors.TextMuted)
                Text("${room.participantCount} participants · ${room.category}", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted, maxLines = 1)
            }
            Text("›", color = ConsumerColors.SapphireDeep, style = MaterialTheme.typography.titleLarge)
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
        Row(Modifier.fillMaxWidth().background(ConsumerColors.SurfaceSoft, RoundedCornerShape(12.dp)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(shape = RoundedCornerShape(9.dp), color = ConsumerColors.Sapphire, modifier = Modifier.weight(1f)) { Text("Live Now", Modifier.padding(vertical = 9.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
            Surface(shape = RoundedCornerShape(9.dp), color = androidx.compose.ui.graphics.Color.Transparent, modifier = Modifier.weight(1f)) { Text("Schedule", Modifier.padding(vertical = 9.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = ConsumerColors.TextMuted, style = MaterialTheme.typography.labelSmall) }
        }
        OutlinedTextField(title, { title = it }, label = { Text(voiceCloudTitleCase("Room Title")) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp))
        OutlinedTextField(description, { description = it }, label = { Text(voiceCloudTitleCase("Description")) }, minLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp))
        OutlinedTextField(category, { category = it }, label = { Text(voiceCloudTitleCase("Category")) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp))
        SwitchLine("Private room", privateRoom) { privateRoom = it; if (it) inviteOnly = true }
        SwitchLine("Lock room entry", locked) { locked = it }
        SwitchLine("Invite only", inviteOnly) { inviteOnly = it }
        SwitchLine("Subscribers only", subscriberOnly) { subscriberOnly = it }
        SwitchLine("Verified listeners only", verifiedOnly) { verifiedOnly = it }
        SwitchLine("Ticketed / premium access", ticketed) { ticketed = it }
        if (ticketed) OutlinedTextField(price, { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(voiceCloudTitleCase("Ticket Price")) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp))
        VoiceCloudApprovedPrimaryButton(
            text = if (roomId == null) "Create live room" else "Save changes",
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
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SwitchLine(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(voiceCloudTitleCase(label), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
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
        VoiceCloudApprovedSectionTitle(if (scheduleId == null) "Schedule a room" else "Edit schedule")
        OutlinedTextField(title, { title = it }, label = { Text(voiceCloudTitleCase("Title")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text(voiceCloudTitleCase("Description")) }, minLines = 2, modifier = Modifier.fillMaxWidth())
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
        OutlinedTextField(duration, { duration = it.filter(Char::isDigit) }, label = { Text(voiceCloudTitleCase("Duration (Minutes)")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        SwitchLine("Private schedule", privateRoom) { privateRoom = it; if (it) inviteOnly = true }
        SwitchLine("Invite only", inviteOnly) { inviteOnly = it }
        SwitchLine("Ticketed / premium", ticketed) { ticketed = it }
        if (ticketed) OutlinedTextField(price, { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(voiceCloudTitleCase("Ticket Price")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        val localDateTime = LocalDateTime.of(date, time)
        val offset = localDateTime.atZone(zone).toOffsetDateTime()
        val future = offset.isAfter(OffsetDateTime.now(zone))
        if (!future) Text(voiceCloudTitleCase("Choose a future date & time"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
        ) { Text(voiceCloudTitleCase(if (scheduleId == null) "Schedule room" else "Save schedule")) }
        if (onDelete != null) VoiceCloudApprovedSecondaryButton("Delete Schedule", onClick = onDelete)
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
    onInteractive: (() -> Unit)? = null,
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
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 11.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VoiceCloudPictogram(VoiceCloudVisualKind.LIVE, size = 54.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(room.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text(voiceCloudTitleCase("${room.status.replaceFirstChar { it.uppercase() }} · ${room.participantCount} Participants"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (room.isPrivate || room.isInviteOnly) Text(voiceCloudTitleCase("Restricted Access"), color = ConsumerColors.SapphireDeep, style = MaterialTheme.typography.labelMedium)
                        if (room.isTicketRequired || room.isPremium) Text(voiceCloudTitleCase("Ticket Access Enabled"), color = ConsumerColors.VipGold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            VoiceCloudApprovedSecondaryButton("Room Settings", onClick = onSettings)
            when {
                room.status.equals("paused", true) -> {
                    Button(onClick = onResume, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Resume Room")) }
                    OutlinedButton(onClick = onEnd, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("End Room")) }
                }
                room.isLive || room.status.equals("live", true) -> {
                    VoiceCloudApprovedPrimaryButton("Open Host Console", onClick = onOpenConsole)
                    if (onInteractive != null) OutlinedButton(onClick = onInteractive, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Polls & Quiz")) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("Pause")) }
                        OutlinedButton(onClick = onEnd, modifier = Modifier.weight(1f)) { Text(voiceCloudTitleCase("End")) }
                    }
                }
                else -> VoiceCloudApprovedPrimaryButton("Start Room", onClick = onStart)
            }
            VoiceCloudApprovedSecondaryButton("Delete Room", onClick = onDelete)
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
    onInteractive: (() -> Unit)? = null,
    onBack: () -> Unit,
) {
    LaunchedEffect(roomId) { onEnter() }
    DisposableEffect(roomId) { onDispose { onLeave() } }
    val context = LocalContext.current
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> permissionDenied = !granted; if (granted) onMicrophone(true) }
    var inviteQuery by rememberSaveable { mutableStateOf("") }
    val room = state.selectedRoom
    val stage = state.stage
    HostPage("Host Console", room?.title ?: "Live room", onBack, dark = true) {
        HostingStatus(state)
        VoiceCloudToastEffect(if (permissionDenied) "Microphone Permission Is Required To Speak" else null, null)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                VoiceCloudLiveBadge()
                Spacer(Modifier.width(8.dp))
                Text("${stage?.speakers?.size ?: 0} speakers · ${stage?.participants?.size ?: 0} listeners", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextOnDarkSecondary, modifier = Modifier.weight(1f))
                Text(if (state.rtcConnected) "Connected" else "Connecting", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.Ice)
            }
            VoiceCloudSpeakingAvatar(room?.hostName ?: "Host", speaking = state.microphoneEnabled, size = 86.dp, dark = true)
            Text(room?.hostName ?: "Host", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.TextOnDark)
            Text("Host", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.VipGold)
            VoiceCloudAnimatedWaveform(Modifier.fillMaxWidth().height(34.dp), color = ConsumerColors.Ice, active = state.rtcConnected && state.microphoneEnabled)
        }
        VoiceCloudSectionHeader("Speakers", dark = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            stage?.speakers.orEmpty().take(4).forEach { speaker ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    VoiceCloudSpeakingAvatar(speaker.username ?: stageName(speaker.userId, stage, viewerId), speaking = !speaker.isMuted, size = 50.dp, dark = true)
                    Text(speaker.username ?: stageName(speaker.userId, stage, viewerId), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextOnDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        VoiceCloudSectionHeader("Listeners", dark = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            stage?.participants.orEmpty().filter { p -> stage?.speakers.orEmpty().none { it.userId == p.userId } }.take(6).forEach { participant ->
                VoiceCloudSpeakingAvatar(participant.username ?: "Listener", speaking = false, size = 36.dp, dark = true)
            }
        }
        VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp, dark = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                HostConsoleAction(if (state.microphoneEnabled) "Mute" else "Mic", VoiceCloudVisualKind.AUDIO) {
                    if (state.microphoneEnabled) onMicrophone(false)
                    else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) onMicrophone(true)
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
                HostConsoleAction("Pause", VoiceCloudVisualKind.ACTIVITY, onPause)
                if (onInteractive != null) HostConsoleAction("Polls", VoiceCloudVisualKind.AUDIENCE, onInteractive)
                HostConsoleAction("End", VoiceCloudVisualKind.SECURITY, onEnd)
            }
        }
        if (permissionDenied) Text("You can still host and listen without microphone access.", color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelSmall)
        VoiceCloudSectionHeader("Speaker queue", dark = true)
        if (stage?.handQueue.isNullOrEmpty()) Text("No microphone requests right now.", color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.bodySmall)
        stage?.handQueue.orEmpty().forEach { hand ->
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 8.dp, dark = true) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VoiceCloudSpeakingAvatar(stageName(hand.userId, stage, viewerId), speaking = false, size = 38.dp, dark = true)
                    Text(stageName(hand.userId, stage, viewerId), Modifier.weight(1f), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { onReject(hand.userId) }) { Text("Decline") }
                    Button(onClick = { onApprove(hand.userId) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)) { Text("Accept", style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
        VoiceCloudSectionHeader("Manage speakers", dark = true)
        stage?.speakers.orEmpty().filter { it.userId != viewerId }.forEach { speaker ->
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 8.dp, dark = true) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(speaker.username ?: stageName(speaker.userId, stage, viewerId), Modifier.weight(1f), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { onMuteSpeaker(speaker.userId, !speaker.isMuted) }) { Text(if (speaker.isMuted) "Unmute" else "Mute") }
                    TextButton(onClick = { onRemoveSpeaker(speaker.userId) }) { Text("Remove") }
                }
            }
        }
        VoiceCloudSectionHeader("Invite participant", dark = true)
        OutlinedTextField(inviteQuery, { inviteQuery = it; onSearchInvite(it) }, label = { Text("Search people") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp))
        state.inviteCandidates.take(6).forEach { candidate ->
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 8.dp, dark = true) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VoiceCloudSpeakingAvatar(candidate.displayName.ifBlank { candidate.username }, speaking = false, size = 38.dp, dark = true)
                    Column(Modifier.weight(1f)) {
                        Text(candidate.displayName.ifBlank { candidate.username }, color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.bodyMedium)
                        Text("@${candidate.username}", color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { onInviteParticipant(candidate.id) }) { Text("Invite") }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun HostConsoleAction(label: String, kind: VoiceCloudVisualKind, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = ConsumerColors.LiveSurfaceElevated, modifier = Modifier.size(56.dp), border = androidx.compose.foundation.BorderStroke(1.dp, ConsumerColors.Ice.copy(alpha = .16f))) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            VoiceCloudPictogram(kind, size = 26.dp, dark = true)
            Text(label, style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextOnDark)
        }
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
        Text(voiceCloudTitleCase("Polls"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(pollTitle, { pollTitle = it }, label = { Text(voiceCloudTitleCase("Poll Question")) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(pollOptions, { pollOptions = it }, label = { Text(voiceCloudTitleCase("Options, Separated By Commas")) }, modifier = Modifier.fillMaxWidth())
        val parsedPollOptions = pollOptions.split(',').map(String::trim).filter(String::isNotBlank).distinct()
        Button(
            onClick = { onCreatePoll(CreatePollBody(roomId, pollTitle.trim(), options = parsedPollOptions, durationSeconds = 60)); pollTitle = ""; pollOptions = "" },
            enabled = pollTitle.trim().isNotBlank() && parsedPollOptions.size >= 2,
        ) { Text(voiceCloudTitleCase("Create Poll")) }
        state.polls.forEach { poll ->
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VoiceCloudPictogram(VoiceCloudVisualKind.AUDIENCE, size = 44.dp)
                    Column(Modifier.weight(1f)) {
                        Text(poll.title, fontWeight = FontWeight.ExtraBold)
                        Text(voiceCloudTitleCase(poll.status), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!poll.status.equals("ACTIVE", true)) TextButton(onClick = { onStartPoll(poll.id) }) { Text(voiceCloudTitleCase("Start")) }
                    else TextButton(onClick = { onStopPoll(poll.id) }) { Text(voiceCloudTitleCase("Stop")) }
                    TextButton(onClick = { onDeletePoll(poll.id) }) { Text(voiceCloudTitleCase("Delete")) }
                }
            }
        }
        HorizontalDivider()
        Text(voiceCloudTitleCase("Quiz"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        val active = state.activeQuiz
        if (active != null) {
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 9.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VoiceCloudPictogram(VoiceCloudVisualKind.REWARD, size = 44.dp)
                    Column(Modifier.weight(1f)) {
                        Text(active.title, fontWeight = FontWeight.ExtraBold)
                        Text(voiceCloudTitleCase("${active.status} · Round ${active.currentRound ?: 0}/${active.totalRounds}"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!active.status.equals("ACTIVE", true)) Button(onClick = { onStartQuiz(active.id) }) { Text(voiceCloudTitleCase("Start")) }
                    else {
                        OutlinedButton(onClick = { onNextQuiz(active.id) }) { Text(voiceCloudTitleCase("Next Round")) }
                        OutlinedButton(onClick = { onStopQuiz(active.id) }) { Text(voiceCloudTitleCase("Stop")) }
                    }
                }
            }
        } else {
            OutlinedTextField(quizTitle, { quizTitle = it }, label = { Text(voiceCloudTitleCase("Quiz Title")) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(question, { question = it }, label = { Text(voiceCloudTitleCase("Question")) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(quizOptions, { quizOptions = it }, label = { Text(voiceCloudTitleCase("Options, Separated By Commas")) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(correct, { correct = it.filter(Char::isDigit) }, label = { Text(voiceCloudTitleCase("Correct Option Number")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            val opts = quizOptions.split(',').map(String::trim).filter(String::isNotBlank)
            val correctIndex = (correct.toIntOrNull() ?: 1) - 1
            Button(
                enabled = quizTitle.isNotBlank() && question.isNotBlank() && opts.size >= 2 && correctIndex in opts.indices,
                onClick = {
                    onCreateQuiz(CreateQuizBody(roomId, quizTitle.trim(), totalRounds = 1, questions = listOf(QuizQuestionInput(1, question.trim(), opts, correctIndex))))
                },
            ) { Text(voiceCloudTitleCase("Create Quiz")) }
        }
        Spacer(Modifier.height(30.dp))
    }
}

private fun formatSchedule(iso: String, zoneId: String): String = runCatching {
    val zone = runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault())
    OffsetDateTime.parse(iso).atZoneSameInstant(zone).format(DateTimeFormatter.ofPattern("dd MMM yyyy · hh:mm a z"))
}.getOrDefault(iso)

@Composable
fun HostVerificationScreen(
    state: HostingUiState,
    onLoad: () -> Unit,
    onApply: (String, String?, String?, List<String>, List<String>, String?) -> Unit,
    onUpload: (HostVerificationAssetKind, ByteArray, String, String) -> Unit,
    onReplace: (String, HostVerificationAssetKind, ByteArray, String, String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var pendingReplacement by remember { mutableStateOf<HostVerificationAsset?>(null) }
    var realName by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }
    var languages by rememberSaveable { mutableStateOf("") }
    var categories by rememberSaveable { mutableStateOf("") }
    var experience by rememberSaveable { mutableStateOf("") }

    fun deliver(uri: android.net.Uri?, kind: HostVerificationAssetKind, replacement: HostVerificationAsset? = null) {
        if (uri == null) return
        runCatching {
            val resolver = context.contentResolver
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Unable to read selected file.")
            val mime = resolver.getType(uri).orEmpty().ifBlank { "application/octet-stream" }
            var name = uri.lastPathSegment.orEmpty().substringAfterLast('/').ifBlank { "verification-file" }
            resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0)?.takeIf(String::isNotBlank)?.let { name = it }
            }
            Triple(bytes, name, mime)
        }.onSuccess { (bytes, name, mime) ->
            if (replacement == null) onUpload(kind, bytes, name, mime)
            else onReplace(replacement.id, kind, bytes, name, mime)
        }
    }

    val governmentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { deliver(it, HostVerificationAssetKind.GOVERNMENT_ID) }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { deliver(it, HostVerificationAssetKind.PROFILE_PHOTO) }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { deliver(it, HostVerificationAssetKind.SUPPORTING_DOCUMENT) }
    val replacementLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val asset = pendingReplacement
        pendingReplacement = null
        if (asset != null) deliver(uri, verificationKind(asset.type), asset)
    }

    LaunchedEffect(Unit) { onLoad() }
    HostPage("Host Verification", "Private identity verification and host progression.", onBack) {
        HostingStatus(state)
        val profile = state.hostProfile
        val eligibility = state.eligibility
        VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(voiceCloudTitleCase("Host Status"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(voiceCloudTitleCase(profile?.status?.ifBlank { "Not Applied" } ?: "Not Applied"), color = MaterialTheme.colorScheme.primary)
                profile?.rejectionReason?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (eligibility != null) {
                    Text(voiceCloudTitleCase(if (eligibility.eligible) "Eligible To Apply" else "Eligibility Requirements Pending"))
                    eligibility.reasons.forEach { Text(voiceCloudTitleCase("• $it"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                if (profile == null && eligibility?.eligible == true && eligibility.applicationsEnabled) {
                    OutlinedTextField(realName, { realName = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Real Name")) }, singleLine = true)
                    OutlinedTextField(bio, { bio = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Bio")) }, minLines = 2, maxLines = 4)
                    OutlinedTextField(country, { country = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Country")) }, singleLine = true)
                    OutlinedTextField(languages, { languages = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Languages")) }, supportingText = { Text(voiceCloudTitleCase("Separate With Commas")) })
                    OutlinedTextField(categories, { categories = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Creator Categories")) }, supportingText = { Text(voiceCloudTitleCase("Separate With Commas")) })
                    OutlinedTextField(experience, { experience = it }, Modifier.fillMaxWidth(), label = { Text(voiceCloudTitleCase("Hosting Experience")) }, minLines = 2, maxLines = 4)
                    Button(
                        onClick = { onApply(realName, bio.takeIf(String::isNotBlank), country.takeIf(String::isNotBlank), languages.split(',').map(String::trim).filter(String::isNotBlank), categories.split(',').map(String::trim).filter(String::isNotBlank), experience.takeIf(String::isNotBlank)) },
                        enabled = realName.isNotBlank() && !state.verificationBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(voiceCloudTitleCase(if (state.verificationBusy) "Submitting…" else "Apply To Become A Host")) }
                }
            }
        }

        state.progression?.let { progression ->
            Text(voiceCloudTitleCase("Progression"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            progression.metrics.forEach { metric ->
                VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                    Row(Modifier.fillMaxWidth()) {
                        Text(voiceCloudTitleCase(metric.label), Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(metric.value, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(voiceCloudTitleCase("Private Verification Files"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            voiceCloudTitleCase("Files are sent only through VoiceCloud's authenticated verification APIs and are not published as profile media."),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VoiceCloudApprovedPrimaryButton("Upload Government ID", enabled = !state.verificationBusy) { governmentLauncher.launch(arrayOf("image/*", "application/pdf")) }
        OutlinedButton(onClick = { photoLauncher.launch(arrayOf("image/*")) }, enabled = !state.verificationBusy, modifier = Modifier.fillMaxWidth()) {
            Text(voiceCloudTitleCase("Upload Verification Selfie"))
        }
        OutlinedButton(onClick = { documentLauncher.launch(arrayOf("image/*", "application/pdf")) }, enabled = !state.verificationBusy, modifier = Modifier.fillMaxWidth()) {
            Text(voiceCloudTitleCase("Upload Supporting Document"))
        }

        if (state.verificationAssets.isEmpty() && !state.loading) {
            Text(voiceCloudTitleCase("No verification assets are currently returned by VoiceCloud."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.verificationAssets.forEach { asset ->
            VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(voiceCloudTitleCase(asset.type.replace('_', ' ').ifBlank { "Verification Asset" }), fontWeight = FontWeight.Bold)
                    if (asset.status.isNotBlank()) Text(voiceCloudTitleCase(asset.status), color = MaterialTheme.colorScheme.primary)
                    asset.fileName?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    asset.rejectionReason?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (asset.status.equals("REJECTED", true) || asset.rejectionReason != null) {
                        OutlinedButton(onClick = { pendingReplacement = asset; replacementLauncher.launch(arrayOf("image/*", "application/pdf")) }, enabled = !state.verificationBusy) {
                            Text(voiceCloudTitleCase("Replace Rejected File"))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

private fun verificationKind(raw: String): HostVerificationAssetKind {
    val normalized = raw.uppercase()
    return when {
        "GOVERNMENT" in normalized || "ID" == normalized -> HostVerificationAssetKind.GOVERNMENT_ID
        "PHOTO" in normalized || "SELFIE" in normalized || "PROFILE" in normalized -> HostVerificationAssetKind.PROFILE_PHOTO
        else -> HostVerificationAssetKind.SUPPORTING_DOCUMENT
    }
}
