from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def ck(name, ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
nav=read('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
creator=read('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt')
hapi=read('feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingApi.kt')
hrepo=read('feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingRepository.kt')
hvm=read('feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingViewModel.kt')
appgradle=read('app/build.gradle.kts')
ck('PH11 app version declared', 'versionName = "1.0.0-ph11"' in appgradle)
for route in ['CreatorLive','CreatorRoomCreate','CreatorRoomManage','CreatorRoomSettings','CreatorLiveConsole','CreatorScheduleCreate','CreatorScheduleEdit']:
    ck(f'PH11 route declared: {route}', f'const val {route}' in nav)
for path in ['creator/live','creator/live/rooms/create','creator/live/rooms/{roomId}/console','creator/live/schedules/create']:
    ck(f'PH11 Creator route namespace present: {path}', path in nav)
ck('Creator Live Studio screen implemented', 'fun CreatorLiveStudioScreen(' in creator)
ck('Creator Live routes are CREATOR role guarded', nav.count('authState.user?.normalizedRole != VoiceCloudRole.CREATOR') >= 12)
for endpoint in ['@GET("rooms/mine")','@POST("rooms")','@PATCH("rooms/{roomId}")','@POST("rooms/{roomId}/start")','@POST("rooms/{roomId}/pause")','@POST("rooms/{roomId}/resume")','@POST("rooms/{roomId}/end")']:
    ck(f'room lifecycle endpoint wired: {endpoint}', endpoint in hapi)
for endpoint in ['@GET("scheduled-rooms")','@POST("scheduled-rooms")','@PATCH("scheduled-rooms/{scheduleId}")','@DELETE("scheduled-rooms/{scheduleId}")']:
    ck(f'schedule endpoint wired: {endpoint}', endpoint in hapi)
for endpoint in ['@POST("rtc/rooms/join")','@GET("rtc/rooms/{roomId}/stage")','approve-speaker','reject-speaker','invite-speaker','remove-speaker','mute-user','lock-seat']:
    ck(f'Creator Live RTC/stage authority wired: {endpoint}', endpoint in hapi)
ck('scheduled start reuses/links room instead of duplicating every start', 'firstOrNull { it.scheduledRoomId == schedule.id }' in hrepo and 'scheduledRoomId = schedule.id' in hrepo)
ck('Creator schedule list uses authenticated host identity', '@Query("hostId") hostId: String' in hapi and 'loadCreatorStudio(creatorId' in hvm and 'repository.schedules(it)' in hvm)
ck('Creator schedule presentation uses 12-hour AM/PM', 'hh:mm a' in creator)
ck('Creator schedule presentation uses device/local timezone', 'ZoneId.systemDefault()' in creator or 'timeZone' in creator)
ck('Creator Live reuses proven Hosting implementation', 'HostLiveConsoleScreen(' in nav and 'RoomEditorScreen(' in nav and 'ScheduleEditorScreen(' in nav)
ck('PH11 does not create a duplicate RTC feature module', not (ROOT/'feature/creator-live').exists() and not (ROOT/'feature/rtc').exists())
ck('Creator module depends on hosting foundation', 'implementation(project(":feature:hosting"))' in read('feature/creator/build.gradle.kts'))
ck('PH11 source contains no Admin endpoint exposure in Creator code', '/admin/' not in creator.lower() and 'admin/' not in read('feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt').lower())
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH11-R02 source authority: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH11-R02 source authority: {passed}/{len(checks)} PASS')
