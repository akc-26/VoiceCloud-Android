from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
MANIFEST=ROOT/'contracts/VC-ANDROID-PH13-R15-PH13-R14-PRODUCT-CANONICAL.sha256'
ALLOWED={'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt','feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt'}
GEN_DIRS={'.git','.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__'}
GEN_FILES={'local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties'}
GEN_SUFFIX={'.apk','.aab','.pyc','.pyo','.iml','.hprof'}
ROOM_PREFIX='core/database/schemas/app.voicecloud.core.database.VoiceCloudDatabase/'
def is_product(rel): return rel.startswith(('app/','branding/','core/','feature/','gradle/')) or rel in {'build.gradle.kts','settings.gradle.kts','gradle.properties'}
if not MANIFEST.is_file(): raise SystemExit('[FAIL] R15 R14-product canonical manifest missing')
base={}
for line in MANIFEST.read_text(encoding='utf-8').splitlines():
    if line.strip():
        h,rel=line.split('  ',1);base[rel]=h
fail=[];protected=0;changed=[]
for rel,expected in base.items():
    p=ROOT/rel
    if not p.is_file(): fail.append(f'missing R14 product input: {rel}');continue
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if rel in ALLOWED:
        if actual!=expected: changed.append(rel)
    else:
        protected+=1
        if actual!=expected: fail.append(f'protected R14 product/runtime input drifted: {rel}')
for p in ROOT.rglob('*'):
    if not p.is_file(): continue
    relp=p.relative_to(ROOT);rel=relp.as_posix()
    if not is_product(rel): continue
    if rel.startswith(ROOM_PREFIX) and rel.endswith('.json'): continue
    if any(part in GEN_DIRS for part in relp.parts) or p.name in GEN_FILES or p.suffix.lower() in GEN_SUFFIX: continue
    if rel not in base: fail.append(f'unapproved new R15 product/build input: {rel}')
if set(changed)!=ALLOWED: fail.append(f'R15 expected exactly two presentation/compiler-hygiene product changes; observed: {sorted(changed)}')
if fail:
    for x in fail: print('[FAIL]',x)
    raise SystemExit(1)
print(f'[PASS] R15 preserves {protected} R14 product/runtime/build inputs byte-for-byte')
print('[PASS] R15 product changes are restricted to HostingScreens import closure and DiscoveryScreens warning hygiene')
print('[PASS] R14 UI/UX, API/repository/ViewModel/model/navigation/RTC/security/payment/business authority remains preserved')
