#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
echo '============================================================='
echo 'VoiceCloud Android PH01 R05 - Full Acceptance'
echo '============================================================='
python3 scripts/ph01_r05_source_check.py
python3 scripts/ph01_r02_windows_acceptance_regression.py
python3 scripts/ph01_r03_agp9_builtin_kotlin_regression.py
python3 scripts/ph01_r04_sdk_logging_regression.py
python3 scripts/ph01_r05_module_abi_regression.py
scripts/VC-ANDROID-JAVA-ENV.sh
scripts/VC-ANDROID-SDK-ENV.sh
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then scripts/BOOTSTRAP-GRADLE-WRAPPER.sh; fi
./gradlew :app:compileDebugKotlin --stacktrace
./gradlew :app:compileStagingKotlin --stacktrace
./gradlew :app:compileReleaseKotlin --stacktrace
./gradlew test --stacktrace
./gradlew lintDebug lintStaging lintRelease --stacktrace
./gradlew :app:assembleDebug :app:assembleStaging :app:assembleRelease --stacktrace
echo '[PASS] VC-ANDROID-PH01-R05 acceptance commands completed successfully.'
