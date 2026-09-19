package app.voicecloud.feature.economy.ui

import app.voicecloud.core.network.toVoiceCloudPaymentMessage
import app.voicecloud.core.network.toVoiceCloudUserMessage
import android.app.Activity
import app.voicecloud.feature.economy.billing.PaymentRail
import app.voicecloud.feature.economy.billing.PlayBillingCoordinator
import app.voicecloud.feature.economy.data.EconomyRepository
import app.voicecloud.feature.economy.model.EconomySection
import app.voicecloud.feature.economy.model.EconomyUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EconomyViewModel @Inject constructor(
    private val repo: EconomyRepository,
    private val playBilling: PlayBillingCoordinator,
) : ViewModel() {
    private enum class GooglePurchaseKind { WALLET, VIP }

    private val _state = MutableStateFlow(EconomyUiState())
    val state = _state.asStateFlow()
    val paymentRail: PaymentRail = PaymentRail.configured()

    private var currentSection: EconomySection? = null
    private val pendingGoogleProducts = mutableMapOf<String, GooglePurchaseKind>()
    private var externalCheckoutPending = false

    init {
        playBilling.setPurchaseListener { candidates ->
            candidates.forEach(::verifyGoogleCandidate)
        }
        playBilling.setErrorListener { message ->
            _state.update { current -> current.copy(busy = false, error = message) }
        }
    }

    fun load(section: EconomySection) {
        currentSection = section
        viewModelScope.launch {
            _state.value = EconomyUiState(loading = true, title = section.label)
            runCatching { repo.load(section.name) }
                .onSuccess { _state.value = EconomyUiState(title = section.label, payload = it) }
                .onFailure { _state.value = EconomyUiState(title = section.label, error = readable(it)) }
        }
    }

    fun claimCheckIn() = mutate("Daily check-in claimed") { repo.claimCheckIn() }
    fun claimTask(id: String) = mutate("Task reward claimed") { repo.claimTask(id) }
    fun buyTicket(id: String) = mutate("Ticket purchase verified") { repo.buyTicket(id) }
    fun buyItem(id: String) = mutate("Store purchase completed") { repo.buyItem(id) }
    fun equip(id: String) = mutate("Item equipped") { repo.equip(id) }
    fun unequip(id: String) = mutate("Item unequipped") { repo.unequip(id) }

    fun buyWalletCredits(
        activity: Activity,
        packageId: String,
        googlePlayProductId: String,
        openHostedCheckout: (String) -> Unit,
    ) {
        when (paymentRail) {
            PaymentRail.HOSTED_GATEWAY -> beginHostedWallet(packageId, openHostedCheckout)
            PaymentRail.GOOGLE_PLAY -> beginGoogleWallet(activity, packageId, googlePlayProductId)
        }
    }

    fun buyVip(
        activity: Activity,
        tierId: String,
        googlePlayProductId: String,
        cycle: String?,
        openHostedCheckout: (String) -> Unit,
    ) {
        when (paymentRail) {
            PaymentRail.HOSTED_GATEWAY -> beginHostedVip(tierId, cycle, openHostedCheckout)
            PaymentRail.GOOGLE_PLAY -> beginGoogleVip(activity, googlePlayProductId)
        }
    }

    fun restoreGooglePlayWallet() {
        if (paymentRail != PaymentRail.GOOGLE_PLAY) return
        _state.update { it.copy(busy = true, error = null, notice = "Checking Google Play purchases…") }
        playBilling.restore(BillingClient.ProductType.INAPP) { candidates ->
            candidates.forEach { pendingGoogleProducts[it.productId] = GooglePurchaseKind.WALLET }
            if (candidates.isEmpty()) {
                _state.update { it.copy(busy = false, notice = "No pending Google Play wallet purchases were found.") }
            } else {
                candidates.forEach(::verifyGoogleCandidate)
            }
        }
    }

    fun restoreGooglePlayVip() {
        if (paymentRail != PaymentRail.GOOGLE_PLAY) return
        _state.update { it.copy(busy = true, error = null, notice = "Checking Google Play subscriptions…") }
        playBilling.restore(BillingClient.ProductType.SUBS) { candidates ->
            candidates.forEach { pendingGoogleProducts[it.productId] = GooglePurchaseKind.VIP }
            if (candidates.isEmpty()) {
                _state.update { it.copy(busy = false, notice = "No pending Google Play VIP purchase was found.") }
            } else {
                candidates.forEach(::verifyGoogleCandidate)
            }
        }
    }

    /**
     * Called when the Economy screen returns to foreground. Hosted gateway settlement remains
     * backend/webhook authoritative; Android only refreshes server state and never fabricates success.
     */
    fun refreshAfterExternalCheckout() {
        if (!externalCheckoutPending) return
        externalCheckoutPending = false
        val section = currentSection ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, notice = "Refreshing verified payment status…") }
            runCatching { repo.load(section.name) }
                .onSuccess { payload ->
                    _state.update {
                        it.copy(
                            busy = false,
                            payload = payload,
                            notice = "Payment status refreshed from VoiceCloud. Credits or membership appear only after provider verification.",
                        )
                    }
                }
                .onFailure { error -> _state.update { it.copy(busy = false, error = readable(error)) } }
        }
    }

    private fun beginHostedWallet(packageId: String, openHostedCheckout: (String) -> Unit) {
        if (packageId.isBlank()) return paymentError("This coin package is not available for checkout.")
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, notice = null) }
            runCatching { repo.initiateHostedWallet(packageId) }
                .onSuccess { checkout ->
                    externalCheckoutPending = true
                    _state.update {
                        it.copy(
                            busy = false,
                            notice = "Secure checkout opened. VoiceCloud will apply credits only after the payment provider verifies the transaction.",
                        )
                    }
                    openHostedCheckout(checkout.checkoutUrl)
                }
                .onFailure { error -> _state.update { it.copy(busy = false, error = readablePayment(error)) } }
        }
    }

    private fun beginHostedVip(tierId: String, cycle: String?, openHostedCheckout: (String) -> Unit) {
        if (tierId.isBlank()) return paymentError("This VIP plan is not available for checkout.")
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, notice = null) }
            runCatching { repo.initiateHostedVip(tierId, cycle) }
                .onSuccess { checkout ->
                    externalCheckoutPending = true
                    _state.update {
                        it.copy(
                            busy = false,
                            notice = "Secure checkout opened. VIP activates only after VoiceCloud verifies the provider payment.",
                        )
                    }
                    openHostedCheckout(checkout.checkoutUrl)
                }
                .onFailure { error -> _state.update { it.copy(busy = false, error = readablePayment(error)) } }
        }
    }

    private fun beginGoogleWallet(activity: Activity, packageId: String, productId: String) {
        if (packageId.isBlank()) return paymentError("This coin package is unavailable.")
        if (productId.isBlank()) return paymentError("This package is not mapped to Google Play yet.")
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, notice = "Preparing Google Play checkout…") }
            runCatching { repo.initiateGooglePlayWallet(packageId) }
                .onFailure { error ->
                    _state.update { it.copy(busy = false, error = readablePayment(error), notice = null) }
                }
                .onSuccess {
                    pendingGoogleProducts[productId] = GooglePurchaseKind.WALLET
                    playBilling.queryProduct(productId, BillingClient.ProductType.INAPP) { lookup ->
                        when (lookup) {
                            is PlayBillingCoordinator.ProductLookup.Error -> paymentError(lookup.message)
                            is PlayBillingCoordinator.ProductLookup.Ready -> {
                                val result = playBilling.launch(activity, lookup)
                                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                                    paymentError("Google Play checkout could not be opened.")
                                } else {
                                    _state.update { state -> state.copy(busy = false, notice = "Complete the purchase in Google Play. VoiceCloud verifies it before crediting your wallet.") }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun beginGoogleVip(activity: Activity, productId: String) {
        if (productId.isBlank()) return paymentError("This VIP plan is not mapped to Google Play yet.")
        _state.update { it.copy(busy = true, error = null, notice = "Preparing Google Play subscription…") }
        pendingGoogleProducts[productId] = GooglePurchaseKind.VIP
        playBilling.queryProduct(productId, BillingClient.ProductType.SUBS) { lookup ->
            when (lookup) {
                is PlayBillingCoordinator.ProductLookup.Error -> paymentError(lookup.message)
                is PlayBillingCoordinator.ProductLookup.Ready -> {
                    val result = playBilling.launch(activity, lookup)
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        paymentError("Google Play subscription checkout could not be opened.")
                    } else {
                        _state.update { it.copy(busy = false, notice = "Complete the subscription in Google Play. VIP activates only after VoiceCloud server verification.") }
                    }
                }
            }
        }
    }

    private fun verifyGoogleCandidate(candidate: PlayBillingCoordinator.VerifiedCandidate) {
        val kind = pendingGoogleProducts[candidate.productId]
            ?: if (currentSection == EconomySection.VIP) GooglePurchaseKind.VIP else GooglePurchaseKind.WALLET
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, notice = "VoiceCloud is verifying your Google Play purchase…") }
            val result = runCatching {
                when (kind) {
                    GooglePurchaseKind.WALLET -> repo.validateCoinPurchase(candidate.productId, candidate.purchaseToken, candidate.orderId)
                    GooglePurchaseKind.VIP -> repo.verifyVip(candidate.productId, candidate.purchaseToken)
                }
            }
            result.onFailure { error ->
                _state.update { it.copy(busy = false, error = readablePayment(error), notice = null) }
            }.onSuccess {
                pendingGoogleProducts.remove(candidate.productId)
                when (kind) {
                    GooglePurchaseKind.WALLET -> playBilling.consumeAfterServerVerification(candidate.purchaseToken)
                    GooglePurchaseKind.VIP -> playBilling.acknowledgeAfterServerVerification(candidate.purchaseToken, candidate.acknowledged)
                }
                val section = currentSection
                val refreshed = if (section != null) runCatching { repo.load(section.name) }.getOrNull() else null
                _state.update { current ->
                    current.copy(
                        busy = false,
                        payload = refreshed ?: current.payload,
                        notice = when (kind) {
                            GooglePurchaseKind.WALLET -> "Google Play purchase verified by VoiceCloud."
                            GooglePurchaseKind.VIP -> "Google Play VIP purchase verified by VoiceCloud."
                        },
                    )
                }
            }
        }
    }

    private fun paymentError(message: String) {
        _state.update { it.copy(busy = false, error = message, notice = null) }
    }

    private fun mutate(notice: String, block: suspend () -> Any) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, notice = null) }
            runCatching { block() }
                .onSuccess {
                    val section = currentSection
                    val refreshed = if (section != null) runCatching { repo.load(section.name) }.getOrNull() else null
                    _state.update { current -> current.copy(busy = false, notice = notice, payload = refreshed ?: current.payload) }
                }
                .onFailure { error -> _state.update { it.copy(busy = false, error = readable(error)) } }
        }
    }

    private fun readablePayment(error: Throwable): String = error.toVoiceCloudPaymentMessage()

    private fun readable(error: Throwable): String = error.toVoiceCloudUserMessage("VoiceCloud Couldn’t Load This Section. Try Again.")
}
