# VC-ANDROID-PH13-R03 Correction Report

## Trigger
PH13-R02 passed all real Android build gates through assemblies, then Gate 8 failed because Gradle created `gradle/gradle-daemon-jvm.properties` in the same directory whose bytes Gate 8 was validating as a clean distributable source tree.

## Root cause
The acceptance design mixed two incompatible roles in one folder: a mutable Gradle build workspace and an immutable delivery/Git source tree. R01 first exposed wrapper-property line-ending drift; R02 then exposed the daemon JVM criteria file. Treating generated filenames one by one would remain fragile.

## R03 correction
All Gradle wrapper bootstrap, Debug/Staging/Release compile, tests, lint, assemblies and device instrumentation run in a unique `%TEMP%` copy. The original extracted package is never used as the Gradle workspace. `PYTHONDONTWRITEBYTECODE=1` prevents Python regression checks from generating caches in the original tree. After the isolated workspace is removed, Gate 8 validates the original tree against the strict R03 delivery manifest.

## Product scope
No PH13 product/Kotlin/API/UI implementation change.
