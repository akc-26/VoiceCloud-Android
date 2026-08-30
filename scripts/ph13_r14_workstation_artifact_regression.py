from pathlib import Path
import os, shutil, subprocess, sys, tempfile

ROOT = Path(__file__).resolve().parents[1]

KNOWN_FILES = {
    'local.properties': 'sdk.dir=C:\\\\Users\\\\acceptance\\\\AppData\\\\Local\\\\Android\\\\Sdk\n',
    'gradle/gradle-daemon-jvm.properties': 'toolchainVersion=21\n',
    '.gradle/9.5.0/gc.properties': '',
    '.gradle/buildOutputCleanup/buildOutputCleanup.lock': 'lock',
    '.gradle/buildOutputCleanup/cache.properties': 'gradle.version=9.5.0\n',
    '.gradle/vcs-1/gc.properties': '',
    '.gradle/9.5.0/checksums/checksums.lock': 'lock',
    '.gradle/9.5.0/executionHistory/executionHistory.lock': 'lock',
    '.gradle/9.5.0/fileChanges/last-build.bin': 'binary-ish',
    '.gradle/9.5.0/fileHashes/fileHashes.bin': 'binary-ish',
    '.gradle/9.5.0/fileHashes/fileHashes.lock': 'lock',
    '.gradle/9.5.0/fileHashes/resourceHashesCache.bin': 'binary-ish',
    '.idea/workspace.xml': '<project />\n',
    '.idea/deploymentTargetSelector.xml': '<project />\n',
    '.kotlin/errors/errors-1.log': 'generated\n',
    'app/build/outputs/apk/debug/app-debug.apk': 'fake-apk',
    'core/database/build/tmp/generated.tmp': 'generated',
    'feature/auth/build/intermediates/generated.txt': 'generated\n',
    'capture.hprof': 'heap',
    'app/app-debug.apk': 'apk',
    'module.iml': 'idea',
    '__pycache__/temp.pyc': 'pyc',
    # Exact legitimate Room export that broke R13. Deliberately no final newline.
    'core/database/schemas/app.voicecloud.core.database.VoiceCloudDatabase/1.json': '{"formatVersion":1,"database":{"version":1}}',
}

def write(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(content.encode('utf-8'))

def run(root: Path, script: str, expect: int = 0):
    env = dict(os.environ); env['PYTHONDONTWRITEBYTECODE']='1'
    cp = subprocess.run([sys.executable, str(root/'scripts'/script)], cwd=root, env=env, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if cp.returncode != expect:
        print(cp.stdout)
        raise AssertionError(f'{script} expected exit {expect}, got {cp.returncode}')
    return cp.stdout

with tempfile.TemporaryDirectory(prefix='vc-r14-artifact-regression-') as td:
    tree = Path(td)/'tree'
    shutil.copytree(ROOT, tree, ignore=shutil.ignore_patterns('.git','.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__','*.apk','*.aab','*.pyc','*.pyo','*.iml','*.hprof','local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties'))
    for rel, content in KNOWN_FILES.items(): write(tree/rel, content)

    run(tree, 'ph13_r14_product_preservation_regression.py', 0)
    run(tree, 'ph13_r14_source_hygiene.py', 0)
    run(tree, 'ph13_r14_delivery_integrity.py', 0)
    print(f'[PASS] R14 workstation/generated-artifact simulation accepted: {len(KNOWN_FILES)} artifacts including exact Room schema JSON')

    drift = tree/'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/UnexpectedR14Drift.kt'
    write(drift, 'package app.voicecloud.feature.auth.ui\ninternal object UnexpectedR14Drift\n')
    preservation = subprocess.run([sys.executable, str(tree/'scripts/ph13_r14_product_preservation_regression.py')], cwd=tree, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    delivery = subprocess.run([sys.executable, str(tree/'scripts/ph13_r14_delivery_integrity.py')], cwd=tree, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if preservation.returncode == 0 or delivery.returncode == 0:
        print(preservation.stdout); print(delivery.stdout)
        raise SystemExit('[FAIL] R14 gates did not fail closed for unknown product/delivery drift')
    print('[PASS] R14 preservation and delivery integrity remain fail-closed for unknown product/source drift')
