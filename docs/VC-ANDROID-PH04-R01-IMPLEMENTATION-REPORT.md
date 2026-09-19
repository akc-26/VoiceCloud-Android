# VC-ANDROID-PH04-R01 Implementation Report

## Baseline
- Parent: `VoiceCloud-Android-VC-ANDROID-PH03-R02`
- Parent commit: `3665f606e43c677d7d2e4652e2a6451d3fe459a4`
- Backend authority: `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06` / `7bee8d463786d21a7200b5663a499408bb229115`

## Architecture
PH04 is isolated in `:feature:engagement`. Existing PH01-PH03 feature/foundation implementations are preserved. App-layer changes are limited to module dependency, navigation, Android notification service/permission wiring and Firebase default-app bootstrap required by PH04.

## Communities
Implemented community list/detail/create/update/delete, join/leave, current membership, member list, private invite-code join, invite-code rotation, member-role management and community scheduled events. Consumer member identities fail closed unless the authoritative profile is a `USER` or `CREATOR`.

## Events
Implemented scheduled-event list/detail and reminder lifecycle. PH05 RTC/live-room join behavior and premium-event purchase are deliberately not introduced in PH04.

## Messaging
Implemented direct-message conversation list, conversation loading, sending, read state and a five-second HTTP refresh cadence. No unsupported chat socket conversation protocol is invented.

## Notifications and FCM
Implemented notification list/unread/read/read-all/delete, authenticated realtime refresh listeners, Firebase Messaging service, current/refreshed FCM token registration, Android 13+ notification permission and allowlisted deep routing to PH04 destinations.

The app does not depend on a committed `google-services.json`. Firebase default-app initialization reuses the centralized public PH02 BuildConfig values and fails closed when configuration is unavailable.

## Preservation
PH03-R02 duplicate lazy-list-key protection is retained and executed during PH04 acceptance. PH02 authentication/bootstrap/Firebase login/session authority and PH01 foundation regressions remain part of the cumulative gate.

## Delivery status
Implementation/source authority is complete. Final Android build/device acceptance must be executed on the user's Windows Android workstation using `scripts\\VC-ANDROID-PH04-R01-ACCEPTANCE.cmd`.
