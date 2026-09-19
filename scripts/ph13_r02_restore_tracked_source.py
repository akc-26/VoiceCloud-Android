from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parents[1]
CANONICAL_WRAPPER_PROPERTIES = (
    "distributionBase=GRADLE_USER_HOME\n"
    "distributionPath=wrapper/dists\n"
    "distributionSha256Sum=553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746\n"
    "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.5.0-bin.zip\n"
    "networkTimeout=60000\n"
    "validateDistributionUrl=true\n"
    "zipStoreBase=GRADLE_USER_HOME\n"
    "zipStorePath=wrapper/dists\n"
).encode("ascii")

props = ROOT / "gradle/wrapper/gradle-wrapper.properties"
props.parent.mkdir(parents=True, exist_ok=True)
props.write_bytes(CANONICAL_WRAPPER_PROPERTIES)

for rel in ("gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar"):
    p = ROOT / rel
    if p.exists() and p.is_file():
        p.unlink()

for d in list(ROOT.rglob("__pycache__")):
    if d.is_dir():
        shutil.rmtree(d, ignore_errors=True)
for pattern in ("*.pyc", "*.pyo"):
    for p in list(ROOT.rglob(pattern)):
        if p.is_file():
            p.unlink(missing_ok=True)

print("[PASS] PH13-R02 restored canonical tracked wrapper source and removed generated wrapper/Python cache artifacts")
