from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
checks = []

def text(path: str) -> str:
    p = ROOT / path
    if not p.is_file():
        raise SystemExit(f"[FAIL] required source missing: {path}")
    return p.read_text(encoding="utf-8")

def check(name: str, condition: bool):
    if not condition:
        raise SystemExit(f"[FAIL] {name}")
    checks.append(name)
    print(f"[PASS] {name}")

settings = text("settings.gradle.kts")
app = text("app/build.gradle.kts")
props = text("gradle.properties")
api = text("feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthApi.kt")
models = text("feature/auth/src/main/java/app/voicecloud/feature/auth/model/AuthModels.kt")
repo = text("feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthRepository.kt")
refresh = text("feature/auth/src/main/java/app/voicecloud/feature/auth/data/SingleFlightTokenRefreshCoordinator.kt")
network = text("core/network/src/main/java/app/voicecloud/core/network/ApiClientFactory.kt")
nav = text("app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt")
screens = text("feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt")
vm = text("feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt")
manifest = text("app/src/main/AndroidManifest.xml")
main = text("app/src/main/java/app/voicecloud/android/MainActivity.kt")
root_ui = text("app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt")
prefs = text("core/preferences/src/main/java/app/voicecloud/core/preferences/VoiceCloudPreferences.kt")
fcm = text("feature/auth/src/main/java/app/voicecloud/feature/auth/data/FcmTokenRegistrationFoundation.kt")
google_exchange = text("feature/auth/src/main/java/app/voicecloud/feature/auth/data/FirebaseGoogleTokenExchange.kt")
catalog = text("gradle/libs.versions.toml")
feature_build = text("feature/auth/build.gradle.kts")

check("PH02 auth module registered", 'include(":feature:auth")' in settings)
check("app consumes PH02 auth module", 'implementation(project(":feature:auth"))' in app)
check("PH02 version marker present", 'versionName = "1.0.0-ph02"' in app)
check("debug staging release variants retained", all(x in app for x in ('debug {', 'create("staging")', 'release {')))
check("PH01 Raspberry Pi endpoint authority retained", 'VOICECLOUD_DEBUG_API_BASE_URL=https://voicecloud.tailfca77b.ts.net' in props)

public_endpoints = [
    'auth/login','auth/register','auth/phone/send-otp','auth/phone/login','auth/google/login',
    'auth/guest/login','auth/forgot-password','auth/reset-password','auth/refresh','creator-access/applications',
]
for endpoint in public_endpoints:
    check(f"public auth endpoint {endpoint} retained", endpoint in api)
for endpoint in ['auth/me','auth/guest/upgrade','auth/logout','auth/logout-all','users/profile','users/settings','notifications/register-device']:
    check(f"authenticated endpoint {endpoint} retained", endpoint in api)

check("public and authenticated Retrofit clients are separated", 'createPublicRetrofit' in network and 'createAuthenticatedRetrofit' in network)
check("public client has no token interceptor", 'createPublicRetrofit(baseUrl: String, moshi: Moshi)' in network and '.client(baseClient().build())' in network)
check("authenticated client uses secure token interceptor", 'AuthTokenInterceptor(tokenVault)' in network)
check("authenticated client uses refresh authenticator", '.authenticator(RefreshingAuthenticator(tokenVault, refreshCoordinator))' in network)
check("refresh coordinator is synchronized single-flight", 'synchronized(lock)' in refresh)
check("refresh rotation saves both returned tokens", 'tokenVault.save(body.accessToken, body.refreshToken)' in refresh)
check("refresh re-authenticates active realtime client", 'realtimeClient.refreshAuthentication()' in refresh)
check("failed refresh clears secure token authority", 'tokenVault.clear()' in refresh)

check("secure session restore validates /auth/me", 'authenticatedApi.me()' in repo and 'restoreSession()' in repo)
check("session metadata persists outside token vault", 'setSessionMetadata' in repo and 'sessionId' in prefs and 'deviceId' in prefs)
check("logout and logout-all both supported", 'authenticatedApi.logout()' in repo and 'authenticatedApi.logoutAll()' in repo)
check("logout clears local token/realtime/preferences state", 'tokenVault.clear()' in repo and 'realtimeClient.disconnect()' in repo and 'preferences.clearAccountPreferences()' in repo)

check("backend role enum is exact and finite", all(role in models for role in ['GUEST','USER','CREATOR','ADMIN','SUPER_ADMIN','UNKNOWN']))
check("Creator portal hard-requires CREATOR role", 'user.normalizedRole != VoiceCloudRole.CREATOR' in repo and 'CreatorRoleRequiredException' in repo)
check("Creator login follows finalized email-only UI", 'Enter a valid Creator email address.' in repo and 'Creator email' in screens)
check("admin roles are blocked from Android user portal", 'VoiceCloudRole.ADMIN' in repo and 'VoiceCloudRole.SUPER_ADMIN' in repo and 'Administrative accounts are not available' in repo)

for route in ['PortalSelector','UserSignIn','Register','PhoneSignIn','OtpVerify','ForgotPassword','ResetPassword','Onboarding','GuestUpgrade','Restricted','SessionExpired','UserReady','CreatorSignIn','CreatorAccess','CreatorReady','Maintenance']:
    check(f"PH02 route {route} exists", f'const val {route}' in nav)
check("portal selector exposes User and Creator entry", 'onUser' in screens and 'onCreator' in screens and 'Creator Studio' in screens)
check("first-time onboarding remains three-step", 'Step $step of 3' in screens and 'updateProfile' in repo and 'updateSettings' in repo)
check("completed onboarding is persisted per user without logout-loop regression", 'markOnboardingCompleted(user.id)' in repo and 'hasCompletedOnboarding(user.id)' in repo and 'stringSetPreferencesKey("onboarding_completed_users")' in prefs and 'it.remove(Keys.onboardingCompletedUsers)' not in prefs)
check("restricted account and session-expired UI exist", 'RestrictedScreen' in screens and 'SessionExpiredScreen' in screens)
check("maintenance handling remains backend-authoritative", 'MaintenanceScreen' in screens and '503 ->' in vm)
check("PH02 ready screens stop before PH03 product scope", 'Consumer Home and discovery begin in PH03' in screens and 'dashboard/product modules begin in the locked later phase' in screens)

check("phone OTP validates E.164 and six-digit code", 'Regex("^\\\\+[1-9]\\\\d{1,14}$")' in repo and 'code.length == 6' in repo)
check("guest upgrade supports email phone Google", all(x in repo for x in ['method = "email"','method = "phone"','method = "google"']))
check("forgot/reset password contract implemented", 'ForgotPasswordRequest' in models and 'ResetPasswordRequest' in models and 'token.trim().length >= 32' in repo)

check("reset-password App Link is HTTPS autoVerify", 'android:autoVerify="true"' in manifest and 'android:scheme="https"' in manifest and 'android:pathPrefix="/auth/reset-password"' in manifest)
check("App Link host comes from centralized Web authority", '${voicecloudWebHost}' in manifest and 'manifestPlaceholders["voicecloudWebHost"]' in app)
check("reset intent validates configured VoiceCloud host", 'BuildConfig.WEB_BASE_URL' in main and 'uri.host.equals(expectedHost' in main)
check("reset intent rejects undersized one-time tokens", 'takeIf { it.length >= 32 }' in main)
check("running app handles reset links through onNewIntent", 'override fun onNewIntent' in main and 'LaunchedEffect(initialResetToken, mobileConfig)' in nav)

check("device metadata foundation implemented", 'DeviceMetadataProvider' in repo and 'registerDeviceFoundation' in repo)
check("all primary auth methods sync device registration foundation", repo.count("registerDeviceFoundation(pushToken = null)") >= 6)
check("FCM token registration foundation implemented", 'onNewToken' in fcm and 'registerPushToken' in fcm and 'notifications/register-device' in api)

check("Google client identifiers are centralized Gradle properties", all(k in props and k in app for k in [
    'VOICECLOUD_GOOGLE_WEB_CLIENT_ID','VOICECLOUD_FIREBASE_API_KEY','VOICECLOUD_FIREBASE_APPLICATION_ID','VOICECLOUD_FIREBASE_PROJECT_ID'
]))
check("Google login is fail-closed when Firebase public config is absent", 'val isConfigured: Boolean' in models and 'Google Sign-In is not configured for this Android build.' in screens)
check("Google OAuth token is exchanged through Firebase Auth", 'GoogleAuthProvider.getCredential' in google_exchange and 'signInWithCredential' in google_exchange)
check("VoiceCloud receives Firebase Authentication ID token", 'user.getIdToken(true)' in google_exchange and 'FirebaseGoogleTokenExchange.exchange' in screens)
check("Firebase SDK is pinned through current BoM authority", 'firebaseBom = "34.18.0"' in catalog and 'implementation(platform(libs.firebase.bom))' in feature_build and 'implementation(libs.firebase.auth)' in feature_build)

check("User portal remains Light-first", 'VoiceCloudTheme(portal = PortalTheme.User, darkTheme = false)' in root_ui)
check("Creator auth presentation uses Creator dark authority", 'darkTheme = creator' in screens)

feature_modules = re.findall(r'include\(":feature:([^\"]+)"\)', settings)
check("PH02 does not pull later business feature modules forward", set(feature_modules) == {'bootstrap','auth'})
for forbidden in [':feature:home', ':feature:rooms', ':feature:wallet', ':feature:messages', ':feature:rtc', ':feature:communities']:
    check(f"later feature {forbidden} absent", forbidden not in settings)

print(f"VC-ANDROID-PH02-R01 source authority: {len(checks)}/{len(checks)} PASS")
