package app.voicecloud.android.ui.room

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCAudioWaveform
import app.voicecloud.core.designsystem.component.VCAvatar
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCLiveBadge
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCScaffold
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCSpeakingAvatar
import app.voicecloud.core.designsystem.foundation.vcTouchTarget
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
import androidx.compose.foundation.clickable

@Composable
fun RoomPreviewScreen(
    state: RoomPreviewUiState,
    onBack: () -> Unit,
    onJoinRoom: () -> Unit,
    modifier: Modifier = Modifier,
    onSave: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    VCScaffold(modifier = modifier) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.liveSurface)
                .padding(padding),
        ) {
            when {
            state.errorMessage != null || state.status == RoomPreviewStatus.Error -> VCErrorState(
                title = "Couldn't load room",
                message = state.errorMessage ?: "Something went wrong while loading this room.",
            )
            state.isLoading || state.status == RoomPreviewStatus.Loading -> RoomPreviewLoading(onBack = onBack)
            state.status == RoomPreviewStatus.Unavailable || state.status == RoomPreviewStatus.Ended -> VCEmptyState(
                title = if (state.status == RoomPreviewStatus.Ended) "Room ended" else "Room unavailable",
                message = state.errorMessage ?: "This room is not available right now.",
            )
            else -> AnimatedVisibility(
                visible = true,
                enter = if (motionEnabled) fadeIn(tween(VoiceCloudMotion.EntranceMs)) else fadeIn(tween(0)),
            ) {
                RoomPreviewContent(
                    state = state,
                    onBack = onBack,
                    onJoinRoom = onJoinRoom,
                    onSave = onSave,
                    onShare = onShare,
                    onReport = onReport,
                )
            }
            }
        }
    }
}

@Composable
private fun RoomPreviewLoading(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = VoiceCloud.spacing
    Column(modifier.fillMaxSize()) {
        RoomPreviewBackButton(onBack = onBack)
        Column(
            Modifier.padding(horizontal = spacing.pageGutter, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            VCSkeleton()
            VCSkeleton()
            VCSkeleton()
        }
    }
}

@Composable
private fun RoomPreviewContent(
    state: RoomPreviewUiState,
    onBack: () -> Unit,
    onJoinRoom: () -> Unit,
    onSave: (() -> Unit)?,
    onShare: (() -> Unit)?,
    onReport: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    val room = state.room
    val joinEnabled = state.status == RoomPreviewStatus.Live ||
        state.status == RoomPreviewStatus.Ready ||
        state.status == RoomPreviewStatus.Private ||
        state.status == RoomPreviewStatus.Locked
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.liveSurface)
                    .padding(bottom = spacing.lg),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.pageGutter),
                ) {
                    RoomPreviewBackButton(onBack = onBack)
                    Spacer(Modifier.height(spacing.sm))
                    if (state.status == RoomPreviewStatus.Live || room.live) {
                        VCLiveBadge()
                        Spacer(Modifier.height(spacing.sm))
                    }
                    Text(
                        text = room.title,
                        style = VoiceCloud.typography.screenTitle,
                        color = colors.textOnDark,
                        modifier = Modifier.semantics { heading() },
                    )
                    state.topic?.let { topic ->
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            text = topic,
                            style = VoiceCloud.typography.bodySecondary,
                            color = colors.textOnDark.copy(alpha = 0.85f),
                        )
                    }
                    state.accessLabel?.let { access ->
                        Spacer(Modifier.height(spacing.sm))
                        Text(
                            text = access,
                            style = VoiceCloud.typography.metadata,
                            color = colors.textOnDark.copy(alpha = 0.75f),
                            modifier = Modifier.semantics { contentDescription = "Room access: $access" },
                        )
                    }
                    if (state.status == RoomPreviewStatus.Live || room.live) {
                        Spacer(Modifier.height(spacing.md))
                        VCAudioWaveform(active = true, color = colors.accent)
                    }
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.background,
                shape = VoiceCloud.shapes.hero,
                tonalElevation = VoiceCloud.elevation.card,
            ) {
                Column(
                    Modifier.padding(vertical = spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    room.hostLabel?.let { host ->
                        VCSectionHeader(title = "Host")
                        Row(
                            Modifier.padding(horizontal = spacing.pageGutter),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            VCAvatar(name = host, contentDescription = "Host $host")
                            Column {
                                Text(host, style = VoiceCloud.typography.actionLabel, color = colors.textPrimary)
                                Text("Hosting this room", style = VoiceCloud.typography.caption, color = colors.textSecondary)
                            }
                        }
                    } ?: RoomPreviewDetail(
                        title = "Host",
                        message = "Host information will appear here when this room is connected.",
                    )
                    VCSectionHeader(title = "Speaking now")
                    if (state.speakers.isEmpty()) {
                        RoomPreviewDetail(
                            message = "Speakers will appear here when this room is connected.",
                        )
                    } else {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.pageGutter),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            state.speakers.take(4).forEach { speaker ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    VCSpeakingAvatar(
                                        name = speaker.name,
                                        speaking = speaker.speaking,
                                        image = speaker.avatar,
                                    )
                                    Spacer(Modifier.height(spacing.xxs))
                                    Text(
                                        text = speaker.name,
                                        style = VoiceCloud.typography.caption,
                                        color = colors.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                    state.audienceLabel?.let { audience ->
                        VCSectionHeader(title = "In the room")
                        Text(
                            text = audience,
                            style = VoiceCloud.typography.bodySecondary,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = spacing.pageGutter),
                        )
                    }
                    RoomPreviewDetail(
                        title = "When you join",
                        message = "You will enter the live audio room using the existing VoiceCloud realtime experience when it is connected.",
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            tonalElevation = VoiceCloud.elevation.elevated,
            shadowElevation = VoiceCloud.elevation.subtle,
        ) {
            Column(
                Modifier.padding(spacing.pageGutter),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                AnimatedVisibility(
                    visible = joinEnabled,
                    enter = fadeIn(tween(VoiceCloudMotion.MicroMs)),
                ) {
                    VCPrimaryButton(
                        text = "Join room",
                        onClick = onJoinRoom,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = joinEnabled,
                    )
                }
                if (onSave != null || onShare != null || onReport != null) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        onSave?.let { save ->
                            RoomPreviewTextAction(label = "Save", onClick = save, modifier = Modifier.weight(1f))
                        }
                        onShare?.let { share ->
                            RoomPreviewTextAction(label = "Share", onClick = share, modifier = Modifier.weight(1f))
                        }
                        onReport?.let { report ->
                            RoomPreviewTextAction(label = "Report", onClick = report, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomPreviewBackButton(onBack: () -> Unit) {
    val colors = VoiceCloud.colors
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
        contentDescription = "Navigate back",
        tint = colors.textOnDark,
        modifier = Modifier
            .vcTouchTarget()
            .clickable(role = Role.Button, onClick = onBack)
            .padding(VoiceCloud.spacing.xs),
    )
}

@Composable
private fun RoomPreviewDetail(
    title: String? = null,
    message: String,
) {
    val spacing = VoiceCloud.spacing
    val colors = VoiceCloud.colors
    Column(Modifier.padding(horizontal = spacing.pageGutter)) {
        title?.let {
            Text(it, style = VoiceCloud.typography.sectionTitle, color = colors.textPrimary)
            Spacer(Modifier.height(spacing.xxs))
        }
        Text(message, style = VoiceCloud.typography.bodySecondary, color = colors.textMuted)
    }
}

@Composable
private fun RoomPreviewTextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = VoiceCloud.typography.actionLabel,
        color = VoiceCloud.colors.primary,
        modifier = modifier
            .vcTouchTarget()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = VoiceCloud.spacing.xs),
    )
}
