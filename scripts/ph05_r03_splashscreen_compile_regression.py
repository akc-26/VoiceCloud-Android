from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
main = (ROOT / "app/src/main/java/app/voicecloud/android/MainActivity.kt").read_text(encoding="utf-8")
app = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
versions = (ROOT / "gradle/libs.versions.toml").read_text(encoding="utf-8")
checks = []

def ck(name, ok):
    checks.append((name, bool(ok)))
    print(f"[{'PASS' if ok else 'FAIL'}] {name}")

ck("SplashScreen dependency remains on app compile classpath", "implementation(libs.androidx.core.splashscreen)" in app)
ck("AndroidX Core SplashScreen 1.2.0 remains pinned", 'coreSplashscreen = "1.2.0"' in versions)
ck("correct Kotlin companion extension import retained", "import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen" in main)
ck("invalid package-level splash import removed", "import androidx.core.splashscreen.installSplashScreen" not in main)
ck("splash API called before super.onCreate", main.index("installSplashScreen()") < main.index("super.onCreate(savedInstanceState)"))
ck("MainActivity remains ComponentActivity", "class MainActivity : ComponentActivity()" in main)

failed = [name for name, ok in checks if not ok]
print(f"VC-ANDROID-PH05-R03 SplashScreen compile regression: {len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    sys.exit(1)
