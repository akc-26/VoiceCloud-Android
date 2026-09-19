from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
accept = (ROOT / "scripts/VC-ANDROID-PH13-R02-ACCEPTANCE.cmd").read_text(encoding="utf-8")
restore = (ROOT / "scripts/ph13_r02_restore_tracked_source.py").read_text(encoding="utf-8")
post = (ROOT / "scripts/ph13_r02_post_build_hygiene.py").read_text(encoding="utf-8")
delivery = (ROOT / "scripts/ph13_r02_delivery_hygiene.py").read_text(encoding="utf-8")
checks=[]
def ck(name, ok):
    checks.append(bool(ok)); print(("[PASS] " if ok else "[FAIL] ")+name)

ck("R02 acceptance restores tracked source before Gate 8 integrity verification", "ph13_r02_restore_tracked_source.py" in accept and accept.index("ph13_r02_restore_tracked_source.py") < accept.index("ph13_r02_post_build_hygiene.py"))
ck("wrapper properties are restored with deterministic LF bytes", 'encode("ascii")' in restore and 'write_bytes(CANONICAL_WRAPPER_PROPERTIES)' in restore)
ck("generated gradlew is removed", '"gradlew"' in restore)
ck("generated gradlew.bat is removed", '"gradlew.bat"' in restore)
ck("generated wrapper jar is removed", '"gradle/wrapper/gradle-wrapper.jar"' in restore)
ck("post-build integrity verifies tracked hashes", "hashlib.sha256(p.read_bytes()).hexdigest()" in post and "tracked source drifted after build" in post)
ck("post-build integrity explicitly allows Gradle build directories", '"build"' in post and "ALLOWED_GENERATED_DIRS" in post)
ck("post-build integrity rejects unknown non-generated files", "unexpected non-generated file appeared after build" in post)
ck("delivery hygiene still forbids build directories", '"build"' in delivery and "FORBIDDEN_DIRS" in delivery)
ck("delivery hygiene still forbids generated wrapper jar", '"gradle-wrapper.jar"' in delivery and "FORBIDDEN_FILES" in delivery)
ck("delivery hygiene rejects unmanifested source", "unmanifested delivery file present" in delivery)
ck("R02 final pass marker is unique", accept.count("[PASS] VC-ANDROID-PH13-R02 acceptance commands completed successfully.") == 1)
if not all(checks):
    raise SystemExit("[FAIL] PH13-R02 corrective regression")
print(f"[PASS] VC-ANDROID-PH13-R02 corrective regression: {sum(checks)}/{len(checks)} PASS")
