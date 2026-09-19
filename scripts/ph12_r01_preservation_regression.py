from pathlib import Path
import hashlib, importlib.util
ROOT=Path(__file__).resolve().parents[1]
BASELINE=ROOT/'contracts/VC-ANDROID-PH12-R01-PH11-R02-BASELINE-CANONICAL.sha256'
ALLOWED={
 'README.md','CHANGELOG.md','app/build.gradle.kts','app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
 'feature/creator/build.gradle.kts','feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/model/CreatorModels.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorViewModel.kt','feature/creator/src/test/java/app/voicecloud/feature/creator/model/CreatorModelsTest.kt',
 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryRepository.kt',
 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/EngagementRepository.kt','feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt','feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementViewModel.kt',
}
REQUIRED_NEW=['VC-ANDROID-PH12-R01-REVISION.txt','docs/VC-ANDROID-PH12-R01-IMPLEMENTATION-REPORT.md','docs/VC-ANDROID-PH12-R01-AUTOMATED-EVIDENCE.md','docs/VC-ANDROID-PH12-R01-MANUAL-QA.md','scripts/VC-ANDROID-PH12-R01-ACCEPTANCE.cmd','scripts/ph12_r01_source_check.py','scripts/ph12_r01_backend_contract_regression.py','scripts/ph12_r01_compile_surface_regression.py','scripts/ph12_r01_unit_contract_regression.py','scripts/ph12_r01_preservation_regression.py','scripts/ph12_r01_package_hygiene.py','scripts/ph12_r01_acceptance_wiring_regression.py','contracts/VC-ANDROID-PH12-R01-SOURCE-MANIFEST.sha256']
spec=importlib.util.spec_from_file_location('canon',ROOT/'scripts/ph09_r08_preservation_regression.py'); canon=importlib.util.module_from_spec(spec); spec.loader.exec_module(canon)
def main():
 if not BASELINE.exists(): raise SystemExit('[FAIL] PH12 canonical PH11-R02 baseline manifest missing')
 preserved=0; controlled=[]
 for line in BASELINE.read_text().splitlines():
  if not line.strip(): continue
  expected,rel=line.split('  ',1); p=ROOT/rel
  if not p.exists(): raise SystemExit(f'[FAIL] PH11-R02 parent file deleted: {rel}')
  actual=hashlib.sha256(canon.canonical_bytes(rel,p.read_bytes())).hexdigest()
  if actual!=expected:
   if rel not in ALLOWED: raise SystemExit(f'[FAIL] unexpected PH11-R02 parent drift outside PH12 scope: {rel}')
   controlled.append(rel)
  else: preserved+=1
 for rel in REQUIRED_NEW:
  if not (ROOT/rel).exists(): raise SystemExit(f'[FAIL] PH12 required delivery file missing: {rel}')
 print(f'[PASS] {preserved} PH11-R02 parent files remain semantically unchanged outside PH12 allowlist')
 print(f'[PASS] PH12 controlled parent changes: {len(set(controlled))}')
 print('[PASS] PH12 introduces no deletion of a frozen PH11-R02 parent file')
if __name__=='__main__': main()
