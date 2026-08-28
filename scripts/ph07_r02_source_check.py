from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def t(p): return (ROOT/p).read_text(encoding='utf-8')
def ck(name,ok):
    if not ok: raise SystemExit(f'[FAIL] {name}')
    print(f'[PASS] {name}')
settings=t('settings.gradle.kts'); app=t('app/build.gradle.kts'); api=t('feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyApi.kt'); repo=t('feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyRepository.kt'); billing=t('feature/economy/src/main/java/app/voicecloud/feature/economy/billing/PlayBillingCoordinator.kt'); nav=t('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'); ui=t('feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt'); gradle=t('feature/economy/build.gradle.kts')
ck('economy module registered','include(":feature:economy")' in settings)
ck('app consumes economy','implementation(project(":feature:economy"))' in app)
ck('PH07 app version','versionName = "1.0.0-ph07"' in app)
ck('Google Play Billing 9.1.0','com.android.billingclient:billing:9.1.0' in gradle)
for ep in ['wallet/summary','wallet/transactions','wallet/packages','wallet/purchases/history','wallet/purchase/validate','android/billing/vip/catalog','android/billing/vip/verify','vip/membership','vip/history','referrals/summary','referrals/history','referrals/rewards','gifts/catalog','gifts/history','store/catalog','store/inventory','store/inventory/equipped','store/purchase','store/equip','store/unequip','tasks-achievements/tasks','tasks-achievements/achievements','tasks-achievements/xp/progress','tasks-achievements/check-in','tasks-achievements/check-in/claim','tasks-achievements/streaks','rankings/leaderboard/users','rankings/leaderboard/creators','rankings/leaderboard/hosts','rankings/leaderboard/rooms','rankings/leaderboard/gift-senders','rankings/leaderboard/gift-receivers','rankings/leaderboard/vip','scheduled-rooms/my-tickets','scheduled-rooms/{roomId}/buy-ticket']:
    ck('canonical endpoint '+ep, ep in api)
ck('coin purchase token is server validated','provider" to "GOOGLE_PLAY"' in repo and 'validatePurchase' in repo)
ck('VIP token is server verified','verifyAndroidVip' in repo and 'purchaseToken' in repo)
ck('nullable Billing purchases handled safely','purchases:List<Purchase>?' in billing and 'purchases.orEmpty()' in billing)
ck('restore queries Play purchases','queryPurchasesAsync' in billing)
ck('no local wallet entitlement grant', all(x not in billing.lower()+repo.lower() for x in ['addcoins','grantcoins','localbalance +=','vipactive = true']))
ck('Agency ranking not fabricated','agency' not in api.lower() and 'Agency ranking is intentionally unavailable' in ui)
ck('VIP screen says Google Play authority','Google Play subscription authority only' in ui)
ck('economy routes installed','const val Economy = "economy"' in nav and 'composable(VoiceCloudRoutes.Economy)' in nav and 'composable(VoiceCloudRoutes.EconomySection)' in nav)
ck('profile entry installed','onEconomy' in nav)
ck('Material3 experimental API opt-in present','@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)' in ui)
print('[PASS] VC-ANDROID-PH07-R02 source authority complete')
