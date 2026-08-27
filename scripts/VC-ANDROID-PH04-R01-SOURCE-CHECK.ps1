$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
$settings=T 'settings.gradle.kts'; $app=T 'app/build.gradle.kts'; $api=T 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/EngagementApi.kt'
$repo=T 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/EngagementRepository.kt'; $screens=T 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt'
$monitor=T 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/NotificationRealtimeMonitor.kt'; $push=T 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/PushNotificationCoordinator.kt'
$manifest=T 'app/src/main/AndroidManifest.xml'; $application=T 'app/src/main/java/app/voicecloud/android/VoiceCloudApplication.kt'; $nav=T 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'
P 'PH04 engagement module registered' $settings.Contains('include(":feature:engagement")')
P 'PH04 version marker retained' $app.Contains('versionName = "1.0.0-ph04"')
foreach($endpoint in @('clubs/{id}/join','clubs/{id}/invite-code','scheduled-rooms/{id}/reminder','chat/conversations/{id}/messages','chat/conversations/{id}/read','notifications/unread-count','notifications/read-all')){P "endpoint $endpoint retained" $api.Contains($endpoint)}
P 'community roles retained' $repo.Contains('setOf("OWNER", "ADMIN", "MODERATOR", "MEMBER")')
P 'consumer identity filtering retained' $repo.Contains('role in setOf("USER", "CREATOR")')
P 'five-second message refresh retained' $screens.Contains('delay(5_000)')
P 'notification realtime new event retained' $monitor.Contains('notification:new')
P 'notification realtime read event retained' $monitor.Contains('notification:read')
P 'notification realtime deleted event retained' $monitor.Contains('notification:deleted')
P 'FCM service registered' ($manifest.Contains('VoiceCloudFirebaseMessagingService') -and $manifest.Contains('com.google.firebase.MESSAGING_EVENT'))
P 'POST_NOTIFICATIONS retained' $manifest.Contains('android.permission.POST_NOTIFICATIONS')
P 'centralized Firebase default app authority retained' ($application.Contains('BuildConfig.FIREBASE_API_KEY') -and $application.Contains('FirebaseApp.DEFAULT_APP_NAME'))
P 'FCM token sync fails closed without Firebase config' $push.Contains('getOrNull() ?: return')
P 'PH03 Home PH04 navigation retained' ($nav.Contains('VoiceCloudRoutes.Communities') -and $nav.Contains('VoiceCloudRoutes.Messages') -and $nav.Contains('VoiceCloudRoutes.Notifications'))
P 'PH05 RTC module not pulled forward' (-not $settings.Contains('include(":feature:rtc")'))
P 'PH03 lazy-list regression retained' (Test-Path (Join-Path $root 'scripts/ph03_r02_lazy_list_key_regression.py'))
Write-Host "VC-ANDROID-PH04-R01 Windows source authority: $script:count/$script:count PASS"
