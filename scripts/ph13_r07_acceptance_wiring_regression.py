from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
a=(ROOT/'scripts/VC-ANDROID-PH13-R07-ACCEPTANCE.cmd').read_text(encoding='utf-8')
b=(ROOT/'scripts/VC-ANDROID-PH13-R07-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
s=(ROOT/'scripts/VC-ANDROID-PH13-R07-SOURCE-DIAGNOSTIC.ps1').read_text(encoding='utf-8')
checks={
 'R07 source diagnostic wired':'VC-ANDROID-PH13-R07-SOURCE-DIAGNOSTIC.ps1' in a,
 'R07 isolated build wired':'VC-ANDROID-PH13-R07-ISOLATED-BUILD.ps1' in a,
 'R07 delivery integrity wired':'ph13_r07_delivery_integrity.py' in a,
 'compile correction regression wired':'ph13_r07_compile_correction_regression.py' in s,
 'product preservation regression wired':'ph13_r07_product_preservation_regression.py' in s,
 'debug compile mandatory':"'[GATE 1] compileDebugKotlin'" in b,
 'staging compile mandatory':"'[GATE 2] compileStagingKotlin'" in b,
 'release compile mandatory':"'[GATE 3] compileReleaseKotlin'" in b,
 'unit tests retained':"'[GATE 4] Unit tests'" in b,
 'lint retained':"'[GATE 5] Lint'" in b,
 'assemblies retained':"'[GATE 6] Assemblies'" in b,
 'device gate retained':"'[GATE 7] Physical-device instrumentation'" in b,
 'verified local wrapper required':"wrapper runtime URL is not pinned to the verified local Gradle ZIP" in b,
 'dependent gates do not waste time after compile failure':'Gates 4-6 skipped because one or more mandatory Kotlin compile variants failed' in b,
 'device gate waits for APK prerequisites':'Gate 7 skipped because the APK/androidTest assembly prerequisites were not produced successfully' in b,
 'final acceptance fail-closed':'exit /b 1' in a,
}
for k,v in checks.items(): print(('[PASS] ' if v else '[FAIL] ')+k)
if not all(checks.values()): raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R07 acceptance wiring: {len(checks)}/{len(checks)} PASS')
