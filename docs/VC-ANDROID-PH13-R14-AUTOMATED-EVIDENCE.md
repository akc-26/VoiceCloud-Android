# VC-ANDROID-PH13-R14 — Automated Evidence

## Packaging-environment evidence
The packaging tree completed the complete applicable source/static regression chain before archive creation. The same chain is rerun after fresh ZIP extraction. Evidence includes:
- R14 acceptance wiring and full-gate authority — **21/21 PASS**.
- R14 physical-device-driven visual regression — **40/40 PASS**.
- R14 Kotlin compile-risk structural regression — **73/73 PASS**.
- R14 Windows acceptance-harness lexical/structure regression — **13/13 PASS**.
- R14 functional/product preservation against the R13 product manifest — **169 protected functional/runtime/build inputs byte-identical**; product drift limited to approved presentation/test/dependency files.
- R14 physical-device-driven visual regression, including the whitespace/media/profile/Home/Creator corrections.
- R14 authored-source hygiene with exact Room generated-schema exclusion.
- R14 workstation artifact simulation — **23 known generated artifacts accepted**, including the exact Room schema output; injected unknown Kotlin drift correctly rejected.
- R13 corrected instrumentation palette authority.
- All applicable inherited PH13/PH12/PH11/PH09/PH07/PH06 source/API/security/payment/RTC regressions.

**Combined pre-package applicable regression chain: 31/31 PASS.**
- Immutable R14 delivery manifest validation.

## Functional preservation
R14 hashes the R13 product/build-input authority and requires all non-presentation functional/runtime/build inputs to remain byte-identical. API/repository/ViewModel/model/navigation/RTC/security/payment/business authority is not intentionally changed.

## Android build evidence
No Android SDK/device is available in the packaging environment, so this document does **not** claim that Debug/Staging/Release compilation, Android lint or device instrumentation ran here. The Windows acceptance command is the authoritative executable gate.

Run only:

```bat
scripts\VC-ANDROID-PH13-R14-ACCEPTANCE.cmd
```

A final exit code `0` means the complete host + physical-device gate passed. Exit `2` means all host gates passed but a healthy physical device was unavailable, so acceptance is still pending. Exit `1` means a genuine failure occurred.
