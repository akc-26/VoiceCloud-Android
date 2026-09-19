# VC-ANDROID-PH13-R14 — Manual Physical-Device UI/UX QA

Do not Git-freeze R14 only because the Gradle command builds. First require the final automated acceptance PASS, then review these visible product conditions on the same physical device class used for the R13 recording.

## Global
- [ ] No user-facing label or sentence has collapsed whitespace (`WelcomeToVoiceCloud`, `UserPortal`, etc. must never appear).
- [ ] Body copy remains natural sentence case; control labels remain readable and do not look mechanically over-capitalized.
- [ ] No critical text clips/overlaps at normal font size; verify a larger Android font scale as well.
- [ ] Ivory/cream shell, emerald/teal primary, restrained gold and neutral premium cards are coherent across both portals.
- [ ] Real backend images are used when present; loading/error/no-media states remain polished.
- [ ] Letter-only avatar/cover placeholders do not dominate any normal screen. Initials may appear only as a legitimate fallback where no media URL exists.
- [ ] Repeated generic microphone/pictogram art is not used as the primary composition for unrelated pages.
- [ ] Bottom navigation, top bars, dialogs, sheets, inputs and primary actions are reachable without clipping or keyboard obstruction.

## Entry / Auth
- [ ] Splash/bootstrap/system-state surfaces are polished and readable.
- [ ] Portal selector visibly distinguishes Listener and Creator choices with rich branded composition.
- [ ] User sign-in, registration, phone/OTP, password recovery, onboarding and Creator sign-in preserve all existing actions and validation.

## Listener discovery/social
- [ ] Home contains a strong featured/live hierarchy, real room imagery where available, readable cards, people/host imagery and polished empty/loading states.
- [ ] Explore, Rooms, Search and People use visual cards/media rather than raw developer-looking lists.
- [ ] Public Profile and My Profile use cover/avatar media and coherent stats/actions.
- [ ] Edit Profile does not show a giant single-letter cover or avatar placeholder.
- [ ] Friends/follow/social lists remain readable with real avatars and correct actions.
- [ ] Communities and Events show real image/banner/cover data when returned.
- [ ] Messages/conversations/notifications have polished list/chat/empty states.

## Listener live room
- [ ] Room preview uses room cover media where available.
- [ ] Live room remains immersive dark emerald with clear LIVE/audio/speaking/listener hierarchy.
- [ ] Join/listen/request-to-speak/chat/reactions/gifts/save/report/leave work without visual regression.

## Economy / Profile / Settings
- [ ] Wallet, VIP, Gifts, Tasks, Achievements, Progression, Rankings, Referrals, Tickets and Store use rich cards/artwork without fabricating balances or entitlement state.
- [ ] Profile tools, replays, activity, blocked users, visitors and help have polished media/empty states.
- [ ] Settings, Notifications, Privacy, Voice & Appearance, Security, Sessions/Devices, Login Activity, CMS/Help, Safety, Report, Contact and About are consistent and readable.

## Creator / Host
- [ ] Creator Dashboard has a purpose-built creator hierarchy, metrics and rich visual treatment.
- [ ] Live Studio room/schedule cards are visually distinct and preserve their actions.
- [ ] Creator Profile uses backend avatar media when present.
- [ ] Audience, Followers, Subscribers, Plans, Analytics, Wallet, Earnings, Gifts, Payouts, Messages, Notifications, Help/CMS/Support remain functionally intact and visually consistent.
- [ ] Host Studio, room create/edit, schedule create/edit, room management and verification remain complete and readable.
- [ ] Host live console remains immersive and all mic/pause/resume/end/speaker/moderation/polls/quiz controls work against existing RTC/socket authority.

## Acceptance condition
Approve PH13-R14 only when the full automated runner reaches its final **FULL ACCEPTANCE PASS** and the visible product no longer exhibits the physical-device defects that triggered R14.
