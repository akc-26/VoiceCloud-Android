from pathlib import Path
import collections
import hashlib
import re

ROOT = Path(__file__).resolve().parents[1]
PARENT_MANIFEST = ROOT / 'contracts/VC-ANDROID-PH13-R04-SOURCE-MANIFEST.sha256'

UI_ALLOWLIST = {
    'app/build.gradle.kts',
    'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
    'branding/res/drawable/vc_brand_app_icon_foreground.xml',
    'branding/res/drawable/vc_brand_splash.xml',
    'branding/voicecloud-brand.properties',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudBrandMark.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
    'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
    'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
    'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
    'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
    'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
    'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
}
NEW_UI_FILES = {
    'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt',
}

UI_SCREEN_EXPECTATIONS = {
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt': ('dd99fb66c22274d1bb1207105e31abc86979b931012aa9335992305e6c96769a', '2a989e7226c712aa672839acbf5b17ae399f8db4e3527d58747a67f0b096d908'),
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt': ('4830abee1b6965dfad1178848b33912010460496228c6651f9882eda58ac2153', '855187649081279f710c0488d9b5700588f6f8fd2781f0fc6882d1f471dbd7f9'),
    'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt': ('b2aee8fdd2c961efe9211cdd60b90088901d5efee603087c2e82a441153755bb', 'a4798de21e84d81a84469969f6c0164640f765bdf7110b804358cb31e05379c8'),
    'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt': ('2a4ce6a136a3d9dd27a67e16c82c6a12796cb3571ea85b249da69edf7d311a92', '209ad5c568d45a3439461b9e9faf9fe16e824881335897a8dda3110c4285fbb1'),
    'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt': ('a27d8478bffacac50ba9ad44026c0323adac20e1f6c0534bb51708c99cab8621', '3a8133f149012cc186b81bf94da60e2e6346ee9659babd2ee4967abbb4d17e73'),
    'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt': ('f1c6c0212ea166d432d65ca400c8b25f3d5850fbc4928392c6d467a009831b08', 'ea1b833eb873d143b0a72a6917d25878129f290cd082de755257138e26d657df'),
    'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt': ('8bdcea674df928ef7204eeda4e0e6e13f4bafbb3b9400362581a64450e20bc3a', 'f238effe1a96193b55a635c7c3927f3d8fb5a328e5ea58377dd7af755d0a0294'),
    'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt': ('66d5378fc25ac6f21e23c0a233ca4a28821e57f15a9b8bbbac073ee331bc1b44', '5b595040bdbd2c695d924ad48c4ade7d73a9d35c6f3f0019729039dbbb024082'),
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt': ('a6fe5e2a81531c675057a7a2bfca1bf86bb4e172e0e7748b9370eb294460d0b6', '07a708274674887dafef1b34009a324cf055b6d437eb80aaf920fe46f9577865'),
}
NAV_NORMALIZED_SHA = 'a0668dc03c3c3740d83df13b34c3ffb5d3aa7d4f9ea766f67597bf96d1fa5ba8'
BUILD_NORMALIZED_SHA = '95d4e3dd43ac7edccdbb9fc578549f8a279f3879ad687d5a5e64c15c98650f9a'

checks = []
def check(label, ok):
    checks.append(bool(ok))
    print(('[PASS] ' if ok else '[FAIL] ') + label)

def sha_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

def sha_text(value: str) -> str:
    return sha_bytes(value.encode('utf-8'))

def public_signatures(text: str):
    out = []
    lines = text.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i]
        if re.match(r'^fun\s+\w+', line):
            acc = line.strip()
            depth = line.count('(') - line.count(')')
            while ('{' not in acc or depth > 0) and i + 1 < len(lines):
                i += 1
                nxt = lines[i].strip()
                acc += ' ' + nxt
                depth += nxt.count('(') - nxt.count(')')
            out.append(re.sub(r'\s+', ' ', acc.split('{', 1)[0].strip()))
        i += 1
    return out

def callback_fingerprint(text: str) -> str:
    calls = collections.Counter(re.findall(r'\b(on[A-Z][A-Za-z0-9_]*)\s*\(', text))
    return sha_text('\n'.join(f'{key}:{calls[key]}' for key in sorted(calls)))

if not PARENT_MANIFEST.is_file():
    raise SystemExit('[FAIL] PH13-R04 parent manifest is missing')

parent = {}
for line in PARENT_MANIFEST.read_text(encoding='utf-8').splitlines():
    if line.strip():
        expected, rel = line.split('  ', 1)
        parent[rel] = expected

# Every parent file outside the UI allowlist must remain byte-identical.
drift = []
for rel, expected in parent.items():
    if rel in UI_ALLOWLIST:
        continue
    path = ROOT / rel
    if not path.is_file() or sha_bytes(path.read_bytes()) != expected:
        drift.append(rel)
check('all API/repository/ViewModel/model/security/realtime/non-UI parent files remain byte-identical to PH13-R04', not drift)
if drift:
    print('[DETAIL] unexpected non-UI drift: ' + ', '.join(drift[:20]))

# New product files are restricted to the centralized visual primitive module.
parent_paths = set(parent)
new_product = []
for path in ROOT.rglob('*'):
    if not path.is_file():
        continue
    rel = path.relative_to(ROOT).as_posix()
    if rel in parent_paths or rel.startswith('docs/') or rel.startswith('scripts/') or rel.startswith('contracts/') or rel.startswith('VC-ANDROID-PH13-R05-'):
        continue
    new_product.append(rel)
check('new product source is limited to approved UI primitive files', set(new_product) <= NEW_UI_FILES)
if set(new_product) - NEW_UI_FILES:
    print('[DETAIL] unexpected new product files: ' + ', '.join(sorted(set(new_product) - NEW_UI_FILES)))

# Public screen contracts and callback invocation counts remain exactly as in R04.
for rel, (sig_sha, callback_sha) in UI_SCREEN_EXPECTATIONS.items():
    text = (ROOT / rel).read_text(encoding='utf-8')
    check(f'public screen contract preserved: {rel}', sha_text('\n'.join(public_signatures(text))) == sig_sha)
    check(f'UI callback invocation contract preserved: {rel}', callback_fingerprint(text) == callback_sha)

# Navigation implementation may only differ by presentation-theme choice.
nav = (ROOT / 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt').read_text(encoding='utf-8')
nav_normalized = re.sub(
    r'VoiceCloudTheme\(portal = PortalTheme\.Creator, darkTheme = (?:true|false)\)',
    'VoiceCloudTheme(portal = PortalTheme.Creator, darkTheme = <UI_THEME>)',
    nav,
)
check('navigation behavior unchanged apart from Creator light/dark presentation selection', sha_text(nav_normalized) == NAV_NORMALIZED_SHA)

# App Gradle may only change the launcher presentation color authority.
build = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
build_normalized = re.sub(
    r'resValue\("color", "vc_brand_launcher_background", brand\("[^"]+"\)\)',
    'resValue("color", "vc_brand_launcher_background", brand("<UI_COLOR>"))',
    build,
)
check('app build behavior unchanged apart from launcher background design token', sha_text(build_normalized) == BUILD_NORMALIZED_SHA)

brand = (ROOT / 'branding/voicecloud-brand.properties').read_text(encoding='utf-8')
for token in ['#006C63', '#004F49', '#FBF8F1', '#FFFEFA', '#D5AA52']:
    check(f'approved premium palette token present: {token}', token in brand)

premium = (ROOT / 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt').read_text(encoding='utf-8')
for primitive in ['VoiceCloudPremiumBackdrop', 'VoiceCloudPremiumCard', 'VoiceCloudHeroCard', 'VoiceCloudAnimatedWaveform', 'VoiceCloudLiveBadge', 'VoiceCloudSpeakingAvatar', 'VoiceCloudShimmer']:
    check(f'premium design primitive implemented: {primitive}', f'fun {primitive}' in premium)
check('reduced-motion preference is respected by premium animation primitives', 'ANIMATOR_DURATION_SCALE' in premium)

live = (ROOT / 'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt').read_text(encoding='utf-8')
check('listener live room uses immersive premium backdrop', 'VoiceCloudPremiumBackdrop' in live and 'dark = true' in live)
check('listener live room has speaking visualization', 'VoiceCloudSpeakingAvatar' in live and 'isSpeaking' in live)

host = (ROOT / 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt').read_text(encoding='utf-8')
check('host live console uses dark premium live treatment', 'HostPage("Host Console"' in host and 'dark = true' in host and 'VoiceCloudLiveBadge' in host)

passed = sum(checks)
if passed != len(checks):
    raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R05 UI/UX preservation regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R05 UI/UX preservation regression: {passed}/{len(checks)} PASS')
