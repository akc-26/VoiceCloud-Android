from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
PARENT = ROOT / 'contracts/VC-ANDROID-PH13-R13-PH13-R12-PARENT-CANONICAL.sha256'
RUNTIME = ROOT / 'contracts/VC-ANDROID-PH13-R13-R09-RUNTIME-CANONICAL.sha256'
TEST_REL = 'app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt'

IGNORED_GENERATED_DIRS = {'.git', '.gradle', '.idea', '.kotlin', 'build', 'captures', '.externalNativeBuild', '.cxx', '__pycache__'}
IGNORED_GENERATED_FILES = {'local.properties', 'secrets.properties', '.DS_Store', 'gradlew', 'gradlew.bat', 'gradle-wrapper.jar', 'gradle-daemon-jvm.properties'}
IGNORED_SUFFIXES = {'.iml', '.apk', '.aab', '.pyc', '.pyo', '.hprof'}

if not PARENT.is_file(): raise SystemExit('[FAIL] R12 parent canonical manifest missing')
if not RUNTIME.is_file(): raise SystemExit('[FAIL] build-proven R09 runtime canonical manifest missing')

def load_manifest(path):
    out={}
    for line in path.read_text(encoding='utf-8').splitlines():
        if not line.strip(): continue
        h, rel=line.split('  ',1); out[rel]=h
    return out

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def is_generated(path):
    rel=path.relative_to(ROOT)
    return any(part in IGNORED_GENERATED_DIRS for part in rel.parts) or path.name in IGNORED_GENERATED_FILES or path.suffix.lower() in IGNORED_SUFFIXES

fail=[]
runtime=load_manifest(RUNTIME)
for rel, expected in runtime.items():
    p=ROOT/rel
    if not p.is_file(): fail.append(f'missing build-proven runtime file: {rel}'); continue
    if sha(p)!=expected: fail.append(f'R09 build-proven runtime drifted: {rel}')

parent=load_manifest(PARENT)
allowed_parent_drift={'README.md','CHANGELOG.md',TEST_REL}
for rel, expected in parent.items():
    p=ROOT/rel
    if not p.is_file(): fail.append(f'missing R12 tracked file: {rel}'); continue
    if sha(p)!=expected and rel not in allowed_parent_drift: fail.append(f'unexpected R12 tracked-file drift: {rel}')

# Require the exact intended androidTest correction and no production Kotlin drift.
test=(ROOT/TEST_REL).read_text(encoding='utf-8')
if '0xFF18312D.toInt(), ConsumerColors.Text.toArgb()' not in test: fail.append('R13 text assertion is not aligned to approved premium branding')
if '0xFF006C63.toInt(), ConsumerColors.Sapphire.toArgb()' not in test: fail.append('R13 primary assertion is not aligned to approved premium branding')
if '0xFF10262E.toInt()' in test or '0xFF0B7C86.toInt()' in test: fail.append('legacy pre-redesign palette assertion remains')

allowed_inherited_unmanifested={'contracts/VC-ANDROID-PH13-R12-SOURCE-MANIFEST.sha256'}
new_r13_prefixes=('scripts/VC-ANDROID-PH13-R13-','scripts/ph13_r13_','contracts/VC-ANDROID-PH13-R13-','docs/VC-ANDROID-PH13-R13-')
new_r13_exact={'VC-ANDROID-PH13-R13-REVISION.txt'}

parent_kt={r for r in parent if r.endswith('.kt')}
for p in ROOT.rglob('*.kt'):
    if is_generated(p): continue
    rel=p.relative_to(ROOT).as_posix()
    if rel not in parent_kt: fail.append(f'unexpected new Kotlin source: {rel}')
    elif rel != TEST_REL and sha(p) != parent[rel]: fail.append(f'unexpected Kotlin product drift: {rel}')

for p in ROOT.rglob('*'):
    if not p.is_file(): continue
    rel=p.relative_to(ROOT).as_posix()
    if is_generated(p): continue
    if rel in parent or rel in allowed_parent_drift or rel in allowed_inherited_unmanifested or rel.endswith('.kt'): continue
    if rel.startswith(new_r13_prefixes) or rel in new_r13_exact: continue
    if rel == 'contracts/VC-ANDROID-PH13-R13-SOURCE-MANIFEST.sha256': continue
    fail.append(f'unexpected new non-product file outside R13 authority: {rel}')

if fail:
    for x in fail: print('[FAIL]',x)
    raise SystemExit(1)
print(f'[PASS] R13 preserves {len(runtime)} build-proven R09 production/runtime inputs byte-for-byte')
print('[PASS] R13 Android production app/UI/API/runtime Kotlin source is unchanged from R12/R09')
print('[PASS] The only Kotlin drift from R12 is the stale foundation androidTest palette expectation correction')
