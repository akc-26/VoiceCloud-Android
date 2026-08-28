from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
modified=[
'core/network/src/main/java/app/voicecloud/core/network/UserFacingErrors.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt',
'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileImageCropper.kt',
'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt',
'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt',
]
for rel in modified:
    text=read(rel)
    ck(f'no merge markers: {rel}', all(x not in text for x in ['<<<<<<<','=======','>>>>>>>']))
    if 'voiceCloudTitleCase(' in text and 'core/designsystem/component/VoiceCloudFeedback.kt' not in rel and not rel.startswith('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/'):
        ck(f'Title Case import resolved: {rel}', 'import app.voicecloud.core.designsystem.component.voiceCloudTitleCase' in text)
# Cropper compile-sensitive imports and APIs
crop=read('feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileImageCropper.kt')
for token in ['import android.graphics.ImageDecoder','import androidx.compose.ui.graphics.asImageBitmap','import androidx.compose.ui.layout.ContentScale','import androidx.compose.ui.unit.IntOffset','import androidx.compose.ui.unit.IntSize','import androidx.compose.ui.unit.LayoutDirection','ByteArrayOutputStream']:
    ck(f'cropper compile surface resolves {token}', token in crop)
# Navigation callbacks and URI routes
nav=read('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
ck('Creator dynamic PH11 room/schedule routes URI encode', all(x in nav for x in ['Uri.encode(id.trim())','Uri.decode(entry.arguments?.getString("roomId")','Uri.decode(entry.arguments?.getString("scheduleId")']))
ck('Creator Live screen callback signatures wired', all(x in nav for x in ['hostingViewModel.startScheduled(schedule)','hostingViewModel::searchInviteCandidates','hostingViewModel::setMicrophoneEnabled']))
ck('Device route uses deviceId argument consistently', 'securityDevice(it)' in nav and 'getString("deviceId")' in nav)
# Retrofit settings body shapes
api=read('feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsApi.kt')
ck('Settings Retrofit has Body/Path imports', 'import retrofit2.http.Body' in api and 'import retrofit2.http.Path' in api)
# Auth safe error extension import
avm=read('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt')
ck('Auth safe error extension import resolves', 'import app.voicecloud.core.network.toVoiceCloudUserMessage' in avm)
# New network tests compile conceptually against HttpException
net=read('core/network/src/main/java/app/voicecloud/core/network/UserFacingErrors.kt')
ck('safe network mapper imports Retrofit HttpException', 'import retrofit2.HttpException' in net)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH11-R02 compile-surface regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH11-R02 compile-surface regression: {passed}/{len(checks)} PASS')
