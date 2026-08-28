# VC-ANDROID-PH08-R01 Manual QA

Use a real authenticated Consumer account against the intended VoiceCloud backend. Verify both populated and empty/error states where practical.

- **PH08-QA-001 — Profile entry:** My Profile still exposes `Economy & progression` and now exposes `Profile & activity`; guest access to PH08 tools goes to Guest Upgrade.
- **PH08-QA-002 — Profile hub:** Open Profile & activity; profile identity, cover/avatar fallbacks and tool entries render without raw IDs or broken layout.
- **PH08-QA-003 — Edit profile:** Username/email/phone are read-only. Change bio/country/interests, save, reopen and confirm server-persisted values.
- **PH08-QA-004 — Avatar:** Select an image with Android Photo Picker, upload it, reopen profile, then remove it. Confirm loading/error/fallback behavior.
- **PH08-QA-005 — Cover:** Select/upload a cover, reopen profile, then remove it. Confirm no broken-image placeholder appears if the media URL is missing or fails.
- **PH08-QA-006 — Replay library:** Open Replays and verify only backend-returned recordings appear; empty state is usable when none exist.
- **PH08-QA-007 — Replay access:** Open an authorized replay and play/pause it. Confirm processing, denied, failed and no-media states do not attempt unauthorized/local playback.
- **PH08-QA-008 — Replay lifecycle:** Start playback, navigate back/away and confirm audio stops/releases rather than continuing unexpectedly.
- **PH08-QA-009 — My Activity:** Verify recent room participation renders meaningful room/Host/type/time/duration information without exposing internal IDs.
- **PH08-QA-010 — Blocked users:** Verify list presentation, unblock one account, and confirm the refreshed list reflects the server mutation.
- **PH08-QA-011 — Profile visitors:** Verify visitor list plus total/today/week statistics and empty state; respect any backend privacy restrictions.
- **PH08-QA-012 — Network/session failures:** Disconnect network or use expired authorization where safe; verify user-facing errors do not expose endpoint URLs/provider internals.
- **PH08-QA-013 — PH07 regression:** Open Economy & progression and verify wallet/VIP/referrals/gifts/store/tasks/achievements/XP/rankings/tickets entry remains available.
- **PH08-QA-014 — PH06/PH05 smoke:** Join a live room and, with an authorized Host account, confirm inherited live/Host navigation still opens.
- **PH08-QA-015 — Visual quality:** Check phone-size and larger layouts for clipping, contrast, readable fallbacks, consistent Royal Sapphire Material theme and no technical placeholder text.

PH08 is ready for Git freeze only after the full Windows acceptance command passes and these phase journeys are approved.
