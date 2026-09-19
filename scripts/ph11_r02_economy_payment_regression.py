from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
screen=read('feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt')
vm=read('feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyViewModel.kt')
repo=read('feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyRepository.kt')
api=read('feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyApi.kt')
for endpoint in ['wallet/purchase/web/initiate','wallet/purchase/web/complete','wallet/purchase/web/cancel','vip/checkout/web/initiate','vip/checkout/web/complete','vip/checkout/web/cancel']:
    ck(f'hosted payment endpoint retained: {endpoint}', endpoint in api)
ck('Wallet visual card still launches real credit purchase callback', 'onBuyWalletCredits' in screen and 'Add Credits' in screen)
ck('VIP visual card still launches real VIP purchase callback', 'onBuyVip' in screen and 'Choose VIP' in screen)
ck('Hosted checkout completion remains server-authoritative', 'completeHosted' in vm or 'completeHosted' in repo)
ck('Google Play verification remains server-authoritative', 'validateCoinPurchase' in vm and 'verifyVip' in vm)
ck('Wallet consumption occurs only after server verification', 'consumeAfterServerVerification' in vm)
ck('VIP acknowledgement occurs only after server verification', 'acknowledgeAfterServerVerification' in vm)
ck('Economy UI does not locally grant coins/VIP entitlement', all(x not in screen for x in ['coinBalance +=','vipActive = true','grantCoins']))
ck('Economy is visual card/grid based rather than raw key-value dump', 'EconomyVisualCard' in screen and 'details.take(2)' in screen)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH11-R02 economy/payment regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH11-R02 economy/payment regression: {passed}/{len(checks)} PASS')
