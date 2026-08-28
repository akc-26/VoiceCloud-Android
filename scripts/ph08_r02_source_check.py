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
nav = text("app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt")
disc_api = text("feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryApi.kt")
disc_repo = text("feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryRepository.kt")
disc_models = text("feature/discovery/src/main/java/app/voicecloud/feature/discovery/model/DiscoveryModels.kt")
disc_vm = text("feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryViewModel.kt")
disc_ui = text("feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt")
econ_api = text("feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyApi.kt")
econ_repo = text("feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyRepository.kt")
econ_models = text("feature/economy/src/main/java/app/voicecloud/feature/economy/model/EconomyModels.kt")
econ_ui = text("feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt")
profile_api = text("feature/profile/src/main/java/app/voicecloud/feature/profile/data/ProfileApi.kt")
profile_repo = text("feature/profile/src/main/java/app/voicecloud/feature/profile/data/ProfileRepository.kt")
profile_ui = text("feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt")
profile_models = text("feature/profile/src/main/java/app/voicecloud/feature/profile/model/ProfileModels.kt")
auth_repo = text("feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthRepository.kt")
auth_ui = text("feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt")
live_ui = text("feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt")
profile_gradle = text("feature/profile/build.gradle.kts")
contract = json.loads(text("contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json"))
ops = {(op.get("method"), op.get("path")) for op in contract.get("operations", [])}

# PH08 original authority remains present.
check("profile module registered", 'include(\":feature:profile\")' in settings)
check("app consumes profile module", 'implementation(project(\":feature:profile\"))' in app)
check("PH08 app version preserved", 'versionName = "1.0.0-ph08"' in app)
check("PH07 economy module preserved", 'include(\":feature:economy\")' in settings and 'implementation(project(\":feature:economy\"))' in app)
check("PH07 server-authoritative economy validation preserved", "wallet/purchase/validate" in econ_api and "android/billing/vip/verify" in econ_api)

required = [
    ("GET", "/replays"), ("GET", "/replays/:param"), ("GET", "/room-activity/me"),
    ("GET", "/users/profile/me"), ("PATCH", "/users/profile"),
    ("POST", "/users/avatar"), ("PUT", "/users/avatar"), ("DELETE", "/users/avatar"),
    ("POST", "/users/cover"), ("PUT", "/users/cover"), ("DELETE", "/users/cover"),
    ("GET", "/blocks"), ("DELETE", "/blocks/:param"),
    ("GET", "/users/visitors"), ("GET", "/users/visitors/stats"),
    ("GET", "/users/friends"), ("GET", "/users/friends/requests/pending"),
    ("GET", "/users/friends/suggested"), ("DELETE", "/users/friends/request/:param/cancel"),
    ("GET", "/cms/pages"), ("GET", "/cms/pages/:param"),
]
for method, path in required:
    check(f"R06 contract contains {method} {path}", (method, path) in ops)

check("PH08 replay/activity endpoints remain wired", all(x in profile_api for x in ['@GET("replays")', '@GET("replays/{replayId}")', '@GET("room-activity/me")']))
check("profile edits remain backend-authoritative", '@PATCH("users/profile")' in profile_api and 'return api.myProfile()' in profile_repo)
check("avatar supports create and replacement uploads", '@POST("users/avatar")' in profile_api and '@PUT("users/avatar")' in profile_api and 'mediaPart("avatar"' in profile_repo)
check("cover supports create and replacement uploads", '@POST("users/cover")' in profile_api and '@PUT("users/cover")' in profile_api and 'mediaPart("cover"' in profile_repo)
check("profile upload recommendations are visible", '800 × 800 px' in profile_ui and '1600 × 600 px' in profile_ui)
check("remote media fallback remains present", 'coil-compose:3.5.0' in profile_gradle and 'SubcomposeAsyncImage' in profile_ui and 'MediaFallback' in profile_ui)
check("replay access and processing states remain explicit", 'accessAllowed == false' in profile_ui and 'PROCESSING' in profile_ui and 'Replay access restricted' in profile_ui)

# Corrective/integration authority.
check("Home is live-room dominant", '.take(10)' in disc_repo and 'people = people.filter' in disc_repo and '.take(4)' in disc_repo and 'SectionTitle("Live now"' in disc_ui)
check("Home uses centralized VoiceCloud branding", 'VoiceCloudBrandMark(size = 44.dp)' in disc_ui and 'VoiceCloudBrand.name' in disc_ui)
check("Explore is differentiated from Home", 'SectionTitle("Trending rooms"' in disc_ui and 'HomeShortcut("Communities"' in disc_ui and 'HomeShortcut("Events"' in disc_ui)
check("Search has nonblank landing snapshot", 'data class SearchLandingSnapshot' in disc_models and 'suspend fun searchLanding' in disc_repo and 'loadSearchLanding' in disc_vm)
check("Search All renders four default sections", all(x in disc_ui for x in ['SectionTitle("People"', 'SectionTitle("Creators"', 'SectionTitle("Rooms"', 'SectionTitle("Communities"']) and 'onLoadDefaults' in disc_ui)
check("Search has tab-specific empty states", 'No ${selectedTab.lowercase()} found' in disc_ui and 'No results found for' in disc_ui)
check("Friends supports incoming/outgoing and sent cancellation", '@DELETE("users/friends/request/{requestId}/cancel")' in disc_api and 'cancelFriendRequest' in disc_repo and 'SectionTitle("Sent")' in disc_ui and '"Cancel"' in disc_ui)
check("Friends tolerate backend wrapper shapes", 'suspend fun friends' in disc_api and ': Any' in disc_api and 'itemMaps(' in disc_repo and 'pendingRequests(' in disc_repo)

check("Profile exposes economy modules directly", 'economyProfileOrder' in econ_models and 'onEconomySection' in disc_ui and 'Economy & progression' in disc_ui)
check("Economy order prioritizes core functions", re.search(r'WALLET,\s*EconomySection\.VIP,\s*EconomySection\.STORE,\s*EconomySection\.GIFTS', econ_models, re.S) is not None and econ_models.find('EconomySection.REFERRALS') > econ_models.find('EconomySection.TICKETS'))
check("Economy no longer renders raw payload toString", 'payload.toString()' not in econ_ui and 'payloadSections(' in econ_ui and 'EconomyDataCard(' in econ_ui)
check("Economy reads degrade per endpoint instead of blanking whole module", 'private suspend fun safe' in econ_repo and econ_repo.count('safe {') >= 10)
check("Economy mutations refresh authoritative server payload", 'repo.load(section.name)' in text("feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyViewModel.kt"))
check("Help/terms pages are admin-CMS driven", '@GET("cms/pages")' in profile_api and '@GET("cms/pages/{slug}")' in profile_api and 'HelpPagesScreen' in profile_ui and 'HelpPageScreen' in profile_ui)
check("Profile exposes consolidated Help entry", 'Help, terms & information' in disc_ui and 'VoiceCloudRoutes.HelpPages' in nav)

check("Onboarding is first-setup only", 'preferences.hasCompletedOnboarding(user.id)' in auth_repo and 'serverProfileAlreadyConfigured' in auth_repo and 'preferences.markOnboardingCompleted(user.id)' in auth_repo)
check("page navigation does not use text Back controls", 'Text("Back")' not in auth_ui + disc_ui + econ_ui + profile_ui + live_ui)
check("secondary navigation uses smooth transitions", 'slideInHorizontally(animationSpec = tween(220))' in nav and 'slideOutHorizontally(animationSpec = tween(220))' in nav and 'fadeIn(animationSpec = tween(180))' in nav)

check("Live-room title supports two lines", 'maxLines = 2' in live_ui and 'LiveRoomTopBar' in live_ui)
check("Live-room leave action is top red identified", 'Text("Leave", color = CommonColors.Error' in live_ui)
check("Live-room chat composer is persistent bottom bar", 'bottomBar = {' in live_ui and 'ChatComposer(' in live_ui)
check("Live-room emoji and gift actions are floating icon buttons", 'LiveFloatingAction(R.drawable.vc_icon_emoji' in live_ui and 'LiveFloatingAction(R.drawable.vc_icon_gift' in live_ui and 'ModalBottomSheet' in live_ui)
check("centralized emoji/gift vector resources exist", (ROOT / 'core/designsystem/src/main/res/drawable/vc_icon_emoji.xml').exists() and (ROOT / 'core/designsystem/src/main/res/drawable/vc_icon_gift.xml').exists())
check("normal room discovery enters live room directly", 'onRoom = { open(VoiceCloudRoutes.roomExperience(it)) }' in nav and 'RoomsScreen(state, { vm.loadRooms() }, { open(VoiceCloudRoutes.roomExperience(it)) }' in nav and 'roomPreview(it)' not in nav)
check("legacy room preview remains isolated for deep links only", 'composable(VoiceCloudRoutes.RoomPreview)' in nav and 'onJoin = { open(VoiceCloudRoutes.roomExperience(roomId)) }' in nav)


# Payment rail authority added in R02.
payment_api = text("feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyApi.kt")
payment_repo = text("feature/economy/src/main/java/app/voicecloud/feature/economy/data/EconomyRepository.kt")
payment_vm = text("feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyViewModel.kt")
payment_ui = text("feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt")
payment_billing = text("feature/economy/src/main/java/app/voicecloud/feature/economy/billing/PlayBillingCoordinator.kt")
payment_rail = text("feature/economy/src/main/java/app/voicecloud/feature/economy/billing/PaymentRail.kt")
payment_gradle = text("feature/economy/build.gradle.kts")
root_props = text("gradle.properties")
for method, path in [
    ("POST", "/wallet/purchase/web/initiate"), ("POST", "/wallet/purchase/web/complete"), ("POST", "/wallet/purchase/web/cancel"),
    ("POST", "/vip/checkout/web/initiate"), ("POST", "/vip/checkout/web/complete"), ("POST", "/vip/checkout/web/cancel"),
    ("POST", "/wallet/purchase/initiate"), ("POST", "/wallet/purchase/validate"),
    ("GET", "/android/billing/vip/catalog"), ("POST", "/android/billing/vip/verify"),
]:
    check(f"R06 contract contains payment authority {method} {path}", (method, path) in ops)
check("current test payment rail defaults to hosted gateway", "VOICECLOUD_ANDROID_PAYMENT_MODE=HOSTED_GATEWAY" in root_props)
check("launch rail supports hosted gateway or Google Play without source rewrite", "VOICECLOUD_PAYMENT_MODE" in payment_gradle and "HOSTED_GATEWAY" in payment_rail and "GOOGLE_PLAY" in payment_rail)
check("hosted Wallet/VIP checkout routes are wired", all(x in payment_api for x in ["wallet/purchase/web/initiate", "wallet/purchase/web/complete", "wallet/purchase/web/cancel", "vip/checkout/web/initiate", "vip/checkout/web/complete", "vip/checkout/web/cancel"]))
check("hosted gateway remains backend-selected and provider-opaque", "checkoutUrl" in payment_repo and "checkoutSessionId" in payment_repo and not re.search(r'mapOf\([^\n]*(STRIPE|RAZORPAY|PAYPAL)', payment_repo, re.I))
check("Google Play Billing queries products and launches native billing", "queryProductDetailsAsync" in payment_billing and "launchBillingFlow" in payment_billing)
check("Google Play Wallet and VIP are server verified", "repo.validateCoinPurchase" in payment_vm and "repo.verifyVip" in payment_vm)
check("Google Play settlement precedes consume/acknowledge", "consumeAfterServerVerification" in payment_vm and "acknowledgeAfterServerVerification" in payment_vm)
check("Wallet and VIP expose real purchase controls", 'Text("Add credits")' in payment_ui and 'Text("Choose VIP")' in payment_ui)

check("no mock/demo corrective production payloads", not re.search(r'\b(mock|dummy|sampleData|fakeReplay|fakeVisitor|fakeRoom)\b', disc_repo + econ_repo + profile_repo, re.I))
print("[PASS] VC-ANDROID-PH08-R02 source authority complete")
