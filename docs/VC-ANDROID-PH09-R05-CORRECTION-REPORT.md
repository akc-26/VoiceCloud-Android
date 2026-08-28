# VC-ANDROID-PH09-R05 Correction Report

## Baseline

- Parent branch: `VoiceCloud-Android-VC-ANDROID-PH08-R02`
- Parent exact commit: `1739f081ac386acca9a24b37863b72c4db952089`
- Parent commit of PH08-R02: `fe2e7857802861e2ace2733a313c00c5d27d331d` (`VC-ANDROID-PH07-R03`)
- PH09 Kotlin/product behavior is unchanged from R04.
- R05 contains one intentional toolchain configuration addition: `org.gradle.tooling.parallel=true`.

## Actual R04 workstation finding

R04 stopped before the Gradle compile gates with:

```text
[FAIL] gradle.properties effective configuration differs from frozen PH08-R02 authority
[FAIL] unexpected keys: org.gradle.tooling.parallel
```

The property is not a VoiceCloud product setting. Gradle 9.4 introduced `org.gradle.tooling.parallel` to control Tooling API/model-building parallelism independently, and current Android Studio Quail recommends setting it explicitly when parallel Gradle sync is desired. The project is pinned to Gradle 9.5.

R04's mistake was classifying every additional root Gradle property as a source-authority violation.

## R05 correction

### 1. Explicit current toolchain setting

R05 intentionally adds:

```properties
org.gradle.tooling.parallel=true
```

next to the existing frozen:

```properties
org.gradle.parallel=true
```

### 2. Classified Gradle authority

`scripts/ph09_r05_gradle_properties_authority.py` now verifies three categories:

**Frozen PH08 product/build-critical authority — 22 keys**

All accepted VoiceCloud environment/payment keys, Android compatibility keys, Kotlin code style, and the existing Gradle JVM/parallel/cache/configuration-cache keys must remain present with exact values.

**Controlled PH09-R05 toolchain authority — 1 key**

`org.gradle.tooling.parallel=true` must be present and exact.

**Workstation/runtime additions**

Additional `org.gradle.*` and `systemProp.*` entries are informational, not product-source differences. This covers IDE/tooling/runtime configuration such as machine Java home, tooling-model flags and proxy system properties.

### 3. Fail-closed source-sensitive namespaces

Unexpected keys still fail when they belong to source/product namespaces including:

- `VOICECLOUD_*`
- `android.*`
- `kotlin.*`
- `kapt.*`
- `ksp.*`
- `org.jetbrains.kotlin.*`
- arbitrary unclassified project properties

Existing required values also fail if changed, including `VOICECLOUD_ANDROID_PAYMENT_MODE`, `org.gradle.parallel` and `org.gradle.tooling.parallel`.

### 4. Single root Gradle authority

Root `gradle.properties` remains absent from the generic PH08 preservation manifest. The R05 Gradle authority is the only acceptance path that classifies root Gradle properties. Other preserved `.properties` files remain protected by semantic key/value comparison.

## Regression coverage

`scripts/ph09_r05_windows_preflight_regression.py` executes the same authority functions called by acceptance and verifies:

1. generic PH08 manifest exists;
2. R05 Gradle authority contract exists;
3. root `gradle.properties` is absent from the generic manifest;
4. delivered root passes both authorities;
5. CRLF passes;
6. UTF-8 BOM + CRLF passes;
7. UTF-16LE BOM passes;
8. comments/order/whitespace representation changes pass;
9. duplicate `org.gradle.tooling.parallel=true` passes;
10. additional `org.gradle.tooling.*` state passes;
11. machine-specific `org.gradle.java.home` passes;
12. `systemProp.*` runtime configuration passes;
13. payment-mode change fails;
14. frozen `org.gradle.parallel` change fails;
15. controlled tooling value change fails;
16. unknown `VOICECLOUD_*` key fails;
17. unknown Android key fails;
18. unknown Kotlin key fails;
19. arbitrary unknown project property fails;
20. delivered R05 project explicitly contains `org.gradle.tooling.parallel=true`.

The final delivery is also stress-tested by rewriting the entire preserved PH08 text set to CRLF and adding representative IDE/runtime Gradle properties before running the full pre-Gradle acceptance chain.
