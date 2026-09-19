from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]

def text(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def check(name: str, condition: bool) -> None:
    if not condition:
        raise SystemExit(f"[FAIL] {name}")
    print(f"[PASS] {name}")

settings = text("settings.gradle.kts")
app = text("app/build.gradle.kts")
api = text("feature/profile/src/main/java/app/voicecloud/feature/profile/data/ProfileApi.kt")
repo = text("feature/profile/src/main/java/app/voicecloud/feature/profile/data/ProfileRepository.kt")
models = text("feature/profile/src/main/java/app/voicecloud/feature/profile/model/ProfileModels.kt")
vm = text("feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileViewModel.kt")
ui = text("feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt")
nav = text("app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt")
discovery = text("feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt")
gradle = text("feature/profile/build.gradle.kts")
economy_api = text("feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyApi.kt")
contract = json.loads(text("contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json"))
contract_ops = {(op.get("method"), op.get("path")) for op in contract.get("operations", [])}

check("profile module registered", 'include(":feature:profile")' in settings)
check("app consumes profile module", 'implementation(project(":feature:profile"))' in app)
check("PH08 app version", 'versionName = "1.0.0-ph08"' in app)
check("PH07 economy module preserved", 'include(":feature:economy")' in settings and 'implementation(project(":feature:economy"))' in app)
check("PH07 wallet authority preserved", "wallet/purchase/validate" in economy_api and "android/billing/vip/verify" in economy_api)

required_api = [
    ("GET", "/replays"),
    ("GET", "/replays/:param"),
    ("GET", "/room-activity/me"),
    ("GET", "/users/profile/me"),
    ("PATCH", "/users/profile"),
    ("POST", "/users/avatar"),
    ("DELETE", "/users/avatar"),
    ("POST", "/users/cover"),
    ("DELETE", "/users/cover"),
    ("GET", "/blocks"),
    ("DELETE", "/blocks/:param"),
    ("GET", "/users/visitors"),
    ("GET", "/users/visitors/stats"),
]
for method, path in required_api:
    check(f"R06 contract contains {method} {path}", (method, path) in contract_ops)

for endpoint in [
    'replays', 'replays/{replayId}', 'room-activity/me', 'users/profile/me', 'users/profile',
    'users/avatar', 'users/cover', 'blocks', 'blocks/{userId}', 'users/visitors', 'users/visitors/stats',
]:
    check("PH08 canonical endpoint " + endpoint, endpoint in api)

check("profile edits use backend-supported fields", all(x in models for x in ["bio:", "country:", "preferredLanguage:", "interests:"]))
check("username and email remain read-only in edit UI", 'ReadOnlyIdentityField("Username"' in ui and 'ReadOnlyIdentityField("Email"' in ui)
check("avatar and cover use multipart backend uploads", '@Multipart' in api and 'MultipartBody.Part' in api and 'createFormData(field' in repo)
check("remote media uses Coil with explicit fallback", 'coil-compose:3.5.0' in gradle and 'coil-network-okhttp:3.5.0' in gradle and 'SubcomposeAsyncImage' in ui and 'MediaFallback' in ui)
check("replay player uses server URL, guarded MediaPlayer creation and lifecycle-safe release", 'ReplayAudioControls' in ui and 'val playerResult = remember(url)' in ui and 'runCatching {' in ui and 'mediaPlayer.release()' in ui)
check("replay access denial and processing states are explicit", 'accessAllowed == false' in ui and 'PROCESSING' in ui and 'Replay access restricted' in ui)
check("blocked users and visitors never render technical ids as labels", 'Text(person.id' not in ui and 'Text(entry.blockedUserId' not in ui and 'Text(visit.visitorUserId' not in ui)
check("PH08 navigation routes installed", all(x in nav for x in [
    'const val ProfileTools', 'const val EditProfile', 'const val ReplayLibrary', 'const val ReplayPlayer',
    'const val ActivityHistory', 'const val ProfileVisitors', 'const val BlockedUsers',
    'composable(VoiceCloudRoutes.ProfileTools)', 'composable(VoiceCloudRoutes.ReplayPlayer)',
]))
check("My Profile exposes PH08 tools without removing PH07 economy", 'Profile & activity' in discovery and 'Economy & progression' in discovery and 'onProfileTools' in nav)
check("guest profile tools route upgrades instead of exposing authenticated PH08", 'onProfileTools = { if (authState.user?.isGuest == true) open(VoiceCloudRoutes.GuestUpgrade)' in nav)
check("no mock/demo PH08 production payloads", not re.search(r'\b(mock|dummy|sampleData|fakeReplay|fakeVisitor)\b', repo + vm + models, re.I))
print("[PASS] VC-ANDROID-PH08-R01 source authority complete")
