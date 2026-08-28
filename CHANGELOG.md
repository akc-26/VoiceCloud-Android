# VC-ANDROID-PH10-R01
- Parent: `VoiceCloud-Android-VC-ANDROID-PH09-R08` @ `10e820a005d363643bcfc5568e661de96a41dbc9`.
- Adds isolated `:feature:creator` for Creator Portal Core, Dashboard, Profile, Settings, Creator CMS and Support.
- Replaces the PH02 Creator-ready dead-end with the CREATOR-role-authorized PH10 dashboard handoff.
- Adds CREATOR-only cross-portal switching without a second account or token and clears the prior portal back stack.
- Uses only canonical R06 Creator/shared-user/CMS/contact/maintenance endpoints required by PH10.
- Preserves PH11 live/schedule, PH12 audience/messages/subscribers/plans and PH13 analytics/financial/verification scope boundaries.
- Retains PH09-R08 security/Windows/device/warning regressions plus PH08 payment, PH07 ADB and PH06 hosting regressions.
- Use only `scripts\VC-ANDROID-PH10-R01-ACCEPTANCE.cmd` for workstation acceptance.

# VC-ANDROID-PH09-R08
- Closes the R07 Gate-7 PowerShell parser failure caused by unbraced `$UserId:` / `$currentUser:` interpolation.
- Adds a real Windows PowerShell parser gate for every `scripts/*.ps1` before any Python/Gradle acceptance work.
- Adds 19/19 PowerShell-surface regression coverage and expands device closure to 23/23.
- Hardens ADB child-process capture with asynchronous stdout/stderr draining to avoid redirected-pipe deadlocks.
- Closes the only remaining compiler warning in the R07 output with the exact `OVERRIDE_DEPRECATION` compatibility suppression for the retained FCM callback.
- Retains R07 metadata/user-aware instrumentation, warning cleanup, PH08 payment authority and all inherited regression gates.
- Use only `scripts\VC-ANDROID-PH09-R08-ACCEPTANCE.cmd`.

# VC-ANDROID-PH09-R07
- Closes the R06 physical-device instrumentation failure by eliminating hard-coded debug package identity and enforcing one foreground Android user across install, package verification and instrumentation.
- Derives built APK identity/path from AGP output metadata and dynamically resolves the installed instrumentation runner/target relationship.
- Audits and closes all safe compiler/assembly warnings surfaced by the R06 workstation log.
- Retains legacy Google Sign-In/FCM registration-token compatibility explicitly until backend-coordinated migration.
- Adds 20/20 device-closure and 11/11 warning/packaging regressions; preserves PH08 payment and inherited compile/runtime gates.
- Use only `scripts\VC-ANDROID-PH09-R07-ACCEPTANCE.cmd`.

# VoiceCloud Android Changelog

## VC-ANDROID-PH09-R06 Kotlin compiler corrective revision
- Corrects the R05 workstation `:app:compileDebugKotlin` failure in `VoiceCloudRoot.kt`.
- Restores the inherited PH08 `androidx.compose.ui.Modifier` import that PH09 appearance integration accidentally removed.
- Adds a 171/171 PH09 compile-surface/import regression covering inherited imports, Compose/navigation imports, Retrofit/Hilt imports and modified callback boundaries.
- Adds a frozen PH08 inherited-import authority for every PH09-modified existing Kotlin file.
- Negative regression proves deleting the `Modifier` import fails before Gradle compilation.
- Product behavior is unchanged from R05; only one product-source line differs: the restored import.
- Retains R05 Gradle 9.5 / Android Studio tooling-property authority.
- Use only `scripts\VC-ANDROID-PH09-R06-ACCEPTANCE.cmd`.

## VC-ANDROID-PH09-R05 Gradle tooling compatibility correction
- Corrects the R04 false acceptance failure on `org.gradle.tooling.parallel`.
- Explicitly declares `org.gradle.tooling.parallel=true` for the pinned Gradle 9.5/current Android Studio toolchain.
- Replaces closed-world root `gradle.properties` comparison with classified authority: frozen VoiceCloud/build-critical keys remain exact; runtime `org.gradle.*` and `systemProp.*` additions are non-source; unknown product/Android/Kotlin/project properties remain fail-closed.
- Adds a 20/20 Windows/tooling preflight and a 19/19 acceptance-wiring gate.
- PH09 Kotlin/product behavior is unchanged from R04.

# Changelog

## VC-ANDROID-PH09-R05
- Replaces the R01-R03 preservation harness corrections.
- `gradle.properties` is no longer checked by generic file hashing; it has one dedicated semantic PH08-R02 key/value authority.
- Generic PH08 preservation excludes `gradle.properties` and verifies every other preserved baseline file.
- Windows preflight tests UTF-8, UTF-8 BOM, UTF-16LE BOM, CRLF, comments/order/trailing whitespace representations and verifies real payment-mode tampering is detected.
- Use only `scripts\VC-ANDROID-PH09-R05-ACCEPTANCE.cmd`.

# VoiceCloud Android Changelog

## VC-ANDROID-PH09-R03
- Supersedes PH09-R01/R02 acceptance tooling; product implementation is unchanged.
- Replaces line-ending-only preservation with one semantic Windows-safe canonicalization path used by the actual preservation gate.
- Generates the PH08 authority manifest from the accepted PH08-R02 package using the same canonical representation.
- `.properties` files are compared by effective key/value configuration; representation-only BOM/newline/comment/ordering/trailing-whitespace differences do not fail.
- Other text normalizes BOM/newlines/trailing representation details; binaries remain byte-exact.
- Adds an 11/11 integration regression that exercises the actual checker and proves real payment-mode value changes still fail.
- Use `scripts\VC-ANDROID-PH09-R03-ACCEPTANCE.cmd`.

## VC-ANDROID-PH09-R01
- Parent baseline: `VoiceCloud-Android-VC-ANDROID-PH08-R02` exact commit `1739f081ac386acca9a24b37863b72c4db952089`.
- Adds isolated `:feature:settings` for Preferences, Security, Safety, CMS and Support.
- Implements backend-persisted notification, privacy and supported voice/audio preferences.
- Adds real Light / Dark / System application-root appearance authority and preserves centralized Royal Sapphire styling.
- Adds safe session/device overview, detail/revoke, bounded login history and backend-success-first sign-out-all.
- Adds dynamic CMS Help/FAQ/legal content, persisted Contact Support, About and Safety Center.
- Adds human-readable User/Room report selection, contextual profile/live-room reporting and server-backed report history.
- Adds session-expired, maintenance and restricted-account global handling.
- Does not fabricate unsupported password/2FA/trusted-device/report-evidence/live-chat/Admin moderation functions.
- Preserves PH08 payment authority and all inherited PH01-PH08 critical regressions.
- Adds PH09 source, product-quality, security/privacy, preservation and compile-risk gates while retaining the locked Debug/Staging/Release compile-before-test acceptance order.

## VC-ANDROID-PH08-R02
- Parent Git baseline remains `VoiceCloud-Android-VC-ANDROID-PH07-R03` (`fe2e7857802861e2ace2733a313c00c5d27d331d`).
- Completes the PH08 corrective/integration pass for the 14 reported Home, Explore, Search, Friends, Profile, onboarding, economy, media-upload and live-room issues.
- Adds launch-selectable payment authority: `HOSTED_GATEWAY` or `GOOGLE_PLAY`.
- `HOSTED_GATEWAY` delegates Stripe/Razorpay/PayPal selection and all credentials/verification to the backend/Admin provider authority; current testing defaults to this rail.
- `GOOGLE_PLAY` uses Billing Library 9.1.0 ProductDetails, server purchase initiation/verification, restore, and post-verification consume/acknowledge.
- Wallet/VIP never grant local financial entitlements before VoiceCloud verification.
- Adds R02 product-quality, payment, preservation and compile-risk regression gates and retains the locked Debug/Staging/Release compile-before-test workflow.

## VC-ANDROID-PH08-R01
- Parent baseline: `VoiceCloud-Android-VC-ANDROID-PH07-R03` (`fe2e7857802861e2ace2733a313c00c5d27d331d`).
- Implements PH08 Replays, Activity & Extended Profile only.
- Adds replay library/player with server-authoritative access/unavailable/processing handling.
- Adds My Activity from `/room-activity/me`.
- Adds Edit Profile using backend-supported profile fields plus avatar/cover upload/remove.
- Adds blocked-users review/unblock and profile-visitors history/stats.
- Adds Coil-backed remote media loading with explicit loading/error/fallback UI.
- Preserves PH01-PH07 source authority and all PH07 economy/progression behavior.
- Acceptance retains compileDebugKotlin, compileStagingKotlin, compileReleaseKotlin before tests, lint and assemblies.

## VC-ANDROID-PH07-R02
- Corrective build revision for PH07.
- Fixed `:feature:economy:compileDebugKotlin` failure by explicitly opting the economy Compose screens into `ExperimentalMaterial3Api` required by the project Material3 version.
- Added PH07 regression coverage that fails if the required Material3 opt-in is removed.
- No PH07 functional scope change; PH06-R03 preservation baseline remains unchanged.

# VC-ANDROID-PH06-R03

## Corrected
- Fixed PH06 Host Live Console Kotlin compilation failure caused by direct dereference of nullable `RoomStageState?` in Audience filtering.
- Audience filtering now snapshots `stage?.speakers.orEmpty()` and iterates `stage?.participants.orEmpty()` safely.
- Added durable R03 source/compiler regression and Windows acceptance authority.

## Preserved
- Full PH06-R02 premium UI/UX work.
- PH06 Host/Speaker functionality, scheduling, stage controls, polls/quiz and explicit microphone permission behavior.
- All PH01-PH05 accepted/preserved behavior and centralized white-label branding.

## VC-ANDROID-PH07-R01
- Added isolated consumer economy/progression module on PH06-R03.
- Added Google Play Billing 9.1.0 purchase/restore coordinator with server-authoritative validation.
- Added wallet, Android VIP, referrals, gifts, store/inventory, tasks, achievements, XP/check-in, supported rankings and scheduled-room tickets.
- Added PH07 source, preservation, workstation acceptance and manual QA gates.
- Agency ranking and unresolved paid creator-subscription acquisition remain fail-closed.

## VC-ANDROID-PH08-R02 acceptance harness correction
- Preservation verification now treats LF/CRLF-only drift in `gradle/wrapper/gradle-wrapper.properties` as equivalent because the verified Windows Gradle wrapper bootstrap rewrites this canonical text file with CRLF.
- Real wrapper content changes remain rejected; Gradle 9.5.0 and the pinned distribution checksum remain protected by the inherited wrapper/source regressions.

## VC-ANDROID-PH09-R02

- Corrected PH09 preservation acceptance on Windows by canonicalizing LF/CRLF for preserved text files.
- Added a dedicated Windows line-ending regression proving real `gradle.properties` value changes are still detected.
- No PH09 product/API/UI/security/payment/RTC scope change from R01.
- Use `scripts\VC-ANDROID-PH09-R02-ACCEPTANCE.cmd` for workstation acceptance.
