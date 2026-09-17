package app.voicecloud.core.designsystem.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.component.VCAudioWaveform
import app.voicecloud.core.designsystem.component.VCAvatar
import app.voicecloud.core.designsystem.component.VCChip
import app.voicecloud.core.designsystem.component.VCCommunityCard
import app.voicecloud.core.designsystem.component.VCCommunityUiModel
import app.voicecloud.core.designsystem.component.VCEmptyState
import app.voicecloud.core.designsystem.component.VCErrorState
import app.voicecloud.core.designsystem.component.VCEventCard
import app.voicecloud.core.designsystem.component.VCEventUiModel
import app.voicecloud.core.designsystem.component.VCGiftTile
import app.voicecloud.core.designsystem.component.VCGiftUiModel
import app.voicecloud.core.designsystem.component.VCLiveAction
import app.voicecloud.core.designsystem.component.VCLiveBadge
import app.voicecloud.core.designsystem.component.VCMessageBubble
import app.voicecloud.core.designsystem.component.VCNavDestination
import app.voicecloud.core.designsystem.component.VCPageHeader
import app.voicecloud.core.designsystem.component.VCPersonRow
import app.voicecloud.core.designsystem.component.VCPersonUiModel
import app.voicecloud.core.designsystem.component.VCPrimaryButton
import app.voicecloud.core.designsystem.component.VCRankingRow
import app.voicecloud.core.designsystem.component.VCRoomCard
import app.voicecloud.core.designsystem.component.VCRoomCompactCard
import app.voicecloud.core.designsystem.component.VCRoomHero
import app.voicecloud.core.designsystem.component.VCRoomUiModel
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VCSectionHeader
import app.voicecloud.core.designsystem.component.VCSelectionRow
import app.voicecloud.core.designsystem.component.VCSettingsRow
import app.voicecloud.core.designsystem.component.VCSkeleton
import app.voicecloud.core.designsystem.component.VCSpeakingAvatar
import app.voicecloud.core.designsystem.component.VCStatCard
import app.voicecloud.core.designsystem.component.VCTab
import app.voicecloud.core.designsystem.component.VCTabBar
import app.voicecloud.core.designsystem.component.VCTextButton
import app.voicecloud.core.designsystem.component.VCWalletBalance
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons
import app.voicecloud.core.designsystem.theme.PortalTheme
import app.voicecloud.core.designsystem.theme.VoiceCloudTheme

/**
 * Preview-only catalog. Labels are layout fixtures, not product or backend data.
 */
@Composable
fun VoiceCloudDesignShowcase() {
    val spacing = VoiceCloud.spacing
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        VCPageHeader(title = "VoiceCloud", subtitle = "Design System 2.0")
        Row(
            Modifier.padding(horizontal = spacing.pageGutter),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            VoiceCloudBrandMark()
            VCAvatar(name = "Ada")
            VCSpeakingAvatar(name = "Ada", speaking = true)
            VCLiveBadge()
            VCAudioWaveform(active = true)
            VCLiveAction(onClick = {})
        }
        Text(
            "Typography",
            style = VoiceCloud.typography.sectionTitle,
            color = VoiceCloud.colors.textPrimary,
            modifier = Modifier.padding(horizontal = spacing.pageGutter),
        )
        Column(Modifier.padding(horizontal = spacing.pageGutter), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Text("Display", style = VoiceCloud.typography.display, color = VoiceCloud.colors.textPrimary)
            Text("Screen title", style = VoiceCloud.typography.screenTitle, color = VoiceCloud.colors.textPrimary)
            Text("Section title", style = VoiceCloud.typography.sectionTitle, color = VoiceCloud.colors.textPrimary)
            Text("Body copy for readable conversation and room context.", style = VoiceCloud.typography.body, color = VoiceCloud.colors.textPrimary)
            Text("Secondary body", style = VoiceCloud.typography.bodySecondary, color = VoiceCloud.colors.textSecondary)
            Text("Metadata", style = VoiceCloud.typography.metadata, color = VoiceCloud.colors.textMuted)
            Text("Action label", style = VoiceCloud.typography.actionLabel, color = VoiceCloud.colors.primary)
            Text("Caption", style = VoiceCloud.typography.caption, color = VoiceCloud.colors.textMuted)
        }
        Row(
            Modifier.padding(horizontal = spacing.pageGutter),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            VCPrimaryButton("Primary", onClick = {})
            VCSecondaryButton("Secondary", onClick = {})
            VCTextButton("Text", onClick = {})
        }
        Row(
            Modifier.padding(horizontal = spacing.pageGutter),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            VCChip("Following", selected = true, onClick = {})
            VCChip("Live", selected = false, onClick = {})
        }
        VCTabBar(
            tabs = listOf(VCTab("one", "Rooms"), VCTab("two", "People")),
            selectedId = "one",
            onSelect = {},
        )
        VCSectionHeader(title = "Rooms", actionLabel = "See all", onAction = {})
        VCRoomHero(room = VCRoomUiModel(title = "Room title", hostLabel = "Host name", metaLabel = "Meta", live = true), onClick = {})
        VCRoomCard(room = VCRoomUiModel(title = "Room title", subtitle = "Subtitle", metaLabel = "Meta", live = true), onClick = {}, modifier = Modifier.padding(horizontal = spacing.pageGutter))
        VCRoomCompactCard(room = VCRoomUiModel(title = "Room title", subtitle = "Subtitle", live = true), onClick = {}, modifier = Modifier.padding(horizontal = spacing.pageGutter))
        VCPersonRow(person = VCPersonUiModel(name = "Member name", subtitle = "Role label", speaking = true))
        Row(Modifier.fillMaxWidth().padding(horizontal = spacing.pageGutter), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            VCStatCard(label = "Label", value = "—", modifier = Modifier.weight(1f))
            VCStatCard(label = "Label", value = "—", modifier = Modifier.weight(1f))
        }
        VCWalletBalance(amountLabel = "—", modifier = Modifier.padding(horizontal = spacing.pageGutter))
        Row(Modifier.fillMaxWidth().padding(horizontal = spacing.pageGutter), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            VCGiftTile(gift = VCGiftUiModel(name = "Gift", costLabel = "—", selected = true), onClick = {}, modifier = Modifier.weight(1f))
            VCGiftTile(gift = VCGiftUiModel(name = "Gift", costLabel = "—"), onClick = {}, modifier = Modifier.weight(1f))
        }
        VCRankingRow(rankLabel = "1", title = "Title", valueLabel = "—")
        VCEventCard(event = VCEventUiModel(title = "Event title", timeLabel = "Time label", placeLabel = "Place label"), onClick = {}, modifier = Modifier.padding(horizontal = spacing.pageGutter))
        VCCommunityCard(community = VCCommunityUiModel(name = "Community", metaLabel = "Meta", description = "Description"), onClick = {}, modifier = Modifier.padding(horizontal = spacing.pageGutter))
        VCMessageBubble(text = "Incoming message", outgoing = false, metaLabel = "Meta", modifier = Modifier.padding(horizontal = spacing.pageGutter))
        VCMessageBubble(text = "Outgoing message", outgoing = true, modifier = Modifier.padding(horizontal = spacing.pageGutter))
        VCSettingsRow(title = "Settings row", subtitle = "Supporting text", icon = VoiceCloudIcons.Settings, onClick = {})
        VCSelectionRow(title = "Selection row", selected = true, onClick = {})
        VCSkeleton(modifier = Modifier.padding(horizontal = spacing.pageGutter))
        VCEmptyState(title = "Empty", message = "Nothing to show yet.", actionLabel = "Action", onAction = {})
        HorizontalDivider()
        VCErrorState(title = "Unavailable", message = "This component is waiting on real application state.", actionLabel = "Retry", onAction = {})
        app.voicecloud.core.designsystem.component.VCBottomNavigation(
            destinations = listOf(
                VCNavDestination("home", "Home", VoiceCloudIcons.Home),
                VCNavDestination("explore", "Explore", VoiceCloudIcons.Explore),
                VCNavDestination("live", "Live", VoiceCloudIcons.Live),
                VCNavDestination("messages", "Messages", VoiceCloudIcons.Messages),
                VCNavDestination("profile", "Profile", VoiceCloudIcons.Profile),
            ),
            selectedRoute = "home",
            onSelect = {},
        )
    }
}

@Preview(name = "Consumer Light", showBackground = true)
@Composable
private fun ShowcaseConsumerLight() {
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = false) { VoiceCloudDesignShowcase() }
}

@Preview(name = "Consumer Dark", showBackground = true)
@Composable
private fun ShowcaseConsumerDark() {
    VoiceCloudTheme(portal = PortalTheme.User, darkTheme = true) { VoiceCloudDesignShowcase() }
}

@Preview(name = "Creator Light", showBackground = true)
@Composable
private fun ShowcaseCreatorLight() {
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = false) { VoiceCloudDesignShowcase() }
}

@Preview(name = "Creator Dark", showBackground = true)
@Composable
private fun ShowcaseCreatorDark() {
    VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = true) { VoiceCloudDesignShowcase() }
}
