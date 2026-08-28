$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
$settings=T 'settings.gradle.kts'; $app=T 'app/build.gradle.kts'; $manifest=T 'app/src/main/AndroidManifest.xml'
$api=T 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingApi.kt'; $repo=T 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingRepository.kt'
$vm=T 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingViewModel.kt'; $ui=T 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt'
$nav=T 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'; $rtc=T 'feature/live/src/main/java/app/voicecloud/feature/live/rtc/RtcAudioEngine.kt'; $disc=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt'
P 'PH06 hosting module registered' $settings.Contains('include(":feature:hosting")')
P 'app consumes PH06 hosting module' $app.Contains('implementation(project(":feature:hosting"))')
P 'PH06 version marker present' $app.Contains('versionName = "1.0.0-ph06"')
P 'PH06 RECORD_AUDIO permission present' $manifest.Contains('android.permission.RECORD_AUDIO')
foreach($ep in @('hosts/profile','hosts/eligibility','rooms/mine','rooms/{roomId}/start','rooms/{roomId}/pause','rooms/{roomId}/resume','rooms/{roomId}/end','scheduled-rooms','rtc/rooms/join','rtc/rooms/{roomId}/stage','rtc/rooms/{roomId}/approve-speaker','rtc/rooms/{roomId}/mute-user','polls/rooms/{roomId}','quizzes/rooms/{roomId}/active')){P "PH06 endpoint $ep retained" $api.Contains($ep)}
P 'Host access is backend-authoritative' ($vm.Contains('repository.hostAccess()') -and $vm.Contains('APPROVED'))
P 'Listener RTC stays receive-only' ($rtc.Contains('audio = false') -and $rtc.Contains('video = false'))
P 'Host microphone publishing is explicit' $rtc.Contains('localParticipant.setMicrophoneEnabled(enabled)')
P 'Host microphone is permission gated' ($ui.Contains('ActivityResultContracts.RequestPermission()') -and $ui.Contains('Manifest.permission.RECORD_AUDIO'))
P 'Schedule uses device timezone' ($ui.Contains('ZoneId.systemDefault()') -and $ui.Contains('timeZone = zone.id'))
P 'Schedule visibility is uppercase' ($ui.Contains('"PRIVATE" else "PUBLIC"') -and $repo.Contains('visibility = visibility.uppercase()'))
P 'Stage controls retained' ($vm.Contains('approve(') -and $vm.Contains('reject(') -and $vm.Contains('muteSpeaker(') -and $vm.Contains('removeSpeaker('))
P 'Polls and quiz retained' ($ui.Contains('Polls & Quiz') -and $vm.Contains('createPoll') -and $vm.Contains('createQuiz'))
P 'People search invitation retained' ($api.Contains('@GET("search")') -and $ui.Contains('Search people'))
P 'Telegram-style tab motion retained' ($nav.Contains('slideInHorizontally') -and $nav.Contains('slideOutHorizontally') -and $nav.Contains('fadeIn'))
P 'Professional selected tab icons retained' ($disc.Contains('vc_nav_home_selected') -and $disc.Contains('painterResource(if (isSelected)'))
P 'Adaptive header/footer metrics retained' ((T 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudPageMetrics.kt').Contains('bottomBarHeight') -and $disc.Contains('metrics.bottomBarHeight'))
P 'Home exposes Host Studio' $disc.Contains('HomeShortcut("Host Studio"')
P 'PH05 listener UI remains microphone-free' (-not (T 'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt').Contains('RECORD_AUDIO'))
Write-Host "VC-ANDROID-PH06-R01 Windows source authority: $script:count/$script:count PASS"
