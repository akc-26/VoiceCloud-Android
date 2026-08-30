from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
MANIFEST=ROOT/'contracts/VC-ANDROID-PH13-R16-PH13-R15-PRODUCT-CANONICAL.sha256'
ALLOWED=set((ROOT/'contracts/VC-ANDROID-PH13-R16-ALLOWED-PRESENTATION-CHANGES.txt').read_text(encoding='utf-8').splitlines())
GEN_DIRS={'.git','.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__'}
GEN_FILES={'local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties','.DS_Store'}
GEN_SUFFIX={'.apk','.aab','.pyc','.pyo','.iml','.hprof'}
ROOM='core/database/schemas/app.voicecloud.core.database.VoiceCloudDatabase/'
def product(rel): return rel.startswith(('app/','branding/','core/','feature/','gradle/')) or rel in {'build.gradle.kts','settings.gradle.kts','gradle.properties'}
def generated(p):
 relp=p.relative_to(ROOT); rel=relp.as_posix()
 return any(x in GEN_DIRS for x in relp.parts) or p.name in GEN_FILES or p.suffix.lower() in GEN_SUFFIX or (rel.startswith(ROOM) and rel.endswith('.json'))
base={}
for line in MANIFEST.read_text(encoding='utf-8').splitlines():
 if line.strip():
  h,r=line.split('  ',1); base[r]=h
fail=[]; changed=[]; protected=0
for rel,expected in base.items():
 p=ROOT/rel
 if not p.is_file(): fail.append(f'missing R15 product input: {rel}'); continue
 actual=hashlib.sha256(p.read_bytes()).hexdigest()
 if rel in ALLOWED:
  if actual!=expected: changed.append(rel)
 else:
  protected+=1
  if actual!=expected: fail.append(f'protected R15 product/runtime/build input drifted: {rel}')
new=[]
for p in ROOT.rglob('*'):
 if not p.is_file() or generated(p): continue
 rel=p.relative_to(ROOT).as_posix()
 if product(rel) and rel not in base:
  if rel not in ALLOWED: fail.append(f'unapproved new R16 product/build input: {rel}')
  else: new.append(rel)
expected_changed={r for r in ALLOWED if r in base}
observed_changed=set(changed)
if observed_changed != expected_changed:
 missing=expected_changed-observed_changed
 extra=observed_changed-expected_changed
 if missing: fail.append('approved R16 existing presentation files did not differ from R15 as expected: '+', '.join(sorted(missing)))
 if extra: fail.append('unexpected changed baseline files: '+', '.join(sorted(extra)))
expected_new={r for r in ALLOWED if r not in base}
if set(new)!=expected_new:
 fail.append('R16 approved new product files mismatch. observed='+repr(sorted(new))+' expected='+repr(sorted(expected_new)))
if fail:
 [print('[FAIL]',x) for x in fail]; raise SystemExit(1)
print(f'[PASS] R16 preserves {protected} R15 functional/runtime/build inputs byte-for-byte')
print(f'[PASS] R16 product drift is restricted to {len(ALLOWED)} approved presentation/navigation-fidelity files/assets')
print('[PASS] API/repository/ViewModel/model/RTC/security/payment/business authority remains protected')
