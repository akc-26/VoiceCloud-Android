from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; checks=[]
def r(p): return (ROOT/p).read_text(encoding='utf-8')
def ck(n,o): checks.append(bool(o)); print(('[PASS] ' if o else '[FAIL] ')+n)
files=['app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/model/CreatorModels.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt','feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorViewModel.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingApi.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingRepository.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/model/HostingModels.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt','feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingViewModel.kt']
for f in files:
    t=r(f); ck('no merge markers '+f,not any(x in t for x in ['<<<<<<<','=======','>>>>>>>'])); ck('balanced braces '+f,t.count('{')==t.count('}')); ck('balanced parentheses '+f,t.count('(')==t.count(')'))
creator=r(files[4]); nav=r(files[0]); hapi=r(files[6]); hrepo=r(files[7]); hscreens=r(files[9]); hvm=r(files[10])
ck('Creator vertical-scroll import resolves','import androidx.compose.foundation.verticalScroll' in creator)
ck('Creator phase13 callbacks match navigation',all(x in creator for x in ['onAnalytics: () -> Unit','onWallet: () -> Unit','onPayouts: () -> Unit','onVerification: () -> Unit']) and all(x in nav for x in ['onAnalytics = {','onWallet = {','onPayouts = {','onVerification = {']))
ck('Creator payout route helper URI encodes','fun creatorPayout(id: String)' in nav and 'Uri.encode(id.trim())' in nav)
ck('Hosting Retrofit multipart imports resolve',all(x in hapi for x in ['import retrofit2.http.Multipart','import retrofit2.http.Part','import retrofit2.http.PUT','import okhttp3.MultipartBody']))
ck('Hosting OkHttp multipart imports resolve',all(x in hrepo for x in ['import okhttp3.MultipartBody','toMediaTypeOrNull','toRequestBody']))
ck('Hosting activity picker imports resolve','rememberLauncherForActivityResult' in hscreens and 'ActivityResultContracts.OpenDocument()' in hscreens and 'LocalContext' in hscreens)
ck('Hosting ViewModel callback signatures resolve','fun uploadVerification(' in hvm and 'fun replaceVerification(' in hvm)
ck('Host Studio verification callback matches navigation','onVerification: () -> Unit' in hscreens and 'onVerification = { open(VoiceCloudRoutes.HostVerification) }' in nav)
ck('No unresolved PH13 formatValue helper','formatValue' not in r(files[2]))
ck('Hosting module has Activity Compose + Retrofit','implementation(libs.androidx.activity.compose)' in r('feature/hosting/build.gradle.kts') and 'implementation(libs.retrofit.core)' in r('feature/hosting/build.gradle.kts'))
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R01 compile surface: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R01 compile surface: {passed}/{len(checks)} PASS')
