from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / 'contracts/VC-ANDROID-PH13-R06-SOURCE-MANIFEST.sha256'

# Acceptance runs in the user's extracted working copy. Android Studio and Gradle
# may legitimately create these machine-local artifacts before the acceptance
# command is invoked. They are not product source and therefore do not invalidate
# the immutable tracked-source manifest.
IGNORED_GENERATED_DIRS = {
    '.git', '.gradle', '.idea', '.kotlin', 'build', 'captures',
    '.externalNativeBuild', '.cxx', '__pycache__'
}
IGNORED_GENERATED_FILES = {
    'local.properties', 'secrets.properties', '.DS_Store', 'gradlew', 'gradlew.bat',
    'gradle-wrapper.jar', 'gradle-daemon-jvm.properties'
}
IGNORED_SUFFIXES = {'.iml', '.apk', '.aab', '.pyc', '.pyo', '.hprof'}

if not MANIFEST.is_file():
    raise SystemExit('[FAIL] PH13-R06 source manifest missing')

tracked = {}
for line in MANIFEST.read_text(encoding='utf-8').splitlines():
    if not line.strip():
        continue
    expected, rel = line.split('  ', 1)
    tracked[rel] = expected
    p = ROOT / rel
    if not p.is_file():
        raise SystemExit(f'[FAIL] PH13-R06 delivery file missing: {rel}')
    actual = hashlib.sha256(p.read_bytes()).hexdigest()
    if actual != expected:
        raise SystemExit(f'[FAIL] PH13-R06 delivery file drifted: {rel}')

manifest_rel = MANIFEST.relative_to(ROOT).as_posix()
ignored = []
for p in ROOT.rglob('*'):
    rel_path = p.relative_to(ROOT)
    rel = rel_path.as_posix()
    generated_dir = any(part in IGNORED_GENERATED_DIRS for part in rel_path.parts)
    if generated_dir:
        if p.is_file():
            ignored.append(rel)
        continue
    if not p.is_file():
        continue
    if p.name in IGNORED_GENERATED_FILES or p.suffix.lower() in IGNORED_SUFFIXES:
        ignored.append(rel)
        continue
    if rel not in tracked and rel != manifest_rel:
        raise SystemExit(f'[FAIL] unmanifested product/delivery file present: {rel}')

print(f'[PASS] PH13-R06 tracked delivery manifest verified: {len(tracked)} files')
if ignored:
    print(f'[PASS] Ignored {len(ignored)} known workstation-generated artifact(s); product-source integrity remains unchanged')
print('[PASS] PH13-R06 product/delivery integrity is clean')
