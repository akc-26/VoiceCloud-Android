from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
PARENT=ROOT/'contracts/VC-ANDROID-PH13-R10-PH13-R09-PARENT-CANONICAL.sha256'
PRODUCT=ROOT/'contracts/VC-ANDROID-PH13-R10-R09-PRODUCT-CANONICAL.sha256'
if not PARENT.is_file(): raise SystemExit('[FAIL] R09 parent canonical manifest missing')
if not PRODUCT.is_file(): raise SystemExit('[FAIL] R09 product canonical manifest missing')
allowed_drift={
 'CHANGELOG.md','README.md','scripts/VC-ANDROID-DEVICE-INSTRUMENTATION.ps1',
}
# Product/build inputs must remain byte-identical to R09.
fail=[]; product_count=0
for line in PRODUCT.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    expected,rel=line.split('  ',1); p=ROOT/rel; product_count+=1
    if not p.is_file(): fail.append(f'missing R09 product file: {rel}'); continue
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=expected: fail.append(f'R09 product/build input drifted: {rel}')
# Existing R09 tracked delivery may differ only in harness/readme/changelog; all other pre-existing files preserved.
parent={}
for line in PARENT.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    expected,rel=line.split('  ',1); parent[rel]=expected
for rel,expected in parent.items():
    p=ROOT/rel
    if not p.is_file(): fail.append(f'missing R09 tracked file: {rel}'); continue
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=expected and rel not in allowed_drift:
        fail.append(f'unexpected R09 tracked-file drift: {rel}')
# No Kotlin/build input additions permitted in R10.
parent_kt={r for r in parent if r.endswith('.kt')}
for p in ROOT.rglob('*.kt'):
    rel=p.relative_to(ROOT).as_posix()
    if rel not in parent_kt: fail.append(f'unexpected new Kotlin product source: {rel}')
if fail:
    for x in fail: print('[FAIL]',x)
    raise SystemExit(1)
print(f'[PASS] R10 preserves {product_count} R09 Android product/build-input files byte-for-byte')
print('[PASS] R10 changes are acceptance/device-harness only; R09 app/UI/API/runtime product source is unchanged')
print('[PASS] No new Kotlin product source introduced in R10')
