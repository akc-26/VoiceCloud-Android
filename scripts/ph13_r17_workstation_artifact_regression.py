from pathlib import Path
import tempfile, shutil, subprocess, sys, os
ROOT=Path(__file__).resolve().parents[1]
ARTIFACTS=[
 'local.properties','.gradle/test/cache.bin','.idea/workspace.xml','.kotlin/session.bin','app/build/tmp.bin',
 'core/database/schemas/app.voicecloud.core.database.VoiceCloudDatabase/1.json','gradle/gradle-daemon-jvm.properties',
 'feature/auth/build/tmp.txt','captures/a.txt','.cxx/cache','app/debug.apk','app/release.aab','module.iml','x.pyc',
 '.externalNativeBuild/x','secrets.properties','.DS_Store','gradlew','gradlew.bat','gradle/wrapper/gradle-wrapper.jar',
 'dump.hprof','__pycache__/x.pyc','build/root.tmp']
env=os.environ.copy(); env['PYTHONUTF8']='1'; env['PYTHONIOENCODING']='utf-8'
with tempfile.TemporaryDirectory(prefix='vc-r17-artifacts-') as td:
 dst=Path(td)/'pkg'; shutil.copytree(ROOT,dst,ignore=shutil.ignore_patterns('.gradle','.idea','.kotlin','build','__pycache__'))
 for rel in ARTIFACTS:
  p=dst/rel; p.parent.mkdir(parents=True,exist_ok=True); p.write_text('{}\n' if rel.endswith('.json') else 'generated\n',encoding='utf-8')
 for script in ['ph13_r17_product_preservation_regression.py','ph13_r17_delivery_integrity.py','ph13_r17_source_hygiene.py']:
  rc=subprocess.run([sys.executable,str(dst/'scripts'/script)],cwd=dst,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf-8',env=env)
  if rc.returncode:
   print(rc.stdout); raise SystemExit(f'[FAIL] generated-artifact simulation failed in {script}')
 print(f'[PASS] R17 workstation/generated-artifact simulation accepted: {len(ARTIFACTS)} artifacts including exact Room schema JSON')
 bad=dst/'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/UnexpectedDrift.kt'; bad.write_text('package bad\n',encoding='utf-8')
 failed=0
 for script in ['ph13_r17_product_preservation_regression.py','ph13_r17_delivery_integrity.py']:
  rc=subprocess.run([sys.executable,str(dst/'scripts'/script)],cwd=dst,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf-8',env=env)
  if rc.returncode!=0: failed+=1
 if failed!=2: raise SystemExit('[FAIL] R17 gates did not remain fail-closed for unknown product/source drift')
 print('[PASS] R17 preservation and delivery integrity remain fail-closed for unknown product/source drift')
