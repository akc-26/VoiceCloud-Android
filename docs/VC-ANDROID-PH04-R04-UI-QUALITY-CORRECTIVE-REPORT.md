# VC-ANDROID-PH04-R04 UI Quality Corrective Report

## Trigger
Physical-device review of PH04 identified a shared presentation problem rather than isolated page defects:

1. Secondary routes such as Communities and another user's Profile could render too close to the Android status bar.
2. Consumer presentation did not visually match the current VoiceCloud Website closely enough, especially hero gradients and Material surface/card colors.
3. Equal-width action labels such as Communities and Messages could wrap or appear misaligned.
4. Secondary-page navigation used inconsistent text-based Back controls and inconsistent title geometry.

## Root causes
- Main-tab pages were hosted by a common Scaffold, while several secondary routes owned their own top content and therefore did not share one page-chrome contract.
- Edge-to-edge window handling did not provide one application-level safe drawing boundary for every route.
- Secondary features implemented local Back/title rows instead of a design-system top bar.
- Material 3 surface-container roles were not fully assigned, allowing default Material tones to appear on elevated components.
- Some Android hero/avatar gradients used Sapphire-to-Indigo combinations rather than the current Website presentation gradients.
- Equal-width shortcut actions relied on default text wrapping.

## Corrective architecture
### Global system-bar safety
`VoiceCloudRoot` now owns `safeDrawingPadding()` once for the application content while preserving the branded background behind system bars. Main and secondary routes inherit the same safe drawing boundary.

### Shared secondary-page chrome
A reusable `VoiceCloudPageTopBar` now owns:
- icon-only back arrow on the left;
- accessibility description for Back;
- centered, single-line, ellipsized page title;
- stable 64dp navigation row;
- optional centered subtitle and right-side action;
- consistent surface/elevation.

Discovery/Profile and Engagement secondary routes consume this component instead of inventing local headers.

### Website-aligned consumer presentation
The centralized Android consumer palette remains based on the finalized R06 Website presentation values and now also includes the Website presentation gradients:
- primary gradient: `#087E8A -> #075F70`;
- hero gradient: `#F5FCFD -> #ECF9FA -> #DEF5F7`.

Home, public profile, communities and avatars consume centralized brushes rather than local teal-to-indigo hero gradients.

Material 3 `surfaceContainer*`, `surfaceBright`, `surfaceDim`, `outlineVariant`, inverse surface and surface tint roles are explicitly branded so Card/ElevatedCard/Navigation surfaces do not fall back to unrelated library defaults.

Radii are mapped centrally to 12/18/24/30dp Material shapes.

### Button/text resilience
- Home shortcuts use equal RowScope widths, 44dp minimum height and 12sp semibold single-line labels.
- PH04 quick actions retain the R02 RowScope compiler correction, add a 76dp minimum card height and force single-line labels.
- Community paired actions use reduced content padding and single-line labels.
- Secondary titles/actions are constrained to one line with ellipsis where required.

### Product-copy cleanup
User-facing PH03/PH04/PH05, authority, canonical/finalized-API and other implementation/roadmap language was removed from consumer UI. Regression assertions were changed to verify the underlying architecture instead of forcing developer copy back into the product.

## Production source delta from PH04-R03
Only these seven production/build files change:
- `app/build.gradle.kts` (direct Compose Foundation dependency for root safe-area API)
- `app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt`
- `core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt` (new)
- `core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudColors.kt`
- `core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt`
- `feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt`
- `feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt`

No API, repository, authentication, token/security, Firebase/FCM, database, realtime or navigation-contract implementation is changed by R04.

## Regression policy
R04 adds a durable UI-quality regression that protects the shared rules above and retains every PH01-PH04-R03 regression, including the PH03 duplicate-key crash protection, the PH04-R02 Compose RowScope fix, and the PH04-R03 healthy-device instrumentation gate.
