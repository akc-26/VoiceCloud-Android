# VC-ANDROID-PH02-R05 Manual QA

Run these cases only after `scripts\VC-ANDROID-PH02-R05-ACCEPTANCE.cmd` completes successfully. Use the physical Android device with Tailscale connected while the current `.ts.net` server remains tailnet-only.

## Preconditions
- [ ] Tailscale is connected on the physical Android device.
- [ ] `https://voicecloud.tailfca77b.ts.net/api/v1/config/mobile?platform=android` opens from the device network path.
- [ ] A normal USER test account exists.
- [ ] A CREATOR test account exists.
- [ ] An ADMIN/SUPER_ADMIN account is available only for the negative role-gate test.
- [ ] SMS provider is healthy if `/config/mobile` advertises `phone`.
- [ ] Firebase/Google provider is healthy if `/config/mobile` advertises `google`.
- [ ] For Google tests, Android Firebase public configuration and the app signing SHA-1 are configured.

## A. Bootstrap and secure restore
- [ ] Fresh install reaches bootstrap and then the portal selector without a stored session.
- [ ] Force-update/maintenance bootstrap behavior remains unchanged from PH01.
- [ ] Relaunch after successful User authentication restores the secure session through `/auth/me` without asking for credentials again.
- [ ] Relaunch after successful Creator authentication restores the Creator handoff only when the account role is still `CREATOR`.
- [ ] Invalid/revoked session reaches Session Expired and does not expose authenticated content.

## B. Portal separation
- [ ] Portal selector visibly offers VoiceCloud User and Creator Studio entry.
- [ ] User portal is Light-first.
- [ ] Creator authentication uses the Creator Studio visual authority/dark presentation.
- [ ] Selecting Creator portal does not grant or infer Creator authorization locally.

## C. User email/username login
- [ ] Valid email + password authenticates the USER account.
- [ ] Valid username + password authenticates the same supported User account.
- [ ] Wrong password shows a clear backend-derived error.
- [ ] Empty identifier/password is blocked locally.
- [ ] ADMIN/SUPER_ADMIN credentials do not enter the public Android User portal.

## D. Registration and onboarding
- [ ] Display name, username, email and 8+ character password register successfully.
- [ ] Invalid email is blocked.
- [ ] Password shorter than 8 characters is blocked.
- [ ] Newly registered non-Guest account requiring profile completion enters onboarding.
- [ ] Onboarding Step 1 saves optional bio/country.
- [ ] Step 2 saves selected interests.
- [ ] Step 3 saves English/timezone and in-app/push reminder preference.
- [ ] Finishing onboarding reaches the PH02 User handoff and does not expose PH03 Home prematurely.

## E. Phone OTP
- [ ] Phone option is shown only when backend mobile configuration advertises phone login.
- [ ] Non-E.164 phone input is rejected.
- [ ] Valid international number sends OTP.
- [ ] OTP screen shows the selected phone number.
- [ ] Resend is unavailable during cooldown and works after cooldown.
- [ ] Wrong OTP shows server error.
- [ ] Valid six-digit OTP authenticates/registers the User as defined by backend authority.

## F. Google/Firebase login
- [ ] Google option is shown only when backend mobile configuration advertises Google login.
- [ ] With incomplete Android Firebase public configuration, Google fails closed with a configuration message instead of sending an invalid token to VoiceCloud.
- [ ] With valid configuration, Google account selection succeeds.
- [ ] Google OAuth credential is accepted by Firebase Authentication.
- [ ] VoiceCloud receives the resulting Firebase ID token and login succeeds.
- [ ] Backend-disabled/unhealthy Firebase provider returns a normal user-facing authentication error without provider secrets/config details.

## G. Guest lifecycle
- [ ] Continue as Guest works when backend config advertises Guest login.
- [ ] Guest reaches the PH02 User handoff and sees Upgrade Guest Account.
- [ ] Guest upgrade by valid email/password succeeds and retains the server-side identity/data relationship.
- [ ] Guest upgrade by phone OTP succeeds.
- [ ] Guest upgrade by Google/Firebase succeeds when configured.
- [ ] After successful upgrade, account is no longer presented as temporary Guest.

## H. Forgot/reset password
- [ ] Forgot Password accepts a valid account email and shows the backend response without leaking account-existence details beyond backend policy.
- [ ] Recovery link targets HTTPS `/auth/reset-password?token=...` on the configured VoiceCloud web host.
- [ ] Cold-starting the app from a valid recovery link opens Reset Password.
- [ ] Opening a recovery link while the app is already running also opens Reset Password.
- [ ] Wrong host, non-HTTPS link, missing token, or token shorter than the expected one-time-token minimum is not accepted as an app reset authority.
- [ ] Password shorter than 8 characters is rejected.
- [ ] Password/confirmation mismatch is rejected.
- [ ] Valid one-time token resets the password through the backend.
- [ ] Android App Link auto-verification is separately confirmed after the production signing certificate and `/.well-known/assetlinks.json` are configured.

## I. Session refresh and logout
- [ ] Expired access token + valid refresh token is recovered transparently.
- [ ] Concurrent authenticated requests during expiry do not cause multiple refresh-token rotations.
- [ ] Rotated access and refresh tokens replace the old values in Keystore storage.
- [ ] Active realtime authentication is refreshed after token rotation.
- [ ] Invalid/revoked refresh results in Session Expired and local secure-session cleanup.
- [ ] Sign Out revokes current server session where reachable and clears local auth.
- [ ] Sign out on all devices invokes logout-all and clears local auth.

## J. Creator Portal
- [ ] Creator login requests Creator email and password.
- [ ] Valid CREATOR account enters the PH02 Creator handoff.
- [ ] Valid USER account credentials are denied Creator entry even if password is correct.
- [ ] Non-Creator session is cleared after the role-gate rejection.
- [ ] ADMIN/SUPER_ADMIN accounts are not treated as Creator.
- [ ] 503 server maintenance routes to Creator maintenance state.
- [ ] Creator handoff does not expose dashboard, rooms, RTC, earnings or other later-phase controls.

## K. Creator access application
- [ ] Application form requires full name, valid email, phone, country, 20+ character experience and motivation.
- [ ] Experience years remains within 0–60.
- [ ] Optional category/audience/portfolio fields submit correctly.
- [ ] Valid application reaches `/creator-access/applications` and displays the backend result.

## L. Device/FCM foundation
- [ ] Successful authenticated session posts device metadata to `/notifications/register-device` without exposing raw `ANDROID_ID`.
- [ ] Device registration failure does not destroy an otherwise-valid login session.
- [ ] `FcmTokenRegistrationFoundation.onNewToken()` can register a supplied FCM token once the notification/Firebase Messaging provider invokes it.

## M. Regression / scope guard
- [ ] PH01 bootstrap, HTTPS endpoint, Socket.IO namespace/path and Keystore behavior remain intact.
- [ ] No `10.0.2.2`, `127.0.0.1`, or adb-reverse dependency is required for the normal Raspberry Pi path.
- [ ] No PH03+ Home, rooms, RTC, communities, messaging, wallet/economy or Creator dashboard functionality appears in PH02.


## N. Cumulative corrective regression
- [ ] Full R03 acceptance passes Gradle build-script configuration without `Unresolved reference 'net'`.
- [ ] `compileDebugKotlin`, `compileStagingKotlin`, and `compileReleaseKotlin` are reached only after accumulated source/regression gates pass.
- [ ] Extracting the delivered ZIP into a folder named `VoiceCloud-Android-VC-ANDROID-PH02-R05` produces one project level only (no `...\PH02-R05\PH02-R05\...`).

- [ ] Compile output contains no DataStore `Preferences` classpath-access error from `AuthRepository`.
- [ ] Compile output contains no unresolved `align` reference from `AuthScreens`.
- [ ] Compile output contains no deprecated `androidx.hilt.navigation.compose.hiltViewModel` warning.
- [ ] Room KSP compile output contains no missing `room.schemaLocation` warning.
- [ ] The compile matrix attempts Debug, Staging, and Release in one run if any variant fails.
