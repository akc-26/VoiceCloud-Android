# VC-ANDROID-PH06-R02 Automated Evidence

## Corrective authority
R02 adds:
- `scripts/VC-ANDROID-PH06-R02-SOURCE-CHECK.ps1`
- `scripts/ph06_r02_premium_ui_regression.py`
- `scripts/VC-ANDROID-PH06-R02-ACCEPTANCE.cmd`

The R02 regression covers the reported PowerShell null-variable defect and the requested portal/login, password visibility, home, Communities, unified search, Messages, live-copy, tab-icon and adaptive-spacing changes.

## Retained acceptance chain
R02 acceptance runs its new corrective gates first, then retains PH06-R01 and all accumulated PH05/PH04/PH03/PH02/PH01 product regressions.

The Windows command then performs:
1. Java 17 and Android SDK discovery.
2. live Raspberry Pi REST / Socket.IO / Web connectivity.
3. verified Gradle 9.5.0 wrapper bootstrap when clean package wrapper binaries are absent.
4. `clean`.
5. one `--continue` broad Android sweep containing:
   - `:app:compileDebugKotlin`
   - `:app:compileStagingKotlin`
   - `:app:compileReleaseKotlin`
   - `test`
   - `lintDebug`, `lintStaging`, `lintRelease`
   - `:app:assembleDebug`, `:app:assembleStaging`, `:app:assembleRelease`
   - `:app:assembleDebugAndroidTest`
6. healthy-device isolated instrumentation.

## Regression evolution
Two historical tests were evolved because R02 intentionally improves the protected behavior:
- PH03 Search keys now require separate collision-safe namespaces for People, Creators, Rooms and Communities.
- PH04 quick-action UI now protects equal-width, single-line, minimum-touch-height icon cards and the concise `Events` label instead of the old stacked placeholder-glyph presentation.

The underlying PH03 duplicate-key and PH04 Compose/UI safety invariants remain protected.

## Assistant-side verification target
Before delivery the exact Python sequence referenced by the R02 acceptance command must pass together, XML resources must parse, generated/machine files must be absent, the clean ZIP must have project files at archive root, ZIP CRC must pass, and the same R02 regression chain must pass again after extracting the exact deliverable.

## Acceptance interpretation
Static/regression/package PASS is not a substitute for Android compilation or device behavior. Build verification remains the user's Windows acceptance output plus physical-device QA.
