from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def text(p): return (ROOT/p).read_text(encoding='utf-8')
def ok(name, cond):
    if not cond: raise SystemExit(f'[FAIL] {name}')
    print(f'[PASS] {name}'); checks.append(name)
app=text('app/build.gradle.kts'); settings=text('settings.gradle.kts'); resolver=text('app/src/main/java/app/voicecloud/android/network/ApiEndpointResolver.kt'); foundation=text('app/src/main/java/app/voicecloud/android/di/FoundationModule.kt'); rt=text('core/realtime/src/main/java/app/voicecloud/core/realtime/RealtimeClient.kt'); helper=text('scripts/VC-ANDROID-LOCAL-DEVICE-BACKEND.cmd'); props=text('gradle.properties'); catalog=text('gradle/libs.versions.toml')
for key in ['VOICECLOUD_DEBUG_API_BASE_URL','VOICECLOUD_DEBUG_SOCKET_BASE_URL','VOICECLOUD_DEBUG_WEB_BASE_URL']:
    ok(f'Android build reads {key}', key in app)
    ok(f'gradle.properties documents {key}', key in props)
ok('BuildConfig exposes API/SOCKET/WEB endpoints', all(x in app for x in ['"API_BASE_URL"','"SOCKET_BASE_URL"','"WEB_BASE_URL"']))
ok('physical-device API default uses adb reverse localhost', 'PhysicalDeviceDebugApiBaseUrl = "http://127.0.0.1:3000/api/v1/"' in resolver)
ok('physical-device Socket default uses adb reverse localhost', 'PhysicalDeviceDebugSocketBaseUrl = "http://127.0.0.1:3000"' in resolver)
ok('physical-device Web default uses adb reverse localhost', 'PhysicalDeviceDebugWebBaseUrl = "http://127.0.0.1:3000/"' in resolver)
ok('core:realtime module registered', 'include(":core:realtime")' in settings)
ok('official Socket.IO Java 2.x client pinned', 'socketIoJava = "2.1.2"' in catalog)
ok('Realtime client uses /realtime namespace', 'trimEnd(\'/\') + "/realtime"' in rt)
ok('Realtime client uses /socket.io path', 'path = "/socket.io"' in rt)
ok('Realtime auth uses secure token authority', 'tokenVault.accessToken()' in rt and 'auth = mapOf("token" to "Bearer $token")' in rt)
ok('Realtime waits for backend connection_established authority', 'active.on("connection_established")' in rt)
ok('Realtime handles backend auth_error authority', 'active.on("auth_error")' in rt)
ok('Foundation injects resolved REST endpoint', 'ApiClientFactory.create(endpoints.apiBaseUrl' in foundation)
ok('Foundation injects resolved Socket endpoint', 'RealtimeClientFactory.create(endpoints.socketBaseUrl' in foundation)
ok('ADB reverse maps shared API/Socket/Web port 3000', 'reverse tcp:3000 tcp:3000' in helper)
ok('Local helper reports clearly when backend is not running', 'VoiceCloud backend is not currently reachable on this PC' in helper)
print(f'VC-ANDROID-PH01-R07 endpoint/realtime regression: {len(checks)}/{len(checks)} PASS')
