from pathlib import Path
import json

root = Path(__file__).resolve().parents[1]
checks = []

def ok(label, condition):
    if not condition:
        raise SystemExit(f"[FAIL] {label}")
    checks.append(label)
    print(f"[PASS] {label}")

def text(rel):
    return (root / rel).read_text(encoding="utf-8")

settings = text("settings.gradle.kts")
for module in [
    ":app", ":core:model", ":core:designsystem", ":core:network", ":core:security",
    ":core:database", ":core:preferences", ":core:logging", ":feature:bootstrap",
]:
    ok(f"module {module} registered", f'include("{module}")' in settings)

app = text("app/build.gradle.kts")
root_build = text("build.gradle.kts")
catalog = text("gradle/libs.versions.toml")
android_builds = "\n".join(path.read_text(encoding="utf-8") for path in root.rglob("build.gradle.kts") if path != root / "build.gradle.kts")
ok("AGP 9 built-in Kotlin removes kotlin-android plugin", "libs.plugins.kotlin.android" not in root_build and "org.jetbrains.kotlin.android" not in catalog and "libs.plugins.kotlin.android" not in android_builds)
ok("AGP 9 built-in Kotlin removes legacy android.kotlinOptions DSL", "kotlinOptions" not in android_builds)
ok("debug staging release variants exist", all(x in app for x in ['debug {', 'create("staging")', 'release {']))
ok("compileSdk 37 locked", "compileSdk = 37" in app)
ok("targetSdk 36 locked", "targetSdk = 36" in app)
ok("minSdk 26 locked", "minSdk = 26" in app)
ok("API base URL is build-config controlled", "VC_API_BASE_URL" in app and "BuildConfig.API_BASE_URL" in text("app/src/main/java/app/voicecloud/android/di/FoundationModule.kt"))

colors = text("core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudColors.kt")
for token in ["0xFF0B7C86", "0xFF075762", "0xFF22C7CF", "0xFF22C55E", "0xFF123A32"]:
    ok(f"R06 design token {token} retained", token in colors)
ok("fresh User launch is Light-first", "darkTheme = false" in text("app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt"))
ok("Navigation Compose bootstrap graph is present", "NavHost" in text("app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt") and "UserPortal" in text("app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt") and "CreatorPortal" in text("app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt"))

vault = text("core/security/src/main/java/app/voicecloud/core/security/AndroidKeyStoreTokenVault.kt")
ok("Keystore AES-GCM token vault present", all(x in vault for x in ["AndroidKeyStore", "AES/GCM/NoPadding", "GCMParameterSpec"]))
interceptor = text("core/network/src/main/java/app/voicecloud/core/network/AuthTokenInterceptor.kt")
ok("HTTP foundation reads only real secure token authority", "tokenVault.accessToken()" in interceptor and 'header("Authorization", "Bearer $token")' in interceptor)
ok("bootstrap uses authoritative mobile config", '@GET("config/mobile")' in text("core/network/src/main/java/app/voicecloud/core/network/BootstrapApi.kt"))

route = text("feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt")
ok("bootstrap surfaces loading maintenance update ready error", all(x in route for x in ["BootstrapState.Loading", "BootstrapState.Maintenance", "BootstrapState.ForceUpdate", "BootstrapState.Ready", "BootstrapState.Error"]))
vm = text("feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapViewModel.kt")
ok("version policy uses actual Android BuildConfig identity", "appIdentity.versionName" in vm and "BuildVersion" not in vm)

prefs = text("core/preferences/src/main/java/app/voicecloud/core/preferences/VoiceCloudPreferences.kt")
ok("DataStore appearance foundation defaults to Light", "ThemePreference.LIGHT" in prefs and "preferencesDataStore" in prefs)
logger = text("core/logging/src/main/java/app/voicecloud/core/logging/VoiceCloudLogger.kt")
ok("logging foundation redacts credentials", "[REDACTED]" in logger and "Bearer" in logger and "password" in logger)

api = json.loads(text("contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json"))
ok("API contract is finalized A0-ready backend authority", api["authority"]["commit"] == "7bee8d463786d21a7200b5663a499408bb229115" and api["authority"].get("androidReadinessRevision") == "VC-ANDROID-A0-R01" and api["backendOperations"] == 700)
ok("Gradle wrapper authority is pinned to 9.5.0", "gradle-9.5.0-bin.zip" in text("gradle/wrapper/gradle-wrapper.properties"))
sock = json.loads(text("contracts/realtime/VC-ANDROID-R06-SOCKET-EVENTS-R01.json"))
ok("Socket event manifest has authoritative events", len(sock["inboundSubscribeMessages"]) >= 50)
screens = json.loads(text("contracts/screens/VC-ANDROID-R06-SCREEN-PARITY-R01.json"))
ok("screen parity locks 74 Website pages", screens["counts"]["websitePageSources"] == 74)
ok("screen parity locks 22 Creator pages", screens["counts"]["creatorPageSources"] == 22)
ok("screen parity locks 84 Website designs", screens["counts"]["approvedWebsiteDesigns"] == 84)
ok("PH01 contains no consumer/creator business feature module", not any((root / "feature").glob("consumer*")) and not any((root / "feature").glob("creator*")))

print(f"VC-ANDROID-PH01-R03 source authority: {len(checks)}/{len(checks)} PASS")
