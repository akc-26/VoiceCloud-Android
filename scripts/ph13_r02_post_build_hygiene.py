from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "contracts/VC-ANDROID-PH13-R02-SOURCE-MANIFEST.sha256"
ALLOWED_GENERATED_DIRS = {
    ".gradle", ".idea", ".kotlin", "build", "captures",
    ".externalNativeBuild", ".cxx", "__pycache__",
}
ALLOWED_GENERATED_FILES = {
    "local.properties", "secrets.properties", ".DS_Store",
    "gradlew", "gradlew.bat", "gradle-wrapper.jar",
}
ALLOWED_GENERATED_SUFFIXES = {".iml", ".apk", ".aab", ".pyc", ".pyo", ".hprof"}

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
        raise SystemExit(f"[FAIL] PH13-R02 tracked source file missing after build: {rel}")
    actual = hashlib.sha256(p.read_bytes()).hexdigest()
    if actual != expected:
        raise SystemExit(f"[FAIL] PH13-R02 tracked source drifted after build: {rel}")

manifest_rel = MANIFEST.relative_to(ROOT).as_posix()
for p in ROOT.rglob("*"):
    if not p.is_file():
        continue
    rel_path = p.relative_to(ROOT)
    rel = rel_path.as_posix()
    if rel in tracked or rel == manifest_rel:
        continue
    if any(part in ALLOWED_GENERATED_DIRS for part in rel_path.parts):
        continue
    if p.name in ALLOWED_GENERATED_FILES or p.suffix.lower() in ALLOWED_GENERATED_SUFFIXES:
        continue
    raise SystemExit(f"[FAIL] unexpected non-generated file appeared after build: {rel}")

print(f"[PASS] PH13-R02 post-build tracked-source integrity: {len(tracked)} manifest files unchanged")
print("[PASS] PH13-R02 post-build workspace contains only manifest source plus explicitly allowed generated artifacts")
