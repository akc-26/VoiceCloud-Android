# VC-ANDROID-PH13-R09 — Manual UI/UX QA Checklist

Use this after the automated acceptance command passes. The purpose is visual/runtime confirmation; do not approve R09 if a page still looks like the old minimal theme-only treatment.

## Global quality gate
- [ ] Ivory/cream shell, emerald/teal primary surfaces and restrained gold accents are consistent.
- [ ] Cards, page heroes, list items, controls and secondary-page chrome have the intended glossy/premium depth without excessive glare.
- [ ] No page falls back to plain text + default Material cards where a richer visual treatment is expected.
- [ ] Icons/pictograms are meaningful, aligned, crisp and not clipped.
- [ ] Remote/backend media remains data-driven; decorative pictorial art never replaces server data or invents content.
- [ ] Loading, empty, error and disabled states remain readable and visually polished.
- [ ] Typography hierarchy, padding, rounded corners and touch targets are consistent.
- [ ] Dark immersive live surfaces remain high-contrast and readable.
- [ ] Animations are smooth and purposeful; reduced-motion/device animator settings are respected.

## End User / Listener
- [ ] Bootstrap/AuthGate: brand, waveform and loading presentation are premium.
- [ ] Portal selector / onboarding / sign-in / register / phone OTP / reset flows use the rich auth visual shell.
- [ ] Home/Discover matches the approved quality bar: premium header/search, topic chips, hero/highlight, pictorial room cards, hosts/people and rich bottom navigation presentation.
- [ ] Explore/Search/Rooms/People use visual sections/cards instead of raw lists.
- [ ] Public/My Profile, friends and social lists use profile imagery/metrics/pictograms and polished actions.
- [ ] Communities/community details/editor/members and Events/detail use pictorial heroes/cards and clear hierarchy.
- [ ] Messages/conversation/notifications use rich inbox/chat surfaces and polished empty states.
- [ ] Room Preview and Live Room use immersive audio visuals, waveform/speaking/live hierarchy, participants, chat/reaction controls and readable state feedback.
- [ ] Wallet & Rewards hub and every economy section (Wallet, VIP, Gifts, Tasks, Achievements, Progression, Rankings, Referrals, Tickets, Store) use premium heroes/cards/pictograms while preserving server-authoritative actions.
- [ ] Profile tools/Edit Profile/Replays/Activity/Blocked Users/Visitors/Help use the premium secondary-page shell and rich content states.
- [ ] Settings, Notifications, Privacy, Voice & Appearance, Security, Sessions/Devices, Login Activity, Help/CMS, Safety, Report, Contact and About use consistent premium secondary-page treatment.

## Creator / Host
- [ ] Creator Dashboard uses a premium summary/hero, metric treatment and creator-specific visual hierarchy.
- [ ] Live Studio, Host Studio, Room Editor, Schedule Editor and Room Management use rich creator/room visuals without changing actions.
- [ ] Host Live Console has immersive live styling, speaking/audio hierarchy and professional moderation controls.
- [ ] Polls/Quiz and Host Verification have clear pictograms/cards, form states and confirmations.
- [ ] Creator Profile/Settings/Help/CMS/Support match the same premium portal system.
- [ ] Audience/Followers/Subscribers/Plans use rich cards/metrics/empty states and retain existing callbacks.
- [ ] Analytics includes metric cards/chart visualization; Wallet/Earnings/Gifts/Payouts use premium financial cards without locally granting financial state.

## Regression / functionality
- [ ] Existing R08 navigation destinations are unchanged.
- [ ] Authentication and session behavior remain unchanged.
- [ ] Search/follow/message/community/event flows behave exactly as R08.
- [ ] RTC/live-room join/listen/request/speak/host/moderation flows behave exactly as R08.
- [ ] Wallet/VIP/payment/payout state remains backend/server authoritative.
- [ ] Creator and Host permission/verification behavior remains unchanged.
- [ ] No crash, clipping, overlap, unreadable contrast, or broken back navigation is observed on the target Android device.
