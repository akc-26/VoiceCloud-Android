from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[1]
manifest=ROOT/'contracts/VC-ANDROID-PH08-R02-PRESERVATION-MANIFEST.sha256'
if not manifest.exists(): raise SystemExit('[FAIL] PH08-R02 preservation manifest missing')
count=0
for line in manifest.read_text(encoding='utf-8').splitlines():
    if not line.strip(): continue
    expected, rel=line.split('  ',1)
    path=ROOT/rel
    if not path.exists(): raise SystemExit(f'[FAIL] preserved file missing: {rel}')
    raw=path.read_bytes()
    # Windows Gradle wrapper bootstrap rewrites this canonical text file with CRLF.
    # Preserve semantic source authority while tolerating only LF/CRLF transport drift.
    if rel == 'gradle/wrapper/gradle-wrapper.properties':
        raw=raw.replace(b'\r\n', b'\n')
    actual=hashlib.sha256(raw).hexdigest()
    if actual != expected: raise SystemExit(f'[FAIL] preserved file changed unexpectedly: {rel}')
    count += 1
print(f'[PASS] {count} untouched PH08-R01 files match the R01 corrective baseline')
# New R02 resources must exist but are not part of the R01 hash manifest.
for rel in ['core/designsystem/src/main/res/drawable/vc_icon_emoji.xml','core/designsystem/src/main/res/drawable/vc_icon_gift.xml']:
    if not (ROOT/rel).exists(): raise SystemExit(f'[FAIL] required R02 resource missing: {rel}')
print('[PASS] PH08-R02 additive live-room icon resources present')
