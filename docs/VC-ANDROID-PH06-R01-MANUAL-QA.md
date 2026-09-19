# VC-ANDROID-PH06-R01 Manual Physical-Device QA

Use a real Android device against the authoritative Raspberry Pi backend. Record PASS/FAIL and evidence for every case before phase approval.

## Host access & Studio
- **PH06-QA-001** Open Home → Host Studio; navigation is smooth and app does not crash.
- **PH06-QA-002** Non-approved Host account sees backend-authoritative Host access status and cannot manage rooms.
- **PH06-QA-003** Approved Host sees My Rooms and Scheduled sections.
- **PH06-QA-004** Guest tapping Host Studio is routed to account upgrade rather than Host controls.

## Room creation/settings
- **PH06-QA-005** Create a normal public room and verify it appears in My Rooms.
- **PH06-QA-006** Create a private/invite-only room; reopen settings and verify access configuration.
- **PH06-QA-007** Enable Lock room entry and verify server state persists.
- **PH06-QA-008** Enable Subscribers only and verify server state persists.
- **PH06-QA-009** Enable Verified listeners only and verify server state persists.
- **PH06-QA-010** Enable ticket/premium access with a valid price and verify persistence.
- **PH06-QA-011** Edit title/description/category without losing unrelated room metadata.
- **PH06-QA-012** Delete an offline room and verify it disappears from My Rooms.

## Scheduling
- **PH06-QA-013** Create schedule using 12-hour local date/time UI; no UTC/ISO input is shown to user.
- **PH06-QA-014** Attempt past date/time; Save remains blocked with visible message.
- **PH06-QA-015** Public schedule saves and reloads as PUBLIC.
- **PH06-QA-016** Private/invite-only schedule saves and reloads correctly.
- **PH06-QA-017** Ticketed scheduled room persists price/access.
- **PH06-QA-018** Edit scheduled date/time and verify local presentation remains correct.
- **PH06-QA-019** Delete scheduled room and verify removal.
- **PH06-QA-020** Tap Start Room on schedule; corresponding room becomes live and opens host console.

## Room lifecycle
- **PH06-QA-021** Start offline room and verify LIVE state.
- **PH06-QA-022** Pause live room and verify PAUSED state.
- **PH06-QA-023** Resume paused room and verify LIVE state.
- **PH06-QA-024** End live room and verify ended/offline server state and RTC disconnect.

## Host microphone / RTC
- **PH06-QA-025** Enter host console: room audio connects without automatically enabling microphone.
- **PH06-QA-026** First Go on mic requests Android microphone permission.
- **PH06-QA-027** Deny permission: no crash, mic stays off, hosting/listening still works.
- **PH06-QA-028** Grant permission then Go on mic: published microphone becomes active.
- **PH06-QA-029** Mute microphone: local track stops/mutes immediately.
- **PH06-QA-030** Leave host console with mic enabled, re-enter another room; microphone must start OFF.
- **PH06-QA-031** Background/return navigation must not produce duplicate RTC ownership or stale microphone state.

## Stage & participant controls
- **PH06-QA-032** Listener raises hand; Host console shows one queue entry without duplicates.
- **PH06-QA-033** Approve raised hand; listener moves to speaker stage.
- **PH06-QA-034** Reject raised hand; request leaves queue.
- **PH06-QA-035** Invite audience member to stage; invitation reaches correct user.
- **PH06-QA-036** Mute/unmute speaker from Host console and verify remote state.
- **PH06-QA-037** Remove speaker and verify stage updates.
- **PH06-QA-038** Search participant by username/display name; privileged/guest/self identities are not offered.
- **PH06-QA-039** Send participant invitation from search result and verify recipient receives it.

## Polls & quiz
- **PH06-QA-040** Create poll with 2+ options; it appears in room interactions.
- **PH06-QA-041** Start and stop poll; state changes for listeners.
- **PH06-QA-042** Delete poll; it disappears.
- **PH06-QA-043** Create quiz with 2+ options and valid correct answer.
- **PH06-QA-044** Start quiz, advance round where applicable, then stop it.

## UI motion/icons/adaptive chrome
- **PH06-QA-045** Switch Home → Explore → Search → Friends → Profile; transition is short directional slide/fade without full-page jank.
- **PH06-QA-046** Switch tabs in reverse order; transition direction reverses naturally.
- **PH06-QA-047** Each bottom tab displays a professional outline state and distinct selected state; no circle placeholders.
- **PH06-QA-048** Home quick actions display Communities, Messages, Alerts, Host Studio without wrapped/cropped labels.
- **PH06-QA-049** On compact phone, all main tab headers and bottom navigation have comfortable system-bar/footer spacing.
- **PH06-QA-050** On secondary pages (profiles, communities, hosting pages), centered title/back arrow remain aligned and do not collide with status bar.
- **PH06-QA-051** Rotate/change display size where supported; header/footer spacing remains usable and no control is clipped.

## Regression
- **PH06-QA-052** Consumer listener joins live room without microphone permission prompt.
- **PH06-QA-053** PH05 save/chat/reaction/gift/listener reconnect flows remain functional.
- **PH06-QA-054** PH04 Communities/Messages/Notifications remain functional.
- **PH06-QA-055** PH03 discovery/profile/friends/search remain functional.
- **PH06-QA-056** PH02 sign-in/session restore/logout remain functional.
- **PH06-QA-057** Splash → first Compose page starts without ColorSpace crash.
