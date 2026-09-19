# VC-ANDROID-PH06-R03 Manual Physical-Device QA

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


## R02 portal & authentication presentation
- **PH06-QA-058** Fresh launch after splash opens a polished portal chooser with only concise User Portal and Creator Portal choices; no long authority/help paragraph dominates the page.
- **PH06-QA-059** User Portal card shows a clear professional user icon and brief supporting copy without clipping on a compact phone.
- **PH06-QA-060** Creator Portal card shows a distinct professional creator/host icon and brief supporting copy without clipping.
- **PH06-QA-061** User login page uses concise production copy, keeps all required fields/actions visible, and does not collide with status/navigation bars.
- **PH06-QA-062** User password eye icon toggles masked/visible password without changing field value or causing layout shift.
- **PH06-QA-063** User login `Creator Portal` action switches directly to Creator login.
- **PH06-QA-064** User login displays `Signup` and registration flow still opens correctly.
- **PH06-QA-065** Creator login `User Portal` action switches directly to User login.
- **PH06-QA-066** Creator password eye icon toggles masking correctly and Creator authentication still enforces Creator access.

## R02 Home / navigation polish
- **PH06-QA-067** Home hero is concise and visually balanced; no large explanatory paragraphs crowd live/discovery content.
- **PH06-QA-068** Communities, Messages, Alerts and Host Studio render as clear professional icon cards with icon and label on the same visual line/row.
- **PH06-QA-069** Home icon cards remain readable without label wrapping at minimum supported display width/font scale.
- **PH06-QA-070** All five bottom navigation tabs show polished outline icons when unselected and distinct filled/selected artwork when selected.
- **PH06-QA-071** Repeated tab switching preserves the Telegram-like short directional slide/fade without flashes, duplicate destinations or unexpected back-stack jumps.

## R02 Communities
- **PH06-QA-072** Communities main opens with all communities and has no persistent search field in the middle of the page.
- **PH06-QA-073** Communities top Search icon opens the unified Search page preselected to Communities.
- **PH06-QA-074** Communities top Upcoming Events icon opens Events; no duplicate Upcoming Events body link is shown.
- **PH06-QA-075** Communities/Messages/Alerts quick actions use professional vector icons at readable size and horizontal icon+text alignment.
- **PH06-QA-076** Create Community presents the major action prominently with clear hierarchy and all required backend fields still editable.
- **PH06-QA-077** Create a community successfully; the resulting Community detail screen has premium hierarchy, readable stats/actions and no excessive copy.
- **PH06-QA-078** From the newly created Community detail, tap Back once; destination is Communities main, not the completed Create Community form.
- **PH06-QA-079** Existing community management/join/leave/members/events flows remain functional after the visual redesign.

## R02 unified Search
- **PH06-QA-080** Bottom Search page provides All, People, Creators, Rooms and Communities filters and remains horizontally usable on a compact device.
- **PH06-QA-081** Search one term and verify matching People, Creators, Rooms and Communities render in their relevant filters without duplicate/colliding list items.
- **PH06-QA-082** Open a People/Creator result and verify correct public profile navigation.
- **PH06-QA-083** Open a Room result and verify room preview navigation.
- **PH06-QA-084** Open a Community result and verify community detail navigation.

## R02 Messages
- **PH06-QA-085** Messages opens without a permanently visible search field; top Search icon reveals/hides conversation search cleanly.
- **PH06-QA-086** Single-conversation red delete icon opens a confirmation dialog; Cancel leaves the conversation untouched.
- **PH06-QA-087** Confirm single deletion and verify the conversation disappears from the refreshed inbox.
- **PH06-QA-088** Enter multi-select, select multiple conversations, tap red delete action, cancel once, then confirm; only selected conversations are deleted.
- **PH06-QA-089** Exiting selection mode with Back keeps the user on Messages and clears selection instead of navigating away.

## R02 live-room copy / global spacing
- **PH06-QA-090** Open a live room preview and joined listener room; essential access/errors remain clear but verbose explanatory paragraphs are absent.
- **PH06-QA-091** Paused, ended, reconnecting and speaker-invitation states use concise readable copy with no loss of required action.
- **PH06-QA-092** Check Home, Search, Communities, Community detail/create/manage, Messages, Conversation, Notifications, profile and hosting pages on the physical device: header/footer/content spacing remains adaptive, controls are not obscured and centered titles do not collide with system bars.
- **PH06-QA-093** Increase Android font/display scale within a reasonable supported range and repeat representative pages; icon cards, top actions, fields and navigation remain usable without critical clipping.


## R03 corrective verification
- **PH06-R03-QA-094** Run `scripts\VC-ANDROID-PH06-R03-ACCEPTANCE.cmd`; confirm Debug, Staging and Release Kotlin compilation progress beyond `feature:hosting:HostingScreens.kt` without the nullable `RoomStageState?` error reported in R02.
- **PH06-R03-QA-095** Open Host Live Console with stage data still loading/temporarily unavailable; confirm Audience/Speakers/Raised Hands sections render safely without a crash.
