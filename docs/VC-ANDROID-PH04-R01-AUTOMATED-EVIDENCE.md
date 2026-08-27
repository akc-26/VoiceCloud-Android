# VC-ANDROID-PH04-R01 Automated Evidence

## Current source/regression evidence
- PH04 source authority: **82/82 PASS**
- PH04 module dependency audit: **33/33 PASS**
- PH03-R02 lazy-list runtime regression: **25/25 PASS**
- PH03-R01 source/module checks: **PASS**
- PH02 accumulated source/Gradle/delivery/compile-surface/preservation regressions: **PASS**
- PH01 accumulated foundation/Windows/AGP/SDK/ABI/live-server/wrapper regressions: **PASS**

## Windows build acceptance command
```bat
scripts\VC-ANDROID-PH04-R01-ACCEPTANCE.cmd
```

The script requires the following order before phase acceptance:
1. accumulated source/regression authority
2. Java and Android SDK resolution
3. Raspberry Pi/Tailscale REST + Socket.IO + Web connectivity
4. `clean`
5. `:app:compileDebugKotlin`
6. `:app:compileStagingKotlin`
7. `:app:compileReleaseKotlin`
8. unit tests
9. lint Debug/Staging/Release
10. assemble Debug/Staging/Release
11. `:app:connectedDebugAndroidTest` when an authorized device is available

The compile/test/lint/assembly sweep uses `--continue` so one workstation run exposes every reachable build failure.

## Acceptance status
The source/regression checks above passed in the delivery environment. Android SDK/physical-device acceptance is **not claimed** until the Windows acceptance output is returned and verified.
