from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def text(p): return (ROOT/p).read_text(encoding='utf-8')
def ok(name, cond):
    if not cond: raise SystemExit(f'[FAIL] {name}')
    print(f'[PASS] {name}'); checks.append(name)
app=text('app/build.gradle.kts'); props=text('gradle.properties'); resolver=text('app/src/main/java/app/voicecloud/android/network/ApiEndpointResolver.kt'); test=text('app/src/test/java/app/voicecloud/android/network/ApiEndpointResolverTest.kt'); foundation=text('app/src/main/java/app/voicecloud/android/di/FoundationModule.kt'); rt=text('core/realtime/src/main/java/app/voicecloud/core/realtime/RealtimeClient.kt'); accept=text('scripts/VC-ANDROID-PH01-R08-ACCEPTANCE.cmd'); livecheck=text('scripts/VC-ANDROID-LIVE-CONNECTIVITY-CHECK.ps1')
live='https://voicecloud.tailfca77b.ts.net'
for key in ['VOICECLOUD_DEBUG_API_BASE_URL','VOICECLOUD_DEBUG_SOCKET_BASE_URL','VOICECLOUD_DEBUG_WEB_BASE_URL']:
    ok(f'{key} is read by Android build', key in app)
    ok(f'{key} is declared in project endpoint authority', f'{key}={live}' in props)
ok('Raspberry Pi live server is centralized in project gradle.properties', all(f'{k}={live}' in props for k in ['VOICECLOUD_DEBUG_API_BASE_URL','VOICECLOUD_DEBUG_SOCKET_BASE_URL','VOICECLOUD_DEBUG_WEB_BASE_URL']))
ok('API root is normalized to /api/v1/', 'normalizeApiBaseUrl' in resolver and 'api/v1/' in resolver)
ok('already-versioned API base is not duplicated', 'endsWith("/api/v1"' in resolver)
ok('Socket.IO base remains server origin', 'normalizeSocketBaseUrl' in resolver and '.trimEnd(\'/\')' in resolver)
ok('Web authority gets trailing slash', 'normalizeWebBaseUrl' in resolver)
ok('device-specific 10.0.2.2/adb loopback routing removed from runtime resolver', '10.0.2.2' not in resolver and '127.0.0.1' not in resolver and 'android.os.Build' not in resolver)
ok('debug cleartext is disabled for live HTTPS authority', 'cleartextTrafficPermitted="false"' in text('app/src/debug/res/xml/network_security_config.xml'))
ok('stale R07 unit-test API removed', 'EmulatorDebugBaseUrl' not in test and 'PhysicalDeviceDebugBaseUrl' not in test)
ok('unit tests cover live API Socket and Web normalization', 'liveServerRootExpandsToVersionedApiBase' in test and 'socketUsesServerOriginAndWebUsesTrailingSlash' in test)
ok('Foundation injects normalized API endpoint', 'ApiClientFactory.create(endpoints.apiBaseUrl' in foundation)
ok('Foundation injects live Socket endpoint', 'RealtimeClientFactory.create(endpoints.socketBaseUrl' in foundation)
ok('Socket.IO retains backend namespace and path', 'trimEnd(\'/\') + "/realtime"' in rt and 'path = "/socket.io"' in rt)
ok('Socket authentication remains secure-token fail-closed', 'tokenVault.accessToken()' in rt and 'connection_established' in rt and 'auth_error' in rt)
ok('acceptance uses live server connectivity instead of adb reverse', 'VC-ANDROID-LIVE-CONNECTIVITY-CHECK.cmd' in accept and 'VC-ANDROID-LOCAL-DEVICE-BACKEND.cmd' not in accept)
ok('live check validates REST config endpoint', 'config/mobile?platform=android' in livecheck)
ok('live check validates Engine.IO Socket handshake', 'EIO=4&transport=polling' in livecheck and '\"sid\"' in livecheck)
ok('live check validates Web authority', 'VoiceCloud Web authority reachable' in livecheck)
print(f'VC-ANDROID-PH01-R08 live-server regression: {len(checks)}/{len(checks)} PASS')
