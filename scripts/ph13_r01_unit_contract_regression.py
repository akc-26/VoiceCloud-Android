from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; t=(ROOT/'feature/creator/src/test/java/app/voicecloud/feature/creator/model/CreatorModelsTest.kt').read_text(); checks=[]
def ck(n,o): checks.append(bool(o)); print(('[PASS] ' if o else '[FAIL] ')+n)
ck('PH12 Audience enum remains preserved','"DASHBOARD", "LIVE", "AUDIENCE", "PROFILE", "SETTINGS", "HELP"' in t)
ck('Financial navigation does not mutate core bottom-nav enum','assertFalse(sections.any { it.contains("WALLET") || it.contains("PAYOUT") || it.contains("EARNINGS") })' in t)
ck('PH12 plan non-paid default retained','creatorPlanDefaultsNeverFabricateActiveOrPaidState' in t)
ck('PH13 financial empty/unknown defaults tested','ph13FinancialModelsDefaultToUnknownOrEmptyBackendState' in t)
ck('PH13 payout default status unknown','CreatorPayoutRequest(id = "request-1").status' in t and 'assertEquals("",' in t)
if sum(checks)!=len(checks): raise SystemExit('[FAIL] PH13 unit contract regression')
print(f'[PASS] VC-ANDROID-PH13-R01 unit contract regression: {sum(checks)}/{len(checks)} PASS')
