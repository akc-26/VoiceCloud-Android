from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
route=(ROOT/'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt').read_text(encoding='utf-8')
nav=(ROOT/'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt').read_text(encoding='utf-8')
checks=[]
def ck(name, cond):
    if not cond: raise SystemExit(f'[FAIL] {name}')
    checks.append(name); print(f'[PASS] {name}')
ck('BootstrapRoute keeps default Hilt ViewModel first', re.search(r'fun\s+BootstrapRoute\(\s*viewModel:\s*BootstrapViewModel\s*=\s*hiltViewModel\(\)', route) is not None)
ck('BootstrapRoute keeps onReady callback last', re.search(r'viewModel:[^,]+,[\s\S]{0,160}onReady:\s*\(app\.voicecloud\.core\.model\.MobileConfig\)\s*->\s*Unit\s*=\s*\{\}\s*\)\s*\{', route) is not None)
ck('VoiceCloudNavHost retains trailing-lambda BootstrapRoute call', 'BootstrapRoute { config ->' in nav)
ck('R04 invalid provider-function @param ApplicationContext not present', '@param:ApplicationContext context: Context' not in (ROOT/'app/src/main/java/app/voicecloud/android/di/FoundationModule.kt').read_text(encoding='utf-8'))
ck('R03 Google auth implementation preserved without R04 Credential Manager migration', 'GoogleSignInOptions' in (ROOT/'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt').read_text(encoding='utf-8'))
print(f'VC-ANDROID-PH02-R05 targeted preservation regression: {len(checks)}/{len(checks)} PASS')
