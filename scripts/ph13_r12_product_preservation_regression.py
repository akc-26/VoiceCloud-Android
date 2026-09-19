from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
PARENT = ROOT / 'contracts/VC-ANDROID-PH13-R12-PH13-R11-PARENT-CANONICAL.sha256'
PRODUCT = ROOT / 'contracts/VC-ANDROID-PH13-R12-R09-PRODUCT-CANONICAL.sha256'

IGNORED_GENERATED_DIRS = {'.git', '.gradle', '.idea', '.kotlin', 'build', 'captures', '.externalNativeBuild', '.cxx', '__pycache__'}
IGNORED_GENERATED_FILES = {'local.properties', 'secrets.properties', '.DS_Store', 'gradlew', 'gradlew.bat', 'gradle-wrapper.jar', 'gradle-daemon-jvm.properties'}
IGNORED_SUFFIXES = {'.iml', '.apk', '.aab', '.pyc', '.pyo', '.hprof'}

if not PARENT.is_file():
    raise SystemExit('[FAIL] R11 parent canonical manifest missing')
if not PRODUCT.is_file():
    raise SystemExit('[FAIL] R09 product canonical manifest missing')

allowed_parent_drift = {'CHANGELOG.md', 'README.md'}
allowed_inherited_unmanifested = {'contracts/VC-ANDROID-PH13-R11-SOURCE-MANIFEST.sha256'}
new_r12_prefixes = (
    'scripts/VC-ANDROID-PH13-R12-',
    'scripts/ph13_r12_',
    'contracts/VC-ANDROID-PH13-R12-',
    'docs/VC-ANDROID-PH13-R12-',
)
new_r12_exact = {'VC-ANDROID-PH13-R12-REVISION.txt'}

def is_generated(path: Path) -> bool:
    rel = path.relative_to(ROOT)
    if any(part in IGNORED_GENERATED_DIRS for part in rel.parts):
        return True
    return path.name in IGNORED_GENERATED_FILES or path.suffix.lower() in IGNORED_SUFFIXES

def load_manifest(path: Path):
    result = {}
    for line in path.read_text(encoding='utf-8').splitlines():
        if not line.strip():
            continue
        expected, rel = line.split('  ', 1)
        result[rel] = expected
    return result

fail = []
product = load_manifest(PRODUCT)
for rel, expected in product.items():
    p = ROOT / rel
    if not p.is_file():
        fail.append(f'missing R09 product file: {rel}')
        continue
    actual = hashlib.sha256(p.read_bytes()).hexdigest()
    if actual != expected:
        fail.append(f'build-proven R09 Android product/build input drifted: {rel}')

parent = load_manifest(PARENT)
for rel, expected in parent.items():
    p = ROOT / rel
    if not p.is_file():
        fail.append(f'missing R11 tracked file: {rel}')
        continue
    actual = hashlib.sha256(p.read_bytes()).hexdigest()
    if actual != expected and rel not in allowed_parent_drift:
        fail.append(f'unexpected R11 tracked-file drift: {rel}')

parent_kt = {r for r in parent if r.endswith('.kt')}
for p in ROOT.rglob('*.kt'):
    if is_generated(p):
        continue
    rel = p.relative_to(ROOT).as_posix()
    if rel not in parent_kt:
        fail.append(f'unexpected new Kotlin product source: {rel}')

ignored_generated = []
for p in ROOT.rglob('*'):
    if not p.is_file():
        continue
    rel = p.relative_to(ROOT).as_posix()
    if is_generated(p):
        ignored_generated.append(rel)
        continue
    if rel in parent or rel in allowed_parent_drift or rel in allowed_inherited_unmanifested or rel.endswith('.kt'):
        continue
    if rel.startswith(new_r12_prefixes) or rel in new_r12_exact:
        continue
    if rel == 'contracts/VC-ANDROID-PH13-R12-SOURCE-MANIFEST.sha256':
        continue
    fail.append(f'unexpected new non-product file outside R12 authority: {rel}')

if fail:
    for item in fail:
        print('[FAIL]', item)
    raise SystemExit(1)

print(f'[PASS] R12 preserves {len(product)} build-proven R09 Android product/build-input files byte-for-byte')
print('[PASS] R12 changes are acceptance-harness only; app/UI/API/runtime source is unchanged')
print('[PASS] No new Kotlin product source introduced in R12')
if ignored_generated:
    print(f'[PASS] R12 preservation ignored {len(ignored_generated)} known workstation-generated artifact(s) consistently with delivery integrity')
