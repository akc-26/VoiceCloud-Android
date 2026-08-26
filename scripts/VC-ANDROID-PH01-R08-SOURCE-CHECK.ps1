$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
$settings=T 'settings.gradle.kts'
@(':app',':core:model',':core:designsystem',':core:network',':core:security',':core:database',':core:preferences',':core:logging',':core:realtime',':feature:bootstrap')|%{P "module $_ registered" $settings.Contains("include(`"$_`")")}
$app=T 'app/build.gradle.kts'; $props=T 'gradle.properties'; $resolver=T 'app/src/main/java/app/voicecloud/android/network/ApiEndpointResolver.kt'; $test=T 'app/src/test/java/app/voicecloud/android/network/ApiEndpointResolverTest.kt'
P 'debug staging release variants retained' ($app.Contains('debug {') -and $app.Contains('create("staging")') -and $app.Contains('release {'))
P 'compileSdk 37 locked' $app.Contains('compileSdk = 37'); P 'targetSdk 36 locked' $app.Contains('targetSdk = 36'); P 'minSdk 26 locked' $app.Contains('minSdk = 26')
$live='https://voicecloud.tailfca77b.ts.net'
@('VOICECLOUD_DEBUG_API_BASE_URL','VOICECLOUD_DEBUG_SOCKET_BASE_URL','VOICECLOUD_DEBUG_WEB_BASE_URL')|%{P "$_ uses Raspberry Pi live server" ($props.Contains("$_=$live") -and $app.Contains($_))}
P 'API root normalizes to /api/v1/' ($resolver.Contains('normalizeApiBaseUrl') -and $resolver.Contains('endsWith("/api/v1"'))
P 'runtime no longer depends on emulator loopback' (-not $resolver.Contains('10.0.2.2') -and -not $resolver.Contains('127.0.0.1') -and -not $resolver.Contains('android.os.Build'))
P 'debug network policy is HTTPS-only' (T 'app/src/debug/res/xml/network_security_config.xml').Contains('cleartextTrafficPermitted="false"')
P 'endpoint tests match R08 resolver contract' ($test.Contains('liveServerRootExpandsToVersionedApiBase') -and $test.Contains('fullApiBaseIsNotDuplicated') -and $test.Contains('socketUsesServerOriginAndWebUsesTrailingSlash'))
P 'obsolete R07 endpoint test symbols removed' (-not $test.Contains('EmulatorDebugBaseUrl') -and -not $test.Contains('PhysicalDeviceDebugBaseUrl'))
$foundation=T 'app/src/main/java/app/voicecloud/android/di/FoundationModule.kt'; P 'resolved REST and Socket endpoints injected' ($foundation.Contains('ApiClientFactory.create(endpoints.apiBaseUrl') -and $foundation.Contains('RealtimeClientFactory.create(endpoints.socketBaseUrl'))
$rt=T 'core/realtime/src/main/java/app/voicecloud/core/realtime/RealtimeClient.kt'; P 'Socket.IO backend authority retained' ($rt.Contains('trimEnd(''/'') + "/realtime"') -and $rt.Contains('path = "/socket.io"') -and $rt.Contains('connection_established'))
$accept=T 'scripts/VC-ANDROID-PH01-R08-ACCEPTANCE.cmd'; P 'acceptance checks live server without adb reverse' ($accept.Contains('VC-ANDROID-LIVE-CONNECTIVITY-CHECK.cmd') -and -not $accept.Contains('VC-ANDROID-LOCAL-DEVICE-BACKEND.cmd'))
P 'Keystore AES-GCM retained' (T 'core/security/src/main/java/app/voicecloud/core/security/AndroidKeyStoreTokenVault.kt').Contains('AES/GCM/NoPadding')
P 'Light-first User theme retained' (T 'app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt').Contains('darkTheme = false')
Write-Host "VC-ANDROID-PH01-R08 Windows source authority: $script:count/$script:count PASS"
