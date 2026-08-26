from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
checks=[]
def text(p): return (root/p).read_text(encoding='utf-8')
def ok(label, condition):
    if not condition: raise SystemExit(f'[FAIL] {label}')
    checks.append(label); print(f'[PASS] {label}')
settings=text('settings.gradle.kts')
for module in [':app',':core:model',':core:designsystem',':core:network',':core:security',':core:database',':core:preferences',':core:logging',':core:realtime',':feature:bootstrap']:
    ok(f'module {module} registered', f'include("{module}")' in settings)
app=text('app/build.gradle.kts'); root_build=text('build.gradle.kts'); catalog=text('gradle/libs.versions.toml'); builds='\n'.join(p.read_text(encoding='utf-8') for p in root.rglob('build.gradle.kts') if p != root/'build.gradle.kts')
ok('AGP9 built-in Kotlin retained', 'org.jetbrains.kotlin.android' not in catalog and 'libs.plugins.kotlin.android' not in builds and 'kotlinOptions' not in builds)
ok('debug staging release variants retained', all(x in app for x in ['debug {','create("staging")','release {']))
ok('compileSdk 37 locked', 'compileSdk = 37' in app); ok('targetSdk 36 locked', 'targetSdk = 36' in app); ok('minSdk 26 locked', 'minSdk = 26' in app)
colors=text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudColors.kt')
for token in ['0xFF0B7C86','0xFF075762','0xFF22C7CF','0xFF22C55E','0xFF123A32']: ok(f'R06 design token {token} retained', token in colors)
ok('fresh User launch is Light-first', 'darkTheme = false' in text('app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt'))
nav=text('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'); ok('dual portal bootstrap navigation retained', all(x in nav for x in ['NavHost','UserPortal','CreatorPortal']))
vault=text('core/security/src/main/java/app/voicecloud/core/security/AndroidKeyStoreTokenVault.kt'); ok('Keystore AES-GCM token vault retained', all(x in vault for x in ['AndroidKeyStore','AES/GCM/NoPadding','GCMParameterSpec']))
inter=text('core/network/src/main/java/app/voicecloud/core/network/AuthTokenInterceptor.kt'); ok('REST auth reads secure token authority', 'tokenVault.accessToken()' in inter and 'Bearer $token' in inter)
ok('bootstrap calls authoritative mobile config', '@GET("config/mobile")' in text('core/network/src/main/java/app/voicecloud/core/network/BootstrapApi.kt'))
route=text('feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt'); ok('bootstrap surfaces all authority states', all(x in route for x in ['BootstrapState.Loading','BootstrapState.Maintenance','BootstrapState.ForceUpdate','BootstrapState.Ready','BootstrapState.Error']))
api=json.loads(text('contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json')); ok('A0-ready API contract retained', api['authority']['commit']=='7bee8d463786d21a7200b5663a499408bb229115' and api['backendOperations']==700)
screens=json.loads(text('contracts/screens/VC-ANDROID-R06-SCREEN-PARITY-R01.json')); ok('Website/Creator/design parity contract retained', screens['counts']['websitePageSources']==74 and screens['counts']['creatorPageSources']==22 and screens['counts']['approvedWebsiteDesigns']==84)
ok('Retrofit public ABI retained', 'api(libs.retrofit.core)' in text('core/network/build.gradle.kts'))
ok('Room/Flow public ABI retained', 'api(libs.androidx.room.runtime)' in text('core/database/build.gradle.kts') and 'api(libs.kotlinx.coroutines.android)' in text('core/database/build.gradle.kts'))
props=text('gradle.properties'); live='https://voicecloud.tailfca77b.ts.net'
for key in ['VOICECLOUD_DEBUG_API_BASE_URL','VOICECLOUD_DEBUG_SOCKET_BASE_URL','VOICECLOUD_DEBUG_WEB_BASE_URL']:
    ok(f'{key} points to Raspberry Pi live authority', f'{key}={live}' in props and key in app)
resolver=text('app/src/main/java/app/voicecloud/android/network/ApiEndpointResolver.kt')
ok('API live root normalizes to /api/v1/', 'normalizeApiBaseUrl' in resolver and 'endsWith("/api/v1"' in resolver)
ok('runtime no longer depends on emulator/adb loopback', all(x not in resolver for x in ['10.0.2.2','127.0.0.1','android.os.Build']))
ok('debug network policy is HTTPS-only for live server', 'cleartextTrafficPermitted="false"' in text('app/src/debug/res/xml/network_security_config.xml'))
endpoint_test=text('app/src/test/java/app/voicecloud/android/network/ApiEndpointResolverTest.kt')
ok('R08 endpoint unit tests match three-authority resolver', all(x in endpoint_test for x in ['liveServerRootExpandsToVersionedApiBase','fullApiBaseIsNotDuplicated','socketUsesServerOriginAndWebUsesTrailingSlash','releaseUsesConfiguredLiveAuthoritiesWithoutDeviceRewrite']))
ok('obsolete R07 endpoint test symbols removed', 'EmulatorDebugBaseUrl' not in endpoint_test and 'PhysicalDeviceDebugBaseUrl' not in endpoint_test)
foundation=text('app/src/main/java/app/voicecloud/android/di/FoundationModule.kt'); ok('REST and Socket consume resolved endpoints', 'ApiClientFactory.create(endpoints.apiBaseUrl' in foundation and 'RealtimeClientFactory.create(endpoints.socketBaseUrl' in foundation)
rt=text('core/realtime/src/main/java/app/voicecloud/core/realtime/RealtimeClient.kt'); ok('Socket.IO matches backend /realtime namespace and /socket.io path', 'trimEnd(\'/\') + "/realtime"' in rt and 'path = "/socket.io"' in rt)
ok('Socket.IO auth remains fail-closed', 'tokenVault.accessToken()' in rt and 'connection_established' in rt and 'auth_error' in rt)
accept=text('scripts/VC-ANDROID-PH01-R08-ACCEPTANCE.cmd'); ok('acceptance verifies live Raspberry Pi connectivity', 'VC-ANDROID-LIVE-CONNECTIVITY-CHECK.cmd' in accept and 'VC-ANDROID-LOCAL-DEVICE-BACKEND.cmd' not in accept)
ok('PH01 still contains no business feature module', not any((root/'feature').glob('consumer*')) and not any((root/'feature').glob('creator*')))
print(f'VC-ANDROID-PH01-R08 source authority: {len(checks)}/{len(checks)} PASS')
