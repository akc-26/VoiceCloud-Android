package app.voicecloud.feature.economy.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.voicecloud.core.designsystem.component.VoiceCloudPageTopBar
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.component.VoiceCloudHeroCard
import app.voicecloud.core.designsystem.component.VoiceCloudPageHero
import app.voicecloud.core.designsystem.component.VoiceCloudEmptyVisual
import app.voicecloud.core.designsystem.component.VoiceCloudGlossCard
import app.voicecloud.core.designsystem.component.VoiceCloudPictogram
import app.voicecloud.core.designsystem.component.VoiceCloudVisualKind
import app.voicecloud.core.designsystem.component.VoiceCloudPremiumCard
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
    Scaffold(topBar = { VoiceCloudPageTopBar("Wallet & Rewards", onBack = onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, ConsumerColors.VipGold.copy(alpha = .52f), RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = ConsumerColors.LiveSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                ) {
                    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(voiceCloudTitleCase("VoiceCloud wallet"), color = ConsumerColors.TextOnDarkSecondary, style = MaterialTheme.typography.labelLarge)
                            Text(voiceCloudTitleCase("Rewards that grow with your voice"), color = ConsumerColors.TextOnDark, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Text(voiceCloudTitleCase("Wallet · Gifts · VIP · Achievements"), color = ConsumerColors.VipGold, style = MaterialTheme.typography.bodySmall)
                        }
                        VoiceCloudPictogram(VoiceCloudVisualKind.WALLET, size = 76.dp, dark = true)
                    }
                }
            }
            itemsIndexed(economyProfileOrder.chunked(2)) { _, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { section ->
                        Box(Modifier.weight(1f)) { EconomyNavigationCard(section, onOpen) }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun EconomyNavigationCard(section: EconomySection, onOpen: (EconomySection) -> Unit) {
    VoiceCloudGlossCard(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(section) },
        contentPadding = 15.dp,
    ) {
        VoiceCloudPictogram(section.visualKind(), size = 54.dp)
        Text(voiceCloudTitleCase(section.label), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        Text(voiceCloudTitleCase(section.subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
    Scaffold(topBar = { VoiceCloudPageTopBar(section.label, section.subtitle, onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                VoiceCloudPageHero(
                    title = section.label,
                    subtitle = section.subtitle,
                    kind = section.visualKind(),
                    badge = when (section) {
                        EconomySection.WALLET -> "Secure & transparent"
                        EconomySection.VIP -> "Premium experience"
                        EconomySection.GIFTS -> "Send appreciation"
                        EconomySection.TASKS -> "Earn rewards"
                        else -> "VoiceCloud rewards"
                    },
                )
            }
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
                Text(voiceCloudTitleCase("Live Rankings Appear When Ranking Data Is Available"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (section == EconomySection.PROGRESSION) item {
                Button(enabled = !state.busy, onClick = onClaimCheckIn, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Claim Daily Check-In")) }
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
    ElevatedCard(
        Modifier.fillMaxWidth().border(1.dp, ConsumerColors.Border.copy(alpha = .62f), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = ConsumerColors.Surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VoiceCloudPictogram(section.visualKind(), size = 48.dp)
                Spacer(Modifier.width(12.dp))
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                if (packageId.isNotBlank()) Button(enabled = !busy, onClick = { onBuyWalletCredits(packageId, productId) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Add Credits")) }
            }
            if (section == EconomySection.VIP && (normalizedGroup.contains("plan") || normalizedGroup.contains("catalog") || normalizedGroup.contains("tier"))) {
                val tierId = item.text("tierId", "planId", "id")
                val productId = item.text("googlePlayProductId", "googlePlaySubscriptionProductId", "androidProductId", "productId")
                val cycle = item.text("cycle", "billingCycle", "period").takeIf { it.isNotBlank() }
                if (tierId.isNotBlank()) Button(enabled = !busy, onClick = { onBuyVip(tierId, productId, cycle) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Choose VIP")) }
            }
            if (id.isNotBlank()) {
                when (section) {
                    EconomySection.TASKS -> if (item.actionable("claimable", "canClaim") || item.text("status").uppercase() in setOf("COMPLETED", "CLAIMABLE")) {
                        Button(enabled = !busy, onClick = { onClaimTask(id) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Claim Reward")) }
                    }
                    EconomySection.STORE -> {
                        val owned = item.actionable("owned", "isOwned", "purchased")
                        val equipped = item.actionable("equipped", "isEquipped")
                        when {
                            equipped -> OutlinedButton(enabled = !busy, onClick = { onUnequip(id) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Unequip")) }
                            owned -> OutlinedButton(enabled = !busy, onClick = { onEquip(id) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Equip")) }
                            else -> Button(enabled = !busy, onClick = { onBuyItem(id) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Purchase")) }
                        }
                    }
                    EconomySection.TICKETS -> if (item.actionable("canPurchase", "availableForPurchase")) {
                        Button(enabled = !busy, onClick = { onBuyTicket(id) }, modifier = Modifier.fillMaxWidth()) { Text(voiceCloudTitleCase("Buy Ticket")) }
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun PaymentRailCard(rail: PaymentRail, section: EconomySection, busy: Boolean, onRestore: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().border(1.dp, ConsumerColors.VipGold.copy(alpha = .32f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ConsumerColors.SurfaceSoft),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VoiceCloudPictogram(if (section == EconomySection.WALLET) VoiceCloudVisualKind.WALLET else VoiceCloudVisualKind.VIP, size = 44.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(voiceCloudTitleCase(if (rail == PaymentRail.HOSTED_GATEWAY) "Secure Payment" else "Google Play Billing"), fontWeight = FontWeight.SemiBold)
                Text(voiceCloudTitleCase(if (section == EconomySection.WALLET) "Verified Wallet Credits" else "Verified VIP Access"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (rail == PaymentRail.GOOGLE_PLAY) TextButton(enabled = !busy, onClick = onRestore) { Text(voiceCloudTitleCase("Restore")) }
        }
    }
}

private fun EconomySection.glyph(): String = when (this) {
    EconomySection.WALLET -> "◈"
    EconomySection.VIP -> "VIP"
    EconomySection.STORE -> "✦"
    EconomySection.GIFTS -> "♥"
    EconomySection.TASKS -> "✓"
    EconomySection.ACHIEVEMENTS -> "★"
    EconomySection.PROGRESSION -> "XP"
    EconomySection.RANKINGS -> "#"
    EconomySection.TICKETS -> "◇"
    EconomySection.REFERRALS -> "+1"
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
