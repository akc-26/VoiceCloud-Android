# VC-ANDROID-PH13-R06 Automated Evidence

## Assistant-side source/regression evidence
The R06 correction adds:

- `ph13_r06_acceptance_wiring_regression.py`
- `ph13_r06_harness_correction_regression.py`
- `ph13_r06_product_preservation_regression.py`
- `ph13_r06_delivery_integrity.py`

These verify:

- R06 acceptance wiring is complete and fail-closed.
- the cached Gradle 9.5.0 ZIP remains SHA-256 verified;
- the runtime wrapper distribution URL remains a local `file:///...` URI after wrapper generation;
- compile gates cannot silently fall back to the public Gradle distribution;
- cleanup cannot trigger the prior wrapper-download failure path;
- R05 Android product/UI/API/runtime source remains byte-identical;
- known Android Studio/Gradle machine artifacts do not masquerade as product-source drift;
- tracked source and unknown unmanifested product files remain fail-closed.

## Full Android build status
The assistant environment does not contain the user's Windows Android SDK/Gradle cache/device environment, so R06 is not declared workstation-build accepted here. The real build authority is the included Windows acceptance command:

```bat
scripts\VC-ANDROID-PH13-R06-ACCEPTANCE.cmd
```

A valid final acceptance must reach:

```text
[PASS] VC-ANDROID-PH13-R06 acceptance commands completed successfully.
```

## Current source/regression result
The complete R06 Python source/regression set was executed in the assistant environment: **23/23 passed**. This includes the R06 harness/wiring/product-preservation checks plus all retained PH13/PH12/PH11/PH09/PH07/PH06 backend-contract, compile-surface, payment, device, warning, and safety regressions.

The R06 delivery integrity check was also exercised twice: once on a clean tree and once with simulated `.gradle`, `local.properties`, and generated wrapper artifacts. In both cases tracked source remained hash-verified; the generated artifacts were ignored only as non-product workstation state.
