# VC-ANDROID-PH05-R04 — Runtime QA

Run after `scripts\VC-ANDROID-PH05-R04-ACCEPTANCE.cmd` succeeds.

1. Cold-launch the debug app. Confirm the temporary splash appears and the app remains open after the splash.
2. Confirm the first User Portal screen renders text without an `ArrayIndexOutOfBoundsException` or `ColorSpace` crash in Logcat.
3. Navigate Home → Explore → Search → Friends → Profile and confirm text/colors render normally.
4. Open at least one secondary page (Communities or another user's Profile) and confirm the shared header, back arrow and status-bar spacing remain correct.
5. Open Communities, Messages and Notifications and confirm branded text/card/surface colors render without crashes.
6. Open a live-room preview and return without joining; confirm no color/runtime crash.
7. If a live room is available, join as listener and confirm remote audio UI, participants/chat/gift surfaces render without color/runtime crashes.
8. Force-stop and relaunch the app once more to confirm startup remains stable with restored session state.
