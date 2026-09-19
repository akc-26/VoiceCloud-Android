$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
function Text($path) { Get-Content (Join-Path $root $path) -Raw }
function P($name, $condition) { if (-not $condition) { Write-Host "[FAIL] $name"; exit 1 }; Write-Host "[PASS] $name"; $script:passed++ }
$script:passed = 0
$rootUi = Text 'app\src\main\java\app\voicecloud\android\ui\VoiceCloudRoot.kt'
$topbar = Text 'core\designsystem\src\main\java\app\voicecloud\core\designsystem\component\VoiceCloudPageTopBar.kt'
$colors = Text 'core\designsystem\src\main\java\app\voicecloud\core\designsystem\theme\VoiceCloudColors.kt'
$theme = Text 'core\designsystem\src\main\java\app\voicecloud\core\designsystem\theme\VoiceCloudTheme.kt'
$discovery = Text 'feature\discovery\src\main\java\app\voicecloud\feature\discovery\ui\DiscoveryScreens.kt'
$engagement = Text 'feature\engagement\src\main\java\app\voicecloud\feature\engagement\ui\EngagementScreens.kt'
$accept = Text 'scripts\VC-ANDROID-PH04-R04-ACCEPTANCE.cmd'
$appBuild = Text 'app\build.gradle.kts'
$combinedUi = $discovery + $engagement
P 'global safe drawing inset protection retained' $rootUi.Contains('.safeDrawingPadding()')
P 'app declares Compose Foundation for safe-area APIs' $appBuild.Contains('implementation(libs.androidx.compose.foundation)')
P 'shared secondary top bar retained' $topbar.Contains('fun VoiceCloudPageTopBar(')
P 'icon-only back control retained' ($topbar.Contains('IconButton(') -and $topbar.Contains('Canvas(Modifier.size(22.dp))'))
P 'centered single-line page title retained' ($topbar.Contains('textAlign = TextAlign.Center') -and $topbar.Contains('maxLines = 1'))
P 'legacy visible Back text removed' (-not $combinedUi.Contains('Text("Back"') -and -not $combinedUi.Contains('Text("‹ Back"'))
P 'discovery secondary pages use shared layout' ($discovery.Contains('fun PublicProfileScreen(') -and $discovery.Contains('SecondaryPageLayout(title = "Profile"'))
P 'engagement secondary pages use shared layout' ($engagement.Contains('title = "Communities"') -and $engagement.Contains('title = "Messages"') -and $engagement.Contains('title = "Notifications"'))
P 'Home shortcut labels cannot wrap' ($discovery.Contains('private fun RowScope.HomeShortcut(') -and $discovery.Contains('maxLines = 1, softWrap = false'))
P 'PH04 quick action labels cannot wrap' ($engagement.Contains('private fun RowScope.QuickAction(') -and $engagement.Contains('maxLines = 1, softWrap = false'))
P 'Website primary gradient retained' ($colors.Contains('0xFF087E8A') -and $colors.Contains('0xFF075F70'))
P 'Website hero gradient retained' ($colors.Contains('0xFFF5FCFD') -and $colors.Contains('0xFFECF9FA') -and $colors.Contains('0xFFDEF5F7'))
P 'Material surface container roles are branded' ($theme.Contains('surfaceContainerLow') -and $theme.Contains('surfaceContainerHigh') -and $theme.Contains('surfaceContainerHighest'))
P 'R02 RowScope compile correction retained' ($engagement.Contains('private fun RowScope.QuickAction(') -and -not $engagement.Contains('private fun QuickAction('))
P 'R04 acceptance runs UI regression' $accept.Contains('ph04_r04_ui_quality_regression.py')
P 'R04 acceptance retains healthy device gate' $accept.Contains('VC-ANDROID-DEVICE-INSTRUMENTATION.ps1')
P 'R04 acceptance retains Debug Staging Release compile gates' ($accept.Contains(':app:compileDebugKotlin') -and $accept.Contains(':app:compileStagingKotlin') -and $accept.Contains(':app:compileReleaseKotlin'))
Write-Host "VC-ANDROID-PH04-R04 Windows UI authority: $passed/17 PASS"
