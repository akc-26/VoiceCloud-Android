package app.voicecloud.android.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.voicecloud.android.BuildConfig
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCDialog
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSelectionRow
import app.voicecloud.core.designsystem.component.VCSettingsRow
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion
import app.voicecloud.core.designsystem.theme.rememberVoiceCloudMotionEnabled
import app.voicecloud.core.preferences.ThemePreference

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onThemeSelected: (ThemePreference) -> Unit = {},
    onOpenCreatorWorkspace: (() -> Unit)? = null,
    onOpenGuestUpgrade: (() -> Unit)? = null,
    onOpenSessions: (() -> Unit)? = null,
    onOpenLoginHistory: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
) {
    val spacing = VoiceCloud.spacing
    val motionEnabled = rememberVoiceCloudMotionEnabled()
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize()) {
        VCPageHeader(title = "Settings", subtitle = "Account, experience, and safety", onBack = onBack)
        when {
            state.errorMessage != null -> VCErrorState(title = "Couldn't load settings", message = state.errorMessage)
            state.isLoading -> Column(
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
                LazyColumn(
                    contentPadding = PaddingValues(bottom = spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    item { VCSectionHeader(title = "Account") }
                    item {
                        Column(Modifier.padding(horizontal = spacing.pageGutter), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                            if (state.accountDisplayName != null || state.accountHandle != null) {
                                state.accountDisplayName?.let {
                                    Text(it, style = VoiceCloud.typography.actionLabel, color = VoiceCloud.colors.textPrimary)
                                }
                                state.accountHandle?.let {
                                    Text(it, style = VoiceCloud.typography.bodySecondary, color = VoiceCloud.colors.textSecondary)
                                }
                            } else {
                                SettingsMuted("Sign in to view account details.")
                            }
                        }
                    }
                    if (onOpenGuestUpgrade != null) {
                        item {
                            VCSettingsRow(
                                title = "Upgrade guest account",
                                subtitle = "Add email and password to keep your profile",
                                icon = VoiceCloudIcons.Profile,
                                onClick = onOpenGuestUpgrade,
                            )
                        }
                    }
                    if (onOpenCreatorWorkspace != null) {
                        item {
                            VCSettingsRow(
                                title = "Creator workspace",
                                subtitle = "Open creator tools",
                                icon = VoiceCloudIcons.CreatorTools,
                                onClick = onOpenCreatorWorkspace,
                            )
                        }
                    }
                    if (onSignOut != null) {
                        item {
                            VCSecondaryButton(
                                text = "Sign out",
                                onClick = { showSignOutDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.pageGutter, vertical = spacing.xs),
                            )
                        }
                    }
                    item { VCSectionHeader(title = "Experience") }
                    item {
                        VCSelectionRow(
                            title = "Light appearance",
                            subtitle = "VoiceCloud light theme",
                            selected = state.themePreference == ThemePreference.LIGHT,
                            onClick = { onThemeSelected(ThemePreference.LIGHT) },
                        )
                    }
                    item {
                        VCSelectionRow(
                            title = "Dark appearance",
                            subtitle = "VoiceCloud dark theme",
                            selected = state.themePreference == ThemePreference.DARK,
                            onClick = { onThemeSelected(ThemePreference.DARK) },
                        )
                    }
                    item {
                        VCSelectionRow(
                            title = "System appearance",
                            subtitle = "Follow device setting",
                            selected = state.themePreference == ThemePreference.SYSTEM,
                            onClick = { onThemeSelected(ThemePreference.SYSTEM) },
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Notifications",
                            subtitle = when {
                                state.pushNotificationsConfigured ->
                                    if (state.pushNotificationsEnabled) "Enabled in VoiceCloud" else "Disabled in VoiceCloud"
                                else -> "Unavailable until notification preferences are connected"
                            },
                            icon = VoiceCloudIcons.Notifications,
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Live audio experience",
                            subtitle = "Unavailable until live audio preferences are connected",
                            icon = VoiceCloudIcons.Live,
                            onClick = null,
                        )
                    }
                    item {
                        SettingsMuted(
                            if (motionEnabled) {
                                "Animations follow your device settings."
                            } else {
                                "Reduced motion is active based on your device settings."
                            },
                            Modifier.padding(horizontal = spacing.pageGutter),
                        )
                    }
                    item { VCSectionHeader(title = "Security") }
                    item {
                        VCSettingsRow(
                            title = "Active sessions",
                            subtitle = "Devices signed in to VoiceCloud",
                            icon = VoiceCloudIcons.Settings,
                            onClick = onOpenSessions,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Login activity",
                            subtitle = "Recent sign-ins on your account",
                            onClick = onOpenLoginHistory,
                        )
                    }
                    item { VCSectionHeader(title = "Safety") }
                    item {
                        VCSettingsRow(
                            title = "Privacy",
                            subtitle = "Unavailable until privacy controls are connected",
                            icon = VoiceCloudIcons.Profile,
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Blocked accounts",
                            subtitle = "Unavailable until blocking is connected",
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Report a problem",
                            subtitle = "Unavailable until reporting is connected",
                            icon = VoiceCloudIcons.Report,
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Moderation & safety",
                            subtitle = "Unavailable until safety settings are connected",
                            onClick = null,
                        )
                    }
                    item { VCSectionHeader(title = "Support") }
                    item {
                        VCSettingsRow(
                            title = "Help",
                            subtitle = if (state.helpAvailable) "VoiceCloud help" else "Unavailable until help is connected",
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "About VoiceCloud",
                            subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.ENVIRONMENT})",
                            onClick = null,
                        )
                    }
                    item {
                        VCSettingsRow(
                            title = "Legal & policies",
                            subtitle = if (state.legalLinksAvailable) "Terms and privacy policy" else "Unavailable until legal links are connected",
                            onClick = null,
                        )
                    }
                    if (state.accountDisplayName == null && !state.pushNotificationsConfigured && !state.helpAvailable) {
                        item {
                            VCEmptyState(
                                title = "More settings coming soon",
                                message = "Additional privacy, notification, and support controls will appear as VoiceCloud enables them for your account.",
                            )
                        }
                    }
                }
            }
        }
    }
    VCDialog(
        visible = showSignOutDialog,
        title = "Sign out?",
        message = "You will leave your VoiceCloud session on this device.",
        confirmLabel = "Sign out",
        dismissLabel = "Cancel",
        onDismiss = { showSignOutDialog = false },
        onConfirm = {
            showSignOutDialog = false
            onSignOut?.invoke()
        },
    )
}

@Composable
private fun SettingsMuted(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = VoiceCloud.typography.bodySecondary,
        color = VoiceCloud.colors.textMuted,
        modifier = modifier.padding(vertical = VoiceCloud.spacing.xxs),
    )
}
