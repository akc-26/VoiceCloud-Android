from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
PARENT=ROOT/'contracts/VC-ANDROID-PH13-R11-PH13-R10-PARENT-CANONICAL.sha256'
PRODUCT=ROOT/'contracts/VC-ANDROID-PH13-R11-R09-PRODUCT-CANONICAL.sha256'
if not PARENT.is_file(): raise SystemExit('[FAIL] R10 parent canonical manifest missing')
if not PRODUCT.is_file(): raise SystemExit('[FAIL] R09 product canonical manifest missing')
allowed_parent_drift={
 'CHANGELOG.md','README.md','scripts/VC-ANDROID-DEVICE-INSTRUMENTATION.ps1',
}
allowed_inherited_unmanifested={'contracts/VC-ANDROID-PH13-R10-SOURCE-MANIFEST.sha256'}
new_r11_prefixes=('scripts/VC-ANDROID-PH13-R11-','scripts/ph13_r11_','contracts/VC-ANDROID-PH13-R11-','docs/VC-ANDROID-PH13-R11-')
fail=[]; product_count=0
for line in PRODUCT.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    expected,rel=line.split('  ',1); p=ROOT/rel; product_count+=1
    if not p.is_file(): fail.append(f'missing R09 product file: {rel}'); continue
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=expected: fail.append(f'build-proven R09 Android product/build input drifted: {rel}')
parent={}
for line in PARENT.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    expected,rel=line.split('  ',1); parent[rel]=expected
for rel,expected in parent.items():
    p=ROOT/rel
    if not p.is_file(): fail.append(f'missing R10 tracked file: {rel}'); continue
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=expected and rel not in allowed_parent_drift:
        fail.append(f'unexpected R10 tracked-file drift: {rel}')
parent_kt={r for r in parent if r.endswith('.kt')}
for p in ROOT.rglob('*.kt'):
    rel=p.relative_to(ROOT).as_posix()
    if rel not in parent_kt: fail.append(f'unexpected new Kotlin product source: {rel}')
# New non-Kotlin files are allowed only in explicit R11 harness/contracts/docs authority.
for p in ROOT.rglob('*'):
    if not p.is_file(): continue
    rel=p.relative_to(ROOT).as_posix()
    if rel in parent or rel in allowed_parent_drift or rel in allowed_inherited_unmanifested or rel.endswith('.kt'): continue
    if rel.startswith(new_r11_prefixes): continue
    # Manifest itself is generated later and is R11 authority.
    if rel == 'contracts/VC-ANDROID-PH13-R11-SOURCE-MANIFEST.sha256': continue
    fail.append(f'unexpected new non-product file outside R11 authority: {rel}')
if fail:
    for x in fail: print('[FAIL]',x)
    raise SystemExit(1)
print(f'[PASS] R11 preserves {product_count} build-proven R09 Android product/build-input files byte-for-byte')
print('[PASS] R11 changes are acceptance/device-harness only; app/UI/API/runtime source is unchanged')
print('[PASS] No new Kotlin product source introduced in R11')
