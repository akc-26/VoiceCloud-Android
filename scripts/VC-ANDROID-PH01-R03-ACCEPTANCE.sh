#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/ph01_r03_source_check.py
python3 scripts/ph01_r03_agp9_builtin_kotlin_regression.py
python3 scripts/ph01_r02_windows_acceptance_regression.py
if [[ ! -f gradle/wrapper/gradle-wrapper.jar ]]; then ./scripts/BOOTSTRAP-GRADLE-WRAPPER.sh; fi
./gradlew :app:compileDebugKotlin --stacktrace
./gradlew :app:compileStagingKotlin --stacktrace
./gradlew :app:compileReleaseKotlin --stacktrace
./gradlew test --stacktrace
./gradlew lintDebug lintStaging lintRelease --stacktrace
./gradlew :app:assembleDebug :app:assembleStaging :app:assembleRelease --stacktrace
if command -v adb >/dev/null && adb devices | tail -n +2 | grep -q $'\tdevice$'; then ./gradlew :app:connectedDebugAndroidTest --stacktrace; else echo '[WARN] No adb device/emulator detected; run connectedDebugAndroidTest before manual approval.'; fi
echo '[PASS] VC-ANDROID-PH01-R03 acceptance commands completed successfully.'
