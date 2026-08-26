from pathlib import Path

root = Path(__file__).resolve().parents[1]
checks = []

def ok(label, condition):
    if not condition:
        raise SystemExit(f"[FAIL] {label}")
    checks.append(label)
    print(f"[PASS] {label}")

def read(rel):
    return (root / rel).read_text(encoding="utf-8")

root_build = read("build.gradle.kts")
catalog = read("gradle/libs.versions.toml")
modules = [root / "app" / "build.gradle.kts", *sorted((root / "core").glob("*/build.gradle.kts")), *sorted((root / "feature").glob("*/build.gradle.kts"))]
module_text = "\n".join(p.read_text(encoding="utf-8") for p in modules)

ok("AGP 9.3.0 retained", 'agp = "9.3.0"' in catalog)
ok("Gradle 9.5.0 retained", "gradle-9.5.0-bin.zip" in read("gradle/wrapper/gradle-wrapper.properties"))
ok("root no longer declares kotlin-android plugin", "libs.plugins.kotlin.android" not in root_build)
ok("version catalog no longer declares org.jetbrains.kotlin.android", "org.jetbrains.kotlin.android" not in catalog)
ok("all Android modules use AGP built-in Kotlin", "libs.plugins.kotlin.android" not in module_text)
ok("legacy android.kotlinOptions DSL removed", "kotlinOptions" not in module_text)
ok("Java 17 compile target retained", module_text.count("JavaVersion.VERSION_17") >= 9)
ok("Compose compiler plugin retained for Compose modules", "libs.plugins.kotlin.compose" in read("app/build.gradle.kts") and "libs.plugins.kotlin.compose" in read("core/designsystem/build.gradle.kts") and "libs.plugins.kotlin.compose" in read("feature/bootstrap/build.gradle.kts"))
ok("KSP retained for generated-code modules", "libs.plugins.ksp" in read("app/build.gradle.kts") and "libs.plugins.ksp" in read("core/database/build.gradle.kts") and "libs.plugins.ksp" in read("feature/bootstrap/build.gradle.kts"))
ok("Hilt retained in app/bootstrap", "libs.plugins.hilt" in read("app/build.gradle.kts") and "libs.plugins.hilt" in read("feature/bootstrap/build.gradle.kts"))
ok("no built-in Kotlin opt-out flag", "android.builtInKotlin=false" not in read("gradle.properties"))
ok("no legacy AGP DSL opt-out flag", "android.newDsl=false" not in read("gradle.properties"))

print(f"VC-ANDROID-PH01-R03 AGP9 built-in Kotlin regression: {len(checks)}/{len(checks)} PASS")
