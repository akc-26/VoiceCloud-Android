from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
checks=[]
def t(rel): return (ROOT/rel).read_text(encoding='utf-8')
def add(name, ok):
    checks.append(ok); print(('[PASS] ' if ok else '[FAIL] ')+name)

screens=t('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt')
repo=t('feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt')
vm=t('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorViewModel.kt')
api=t('feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt')
module=t('feature/creator/src/main/java/app/voicecloud/feature/creator/di/CreatorNetworkModule.kt')
nav=t('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
auth=t('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt')
discovery=t('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')

for sym, imp in [
    ('rememberSaveable','androidx.compose.runtime.saveable.rememberSaveable'),
    ('KeyboardOptions','androidx.compose.foundation.text.KeyboardOptions'),
    ('KeyboardType','androidx.compose.ui.text.input.KeyboardType'),
    ('Modifier','androidx.compose.ui.Modifier'),
    ('Alignment','androidx.compose.ui.Alignment'),
    ('TextOverflow','androidx.compose.ui.text.style.TextOverflow'),
    ('Html','android.text.Html'),
]: add(f'CreatorScreens resolves {sym}', imp in screens)
for sym in ['Body','GET','PATCH','POST','Path']:
    add(f'CreatorApi imports Retrofit {sym}', f'import retrofit2.http.{sym}' in api)
for sym in ['Module','Provides','InstallIn','SingletonComponent','Named','Singleton']:
    add(f'CreatorNetworkModule imports {sym}', sym in module)
for sym in ['ViewModel','viewModelScope','HiltViewModel','HttpException','Channel','MutableStateFlow','StateFlow','asStateFlow','receiveAsFlow','launch','Job']:
    add(f'CreatorViewModel resolves {sym}', sym in vm)
add('CreatorRepository uses explicit empty generic maps', 'emptyMap<Any?, Any?>()' in repo)
add('CreatorRepository Any envelope parsing is null-safe', 'as? Map<*, *>' in repo and 'as? List<*>' in repo)
add('Creator route helper URI-encodes CMS slug', 'Uri.encode(slug.trim())' in nav)
add('Creator CMS route URI-decodes slug', 'Uri.decode(entry.arguments?.getString("slug").orEmpty())' in nav)
add('Creator dashboard callbacks match screen signature', 'CreatorDashboardScreen(' in nav and 'onSwitchToVoiceCloud = authViewModel::switchToUserPortal' in nav)
add('Creator profile callbacks match screen signature', 'onSave = creatorViewModel::saveProfile' in nav)
add('Creator settings callbacks match screen signature', 'onSave = creatorViewModel::saveSettings' in nav)
add('Creator support callback matches ViewModel signature', 'onSubmit = creatorViewModel::contact' in nav)
add('consumer profile Creator switch callback matches signature', 'onSwitchToCreator = authViewModel::switchToCreatorPortal' in nav and 'onSwitchToCreator: () -> Unit' in discovery)
add('AuthViewModel imports LastPortal for portal switching', 'import app.voicecloud.core.preferences.LastPortal' in auth)
add('PH10 compile surface contains no merge markers', not any(x in '\n'.join([screens,repo,vm,api,module,nav,auth,discovery]) for x in ['<<<<<<<','=======\n>>>>>>>','>>>>>>>']))
add('PH10 Creator module has no direct admin imports/endpoints', 'admin/' not in '\n'.join([screens,repo,vm,api,module]))

if not all(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH10-R01 compile-surface regression: {sum(checks)}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH10-R01 compile-surface regression: {len(checks)}/{len(checks)} PASS')
