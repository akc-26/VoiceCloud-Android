from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def t(rel): return (ROOT/rel).read_text(encoding='utf-8')
checks=[]
def ck(label, cond): checks.append((label,bool(cond)))
rtc=t('feature/live/src/main/java/app/voicecloud/feature/live/rtc/RtcAudioEngine.kt')
device=t('feature/auth/src/main/java/app/voicecloud/feature/auth/data/DeviceMetadataProvider.kt')
auth=t('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt')
push=t('feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/PushNotificationCoordinator.kt')
fcm=t('app/src/main/java/app/voicecloud/android/notifications/VoiceCloudFirebaseMessagingService.kt')
discovery=t('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
eng=t('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt')
gradle=t('app/build.gradle.kts')
ck('LiveKit ApplicationContext qualifier has explicit parameter target', '@param:ApplicationContext private val context: Context' in rtc)
ck('device metadata ApplicationContext qualifier has explicit parameter target', '@param:ApplicationContext private val context: Context' in device)
ck('Google Sign-In legacy deprecations are explicit compatibility debt', auth.startswith('@file:Suppress("DEPRECATION")') and 'Credential Manager migration is coordinated' in auth)
ck('FCM current-token deprecation is explicit backend compatibility debt', '@Suppress("DEPRECATION")' in push and 'R06 backend still persists FCM registration tokens' in push)
ck('FCM onNewToken override-deprecation diagnostic is explicitly suppressed', '@Suppress("OVERRIDE_DEPRECATION")' in fcm and 'R06 backend token-registration contract migrates' in fcm)
ck('discovery non-null descriptor no longer uses redundant Elvis', 'Text(descriptor ?:' not in discovery and 'Text(descriptor, maxLines = 1' in discovery)
ck('engagement sender rendering no longer uses unnecessary smart-cast safe call', 'val senderName = message.sender?.displayName.orEmpty()' in eng and 'message.sender?.displayName.orEmpty(), fontWeight' not in eng)
for lib in ['libandroidx.graphics.path.so','libdatastore_shared_counter.so','liblkjingle_peerconnection_so.so']:
    ck(f'known unstrippable native library explicitly keeps debug symbols: {lib}', f'**/{lib}' in gradle)
ck('JNI keepDebugSymbols uses current packaging DSL', 'jniLibs.keepDebugSymbols += setOf(' in gradle)
failed=[label for label,ok in checks if not ok]
for label,ok in checks: print(f"[{'PASS' if ok else 'FAIL'}] {label}")
if failed: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH09-R08 warning/packaging closure regression: {len(checks)}/{len(checks)} PASS')
