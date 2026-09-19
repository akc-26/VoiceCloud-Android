from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; checks=[]
def r(p): return (ROOT/p).read_text(encoding='utf-8')
def ck(n,o): checks.append(bool(o)); print(('[PASS] ' if o else '[FAIL] ')+n)
nav=r('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
api=r('feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt')
repo=r('feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt')
screens=r('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt')
hapi=r('feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingApi.kt')
hrepo=r('feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingRepository.kt')
hscreens=r('feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt')
ck('PH13 app version declared','versionName = "1.0.0-ph13"' in r('app/build.gradle.kts'))
for route in ['CreatorAnalytics','CreatorWallet','CreatorEarnings','CreatorGifts','CreatorPayouts','CreatorPayoutDetail','CreatorNotifications','CreatorVerification','HostVerification']:
    ck('PH13 route '+route,f'const val {route}' in nav)
for fn in ['CreatorAnalyticsScreen','CreatorWalletScreen','CreatorEarningsScreen','CreatorGiftsScreen','CreatorPayoutsScreen','CreatorPayoutDetailScreen']:
    ck('PH13 screen '+fn,f'fun {fn}(' in screens)
ck('Creator notifications reuse Engagement authority','NotificationsScreen(' in nav and 'CreatorNotifications' in nav)
ck('Host verification screen implemented','fun HostVerificationScreen(' in hscreens)
for endpoint in ['@GET("analytics")','@GET("wallet/balance")','@GET("wallet/summary")','@GET("wallet/transactions")','@GET("gifts/history")','@GET("creator/earnings")','@POST("creator/payout-request")','@GET("creator/payout-requests")','@GET("creator/payout-requests/{id}")']:
    ck('Creator PH13 endpoint '+endpoint,endpoint in api)
for endpoint in ['@POST("hosts/apply")','@GET("hosts/progression")','@GET("hosts/verification/assets")','@POST("hosts/verification/government-id")','@POST("hosts/verification/profile-photo")','@POST("hosts/verification/documents")','@PUT("hosts/verification/assets/{assetId}/replacement")']:
    ck('Host PH13 endpoint '+endpoint,endpoint in hapi)
ck('Payout minimum enforced','diamondAmount >= 100' in repo)
ck('Payout idempotency wired','operationKey' in repo and 'UUID.randomUUID()' in repo)
ck('Verification replacement uses backend asset id','"replacementAssetId" to replacementId' in hrepo)
ck('Verification is transient in-memory selection','openInputStream(uri)?.use { it.readBytes() }' in hscreens)
combined='\n'.join([api,repo,screens,hapi,hrepo,hscreens,nav]).lower()
ck('Android stream credentials are prohibited','stream-credentials' not in combined and 'streamcredentials' not in combined and 'rtmpurl' not in combined and 'streamkey' not in combined)
ck('No public verification upload path introduced','/uploads' not in hapi and '/uploads' not in hrepo)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R01 source authority: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R01 source authority: {passed}/{len(checks)} PASS')
