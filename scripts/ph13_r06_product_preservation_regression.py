from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
PARENT = ROOT / 'contracts/VC-ANDROID-PH13-R06-PH13-R05-PARENT-CANONICAL.sha256'

# R06 is acceptance-harness-only. The R05 Android product/UI implementation must
# remain byte-identical. Only shared wrapper bootstrap and R06 documentation/
# acceptance authority may differ or be added.
ALLOWED_PARENT_DRIFT = {
    'README.md',
    'CHANGELOG.md',
    'scripts/BOOTSTRAP-GRADLE-WRAPPER.ps1',
    'scripts/BOOTSTRAP-GRADLE-WRAPPER.sh',
}

IGNORED_GENERATED_DIRS = {
    '.git', '.gradle', '.idea', '.kotlin', 'build', 'captures',
    '.externalNativeBuild', '.cxx', '__pycache__'
}
IGNORED_GENERATED_FILES = {
    'local.properties', 'secrets.properties', 'gradlew', 'gradlew.bat',
    'gradle-wrapper.jar', 'gradle-daemon-jvm.properties', '.DS_Store'
}
IGNORED_SUFFIXES = {'.apk', '.aab', '.pyc', '.pyo', '.iml', '.hprof'}


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def is_generated(path: Path) -> bool:
    rel = path.relative_to(ROOT)
    return (
        any(part in IGNORED_GENERATED_DIRS for part in rel.parts)
        or path.name in IGNORED_GENERATED_FILES
        or path.suffix.lower() in IGNORED_SUFFIXES
    )


def check(label: str, ok: bool) -> None:
    print(('[PASS] ' if ok else '[FAIL] ') + label)
    if not ok:
        raise SystemExit(1)

if not PARENT.is_file():
    raise SystemExit('[FAIL] R06 parent canonical manifest is missing')

parent = {}
for line in PARENT.read_text(encoding='utf-8').splitlines():
    if line.strip():
        expected, rel = line.split('  ', 1)
        parent[rel] = expected

unexpected_drift = []
for rel, expected in parent.items():
    if rel in ALLOWED_PARENT_DRIFT:
        continue
    p = ROOT / rel
    if not p.is_file() or sha(p) != expected:
        unexpected_drift.append(rel)

check(
    'R05 Android product/UI/API/runtime source remains byte-identical in R06',
    not unexpected_drift,
)

# The correction must not introduce new product files. R06 operational authority
# is confined to revision/docs/scripts/contracts; Android Studio/Gradle machine
# artifacts are ignored because they are not product source.
parent_paths = set(parent)
unexpected_new = []
for p in ROOT.rglob('*'):
    if not p.is_file() or is_generated(p):
        continue
    rel = p.relative_to(ROOT).as_posix()
    if rel in parent_paths or rel == 'contracts/VC-ANDROID-PH13-R05-SOURCE-MANIFEST.sha256':
        continue
    if (
        rel == 'VC-ANDROID-PH13-R06-REVISION.txt'
        or rel.startswith('docs/VC-ANDROID-PH13-R06-')
        or rel.startswith('scripts/VC-ANDROID-PH13-R06-')
        or rel.startswith('scripts/ph13_r06_')
        or rel.startswith('contracts/VC-ANDROID-PH13-R06-')
    ):
        continue
    unexpected_new.append(rel)

check('R06 introduces no new Android product source outside acceptance authority', not unexpected_new)

for rel in [
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
    'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
    'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
    'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
    'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
]:
    check(f'R05 premium UI implementation preserved: {rel}', rel in parent and sha(ROOT / rel) == parent[rel])

print('[PASS] VC-ANDROID-PH13-R06 product preservation regression complete')
