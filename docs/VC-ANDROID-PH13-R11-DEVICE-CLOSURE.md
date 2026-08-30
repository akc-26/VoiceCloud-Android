# VC-ANDROID-PH13-R11 Device Closure

Run:

```bat
scripts\VC-ANDROID-PH13-R11-ACCEPTANCE.cmd
```

Expected behavior:

- **Device unavailable:** the script prints `[DEVICE-PENDING]`, skips any fallback build, completes host acceptance successfully, and exits 0. Connect/authorize a device later and rerun the same command.
- **Device available + reusable artifacts found:** the script verifies product hashes and proceeds directly to resilient install/instrumentation.
- **Device available + no reusable artifacts:** the script performs only `assembleDebug` and `assembleDebugAndroidTest`, then runs device instrumentation.
- **Real device failure:** failed ADB transport, install verification, instrumentation runner/target mismatch, or failed tests remain a nonzero failure.

The full Debug/Staging/Release compile, unit-test, lint and full-assembly chain is not repeated because that exact Android product source has already passed those gates in the supplied R09 workstation run.
