# VC-ANDROID-PH13-R16 — Manual Physical-Device / Visual Fidelity QA

Use only after the automated Windows acceptance reaches the device stage. Compare the running phone side-by-side with the bundled `docs/reference/approved-r16/` boards. Do not approve a screen merely because it uses the correct palette.

## Global
- [ ] Ivory/white ordinary surfaces, deep emerald/teal primary controls and restrained gold accents match the boards.
- [ ] Typography scale/density, card dimensions, margins, radii, dividers and bottom navigation visually match the reference phone compositions.
- [ ] No feature screen falls back to the rejected oversized generic hero/card layout.
- [ ] Backend avatars/covers render where available; fallbacks remain polished and do not dominate the page.
- [ ] No clipped/overlapping text at common 360dp/384dp/411dp widths and normal font scale.

## Board 02 — Auth / Onboarding
- [ ] Splash, Welcome, role choice, Sign In, Sign Up, OTP, Reset and Interests correspond to the individual approved compositions.
- [ ] OTP presents six clear digits; terms consent and input/keyboard behavior remain usable.

## Board 03 — Discover / Search / Community
- [ ] Home has compact header/search/chips, featured room and Popular Rooms hierarchy—not R15's oversized generic sections.
- [ ] Search, topic grid, creator profile, community detail/feed, suggestions and Saved presentation follow the board density.

## Board 04 — Listener Live
- [ ] Preview and live stage use immersive deep emerald hierarchy with real host/speaker/listener state.
- [ ] Speaking ring, LIVE state, reactions/gifts, chat/participants/report/saved flows are readable and functional.

## Board 05 — Economy / Profile / Settings
- [ ] Wallet, transaction/gifts/VIP/rewards/profile/settings pages use compact board structure without inventing server balances or entitlements.

## Boards 06–09 — Creator / Host
- [ ] Dashboard/growth/analytics use backend metrics and board-like compact hierarchy.
- [ ] Create/Schedule/Edit/Manage/preflight preserve all real room controls while matching approved forms.
- [ ] Host live/moderation uses dark emerald live composition and all RTC/moderation actions work.
- [ ] Messages, direct chat, earnings/payout, Creator profile/settings/support/CMS follow Creator Showcase compositions.

## Functional regression
- [ ] Authentication/session/deep-link behavior remains R15-equivalent.
- [ ] Listener join/listen/request-to-speak/chat/reactions/gifts/save/report flows work against the real backend.
- [ ] Creator/Host permission and verification rules remain server-authoritative.
- [ ] Wallet/VIP/payment/payout outcomes remain server-authoritative.
- [ ] No crash or broken back navigation across the tested paths.

**Approval requires automated acceptance + physical-device instrumentation + this visual comparison + user approval.**
