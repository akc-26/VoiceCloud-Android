from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; t=(ROOT/'feature/creator/src/test/java/app/voicecloud/feature/creator/model/CreatorModelsTest.kt').read_text(); checks=[]
def ck(n,o): checks.append(bool(o)); print(('[PASS] ' if o else '[FAIL] ')+n)
ck('Creator unit test expects PH12 AUDIENCE section','"DASHBOARD", "LIVE", "AUDIENCE", "PROFILE", "SETTINGS", "HELP"' in t)
ck('Creator unit test still forbids PH13 finance','WALLET' in t and 'PAYOUT' in t and 'EARNINGS' in t)
ck('Plan defaults are non-paid DRAFT','creatorPlanDefaultsNeverFabricateActiveOrPaidState' in t and '"DRAFT"' in t)
ck('Subscriber total unknown state covered','subscriberTotalMayRemainUnknownWhenBackendOmitsPaginationMetadata' in t)
if sum(checks)!=len(checks): raise SystemExit('[FAIL] PH12 unit contract regression')
print(f'[PASS] VC-ANDROID-PH12-R01 unit contract regression: {sum(checks)}/{len(checks)} PASS')
