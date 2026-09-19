# VC-ANDROID-PH13-R18 — Consolidated Component Signature Compiler Closure

## Parent
VC-ANDROID-PH13-R17.

## Actual compiler defects closed
R16/R17 presentation callsites used obsolete named arguments for `VoiceCloudRemoteMedia`. The component authority is `contentDescription` and `kind`; stale callsites used `label` and `fallbackKind`. R18 corrects all ten stale callsites across Economy (4), Hosting (2) and Creator (4), so the fix is not limited to the first modules reported by Gradle.

The Economy source also removes the ten unused imports and unused `EconomySection.glyph()` helper reported by Android Studio.

## Functional/design preservation
No API, repository, ViewModel, model, navigation authority, RTC, security, payment or business logic is changed. The approved R16 visual composition and R17 UTF-8/Canvas compiler fixes remain authoritative.

## New regression protection
`ph13_r18_component_signature_regression.py` discovers project-declared `VoiceCloud*` function signatures and validates top-level named arguments at all project callsites. It additionally rejects the exact stale `VoiceCloudRemoteMedia` argument names in Economy, Hosting and Creator.
