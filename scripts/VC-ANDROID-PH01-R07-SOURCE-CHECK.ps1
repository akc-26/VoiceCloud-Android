$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count = 0
function Read-Text([string]$rel) { return [IO.File]::ReadAllText((Join-Path $root $rel)) }
function Pass([string]$label, [bool]$condition) {
    if (-not $condition) { Write-Host "[FAIL] $label"; exit 1 }
    $script:count++; Write-Host "[PASS] $label"
}
$settings = Read-Text 'settings.gradle.kts'
@(':app',':core:model',':core:designsystem',':core:network',':core:security',':core:database',':core:preferences',':core:logging',':core:realtime',':feature:bootstrap') | ForEach-Object { Pass "module $_ registered" ($settings.Contains("include(`"$_`")")) }
$app = Read-Text 'app/build.gradle.kts'
$rootBuild = Read-Text 'build.gradle.kts'
$catalog = Read-Text 'gradle/libs.versions.toml'
$androidBuildFiles = @(Get-ChildItem $root -Recurse -Filter 'build.gradle.kts' | Where-Object { $_.FullName -ne (Join-Path $root 'build.gradle.kts') })
$allAndroidBuildText = ($androidBuildFiles | ForEach-Object { [IO.File]::ReadAllText($_.FullName) }) -join "`n"
Pass 'AGP 9 built-in Kotlin removes kotlin-android plugin' (-not $rootBuild.Contains('libs.plugins.kotlin.android') -and -not $catalog.Contains('org.jetbrains.kotlin.android') -and -not $allAndroidBuildText.Contains('libs.plugins.kotlin.android'))
Pass 'AGP 9 built-in Kotlin removes legacy android.kotlinOptions DSL' (-not $allAndroidBuildText.Contains('kotlinOptions'))
Pass 'debug staging release variants exist' ($app.Contains('debug {') -and $app.Contains('create("staging")') -and $app.Contains('release {'))
Pass 'compileSdk 37 locked' $app.Contains('compileSdk = 37')
Pass 'targetSdk 36 locked' $app.Contains('targetSdk = 36')
Pass 'minSdk 26 locked' $app.Contains('minSdk = 26')
Pass 'API base URL is build-config controlled' ($app.Contains('VOICECLOUD_DEBUG_API_BASE_URL') -and (Read-Text 'app/src/main/java/app/voicecloud/android/di/FoundationModule.kt').Contains('BuildConfig.API_BASE_URL'))
$colors = Read-Text 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudColors.kt'
@('0xFF0B7C86','0xFF075762','0xFF22C7CF','0xFF22C55E','0xFF123A32') | ForEach-Object { Pass "R06 design token $_ retained" $colors.Contains($_) }
Pass 'fresh User launch is Light-first' (Read-Text 'app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt').Contains('darkTheme = false')
$nav = Read-Text 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'
Pass 'Navigation Compose bootstrap graph is present' ($nav.Contains('NavHost') -and $nav.Contains('UserPortal') -and $nav.Contains('CreatorPortal'))
$vault = Read-Text 'core/security/src/main/java/app/voicecloud/core/security/AndroidKeyStoreTokenVault.kt'
Pass 'Keystore AES-GCM token vault present' ($vault.Contains('AndroidKeyStore') -and $vault.Contains('AES/GCM/NoPadding') -and $vault.Contains('GCMParameterSpec'))
$interceptor = Read-Text 'core/network/src/main/java/app/voicecloud/core/network/AuthTokenInterceptor.kt'
Pass 'HTTP foundation reads only real secure token authority' ($interceptor.Contains('tokenVault.accessToken()') -and $interceptor.Contains('Bearer $token'))
Pass 'bootstrap uses authoritative mobile config' (Read-Text 'core/network/src/main/java/app/voicecloud/core/network/BootstrapApi.kt').Contains('@GET("config/mobile")')
$route = Read-Text 'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt'
Pass 'bootstrap surfaces Loading state' ($route.Contains('BootstrapState.Loading'))
Pass 'bootstrap surfaces Maintenance state' ($route.Contains('BootstrapState.Maintenance'))
Pass 'bootstrap surfaces ForceUpdate state' ($route.Contains('BootstrapState.ForceUpdate'))
Pass 'bootstrap surfaces Ready state' ($route.Contains('BootstrapState.Ready'))
Pass 'bootstrap surfaces Error state' ($route.Contains('BootstrapState.Error'))
$vm = Read-Text 'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapViewModel.kt'
Pass 'version policy uses actual Android BuildConfig identity' ($vm.Contains('appIdentity.versionName') -and -not $vm.Contains('BuildVersion'))
$prefs = Read-Text 'core/preferences/src/main/java/app/voicecloud/core/preferences/VoiceCloudPreferences.kt'
Pass 'DataStore appearance foundation defaults to Light' ($prefs.Contains('ThemePreference.LIGHT') -and $prefs.Contains('preferencesDataStore'))
$logger = Read-Text 'core/logging/src/main/java/app/voicecloud/core/logging/VoiceCloudLogger.kt'
Pass 'logging foundation redacts credentials' ($logger.Contains('[REDACTED]') -and $logger.Contains('Bearer') -and $logger.Contains('password'))
Pass 'logging regex is Kotlin-compile-safe' ($logger.Contains('Regex("""(?i)Bearer\s+') -and $logger.Contains('([^\s&]+)'))
$sdkCmd = Read-Text 'scripts/VC-ANDROID-SDK-ENV.cmd'
Pass 'Windows CLI acceptance auto-detects Android Studio SDK' ($sdkCmd.Contains('%LOCALAPPDATA%\Android\Sdk') -and $sdkCmd.Contains('ANDROID_HOME'))
$api = Get-Content (Join-Path $root 'contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json') -Raw | ConvertFrom-Json
Pass 'API contract is finalized A0-ready backend authority' ($api.authority.commit -eq '7bee8d463786d21a7200b5663a499408bb229115' -and $api.authority.androidReadinessRevision -eq 'VC-ANDROID-A0-R01' -and $api.backendOperations -eq 700)
Pass 'Gradle wrapper authority is pinned to 9.5.0' (Read-Text 'gradle/wrapper/gradle-wrapper.properties').Contains('gradle-9.5.0-bin.zip')
$sock = Get-Content (Join-Path $root 'contracts/realtime/VC-ANDROID-R06-SOCKET-EVENTS-R01.json') -Raw | ConvertFrom-Json
Pass 'Socket event manifest has authoritative events' ($sock.inboundSubscribeMessages.Count -ge 50)
$screens = Get-Content (Join-Path $root 'contracts/screens/VC-ANDROID-R06-SCREEN-PARITY-R01.json') -Raw | ConvertFrom-Json
Pass 'screen parity locks 74 Website pages' ($screens.counts.websitePageSources -eq 74)
Pass 'screen parity locks 22 Creator pages' ($screens.counts.creatorPageSources -eq 22)
Pass 'screen parity locks 84 Website designs' ($screens.counts.approvedWebsiteDesigns -eq 84)
$business = @(Get-ChildItem (Join-Path $root 'feature') -Directory | Where-Object { $_.Name -like 'consumer*' -or $_.Name -like 'creator*' })
Pass 'PH01 contains no consumer/creator business feature module' ($business.Count -eq 0)
$networkBuild = Read-Text 'core/network/build.gradle.kts'
Pass 'Retrofit public ABI exported from core:network' ($networkBuild.Contains('api(libs.retrofit.core)'))
Pass 'network model/security public ABI exported' ($networkBuild.Contains('api(project(":core:model"))') -and $networkBuild.Contains('api(project(":core:security"))'))
$databaseBuild = Read-Text 'core/database/build.gradle.kts'
Pass 'Room and Flow public ABI exported from core:database' ($databaseBuild.Contains('api(libs.androidx.room.runtime)') -and $databaseBuild.Contains('api(libs.kotlinx.coroutines.android)'))
$prefsBuild = Read-Text 'core/preferences/build.gradle.kts'
Pass 'Flow public ABI exported from core:preferences' ($prefsBuild.Contains('api(libs.kotlinx.coroutines.android)'))
$javaCmd = Read-Text 'scripts/VC-ANDROID-JAVA-ENV.cmd'
Pass 'Windows CLI acceptance auto-detects Android Studio JBR' ($javaCmd.Contains('%ProgramFiles%\Android\Android Studio\jbr') -and $javaCmd.Contains('set "JAVA_HOME="'))
$resolver = Read-Text 'app/src/main/java/app/voicecloud/android/network/ApiEndpointResolver.kt'
Pass 'real-device debug API resolves through adb reverse loopback' ($resolver.Contains('PhysicalDeviceDebugApiBaseUrl = "http://127.0.0.1:3000/api/v1/"') -and $resolver.Contains('PhysicalDeviceDebugSocketBaseUrl = "http://127.0.0.1:3000"'))
Pass 'emulator debug API retains 10.0.2.2 host alias' ($resolver.Contains('EmulatorDebugApiBaseUrl = "http://10.0.2.2:3000/api/v1/"') -and $resolver.Contains('EmulatorDebugSocketBaseUrl = "http://10.0.2.2:3000"'))
$foundation = Read-Text 'app/src/main/java/app/voicecloud/android/di/FoundationModule.kt'
Pass 'runtime endpoint resolver feeds REST and Socket authority' ($foundation.Contains('BuildConfig.API_BASE_URL') -and $foundation.Contains('BuildConfig.SOCKET_BASE_URL') -and $foundation.Contains('RealtimeClientFactory.create(endpoints.socketBaseUrl'))
$debugSecurity = Read-Text 'app/src/debug/res/xml/network_security_config.xml'
$mainSecurity = Read-Text 'app/src/main/res/xml/network_security_config.xml'
Pass 'local cleartext is debug-only' ($debugSecurity.Contains('cleartextTrafficPermitted="true"') -and -not $mainSecurity.Contains('cleartextTrafficPermitted="true"'))
$deviceScript = Read-Text 'scripts/VC-ANDROID-LOCAL-DEVICE-BACKEND.cmd'
Pass 'physical-device helper establishes adb reverse tunnel' ($deviceScript.Contains('reverse tcp:3000 tcp:3000') -and $deviceScript.Contains('reverse --list'))
$realtime = Read-Text 'core/realtime/src/main/java/app/voicecloud/core/realtime/RealtimeClient.kt'
Pass 'Socket.IO foundation matches finalized backend namespace/path' ($realtime.Contains('trimEnd(''/'') + "/realtime"') -and $realtime.Contains('path = "/socket.io"'))
Pass 'Socket.IO authentication is fail-closed on secure token' ($realtime.Contains('tokenVault.accessToken()') -and $realtime.Contains('auth = mapOf("token" to "Bearer $token")') -and $realtime.Contains('active.on("connection_established")') -and $realtime.Contains('active.on("auth_error")'))
@('VOICECLOUD_DEBUG_API_BASE_URL','VOICECLOUD_DEBUG_SOCKET_BASE_URL','VOICECLOUD_DEBUG_WEB_BASE_URL') | ForEach-Object { Pass "$_ is an Android build authority" $app.Contains($_) }
Write-Host "VC-ANDROID-PH01-R07 source authority: $script:count/$script:count PASS"
