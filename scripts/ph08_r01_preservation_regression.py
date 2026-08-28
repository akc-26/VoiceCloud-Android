from pathlib import Path
import hashlib
import sys

ROOT = Path(__file__).resolve().parents[1]
manifest = ROOT / "contracts/VC-ANDROID-PH08-PRESERVATION-MANIFEST.sha256"
count = 0
for line in manifest.read_text(encoding="utf-8").splitlines():
    expected, rel = line.split("  ", 1)
    path = ROOT / rel
    if not path.exists():
        print("[FAIL] missing PH07-preserved file", rel)
        sys.exit(1)
    actual = hashlib.sha256(path.read_bytes()).hexdigest()
    if actual != expected:
        print("[FAIL] PH07-preserved source changed", rel)
        sys.exit(1)
    count += 1

nav = (ROOT / "app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt").read_text(encoding="utf-8")
discovery = (ROOT / "feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt").read_text(encoding="utf-8")
checks = {
    "PH06 host graph preserved": all(route in nav for route in ["HostStudio", "HostRoomCreate", "HostLiveConsole", "HostInteractive"]),
    "PH05 live listener graph preserved": all(route in nav for route in ["RoomPreview", "RoomExperience"]),
    "PH07 economy graph preserved": "const val Economy = \"economy\"" in nav and "composable(VoiceCloudRoutes.Economy)" in nav,
    "consumer My Profile preserved": "fun MyProfileScreen(" in discovery,
    "PH08 augments rather than replaces My Profile": "Profile & activity" in discovery and "Economy & progression" in discovery,
}
for name, ok in checks.items():
    if not ok:
        print("[FAIL]", name)
        sys.exit(1)
    print("[PASS]", name)
print(f"[PASS] {count} inherited PH01-PH07 Kotlin source files match the PH07-R03 baseline")
