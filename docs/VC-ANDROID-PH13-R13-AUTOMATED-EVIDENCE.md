# VC-ANDROID-PH13-R13 Automated Evidence

R13 adds fail-closed regressions for:

- approved premium branding authority values;
- absence of stale pre-redesign instrumentation assertions;
- preservation of the complete runtime palette conversion guard;
- byte-identical build-proven production/runtime source;
- only one permitted Kotlin drift from R12: the foundation androidTest file;
- workstation-generated Gradle/Android Studio artifact tolerance;
- unknown source/delivery drift rejection;
- device preflight before any build;
- verified prior-workspace reuse;
- androidTest-only fast rebuild;
- fallback Debug + androidTest prerequisite build;
- genuine device/instrumentation failures remaining fail-closed.

The final ZIP is re-extracted and the same R13-specific and inherited static/source regression chain is rerun against the extracted archive.

## Assistant-side source/regression result
- R13 acceptance wiring: 14/14 PASS
- R13 premium palette instrumentation regression: 11/11 PASS
- R13 production/runtime preservation: 184 build-proven R09 inputs byte-identical
- R13 device/test closure regression: 16/16 PASS
- R13 workstation artifact simulation: 22 known generated artifacts accepted; unknown drift rejected
- Complete applicable R13 + inherited Python source/regression chain: 29/29 PASS

Physical Android instrumentation is not claimed as passed in the assistant environment; the Windows/device run remains the authority for that final gate.
