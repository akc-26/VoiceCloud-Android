# VC-ANDROID-PH04-R04 Manual QA

Run after `scripts\VC-ANDROID-PH04-R04-ACCEPTANCE.cmd` succeeds.

## System bars and page chrome
1. Open Home and verify content does not collide with the status bar or navigation bar.
2. Home -> Communities: verify a clear safe gap below the status bar.
3. Home/People -> another user's Profile: verify the same safe gap.
4. Communities -> Community detail: verify the same safe gap.
5. Communities -> Create/Manage community: verify the same safe gap.
6. Communities -> Members and Events: verify the same safe gap.
7. Home -> Messages -> Conversation: verify the same safe gap.
8. Home -> Alerts/Notifications: verify the same safe gap.
9. On every secondary page above, verify an icon-only back arrow is at the left and is vertically centered.
10. Verify the page title is visually centered, one line, not merged with the status bar, and does not jump when a right-side action is present.
11. Notifications: verify `Read all` remains on the right without shifting/overlapping the centered title.

## Theme / Website parity
12. Verify Home hero is a light aqua/cloud gradient with dark text, not a teal-to-purple slab.
13. Verify another user's Profile hero follows the same light Website presentation family.
14. Verify Communities hero follows the same light Website presentation family.
15. Verify Cards/ElevatedCards use white/cloud/soft-aqua surfaces and do not show unrelated Material purple/lavender defaults.
16. Verify primary actions use the VoiceCloud teal/sapphire authority consistently.
17. Verify corner radii, cards and action controls feel visually consistent across Home, discovery, profiles and PH04 pages.

## Text/button layout
18. Home: verify Communities, Messages and Alerts stay on one line and are centered.
19. Communities quick actions: verify Communities, Messages and Alerts stay on one line and are centered.
20. Community detail: verify Members and Rooms & events stay on one line without clipping or overlap.
21. Rotate device / test a narrow phone width and repeat cases 18-20.
22. Verify Search, Create community, Join, Manage, Leave, Message, Follow, Read all, Mark read and Delete controls have readable aligned labels.
23. Verify no consumer page displays internal wording such as PH03/PH04/PH05, API authority, canonical/finalized API or administrative filtering notes.

## Regression smoke
24. Repeat PH03 Home scrolling through People and Creators; app must not crash.
25. Open Friends and Profile main tabs; confirm existing tab-bar layout remains intact.
26. Login/logout/session restore smoke test.
27. Open Communities, Events, Messages and Notifications and confirm backend loading/error states still work.
28. Send/open a direct conversation and verify message list/input layout remains usable.
29. Verify Android Back/system gesture and the header back arrow both return to the expected previous route.
30. If a healthy device is connected, confirm instrumentation reports at least one executed passing test.
