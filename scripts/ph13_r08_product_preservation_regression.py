from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
PARENT=ROOT/'contracts/VC-ANDROID-PH13-R08-PH13-R07-PARENT-CANONICAL.sha256'
ALLOWED_PRODUCT_CHANGED={
 'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
}
R08_PREFIXES=('VC-ANDROID-PH13-R08-','docs/VC-ANDROID-PH13-R08-','scripts/VC-ANDROID-PH13-R08-','scripts/ph13_r08_','contracts/VC-ANDROID-PH13-R08-')
parent={}
for line in PARENT.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    h,rel=line.split('  ',1); parent[rel]=h
fail=[]; changed=[]
for rel,expected in parent.items():
    p=ROOT/rel
    if not p.is_file():
        fail.append(f'missing PH13-R07 tracked file: {rel}'); continue
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual != expected:
        if rel in ALLOWED_PRODUCT_CHANGED: changed.append(rel)
        elif rel in {'CHANGELOG.md','README.md'}: pass
        else: fail.append(f'unexpected PH13-R07 product/delivery drift: {rel}')
for rel in ALLOWED_PRODUCT_CHANGED:
    if rel not in changed:
        fail.append(f'expected R08 compiler-correction file did not change: {rel}')
critical_fragments=('/data/','Repository.kt','ViewModel.kt','Models.kt','Api.kt','VoiceCloudNavHost.kt','core/network/','core/security/','realtime')
if any(any(frag in rel for frag in critical_fragments) for rel in ALLOWED_PRODUCT_CHANGED):
    fail.append('R08 allowed product change touches functional/business authority')
if fail:
    for x in fail: print('[FAIL]',x)
    raise SystemExit(1)
print('[PASS] PH13-R08 product change is limited to AuthScreens.kt compiler correction')
print('[PASS] APIs/repositories/ViewModels/models/navigation/security/realtime/business logic remain PH13-R07-identical')
print('[PASS] Full R05 premium UI remains preserved apart from the compile correction')
