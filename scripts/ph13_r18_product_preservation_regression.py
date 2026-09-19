from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
MANIFEST=ROOT/'contracts/VC-ANDROID-PH13-R18-PH13-R17-PRODUCT-CANONICAL.sha256'
ALLOWED=set((ROOT/'contracts/VC-ANDROID-PH13-R18-ALLOWED-PRODUCT-CHANGES.txt').read_text(encoding='utf-8').splitlines())
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
fail=[];changed=[];protected=0
for rel,expected in base.items():
 p=ROOT/rel
 if not p.is_file(): fail.append(f'missing R17 product input: {rel}'); continue
 actual=hashlib.sha256(p.read_bytes()).hexdigest()
 if rel in ALLOWED:
  if actual!=expected: changed.append(rel)
 else:
  protected+=1
  if actual!=expected: fail.append(f'protected R17 product/runtime/build input drifted: {rel}')
new=[]
for p in ROOT.rglob('*'):
 if not p.is_file() or generated(p): continue
 rel=p.relative_to(ROOT).as_posix()
 if product(rel) and rel not in base:
  if rel not in ALLOWED: fail.append(f'unapproved new R18 product/build input: {rel}')
  else: new.append(rel)
expected_changed={r for r in ALLOWED if r in base}; expected_new={r for r in ALLOWED if r not in base}
if set(changed)!=expected_changed: fail.append(f'R18 changed product set mismatch observed={sorted(changed)!r} expected={sorted(expected_changed)!r}')
if set(new)!=expected_new: fail.append(f'R18 new product set mismatch observed={sorted(new)!r} expected={sorted(expected_new)!r}')
if fail:
 for x in fail: print('[FAIL]',x)
 raise SystemExit(1)
print(f'[PASS] R18 preserves {protected} R17 product/runtime/build inputs byte-for-byte')
print(f'[PASS] R18 product changes are restricted to {len(ALLOWED)} compiler-closure presentation files')
print('[PASS] Approved R16 UI/UX plus R17 API/repository/ViewModel/model/navigation/RTC/security/payment/business authority remains protected')
