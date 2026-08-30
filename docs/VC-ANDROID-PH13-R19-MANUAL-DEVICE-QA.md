# VC-ANDROID-PH13-R19 — Manual Physical-Device QA

Use this after the full Windows acceptance command has installed/validated the build on a healthy authorized device.

## End User / Listener
1. Portal selector shows **Listener** and **Creator** only; no standalone Speaker portal.
2. Listener bottom navigation reads/behaves as **Home / Discover / Live / Messages / Profile**.
3. Tap the center Live/microphone control: it must open the live-room/listening directory, **not Search**.
4. Search from Home/Discover: Search remains reachable and results return to the Discover context correctly.
5. Home says `For You` and shows backend-driven live/discovery content, creator discovery and quick-access sections without giant unexplained blank regions.
6. Discover remains a separate destination from Home and Search.
7. Messages bottom destination opens direct messaging/conversation surfaces.
8. Profile opens identity/profile overview first with backend cover/avatar when available; Edit Profile is explicit; Wallet remains reachable but does not replace the profile identity area.
9. Open a live room as a listener and verify room preview/live stage/chat/reactions/gifts/request-to-speak flows remain reachable according to account/room authority.
10. Exercise Communities, Events, Economy/VIP/Rewards, Settings, Privacy, Security/Devices, Help and Safety for layout clipping, navigation and readable text.

## Creator / Host
11. Switch to Creator with the same account where role authority allows it.
12. Creator Dashboard presents metrics/empty states and purposeful actions; it must not present fake sample metrics.
13. Creator bottom navigation uses Studio semantics and no unexplained generic plus action.
14. For a Creator who is **not** an approved Host: open Live Studio and confirm Go Live/Create/Schedule/room-host controls are not granted; Host access/verification guidance is shown.
15. For an `APPROVED` Host: Live Studio loads Host rooms/schedules and exposes the existing create/schedule/live management callbacks.
16. Creator Profile opens as a profile overview; Edit Profile must be entered explicitly, support Save/Cancel, and return to overview.
17. Creator Analytics/Earnings/Wallet/Gifts/Payouts display only backend-authoritative values; unavailable time-series data must not become a fabricated chart.
18. Approved Host: exercise room create/edit/schedule/manage/preflight/live console, speaker/co-host/participant moderation, polls/quiz and verification surfaces.

## Device quality
19. Verify 360dp/384dp/411dp-class phone widths for clipping, awkward wrapping, inaccessible controls and bottom-navigation overlap.
20. Verify system font scaling, keyboard/IME screens, touch targets and scroll reachability.
21. Verify remote avatar/cover/media loading and designed fallback behavior with both populated and missing backend media.
22. Confirm no user-facing strings lose whitespace and no technical provider/database/HTTP detail is exposed in normal feedback.

Any functional, navigation, authority or high-impact visual defect in these checks blocks Git freeze.
