from pathlib import Path
import hashlib, sys, shutil
ROOT=Path(__file__).resolve().parents[1]
MANIFEST=ROOT/'contracts/VC-ANDROID-PH13-R01-SOURCE-MANIFEST.sha256'
FORBIDDEN_DIRS={'.git','.idea','.gradle','build','__pycache__','.kotlin'}
FORBIDDEN_FILES={'local.properties'}
FORBIDDEN_SUFFIXES={'.apk','.aab','.pyc','.pyo','.zip'}
for d in list(ROOT.rglob('__pycache__')):
    if d.is_dir(): shutil.rmtree(d, ignore_errors=True)
for p in list(ROOT.rglob('*.pyc')):
    if p.is_file(): p.unlink(missing_ok=True)
if not MANIFEST.exists(): raise SystemExit('[FAIL] PH13 delivery manifest missing')
for line in MANIFEST.read_text().splitlines():
    if not line.strip(): continue
    expected,rel=line.split('  ',1); p=ROOT/rel
    if not p.is_file(): raise SystemExit(f'[FAIL] PH13 delivery source file missing: {rel}')
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=expected: raise SystemExit(f'[FAIL] PH13 delivery source file drifted after packaging manifest: {rel}')
for p in ROOT.rglob('*'):
    rel=p.relative_to(ROOT)
    if any(part in FORBIDDEN_DIRS for part in rel.parts): raise SystemExit(f'[FAIL] forbidden generated directory in package: {rel}')
    if p.is_file() and (p.name in FORBIDDEN_FILES or p.suffix.lower() in FORBIDDEN_SUFFIXES): raise SystemExit(f'[FAIL] forbidden generated file in package: {rel}')
print('[PASS] PH13 package manifest and hygiene are clean')
