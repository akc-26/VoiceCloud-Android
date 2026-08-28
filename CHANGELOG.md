# VoiceCloud Android Changelog

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
