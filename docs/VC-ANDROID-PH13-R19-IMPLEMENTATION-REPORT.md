# VC-ANDROID-PH13-R19 — Consolidated Physical-Device Video Product Correction

## Status
**COMPLETE SOURCE/PACKAGE CANDIDATE. WINDOWS FULL BUILD + PHYSICAL-DEVICE ACCEPTANCE PENDING.**

## Authority
- Functional/build baseline: the R15 Android core that passed Debug/Staging/Release compilation, unit tests, lint and all assemblies on the user's Windows workstation, carried forward through R18.
- Visual authority: the nine approved high-resolution VoiceCloud End User + Creator/Host boards retained under `docs/reference/approved-r16/`.
- Rejection evidence: the latest End User and Creator physical-device video review. R19 corrects the product/navigation problems exposed there rather than patching only the last acceptance-log error.
- Backend/API/business truth takes precedence over illustrative names, balances, rankings or charts contained in static design boards.

## Consolidated corrections

### End User / Listener information architecture
- Bottom navigation now has product-meaningful **Home / Discover / Live / Messages / Profile** behavior.
- The center microphone/live control no longer opens Search. It opens the live-room directory/listening path.
- Search remains an explicit Discover/header capability instead of masquerading as a live-audio action.
- Home is presented as `For You`; Discover is a separate destination.
- Home adds creator discovery and purposeful quick-access sections instead of collapsing discovery and search into one generic page.

### End User profile
- My Profile now leads with backend cover/avatar media, identity and stats.
- Wallet is no longer the dominant profile hero; Edit Profile and Wallet are compact intentional actions beneath the identity area.
- Existing profile, wallet, settings and safety destinations remain reachable without turning the profile itself into a settings menu.

### Authentication / portal authority
- Removed the misleading standalone `Speaker` portal. Speaker is a room participation state, not a separate account portal.
- Portal selection exposes Listener and Creator experiences only.
- Creator copy no longer implies automatic Host/create-room authority.

### Creator / Host architecture
- Creator bottom navigation presents Studio rather than a generic Rooms destination and uses a live-audio visual rather than an unexplained plus button.
- Creator Dashboard receives a real empty/low-data dashboard composition with honest Rooms/Audience/Earnings states and purposeful Creator actions.
- `CreatorLiveStudioScreen` now consumes actual Host approval state. Go Live, Schedule and Host room controls are visible only when Host status is `APPROVED`.
- A Creator who is not an approved Host receives a clear Host-access state and verification/review action while normal Creator tools remain available.
- `HostingViewModel.loadCreatorStudio()` now calls the existing `hostAccess()` authority before loading Host rooms/schedules; it does not invent a new backend contract.
- Creator Profile opens as a profile overview. Editing is entered explicitly via Edit Profile and can be saved/cancelled.

## Functional preservation
R19 changes exactly five existing product files. API definitions, repositories, models, RTC/realtime implementation, security, payment/economy business authority and backend contracts remain protected. The one ViewModel change consumes the already-existing Host-access repository authority to enforce a product rule that the prior UI failed to respect.

The product baseline and allowlist are stored in:
- `contracts/VC-ANDROID-PH13-R19-PH13-R18-PRODUCT-CANONICAL.sha256`
- `contracts/VC-ANDROID-PH13-R19-ALLOWED-PRODUCT-CHANGES.txt`

## Acceptance
Run on Windows from a freshly extracted package:

```bat
scripts\VC-ANDROID-PH13-R19-ACCEPTANCE.cmd
```

The acceptance remains fail-closed for real source/build/test/lint/assembly/device failures. A missing healthy device is reported as PENDING, never as full acceptance.
