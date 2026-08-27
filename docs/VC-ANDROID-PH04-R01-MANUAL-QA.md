# VC-ANDROID-PH04-R01 Manual QA

Run after `scripts\\VC-ANDROID-PH04-R01-ACCEPTANCE.cmd` passes and install the Debug APK on a Tailscale-connected Android device.

## Communities
- **PH04-QA-001** Open Home and enter Communities; list loads without crash.
- **PH04-QA-002** Open a public community; detail/membership state is correct.
- **PH04-QA-003** Join and leave a public community; state refreshes correctly.
- **PH04-QA-004** Join a private community with a valid invite code; invalid code fails with a user-safe error.
- **PH04-QA-005** Create a community with required fields and verify it appears in the list.
- **PH04-QA-006** As authorized owner/admin, edit community details and verify persistence.
- **PH04-QA-007** Owner rotates invite code; old/new behavior matches backend authority.
- **PH04-QA-008** Open members; privileged/non-consumer identities are not exposed as public consumer identities.
- **PH04-QA-009** Authorized role management works for Owner/Admin/Moderator/Member rules; unauthorized action is blocked.
- **PH04-QA-010** Owner delete flow removes the community and returns safely to list.

## Events
- **PH04-QA-011** Open Events and an event detail; date/time/content render correctly.
- **PH04-QA-012** Open community events from community detail.
- **PH04-QA-013** Add/remove an event reminder and verify state refreshes.
- **PH04-QA-014** Confirm no PH05 listener/RTC join controls are incorrectly introduced by this phase.

## Messaging
- **PH04-QA-015** Open Messages inbox; direct conversations load.
- **PH04-QA-016** Open a conversation; messages load in correct order without duplicate-key/scroll crash.
- **PH04-QA-017** Send a direct message and verify it appears/persists.
- **PH04-QA-018** Verify opening/reading a conversation updates read state.
- **PH04-QA-019** From another user's public profile, choose Message and verify the correct direct conversation opens/creates.
- **PH04-QA-020** Leave a conversation open for multiple refresh cycles and verify no duplicate messages/crash.

## Notifications / FCM
- **PH04-QA-021** Open Notifications; list and unread state load.
- **PH04-QA-022** Mark one notification read; state updates.
- **PH04-QA-023** Mark all read; unread count clears.
- **PH04-QA-024** Delete a notification; it disappears and remains deleted after refresh.
- **PH04-QA-025** On Android 13+, notification permission is requested through the app flow and denial does not crash the app.
- **PH04-QA-026** With valid Firebase public config, FCM token registration succeeds after login/token refresh.
- **PH04-QA-027** With missing Firebase public config, notification foundation fails closed without app crash or secret exposure.
- **PH04-QA-028** Test notification routes for conversation, scheduled event, community and supported external route; each opens only an allowlisted destination.
- **PH04-QA-029** Authenticated realtime notification changes refresh the notification UI without requiring logout/login.

## Regression
- **PH04-QA-030** Re-test PH03 Home scrolling through People/Creators repeatedly; no duplicate-key crash.
- **PH04-QA-031** Re-test User login/session restore/logout from PH02.
- **PH04-QA-032** Re-test Creator login role enforcement from PH02.
- **PH04-QA-033** Confirm Admin/Super Admin/Guest identities remain excluded from consumer discovery surfaces.
