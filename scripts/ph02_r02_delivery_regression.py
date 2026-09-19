from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
script = (ROOT / "scripts" / "CREATE-CLEAN-PACKAGE.ps1").read_text(encoding="utf-8")
checks = []

def check(name, cond):
    if not cond:
        raise SystemExit(f"[FAIL] {name}")
    checks.append(name)
    print(f"[PASS] {name}")

check("clean-package script retained", "Compress-Archive" in script)
check("package archives staging contents instead of enclosing project folder", "Compress-Archive -Path (Join-Path $stage '*')" in script)
check("package verifies settings.gradle.kts at ZIP root", "settings.gradle.kts is not at ZIP root" in script)
check("package excludes generated Gradle wrapper bootstrap artifacts", all(x in script for x in ["'gradlew'", "'gradlew.bat'", "'gradle-wrapper.jar'"]))
check("package excludes machine/build directories", all(x in script for x in ["'.gradle'", "'.idea'", "'build'", "'.cxx'"]))
print(f"VC-ANDROID-PH02-R02 clean-delivery regression: {len(checks)}/{len(checks)} PASS")
