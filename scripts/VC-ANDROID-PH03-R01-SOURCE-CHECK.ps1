$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
$settings=T 'settings.gradle.kts'; $app=T 'app/build.gradle.kts'; $api=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryApi.kt'; $nav=T 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'; $policy=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/ConsumerIdentityPolicy.kt'; $screens=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt'
P 'PH03 discovery module registered' $settings.Contains('include(":feature:discovery")')
P 'app consumes PH03 discovery module' $app.Contains('implementation(project(":feature:discovery"))')
P 'PH03 version marker retained' $app.Contains('versionName = "1.0.0-ph03"')
@('discovery/rooms/live','discovery/users/trending','discovery/users/suggested','search','users/public/{username}','users/profile/me','users/{userId}/follow','users/followers','users/following','users/friends','users/friends/requests/pending','users/friends/suggested')|%{P "endpoint $_ retained" $api.Contains($_)}
P 'consumer roles restricted to USER CREATOR' $policy.Contains('setOf("USER", "CREATOR")')
P 'guest identities excluded' $policy.Contains('if (user.isGuest) return false')
P 'self exclusion by id retained' $policy.Contains('user.id == viewer.id')
P 'self exclusion by username retained' $policy.Contains('user.username.trim().lowercase() == viewerName')
@('Home','Explore','Rooms','People','Creators','Search','MyProfile','Followers','Following','Friends','PublicProfile')|%{P "route $_ retained" $nav.Contains("const val $_")}
P 'human-readable username profile route retained' ($nav.Contains('fun profile(username: String)') -and $nav.Contains('Uri.encode(username.trim())'))
P 'PH02 user-ready hands off to PH03 Home' ($nav.Contains('popUpTo(VoiceCloudRoutes.UserReady)') -and $nav.Contains('VoiceCloudRoutes.Home'))
P 'PH04+ feature modules not pulled into PH03' (-not ($settings -match ':feature:(communities|messages|notifications|rtc|wallet)'))
P 'PH03 consumer presentation retained' ($screens.Contains('ConsumerColors.Sapphire') -and $screens.Contains('ConsumerColors.Indigo'))
Write-Host "VC-ANDROID-PH03-R01 Windows source authority: $script:count/$script:count PASS"
