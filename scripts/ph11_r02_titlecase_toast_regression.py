from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
feedback=read('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt')
errors=read('core/network/src/main/java/app/voicecloud/core/network/UserFacingErrors.kt')
ck('central Title Case helper exists', 'fun voiceCloudTitleCase(value: String)' in feedback)
ck('central transient feedback uses Android Toast', 'Toast.makeText(' in feedback and 'fun VoiceCloudToastEffect' in feedback)
ck('toast copy passes through Title Case helper', 'voiceCloudTitleCase(message)' in feedback)
ui_roots=[ROOT/'feature',ROOT/'app/src/main/java',ROOT/'core/designsystem/src/main/java']
direct=[]
for root in ui_roots:
    if not root.exists(): continue
    for p in root.rglob('*.kt'):
        text=p.read_text(encoding='utf-8',errors='ignore')
        # User-visible Compose literal Text calls must use the centralized helper.
        if re.search(r'\bText\(\s*"', text): direct.append(str(p.relative_to(ROOT)))
ck('no direct user-visible Compose Text string literals remain', not direct)
# Major mutable feature surfaces all consume transient feedback
for rel in [
 'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
 'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
 'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
 'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
 'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
 'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
]: ck(f'transient toast feedback wired: {rel}', 'VoiceCloudToastEffect' in read(rel))
# No raw state.error is rendered as permanent Text
raw=[]
for p in (ROOT/'feature').rglob('*Screens.kt'):
    text=p.read_text(encoding='utf-8',errors='ignore')
    if re.search(r'Text\([^\n]*(?:state|uiState)\.error',text): raw.append(str(p.relative_to(ROOT)))
ck('feature screens do not render state.error inline as Text', not raw)
ck('technical HTTP/database markers are sanitized centrally', all(x in errors for x in ['"http "','"sql"','"typeorm"','"constraint"','"stack trace"','"retrofit"']))
ck('400/404/409/422 settings errors use contextual fallback', 't.code() in listOf(400, 404, 409, 422) -> fallback' in read('feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsViewModel.kt'))
ck('Auth errors no longer directly expose generic exception messages', 'e.toVoiceCloudUserMessage(' in read('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt') and 'authApiMessage(' in read('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt'))
# Examples explicitly requested
all_ui='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in (ROOT/'feature').rglob('*Screens.kt'))
ck('Voice & Appearance exact Title Case is present', 'Voice & Appearance' in all_ui)
ck('lowercase Voice & appearance example is absent', 'Voice & appearance' not in all_ui)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH11-R02 Title Case/toast regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH11-R02 Title Case/toast regression: {passed}/{len(checks)} PASS')
