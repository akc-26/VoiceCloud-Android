package app.voicecloud.android.ui.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCAudioWaveform
import app.voicecloud.core.designsystem.component.VCBottomSheet
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCLiveBadge
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCSpeakingAvatar
import app.voicecloud.core.designsystem.component.VCTextButton
import app.voicecloud.core.designsystem.component.VCToast
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled

@Composable
fun HostLiveScreen(
    state: HostLiveUiState,
    mode: HostLiveMode,
    modifier: Modifier = Modifier,
    onConnect: (() -> Unit)? = null,
    onDisconnect: (() -> Unit)? = null,
    onToggleMute: (() -> Unit)? = null,
    onEndRoom: (() -> Unit)? = null,
    toastMessage: String? = null,
    onToastDismiss: () -> Unit = {},
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    var showEngagement by rememberSaveable { mutableStateOf(false) }
    var showModeration by rememberSaveable { mutableStateOf(false) }
    var showAdmin by rememberSaveable { mutableStateOf(false) }
    val title = when (mode) {
        HostLiveMode.Host -> "Live Studio"
        HostLiveMode.Audience -> "Live"
    }
    val subtitle = when (mode) {
        HostLiveMode.Host -> "Host controls"
        HostLiveMode.Audience -> "Listening in"
    }
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = title, subtitle = subtitle)
        when {
            state.errorMessage != null || state.sessionStatus == HostSessionStatus.Error -> VCErrorState(
                title = "Live unavailable",
                message = state.errorMessage ?: state.sessionStatus.statusMessage(mode),
            )
            state.isLoading || state.sessionStatus == HostSessionStatus.Connecting -> Column(
                Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                VCSkeleton()
                VCSkeleton()
            }
            else -> AnimatedVisibility(
                visible = true,
                enter = if (motionEnabled) fadeIn(tween(VoiceCloudMotion.EntranceMs)) else fadeIn(tween(0)),
            ) {
                HostLiveContent(
                    state = state,
                    mode = mode,
                    onConnect = onConnect,
                    onDisconnect = onDisconnect,
                    onToggleMute = onToggleMute,
                    onOpenEngagement = { showEngagement = true },
                    onOpenModeration = { showModeration = true },
                    onOpenAdmin = { showAdmin = true },
                )
            }
        }
        HostEngagementSheet(visible = showEngagement, onDismiss = { showEngagement = false })
        HostModerationSheet(visible = showModeration, onDismiss = { showModeration = false })
        HostAdminSheet(
            visible = showAdmin,
            onDismiss = { showAdmin = false },
            onEndRoom = onEndRoom,
        )
        VCToast(message = toastMessage, onDismiss = onToastDismiss)
    }
}

@Composable
private fun HostLiveContent(
    state: HostLiveUiState,
    mode: HostLiveMode,
    onConnect: (() -> Unit)?,
    onDisconnect: (() -> Unit)?,
    onToggleMute: (() -> Unit)?,
    onOpenEngagement: () -> Unit,
    onOpenModeration: () -> Unit,
    onOpenAdmin: () -> Unit,
) {
    val spacing = VoiceCloud.spacing
    val colors = VoiceCloud.colors
    val isLive = state.sessionStatus == HostSessionStatus.Live ||
        state.sessionStatus == HostSessionStatus.Speaking ||
        state.sessionStatus == HostSessionStatus.Muted
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.liveSurface)
                    .padding(spacing.pageGutter),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    if (isLive) {
                        VCLiveBadge()
                    }
                    Text(
                        text = state.roomTitle ?: if (mode == HostLiveMode.Host) "Your live room" else "Live room",
                        style = VoiceCloud.typography.screenTitle,
                        color = colors.textOnDark,
                    )
                    state.topicLabel?.let {
                        Text(it, style = VoiceCloud.typography.bodySecondary, color = colors.textOnDark.copy(alpha = 0.85f))
                    }
                    Text(
                        text = state.sessionStatus.statusMessage(mode),
                        style = VoiceCloud.typography.bodySecondary,
                        color = colors.textOnDark.copy(alpha = 0.8f),
                        modifier = Modifier.semantics {
                            contentDescription = "Live status: ${state.sessionStatus.statusMessage(mode)}"
                        },
                    )
                    if (state.hostSpeaking && isLive) {
                        VCAudioWaveform(active = true, color = colors.accent)
                    }
                }
            }
        }
        if (mode == HostLiveMode.Host) {
            item { VCSectionHeader(title = "Primary live controls") }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.pageGutter),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    onConnect?.let { connect ->
                        VCPrimaryButton(
                            text = "Go live",
                            onClick = connect,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    onDisconnect?.let { disconnect ->
                        VCSecondaryButton(
                            text = "Disconnect",
                            onClick = disconnect,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (onConnect == null && onDisconnect == null) {
                    HostLiveMuted(
                        "Live controls will use the existing VoiceCloud realtime stack when hosting is connected.",
                        modifier = Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.xs),
                    )
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.pageGutter),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    onToggleMute?.let { toggle ->
                        VCSecondaryButton(
                            text = if (state.hostMuted) "Unmute" else "Mute",
                            onClick = toggle,
                            modifier = Modifier.padding(horizontal = spacing.pageGutter),
                        )
                    }
                }
            }
        }
        item { VCSectionHeader(title = if (mode == HostLiveMode.Host) "Stage" else "Speaking now") }
        if (state.speakers.isEmpty()) {
            item {
                HostLiveMuted(
                    "Speakers will appear here when the live stage is connected.",
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        } else {
            items(state.speakers, key = { it.name }) { speaker ->
                if (speaker.speaking) {
                    Row(
                        Modifier.padding(horizontal = spacing.pageGutter),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        VCSpeakingAvatar(name = speaker.name, speaking = true, image = speaker.avatar)
                        Column {
                            Text(speaker.name, style = VoiceCloud.typography.actionLabel, color = colors.textPrimary)
                            speaker.subtitle?.let {
                                Text(it, style = VoiceCloud.typography.caption, color = colors.textSecondary)
                            }
                        }
                    }
                } else {
                    VCPersonRow(person = speaker)
                }
            }
        }
        item { VCSectionHeader(title = if (mode == HostLiveMode.Host) "Room" else "In the room") }
        state.audienceLabel?.let { audience ->
            item {
                Text(
                    text = audience,
                    style = VoiceCloud.typography.bodySecondary,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = spacing.pageGutter),
                )
            }
        } ?: item {
            HostLiveMuted(
                "Audience information will appear here when live room data is connected.",
                modifier = Modifier.padding(horizontal = spacing.pageGutter),
            )
        }
        if (state.listeners.isNotEmpty()) {
            items(state.listeners, key = { "listener-${it.name}" }) { listener ->
                VCPersonRow(person = listener)
            }
        }
        if (mode == HostLiveMode.Host) {
            item { VCSectionHeader(title = "More controls") }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.pageGutter),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    VCTextButton(text = "Engagement", onClick = onOpenEngagement)
                    VCTextButton(text = "Moderation", onClick = onOpenModeration)
                    VCTextButton(text = "Admin", onClick = onOpenAdmin)
                }
            }
        }
        if (state.roomTitle == null && state.speakers.isEmpty() && state.audienceLabel == null) {
            item {
                VCEmptyState(
                    title = if (mode == HostLiveMode.Host) "Live Studio is ready" else "Live is ready",
                    message = "Hosting and listening will use the existing VoiceCloud realtime experience when it is connected.",
                )
            }
        }
    }
}

@Composable
private fun HostEngagementSheet(visible: Boolean, onDismiss: () -> Unit) {
    VCBottomSheet(visible = visible, onDismiss = onDismiss, title = "Engagement") {
        VCEmptyState(
            title = "Engagement controls",
            message = "Reactions, gifts, and audience engagement will appear here when live engagement is connected.",
        )
    }
}

@Composable
private fun HostModerationSheet(visible: Boolean, onDismiss: () -> Unit) {
    VCBottomSheet(visible = visible, onDismiss = onDismiss, title = "Moderation") {
        VCEmptyState(
            title = "Moderation",
            message = "Moderation tools will use existing VoiceCloud room rules when that capability is connected.",
        )
    }
}

@Composable
private fun HostAdminSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onEndRoom: (() -> Unit)?,
) {
    VCBottomSheet(visible = visible, onDismiss = onDismiss, title = "Room administration") {
        Text(
            text = "Advanced room settings and administration appear here when supported.",
            style = VoiceCloud.typography.bodySecondary,
            color = VoiceCloud.colors.textSecondary,
        )
        if (onEndRoom != null) {
            VCPrimaryButton(
                text = "End room",
                onClick = onEndRoom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VoiceCloud.spacing.md),
            )
        }
    }
}

@Composable
private fun HostLiveMuted(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = VoiceCloud.typography.bodySecondary,
        color = VoiceCloud.colors.textMuted,
        modifier = modifier,
    )
}
