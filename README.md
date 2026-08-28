# VoiceCloud Android — VC-ANDROID-PH08-R02

PH08-R02 is the consolidated corrective/integration revision built on the frozen PH07-R03 Git baseline.

## Parent authority
- Git branch: `VoiceCloud-Android-VC-ANDROID-PH07-R03`
- Exact parent commit: `fe2e7857802861e2ace2733a313c00c5d27d331d`
- PH01-PH07 behavior remains inherited; PH08-R01 replay/activity/extended-profile scope remains present.

## PH08-R02 completion scope
- Live-room dominant Home with centralized VoiceCloud branding
- differentiated Explore experience
- nonblank Search landing with People, Creators, Rooms and Communities plus View all and tab-specific empty states
- completed Friends requests/suggestions/cancellation flow
- Profile-first Economy & Progression navigation with corrected priority order
- structured Economy data/actions rather than raw payload rendering
- CMS-driven Help, Terms & Information pages
- first-setup-only onboarding
- avatar/cover create + replace flows with recommended resolutions
- smooth navigation transitions and arrow-based back navigation
- professional live-room header, persistent chat, leave action, emoji/gift sheets and direct room entry
- PH08-R01 replay/activity/extended-profile functionality preserved

## Payment rails
The APK exposes only two launch modes:
- `HOSTED_GATEWAY` — backend/Admin selects the active healthy hosted provider. Stripe is the current testing provider; Razorpay and PayPal use the same Android flow when Admin switches the backend provider.
- `GOOGLE_PLAY` — native Google Play Billing 9.1.0 with ProductDetails query, purchase/restore, VoiceCloud server verification and only then consume/acknowledge.

Default in this package:
```properties
VOICECLOUD_ANDROID_PAYMENT_MODE=HOSTED_GATEWAY
```
For a Google Play launch build, change only the Gradle property to:
```properties
VOICECLOUD_ANDROID_PAYMENT_MODE=GOOGLE_PLAY
```
Provider credentials remain server-side and are never compiled into the APK.

## Full Windows acceptance
```powershell
scripts\VC-ANDROID-PH08-R02-ACCEPTANCE.cmd
```

The acceptance order is locked: source/regression gates, `compileDebugKotlin`, `compileStagingKotlin`, `compileReleaseKotlin`, unit tests, lint, assemblies, then physical-device instrumentation.

## Documentation
- `docs/VC-ANDROID-PH08-R02-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH08-R02-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH08-R02-MANUAL-QA.md`

PH08-R02 implementation/package verification is not a substitute for the Windows Gradle/device acceptance run.
