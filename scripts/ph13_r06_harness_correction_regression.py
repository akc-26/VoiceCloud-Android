from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ps = (ROOT / 'scripts/BOOTSTRAP-GRADLE-WRAPPER.ps1').read_text(encoding='utf-8')
sh = (ROOT / 'scripts/BOOTSTRAP-GRADLE-WRAPPER.sh').read_text(encoding='utf-8')
build = (ROOT / 'scripts/VC-ANDROID-PH13-R06-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
delivery = (ROOT / 'scripts/ph13_r06_delivery_integrity.py').read_text(encoding='utf-8')

checks=[]
def ck(label, cond):
    checks.append(bool(cond)); print(('[PASS] ' if cond else '[FAIL] ') + label)

ck('Windows bootstrap still checksum-verifies cached Gradle ZIP', 'Get-FileHash $zip -Algorithm SHA256' in ps and '553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746' in ps)
ck('Windows wrapper generation uses verified local distribution URI', '--gradle-distribution-url $localDistributionUri' in ps)
ck('Windows bootstrap keeps generated wrapper pinned to local file URI', 'distributionUrl=$escapedLocalDistributionUri' in ps and "distributionUrl=https\\://services.gradle.org" not in ps)
ck('Windows bootstrap explicitly verifies local wrapper runtime', "Contains('distributionUrl=file\\:///')" in ps)
ck('Unix bootstrap keeps generated wrapper pinned to local file URI', 'distributionUrl=$ESCAPED_LOCAL_URI' in sh and 'distributionUrl=https\\://services.gradle.org' not in sh)
ck('R06 build verifies local wrapper before compile gates', "Contains('distributionUrl=file\\:///')" in build and '$script:buildReady = $true' in build)
ck('R06 compile gates are blocked if local wrapper bootstrap is invalid', 'blocked because verified local Gradle wrapper bootstrap did not complete' in build)
ck('R06 cleanup uses cached Gradle executable and cannot force remote wrapper fetch', 'Stopping isolated Gradle daemons with cached Gradle executable' in build and '& $cachedGradleBat --stop' in build)
ck('R06 source copy excludes workstation machine artifacts', "'.gradle'" in build and "'local.properties'" in build and "'gradle-daemon-jvm.properties'" in build)
ck('R06 delivery integrity distinguishes generated workstation artifacts from product drift', 'IGNORED_GENERATED_DIRS' in delivery and 'unmanifested product/delivery file present' in delivery)
ck('R06 delivery integrity remains hash fail-closed for tracked files', 'delivery file drifted' in delivery)

passed=sum(checks)
if passed != len(checks):
    raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R06 harness correction regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R06 harness correction regression: {passed}/{len(checks)} PASS')
