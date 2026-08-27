from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def ck(name, cond):
    if not cond: raise SystemExit(f'[FAIL] {name}')
    checks.append(name); print(f'[PASS] {name}')
def text(path): return (ROOT/path).read_text(encoding='utf-8')
settings=text('settings.gradle.kts'); app=text('app/build.gradle.kts'); catalog=text('gradle/libs.versions.toml')
nav=text('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
manifest=text('app/src/main/AndroidManifest.xml'); application=text('app/src/main/java/app/voicecloud/android/VoiceCloudApplication.kt')
service=text('app/src/main/java/app/voicecloud/android/notifications/VoiceCloudFirebaseMessagingService.kt')
api=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/EngagementApi.kt')
repo=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/EngagementRepository.kt')
monitor=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/NotificationRealtimeMonitor.kt')
push=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/PushNotificationCoordinator.kt')
resolver=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/EngagementRouteResolver.kt')
network=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/di/EngagementNetworkModule.kt')
models=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/model/EngagementModels.kt')
vm=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementViewModel.kt')
screens=text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt')
discovery=text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
build=text('feature/engagement/build.gradle.kts')
main=text('app/src/main/java/app/voicecloud/android/MainActivity.kt')
test=text('feature/engagement/src/test/java/app/voicecloud/feature/engagement/data/EngagementRouteResolverTest.kt')

ck('PH04 engagement module registered', 'include(":feature:engagement")' in settings)
ck('app consumes PH04 engagement module', 'implementation(project(":feature:engagement"))' in app)
ck('PH04 version marker present', 'versionName = "1.0.0-ph04"' in app)
ck('PH03 discovery module retained', 'include(":feature:discovery")' in settings)
ck('PH02 auth module retained', 'include(":feature:auth")' in settings)

for endpoint in [
 'clubs','clubs/{id}','clubs/{id}/join','clubs/{id}/leave','clubs/{id}/membership/me','clubs/{id}/members','clubs/{id}/scheduled-rooms','clubs/{id}/invite-code','clubs/{id}/members/{userId}',
 'scheduled-rooms','scheduled-rooms/{id}','scheduled-rooms/{id}/reminder',
 'chat/conversations','chat/conversations/{id}','chat/conversations/{id}/messages','chat/conversations/{id}/read',
 'notifications','notifications/unread-count','notifications/{id}/read','notifications/read-all','notifications/{id}'
]: ck(f'PH04 endpoint {endpoint} retained', endpoint in api)

ck('community create update delete supported', '@POST("clubs")' in api and '@PATCH("clubs/{id}")' in api and '@DELETE("clubs/{id}")' in api)
ck('community public/private visibility normalized', 'setOf("PUBLIC", "PRIVATE")' in repo)
ck('private invite code sent only through join body', 'JoinCommunityBody(inviteCode?.trim()?.ifBlank { null })' in repo)
ck('invite code rotation supported', 'rotateInviteCode' in repo and 'inviteCode = result.inviteCode' in vm)
ck('community roles cover owner admin moderator member', 'setOf("OWNER", "ADMIN", "MODERATOR", "MEMBER")' in repo)
ck('owner/admin member management UI present', 'assignableRoles' in screens and 'Remove member' in screens)
ck('community delete is owner workflow', 'onDelete' in screens and 'deleteCommunity' in vm)
ck('community member identities fail closed to USER/CREATOR', 'role in setOf("USER", "CREATOR")' in repo and '!user.isGuest' in repo)

ck('event listing detail reminder supported', all(x in vm for x in ['loadEvents','loadEvent','remindEvent']))
ck('premium event purchase deliberately deferred', all(x not in api for x in ['purchase', 'checkout', 'ticket/purchase']) and 'Buy ticket' not in screens and 'Purchase ticket' not in screens)
ck('scheduled decimal price is parse-safe', 'val ticketPriceAmount: Any?' in models)

ck('messages inbox conversation send read supported', all(x in vm for x in ['loadConversations','loadConversation','sendMessage']))
ck('conversation read receipt retained', 'markConversationRead' in repo)
ck('direct conversation from public profile retained', 'startDirectConversation' in vm and 'onMessage' in discovery and 'startDirectConversation(userId)' in nav)
ck('conversation uses HTTP refresh cadence', 'delay(5_000)' in screens and 'refreshConversationSilently' in vm)
ck('PH04 does not invent chat socket conversation join', 'chat_message' not in monitor and 'conversationId' not in monitor)

for route in ['Communities','CommunityDetail','CommunityCreate','CommunityManage','CommunityMembers','CommunityEvents','Events','EventDetail','Messages','Conversation','Notifications']:
    ck(f'PH04 route {route} exists', re.search(rf'const val {route}\s*=', nav) is not None)
ck('static community create route declared before dynamic detail route', nav.index('composable(VoiceCloudRoutes.CommunityCreate)') < nav.index('composable(VoiceCloudRoutes.CommunityDetail)'))
ck('PH03 Home exposes PH04 entry points', all(x in discovery for x in ['onCommunities','onMessages','onNotifications']))

ck('notification lifecycle supported', all(x in vm for x in ['loadNotifications','markNotificationRead','markAllNotificationsRead','deleteNotification']))
ck('exact notification realtime event set retained', all(x in monitor for x in ['notification:new','notification:read','notification:deleted']))
ck('realtime listeners rebound after authenticated reconnect', 'RealtimeConnectionState.Authenticated' in vm and 'ensureNotificationRealtimeListeners' in vm)
ck('FCM service registered', 'VoiceCloudFirebaseMessagingService' in manifest and 'com.google.firebase.MESSAGING_EVENT' in manifest)
ck('Android 13 notification permission declared', 'android.permission.POST_NOTIFICATIONS' in manifest)
ck('Android 13 notification permission requested in UI', 'ActivityResultContracts.RequestPermission()' in screens and 'POST_NOTIFICATIONS' in screens)
ck('Firebase Messaging dependency is BoM managed', 'firebase-messaging' in catalog and 'implementation(libs.firebase.messaging)' in app and 'implementation(libs.firebase.messaging)' in build)
ck('default Firebase app initialized from centralized BuildConfig', all(x in application for x in ['BuildConfig.FIREBASE_API_KEY','BuildConfig.FIREBASE_APPLICATION_ID','BuildConfig.FIREBASE_PROJECT_ID','FirebaseApp.DEFAULT_APP_NAME']))
ck('FCM token sync fails closed if Firebase default app unavailable', 'runCatching { FirebaseMessaging.getInstance() }.getOrNull() ?: return' in push)
ck('FCM refresh token uses PH02 registration foundation', 'FcmTokenRegistrationFoundation' in service and 'tokenRegistration.onNewToken(token)' in service)
ck('post-login current FCM token sync retained', 'engagementViewModel.syncPushToken()' in nav and 'syncCurrentToken' in push)

for literal in ['conversationId','scheduledRoomId','eventId','clubId','communityId','roomId']:
    ck(f'notification route resolver handles {literal}', f'string("{literal}")' in resolver)
ck('room notification stops at PH03 rooms list before PH05', 'string("roomId") != null -> Rooms' in resolver)
ck('external navigation is allowlisted', 'EXTRA_NAVIGATION_ROUTE' in main and 'value == "notifications"' in main and 'Regex("communities/' in main and 'Regex("events/' in main and 'Regex("messages/' in main)
ck('notification route tests cover all PH04 destinations', test.count('@Test') >= 7 and 'roomStopsAtRoomsBeforePh05' in test)

ck('engagement uses authenticated refresh-capable Retrofit', 'createAuthenticatedRetrofit' in network and 'TokenRefreshCoordinator' in network)
ck('engagement exports discovery model public ABI', 'api(project(":feature:discovery"))' in build)
ck('engagement exports Retrofit public ABI', 'api(libs.retrofit.core)' in build)
ck('engagement exports coroutine public ABI', 'api(libs.kotlinx.coroutines.android)' in build)
ck('dynamic PH04 LazyColumn entries use indexed namespaced keys', screens.count('itemsIndexed(') >= 7 and 'key = { it.id }' not in screens)
ck('PH05 RTC feature module not pulled forward', 'include(":feature:rtc")' not in settings and 'livekit' not in catalog.lower())
ck('PH05 live-room detail/join not introduced', 'roomId") != null -> Rooms' in resolver)

print(f'VC-ANDROID-PH04-R01 source authority: {len(checks)}/{len(checks)} PASS')
