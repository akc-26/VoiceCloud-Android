from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
BASELINE = ROOT / "contracts/VC-ANDROID-PH13-R02-PH13-R01-BASELINE-CANONICAL.sha256"
ALLOWED_PARENT_EDITS = {"README.md", "CHANGELOG.md"}

if not BASELINE.is_file():
    raise SystemExit("[FAIL] PH13-R02 canonical PH13-R01 baseline manifest missing")

preserved = 0
controlled = []
for line in BASELINE.read_text(encoding="utf-8").splitlines():
    if not line.strip():
        continue
    expected, rel = line.split("  ", 1)
    p = ROOT / rel
    if not p.is_file():
        raise SystemExit(f"[FAIL] PH13-R01 parent file deleted in R02: {rel}")
    actual = hashlib.sha256(p.read_bytes()).hexdigest()
    if actual != expected:
        if rel not in ALLOWED_PARENT_EDITS:
            raise SystemExit(f"[FAIL] unexpected PH13-R01 product/source drift in R02: {rel}")
        controlled.append(rel)
    else:
        preserved += 1

if set(controlled) != ALLOWED_PARENT_EDITS:
    raise SystemExit(f"[FAIL] PH13-R02 expected only README/CHANGELOG controlled edits, got: {controlled}")
print(f"[PASS] PH13-R02 preserves {preserved} PH13-R01 parent files byte-identically")
print("[PASS] PH13-R02 controlled parent edits are exactly README.md and CHANGELOG.md")
print("[PASS] PH13-R02 introduces no PH13 product/Kotlin source change")
