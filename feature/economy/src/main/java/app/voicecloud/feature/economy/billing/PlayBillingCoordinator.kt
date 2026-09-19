package app.voicecloud.feature.economy.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingCoordinator @Inject constructor(
    @ApplicationContext context: Context,
) : PurchasesUpdatedListener {

    data class VerifiedCandidate(
        val productId: String,
        val purchaseToken: String,
        val orderId: String?,
        val acknowledged: Boolean,
    )

    sealed interface ProductLookup {
        data class Ready(val details: ProductDetails, val offerToken: String?) : ProductLookup
        data class Error(val message: String) : ProductLookup
    }

    private var purchaseListener: ((List<VerifiedCandidate>) -> Unit)? = null
    private var errorListener: ((String) -> Unit)? = null
    private var connecting = false
    private val readyCallbacks = mutableListOf<(Boolean) -> Unit>()

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build(),
        )
        .enableAutoServiceReconnection()
        .build()

    fun setPurchaseListener(listener: (List<VerifiedCandidate>) -> Unit) {
        purchaseListener = listener
    }

    fun setErrorListener(listener: (String) -> Unit) {
        errorListener = listener
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val candidates = purchases.orEmpty()
                    .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .flatMap { purchase ->
                        purchase.products.map { productId ->
                            VerifiedCandidate(
                                productId = productId,
                                purchaseToken = purchase.purchaseToken,
                                orderId = purchase.orderId,
                                acknowledged = purchase.isAcknowledged,
                            )
                        }
                    }
                if (candidates.isNotEmpty()) purchaseListener?.invoke(candidates)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> errorListener?.invoke("Purchase cancelled")
            else -> errorListener?.invoke(result.debugMessage.ifBlank { "Google Play could not complete the purchase." })
        }
    }

    fun queryProduct(
        productId: String,
        productType: String,
        onResult: (ProductLookup) -> Unit,
    ) {
        if (productId.isBlank()) {
            onResult(ProductLookup.Error("This item is not mapped to a Google Play product yet."))
            return
        }
        ensureReady { ready ->
            if (!ready) {
                onResult(ProductLookup.Error("Google Play Billing is unavailable on this device."))
                return@ensureReady
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(productType)
                            .build(),
                    ),
                )
                .build()
            client.queryProductDetailsAsync(params) { billingResult, queryResult ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    onResult(ProductLookup.Error(billingResult.debugMessage.ifBlank { "Google Play product details are unavailable." }))
                    return@queryProductDetailsAsync
                }
                val details = queryResult.productDetailsList.firstOrNull()
                if (details == null) {
                    onResult(ProductLookup.Error("This Google Play product is not available for this account/device."))
                    return@queryProductDetailsAsync
                }
                val offerToken = when (productType) {
                    BillingClient.ProductType.SUBS -> details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    else -> details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
                }
                onResult(ProductLookup.Ready(details, offerToken))
            }
        }
    }

    fun launch(activity: Activity, lookup: ProductLookup.Ready): BillingResult {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(lookup.details)
            .apply {
                if (!lookup.offerToken.isNullOrBlank()) setOfferToken(lookup.offerToken)
            }
            .build()
        return client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build(),
        )
    }

    fun restore(productType: String, onResult: (List<VerifiedCandidate>) -> Unit) {
        ensureReady { ready ->
            if (!ready) {
                onResult(emptyList())
                return@ensureReady
            }
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(productType).build(),
            ) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    onResult(
                        purchases
                            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                            .flatMap { purchase ->
                                purchase.products.map { productId ->
                                    VerifiedCandidate(productId, purchase.purchaseToken, purchase.orderId, purchase.isAcknowledged)
                                }
                            },
                    )
                } else {
                    onResult(emptyList())
                }
            }
        }
    }

    fun consumeAfterServerVerification(token: String, onResult: (Boolean) -> Unit = {}) {
        ensureReady { ready ->
            if (!ready) {
                onResult(false)
                return@ensureReady
            }
            client.consumeAsync(
                ConsumeParams.newBuilder().setPurchaseToken(token).build(),
            ) { result, _ -> onResult(result.responseCode == BillingClient.BillingResponseCode.OK) }
        }
    }

    fun acknowledgeAfterServerVerification(
        token: String,
        alreadyAcknowledged: Boolean,
        onResult: (Boolean) -> Unit = {},
    ) {
        if (alreadyAcknowledged) {
            onResult(true)
            return
        }
        ensureReady { ready ->
            if (!ready) {
                onResult(false)
                return@ensureReady
            }
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build(),
            ) { result -> onResult(result.responseCode == BillingClient.BillingResponseCode.OK) }
        }
    }

    private fun ensureReady(callback: (Boolean) -> Unit) {
        if (client.isReady) {
            callback(true)
            return
        }
        readyCallbacks += callback
        if (connecting) return
        connecting = true
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connecting = false
                val ready = result.responseCode == BillingClient.BillingResponseCode.OK
                val callbacks = readyCallbacks.toList()
                readyCallbacks.clear()
                callbacks.forEach { it(ready) }
            }

            override fun onBillingServiceDisconnected() {
                connecting = false
            }
        })
    }
}
