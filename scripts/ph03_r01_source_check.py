from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def ck(name, cond):
    if not cond: raise SystemExit(f'[FAIL] {name}')
    checks.append(name); print(f'[PASS] {name}')
def text(path): return (ROOT/path).read_text(encoding='utf-8')
settings=text('settings.gradle.kts'); app=text('app/build.gradle.kts'); nav=text('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
api=text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryApi.kt')
repo=text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryRepository.kt')
policy=text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/ConsumerIdentityPolicy.kt')
models=text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/model/DiscoveryModels.kt')
screens=text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
vm=text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryViewModel.kt')
build=text('feature/discovery/build.gradle.kts')

ck('PH03 discovery module registered', 'include(":feature:discovery")' in settings)
ck('app consumes PH03 discovery module', 'implementation(project(":feature:discovery"))' in app)
ck('PH03 app version marker present', 'versionName = "1.0.0-ph03"' in app)
ck('PH02 auth module remains registered', 'include(":feature:auth")' in settings)
ck('PH02 bootstrap module remains registered', 'include(":feature:bootstrap")' in settings)

for endpoint in [
    'discovery/rooms/live','discovery/rooms/trending','discovery/rooms/popular','discovery/rooms/following',
    'discovery/users/trending','discovery/users/suggested','discovery/users/online','search',
    'users/public/{username}','users/{userId}/profile','users/profile/me','users/{userId}/follow',
    'users/followers','users/following','users/friends','users/friends/requests/pending','users/friends/suggested',
    'users/friends/request','users/friends/request/{requestId}/accept','users/friends/request/{requestId}/reject','users/friends/{friendId}'
]: ck(f'PH03 endpoint {endpoint} retained', endpoint in api)

ck('consumer identity policy allows USER and CREATOR only when role is present', 'setOf("USER", "CREATOR")' in policy)
ck('consumer identity policy excludes guests', 'if (user.isGuest) return false' in policy)
ck('consumer identity policy excludes self by id', 'user.id == viewer.id' in policy)
ck('consumer identity policy excludes self by normalized username', 'user.username.trim().lowercase() == viewerName' in policy)
ck('repository applies identity policy across consumer discovery', 'ConsumerIdentityPolicy.filter' in repo and 'authoritativeRelationshipUser' in repo)
ck('role-less relationship DTOs fail closed through authoritative profile lookup', 'if (user.role.isNullOrBlank())' in repo and 'api.profileById(user.id).asUser()' in repo and '?: return null' in repo)

for route in ['Home','Explore','Rooms','People','Creators','Search','MyProfile','Followers','Following','Friends','PublicProfile']:
    ck(f'PH03 route {route} exists', re.search(rf'const val {route}\s*=', nav) is not None)
ck('public profile navigation is username based', 'fun profile(username: String)' in nav and 'profile/${Uri.encode(username.trim())}' in nav)
ck('public profile route does not expose UUID parameter', 'profile/{userId}' not in nav and 'profile/{id}' not in nav)
ck('PH02 user-ready handoff enters PH03 Home', 'popUpTo(VoiceCloudRoutes.UserReady)' in nav and 'VoiceCloudRoutes.Home' in nav)
ck('guest upgrade remains reachable from PH03 consumer shell', 'VoiceCloudRoutes.GuestUpgrade' in nav and 'onUpgrade' in screens)

for screen in ['HomeScreen','ExploreScreen','RoomsScreen','PeopleScreen','SearchScreen','PublicProfileScreen','MyProfileScreen','SocialListScreen','FriendsScreen']:
    ck(f'PH03 UI {screen} exists', f'fun {screen}(' in screens)
ck('live room UI remains discovery-only before PH05', 'Room detail/join is introduced in PH05' in screens)
ck('friends supports request accept reject and remove', all(x in vm for x in ['sendFriendRequest','acceptFriendRequest','rejectFriendRequest','removeFriend']))
ck('follow and unfollow supported', 'followProfile' in vm and 'unfollowFromList' in vm)
ck('search returns users and rooms only for consumer scope', 'SearchSnapshot' in models and 'users = visibleUsers' in repo and 'rooms = rooms.await().results.rooms' in repo)
ck('Creator list is derived from consumer-visible CREATOR identities', 'it.role?.uppercase() == "CREATOR"' in repo)
ck('User portal remains centralized Light-first theme', 'ConsumerColors.Sapphire' in screens and 'ConsumerColors.Indigo' in screens)

feature_modules=set(re.findall(r'include\(":feature:([^\"]+)"\)', settings))
ck('PH03 feature module set is scoped to bootstrap auth discovery', feature_modules == {'bootstrap','auth','discovery'})
for forbidden in [':feature:communities',':feature:messages',':feature:notifications',':feature:rtc',':feature:wallet']:
    ck(f'PH04+ module {forbidden} absent', forbidden not in settings)

ck('discovery declares Hilt directly', 'implementation(libs.hilt.android)' in build and 'ksp(libs.hilt.compiler)' in build)
ck('discovery exports coroutine public ABI directly', 'api(libs.kotlinx.coroutines.android)' in build)
ck('discovery exports Retrofit public API directly', 'api(libs.retrofit.core)' in build)
ck('discovery uses authenticated refresh-capable Retrofit authority', 'createAuthenticatedRetrofit' in text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/di/DiscoveryNetworkModule.kt'))

print(f'VC-ANDROID-PH03-R01 source authority: {len(checks)}/{len(checks)} PASS')
