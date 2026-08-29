from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]; checks=[]
def r(p): return (ROOT/p).read_text(encoding='utf-8')
def ck(n,o): checks.append(bool(o)); print(('[PASS] ' if o else '[FAIL] ')+n)
files=['app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/model/CreatorModels.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorViewModel.kt','feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryRepository.kt','feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/EngagementRepository.kt','feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt','feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementViewModel.kt']
for f in files:
 t=r(f); ck('no merge markers '+f,not any(x in t for x in ['<<<<<<<','=======','>>>>>>>']))
ck('no direct Text literals after Title Case authority',not any(re.search(r'\bText\(\s*"',r(f)) for f in files if f.endswith('Screens.kt')))
creator=r(files[4]); nav=r(files[0]); engagement=r('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt')
ck('Creator Dashboard callback signature matches PH12 navigation','onAudience: () -> Unit' in creator and 'onMessages: () -> Unit' in creator and 'onAudience = {' in nav and 'onMessages = {' in nav)
ck('LazyRow import resolves PH12 chips','import androidx.compose.foundation.lazy.LazyRow' in creator)
ck('IME send imports resolve','import androidx.compose.foundation.text.KeyboardActions' in engagement and 'import androidx.compose.ui.text.input.ImeAction' in engagement and 'ImeAction.Send' in engagement)
ck('Creator dynamic conversation route URI encodes','fun creatorConversation(id: String)' in nav and 'Uri.encode(id.trim())' in nav)
ck('PH12 creator module dependency resolves discovery','implementation(project(":feature:discovery"))' in r('feature/creator/build.gradle.kts'))
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH12-R01 compile surface: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH12-R01 compile surface: {passed}/{len(checks)} PASS')
