package app.voicecloud.feature.live.ui

import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.VoiceCloudBrand

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { VoiceCloudPageTopBar(title = "Room", onBack = onBack) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.loading) item { LoadingBlock() }
            state.room?.let { room ->
                item {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(ConsumerBrushes.Hero).padding(22.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(if (room.isLive) CommonColors.Error else MaterialTheme.colorScheme.outline))
                                Spacer(Modifier.width(8.dp))
                                Text(voiceCloudTitleCase(if (room.isLive) "LIVE" else room.status.ifBlank { "ROOM" }), color = ConsumerColors.SapphireDeep, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = onToggleSave, enabled = !state.mutationBusy) { Text(voiceCloudTitleCase(if (state.saved) "Saved" else "Save")) }
                            }
                            Text(room.title.ifBlank { "${VoiceCloudBrand.name} Room" }, style = MaterialTheme.typography.headlineMedium, color = ConsumerColors.Ink)
                            if (!room.description.isNullOrBlank()) Text(room.description, color = ConsumerColors.Text, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(voiceCloudTitleCase("${room.listenerCount} Listening"), style = MaterialTheme.typography.bodyMedium)
                                Text(voiceCloudTitleCase("${room.speakerCount} Speakers"), style = MaterialTheme.typography.bodyMedium)
                            }
                            val metadata = listOf(room.category, room.language).filter { it.isNotBlank() }
                            if (metadata.isNotEmpty()) Text(metadata.joinToString("  •  "), color = ConsumerColors.TextMuted)
                        }
                    }
                }
                val restrictions = room.restrictionLabels()
                if (restrictions.isNotEmpty()) item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(voiceCloudTitleCase("Access"), style = MaterialTheme.typography.titleMedium)
                        restrictions.forEach { label ->
                            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(14.dp)) {
                                Text(label, Modifier.padding(horizontal = 12.dp, vertical = 9.dp), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                item {
                    Button(
                        onClick = onJoin,
                        enabled = room.isJoinablePresentation() && !state.joining,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text(voiceCloudTitleCase(if (state.joining) "Joining…" else if (room.isJoinablePresentation()) "Join room" else "Unavailable"), maxLines = 1) }
                }
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
    val closeRoom: () -> Unit = { onBack() } // onDispose performs the authoritative leave exactly once.

    if (showReactions) {
        ModalBottomSheet(onDismissRequest = { showReactions = false }, containerColor = ConsumerColors.LiveSurfaceElevated) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(voiceCloudTitleCase("Send A Reaction"), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.titleLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(listOf("👏", "❤️", "🔥", "😂", "🎉", "😍", "😮", "💯", "🙌", "✨")) { _, emoji ->
                        FilledTonalButton(onClick = { onReaction(emoji); showReactions = false }) { Text(emoji, style = MaterialTheme.typography.headlineSmall) }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
    if (showGifts) {
        ModalBottomSheet(onDismissRequest = { showGifts = false }, containerColor = ConsumerColors.LiveSurfaceElevated) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(voiceCloudTitleCase("Send A Gift"), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.titleLarge)
                if (state.gifts.isEmpty()) Text(voiceCloudTitleCase("Gifts Are Unavailable For This Room."), color = ConsumerColors.TextOnDarkSecondary)
                else GiftRow(state.gifts, state.mutationBusy) { gift -> onGift(gift); showGifts = false }
                Spacer(Modifier.height(20.dp))
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
                Surface(color = ConsumerColors.DeepNavy, tonalElevation = 0.dp, shadowElevation = 8.dp) {
                    ChatComposer(
                        enabled = state.conversationId != null && !state.mutationBusy,
                        onSend = onSendMessage,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(ConsumerColors.DeepNavy)) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, top = 12.dp, end = 62.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { RoomRuntimeHeader(state, onRetryAudio) }
                if (state.joining) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ConsumerColors.Ice) } }
                state.accessIssue?.let { issue -> item { AccessIssueCard(issue, onBack) } }
                if (state.paused) item { DarkMessageCard("Room paused", false, null) }
                if (state.ended) item { DarkMessageCard("Room ended", false, onBack) }
                if (state.speakerInvitationPending) item {
                    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = ConsumerColors.LiveSurfaceElevated), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(voiceCloudTitleCase("Stage Invite"), color = ConsumerColors.TextOnDark, fontWeight = FontWeight.Bold)
                            Text(voiceCloudTitleCase("Join As A Speaker?"), color = ConsumerColors.TextOnDarkSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = onAcceptInvitation, enabled = !state.mutationBusy) { Text(voiceCloudTitleCase("Accept")) }
                                OutlinedButton(onClick = onRejectInvitation, enabled = !state.mutationBusy) { Text(voiceCloudTitleCase("Decline")) }
                            }
                        }
                    }
                }
                if (state.inRoom) {
                    item { ListenerActions(state, onToggleHand) }
                    item { SectionHeading("People", "${state.participants.size}") }
                    itemsIndexed(state.participants.distinctBy { it.userId }, key = { index, participant -> "live-person:${participant.userId}:$index" }) { _, participant ->
                        ParticipantCard(participant, viewerId)
                    }
                    item { ReactionsStrip(state.reactions) }
                    item { SectionHeading("Room chat", null) }
                    if (state.messages.isEmpty()) item { Text(voiceCloudTitleCase("Messages Sent In This Room Will Appear Here."), color = ConsumerColors.TextOnDarkSecondary) }
                    itemsIndexed(state.messages.distinctBy { it.id }, key = { index, message -> "live-chat:${message.id}:$index" }) { _, message ->
                        ChatMessageCard(message, viewerId, onMessageReaction)
                    }
                }
            }

            if (state.inRoom) {
                Column(
                    Modifier.align(Alignment.CenterEnd).padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LiveFloatingAction(R.drawable.vc_icon_emoji, "Reactions") { showReactions = true }
                    LiveFloatingAction(R.drawable.vc_icon_gift, "Gifts") { showGifts = true }
                }
            }
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
    SmallFloatingActionButton(onClick = onClick, containerColor = ConsumerColors.LiveSurfaceElevated, contentColor = ConsumerColors.TextOnDark) {
        Icon(painterResource(iconRes), contentDescription = description, tint = ConsumerColors.TextOnDark, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun RoomRuntimeHeader(state: LiveRoomUiState, onRetryAudio: () -> Unit) {
    val rtcLabel = when (state.rtcState) {
        RtcAudioState.Connected, RtcAudioState.Reconnected -> "Connected"
        RtcAudioState.Connecting -> "Connecting audio…"
        RtcAudioState.Reconnecting -> "Reconnecting…"
        is RtcAudioState.Failed -> "Audio interrupted"
        RtcAudioState.Disconnected -> if (state.inRoom) "Disconnected" else "Preparing"
    }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(ConsumerBrushes.Primary).padding(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(voiceCloudTitleCase("Live Audio · $rtcLabel"), color = Color.White, fontWeight = FontWeight.SemiBold)
            state.room?.let { room -> Text(voiceCloudTitleCase("${room.listenerCount} Listeners  •  ${room.speakerCount} Speakers"), color = Color.White.copy(alpha = .85f)) }
            if (state.rtcState is RtcAudioState.Failed || (state.rtcState is RtcAudioState.Disconnected && state.inRoom)) {
                TextButton(onClick = onRetryAudio) { Text(voiceCloudTitleCase("Retry Audio"), color = Color.White) }
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
private fun ParticipantCard(participant: RtcPresenceState, viewerId: String?) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = ConsumerColors.LiveSurface), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(ConsumerColors.Sapphire), contentAlignment = Alignment.Center) {
                Text((participant.username?.firstOrNull() ?: 'V').uppercaseChar().toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(participant.username?.takeIf { it.isNotBlank() } ?: if (participant.userId == viewerId) "You" else "${VoiceCloudBrand.name} member", color = ConsumerColors.TextOnDark, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val role = participant.role.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                Text(voiceCloudTitleCase(role), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            if (participant.handRaised) Text(voiceCloudTitleCase("✋"))
            if (participant.isSpeaking) Text(voiceCloudTitleCase("  Speaking"), color = ConsumerColors.Ice, style = MaterialTheme.typography.labelLarge)
            else if (participant.isMuted && participant.role.lowercase() != "listener") Text(voiceCloudTitleCase("  Muted"), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelLarge)
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
private fun ChatComposer(enabled: Boolean, onSend: (String) -> Unit, modifier: Modifier = Modifier) {
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
            focusedTextColor = ConsumerColors.TextOnDark,
            unfocusedTextColor = ConsumerColors.TextOnDark,
            focusedBorderColor = ConsumerColors.Ice,
            unfocusedBorderColor = ConsumerColors.DarkBorder,
            focusedLabelColor = ConsumerColors.Ice,
            unfocusedLabelColor = ConsumerColors.TextOnDarkSecondary,
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
