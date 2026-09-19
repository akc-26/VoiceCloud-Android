package app.voicecloud.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.voicecloud.core.designsystem.R
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.CreatorColors

/**
 * Board-authority components derived from the approved VoiceCloud Android presentation.
 * These deliberately stay compact and information-dense: feature screens own state/actions,
 * while this file owns the visual geometry shared by the supplied reference boards.
 */
object VoiceCloudApprovedMetrics {
    val pagePadding: Dp = 20.dp
    val cardRadius: Dp = 16.dp
    val fieldRadius: Dp = 12.dp
    val buttonRadius: Dp = 28.dp
    val primaryButtonHeight: Dp = 50.dp
    val compactCardPadding: Dp = 14.dp
    val sectionGap: Dp = 16.dp
    val rowGap: Dp = 10.dp
}

@Composable
fun VoiceCloudApprovedPage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.background(ConsumerColors.Surface)) { content() }
}

@Composable
fun VoiceCloudApprovedTopBar(
    title: String,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    avatarLabel: String = title,
    onSearch: (() -> Unit)? = null,
    onNotifications: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            onBack != null -> Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(36.dp),
            ) { Box(contentAlignment = Alignment.Center) { Text("‹", style = MaterialTheme.typography.headlineSmall, color = ConsumerColors.Ink) } }
            avatarUrl != null -> VoiceCloudAvatar(avatarUrl, avatarLabel, size = 30.dp, verified = false)
        }
        Text(
            voiceCloudTitleCase(title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ConsumerColors.Ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onSearch != null) VoiceCloudApprovedIconButton(R.drawable.vc_icon_search, "Search", onSearch)
        if (onNotifications != null) VoiceCloudApprovedIconButton(R.drawable.vc_icon_bell, "Notifications", onNotifications)
    }
}

@Composable
fun VoiceCloudApprovedIconButton(@DrawableRes icon: Int, contentDescription: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = Color.Transparent, modifier = Modifier.size(36.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription, tint = ConsumerColors.Ink, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun VoiceCloudApprovedPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(VoiceCloudApprovedMetrics.primaryButtonHeight),
        shape = RoundedCornerShape(VoiceCloudApprovedMetrics.buttonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = ConsumerColors.Sapphire,
            contentColor = Color.White,
            disabledContainerColor = ConsumerColors.Sapphire.copy(alpha = .38f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
    ) { Text(voiceCloudTitleCase(text), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
}

@Composable
fun VoiceCloudApprovedSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(VoiceCloudApprovedMetrics.buttonRadius),
        border = BorderStroke(1.dp, ConsumerColors.Sapphire.copy(alpha = .42f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ConsumerColors.SapphireDeep),
    ) { Text(voiceCloudTitleCase(text), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun VoiceCloudApprovedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable (() -> Unit))? = null,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(voiceCloudTitleCase(label), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = visualTransformation,
            trailingIcon = trailing,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(VoiceCloudApprovedMetrics.fieldRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ConsumerColors.Sapphire,
                unfocusedBorderColor = ConsumerColors.Border,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
        )
    }
}

@Composable
fun VoiceCloudApprovedSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search rooms, hosts, topics...",
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().height(48.dp),
        singleLine = true,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted) },
        leadingIcon = { Icon(painterResource(R.drawable.vc_icon_search), null, tint = ConsumerColors.TextMuted, modifier = Modifier.size(18.dp)) },
        shape = RoundedCornerShape(18.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ConsumerColors.Sapphire.copy(alpha = .55f),
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = ConsumerColors.SurfaceSoft,
            unfocusedContainerColor = ConsumerColors.SurfaceSoft,
        ),
    )
}

@Composable
fun VoiceCloudApprovedChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (selected) ConsumerColors.Sapphire else ConsumerColors.SurfaceSoft,
        border = BorderStroke(1.dp, if (selected) ConsumerColors.Sapphire else ConsumerColors.Border.copy(alpha = .7f)),
    ) {
        Text(
            voiceCloudTitleCase(text),
            Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else ConsumerColors.Text,
        )
    }
}

@Composable
fun VoiceCloudApprovedSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(voiceCloudTitleCase(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(action, style = MaterialTheme.typography.labelSmall, color = ConsumerColors.Sapphire, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onAction).padding(8.dp))
        }
    }
}

@Composable
fun VoiceCloudApprovedCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = VoiceCloudApprovedMetrics.compactCardPadding,
    dark: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(VoiceCloudApprovedMetrics.cardRadius)
    val baseModifier = modifier
        .shadow(if (dark) 3.dp else 2.dp, shape, ambientColor = Color.Black.copy(alpha = .05f), spotColor = Color.Black.copy(alpha = .04f))
        .clip(shape)
        .border(1.dp, if (dark) ConsumerColors.Ice.copy(alpha = .14f) else ConsumerColors.Border.copy(alpha = .75f), shape)
    val actualModifier = if (onClick != null) baseModifier.clickable(onClick = onClick) else baseModifier
    Column(
        actualModifier.background(if (dark) ConsumerColors.LiveSurface else Color.White).padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) { content() }
}

@Composable
fun VoiceCloudApprovedFeaturedRoom(
    title: String,
    subtitle: String,
    imageUrl: String?,
    listeners: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier.fillMaxWidth().height(168.dp).clip(shape).clickable(onClick = onClick)
            .background(ConsumerColors.DeepNavy)
    ) {
        VoiceCloudRemoteMedia(imageUrl, title, Modifier.fillMaxSize(), VoiceCloudVisualKind.LIVE, dark = true, contentScale = ContentScale.Crop, fallbackDrawable = R.drawable.vc_ref_home_mic)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(ConsumerColors.DeepNavy.copy(alpha = .92f), ConsumerColors.DeepNavy.copy(alpha = .48f), Color.Transparent))))
        Column(Modifier.align(Alignment.BottomStart).padding(14.dp).fillMaxWidth(.72f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextOnDarkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(50), color = Color(0xFFE84C4C)) { Text("LIVE", Modifier.padding(horizontal = 7.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold) }
                Text(listeners, style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
fun VoiceCloudApprovedRoomRow(
    title: String,
    subtitle: String,
    imageUrl: String?,
    meta: String,
    modifier: Modifier = Modifier,
    live: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        VoiceCloudRemoteMedia(imageUrl, title, Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)), VoiceCloudVisualKind.LIVE, dark = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ConsumerColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (live) Surface(shape = RoundedCornerShape(50), color = Color(0xFFE84C4C)) { Text("LIVE", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.White) }
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(meta, style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
    }
}

@Composable
fun VoiceCloudApprovedPersonRow(
    name: String,
    subtitle: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    online: Boolean = false,
    verified: Boolean = false,
    onClick: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null,
) {
    val rowModifier = if (onClick != null) modifier.fillMaxWidth().clickable(onClick = onClick) else modifier.fillMaxWidth()
    Row(rowModifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        VoiceCloudAvatar(avatarUrl, name, size = 42.dp, online = online, verified = verified)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ConsumerColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ConsumerColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (actionLabel != null && onAction != null) {
            OutlinedButton(onClick = onAction, shape = RoundedCornerShape(50), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 5.dp), modifier = Modifier.height(34.dp)) {
                Text(voiceCloudTitleCase(actionLabel), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun VoiceCloudApprovedMetricRow(
    metrics: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        metrics.forEachIndexed { index, (value, label) ->
            if (index > 0) Spacer(Modifier.width(1.dp).height(32.dp).background(if (dark) Color.White.copy(alpha = .13f) else ConsumerColors.Border.copy(alpha = .7f)))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (dark) Color.White else ConsumerColors.Ink)
                Text(voiceCloudTitleCase(label), style = MaterialTheme.typography.labelSmall, color = if (dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted)
            }
        }
    }
}

@Composable
fun VoiceCloudApprovedStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    delta: String? = null,
) {
    VoiceCloudApprovedCard(modifier, contentPadding = 10.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ConsumerColors.Ink)
            Text(voiceCloudTitleCase(label), style = MaterialTheme.typography.labelSmall, color = ConsumerColors.TextMuted)
            if (!delta.isNullOrBlank()) Text(delta, style = MaterialTheme.typography.labelSmall, color = CreatorColors.Primary, fontWeight = FontWeight.SemiBold)
        }
    }
}
