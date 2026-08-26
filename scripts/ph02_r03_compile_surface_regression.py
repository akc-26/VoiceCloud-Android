from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
checks = []

def read(rel: str) -> str:
    p = ROOT / rel
    if not p.is_file():
        raise SystemExit(f"[FAIL] required source missing: {rel}")
    return p.read_text(encoding="utf-8")

def check(name: str, condition: bool):
    if not condition:
        raise SystemExit(f"[FAIL] {name}")
    checks.append(name)
    print(f"[PASS] {name}")

prefs = read("core/preferences/src/main/java/app/voicecloud/core/preferences/VoiceCloudPreferences.kt")
prefs_build = read("core/preferences/build.gradle.kts")
auth_ui = read("feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt")
auth_build = read("feature/auth/build.gradle.kts")
app_build = read("app/build.gradle.kts")
app_nav = read("app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt")
bootstrap_build = read("feature/bootstrap/build.gradle.kts")
bootstrap_route = read("feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt")
realtime_build = read("core/realtime/build.gradle.kts")
database_build = read("core/database/build.gradle.kts")
catalog = read("gradle/libs.versions.toml")
root_build = read("build.gradle.kts")
accept = read("scripts/VC-ANDROID-PH02-R03-ACCEPTANCE.cmd")

# Workstation failure 2026-08-26: DataStore Preferences leaked through inferred expression-body return types.
for method in ["setTheme", "setLastPortal", "setSessionMetadata", "markOnboardingCompleted", "clearAccountPreferences"]:
    check(f"Preferences mutation {method} uses block body", re.search(rf"suspend\s+fun\s+{method}\([^)]*\)\s*\{{", prefs) is not None)
check("DataStore remains an internal implementation dependency", "implementation(libs.androidx.datastore.preferences)" in prefs_build and "api(libs.androidx.datastore.preferences)" not in prefs_build)
check("Preferences mutation API no longer infers DataStore Preferences return type", "= context.voiceCloudDataStore.edit" not in prefs)

# Workstation failure 2026-08-26: Modifier.align used outside a ColumnScope receiver.
check("TextAction no longer uses scope-only Modifier.align", "Modifier.align(Alignment.CenterHorizontally)" not in auth_ui)
check("TextAction uses scope-independent full-width centering", 'TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth())' in auth_ui)

# Direct dependency + public ABI hardening so the next compiler stage cannot expose another hidden classpath gap.
check("feature auth declares Activity Compose directly", "implementation(libs.androidx.activity.compose)" in auth_build)
check("feature auth does not rely on unused Navigation Compose transitively", "implementation(libs.androidx.navigation.compose)" not in auth_build)
check("feature auth exports Retrofit used by its public API", "api(libs.retrofit.core)" in auth_build)
check("feature auth exports coroutine Flow/StateFlow ABI", "api(libs.kotlinx.coroutines.android)" in auth_build)
check("app declares lifecycle-runtime-compose directly", "implementation(libs.androidx.lifecycle.runtime.compose)" in app_build)
check("bootstrap exports coroutine StateFlow ABI", "api(libs.kotlinx.coroutines.android)" in bootstrap_build)
check("realtime exports secure-token public ABI", 'api(project(":core:security"))' in realtime_build)
check("realtime exports coroutine StateFlow public ABI", "api(libs.kotlinx.coroutines.android)" in realtime_build)

# Hilt 1.3 moved hiltViewModel to lifecycle-viewmodel-compose; old package produced warnings on R02.
check("Hilt lifecycle ViewModel Compose artifact declared", "androidx-hilt-lifecycle-viewmodel-compose" in catalog)
check("app consumes Hilt lifecycle ViewModel Compose artifact", "implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)" in app_build)
check("bootstrap consumes Hilt lifecycle ViewModel Compose artifact", "implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)" in bootstrap_build)
check("app uses non-deprecated hiltViewModel package", "import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel" in app_nav)
check("bootstrap uses non-deprecated hiltViewModel package", "import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel" in bootstrap_route)
check("deprecated hiltViewModel import removed", "import androidx.hilt.navigation.compose.hiltViewModel" not in app_nav + bootstrap_route)

# Room warning is made durable with schema export configuration rather than suppressing it.
check("Room Gradle plugin declared", 'room = { id = "androidx.room", version.ref = "room" }' in catalog)
check("Room Gradle plugin registered at root", "alias(libs.plugins.room) apply false" in root_build)
check("database module applies Room Gradle plugin", "alias(libs.plugins.room)" in database_build)
check("Room schema directory configured", 'schemaDirectory("$projectDir/schemas")' in database_build)
check("Room schema directory retained in source", (ROOT / "core/database/schemas").is_dir())

# One failed workstation run should reveal the full variant compile surface instead of stopping at debug only.
compile_line = next((line for line in accept.splitlines() if ":app:compileDebugKotlin" in line and ":app:compileStagingKotlin" in line), "")
check("acceptance compile sweep includes debug staging release", all(task in compile_line for task in [":app:compileDebugKotlin", ":app:compileStagingKotlin", ":app:compileReleaseKotlin"]))
check("acceptance compile sweep continues to expose all variant failures", "--continue" in compile_line)

print(f"VC-ANDROID-PH02-R03 compile-surface regression: {len(checks)}/{len(checks)} PASS")
