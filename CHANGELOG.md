## VC-ANDROID-PH13-R19
- Consolidates the latest End User + Creator physical-device video findings instead of patching one acceptance-log symptom at a time.
- Corrects Listener bottom navigation semantics to Home / Discover / Live / Messages / Profile; the center microphone/live action no longer opens Search.
- Separates Home (`For You`) from Discover/Search and strengthens Home creator discovery/quick access.
- Reworks My Profile around backend cover/avatar identity and profile actions rather than a wallet/settings-first hub.
- Removes the misleading standalone Speaker portal.
- Makes Creator Studio consume existing Host access authority before exposing rooms/schedules/go-live controls; non-approved Creators receive Host-access guidance.
- Reworks Creator Dashboard low-data composition and Creator Profile overview/edit flow while preserving backend-authoritative analytics/economy values.
- Adds dedicated video-product, preservation, compile-surface, workstation-artifact and full acceptance gates.

## VC-ANDROID-PH13-R18
- Corrected all stale `VoiceCloudRemoteMedia` named-argument callsites across Economy, Hosting and Creator (`label` -> `contentDescription`, `fallbackKind` -> `kind`).
- Removed the ten Economy unused imports and obsolete `glyph()` helper reported by Android Studio.
- Added project-wide VoiceCloud component named-argument/signature regression and retained the full R16 design + R17 compiler/UTF-8 closure.

# VC-ANDROID-PH13-R17

- Parent: VC-ANDROID-PH13-R16.
- Preserves the approved R16 page-by-page UI/UX implementation and all functional/API/RTC/payment/navigation authority.
- Closes the real R16 Kotlin compiler failure in `VoiceCloudSpeakingAvatar`: the public `size: Dp` parameter shadowed `DrawScope.size`, so Canvas geometry now captures `val canvasSize = this.size` before reading width/height/minDimension.
- Closes the Windows Python 3.14 `cp1252` diagnostic crash by forcing UTF-8 mode in the acceptance CMD and PowerShell diagnostic launcher and making the exact R16 compile-surface checker use explicit UTF-8 reads.
- Adds R17 regression coverage for DrawScope-size shadowing, Compose extension imports across the R16-changed UI surface, callback wiring, locale-independent diagnostics, workstation artifacts and fail-closed delivery integrity.
- Full Debug/Staging/Release compile, unit tests, lint, assemblies and physical-device instrumentation remain mandatory on the Android workstation before acceptance.

# VC-ANDROID-PH13-R16

- Rebuilt the Android presentation against the approved End User + Creator/Host design boards while preserving R15 functional authority.
- Added compact board-derived components, board typography/spacing metrics, backend-media-first artwork treatment and approved board-derived fallback assets.
- Reconstructed Auth/Onboarding, Discovery/Search/Community, Listener Live, Economy/Profile/Settings, Creator Dashboard/Growth, Room Creation/Scheduling, Host Controls/Moderation and Creator Showcase surfaces.
- Removed the rejected generic feature `VoiceCloudPageHero` composition from feature-screen usage.
- Preserved whitespace in application-authored title handling and added board-like OTP digit entry and registration terms consent.
- Removed fabricated hard-coded Creator analytics/earnings time-series charts; backend metrics remain authoritative and unavailable series are represented honestly.
- Added R16-specific design-fidelity, product-preservation, compiler-surface, workstation-artifact, hygiene, harness and delivery-integrity gates.
- Restored the full Windows acceptance sequence: Debug/Staging/Release Kotlin compile, unit tests, lint, all assemblies, physical-device instrumentation and final integrity.

## VC-ANDROID-PH13-R15
- Closed the actual R14 Kotlin compiler failure: missing `androidx.compose.ui.draw.clip` import in HostingScreens.
- Removed the remaining redundant-Elvis compiler warning in DiscoveryScreens.
- R14 intended UI/UX and functional/API/RTC/payment/navigation authority otherwise unchanged.

# VC-ANDROID-PH13-R14 — Physical-Device-Driven Full UI/UX Correction

- Parent: `VC-ANDROID-PH13-R13`.
- Corrects the real-device systemic whitespace defect and adds device instrumentation assertions that prevent its return.
- Reworks shared premium heroes/cards/top bars/typography and adds centralized Coil-backed backend media/avatar presentation.
- Replaces giant letter placeholders and expands real cover/avatar/community/event/conversation media across Listener + Creator/Host surfaces.
- Applies targeted Home/Auth/Profile/Live/Economy/Settings/Creator/Host corrections while protecting API/repository/ViewModel/navigation/RTC/security/payment/business authority.
- Restores the full Debug/Staging/Release compile + tests + lint + assemblies + physical-device acceptance sequence.
- Treats only the configured Room database schema JSON path as generated build evidence; arbitrary drift remains fail-closed.

## VC-ANDROID-PH13-R12

- Acceptance-only correction: preservation now ignores the same known workstation-generated artifacts as delivery integrity.
- Added exact R11 contamination simulation and fail-closed unknown-drift checks.
- Fixed Windows batch device exit-code handling using delayed expansion.
- Android product/UI/API/runtime source unchanged from build-proven R09/R10/R11.

# VC-ANDROID-PH13-R11 — R09 Device Acceptance Closure

- Parent: `VC-ANDROID-PH13-R09`.
- Android product/UI/API/runtime/build-input source remains byte-identical to R09.
- Uses the supplied R09 workstation result as build evidence: Debug/Staging/Release compile, unit tests, lint and assemblies all passed; only Gate 7 failed because ADB install timed out on the selected wireless OnePlus device.
- Hardens physical-device APK installation with exact installed-APK SHA-256 verification, non-incremental direct install, one transport recovery, and one push + package-manager fallback.
- Adds a fast path that reuses the still-present hash-verified R09 isolated workspace/build artifacts.
- Adds a bounded fallback that builds only `assembleDebug` + `assembleDebugAndroidTest`, avoiding another ~30-minute full acceptance cycle for unchanged product source.
- Retains fail-closed device/instrumentation behavior and delivery integrity.

# VC-ANDROID-PH13-R09 — Full Page-by-Page Premium UI/UX

- Parent: `VC-ANDROID-PH13-R08` (working/build-accepted user baseline).
- Applies the approved glossy premium VoiceCloud visual quality across all existing End User and Creator/Host feature screen groups rather than a theme-only pass.
- Adds centralized pictorial/gloss primitives, audio/poster artwork, page heroes, metric tiles, rich empty states and mini charts.
- Current source inventory contains 87 named feature screens; R09 regression requires every named screen to enter a premium visual shell.
- Preserves R08 APIs, repositories, ViewModels, models, navigation authority, security, RTC/realtime, payment/economy authority, permissions and business rules.
- R09 source closure: acceptance wiring 18/18 PASS, full UI compile-risk/coverage 55/55 PASS, product preservation PASS, applicable inherited source chain 26/26 PASS.
- Use `scripts\VC-ANDROID-PH13-R09-ACCEPTANCE.cmd` for real Windows Debug/Staging/Release compilation, tests, lint, assemblies, device instrumentation and delivery integrity.

# VC-ANDROID-PH13-R07

- Corrected real Kotlin/Compose compilation failures reported by PH13-R06 workstation acceptance.
- Fixed `drawArc` overload usage in `VoiceCloudBrandMark.kt` with named `style = cloudStroke`.
- Replaced unresolved `matchParentSize` with supported `fillMaxSize` in `VoiceCloudPremium.kt`.
- No feature, API, navigation, business-rule, permission, RTC, repository, ViewModel, or model changes.
- Added fail-closed R07 compiler-correction/preservation regressions and faster dependent-gate handling.

# VC-ANDROID-PH13-R06 — R05 Acceptance Harness Correction

- Parent package: `VC-ANDROID-PH13-R05`.
- No Android product/UI/API/runtime change; R05 implementation is preserved byte-for-byte.
- Fixes the R05 wrapper-bootstrap defect that generated from the verified local Gradle ZIP and then rewrote runtime back to `services.gradle.org`.
- Keeps compile/test/lint/assembly wrapper execution pinned to the verified local Gradle 9.5.0 ZIP.
- Makes product/source integrity ignore known Android Studio/Gradle machine artifacts while remaining hash fail-closed for tracked files and fail-closed for unknown unmanifested product files.
- Uses the cached Gradle executable for daemon cleanup to avoid the prior network-fetch cleanup path.
- Use only `scripts\VC-ANDROID-PH13-R06-ACCEPTANCE.cmd` for workstation/device acceptance.

# VC-ANDROID-PH13-R02 — Gate 8 Acceptance Harness Correction

- Product/Kotlin implementation is unchanged from PH13-R01.
- Corrects the post-build Gate 8 false failure caused by Windows wrapper-bootstrap CRLF rewriting of `gradle/wrapper/gradle-wrapper.properties`.
- Separates post-build tracked-source integrity from distributable-ZIP cleanliness.
- Restores canonical wrapper properties and removes generated wrapper scripts/JAR before final source-integrity verification.
- Keeps build/cache directories permissible only in the tested workstation tree while continuing to forbid them in the delivered ZIP.
- Adds PH13-R01 → PH13-R02 preservation and corrective regression gates.
- Use only `scripts\VC-ANDROID-PH13-R02-ACCEPTANCE.cmd`.

# VC-ANDROID-PH13-R01 — Creator Analytics, Economy & Host Verification

- Adds Creator Analytics, wallet, earnings, gift history, payout request create/list/detail and Creator Notifications.
- Adds Host eligibility/application, progression and private verification asset upload/replacement.
- Keeps all financial state server-authoritative and adds idempotency keys to payout submission.
- Enforces the Android prohibition on stream-key/RTMP/provider-secret reveal or regeneration.
- Preserves the complete PH12 package outside the explicit PH13 allowlist and adds PH13 acceptance/regression gates.

# VC-ANDROID-PH12-R01 — Creator Audience, Messaging, Followers & Subscribers

- Added Creator Audience, follower metrics/search/sort/follow-back, direct-only Creator messaging, subscription plan create/update/archive, subscriber list/status metrics, and PH12 acceptance/preservation gates.
- Reused existing Discovery/Engagement authorities and kept subscription/payment state server-authoritative.
- Parent baseline: `VoiceCloud-Android-VC-ANDROID-PH11-R02` @ `fad14e236a780cbe14a74a4676a3ad94237d9bbe`.

# VC-ANDROID-PH11-R02
- Parent remains `VoiceCloud-Android-VC-ANDROID-PH10-R01` @ `999a3c310c9e40bf1a47972551050bfd5b00e54d`; PH11-R01 was not Git-frozen.
- Corrects the R01 workstation unit-test failure after all three Kotlin compile variants passed.
- Updates Creator unit authority to include the intentional PH11 `LIVE` section while continuing to reject financial/payout scope.
- Makes Privacy/Contact reflection tests ignore compiler-generated JVM fields and assert only backend business fields.
- Adds an 8/8 R02 unit-contract regression and R02 acceptance wiring.
- PH11 production implementation remains unchanged from R01.
- Use only `scripts\VC-ANDROID-PH11-R02-ACCEPTANCE.cmd`.

# VC-ANDROID-PH11-R01
- Parent: `VoiceCloud-Android-VC-ANDROID-PH10-R01` @ `999a3c310c9e40bf1a47972551050bfd5b00e54d`.
- Implements Creator Live Rooms & Schedule using the existing server-authoritative Hosting/LiveKit foundation.
- Adds Creator Live Studio, room lifecycle, Live Console/stage controls, schedule CRUD and scheduled start.
- Implements the requested Title Case, toast/error, media crop, Privacy/Device/Contact contract, CMS audience, navigation simplification, economy visual redesign and Portal Selector corrections.
- Adds PH11 source/backend-contract/Title Case-toast/corrections/compile/economy/preservation gates while retaining compatible inherited Gradle, device, warning, ADB and hosting authorities.
- Use only `scripts\VC-ANDROID-PH11-R01-ACCEPTANCE.cmd`.

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


## VC-ANDROID-PH13-R03
- Replaced post-build mutation classification with an isolated temporary Gradle workspace.
- Prevents Gradle-generated `gradle/gradle-daemon-jvm.properties`, wrapper files, caches, lint reports and build outputs from ever mutating the delivery/Git source tree.
- Gate 8 now verifies the untouched original delivery manifest strictly after the isolated workspace is removed.
- PH13 product/Kotlin implementation remains unchanged from PH13-R01/R02.
## VC-ANDROID-PH13-R04
- Product/Kotlin/API implementation remains unchanged from PH13-R01/R02/R03.
- Replaces fail-fast acceptance discovery with a complete diagnostic pass: independent source checks and Android build gates continue after failures and aggregate all findings at the end.
- Gate 8 original delivery-tree integrity now always runs after the isolated build attempt, even when an earlier build/cleanup diagnostic reports failure.
- Gives each acceptance run a private `GRADLE_USER_HOME`, disables persistent Gradle daemons, explicitly stops the run's Gradle daemon, and uses bounded Windows cleanup retries.
- Separates genuine source/build failures from non-source temporary-workspace cleanup warnings.
- Corrects the stale README top-level revision label that remained PH13-R02 in the R03 package.
- Retains strict distributable/Git-source hygiene: build/cache/wrapper/daemon-JVM artifacts remain forbidden from the original delivery tree.


## VC-ANDROID-PH13-R08 — Premium UI compile closure
- Fixed the remaining compiler error reported by the PH13-R07 workstation acceptance run: `AuthScreens.kt` now binds the premium waveform animation to the real `AuthUiState.busy` property instead of nonexistent `state.loading`.
- Audited the complete premium UI change surface against declared UiState/model/design-token contracts.
- Added a full UI compile-risk regression covering every redesigned presentation file, centralized premium primitives, RTC presence fields, economy enum references, and theme token references.
- Compile gates now use Gradle `--continue` so independent feature-module compiler errors are surfaced in the same run rather than serially hidden behind the first failing module.
- No APIs, repositories, ViewModels, models, navigation behavior, permissions, RTC/realtime authority, security behavior, wallet/economy rules, or backend business logic changed.

## VC-ANDROID-PH13-R13
- Corrected the stale physical-device instrumentation palette assertions left from the pre-redesign color authority.
- `ConsumerColors.Text` instrumentation expectation now matches `consumer.text=#18312D`.
- `ConsumerColors.Sapphire` instrumentation expectation now matches `consumer.sapphire=#006C63`.
- Production Android app/UI/API/runtime source remains unchanged from the build-proven R09/R12 authority.
- Added a verified prior-workspace fast path that rebuilds only the androidTest APK when the prior Debug app APK is available.

## VC-ANDROID-PH13-R20
- Corrected systemic UI overlap by changing the shared approved card container from Box stacking to vertically spaced Column layout.
- Aligned global typography to clean sans-serif approved-board hierarchy.
- Replaced screenshot-fragment fallback artwork with clean high-resolution VoiceCloud visual assets.
- Added dedicated Live microphone navigation icon for Listener and Creator center actions.
- Rebalanced startup/onboarding composition and reconstructed Discover into a content-rich experience.
- Preserved R19 functional/API/RTC/payment/security/business authority.
