from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
M=ROOT/'contracts/VC-ANDROID-PH13-R03-PH13-R02-PARENT-CANONICAL.sha256'
allowed={'README.md','CHANGELOG.md'}
unchanged=0; controlled=0
for line in M.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    expected, rel=line.split('  ',1); p=ROOT/rel
    if not p.is_file(): raise SystemExit(f'[FAIL] PH13-R03 deleted PH13-R02 parent file: {rel}')
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual==expected: unchanged+=1; continue
    if rel in allowed: controlled+=1; continue
    raise SystemExit(f'[FAIL] PH13-R03 changed non-harness PH13-R02 parent file: {rel}')
print(f'[PASS] PH13-R03 preserves {unchanged} PH13-R02 parent files byte-identically')
print(f'[PASS] PH13-R03 controlled parent edits: {controlled} (README.md / CHANGELOG.md only)')
print('[PASS] PH13-R03 introduces no PH13 product/Kotlin source change or parent deletion')
