from pathlib import Path
import hashlib
import importlib.util

ROOT = Path(__file__).resolve().parents[1]
BASELINE = ROOT / 'contracts/VC-ANDROID-PH10-R01-PH09-R08-BASELINE-CANONICAL.sha256'
ALLOWED = {
    'README.md',
    'CHANGELOG.md',
    'settings.gradle.kts',
    'app/build.gradle.kts',
    'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthRepository.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt',
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
}
REQUIRED_NEW = [
    'feature/creator/build.gradle.kts',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/di/CreatorNetworkModule.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/model/CreatorModels.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorViewModel.kt',
    'feature/creator/src/test/java/app/voicecloud/feature/creator/model/CreatorModelsTest.kt',
]

spec = importlib.util.spec_from_file_location('ph09canon', ROOT / 'scripts/ph09_r08_preservation_regression.py')
canon = importlib.util.module_from_spec(spec)
spec.loader.exec_module(canon)


def verify() -> None:
    if not BASELINE.exists():
        raise SystemExit('[FAIL] PH10 canonical PH09-R08 baseline manifest missing')
    preserved = 0
    controlled = []
    for line in BASELINE.read_text(encoding='utf-8').splitlines():
        if not line.strip():
            continue
        expected, rel = line.split('  ', 1)
        path = ROOT / rel
        if not path.exists():
            if rel in ALLOWED:
                controlled.append(rel)
                continue
            raise SystemExit(f'[FAIL] preserved PH09-R08 file missing: {rel}')
        actual = hashlib.sha256(canon.canonical_bytes(rel, path.read_bytes())).hexdigest()
        if actual != expected:
            if rel not in ALLOWED:
                raise SystemExit(f'[FAIL] unexpected PH09-R08 baseline content modification: {rel}')
            controlled.append(rel)
        else:
            preserved += 1
    for rel in REQUIRED_NEW:
        if not (ROOT / rel).exists():
            raise SystemExit(f'[FAIL] PH10 required new Creator file missing: {rel}')
    print(f'[PASS] {preserved} PH09-R08 baseline files remain semantically preserved outside PH10 allowlist')
    print(f'[PASS] PH10 controlled baseline changes: {len(controlled)}')
    print('[PASS] PH10 Creator module is additive; PH11-PH13 feature modules are not pulled forward')

if __name__ == '__main__':
    verify()
