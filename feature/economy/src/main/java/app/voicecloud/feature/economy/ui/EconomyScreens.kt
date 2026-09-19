package app.voicecloud.feature.economy.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.VoiceCloudEmptyVisual
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudRemoteMedia
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedCard
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedPrimaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSecondaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedSectionTitle
import app.voicecloud.core.designsystem.component.VoiceCloudApprovedTopBar
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.feature.economy.billing.PaymentRail
import app.voicecloud.feature.economy.model.*

private fun EconomySection.visualKind(): VoiceCloudVisualKind = when (this) {
    EconomySection.WALLET -> VoiceCloudVisualKind.WALLET
    EconomySection.VIP -> VoiceCloudVisualKind.VIP
    EconomySection.GIFTS -> VoiceCloudVisualKind.GIFT
    EconomySection.TASKS, EconomySection.ACHIEVEMENTS, EconomySection.PROGRESSION, EconomySection.RANKINGS, EconomySection.REFERRALS -> VoiceCloudVisualKind.REWARD
    EconomySection.TICKETS -> VoiceCloudVisualKind.EVENT
    EconomySection.STORE -> VoiceCloudVisualKind.GIFT
}

/** Compact visual economy hub for wallet, membership, rewards and progression. */
@Composable
fun EconomyHubScreen(onOpen: (EconomySection) -> Unit, onBack: () -> Unit) {
    Scaffold(
        containerColor = ConsumerColors.Cloud,
        topBar = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                VoiceCloudApprovedTopBar("Wallet", onBack = onBack)
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = ConsumerColors.Sapphire),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("VoiceCloud Coins", color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelSmall)
                            Text("Wallet & Rewards", color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Secure · Transparent · Rewarding", color = ConsumerColors.VipGold, style = MaterialTheme.typography.labelSmall)
                        }
                        VoiceCloudRemoteMedia(
                            url = null,
                            contentDescription = "VoiceCloud wallet",
                            modifier = Modifier.size(width = 92.dp, height = 68.dp).clip(RoundedCornerShape(10.dp)),
                            kind = VoiceCloudVisualKind.WALLET,
                            dark = true,
                            fallbackDrawable = app.voicecloud.core.designsystem.R.drawable.vc_ref_wallet_spark,
                        )
                    }
                }
            }
            item { VoiceCloudApprovedSectionTitle("Quick access") }
            itemsIndexed(economyProfileOrder.chunked(2)) { _, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { section -> Box(Modifier.weight(1f)) { EconomyNavigationCard(section, onOpen) } }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun EconomyNavigationCard(section: EconomySection, onOpen: (EconomySection) -> Unit) {
    VoiceCloudApprovedCard(
        modifier = Modifier.fillMaxWidth().height(94.dp),
        onClick = { onOpen(section) },
        contentPadding = 11.dp,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            VoiceCloudPictogram(section.visualKind(), size = 38.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(voiceCloudTitleCase(section.label), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(voiceCloudTitleCase(section.subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun EconomySectionScreen(
    section: EconomySection,
    state: EconomyUiState,
    onLoad: () -> Unit,
    onBack: () -> Unit,
    onClaimCheckIn: () -> Unit,
    onClaimTask: (String) -> Unit,
    onBuyItem: (String) -> Unit,
    onEquip: (String) -> Unit,
    onUnequip: (String) -> Unit,
    onBuyTicket: (String) -> Unit,
    paymentRail: PaymentRail,
    onBuyWalletCredits: (Activity, String, String, (String) -> Unit) -> Unit,
    onBuyVip: (Activity, String, String, String?, (String) -> Unit) -> Unit,
    onRestoreWallet: () -> Unit,
    onRestoreVip: () -> Unit,
    onPaymentResume: () -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val openHostedCheckout: (String) -> Unit = remember(context) {
        { url -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, section) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) onPaymentResume() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(section) { onLoad() }
    VoiceCloudToastEffect(state.error, state.notice)
    Scaffold(
        containerColor = ConsumerColors.Cloud,
        topBar = { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { VoiceCloudApprovedTopBar(section.label, onBack = onBack) } },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { EconomyBoardHeader(section, state.payload) }
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (section == EconomySection.WALLET || section == EconomySection.VIP) item {
                PaymentRailCard(
                    rail = paymentRail,
                    section = section,
                    busy = state.busy,
                    onRestore = if (section == EconomySection.WALLET) onRestoreWallet else onRestoreVip,
                )
            }
            if (section == EconomySection.RANKINGS) item {
                Text(voiceCloudTitleCase("Live rankings appear when ranking data is available"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (section == EconomySection.PROGRESSION) item {
                VoiceCloudApprovedPrimaryButton("Claim Daily Check-In", enabled = !state.busy, onClick = onClaimCheckIn)
            }
            if (!state.loading && state.error == null) {
                val sections = payloadSections(state.payload)
                if (sections.isEmpty()) item { EconomyEmpty(section) }
                sections.forEach { data ->
                    if (sections.size > 1) item { Text(voiceCloudTitleCase(data.first), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    itemsIndexed(data.second, key = { index, item -> "${section.name}:${data.first}:$index:${item.stableId()}" }) { _, item ->
                        EconomyVisualCard(
                            section = section,
                            group = data.first,
                            item = item,
                            busy = state.busy,
                            onClaimTask = onClaimTask,
                            onBuyItem = onBuyItem,
                            onEquip = onEquip,
                            onUnequip = onUnequip,
                            onBuyTicket = onBuyTicket,
                            onBuyWalletCredits = { packageId, productId -> if (activity != null) onBuyWalletCredits(activity, packageId, productId, openHostedCheckout) },
                            onBuyVip = { tierId, productId, cycle -> if (activity != null) onBuyVip(activity, tierId, productId, cycle, openHostedCheckout) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EconomyBoardHeader(section: EconomySection, payload: Any?) {
    val sections = payloadSections(payload)
    val first = sections.asSequence().flatMap { it.second.asSequence() }.firstOrNull()
    val value = first?.firstDisplayValue("balance", "availableBalance", "coinBalance", "coins", "amount", "total", "points", "xp", "rank", "streak")
    val dark = section == EconomySection.VIP
    VoiceCloudApprovedCard(Modifier.fillMaxWidth(), contentPadding = 0.dp, dark = dark) {
        Box(Modifier.fillMaxWidth().height(if (dark) 132.dp else 108.dp)) {
            VoiceCloudRemoteMedia(
                url = null,
                contentDescription = section.label,
                modifier = Modifier.fillMaxSize(),
                kind = section.visualKind(),
                dark = dark,
                fallbackDrawable = when (section) {
                    EconomySection.WALLET -> app.voicecloud.core.designsystem.R.drawable.vc_ref_wallet_spark
                    EconomySection.VIP -> app.voicecloud.core.designsystem.R.drawable.vc_ref_vip_crown
                    else -> null
                },
            )
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(
                if (dark) ConsumerColors.LiveSurface.copy(alpha = .96f) else ConsumerColors.Surface.copy(alpha = .96f),
                if (dark) ConsumerColors.LiveSurface.copy(alpha = .72f) else ConsumerColors.Surface.copy(alpha = .72f),
                Color.Transparent,
            ))))
            Column(Modifier.align(Alignment.CenterStart).padding(14.dp).fillMaxWidth(.68f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(voiceCloudTitleCase(section.label), style = MaterialTheme.typography.labelSmall, color = if (dark) ConsumerColors.TextOnDarkSecondary else ConsumerColors.TextMuted)
                Text(value ?: voiceCloudTitleCase(section.subtitle), style = if (value != null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (dark) Color.White else ConsumerColors.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(when (section) {
                    EconomySection.WALLET -> "Secure wallet · Verified balance"
                    EconomySection.VIP -> "Premium access and benefits"
                    EconomySection.GIFTS -> "Send love. Share joy."
                    EconomySection.TASKS -> "Complete tasks. Earn rewards."
                    EconomySection.ACHIEVEMENTS -> "Celebrate your milestones"
                    else -> section.subtitle
                }, style = MaterialTheme.typography.labelSmall, color = if (dark) ConsumerColors.VipGold else ConsumerColors.TextMuted, maxLines = 2)
            }
        }
    }
}

@Composable
private fun EconomyEmpty(section: EconomySection) {
    VoiceCloudEmptyVisual("Nothing Here Yet", "New ${section.label} activity will appear here", Modifier.fillMaxWidth(), section.visualKind())
}

@Composable
private fun EconomyVisualCard(
    section: EconomySection,
    group: String,
    item: Map<String, Any?>,
    busy: Boolean,
    onClaimTask: (String) -> Unit,
    onBuyItem: (String) -> Unit,
    onEquip: (String) -> Unit,
    onUnequip: (String) -> Unit,
    onBuyTicket: (String) -> Unit,
    onBuyWalletCredits: (String, String) -> Unit,
    onBuyVip: (String, String, String?) -> Unit,
) {
    val title = item.text("title", "name", "label", "username", "roomTitle", "code").ifBlank { voiceCloudTitleCase(group.ifBlank { section.label }) }
    val id = item.text("id", "itemId", "taskId", "roomId", "scheduledRoomId")
    val hero = item.firstDisplayValue("balance", "availableBalance", "coinBalance", "coins", "amount", "price", "reward", "rewardAmount", "xp", "points", "level", "rank", "streak", "progress", "total")
    val details = item.conciseDetails()
    VoiceCloudApprovedCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 12.dp,
    ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VoiceCloudRemoteMedia(
                    url = item.text("imageUrl", "image", "coverUrl", "thumbnailUrl").takeIf(String::isNotBlank),
                    contentDescription = title,
                    modifier = Modifier.size(width = 58.dp, height = 54.dp).clip(RoundedCornerShape(10.dp)),
                    kind = section.visualKind(),
                    dark = section == EconomySection.VIP || section == EconomySection.GIFTS,
                    fallbackDrawable = when (section) {
                        EconomySection.WALLET -> app.voicecloud.core.designsystem.R.drawable.vc_ref_wallet_spark
                        EconomySection.VIP -> app.voicecloud.core.designsystem.R.drawable.vc_ref_vip_crown
                        else -> null
                    },
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(voiceCloudTitleCase(group.ifBlank { section.label }), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (hero != null) Text(hero, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = if (section == EconomySection.VIP || section == EconomySection.GIFTS) ConsumerColors.VioletDeep else ConsumerColors.SapphireDeep, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (details.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    details.take(2).forEach { detail ->
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(detail, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            val normalizedGroup = group.lowercase()
            if (section == EconomySection.WALLET && normalizedGroup.contains("package")) {
                val packageId = item.text("packageId", "id")
                val productId = item.text("googlePlayProductId", "googleProductId", "productId")
                if (packageId.isNotBlank()) VoiceCloudApprovedPrimaryButton("Add Credits", enabled = !busy) { onBuyWalletCredits(packageId, productId) }
            }
            if (section == EconomySection.VIP && (normalizedGroup.contains("plan") || normalizedGroup.contains("catalog") || normalizedGroup.contains("tier"))) {
                val tierId = item.text("tierId", "planId", "id")
                val productId = item.text("googlePlayProductId", "googlePlaySubscriptionProductId", "androidProductId", "productId")
                val cycle = item.text("cycle", "billingCycle", "period").takeIf { it.isNotBlank() }
                if (tierId.isNotBlank()) VoiceCloudApprovedPrimaryButton("Choose VIP", enabled = !busy) { onBuyVip(tierId, productId, cycle) }
            }
            if (id.isNotBlank()) {
                when (section) {
                    EconomySection.TASKS -> if (item.actionable("claimable", "canClaim") || item.text("status").uppercase() in setOf("COMPLETED", "CLAIMABLE")) {
                        VoiceCloudApprovedPrimaryButton("Claim Reward", enabled = !busy) { onClaimTask(id) }
                    }
                    EconomySection.STORE -> {
                        val owned = item.actionable("owned", "isOwned", "purchased")
                        val equipped = item.actionable("equipped", "isEquipped")
                        when {
                            equipped -> VoiceCloudApprovedSecondaryButton("Unequip", enabled = !busy) { onUnequip(id) }
                            owned -> VoiceCloudApprovedSecondaryButton("Equip", enabled = !busy) { onEquip(id) }
                            else -> VoiceCloudApprovedPrimaryButton("Purchase", enabled = !busy) { onBuyItem(id) }
                        }
                    }
                    EconomySection.TICKETS -> if (item.actionable("canPurchase", "availableForPurchase")) {
                        VoiceCloudApprovedPrimaryButton("Buy Ticket", enabled = !busy) { onBuyTicket(id) }
                    }
                    else -> Unit
                }
            }
    }
}

@Composable
private fun PaymentRailCard(rail: PaymentRail, section: EconomySection, busy: Boolean, onRestore: () -> Unit) {
    VoiceCloudApprovedCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 11.dp,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VoiceCloudRemoteMedia(
                url = null,
                contentDescription = if (section == EconomySection.WALLET) "Wallet payment" else "VIP membership",
                modifier = Modifier.size(width = 66.dp, height = 56.dp).clip(RoundedCornerShape(10.dp)),
                kind = if (section == EconomySection.WALLET) VoiceCloudVisualKind.WALLET else VoiceCloudVisualKind.VIP,
                dark = section == EconomySection.VIP,
                fallbackDrawable = if (section == EconomySection.WALLET) app.voicecloud.core.designsystem.R.drawable.vc_ref_wallet_spark else app.voicecloud.core.designsystem.R.drawable.vc_ref_vip_crown,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(voiceCloudTitleCase(if (rail == PaymentRail.HOSTED_GATEWAY) "Secure Payment" else "Google Play Billing"), fontWeight = FontWeight.SemiBold)
                Text(voiceCloudTitleCase(if (section == EconomySection.WALLET) "Verified Wallet Credits" else "Verified VIP Access"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (rail == PaymentRail.GOOGLE_PLAY) TextButton(enabled = !busy, onClick = onRestore) { Text(voiceCloudTitleCase("Restore")) }
        }
    }
}

private fun Map<String, Any?>.firstDisplayValue(vararg keys: String): String? = keys.firstNotNullOfOrNull { key ->
    this[key]?.takeIf { it.isDisplayScalar() }?.displayValue()?.takeIf { it.isNotBlank() && it != "—" }
}

private fun Map<String, Any?>.conciseDetails(): List<String> {
    val preferred = listOf("status", "currency", "category", "type", "cycle", "billingCycle", "period", "level", "streak", "code")
    return preferred.mapNotNull { key ->
        val value = this[key]?.takeIf { it.isDisplayScalar() }?.displayValue()?.takeIf { it.isNotBlank() && it != "—" } ?: return@mapNotNull null
        "${key.humanize()}: $value"
    }.distinct().take(2)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun payloadSections(payload: Any?): List<Pair<String, List<Map<String, Any?>>>> {
    if (payload == null) return emptyList()
    if (payload is List<*>) return listOf("Overview" to payload.mapNotNull(::asStringMap))
    val map = asStringMap(payload) ?: return emptyList()
    val result = mutableListOf<Pair<String, List<Map<String, Any?>>>>()
    map.forEach { (key, value) ->
        val rows = when (value) {
            is List<*> -> value.mapNotNull(::asStringMap)
            is Map<*, *> -> {
                val nested = asStringMap(value)
                val collection = nested?.values?.firstOrNull { it is List<*> } as? List<*>
                collection?.mapNotNull(::asStringMap) ?: nested?.let { listOf(it) }.orEmpty()
            }
            else -> if (value != null) listOf(mapOf(key to value)) else emptyList()
        }
        if (rows.isNotEmpty()) result += key.humanize() to rows
    }
    if (result.isEmpty() && map.isNotEmpty()) result += "Overview" to listOf(map)
    return result
}

private fun asStringMap(value: Any?): Map<String, Any?>? = (value as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value }
private fun Map<String, Any?>.text(vararg keys: String): String = keys.firstNotNullOfOrNull { key -> this[key]?.toString()?.takeIf(String::isNotBlank) }.orEmpty()
private fun Map<String, Any?>.actionable(vararg keys: String): Boolean = keys.any { key -> when (val value = this[key]) { is Boolean -> value; is Number -> value.toInt() != 0; is String -> value.equals("true", true) || value.equals("yes", true); else -> false } }
private fun Map<String, Any?>.stableId(): String = text("id", "itemId", "taskId", "roomId", "code", "name").take(60)
private fun Any?.isDisplayScalar(): Boolean = this == null || this is String || this is Number || this is Boolean
private fun Any?.displayValue(): String = when (this) { null -> "—"; is Boolean -> if (this) "Yes" else "No"; else -> toString().take(180) }
private fun String.humanize(): String = replace(Regex("([a-z])([A-Z])"), "$1 $2").replace('_', ' ').trim().replaceFirstChar { it.uppercase() }
