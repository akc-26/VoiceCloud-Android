# VC-ANDROID-PH13-R12 Automated Evidence

R12 adds direct regression coverage for the exact R11 workstation-contamination failure and Windows batch exit-code handling.

Required closure evidence:
- R12 acceptance wiring regression PASS.
- R12 product preservation PASS in clean tree.
- R12 workstation-artifact simulation PASS with the exact 12 R11-reported generated paths plus additional common Gradle/Android Studio artifacts.
- R12 preservation/integrity fail closed on an unknown untracked file.
- R11 resilient device-closure regression retained.
- R09/R08 and inherited functional/UI/API/security/payment/RTC regression chain retained.
- Final ZIP re-extracted and all R12-specific gates rerun against the extracted archive.
