package app.voicecloud.feature.live.ui

import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.component.VcConnectionBanner
import app.voicecloud.core.designsystem.component.VcLiveBadge
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedCard
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedMetricRow
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedPrimaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSecondaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.VoiceCloudAnimatedWaveform
import app.voicecloud.core.designsystem.component.VoiceCloudAudioArtwork
import app.voicecloud.core.designsystem.component.VoiceCloudPosterArtwork
import app.voicecloud.core.designsystem.component.VoiceCloudRemoteMedia
import app.voicecloud.core.designsystem.component.VoiceCloudGlossCard
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.VoiceCloudLiveBadge
import app.voicecloud.core.designsystem.component.VoiceCloudPremiumBackdrop
import app.voicecloud.core.designsystem.component.VoiceCloudSpeakingAvatar
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.theme.CommonColors
import app.voicecloud.core.designsystem.theme.ConsumerBrushes
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.feature.live.model.*
import app.voicecloud.feature.live.rtc.RtcAudioState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun RoomPreviewScreen(
    state: LiveRoomUiState,
    roomId: String,
    onLoad: () -> Unit,
    onJoin: () -> Unit,
    onToggleSave: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(roomId) { onLoad() }
    VoiceCloudToastEffect(state.error, state.notice)
    Scaffold(
        containerColor = ConsumerColors.DarkBackground,
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) {
                    Text("‹", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
                Text("Room preview", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                TextButton(onClick = onToggleSave, enabled = !state.mutationBusy) {
                    Text(if (state.saved) "Saved" else "Save", color = Color.White)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).background(ConsumerColors.DarkBackground),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = ConsumerColors.Sapphire) }
            state.room?.let { room ->
                item {
                    Box(Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(28.dp)).background(ConsumerColors.LiveSurface)) {
                        VoiceCloudRemoteMedia(room.coverUrl, room.title.ifBlank { "Room artwork" }, Modifier.fillMaxSize(), VoiceCloudVisualKind.LIVE, dark = true, fallbackDrawable = app.voicecloud.core.designsystem.R.drawable.vc_ref_live_forest)
                        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, ConsumerColors.DarkBackground.copy(alpha = .92f)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (room.isLive) VcLiveBadge(count = "${room.listenerCount}")
                            Text(room.title.ifBlank { "VoiceCloud Room" }, style = MaterialTheme.typography.headlineMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(listOf(room.category, room.language).filter(String::isNotBlank).joinToString(" · ").ifBlank { "Live audio" }, style = MaterialTheme.typography.bodyLarge, color = ConsumerColors.TextOnDarkSecondary)
                        }
                    }
                }
                item { VoiceCloudApprovedMetricRow(listOf(room.speakerCount.toString() to "Speakers", room.listenerCount.toString() to "Listeners", (if (room.isPremium) "VIP" else "Open") to "Access"), dark = true) }
                if (!room.description.isNullOrBlank()) item {
                    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), dark = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("About this room", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(room.description, style = MaterialTheme.typography.bodyLarge, color = ConsumerColors.TextOnDarkSecondary)
                        }
                    }
                }
                val restrictions = room.restrictionLabels()
                if (restrictions.isNotEmpty()) item {
                    Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        restrictions.forEach { label -> Surface(shape = RoundedCornerShape(50), color = ConsumerColors.LiveSurfaceElevated) { Text(label, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextOnDarkSecondary) } }
                    }
                }
                item { VoiceCloudApprovedPrimaryButton(if (state.joining) "Joining..." else if (room.isJoinablePresentation()) "Join" else "Unavailable", enabled = room.isJoinablePresentation() && !state.joining, onClick = onJoin) }
                item { VoiceCloudApprovedSecondaryButton(if (state.saved) "Saved" else "Save", enabled = !state.mutationBusy, onClick = onToggleSave) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveRoomScreen(
    state: LiveRoomUiState,
    roomId: String,
    viewerId: String?,
    onEnter: () -> Unit,
    onLeave: () -> Unit,
    onRetryAudio: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleHand: () -> Unit,
    onSendMessage: (String) -> Unit,
    onMessageReaction: (String, String) -> Unit,
    onReaction: (String) -> Unit,
    onGift: (GiftCatalogItem) -> Unit,
    onAcceptInvitation: () -> Unit,
    onRejectInvitation: () -> Unit,
    onBackground: () -> Unit,
    onForeground: () -> Unit,
    onReport: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(roomId) { onEnter() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onForeground()
                Lifecycle.Event.ON_STOP -> onBackground()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(roomId) { onDispose { onLeave() } }

    var showReactions by rememberSaveable { mutableStateOf(false) }
    var showGifts by rememberSaveable { mutableStateOf(false) }
    var showChat by rememberSaveable { mutableStateOf(false) }
    val closeRoom: () -> Unit = { onBack() }

    if (showReactions) {
        ModalBottomSheet(onDismissRequest = { showReactions = false }, containerColor = ConsumerColors.Surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Send a Reaction", color = ConsumerColors.Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(listOf("👏", "❤️", "🔥", "😂", "🎉", "😍", "😮", "💯", "🙌", "✨")) { _, emoji ->
                        Surface(onClick = { onReaction(emoji); showReactions = false }, shape = CircleShape, color = ConsumerColors.SurfaceSoft, modifier = Modifier.size(52.dp)) { Box(contentAlignment = Alignment.Center) { Text(emoji, style = MaterialTheme.typography.headlineSmall) } }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
    if (showGifts) {
        ModalBottomSheet(onDismissRequest = { showGifts = false }, containerColor = ConsumerColors.Surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Send a Gift", color = ConsumerColors.Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (state.gifts.isEmpty()) Text("Gifts are unavailable for this room.", color = ConsumerColors.TextMuted)
                else GiftRow(state.gifts, state.mutationBusy) { gift -> onGift(gift); showGifts = false }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
    if (showChat) {
        ModalBottomSheet(onDismissRequest = { showChat = false }, containerColor = ConsumerColors.Surface) {
            Column(Modifier.fillMaxWidth().heightIn(max = 620.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink, modifier = Modifier.weight(1f))
                    Text("${state.messages.size} messages", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
                }
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (state.messages.isEmpty()) item { Text("Say something to the room.", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted, modifier = Modifier.padding(12.dp)) }
                    itemsIndexed(state.messages.takeLast(30).distinctBy { it.id }, key = { index, message -> "sheet-chat:${message.id}:$index" }) { _, message ->
                        val mine = message.senderId == viewerId
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                            Surface(color = if (mine) ConsumerColors.Sapphire else ConsumerColors.SurfaceSoft, shape = RoundedCornerShape(14.dp), modifier = Modifier.widthIn(max = 300.dp)) {
                                Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
                                    if (!mine) Text(message.sender?.displayName ?: message.sender?.username ?: "Member", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ConsumerColors.SapphireDeep)
                                    Text(message.content.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = if (mine) Color.White else ConsumerColors.Ink)
                                }
                            }
                        }
                    }
                }
                ChatComposer(
                    enabled = state.conversationId != null && !state.mutationBusy,
                    onSend = onSendMessage,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    light = true,
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    Scaffold(
        containerColor = ConsumerColors.DeepNavy,
        topBar = {
            LiveRoomTopBar(
                title = state.room?.title?.ifBlank { "Live room" } ?: "Live room",
                saved = state.saved,
                busy = state.mutationBusy,
                onBack = closeRoom,
                onSave = onToggleSave,
                onReport = { onReport(roomId, state.room?.title?.ifBlank { "Live room" } ?: "Live room") },
                onLeave = closeRoom,
            )
        },
        bottomBar = {
            if (state.inRoom) {
                Surface(color = ConsumerColors.DeepNavy, shadowElevation = 8.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LiveBottomControl("💬", "Chat") { showChat = true }
                        LiveBottomControl("🎙", "Mic Request") { onToggleHand() }
                        Surface(onClick = onToggleHand, shape = CircleShape, color = ConsumerColors.Sapphire, modifier = Modifier.size(52.dp), enabled = !state.mutationBusy && !state.paused) {
                            Box(contentAlignment = Alignment.Center) { Text(if (state.handRaised) "✓" else "✋", style = MaterialTheme.typography.headlineSmall, color = Color.White) }
                        }
                        LiveBottomControl("☺", "Reactions") { showReactions = true }
                        LiveBottomControl("🎁", "Gifts") { showGifts = true }
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(ConsumerColors.DeepNavy, ConsumerColors.LiveSurface)))) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { LiveStageHeader(state, viewerId, onRetryAudio) }
                if (state.joining) item { Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ConsumerColors.Ice) } }
                state.accessIssue?.let { issue -> item { AccessIssueCard(issue, onBack) } }
                if (state.paused) item { DarkMessageCard("Room paused", false, null) }
                if (state.ended) item { DarkMessageCard("Room ended", false, onBack) }
                if (state.speakerInvitationPending) item {
                    Surface(shape = RoundedCornerShape(14.dp), color = ConsumerColors.Surface) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text("Speaker invite", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                                Text("You’ve been invited to speak.", style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted)
                            }
                            TextButton(onClick = onRejectInvitation, enabled = !state.mutationBusy) { Text("Decline", style = MaterialTheme.typography.labelSmall) }
                            Button(onClick = onAcceptInvitation, enabled = !state.mutationBusy, shape = RoundedCornerShape(10.dp)) { Text("Accept", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
                if (state.inRoom) {
                    item { StageRoster(state.participants.distinctBy { it.userId }, viewerId) }
                    item { ReactionsStrip(state.reactions) }
                    state.messages.lastOrNull()?.let { message ->
                        item {
                            Surface(onClick = { showChat = true }, color = ConsumerColors.Surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    VoiceCloudPictogram(VoiceCloudVisualKind.MESSAGE, size = 30.dp)
                                    Column(Modifier.weight(1f)) {
                                        Text(message.sender?.displayName ?: message.sender?.username ?: "Member", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
                                        Text(message.content.orEmpty(), style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveBottomControl(symbol: String, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 4.dp)) {
        Text(symbol, style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextOnDarkSecondary, maxLines = 1)
    }
}

@Composable
private fun LiveStageHeader(state: LiveRoomUiState, viewerId: String?, onRetryAudio: () -> Unit) {
    val room = state.room
    val participants = state.participants
    val primary = participants.firstOrNull { it.isSpeaking } ?: participants.firstOrNull { it.role.lowercase() != "listener" }
    val rtcLabel = when (state.rtcState) {
        RtcAudioState.Connected, RtcAudioState.Reconnected -> "Live audio connected"
        RtcAudioState.Connecting -> "Connecting audio..."
        RtcAudioState.Reconnecting -> "Reconnecting..."
        is RtcAudioState.Failed -> "Audio interrupted"
        RtcAudioState.Disconnected -> if (state.inRoom) "Disconnected" else "Preparing room"
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            VoiceCloudLiveBadge()
            Spacer(Modifier.width(7.dp))
            Text("${room?.speakerCount ?: 0} speakers · ${room?.listenerCount ?: participants.size} listeners", style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextOnDarkSecondary, modifier = Modifier.weight(1f))
            Text(rtcLabel, style = MaterialTheme.typography.labelSmall, color = if (state.rtcState is RtcAudioState.Failed) CommonColors.Error else ConsumerColors.Ice)
        }
        Text(room?.title?.ifBlank { "Live Room" } ?: "Live Room", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box(Modifier.fillMaxWidth().height(126.dp), contentAlignment = Alignment.Center) {
            VoiceCloudAnimatedWaveform(Modifier.fillMaxWidth(.88f).height(52.dp), color = ConsumerColors.Ice, active = state.inRoom && !state.paused)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                VoiceCloudSpeakingAvatar(primary?.username ?: if (primary?.userId == viewerId) "You" else "Host", speaking = primary?.isSpeaking == true, size = 82.dp, dark = true)
                Text(primary?.username?.takeIf(String::isNotBlank) ?: if (primary?.userId == viewerId) "You" else "Host", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(if (primary?.isSpeaking == true) "Speaking" else primary?.role?.replace('_', ' ') ?: "Host", color = if (primary?.isSpeaking == true) ConsumerColors.Ice else ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
        if (state.joining && !state.inRoom) {
            VcConnectionBanner("Getting your room ready...", ConsumerColors.Ice, ConsumerColors.Ice.copy(alpha = 0.14f))
        } else when (state.rtcState) {
            RtcAudioState.Connecting -> VcConnectionBanner("Joining the conversation...", ConsumerColors.Sapphire, ConsumerColors.Sapphire.copy(alpha = 0.18f))
            RtcAudioState.Reconnecting -> VcConnectionBanner("Connection interrupted. Reconnecting...", CommonColors.Warning, CommonColors.Warning.copy(alpha = 0.15f), action = "Retry", onAction = onRetryAudio)
            is RtcAudioState.Failed -> VcConnectionBanner("Audio interrupted. Check your connection.", CommonColors.Warning, CommonColors.Warning.copy(alpha = 0.15f), action = "Retry", onAction = onRetryAudio)
            RtcAudioState.Disconnected -> if (state.inRoom) VcConnectionBanner("You've been disconnected.", CommonColors.Error, CommonColors.Error.copy(alpha = 0.14f), action = "Retry", onAction = onRetryAudio)
            else -> Unit
        }
        if (state.rtcState is RtcAudioState.Failed || (state.rtcState is RtcAudioState.Disconnected && state.inRoom)) {
            TextButton(onClick = onRetryAudio) { Text("Retry audio", color = ConsumerColors.Ice, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun LiveRoomTopBar(
    title: String,
    saved: Boolean,
    busy: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onReport: () -> Unit,
    onLeave: () -> Unit,
) {
    Surface(color = ConsumerColors.DeepNavy, shadowElevation = 3.dp) {
        Row(
            Modifier.fillMaxWidth().padding(start = 2.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Canvas(Modifier.size(22.dp)) {
                    val stroke = 2.2.dp.toPx()
                    val color = ConsumerColors.TextOnDark
                    drawLine(color, Offset(size.width * .78f, size.height * .5f), Offset(size.width * .24f, size.height * .5f), stroke, StrokeCap.Round)
                    drawLine(color, Offset(size.width * .24f, size.height * .5f), Offset(size.width * .48f, size.height * .25f), stroke, StrokeCap.Round)
                    drawLine(color, Offset(size.width * .24f, size.height * .5f), Offset(size.width * .48f, size.height * .75f), stroke, StrokeCap.Round)
                }
            }
            Text(
                title,
                modifier = Modifier.weight(1f).padding(start = 2.dp, end = 4.dp),
                color = ConsumerColors.TextOnDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(enabled = !busy, onClick = onSave, contentPadding = PaddingValues(horizontal = 5.dp)) {
                Text(voiceCloudTitleCase(if (saved) "Saved" else "Save"), color = ConsumerColors.Ice, maxLines = 1)
            }
            TextButton(enabled = !busy, onClick = onReport, contentPadding = PaddingValues(horizontal = 5.dp)) {
                Text(voiceCloudTitleCase("Report"), color = ConsumerColors.TextOnDarkSecondary, maxLines = 1)
            }
            TextButton(onClick = onLeave, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text(voiceCloudTitleCase("Leave"), color = CommonColors.Error, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun LiveFloatingAction(iconRes: Int, description: String, onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = ConsumerColors.LiveSurfaceElevated,
        contentColor = ConsumerColors.TextOnDark,
        modifier = Modifier.border(1.dp, ConsumerColors.VipGold.copy(alpha = .42f), CircleShape),
    ) {
        Icon(painterResource(iconRes), contentDescription = description, tint = if (description == "Gifts") ConsumerColors.VipGold else ConsumerColors.TextOnDark, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun RoomRuntimeHeader(state: LiveRoomUiState, onRetryAudio: () -> Unit) {
    val rtcLabel = when (state.rtcState) {
        RtcAudioState.Connected, RtcAudioState.Reconnected -> "Crystal clear audio"
        RtcAudioState.Connecting -> "Connecting audio…"
        RtcAudioState.Reconnecting -> "Reconnecting…"
        is RtcAudioState.Failed -> "Audio interrupted"
        RtcAudioState.Disconnected -> if (state.inRoom) "Disconnected" else "Preparing room"
    }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(ConsumerColors.LiveSurfaceElevated, ConsumerColors.DeepNavy)))
            .border(1.dp, ConsumerColors.VipGold.copy(alpha = .36f), RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VoiceCloudLiveBadge()
                Spacer(Modifier.width(8.dp))
                VoiceCloudAudioArtwork(Modifier.size(34.dp), dark = true)
                Spacer(Modifier.width(8.dp))
                Text(voiceCloudTitleCase(rtcLabel), color = ConsumerColors.TextOnDark, fontWeight = FontWeight.SemiBold)
            }
            state.room?.let { room ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(room.title.ifBlank { "Live room" }, color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(voiceCloudTitleCase("${room.listenerCount} listeners  •  ${room.speakerCount} speakers"), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Box(Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))) {
                        VoiceCloudRemoteMedia(room.coverUrl, room.title, Modifier.fillMaxSize(), VoiceCloudVisualKind.LIVE, dark = true)
                    }
                }
            }
            VoiceCloudAnimatedWaveform(Modifier.fillMaxWidth().height(38.dp), color = ConsumerColors.Ice, active = state.inRoom && !state.paused)
            if (state.rtcState is RtcAudioState.Failed || (state.rtcState is RtcAudioState.Disconnected && state.inRoom)) {
                TextButton(onClick = onRetryAudio) { Text(voiceCloudTitleCase("Retry audio"), color = ConsumerColors.Ice, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun ListenerActions(state: LiveRoomUiState, onToggleHand: () -> Unit) {
    Button(onClick = onToggleHand, enabled = !state.mutationBusy && !state.paused, modifier = Modifier.fillMaxWidth()) {
        Text(voiceCloudTitleCase(if (state.handRaised) "Lower hand" else "Raise hand"), maxLines = 1)
    }
}

@Composable
private fun StageRoster(participants: List<RtcPresenceState>, viewerId: String?) {
    val speakers = participants.filter { it.role.lowercase() != "listener" || it.isSpeaking }
    val listeners = participants.filter { it !in speakers }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (speakers.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                itemsIndexed(speakers, key = { index, participant -> "stage:${participant.userId}:$index" }) { _, participant ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(78.dp)) {
                        VoiceCloudSpeakingAvatar(
                            label = participant.username?.takeIf { it.isNotBlank() } ?: if (participant.userId == viewerId) "You" else "V",
                            speaking = participant.isSpeaking,
                            size = 58.dp,
                            dark = true,
                        )
                        Text(
                            participant.username?.takeIf { it.isNotBlank() } ?: if (participant.userId == viewerId) "You" else "Member",
                            color = ConsumerColors.TextOnDark,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        val stageState = when {
                            participant.handRaised -> "Hand raised"
                            participant.isSpeaking -> "Speaking"
                            participant.isMuted && participant.role.lowercase() != "listener" -> "Muted"
                            else -> participant.role.replace('_', ' ')
                        }
                        Text(
                            voiceCloudTitleCase(stageState),
                            color = if (participant.isSpeaking) ConsumerColors.Ice else if (participant.handRaised) ConsumerColors.VipGold else ConsumerColors.TextOnDarkSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        if (listeners.isNotEmpty()) {
            Surface(color = ConsumerColors.LiveSurface.copy(alpha = .78f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(voiceCloudTitleCase("Listeners"), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        itemsIndexed(listeners.take(18), key = { index, participant -> "listener:${participant.userId}:$index" }) { _, participant ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(62.dp)) {
                                VoiceCloudSpeakingAvatar(
                                    label = participant.username?.takeIf { it.isNotBlank() } ?: if (participant.userId == viewerId) "You" else "V",
                                    speaking = false,
                                    size = 34.dp,
                                    dark = true,
                                )
                                Text(
                                    participant.username?.takeIf { it.isNotBlank() } ?: if (participant.userId == viewerId) "You" else "Member",
                                    color = ConsumerColors.TextOnDarkSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                                if (participant.handRaised) Text(voiceCloudTitleCase("✋"), color = ConsumerColors.VipGold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (listeners.size > 18) item {
                            Surface(shape = CircleShape, color = ConsumerColors.LiveSurfaceElevated) {
                                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                    Text(voiceCloudTitleCase("+${listeners.size - 18}"), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantCard(participant: RtcPresenceState, viewerId: String?) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = ConsumerColors.LiveSurface), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            VoiceCloudSpeakingAvatar(participant.username ?: "V", speaking = participant.isSpeaking, size = 42.dp, dark = true)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(participant.username?.takeIf { it.isNotBlank() } ?: if (participant.userId == viewerId) "You" else "${VoiceCloudBrand.name} member", color = ConsumerColors.TextOnDark, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(voiceCloudTitleCase(participant.role.replace('_', ' ')), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            if (participant.handRaised) Text(voiceCloudTitleCase("✋"), color = ConsumerColors.VipGold)
            if (participant.isSpeaking) Text(voiceCloudTitleCase("Speaking"), color = ConsumerColors.Ice, style = MaterialTheme.typography.labelLarge)
            else if (participant.isMuted && participant.role.lowercase() != "listener") Text(voiceCloudTitleCase("Muted"), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ChatMessageCard(message: RoomChatMessage, viewerId: String?, onReaction: (String, String) -> Unit) {
    val own = message.senderId == viewerId
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (own) Alignment.End else Alignment.Start) {
        Surface(
            color = if (own) ConsumerColors.Sapphire else ConsumerColors.LiveSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 310.dp),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                if (!own) Text(message.sender?.displayName ?: message.sender?.username ?: "${VoiceCloudBrand.name} member", color = ConsumerColors.Ice, style = MaterialTheme.typography.labelLarge)
                Text(message.content.orEmpty(), color = ConsumerColors.TextOnDark)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf("❤️", "👏", "😂").forEach { emoji -> TextButton(onClick = { onReaction(message.id, emoji) }, contentPadding = PaddingValues(horizontal = 6.dp)) { Text(emoji) } }
        }
    }
}

@Composable
private fun ChatComposer(enabled: Boolean, onSend: (String) -> Unit, modifier: Modifier = Modifier, light: Boolean = false) {
    var text by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    fun submit() {
        val value = text.trim()
        if (value.isNotEmpty() && enabled) { onSend(value); text = ""; keyboard?.hide() }
    }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it.take(1000) },
        modifier = modifier,
        enabled = enabled,
        placeholder = { Text(voiceCloudTitleCase("Message The Room")) },
        minLines = 1,
        maxLines = 4,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { submit() }),
        trailingIcon = { TextButton(onClick = { submit() }, enabled = enabled && text.isNotBlank()) { Text(voiceCloudTitleCase("Send")) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = if (light) ConsumerColors.Ink else ConsumerColors.TextOnDark,
            unfocusedTextColor = if (light) ConsumerColors.Ink else ConsumerColors.TextOnDark,
            focusedBorderColor = ConsumerColors.Sapphire,
            unfocusedBorderColor = if (light) ConsumerColors.Border else ConsumerColors.DarkBorder,
            focusedLabelColor = ConsumerColors.Sapphire,
            unfocusedLabelColor = if (light) ConsumerColors.TextMuted else ConsumerColors.TextOnDarkSecondary,
            focusedContainerColor = if (light) Color.White else Color.Transparent,
            unfocusedContainerColor = if (light) Color.White else Color.Transparent,
        ),
    )
}

@Composable
private fun GiftRow(gifts: List<GiftCatalogItem>, busy: Boolean, onGift: (GiftCatalogItem) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(gifts.distinctBy { it.id }.take(20), key = { index, gift -> "live-gift:${gift.id}:$index" }) { _, gift ->
            ElevatedCard(
                modifier = Modifier.width(132.dp).clickable(enabled = !busy) { onGift(gift) },
                colors = CardDefaults.elevatedCardColors(containerColor = ConsumerColors.LiveSurfaceElevated),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(voiceCloudTitleCase("🎁"), style = MaterialTheme.typography.headlineMedium)
                    Text(gift.name, color = ConsumerColors.TextOnDark, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(voiceCloudTitleCase("${gift.coinPrice} Coins"), color = ConsumerColors.VipGold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ReactionsStrip(reactions: List<RoomReaction>) {
    if (reactions.isEmpty()) return
    Surface(color = ConsumerColors.LiveSurface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Text(reactions.joinToString("  ") { it.emoji }, Modifier.padding(12.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun SectionHeading(title: String, meta: String?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(voiceCloudTitleCase(title), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (!meta.isNullOrBlank()) Text(meta, color = ConsumerColors.TextOnDarkSecondary)
    }
}

@Composable
private fun AccessIssueCard(issue: RoomAccessIssue, onBack: () -> Unit) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = ConsumerColors.LiveSurface), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(voiceCloudTitleCase(issue.title), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.titleLarge)
            Text(voiceCloudTitleCase(issue.message), color = ConsumerColors.TextOnDarkSecondary)
            OutlinedButton(onClick = onBack) { Text(voiceCloudTitleCase("Return To Rooms")) }
        }
    }
}

@Composable
private fun DarkMessageCard(message: String, error: Boolean, action: (() -> Unit)?) {
    Surface(color = if (error) CommonColors.Error.copy(alpha = .18f) else ConsumerColors.LiveSurface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(voiceCloudTitleCase(message), color = ConsumerColors.TextOnDark, modifier = Modifier.weight(1f))
            if (action != null) TextButton(onClick = action) { Text(voiceCloudTitleCase(if (error) "Retry" else "Continue")) }
        }
    }
}

@Composable private fun LoadingBlock() = Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
