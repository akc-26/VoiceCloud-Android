from pathlib import Path
import shutil, subprocess, sys, tempfile
ROOT=Path(__file__).resolve().parents[1]
EXACT=[
 'local.properties','gradle/gradle-daemon-jvm.properties','.gradle/9.5.0/gc.properties',
 '.gradle/buildOutputCleanup/buildOutputCleanup.lock','.gradle/buildOutputCleanup/cache.properties',
 '.gradle/vcs-1/gc.properties','.gradle/9.5.0/checksums/checksums.lock',
 '.gradle/9.5.0/executionHistory/executionHistory.lock','.gradle/9.5.0/fileChanges/last-build.bin',
 '.gradle/9.5.0/fileHashes/fileHashes.bin','.gradle/9.5.0/fileHashes/fileHashes.lock',
 '.gradle/9.5.0/fileHashes/resourceHashesCache.bin']
EXTRA=['secrets.properties','gradlew','gradlew.bat','gradle/wrapper/gradle-wrapper.jar','.idea/workspace.xml','.kotlin/cache.bin','app/build/outputs/apk/debug/app-debug.apk','__pycache__/probe.pyc','temp.iml','heap.hprof']
def write(root,rel):
 p=root/rel; p.parent.mkdir(parents=True,exist_ok=True); p.write_bytes(b'generated\n')
def run(root,name): return subprocess.run([sys.executable,str(root/'scripts'/name)],cwd=root,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
with tempfile.TemporaryDirectory(prefix='vc-r13-contamination-') as td:
 t=Path(td)/'pkg'; shutil.copytree(ROOT,t,ignore=shutil.ignore_patterns('.gradle','.idea','.kotlin','build','__pycache__','*.pyc'))
 for rel in EXACT+EXTRA: write(t,rel)
 a=run(t,'ph13_r13_product_preservation_regression.py'); b=run(t,'ph13_r13_delivery_integrity.py')
 if a.returncode!=0: print(a.stdout); raise SystemExit('[FAIL] R13 preservation rejected known workstation artifacts')
 if b.returncode!=0: print(b.stdout); raise SystemExit('[FAIL] R13 integrity rejected known workstation artifacts')
 u=t/'unexpected-product-drift.txt'; u.write_text('must fail closed\n')
 a=run(t,'ph13_r13_product_preservation_regression.py'); b=run(t,'ph13_r13_delivery_integrity.py')
 if a.returncode==0: raise SystemExit('[FAIL] R13 preservation did not fail closed on unknown drift')
 if b.returncode==0: raise SystemExit('[FAIL] R13 integrity did not fail closed on unknown drift')
print(f'[PASS] R13 workstation artifact simulation accepted: {len(EXACT)+len(EXTRA)} known generated artifacts')
print('[PASS] R13 preservation and integrity remain fail-closed for unknown drift')
