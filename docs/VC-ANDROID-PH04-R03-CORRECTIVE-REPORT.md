# VC-ANDROID-PH04-R03 Corrective Report

## Trigger
The PH04-R02 Windows acceptance log proves the complete host build sweep succeeded: Debug/Staging/Release Kotlin compilation, unit tests, lint and all three app assemblies completed successfully. Gate 6 then failed inside Gradle/UTP device discovery.

The failing aliases were wireless/mDNS ADB entries whose property queries timed out or were treated as Unknown/API 1. The same run also saw the real `ONEPLUS A6000 - 11` device, but the all-device UTP task failed because one phantom alias could not install the APK.

## Scope
No production Android source is changed in R03. The correction is restricted to acceptance infrastructure and documentation.

## Correction
1. Add `:app:assembleDebugAndroidTest` to the broad build sweep so test APK compilation is still a normal Gradle build gate.
2. Replace `:app:connectedDebugAndroidTest` with `VC-ANDROID-DEVICE-INSTRUMENTATION.ps1`.
3. Enumerate ADB entries using the tab-delimited serial/state format.
4. Probe API level with a bounded timeout and reject unresponsive, unknown, or API < 26 entries.
5. Prefer a direct/non-mDNS transport when available.
6. Install the Debug app APK and Debug androidTest APK explicitly to one selected serial with `adb -s`.
7. Discover the installed instrumentation runner on that same device.
8. Run `am instrument -w` only on the selected device.
9. Fail unless instrumentation reports at least one passing test.
10. Treat absence of any healthy device as a clearly reported pending device gate rather than a host-build failure.

## Preservation
PH04-R02 production source, PH03 duplicate-key runtime correction, PH02 authentication/account lifecycle, and PH01 platform foundation are unchanged. All accumulated source/regression checks remain in the R03 acceptance chain.
