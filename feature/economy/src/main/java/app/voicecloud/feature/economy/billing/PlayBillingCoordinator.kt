package app.voicecloud.feature.economy.billing
import android.app.Activity; import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext; import javax.inject.Inject; import javax.inject.Singleton
@Singleton class PlayBillingCoordinator @Inject constructor(@ApplicationContext context:Context):PurchasesUpdatedListener{
 data class VerifiedCandidate(val productId:String,val purchaseToken:String,val orderId:String?)
 private var callback:((List<VerifiedCandidate>)->Unit)?=null
 private val client=BillingClient.newBuilder(context).setListener(this).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).enableAutoServiceReconnection().build()
 fun setPurchaseListener(listener:(List<VerifiedCandidate>)->Unit){callback=listener}
 override fun onPurchasesUpdated(result:BillingResult,purchases:List<Purchase>?){ if(result.responseCode==BillingClient.BillingResponseCode.OK) callback?.invoke(purchases.orEmpty().filter{it.purchaseState==Purchase.PurchaseState.PURCHASED}.flatMap{p->p.products.map{VerifiedCandidate(it,p.purchaseToken,p.orderId)}}) }
 fun restore(productType:String,onResult:(List<VerifiedCandidate>)->Unit){ client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(productType).build()){r,purchases-> if(r.responseCode==BillingClient.BillingResponseCode.OK) onResult(purchases.flatMap{p->p.products.map{VerifiedCandidate(it,p.purchaseToken,p.orderId)}}) else onResult(emptyList()) } }
 fun launch(activity:Activity,details:ProductDetails,offerToken:String?=null):BillingResult{ val pd=BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).apply{if(!offerToken.isNullOrBlank())setOfferToken(offerToken)}.build(); return client.launchBillingFlow(activity,BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(pd)).build()) }
}
