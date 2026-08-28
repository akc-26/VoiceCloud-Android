# VC-ANDROID-PH09-R05 Automated Evidence

## Authority

- Parent Git baseline: `VoiceCloud-Android-VC-ANDROID-PH08-R02`
- Parent exact commit: `1739f081ac386acca9a24b37863b72c4db952089`
- PH09 Kotlin/product behavior: unchanged from R04.
- R05 controlled toolchain change: `org.gradle.tooling.parallel=true`.

## R05 pre-Gradle regression results

- `scripts/ph09_r05_acceptance_wiring_regression.py` — **19/19 PASS**
- `scripts/ph09_r05_source_check.py` — **PASS**
- `scripts/ph09_r05_product_quality_regression.py` — **15/15 PASS**
- `scripts/ph09_r05_security_authority_regression.py` — **15/15 PASS**
- `scripts/ph09_r05_gradle_properties_authority.py` — **23 required keys PASS** (22 frozen PH08 + 1 controlled R05 tooling key)
- `scripts/ph09_r05_windows_preflight_regression.py` — **20/20 PASS**
- `scripts/ph09_r05_preservation_regression.py` — **PASS**
- `scripts/ph09_r05_compile_risk_regression.py` — **20/20 PASS**
- `scripts/ph08_r02_payment_regression.py` — **33/33 PASS**
- `scripts/ph08_r02_compile_risk_regression.py` — **19/19 PASS**
- `scripts/ph07_r03_adb_daemon_regression.py` — **4/4 PASS**
- `scripts/ph06_r03_hosting_compile_regression.py` — **13/13 PASS**

Preservation result:

- **323** PH08 baseline files semantically preserved outside the controlled PH09 allowlist; root `gradle.properties` is intentionally excluded from this generic count.
- Root `gradle.properties`: **22 frozen PH08 product/build keys + 1 controlled R05 tooling key verified**.
- **10** controlled PH09 integration changes in the generic baseline allowlist.
- Required additive `:feature:settings` files present.

## Windows/tooling stress verification

A copy of the delivery tree was deliberately altered as follows before rerunning the complete pre-Gradle chain:

- **329** preserved PH08 text files rewritten to CRLF;
- root `gradle.properties` rewritten with UTF-8 BOM, CRLF and trailing spaces;
- duplicate `org.gradle.tooling.parallel=true` added;
- `org.gradle.tooling.parallel.ignore-legacy-default=true` added;
- machine-specific `org.gradle.java.home` added;
- `systemProp.http.proxyHost` added.

The complete PH09 + inherited pre-Gradle acceptance chain remained **PASS**.

Tamper regressions separately prove that payment-mode changes, frozen Gradle parallel changes, controlled tooling changes, unknown VoiceCloud/Android/Kotlin keys and arbitrary project properties are still rejected.

## Required Windows workstation acceptance

Run:

```powershell
scripts\VC-ANDROID-PH09-R05-ACCEPTANCE.cmd
```

Mandatory order remains:

1. `:app:compileDebugKotlin`
2. `:app:compileStagingKotlin`
3. `:app:compileReleaseKotlin`
4. unit tests
5. lint Debug/Staging/Release
6. Debug/Staging/Release assemblies + Debug AndroidTest
7. physical-device instrumentation when available

Assistant-side source/regression verification does not substitute for workstation Gradle/device acceptance.
