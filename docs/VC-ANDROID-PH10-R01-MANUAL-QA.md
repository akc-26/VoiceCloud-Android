# VC-ANDROID-PH10-R01 Manual QA

Run only after `scripts\VC-ANDROID-PH10-R01-ACCEPTANCE.cmd` completes its automated build gates.

## Portal and role
1. **PH10-QA-001 Creator sign-in** — sign in with a real CREATOR account; Creator dashboard opens after authentication.
2. **PH10-QA-002 USER rejected from Creator login** — normal USER credentials must not enter Creator navigation.
3. **PH10-QA-003 Admin rejected** — ADMIN/SUPER_ADMIN must not enter either Android product workspace.
4. **PH10-QA-004 Cold restore Creator** — with Creator as last portal, relaunch app; valid session restores Creator dashboard.
5. **PH10-QA-005 Creator → VoiceCloud** — use the Creator shell switch; consumer Home opens without another login.
6. **PH10-QA-006 VoiceCloud → Creator** — on My Profile, CREATOR account sees `Switch to Creator`; switching opens Creator dashboard.
7. **PH10-QA-007 USER no switch** — ordinary USER My Profile must not show `Switch to Creator`.
8. **PH10-QA-008 Back-stack isolation** — after switching portals, Android Back must not return to the previous portal stack.
9. **PH10-QA-009 Creator access linkage** — logged-out Creator Sign In still provides the Creator access application path.

## Dashboard
10. **PH10-QA-010 Dashboard load** — `/creator/dashboard` loads without fabricated placeholders.
11. **PH10-QA-011 Missing dashboard fields** — if backend omits a metric, Android must omit it rather than showing invented `0`/success values.
12. **PH10-QA-012 Refresh/re-entry** — revisit Dashboard; canonical values refresh from server.
13. **PH10-QA-013 Scope isolation** — dashboard does not expose room/schedule/messaging/subscriber/wallet/payout/verification controls early.

## Creator profile
14. **PH10-QA-014 Profile load** — Creator profile shows backend display name/username and supported profile fields.
15. **PH10-QA-015 Profile save** — edit bio/country/interests and save; leave/re-enter page and confirm persistence.
16. **PH10-QA-016 Read-only authority** — username/email/Creator role cannot be edited through PH10 profile.
17. **PH10-QA-017 English authority** — language remains English-only.
18. **PH10-QA-018 Human identity** — no UUID/user ID is shown as the primary Creator identity.
19. **PH10-QA-019 Save failure** — disconnect backend or force validation failure; visible safe error is shown and local UI does not claim success.

## Creator settings
20. **PH10-QA-020 Settings load** — notification settings/timezone load from `/users/settings`.
21. **PH10-QA-021 Notification save** — change email/push/in-app/sound, save, reload, confirm backend persistence.
22. **PH10-QA-022 Timezone save** — save a valid IANA timezone and confirm persistence.
23. **PH10-QA-023 No fake streaming credentials** — PH10 settings contains no invented stream key/secret controls.

## Creator CMS/help/support
24. **PH10-QA-024 Creator CMS list** — Creator Help shows pages published to Creator/Host audience.
25. **PH10-QA-025 CMS detail** — open a Creator CMS page; title/content display cleanly and Back returns to Help.
26. **PH10-QA-026 CMS empty** — with no published Creator pages, show a proper empty state rather than fake content.
27. **PH10-QA-027 Contact Support submit** — submit name/email/optional phone/message; backend `/contact` receives it and success is shown only after server success.
28. **PH10-QA-028 No mail client fallback** — Contact Support remains inside VoiceCloud and does not launch local email software.
29. **PH10-QA-029 Support failure** — backend failure produces safe visible error and no false success.

## Global auth/service states
30. **PH10-QA-030 Session expired** — force 401 from Creator request; local session clears and Session Expired screen opens.
31. **PH10-QA-031 Restricted** — force backend 403/restricted Creator; Restricted screen opens.
32. **PH10-QA-032 Maintenance** — enable maintenance or return 503; Creator routes move to Maintenance state with safe backend message.
33. **PH10-QA-033 Diagnostic redaction** — server/database exception text must not expose SQL/Postgres/TypeORM/constraint internals.

## Regression smoke
34. **PH10-QA-034 Consumer portal** — Home/Explore/Search/Friends/My Profile still operate after Creator integration.
35. **PH10-QA-035 PH09 Settings/Security/Safety** — open each and verify existing PH09 functionality remains reachable.
36. **PH10-QA-036 Wallet/VIP rail** — PH08 payment screen still uses configured authority and grants no local entitlement before server verification.
37. **PH10-QA-037 Listener room** — join/leave a live room as consumer; listener flow remains intact.
38. **PH10-QA-038 Existing Host Studio** — existing PH06 host tooling remains unchanged; PH10 Creator dashboard does not replace it.
39. **PH10-QA-039 App relaunch after switching** — last selected portal restores only when backend role allows it.
40. **PH10-QA-040 Device instrumentation** — final physical-device acceptance gate completes successfully on the selected healthy device.

Record any runtime finding with the QA ID and exact screen/action; any defect must become a durable regression where technically possible.
