# VC-ANDROID-PH09-R06 Manual QA

Run this first from the extracted package:

```powershell
scripts\VC-ANDROID-PH09-R06-ACCEPTANCE.cmd
```

Proceed with functional QA after the compile/test/lint/assembly gates succeed. When no healthy device is connected, the acceptance script may return the documented nonfatal device warning; physical-device QA then remains mandatory before phase approval.

## A. Settings overview

### PH09-QA-001 — Settings entry
1. Sign in as a normal registered user.
2. Open **Profile**.
3. Verify **Settings**, **Security & devices**, and **Safety Center** are visible and functional.
4. Open Settings.
5. Confirm there are no placeholder/dead destinations.

### PH09-QA-002 — English-only state
1. Open Settings.
2. Verify the current language is shown as **English**.
3. Verify the app does not offer unsupported alternate product-language selection.

## B. Notifications

### PH09-QA-003 — Notification persistence
1. Open Notification preferences.
2. Change Email, Push, In-app and Sound values.
3. Save.
4. Leave the page and reopen it.
5. Force-close/relaunch and sign in again if needed.
6. Verify backend-persisted values return.

### PH09-QA-004 — Notification failure
1. Disconnect network or make the backend unavailable.
2. Attempt to save.
3. Verify a safe user-facing error appears.
4. Verify no raw SQL/ORM/server diagnostic is shown.
5. Restore network and retry.

## C. Privacy

### PH09-QA-005 — Messaging permission
Test supported values and verify persistence:
- Everyone
- Following
- Friends
- None

### PH09-QA-006 — Follow permission
Test supported values:
- Everyone
- Approval
- None

### PH09-QA-007 — Invitation permission
Test supported values:
- Everyone
- Friends
- None

### PH09-QA-008 — Visitor privacy
1. Change Visitor permission.
2. Toggle visitor tracking.
3. Toggle anonymous visiting.
4. Save and reload.
5. Verify the values are backend-persisted.
6. Recheck Profile Visitors behavior from PH08 for consistency.

## D. Voice & audio preferences

### PH09-QA-009 — Audio preferences
1. Open Voice & audio.
2. Change the supported audio preset if applicable.
3. Toggle Noise Suppression, Echo Cancellation and Automatic Gain Control.
4. Save.
5. Reopen and confirm backend-persisted state.

### PH09-QA-010 — No secret RTC controls
Verify this user settings surface contains no RTC provider keys, stream secrets, Admin provider selection or credential management.

## E. Appearance

### PH09-QA-011 — Light mode
1. Select Light.
2. Verify the application root changes to the centralized light Royal Sapphire theme.
3. Navigate across Home, Explore, Profile and Settings.
4. Confirm the appearance remains consistent.

### PH09-QA-012 — Dark mode
Repeat with Dark and verify all primary surfaces remain readable.

### PH09-QA-013 — System mode
1. Select System.
2. Change Android system appearance Light -> Dark and Dark -> Light.
3. Verify VoiceCloud follows system mode.
4. Restart the app and confirm the preference remains.

## F. Security overview

### PH09-QA-014 — Security counts
1. Open Security & devices.
2. Verify active session, device and login-activity information is server-backed.
3. Do not accept fabricated fixed counts.

### PH09-QA-015 — Session list privacy
1. Open Active Sessions.
2. Verify the current session is marked when the backend/device identity permits it.
3. Verify only safe fields are shown.
4. Confirm no access token, refresh-token hash or backend owner ID appears.

### PH09-QA-016 — Session detail
Open a session detail and confirm device/type, IP, activity/expiry and status metadata render only when provided by the backend.

### PH09-QA-017 — Revoke non-current session
1. Select a non-current active session.
2. Revoke it.
3. Verify success is shown only after the backend responds successfully.
4. Refresh and confirm it is removed/inactive.

### PH09-QA-018 — Current-session protection
If the current session is shown, verify the app does not present a misleading remote-revoke workflow that would leave local state inconsistent.

### PH09-QA-019 — Device list privacy
1. Open Devices.
2. Verify device type/name/manufacturer/model/OS/app version/last IP/last used when supplied.
3. Verify `pushToken` and backend owner ID are never exposed.

### PH09-QA-020 — Device revoke
1. Revoke a non-current device.
2. Verify backend success feedback.
3. Refresh and confirm canonical state.

### PH09-QA-021 — Login activity
1. Open Login Activity.
2. Verify recent backend events render action, method, platform, IP/location and timestamp when present.
3. Confirm the screen remains bounded and responsive.
4. Confirm no credentials/tokens are rendered.

### PH09-QA-022 — Sign out all devices success
1. Trigger Sign out all devices.
2. Verify backend success occurs.
3. Verify the app then clears local authentication and returns to sign in.
4. Verify a prior remote session is no longer active.

### PH09-QA-023 — Sign out all devices backend failure
1. Make backend logout-all fail/unreachable.
2. Trigger Sign out all devices.
3. Verify local credentials are **not** falsely cleared as though remote sessions were revoked.
4. Verify safe failure feedback.

## G. CMS / Help / legal / About

### PH09-QA-024 — Help list
1. Publish End User help/FAQ/legal pages in Admin CMS.
2. Open Android Help Center.
3. Verify published pages are loaded dynamically.
4. Confirm deleted/unpublished content does not appear after refresh.

### PH09-QA-025 — CMS detail
1. Open an individual Help/Terms/Privacy/Safety page.
2. Verify title/content correspond to Admin CMS.
3. Verify HTML content is rendered safely as readable Compose text and does not expose raw markup noise.

### PH09-QA-026 — About
Open About and verify VoiceCloud identity/version information is professional and contains no fabricated usage statistics.

## H. Contact Support

### PH09-QA-027 — Persisted support submission
1. Open Contact Support.
2. Fill Name, Email, optional Phone and Message.
3. Submit.
4. Verify success only after backend success.
5. In Admin Contact Inbox verify the submission exists.
6. Confirm the Android app does not expose SMTP/provider credentials.

### PH09-QA-028 — Support claims
Verify Android does not promise fake live chat availability or fixed response-time/SLA text that is not backend-authoritative.

## I. Reporting

### PH09-QA-029 — Contextual user report
1. Open another user's public profile.
2. Tap Report profile.
3. Verify the user is preselected using display name/username context.
4. Verify no UUID entry is requested.
5. Select a supported reason, optionally enter description, and submit.

### PH09-QA-030 — Contextual room report
1. Join/open a live room.
2. Tap Report.
3. Verify room title context is used and raw room ID is hidden from the normal UI.
4. Submit a supported report.

### PH09-QA-031 — Non-contextual target search
1. Open Report from Safety Center.
2. Search People by human-readable name/username.
3. Search Rooms by title.
4. Select a result.
5. Confirm only User and Room target types are exposed to normal Android users.

### PH09-QA-032 — Report reasons
Verify exactly these supported reasons are available:
- Spam
- Abuse
- Harassment
- Fake profile
- Sexual content
- Violence
- Other

### PH09-QA-033 — My report history
1. Submit at least one report.
2. Open Your reports / report history.
3. Verify server-backed status and human-readable target label.
4. Confirm no Admin approve/dismiss controls are present.

## J. Safety Center

### PH09-QA-034 — Safety destinations
Verify Safety Center provides working access to:
- Report
- Blocked users
- Privacy
- Communities/community guidance
- Admin-published safety/community CMS content when available

## K. Global runtime handling

### PH09-QA-035 — Session expired
1. Expire/revoke the active token server-side where practical.
2. Trigger an authenticated PH09 request.
3. Verify a safe session-expired state and sign-in recovery.
4. Verify no infinite request/navigation loop.

### PH09-QA-036 — Maintenance
1. Enable maintenance in Admin and configure a recognizable message.
2. Trigger Android maintenance/config handling.
3. Verify non-Admin Android access is blocked appropriately and the safe configured message is shown when supplied.
4. Disable maintenance and verify recovery.

### PH09-QA-037 — Restricted account
1. Use a test account placed into the supported restricted/suspended/disabled state.
2. Authenticate.
3. Verify the app routes to restricted-account handling rather than normal consumer content.

## L. Preserved critical regressions

### PH09-QA-038 — Home / Explore / Search / Friends
Recheck PH08-R02 corrected discovery/search/friends flows and direct room entry.

### PH09-QA-039 — Profile / replays / activity / media
Recheck PH08 replays, My Activity, Edit Profile, avatar/cover replacement, visitors and blocked users.

### PH09-QA-040 — Live room
Join a live room and verify listener audio, chat, emoji/reaction, gifts, floating composer, title layout and Leave behavior remain functional.

### PH09-QA-041 — Hosted payment rail
With `VOICECLOUD_ANDROID_PAYMENT_MODE=HOSTED_GATEWAY`, verify Wallet/VIP still open the backend-generated hosted checkout and the app refreshes server state on return without local entitlement fabrication.

### PH09-QA-042 — Google Play rail build configuration
For a dedicated Google Play test build, switch only:

```properties
VOICECLOUD_ANDROID_PAYMENT_MODE=GOOGLE_PLAY
```

Verify ProductDetails purchase/restore still requires VoiceCloud server verification before consume/acknowledge.

## Approval rule

Approve PH09 only when:

- workstation acceptance is successful,
- required physical-device QA is successful,
- no PH01-PH08 regression is introduced,
- and the user has reviewed the PH09 functional cases above.
