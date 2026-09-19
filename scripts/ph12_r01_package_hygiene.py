from pathlib import Path
import hashlib, shutil
ROOT=Path(__file__).resolve().parents[1]
MANIFEST=ROOT/'contracts/VC-ANDROID-PH12-R01-SOURCE-MANIFEST.sha256'
# Python regression runs can create caches. Remove only these disposable artifacts.
for d in list(ROOT.rglob('__pycache__')):
    if d.is_dir(): shutil.rmtree(d, ignore_errors=True)
for p in list(ROOT.rglob('*.pyc')):
    if p.is_file(): p.unlink(missing_ok=True)
if not MANIFEST.exists(): raise SystemExit('[FAIL] PH12 source delivery manifest missing')
forbidden_parts={'.git','.idea','.gradle','__pycache__','build'}
forbidden_names={'local.properties','.DS_Store'}
forbidden_suffixes={'.apk','.aab','.pyc','.pyo','.zip'}
entries=0
for line in MANIFEST.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    expected,rel=line.split('  ',1); path=ROOT/rel; parts=Path(rel).parts
    if any(x in forbidden_parts for x in parts) or Path(rel).name in forbidden_names or Path(rel).suffix.lower() in forbidden_suffixes:
        raise SystemExit(f'[FAIL] forbidden delivery artifact is recorded in PH12 source manifest: {rel}')
    if not path.is_file(): raise SystemExit(f'[FAIL] PH12 delivery source file missing: {rel}')
    actual=hashlib.sha256(path.read_bytes()).hexdigest()
    if actual!=expected: raise SystemExit(f'[FAIL] PH12 delivery source file drifted after packaging manifest: {rel}')
    entries+=1
print(f'[PASS] PH12 clean-package verification: {entries} manifest-tracked delivery files intact; transient workstation files are not package authority')
