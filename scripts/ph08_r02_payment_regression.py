from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
def t(rel): return (ROOT / rel).read_text(encoding='utf-8')
def check(name, cond):
    if not cond: raise SystemExit(f'[FAIL] {name}')
    print(f'[PASS] {name}')

api=t('feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyApi.kt')
repo=t('feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyRepository.kt')
vm=t('feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyViewModel.kt')
ui=t('feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt')
billing=t('feature/economy/src/main/java/app/voicecloud/feature/economy/billing/PlayBillingCoordinator.kt')
rail=t('feature/economy/src/main/java/app/voicecloud/feature/economy/billing/PaymentRail.kt')
build=t('feature/economy/build.gradle.kts')
props=t('gradle.properties')
contract=json.loads(t('contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json'))
ops={(o.get('method'),o.get('path')) for o in contract.get('operations',[])}

for method,path in [
 ('POST','/wallet/purchase/web/initiate'),('POST','/wallet/purchase/web/complete'),('POST','/wallet/purchase/web/cancel'),
 ('POST','/vip/checkout/web/initiate'),('POST','/vip/checkout/web/complete'),('POST','/vip/checkout/web/cancel'),
 ('POST','/wallet/purchase/initiate'),('POST','/wallet/purchase/validate'),
 ('GET','/android/billing/vip/catalog'),('POST','/android/billing/vip/verify')]:
    check(f'contract contains payment authority {method} {path}', (method,path) in ops)

check('hosted gateway is current test default', 'VOICECLOUD_ANDROID_PAYMENT_MODE=HOSTED_GATEWAY' in props)
check('launch payment rail is generated as non-secret BuildConfig', 'VOICECLOUD_PAYMENT_MODE' in build and 'HOSTED_GATEWAY' in build and 'GOOGLE_PLAY' in build)
check('payment rail has only hosted gateway and Google Play choices', 'HOSTED_GATEWAY' in rail and 'GOOGLE_PLAY' in rail and 'STRIPE' not in rail and 'RAZORPAY' not in rail and 'PAYPAL' not in rail)
check('Android exposes canonical wallet hosted checkout API', all(x in api for x in ['wallet/purchase/web/initiate','wallet/purchase/web/complete','wallet/purchase/web/cancel']))
check('Android exposes canonical VIP hosted checkout API', all(x in api for x in ['vip/checkout/web/initiate','vip/checkout/web/complete','vip/checkout/web/cancel']))
check('hosted checkout consumes only opaque checkout session and HTTPS URL', 'checkoutSessionId' in repo and 'checkoutUrl' in repo and 'startsWith("https://")' in repo)
check('hosted checkout does not send consumer-selected Stripe Razorpay or PayPal provider', not re.search(r'mapOf\([^\n]*(STRIPE|RAZORPAY|PAYPAL)', repo, re.I))
check('Wallet UI launches configured payment rail', 'buyWalletCredits' in vm and 'beginHostedWallet' in vm and 'beginGoogleWallet' in vm)
check('VIP UI launches configured payment rail', 'buyVip' in vm and 'beginHostedVip' in vm and 'beginGoogleVip' in vm)
check('hosted checkout uses external HTTPS provider URL', 'Intent.ACTION_VIEW' in ui and 'Uri.parse(url)' in ui)
check('hosted checkout never fabricates financial success', 'Credits or membership appear only after provider verification' in vm and 'Payment status refreshed from VoiceCloud' in vm)

check('Google Play 9.1 pending purchase setup retained', 'enablePendingPurchases' in billing and 'enableOneTimeProducts()' in billing)
check('Google Play starts BillingClient connection', 'startConnection' in billing and 'BillingClientStateListener' in billing)
check('Google Play queries ProductDetails before launch', 'queryProductDetailsAsync' in billing and 'QueryProductDetailsParams.Product.newBuilder()' in billing)
check('Google Play launch uses current ProductDetails and offer token', 'setProductDetails(lookup.details)' in billing and 'setOfferToken(lookup.offerToken)' in billing)
check('wallet Google Play flow initiates server purchase before Billing launch', vm.find('repo.initiateGooglePlayWallet(packageId)') < vm.find('playBilling.queryProduct(productId, BillingClient.ProductType.INAPP)'))
check('wallet Google Play token is server validated', 'repo.validateCoinPurchase(candidate.productId, candidate.purchaseToken, candidate.orderId)' in vm)
check('VIP Google Play token is server verified', 'repo.verifyVip(candidate.productId, candidate.purchaseToken)' in vm)
check('wallet consumable is consumed only after server success block', 'consumeAfterServerVerification(candidate.purchaseToken)' in vm and vm.find('consumeAfterServerVerification(candidate.purchaseToken)') > vm.find('result.onFailure'))
check('VIP subscription is acknowledged only after server success block', 'acknowledgeAfterServerVerification(candidate.purchaseToken, candidate.acknowledged)' in vm and vm.find('acknowledgeAfterServerVerification(candidate.purchaseToken, candidate.acknowledged)') > vm.find('result.onFailure'))
check('Google Play restore covers wallet and VIP', 'ProductType.INAPP' in vm and 'ProductType.SUBS' in vm and 'restoreGooglePlayWallet' in vm and 'restoreGooglePlayVip' in vm)
check('no local coin or VIP entitlement grant introduced', not re.search(r'addCoins|grantCoins|walletBalance\s*[+]=|vipActive\s*=\s*true|grantVip', vm+repo+billing, re.I))
check('Wallet/VIP screen exposes real payment actions', 'Text("Add credits")' in ui and 'Text("Choose VIP")' in ui and 'PaymentRailCard' in ui)

print('[PASS] VC-ANDROID-PH08-R02 payment regression: 33/33 PASS')
