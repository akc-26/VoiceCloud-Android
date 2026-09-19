from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "contracts/VC-ANDROID-PH13-R02-SOURCE-MANIFEST.sha256"
FORBIDDEN_DIRS = {
    ".git", ".idea", ".gradle", ".kotlin", "build", "captures",
    ".externalNativeBuild", ".cxx", "__pycache__",
}
FORBIDDEN_FILES = {
    "local.properties", "secrets.properties", ".DS_Store",
    "gradlew", "gradlew.bat", "gradle-wrapper.jar",
}
FORBIDDEN_SUFFIXES = {".iml", ".apk", ".aab", ".pyc", ".pyo", ".zip", ".jks", ".keystore"}

if not MANIFEST.is_file():
    raise SystemExit("[FAIL] PH13-R02 source manifest missing")

tracked = set()
for line in MANIFEST.read_text(encoding="utf-8").splitlines():
    if not line.strip():
        continue
    expected, rel = line.split("  ", 1)
    tracked.add(rel)
    p = ROOT / rel
    if not p.is_file():
        raise SystemExit(f"[FAIL] PH13-R02 delivery file missing: {rel}")
    actual = hashlib.sha256(p.read_bytes()).hexdigest()
    if actual != expected:
        raise SystemExit(f"[FAIL] PH13-R02 delivery file drifted: {rel}")

manifest_rel = MANIFEST.relative_to(ROOT).as_posix()
for p in ROOT.rglob("*"):
    rel_path = p.relative_to(ROOT)
    rel = rel_path.as_posix()
    if any(part in FORBIDDEN_DIRS for part in rel_path.parts):
        raise SystemExit(f"[FAIL] forbidden generated directory in delivery package: {rel}")
    if p.is_file():
        if p.name in FORBIDDEN_FILES or p.suffix.lower() in FORBIDDEN_SUFFIXES:
            raise SystemExit(f"[FAIL] forbidden generated/machine file in delivery package: {rel}")
        if rel not in tracked and rel != manifest_rel:
            raise SystemExit(f"[FAIL] unmanifested delivery file present: {rel}")

print(f"[PASS] PH13-R02 delivery manifest verified: {len(tracked)} tracked files")
print("[PASS] PH13-R02 distributable package hygiene is strict and clean")
