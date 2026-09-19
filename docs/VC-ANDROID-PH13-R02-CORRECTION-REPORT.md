# VC-ANDROID-PH13-R02 Correction Report

## Reported workstation evidence
PH13-R01 completed the source/contract gates, generated the verified Gradle 9.5 wrapper, and passed:

- `:app:compileDebugKotlin`
- `:app:compileStagingKotlin`
- `:app:compileReleaseKotlin`
- unit tests
- Debug/Staging/Release lint
- Debug/Staging/Release assemblies and Debug AndroidTest assembly

No healthy physical device was connected, which remained a nonfatal manual-QA warning. Gate 8 then failed on `gradle/wrapper/gradle-wrapper.properties` drift.

## Root cause
The clean delivery package contains canonical LF bytes in `gradle/wrapper/gradle-wrapper.properties`. `BOOTSTRAP-GRADLE-WRAPPER.ps1` invokes the standard Gradle wrapper task and then rewrites the canonical properties with Windows PowerShell `Set-Content`, which emits CRLF line endings. The old Gate 8 verifier compared byte hashes after the build and therefore interpreted this expected bootstrap representation change as source drift.

The old Gate 8 verifier also rejected every directory named `build`, even though Gates 1-6 necessarily create module build directories. That means fixing only the line-ending mismatch would expose a second false failure.

## R02 correction
R02 does not change PH13 application/Kotlin behavior. It changes only the acceptance/delivery harness:

1. After device instrumentation, `ph13_r02_restore_tracked_source.py` restores the canonical wrapper-properties bytes and removes generated `gradlew`, `gradlew.bat`, `gradle-wrapper.jar`, Python cache files and Python cache directories.
2. `ph13_r02_post_build_hygiene.py` verifies every R02 manifest-tracked source/delivery file byte-for-byte and rejects unknown non-generated files, while allowing only explicit workstation-generated directories/files such as `.gradle`, `.kotlin`, module `build` directories, IDE metadata and machine-local `local.properties`.
3. `ph13_r02_delivery_hygiene.py` remains strict for the distributable package and rejects build/cache/IDE/machine/wrapper artifacts.
4. PH13-R01 → PH13-R02 preservation proves product source is unchanged; only README/CHANGELOG are controlled parent edits and R02 delivery/acceptance files are additions.

## Required result
Run `scripts\VC-ANDROID-PH13-R02-ACCEPTANCE.cmd`. PH13 is not Git-frozen until that command reaches its final PASS.
