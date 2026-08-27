# VC-ANDROID-PH05-R01 Implementation Report

## Baseline
PH05-R01 is based on frozen Android branch `VoiceCloud-Android-VC-ANDROID-PH04-R04`, exact parent commit `702cf4c1ae966cf826ddbc64c6cd86487f6d91e9`. Backend authority is `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06` at `7bee8d463786d21a7200b5663a499408bb229115`.

## Phase objective
Implement the locked User-portal live-room listener experience without pulling PH06 microphone/host publishing forward, while preserving all previously accepted Android architecture and regressions.

## Implemented architecture
A new isolated `:feature:live` module owns PH05 UI, API mapping, access presentation, realtime room monitoring, listener state and LiveKit receive-only audio. Existing `:core:network`, `:core:security` and `:core:realtime` remain the shared authorities for authenticated REST/token/socket foundations.

## REST coverage
PH05 maps the finalized backend contracts for room detail, RTC join/rejoin/leave, participant presence, raise/lower hand, room conversation/messages/reactions, saved rooms, activity tracking and gift catalogue/send operations. Backend error states are converted to consumer-safe access messages rather than exposing provider/key/internal information.

## Listener RTC behavior
LiveKit uses `autoSubscribe=true`, `audio=false`, `video=false`. This receives remote room media while preventing PH05 from publishing microphone/camera tracks. `RECORD_AUDIO` is not added to the manifest and PH06 publishing controls are absent.

## Lifecycle/concurrency hardening
`LiveRoomViewModel` owns a monotonically increasing session generation and explicit jobs for join, rejoin, extras, participants and messages. Leave/end/navigation invalidates the generation and cancels all room jobs before asynchronous cleanup. Every suspended result verifies that it still belongs to the active room before mutating UI state.

The shared `RtcAudioEngine` has an independent atomic connection generation. A stale LiveKit connect can no longer claim ownership after a newer room connection starts, and stale cleanup cannot disconnect the newer room.

## Realtime/engagement
The phase handles participant presence, hand/stage invitation transitions, room pause/resume/end, RTC lifecycle signals, reactions and gifts through the existing authenticated realtime foundation. Room chat remains backed by finalized HTTP conversation/message contracts where that is the established backend behavior.

## Professional UI and white-label additions
The approved PH05 additions are included in this delivery:
- real Home/Explore/Search/Friends/Profile vector tab icons;
- AndroidX SplashScreen with one temporary replaceable splash drawable;
- one root `branding/` authority for brand identity, app ID, visible app name, colors, gradients, shapes/motion and replaceable splash/app-icon assets.

The internal Kotlin package remains stable deliberately; it is implementation namespace, not customer-facing branding.

## Preservation
No unrelated PH01-PH04 product workflow was rewritten. Existing auth/session/security/database/realtime/discovery/engagement protections are retained and executed from PH05 acceptance.
