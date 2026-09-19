$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
$settings=T 'settings.gradle.kts'; $app=T 'app/build.gradle.kts'; $api=T 'feature/live/src/main/java/app/voicecloud/feature/live/data/LiveRoomApi.kt'
$vm=T 'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomViewModel.kt'; $rtc=T 'feature/live/src/main/java/app/voicecloud/feature/live/rtc/RtcAudioEngine.kt'
$screen=T 'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt'; $rt=T 'feature/live/src/main/java/app/voicecloud/feature/live/data/LiveRoomRealtimeMonitor.kt'
$nav=T 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'; $manifest=T 'app/src/main/AndroidManifest.xml'; $activity=T 'app/src/main/java/app/voicecloud/android/MainActivity.kt'
$style=T 'app/src/main/res/values/styles.xml'; $discovery=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt'; $brand=T 'branding/voicecloud-brand.properties'
P 'PH05 live module registered' $settings.Contains('include(":feature:live")')
P 'app consumes PH05 live module' $app.Contains('implementation(project(":feature:live"))')
P 'PH05 or later version marker present' $app.Contains('versionName = "1.0.0-ph0')
foreach($endpoint in @('rtc/rooms/join','rtc/rooms/rejoin','rtc/rooms/leave','rtc/rooms/{roomId}/participants','rtc/rooms/{roomId}/raise-hand','chat/conversations/{conversationId}/messages','rooms/saved/{roomId}','room-activity/{roomId}/join','room-activity/{roomId}/leave','gifts/catalog','gifts/send')){P "endpoint $endpoint retained" $api.Contains($endpoint)}
P 'listener auto subscribes' $rtc.Contains('autoSubscribe = true')
P 'listener microphone capture disabled' $rtc.Contains('audio = false')
P 'listener video capture disabled' $rtc.Contains('video = false')
P 'PH05 listener UI does not request microphone permission' ((-not $screen.Contains('RequestPermission')) -and (-not $screen.Contains('RECORD_AUDIO')))
P 'room preview route retained' $nav.Contains('rooms/{roomId}/preview')
P 'live room route retained' $nav.Contains('rooms/{roomId}/live')
P 'join lifecycle generation retained' ($vm.Contains('sessionGeneration') -and $vm.Contains('joinJob') -and $vm.Contains('rejoinJob'))
P 'leave invalidates and cancels session' ($vm.Contains('val invalidatedGeneration = invalidateSession()') -and $vm.Contains('cancelSessionJobs()'))
P 'engine connection generation retained' ($rtc.Contains('AtomicLong') -and $rtc.Contains('beginConnectionOperation()'))
P 'engine stale operation cancellation retained' $rtc.Contains('Audio connection superseded')
P 'five-second room chat refresh retained' $vm.Contains('delay(5_000)')
P 'room presence events retained' ($rt.Contains('participant_joined') -and $rt.Contains('participant_left') -and $rt.Contains('presence_updated'))
P 'raise hand events retained' ($rt.Contains('hand_approved') -and $rt.Contains('hand_rejected'))
P 'speaker invitation actions retained' ($rt.Contains('stage:accept_invitation') -and $rt.Contains('stage:reject_invitation'))
P 'room pause/resume/end events retained' ($rt.Contains('room.paused') -and $rt.Contains('room.resumed') -and $rt.Contains('room.ended'))
P 'live lazy lists use collision-safe keys' ($screen.Contains('live-person:${participant.userId}:$index') -and $screen.Contains('live-chat:${message.id}:$index') -and $screen.Contains('live-gift:${gift.id}:$index'))
foreach($icon in @('home','explore','search','friends','profile')){P "real tab icon $icon retained" ($discovery.Contains("R.drawable.vc_nav_$icon") -and (Test-Path (Join-Path $root "core/designsystem/src/main/res/drawable/vc_nav_$icon.xml")))}
P 'tab bar uses professional vector Icon painters' ($discovery.Contains('vc_nav_home_selected') -and $discovery.Contains('painterResource(if (isSelected)'))
P 'Android SplashScreen API installed' ($activity.Contains('installSplashScreen()') -and $style.Contains('Theme.SplashScreen'))
P 'central temporary splash asset retained' (Test-Path (Join-Path $root 'branding/res/drawable/vc_brand_splash.xml'))
P 'central brand app icon retained' (Test-Path (Join-Path $root 'branding/res/drawable/vc_brand_app_icon_foreground.xml'))
P 'white-label brand identity authority retained' ($brand.Contains('brand.name=') -and $brand.Contains('brand.slug=') -and $brand.Contains('brand.applicationId='))
P 'white-label consumer color authority retained' ($brand.Contains('consumer.sapphire=') -and $brand.Contains('consumer.primaryGradientStart='))
P 'white-label creator color authority retained' $brand.Contains('creator.primary=')
P 'application ID comes from branding authority' $app.Contains('applicationId = brand("brand.applicationId")')
P 'PH04 UI-quality regression retained' (Test-Path (Join-Path $root 'scripts/ph04_r04_ui_quality_regression.py'))
P 'PH03 scroll-crash regression retained' (Test-Path (Join-Path $root 'scripts/ph03_r02_lazy_list_key_regression.py'))
Write-Host "VC-ANDROID-PH05-R01 Windows source authority: $script:count/$script:count PASS"
