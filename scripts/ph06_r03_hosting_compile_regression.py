from pathlib import Path
import re, sys
root = Path(__file__).resolve().parents[1]
path = root / 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt'
s = path.read_text(encoding='utf-8')
accept = (root / 'scripts/VC-ANDROID-PH06-R03-ACCEPTANCE.cmd').read_text(encoding='utf-8')
checks=[]
def ck(name, ok):
    if not ok:
        print(f'[FAIL] {name}')
        sys.exit(1)
    checks.append(name); print(f'[PASS] {name}')

ck('reported unsafe nullable stage speaker dereference removed',
   'stage?.participants.orEmpty().filter { p -> stage.speakers.none' not in s)
ck('nullable stage model remains explicit', 'stage: RoomStageState?' in s)
ck('speaker snapshot is null-safe', 'val currentSpeakers = stage?.speakers.orEmpty()' in s)
ck('participant list is null-safe', 'stage?.participants.orEmpty()' in s)
ck('audience filter uses non-null speaker snapshot',
   'currentSpeakers.none { speaker -> speaker.userId == participant.userId }' in s)
ck('hosting UI contains no direct stage dot dereference', re.search(r'(?<!\?)\bstage\.', s) is None)
ck('R03 acceptance runs Windows R03 source checker', 'VC-ANDROID-PH06-R03-SOURCE-CHECK.ps1' in accept)
ck('R03 acceptance runs R03 compile regression', 'ph06_r03_hosting_compile_regression.py' in accept)
ck('R03 acceptance retains R02 premium UI regression', 'ph06_r02_premium_ui_regression.py' in accept)
ck('R03 acceptance retains debug compile gate', ':app:compileDebugKotlin' in accept)
ck('R03 acceptance retains staging compile gate', ':app:compileStagingKotlin' in accept)
ck('R03 acceptance retains release compile gate', ':app:compileReleaseKotlin' in accept)
ck('R03 acceptance retains tests lint assemblies and androidTest', all(x in accept for x in [' test ', 'lintDebug', 'lintStaging', 'lintRelease', ':app:assembleDebug', ':app:assembleStaging', ':app:assembleRelease', ':app:assembleDebugAndroidTest']))
print(f'VC-ANDROID-PH06-R03 hosting compile regression: {len(checks)}/{len(checks)} PASS')
