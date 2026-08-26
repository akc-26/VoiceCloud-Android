$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }

# Preserve all earlier PH02 corrections first.
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'VC-ANDROID-PH02-R02-SOURCE-CHECK.ps1')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$prefs=T 'core/preferences/src/main/java/app/voicecloud/core/preferences/VoiceCloudPreferences.kt'
$prefsBuild=T 'core/preferences/build.gradle.kts'
$authUi=T 'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt'
$authBuild=T 'feature/auth/build.gradle.kts'
$appBuild=T 'app/build.gradle.kts'
$appNav=T 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'
$bootstrapBuild=T 'feature/bootstrap/build.gradle.kts'
$bootstrapRoute=T 'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt'
$realtimeBuild=T 'core/realtime/build.gradle.kts'
$databaseBuild=T 'core/database/build.gradle.kts'
$catalog=T 'gradle/libs.versions.toml'
$rootBuild=T 'build.gradle.kts'
$accept=T 'scripts/VC-ANDROID-PH02-R03-ACCEPTANCE.cmd'

P 'DataStore mutation return-type leak removed' (-not $prefs.Contains('= context.voiceCloudDataStore.edit'))
P 'DataStore dependency remains encapsulated' ($prefsBuild.Contains('implementation(libs.androidx.datastore.preferences)'))
P 'scope-only TextAction align removed' (-not $authUi.Contains('Modifier.align(Alignment.CenterHorizontally)'))
P 'feature auth declares Activity Compose directly' $authBuild.Contains('implementation(libs.androidx.activity.compose)')
P 'feature auth exports Retrofit public ABI' $authBuild.Contains('api(libs.retrofit.core)')
P 'feature auth exports coroutine public ABI' $authBuild.Contains('api(libs.kotlinx.coroutines.android)')
P 'app declares lifecycle runtime compose directly' $appBuild.Contains('implementation(libs.androidx.lifecycle.runtime.compose)')
P 'realtime exports security public ABI' $realtimeBuild.Contains('api(project(":core:security"))')
P 'realtime exports coroutine public ABI' $realtimeBuild.Contains('api(libs.kotlinx.coroutines.android)')
P 'new Hilt lifecycle compose artifact retained' $catalog.Contains('androidx-hilt-lifecycle-viewmodel-compose')
P 'deprecated Hilt ViewModel import removed from app' (-not $appNav.Contains('androidx.hilt.navigation.compose.hiltViewModel'))
P 'deprecated Hilt ViewModel import removed from bootstrap' (-not $bootstrapRoute.Contains('androidx.hilt.navigation.compose.hiltViewModel'))
P 'Room plugin retained' ($databaseBuild.Contains('alias(libs.plugins.room)') -and $rootBuild.Contains('alias(libs.plugins.room) apply false'))
P 'Room schema export directory retained' $databaseBuild.Contains('schemaDirectory("$projectDir/schemas")')
P 'compile matrix uses continue mode' ($accept.Contains(':app:compileDebugKotlin :app:compileStagingKotlin :app:compileReleaseKotlin --continue'))
Write-Host "VC-ANDROID-PH02-R03 Windows corrective authority: $script:count/$script:count PASS"
