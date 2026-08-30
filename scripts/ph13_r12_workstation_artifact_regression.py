from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]

EXACT_R11_LOG_ARTIFACTS = [
    'local.properties',
    'gradle/gradle-daemon-jvm.properties',
    '.gradle/9.5.0/gc.properties',
    '.gradle/buildOutputCleanup/buildOutputCleanup.lock',
    '.gradle/buildOutputCleanup/cache.properties',
    '.gradle/vcs-1/gc.properties',
    '.gradle/9.5.0/checksums/checksums.lock',
    '.gradle/9.5.0/executionHistory/executionHistory.lock',
    '.gradle/9.5.0/fileChanges/last-build.bin',
    '.gradle/9.5.0/fileHashes/fileHashes.bin',
    '.gradle/9.5.0/fileHashes/fileHashes.lock',
    '.gradle/9.5.0/fileHashes/resourceHashesCache.bin',
]
EXTRA_GENERATED = [
    'secrets.properties', 'gradlew', 'gradlew.bat',
    'gradle/wrapper/gradle-wrapper.jar', '.idea/workspace.xml',
    '.kotlin/cache.bin', 'app/build/outputs/apk/debug/app-debug.apk',
    '__pycache__/probe.pyc', 'temp.iml', 'heap.hprof',
]

def write_artifact(root: Path, rel: str):
    p = root / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_bytes(b'generated-workstation-artifact\n')

def run(root: Path, script: str):
    return subprocess.run(
        [sys.executable, str(root / 'scripts' / script)],
        cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
    )

with tempfile.TemporaryDirectory(prefix='vc-r12-contamination-') as td:
    test_root = Path(td) / 'pkg'
    shutil.copytree(ROOT, test_root, ignore=shutil.ignore_patterns('.gradle', '.idea', '.kotlin', 'build', '__pycache__', '*.pyc'))
    for rel in EXACT_R11_LOG_ARTIFACTS + EXTRA_GENERATED:
        write_artifact(test_root, rel)

    preservation = run(test_root, 'ph13_r12_product_preservation_regression.py')
    if preservation.returncode != 0:
        print(preservation.stdout)
        raise SystemExit('[FAIL] R12 preservation rejected known workstation-generated artifacts')
    integrity = run(test_root, 'ph13_r12_delivery_integrity.py')
    if integrity.returncode != 0:
        print(integrity.stdout)
        raise SystemExit('[FAIL] R12 integrity rejected known workstation-generated artifacts')

    unknown = test_root / 'unexpected-product-drift.txt'
    unknown.write_text('must fail closed\n', encoding='utf-8')
    preservation_unknown = run(test_root, 'ph13_r12_product_preservation_regression.py')
    integrity_unknown = run(test_root, 'ph13_r12_delivery_integrity.py')
    if preservation_unknown.returncode == 0:
        raise SystemExit('[FAIL] R12 preservation did not fail closed on unknown untracked file')
    if integrity_unknown.returncode == 0:
        raise SystemExit('[FAIL] R12 integrity did not fail closed on unknown unmanifested file')

print(f'[PASS] R12 exact R11 workstation-artifact simulation accepted: {len(EXACT_R11_LOG_ARTIFACTS)} reported artifacts')
print(f'[PASS] R12 extended generated-artifact simulation accepted: {len(EXTRA_GENERATED)} additional common artifacts')
print('[PASS] R12 preservation and integrity remain fail-closed for unknown product/delivery drift')
