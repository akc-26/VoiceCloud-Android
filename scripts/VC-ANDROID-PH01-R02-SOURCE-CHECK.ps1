$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count = 0
function Read-Text([string]$rel) { return [IO.File]::ReadAllText((Join-Path $root $rel)) }
function Pass([string]$label, [bool]$condition) {
    if (-not $condition) { Write-Host "[FAIL] $label"; exit 1 }
    $script:count++; Write-Host "[PASS] $label"
}
$settings = Read-Text 'settings.gradle.kts'
@(':app',':core:model',':core:designsystem',':core:network',':core:security',':core:database',':core:preferences',':core:logging',':feature:bootstrap') | ForEach-Object { Pass "module $_ registered" ($settings.Contains("include(`"$_`")")) }
$app = Read-Text 'app/build.gradle.kts'
Pass 'debug staging release variants exist' ($app.Contains('debug {') -and $app.Contains('create("staging")') -and $app.Contains('release {'))
Pass 'compileSdk 37 locked' $app.Contains('compileSdk = 37')
Pass 'targetSdk 36 locked' $app.Contains('targetSdk = 36')
Pass 'minSdk 26 locked' $app.Contains('minSdk = 26')
Pass 'API base URL is build-config controlled' ($app.Contains('VC_API_BASE_URL') -and (Read-Text 'app/src/main/java/app/voicecloud/android/di/FoundationModule.kt').Contains('BuildConfig.API_BASE_URL'))
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
Write-Host "VC-ANDROID-PH01-R02 source authority: $script:count/$script:count PASS"
