from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
PARENT=ROOT/'contracts/VC-ANDROID-PH13-R07-PH13-R06-PARENT-CANONICAL.sha256'
ALLOWED_CHANGED={
 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudBrandMark.kt',
 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt',
}
# R07 authority files are new delivery metadata/harness, never Android product behavior.
R07_PREFIXES=('VC-ANDROID-PH13-R07-','docs/VC-ANDROID-PH13-R07-','scripts/VC-ANDROID-PH13-R07-','scripts/ph13_r07_','contracts/VC-ANDROID-PH13-R07-')
parent={}
for line in PARENT.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    h,rel=line.split('  ',1); parent[rel]=h
fail=[]; changed=[]
for rel,expected in parent.items():
    p=ROOT/rel
    if not p.is_file():
        fail.append(f'missing parent tracked file: {rel}'); continue
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=expected:
        if rel in ALLOWED_CHANGED: changed.append(rel)
        elif rel in {'CHANGELOG.md','README.md'}: pass
        else: fail.append(f'unexpected PH13-R06 product/delivery drift: {rel}')
for rel in ALLOWED_CHANGED:
    if rel not in changed:
        fail.append(f'expected compile-correction file did not change from PH13-R06: {rel}')
# Critical behavior layers must not be among allowed changes.
critical_fragments=('/data/','Repository.kt','ViewModel.kt','Models.kt','Api.kt','VoiceCloudNavHost.kt','core/network/','core/security/','realtime')
if any(any(frag in rel for frag in critical_fragments) for rel in ALLOWED_CHANGED):
    fail.append('R07 allowed-change set touches a critical functional layer')
if fail:
    for x in fail: print('[FAIL]',x)
    raise SystemExit(1)
print('[PASS] PH13-R07 changes are limited to the two compiler-failing premium UI primitive files')
print('[PASS] APIs/repositories/ViewModels/models/navigation/security/realtime/business logic remain PH13-R06-identical')
for rel in sorted(changed): print('[PASS] compile-corrected UI primitive:', rel)
