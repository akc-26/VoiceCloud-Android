# VoiceCloud Android VC-ANDROID-PH13-R14 — Physical-Device-Driven Full UI/UX Correction

## Why R14 exists
The PH13-R13 application was run on a real Android device. That run proved the app could launch and navigate, but it also exposed product-quality defects that source-only checks had not caught: the centralized text formatter removed whitespace, backend media was underused, large single-letter fallbacks dominated profile surfaces, and repeated generic pictograms made unrelated pages look like one template rather than a finished VoiceCloud product.

R14 therefore is not an acceptance-script-only revision. It is a consolidated presentation correction across the complete Listener and Creator/Host product while preserving working functional authority.

## Protected product authority
R14 keeps API, repository, ViewModel, model, navigation, role/capability, RTC/realtime, security, permissions, wallet/payment and business-rule source protected from R13. The R14 preservation gate hashes the R13 Android product/build-input authority and permits drift only in the explicit presentation/test/dependency allowlist plus the new centralized media component.

## Global UI corrections
- Replaced the destructive short-label formatter with whitespace-preserving formatting; long body copy keeps natural sentence casing.
- Added device instrumentation assertions for readable `Welcome to VoiceCloud` text and long descriptive copy so the exact physical-device regression cannot silently return.
- Added centralized Coil-backed `VoiceCloudRemoteMedia` and `VoiceCloudAvatar` components so real backend avatar/cover/image URLs render whenever supplied.
- Replaced giant single-letter media fallbacks with polished VoiceCloud artwork; initials remain only where the runtime model genuinely supplies no media URL (for example RTC presence-only participants).
- Reworked shared page heroes into broader, richer page-specific artwork compositions with stronger phone typography and multi-line body copy.
- Reduced repetitive gold outlining and moved ordinary cards toward neutral premium borders/elevation, retaining gold as a restrained premium/live accent.
- Simplified the common top bar to improve title/subtitle space and reduce repeated decorative pictograms.
- Increased the smallest product label typography for physical-phone legibility.

## Listener / End User corrections
- Bootstrap and system states: richer brand/artwork presentation without altering bootstrap decisions.
- Portal selector and authentication: dedicated branded hero, improved portal cards and readable form hierarchy.
- Home/Discover: backend room cover art, richer featured live presentation, real avatars, richer room/host/trending cards and stronger hierarchy.
- Explore/Rooms/Search/People: data-driven room artwork and user avatars instead of placeholder-like repeated illustrations.
- Public/My Profile: real cover/avatar media, better profile identity hierarchy and less sparse composition.
- Profile tools/Edit Profile/Replays/Visitors: removed large letter cover/avatar placeholders; preserved real remote media and polished fallback art.
- Communities/Events/Messages: community image/banner, event cover and conversation avatar media are rendered from backend state.
- Listener room preview/live room: real room cover art added while retaining immersive live audio, waveform, speaking, participants, reactions/chat/gifts and RTC state authority.
- Economy: richer wallet/VIP/reward art and item/payment cards; all purchase/restore/entitlement logic remains server-authoritative.
- Settings/Security/Safety: richer navigation and audio/security presentation with improved readability and the same existing actions.

## Creator / Host corrections
- Creator portal frame: cleaner brand/title hierarchy and less redundant decoration.
- Creator Dashboard/Live Studio: richer room/schedule visuals and polished empty states.
- Creator Profile: real backend avatar presentation rather than generic identity treatment.
- Creator analytics/wallet/earnings/gifts/payouts/audience/plans/support retain their existing callbacks/business authority while inheriting the corrected premium page system.
- Host Studio/room editor/schedule/management: page-specific room/live/schedule artwork while retaining all existing fields and lifecycle actions.
- Host live console remains intentionally immersive/dark and preserves RTC/socket/moderation authority.

## Screen coverage
The R14 source retains all 87 named feature screens from the PH13 screen inventory: 17 Auth, 17 Creator, 9 Discovery, 2 Economy, 9 Engagement, 7 Hosting, 2 Live, 9 Profile and 15 Settings screens. R14 quality changes are centralized where possible and targeted where the physical-device recording demonstrated that shared styling was insufficient.

## Room schema / workstation correction
`core/database` is configured to export Room schemas. Files matching only:

`core/database/schemas/app.voicecloud.core.database.VoiceCloudDatabase/*.json`

are therefore classified as generated build evidence by R14 source hygiene and delivery checks. Arbitrary files elsewhere remain fail-closed. The full Gradle acceptance runs in an isolated workspace so Room/Gradle outputs cannot contaminate the delivered source tree.

## Acceptance policy
R14 restores the complete acceptance sequence. It is not fully accepted until the Windows runner passes:
1. source/regression diagnostics;
2. `compileDebugKotlin`;
3. `compileStagingKotlin`;
4. `compileReleaseKotlin`;
5. unit tests;
6. Debug/Staging/Release lint;
7. Debug/Staging/Release + DebugAndroidTest assemblies;
8. physical-device instrumentation;
9. immutable delivery integrity.

If no healthy authorized physical device is available, the runner exits `2` and reports **PENDING**, not accepted.
