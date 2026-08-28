package app.voicecloud.feature.economy.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
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
import app.voicecloud.feature.economy.billing.PaymentRail
import app.voicecloud.feature.economy.model.*

/** Legacy/deep-link hub. My Profile now exposes these modules directly. */
@Composable
fun EconomyHubScreen(onOpen: (EconomySection) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { VoiceCloudPageTopBar("Economy & progression", onBack = onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Wallet, rewards and progression", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            itemsIndexed(economyProfileOrder) { _, section -> EconomyNavigationCard(section, onOpen) }
        }
    }
}

@Composable
fun EconomyNavigationCard(section: EconomySection, onOpen: (EconomySection) -> Unit) {
    ElevatedCard(onClick = { onOpen(section) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(section.label, fontWeight = FontWeight.SemiBold)
            Text(section.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
        { url ->
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, section) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onPaymentResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(section) { onLoad() }
    Scaffold(topBar = { VoiceCloudPageTopBar(section.label, section.subtitle, onBack) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.error?.let { message -> item { EconomyMessage(message, true, onLoad) } }
            state.notice?.let { message -> item { EconomyMessage(message, false, null) } }
            if (section == EconomySection.WALLET || section == EconomySection.VIP) item {
                PaymentRailCard(
                    rail = paymentRail,
                    section = section,
                    busy = state.busy,
                    onRestore = if (section == EconomySection.WALLET) onRestoreWallet else onRestoreVip,
                )
            }
            if (section == EconomySection.RANKINGS) item {
                Text("Agency ranking is unavailable until the backend Agency subsystem exists.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (section == EconomySection.PROGRESSION) item {
                Button(enabled = !state.busy, onClick = onClaimCheckIn, modifier = Modifier.fillMaxWidth()) { Text("Claim daily check-in") }
            }
            if (!state.loading && state.error == null) {
                val sections = payloadSections(state.payload)
                if (sections.isEmpty()) item { EconomyEmpty(section) }
                sections.forEach { data ->
                    item { Text(data.first, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                    if (data.second.isEmpty()) item { Text("No information available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    itemsIndexed(data.second, key = { index, item -> "${section.name}:${data.first}:$index:${item.stableId()}" }) { _, item ->
                        EconomyDataCard(
                            section = section,
                            group = data.first,
                            item = item,
                            busy = state.busy,
                            onClaimTask = onClaimTask,
                            onBuyItem = onBuyItem,
                            onEquip = onEquip,
                            onUnequip = onUnequip,
                            onBuyTicket = onBuyTicket,
                            onBuyWalletCredits = { packageId, productId ->
                                if (activity != null) onBuyWalletCredits(activity, packageId, productId, openHostedCheckout)
                            },
                            onBuyVip = { tierId, productId, cycle ->
                                if (activity != null) onBuyVip(activity, tierId, productId, cycle, openHostedCheckout)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EconomyMessage(message: String, error: Boolean, retry: (() -> Unit)?) {
    Card(colors = CardDefaults.cardColors(containerColor = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = if (error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
            if (retry != null) TextButton(onClick = retry) { Text("Retry") }
        }
    }
}

@Composable
private fun EconomyEmpty(section: EconomySection) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No ${section.label.lowercase()} information yet", fontWeight = FontWeight.SemiBold)
            Text("VoiceCloud will show server-authorized information here when it is available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EconomyDataCard(
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
    val title = item.text("title", "name", "label", "username", "roomTitle", "code").ifBlank { "Details" }
    val id = item.text("id", "itemId", "taskId", "roomId", "scheduledRoomId")
    ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            item.entries
                .filter { (key, value) -> key !in setOf("id", "itemId", "taskId", "roomId", "scheduledRoomId", "title", "name", "label") && value.isDisplayScalar() }
                .take(8)
                .forEach { (key, value) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(key.toString().humanize(), modifier = Modifier.weight(.42f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(value.displayValue(), modifier = Modifier.weight(.58f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            val normalizedGroup = group.lowercase()
            if (section == EconomySection.WALLET && normalizedGroup.contains("package")) {
                val packageId = item.text("packageId", "id")
                val productId = item.text("googlePlayProductId", "googleProductId", "productId")
                if (packageId.isNotBlank()) {
                    Button(
                        enabled = !busy,
                        onClick = { onBuyWalletCredits(packageId, productId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Add credits") }
                }
            }
            if (section == EconomySection.VIP && (normalizedGroup.contains("plan") || normalizedGroup.contains("catalog") || normalizedGroup.contains("tier"))) {
                val tierId = item.text("tierId", "planId", "id")
                val productId = item.text("googlePlayProductId", "googlePlaySubscriptionProductId", "androidProductId", "productId")
                val cycle = item.text("cycle", "billingCycle", "period").takeIf { it.isNotBlank() }
                if (tierId.isNotBlank()) {
                    Button(
                        enabled = !busy,
                        onClick = { onBuyVip(tierId, productId, cycle) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Choose VIP") }
                }
            }
            if (id.isNotBlank()) {
                when (section) {
                    EconomySection.TASKS -> if (item.actionable("claimable", "canClaim") || item.text("status").uppercase() in setOf("COMPLETED", "CLAIMABLE")) {
                        Button(enabled = !busy, onClick = { onClaimTask(id) }) { Text("Claim reward") }
                    }
                    EconomySection.STORE -> {
                        val owned = item.actionable("owned", "isOwned", "purchased")
                        val equipped = item.actionable("equipped", "isEquipped")
                        when {
                            equipped -> OutlinedButton(enabled = !busy, onClick = { onUnequip(id) }) { Text("Unequip") }
                            owned -> OutlinedButton(enabled = !busy, onClick = { onEquip(id) }) { Text("Equip") }
                            else -> Button(enabled = !busy, onClick = { onBuyItem(id) }) { Text("Purchase") }
                        }
                    }
                    EconomySection.TICKETS -> if (item.actionable("canPurchase", "availableForPurchase")) {
                        Button(enabled = !busy, onClick = { onBuyTicket(id) }) { Text("Buy ticket") }
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun PaymentRailCard(
    rail: PaymentRail,
    section: EconomySection,
    busy: Boolean,
    onRestore: () -> Unit,
) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (rail == PaymentRail.HOSTED_GATEWAY) "Secure payment gateway" else "Google Play billing",
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (rail == PaymentRail.HOSTED_GATEWAY)
                    "VoiceCloud uses the active payment gateway configured by Admin. Payment details stay with the provider and credits/membership update only after server verification."
                else
                    "Google Play purchase authority is verified by VoiceCloud before ${if (section == EconomySection.WALLET) "wallet credits" else "VIP access"} are applied.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            if (rail == PaymentRail.GOOGLE_PLAY) {
                TextButton(enabled = !busy, onClick = onRestore) { Text("Restore purchases") }
            }
        }
    }
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
