# VC-ANDROID-PH13-R17 — Workstation / Device QA

Run from a freshly extracted R17 directory:

```bat
scripts\VC-ANDROID-PH13-R17-ACCEPTANCE.cmd
```

Expected sequence:

1. R17 source/regression diagnostics complete without Windows locale decoding errors.
2. Debug Kotlin compilation passes.
3. Staging Kotlin compilation passes.
4. Release Kotlin compilation passes.
5. Unit tests pass.
6. All-variant lint passes.
7. Debug/Staging/Release and Debug AndroidTest assemblies pass.
8. If a healthy authorized API 26+ device is available, physical-device instrumentation runs and must pass.
9. Delivery integrity passes.

If no healthy authorized device is available, the command must return the distinct **PENDING** device state only after every host build gate passes. A genuine compiler, test, lint, assembly, install or instrumentation failure remains a failure.

After executable acceptance, visually inspect the approved R16/R17 UI on the physical device against the bundled approved boards. R17 intentionally preserves that R16 design implementation while correcting compiler/acceptance defects.
