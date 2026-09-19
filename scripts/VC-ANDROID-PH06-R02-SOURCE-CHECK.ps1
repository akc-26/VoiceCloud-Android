$ErrorActionPreference='Stop'
$root=(Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
$auth=T 'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt'
$disc=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt'
$eng=T 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt'
$nav=T 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'
$vm=T 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementViewModel.kt'
$live=T 'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt'
$ph05=T 'scripts/VC-ANDROID-PH05-R01-SOURCE-CHECK.ps1'
$app=T 'app/build.gradle.kts'
P 'PH05 inherited PowerShell null-variable defect corrected' ($ph05.Contains('$screen.Contains') -and (-not $ph05.Contains('$screens.Contains')))
P 'PH06 phase version marker retained' $app.Contains('versionName = "1.0.0-ph06"')
P 'premium User Portal selector retained' ($auth.Contains('"User Portal"') -and $auth.Contains('R.drawable.vc_portal_user'))
P 'premium Creator Portal selector retained' ($auth.Contains('"Creator Portal"') -and $auth.Contains('R.drawable.vc_portal_creator'))
P 'password visibility control retained' ($auth.Contains('passwordVisible') -and $auth.Contains('R.drawable.vc_icon_visibility'))
P 'Signup label retained' $auth.Contains('TextAction("Signup", onRegister)')
P 'User login directly switches to Creator Portal' $nav.Contains('onBack = { open(VoiceCloudRoutes.CreatorSignIn) }')
P 'Creator login directly switches to User Portal' $nav.Contains('onBack = { open(VoiceCloudRoutes.UserSignIn) }')
P 'Home action icons retained' ($disc.Contains('vc_icon_community') -and $disc.Contains('vc_icon_message') -and $disc.Contains('vc_icon_bell'))
P 'Communities top search action retained' ($eng.Contains('Search communities') -and $eng.Contains('vc_icon_search'))
P 'Communities top events action retained' ($eng.Contains('Upcoming events') -and $eng.Contains('vc_icon_calendar'))
P 'Community create back stack corrected' $nav.Contains('popUpTo(VoiceCloudRoutes.CommunityCreate) { inclusive = true }')
P 'Unified search tabs retained' ($disc.Contains('"People", "Creators", "Rooms", "Communities"') -and $disc.Contains('search-community:'))
P 'Community search route retained' ($nav.Contains('const val CommunitySearch = "search/communities"') -and $nav.Contains('initialTab = "Communities"'))
P 'Messages search is icon-triggered' ($eng.Contains('searchVisible') -and $eng.Contains('Search conversations'))
P 'Messages delete confirmation retained' ($eng.Contains('AlertDialog(') -and $eng.Contains('Delete conversation?'))
P 'Messages red delete icon retained' ($eng.Contains('vc_icon_delete') -and $eng.Contains('MaterialTheme.colorScheme.error'))
P 'Messages multi-select retained' ($eng.Contains('selectionMode') -and $eng.Contains('Checkbox('))
P 'Messages multi-delete ViewModel retained' $vm.Contains('fun deleteConversations(ids: Set<String>)')
P 'Verbose live access paragraph removed' (-not $live.Contains('Room access is checked securely when you join'))
P 'Adaptive page padding retained' ($disc.Contains('adaptivePagePadding()') -and $eng.Contains('adaptivePagePadding()'))
P 'Telegram-style tab animation retained' ($nav.Contains('slideInHorizontally') -and $nav.Contains('slideOutHorizontally'))
Write-Host "VC-ANDROID-PH06-R02 Windows corrective authority: $script:count/$script:count PASS"
