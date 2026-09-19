from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / 'contracts/VC-ANDROID-PH13-R14-PH13-R13-PARENT-PRODUCT-CANONICAL.sha256'

ALLOWED_CHANGED = {
    'app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt',
    'core/designsystem/build.gradle.kts',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt',
    'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
    'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
    'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
    'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
    'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
    'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
}
ALLOWED_NEW = {
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudMedia.kt',
}
ROOT_PREFIXES = ('app/', 'branding/', 'core/', 'feature/', 'gradle/')
ROOT_FILES = {'build.gradle.kts', 'settings.gradle.kts', 'gradle.properties'}
GENERATED_ROOM_PREFIX = 'core/database/schemas/app.voicecloud.core.database.VoiceCloudDatabase/'


def is_product(rel: str) -> bool:
    return rel.startswith(ROOT_PREFIXES) or rel in ROOT_FILES


def is_generated_room_schema(rel: str) -> bool:
    return rel.startswith(GENERATED_ROOM_PREFIX) and rel.endswith('.json')

if not MANIFEST.is_file():
    raise SystemExit('[FAIL] R14 parent-product canonical manifest is missing')
parent = {}
for line in MANIFEST.read_text(encoding='utf-8').splitlines():
    if not line.strip():
        continue
    digest, rel = line.split('  ', 1)
    parent[rel] = digest

failures = []
protected = 0
changed = []
for rel, expected in parent.items():
    p = ROOT / rel
    if not p.is_file():
        failures.append(f'missing parent product/build input: {rel}')
        continue
    actual = hashlib.sha256(p.read_bytes()).hexdigest()
    if rel in ALLOWED_CHANGED:
        if actual != expected:
            changed.append(rel)
        continue
    protected += 1
    if actual != expected:
        failures.append(f'protected functional/build input drifted: {rel}')

# No unapproved new product/build-input file may appear. Exact Room schema output is generated and ignored.
for p in ROOT.rglob('*'):
    if not p.is_file():
        continue
    rel = p.relative_to(ROOT).as_posix()
    if not is_product(rel) or is_generated_room_schema(rel):
        continue
    if rel in parent or rel in ALLOWED_NEW:
        continue
    # machine/build outputs never count as product authority
    if any(part in {'.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__'} for part in p.relative_to(ROOT).parts):
        continue
    if p.name in {'local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties'} or p.suffix.lower() in {'.apk','.aab','.pyc','.pyo','.iml','.hprof'}:
        continue
    failures.append(f'unapproved new product/build-input file: {rel}')

for rel in sorted(ALLOWED_NEW):
    if not (ROOT / rel).is_file():
        failures.append(f'expected R14 presentation file missing: {rel}')

if failures:
    for failure in failures:
        print(f'[FAIL] {failure}')
    raise SystemExit(1)

print(f'[PASS] R14 preserves {protected} R13 functional/runtime/build inputs byte-for-byte')
print(f'[PASS] R14 product drift is restricted to {len(changed)} approved existing presentation/test/dependency files plus {len(ALLOWED_NEW)} centralized media file')
print('[PASS] API/repository/ViewModel/model/navigation/RTC/security/payment/business authority remains protected')
print('[PASS] Exact Room schema JSON output is classified as generated build evidence, not source drift')
