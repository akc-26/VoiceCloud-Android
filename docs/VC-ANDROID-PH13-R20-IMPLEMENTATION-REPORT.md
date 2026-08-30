# VC-ANDROID-PH13-R20 — Physical-Device UI/UX Root-Cause Correction

## Status
COMPLETE SOURCE/PACKAGE CANDIDATE. Windows full build and physical-device acceptance remain mandatory.

## Parent
VC-ANDROID-PH13-R19.

## Why R19 was rejected
The latest physical-device video showed systemic presentation defects rather than isolated screen bugs: overlapping card content, mismatched typography, poor screenshot-fragment fallback artwork, weak iconography, an oversized/awkward startup composition, and a sparse Discover experience.

## Root causes corrected
- `VoiceCloudApprovedCard` previously used a `Box` while feature screens supplied multiple children as if it were a vertical card. Those children could overlap. R20 changes the shared card to a vertically spaced `Column`, fixing this entire class of overlap across Listener, Creator and Host screens.
- Global display/headline/title typography no longer uses serif fonts; the application now uses the clean sans-serif family consistent with the approved boards.
- The center Live action uses a dedicated microphone vector in both Listener and Creator navigation instead of a generic pictogram.
- Seven screenshot-derived/cropped fallback assets were replaced by clean high-resolution artwork with no embedded UI text or screenshot chrome.
- The startup onboarding composition was resized/rebalanced around a controlled hero image, title, copy and actions rather than an oversized image/heading treatment.
- Discover was reconstructed from a sparse category grid into a useful discovery experience with search, trending/live rooms, popular creators, categories, Communities and Events using real backend state.
- Shared pictogram chrome was simplified so gold is reserved for premium/gift/reward semantics rather than appearing as a border/accent on every icon.

## Functional preservation
R19 is the parent product authority. R20 permits presentation-only changes listed in `contracts/VC-ANDROID-PH13-R20-ALLOWED-PRODUCT-CHANGES.txt`; API, repository, ViewModel, model, navigation authority, RTC, security, payment and business logic remain protected by hash regression.

## Acceptance
Run `scripts\VC-ANDROID-PH13-R20-ACCEPTANCE.cmd` on Windows. Full acceptance still requires Debug/Staging/Release compile, unit tests, lint, assemblies and physical-device instrumentation.
