from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
PARENT = ROOT / 'contracts/VC-ANDROID-PH13-R09-PH13-R08-PARENT-CANONICAL.sha256'
ALLOWED_PRODUCT_CHANGED = {
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt',
    'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
    'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
    'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
    'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
    'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
    'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
}
FUNCTIONAL_MARKERS = (
    '/data/', 'Repository.kt', 'ViewModel.kt', 'Models.kt', 'Api.kt',
    'VoiceCloudNavHost.kt', 'core/network/', 'core/security/', 'realtime/',
)
if not PARENT.is_file():
    raise SystemExit('[FAIL] R08 parent canonical manifest missing')
parent = {}
for line in PARENT.read_text(encoding='utf-8').splitlines():
    if line.strip():
        digest, rel = line.split('  ', 1)
        parent[rel] = digest
failures=[]; changed=[]
for rel, expected in parent.items():
    path=ROOT/rel
    if not path.is_file():
        failures.append(f'missing R08 tracked file: {rel}')
        continue
    actual=hashlib.sha256(path.read_bytes()).hexdigest()
    if actual != expected:
        if rel in ALLOWED_PRODUCT_CHANGED or rel in {'CHANGELOG.md','README.md'}:
            changed.append(rel)
        elif rel.startswith(('VC-ANDROID-PH13-R08-','docs/VC-ANDROID-PH13-R08-','scripts/VC-ANDROID-PH13-R08-','scripts/ph13_r08_','contracts/VC-ANDROID-PH13-R08-')):
            failures.append(f'R08 acceptance/evidence authority drifted unexpectedly: {rel}')
        else:
            failures.append(f'unexpected R08 product/delivery drift: {rel}')
# The one intentional new Android source file.
visual_rel='core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt'
if not (ROOT/visual_rel).is_file(): failures.append('new centralized visual primitive file missing')
# No other new Kotlin source may appear relative to R08.
parent_kotlin={rel for rel in parent if rel.endswith('.kt')}
for path in ROOT.rglob('*.kt'):
    rel=path.relative_to(ROOT).as_posix()
    if rel not in parent_kotlin and rel != visual_rel:
        failures.append(f'unexpected new Kotlin product source: {rel}')
# Functional authority is explicitly forbidden from the allowed set.
if any(any(marker in rel for marker in FUNCTIONAL_MARKERS) for rel in ALLOWED_PRODUCT_CHANGED):
    failures.append('R09 allowed UI set touches functional/business authority')
# Ensure all intended page groups actually changed from R08.
required_changed={rel for rel in ALLOWED_PRODUCT_CHANGED if rel != visual_rel}
missing_changes=sorted(required_changed-set(changed))
if missing_changes:
    failures.append('expected page-level UI files did not change: '+', '.join(missing_changes))
if failures:
    for item in failures: print('[FAIL]', item)
    raise SystemExit(1)
print(f'[PASS] R09 product changes are limited to {len(ALLOWED_PRODUCT_CHANGED)} approved presentation files')
print('[PASS] APIs/repositories/ViewModels/models/navigation/security/realtime/business logic remain R08-identical')
print('[PASS] End User and Creator/Host screen modules all receive page-level presentation changes')
print('[PASS] New centralized VoiceCloudVisuals.kt is the only new Android product source file')
