from pathlib import Path
import hashlib, importlib.util
ROOT=Path(__file__).resolve().parents[1]
BASELINE=ROOT/'contracts/VC-ANDROID-PH11-R02-PH10-R01-BASELINE-CANONICAL.sha256'
ALLOWED={
 'README.md','CHANGELOG.md','app/build.gradle.kts','app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
 'feature/creator/build.gradle.kts','feature/creator/src/test/java/app/voicecloud/feature/creator/model/CreatorModelsTest.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/model/CreatorModels.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorViewModel.kt',
 'feature/economy/src/main/java/app/voicecloud/feature/economy/model/EconomyModels.kt','feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt','feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyViewModel.kt',
 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt','feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryViewModel.kt',
 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingRepository.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingViewModel.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
 'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt','feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileViewModel.kt',
 'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt','feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt',
 'feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsRepository.kt','feature/settings/src/main/java/app/voicecloud/feature/settings/model/SettingsModels.kt','feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt','feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsViewModel.kt','feature/settings/src/test/java/app/voicecloud/feature/settings/model/SettingsModelsTest.kt',
 'feature/live/src/main/java/app/voicecloud/feature/live/data/LiveRoomRepository.kt','feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt','feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomViewModel.kt',
 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt','feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementViewModel.kt','feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt',
}
REQUIRED_NEW=[
 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt',
 'core/network/src/main/java/app/voicecloud/core/network/UserFacingErrors.kt',
 'core/network/src/test/java/app/voicecloud/core/network/UserFacingErrorsTest.kt',
 'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileImageCropper.kt',
]
spec=importlib.util.spec_from_file_location('canon',ROOT/'scripts/ph09_r08_preservation_regression.py')
canon=importlib.util.module_from_spec(spec); spec.loader.exec_module(canon)
def verify():
    if not BASELINE.exists(): raise SystemExit('[FAIL] PH11 canonical PH10-R01 baseline manifest missing')
    preserved=0; controlled=[]
    for line in BASELINE.read_text(encoding='utf-8').splitlines():
        if not line.strip(): continue
        expected,rel=line.split('  ',1); path=ROOT/rel
        if not path.exists():
            if rel in ALLOWED: controlled.append(rel); continue
            raise SystemExit(f'[FAIL] preserved PH10-R01 file missing: {rel}')
        actual=hashlib.sha256(canon.canonical_bytes(rel,path.read_bytes())).hexdigest()
        if actual!=expected:
            if rel not in ALLOWED: raise SystemExit(f'[FAIL] unexpected PH10-R01 baseline content modification: {rel}')
            controlled.append(rel)
        else: preserved+=1
    missing_allowed=[rel for rel in controlled if not (ROOT/rel).exists()]
    if missing_allowed: raise SystemExit('[FAIL] PH11 controlled parent file was deleted: '+', '.join(missing_allowed))
    for rel in REQUIRED_NEW:
        if not (ROOT/rel).exists(): raise SystemExit(f'[FAIL] PH11 required additive file missing: {rel}')
    print(f'[PASS] {preserved} PH10-R01 baseline files remain semantically preserved outside PH11 allowlist')
    print(f'[PASS] PH11 controlled PH10-R01 baseline changes: {len(set(controlled))}')
    print('[PASS] PH11 required feedback/error/cropper additions are present')
if __name__=='__main__': verify()
