# VC-ANDROID-PH13-R05 Manual UI/UX QA

Run after `scripts\VC-ANDROID-PH13-R05-ACCEPTANCE.cmd` succeeds.

## A. Global design system

- Confirm launcher/splash uses the new teal cloud/wave mark on warm ivory rather than the previous sapphire block treatment.
- Confirm the standard app shell is warm ivory/off-white with deep emerald/teal primary controls, restrained gold accents and readable dark text.
- Confirm cards use consistent rounded geometry, subtle borders/elevation and spacing; no screen should revert to a mismatched generic palette.
- Confirm system Dark preference still functions and retains coherent emerald live colors; do not alter the existing appearance feature behavior.

## B. Authentication / onboarding

- Open portal selector, User sign-in, registration, phone sign-in, OTP, forgot/reset password and Creator sign-in/application paths.
- Confirm brand mark, title hierarchy, premium auth card and waveform render cleanly at common phone sizes.
- Verify all existing buttons/actions still call the same flows; validation, OTP, password recovery, Google/phone/guest options must behave exactly as before.
- Confirm keyboard/IME does not hide primary actions and scrolling remains available.

## C. End User discovery / social

- Check Home/Discover, Explore, Rooms, Search, People/Creators, Friends, public profile and My Profile.
- Confirm live room cards use the deep emerald audio hero, LIVE pulse and waveform without hiding metadata.
- Confirm bottom navigation is readable, premium and retains all original destinations/actions.
- Confirm loading state displays shimmer without changing data-loading behavior.

## D. Listener live audio room

- Open a real live room from preview and join normally.
- Confirm deep emerald immersive runtime, visible LIVE state, listening/speaker metadata and waveform.
- Confirm actual speakers are grouped clearly and speaking participants receive an animated speaking ring based on real `isSpeaking` state.
- Confirm listeners remain visible and overflow count behaves correctly.
- Exercise save/report/leave, hand raise, chat, message reaction, room reaction, gift sheet, invitation accept/reject, background/foreground and audio retry.
- Verify every action still works exactly as PH13-R04; visual changes must not alter socket/RTC/business behavior.

## E. Economy / profile / settings

- Check Economy hub, wallet/balance, transactions, gifts, rewards/tasks/VIP and hosted payment entry paths supported by the build.
- Confirm emerald wallet/premium cards and gold premium accents are readable and no server values are visually fabricated.
- Check Profile tools and Settings/Privacy/Notifications pages; all toggles and navigation must behave exactly as before.

## F. Creator Portal

- Enter Creator Portal using an eligible Creator account.
- Confirm Dashboard is the premium light Creator shell with ivory surfaces, emerald primary actions, gold premium badge/accent, metric cards and light floating bottom navigation.
- Check Audience, Followers, Subscribers, Plans, Profile, Settings, Help, CMS/Support, Analytics, Wallet, Earnings, Gifts, Payouts, Messages and Notifications.
- Confirm the previous generic dark wrapper is not shown on ordinary Creator pages.

## G. Host Studio / room creation / scheduling

- Check Host Studio, room create/edit, schedule create/edit, scheduled room list/management and host verification.
- Confirm room editor and schedule editor show the premium hero treatment but retain every PH13-R04 field and control.
- Verify schedule date/time remains device-local with 12-hour picker presentation where already implemented and backend submission remains unchanged.
- Start/manage a real room and confirm all existing room lifecycle controls continue to work.

## H. Host live console

- Confirm Host Console intentionally switches to immersive dark emerald rather than the ordinary light Creator shell.
- Verify LIVE pulse, waveform and mic state hierarchy are clear.
- Exercise microphone permission/mute/unmute, pause/resume/end, raised-hand approval/rejection, speaker invite/remove/mute, participant invite and Polls & Quiz navigation.
- Confirm there is no functional regression in RTC/socket authentication or host lifecycle.

## I. Motion / accessibility

- Confirm LIVE and speaking animations are smooth and not visually distracting.
- Confirm shimmer and card transitions do not block taps or cause layout jumps.
- Set Android animator duration scale to 0/reduced motion and confirm continuous premium animations effectively stop/slow rather than ignoring the accessibility preference.

## Acceptance condition

Approve R05 UI/UX only when the full Gradle gate succeeds and all existing PH13-R04 functions remain operational while the two portals consistently match the new premium ivory/emerald/gold direction.
