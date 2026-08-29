from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
BASELINE=ROOT/'contracts/VC-ANDROID-PH13-R01-PH12-R01-BASELINE-CANONICAL.sha256'
ALLOWED={
 'README.md','CHANGELOG.md','app/build.gradle.kts','app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
 'feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/model/CreatorModels.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorViewModel.kt','feature/creator/src/test/java/app/voicecloud/feature/creator/model/CreatorModelsTest.kt',
 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingApi.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingRepository.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/model/HostingModels.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingViewModel.kt',
}
REQUIRED_NEW=['VC-ANDROID-PH13-R01-REVISION.txt','docs/VC-ANDROID-PH13-R01-IMPLEMENTATION-REPORT.md','docs/VC-ANDROID-PH13-R01-AUTOMATED-EVIDENCE.md','docs/VC-ANDROID-PH13-R01-MANUAL-QA.md','scripts/VC-ANDROID-PH13-R01-ACCEPTANCE.cmd','scripts/ph13_r01_source_check.py','scripts/ph13_r01_backend_contract_regression.py','scripts/ph13_r01_compile_surface_regression.py','scripts/ph13_r01_unit_contract_regression.py','scripts/ph13_r01_preservation_regression.py','scripts/ph13_r01_package_hygiene.py','scripts/ph13_r01_acceptance_wiring_regression.py','scripts/ph13_r01_whitespace_regression.py']
def main():
 if not BASELINE.exists(): raise SystemExit('[FAIL] PH13 canonical PH12-R01 baseline manifest missing')
 preserved=0; controlled=[]
 for line in BASELINE.read_text().splitlines():
  if not line.strip(): continue
  expected,rel=line.split('  ',1); p=ROOT/rel
  if not p.exists(): raise SystemExit(f'[FAIL] PH12-R01 parent file deleted: {rel}')
  actual=hashlib.sha256(p.read_bytes()).hexdigest()
  if actual!=expected:
   if rel not in ALLOWED: raise SystemExit(f'[FAIL] unexpected PH12-R01 parent drift outside PH13 scope: {rel}')
   controlled.append(rel)
  else: preserved+=1
 for rel in REQUIRED_NEW:
  if not (ROOT/rel).exists(): raise SystemExit(f'[FAIL] PH13 required delivery file missing: {rel}')
 print(f'[PASS] {preserved} PH12-R01 parent files remain byte-identical outside PH13 allowlist')
 print(f'[PASS] PH13 controlled parent changes: {len(set(controlled))}')
 print('[PASS] PH13 introduces no deletion of a frozen PH12-R01 parent file')
if __name__=='__main__': main()
