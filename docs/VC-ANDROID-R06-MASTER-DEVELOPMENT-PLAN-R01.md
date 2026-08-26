# VoiceCloud Android Application — Master Development Plan R01

## Status
**PROPOSED LOCK for Android implementation against finalized VoiceCloud Platform Closure R06.**

This plan is intended to become the non-drifting implementation authority for a new Android application. Once approved, phase names, order, scope, API semantics, portal behavior, and acceptance gates must not be changed, merged, deferred, or reinterpreted without explicit approval.

---

## 1. Authoritative baseline

### Repository authority
- Repository: `https://github.com/akc-26/VoiceCloud-Backend`
- Authoritative branch: `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06`
- Exact commit: `7bee8d463786d21a7200b5663a499408bb229115`
- Parent: `7e95a08d02768ad4199cd7f6610b8171707c85af` (`VC-WEB-PH12-R03`)
- Deployment/integration branch `beta-pi` is currently fast-forwarded to the same exact R06 commit.

### Current source audit basis
- Backend controllers: **54**
- Backend route/method operations inventoried by the current R11 parity script: **691**
- Existing Website/Creator API calls mapped by parity script: **251**
- Website page source files: **74**
- Creator Studio page source files: **22**
- Approved Website visual map: **84 historical VC-WEB screens**, plus later R03–R06 functional/corrective additions.

### Source-of-truth order
1. **R06 backend source, DTOs, guards, services, migrations, persistence, realtime and provider authority.**
2. **R06 Consumer Website** for end-user feature/function/workflow parity.
3. **R06 Creator Studio** for creator feature/function/workflow parity.
4. **Admin panel** only as configuration/content/provider authority. Admin UI is **not** part of Android.
5. Approved VoiceCloud branding/design authorities, adapted to native Android without changing business behavior.
6. Android platform requirements (permissions, Billing, FCM, App Links, lifecycle) may change presentation/integration mechanics but may **not** change VoiceCloud business semantics.

---

## 2. Product definition — one Android application, two portals

The Android product is **one application package** with two isolated product portals:

### Portal A — VoiceCloud User
Native equivalent of the Consumer Website.

### Portal B — VoiceCloud Creator
Native equivalent of Creator Studio.

They share one backend, one secure authentication/session infrastructure, one realtime transport layer, one media/RTC core and one brand system, but have **separate navigation graphs, separate role gates and separate portal UI shells**.

### Entry behavior
Cold start:
1. Splash/bootstrap.
2. Fetch `/api/v1/config/mobile?platform=android`.
3. Enforce maintenance state and app-version policy.
4. Restore valid session if present.
5. Show or restore portal choice.
6. Route only to a portal allowed by backend user role/capabilities.

Fresh/logged-out users are presented with explicit entry choices:
- **Continue to VoiceCloud** / User Sign In
- **Creator Sign In**
- Creator access/application path from Creator entry

### Role/portal rules
| Backend/account role | User Portal | Creator Portal | Notes |
|---|---:|---:|---|
| `GUEST` | Yes | No | Browse/use guest-authorized functions; upgrade where required |
| `USER` | Yes | No | May apply for Creator access |
| `CREATOR` | Yes | Yes | Creator can use the normal consumer experience and Creator workspace |
| `ADMIN` | No consumer/admin mobile workspace | No | Admin stays Web Admin only |
| `SUPER_ADMIN` | No consumer/admin mobile workspace | No | Admin stays Web Admin only |

Additional roles are **not login roles**:
- Community role: `OWNER`, `ADMIN`, `MODERATOR`, `MEMBER`
- Room/stage role: host/co-host/moderator/speaker/listener — derived by backend/RTC authority
- Host verification status — separate capability/state within Creator workflow

The app must never infer one role from another.

### Portal switching
If the authenticated account is `CREATOR`, a controlled **Switch to Creator / Switch to VoiceCloud** action may change portal without creating a second account or token. A `USER` cannot switch into Creator. The Creator navigation must never appear merely because a user hosts a room or owns a community.

---

## 3. Non-negotiable implementation rules

1. No fabricated/mock/demo runtime data in production paths.
2. No client-side invention of wallet, VIP, gift, ticket, payout, subscription, moderation, stage or RTC success.
3. All financial settlement is server-authoritative and idempotent.
4. All API DTOs must map to R06 semantics; do not rename fields in ways that alter meaning.
5. Technical UUIDs remain internal. Human-facing screens show username/display name/room name/community name, never IDs unless technically essential to a debug-only view.
6. Provider/key/configuration errors must be sanitized for consumers/creators.
7. Android must not expose Admin-only APIs or Admin/Super Admin workspace features.
8. Feature flags, maintenance, supported login methods, app version, wallet packages and runtime provider capabilities come from backend configuration, not hard-coded Android constants.
9. Live room authority remains backend + LiveKit + Socket.IO. Android UI is not authoritative for stage/mute/participant status.
10. Google Play purchases are not granted locally. Server verification must succeed first.
11. Cached data can improve UX but cannot override backend truth for live rooms, money, access, subscriptions, moderation, tickets or security.
12. English is the currently supported application language because R06 publishes `supportedLanguages: ['en']`. Do not expose unsupported localization controls.
13. Fresh Android consumer presentation defaults to **Light** appearance. Authenticated saved preference may change to Light/Dark/System.
14. Branding must be centralized. No screen-level scattered colors, logos, radii or typography constants.
15. Every defect found during development must become a durable regression check where technically possible.

---

## 4. Android-specific backend readiness closure required before financial parity

The current R06 backend is strong, but Android exposes platform-specific requirements that the Website does not exercise. These must be closed deliberately before the related Android phases are accepted.

### BR-01 — Android Wallet purchases — backend is ready in principle
R06 already provides:
- `/config/mobile` coin package catalogue
- `googlePlayProductId` on `CoinPackage`
- `/wallet/purchase/initiate`
- `/wallet/purchase/validate`
- Google Play Developer API server verification
- transaction replay/idempotency authority

Android flow must use Play Billing, then send the purchase token to VoiceCloud for authoritative validation, and only consume/acknowledge the product after VoiceCloud confirms settlement.

### BR-02 — Android VIP recurring subscriptions — **not yet complete in R06**
Current VIP tiers do **not** have Google Play subscription product/base-plan mappings. `VipFinancialAuthorityService` calls the generic Google provider without passing a VIP Google Play product ID, while the Google provider requires an Admin-configured expected product ID. The current Google adapter verifies one-time `productsv2` purchases, not Android subscription lifecycle authority.

Before Android VIP is accepted, backend must add:
- Admin-configured Google Play VIP subscription product mapping per tier/cycle (or product + base-plan mapping)
- authoritative subscription purchase verification using the proper Google Play subscription server API
- provider subscription ID / expiry / renewal state persistence
- cancellation/renewal handling compatible with Android subscriptions
- RTDN or equivalent server renewal/cancellation reconciliation
- replay/idempotency protection tied to purchase token/order/notification
- public mobile VIP catalogue exposing only the configured Android product mappings

Android must **not** fake VIP auto-renew with local timers or the one-time product API.

### BR-03 — Creator paid subscriptions — existing backend currently records intent only
`POST /creator/subscribe/:creatorId` explicitly creates a subscription record and **does not deduct balance or integrate a payment gateway**. The current Consumer Website does not expose a complete paid creator-subscription acquisition flow, even though Creator Studio manages plans/subscribers and rooms can be subscriber-only.

Before Android presents paid creator subscriptions as a purchase feature, the platform must decide and implement an authoritative economy:
- Google Play subscription mapping/payment, or
- another Android-policy-compliant server-authoritative model
- settlement to creator earnings
- renewal/cancellation/refund lifecycle
- subscriber-only access based on paid active authority

Until this is closed, Android may display backend-authorized existing subscriber state but must not manufacture a paid purchase flow.

### BR-04 — Agency functions are explicitly excluded
R06 has no active Agency product authority. Android must not expose Agency login, Agency management or fabricated Agency rankings. Any `agencySettings` value in mobile config is not a product feature by itself.

### BR-05 — Password-reset App Link
Backend email currently points to the public Website path `/auth/reset-password?token=...`. Android should register a verified HTTPS App Link for that same route/domain so the link can open the native Reset Password destination while remaining compatible with the Website fallback.

---

## 5. Android technical architecture

### Language/UI
- Kotlin
- Jetpack Compose
- Material 3 adapted to VoiceCloud centralized design tokens
- Single-Activity architecture
- Navigation Compose with independent portal graphs

### Build variants
- `debug`
- `staging`
- `release`

Each variant has explicit backend/environment configuration. Production secrets are never packaged into source or BuildConfig.

### Recommended module structure
- `app`
- `core:branding`
- `core:ui`
- `core:model`
- `core:network`
- `core:auth`
- `core:config`
- `core:database`
- `core:realtime`
- `core:rtc`
- `core:billing`
- `core:notifications`
- `core:media`
- `core:security`
- `feature:bootstrap`
- `feature:auth`
- `feature:consumer:*`
- `feature:creator:*`

### State architecture
- ViewModel + `StateFlow`
- unidirectional state/events
- repository layer separating remote/local sources
- explicit loading/success/empty/error states
- domain use cases for financial/live/security mutations

### Dependency injection
- Hilt/Dagger

### HTTP/API
- Retrofit + OkHttp
- strict DTO serialization
- one API base `/api/v1`
- access token interceptor
- single-flight refresh-token authenticator using `/auth/refresh`
- retry only where semantically safe
- request idempotency keys for relevant purchase/mutation endpoints

### Secure local storage
- Android Keystore-backed token encryption
- DataStore for non-secret preferences
- Room for bounded cache/history where useful
- never store provider secrets, payment credentials or raw verification documents beyond upload lifetime

### Images/media
- Coil
- Android Photo Picker where applicable
- multipart upload contracts matching backend
- bounded temporary cache with cleanup

### Native integrations
- LiveKit Android SDK
- Socket.IO Android/Java client compatible with current server
- Firebase Cloud Messaging
- Firebase/Google identity token flow for Google login
- Android Credential Manager where compatible
- Google Play Billing Library
- WorkManager for durable non-realtime background work only

---

## 6. Bootstrap/config authority

Every app launch must reconcile `/config/mobile?platform=android` before exposing normal application state.

Android must consume:
- app version policy
- `latestVersion`
- `minSupportedVersion`
- `forceUpdate`
- release notes / download URL where present
- maintenance mode/message
- feature flags
- wallet settings
- Admin-managed coin packages and Google Play IDs
- gift limits
- VIP general settings
- host application configuration
- supported login methods
- supported languages
- available healthy RTC providers
- push notification enablement
- upload limits/types
- room capacity
- speaker-seat limits
- audio profiles

The app hides/blocks unavailable capabilities based on backend configuration rather than hard-coded assumptions.

---

## 7. Authentication/session contract

### User Portal authentication
Must support the same R06 consumer auth capabilities when enabled:
- email/username + password — `/auth/login`
- registration — `/auth/register`
- phone OTP request — `/auth/phone/send-otp`
- phone login — `/auth/phone/login`
- Google ID token — `/auth/google/login`
- guest login — `/auth/guest/login`
- guest upgrade — `/auth/guest/upgrade`
- forgot password — `/auth/forgot-password`
- reset password — `/auth/reset-password`
- current identity — `/auth/me`
- refresh — `/auth/refresh`
- logout — `/auth/logout`
- logout all — `/auth/logout-all`

### Creator Portal authentication
- same `/auth/login` contract
- **must require returned role = `CREATOR`**
- non-Creator account must not enter Creator navigation
- Creator access application remains available
- maintenance behavior must match Creator Studio

### Session/device management
- `/auth/sessions`
- `/auth/sessions/:sessionId`
- `/auth/devices`
- `/auth/devices/:deviceId`
- `/auth/history`
- FCM/device registration through notification/device authority

Token refresh must also update Socket.IO authentication and any long-lived API clients.

---

## 8. Consumer Portal — complete parity scope

### A. Home/discovery/search
Android must cover R06 Website behavior for:
- Home
- Explore
- Live Rooms
- Room preview/detail
- People/Creators discovery
- Search results
- trending/popular/following/live discovery
- user/creator public profiles
- no backend-only/admin identities in public suggestions
- self-exclusion where the Website already enforces it

Primary API families:
- `/discovery/*`
- `/search/*`
- `/users/public/:username`
- `/users/:userId/profile`
- `/users/profile/me`

### B. Profile/social graph
- My Profile
- Public Profile
- Follow/unfollow
- Followers
- Following
- Friends
- friend requests
- suggested friends
- Edit Profile
- avatar upload
- cover upload
- visitors
- blocked users

Primary API families:
- `/users/*`
- `/users/friends/*`
- `/users/visitors/*`
- `/blocks/*`

### C. Communities
Must include R06 corrections, not only the older 84-screen map:
- community discovery
- community detail
- create community
- manage community
- public/private community
- secure invite code join
- membership status
- members
- community roles (`OWNER`/`ADMIN`/`MODERATOR`/`MEMBER`)
- Owner/Admin member management
- ownership transfer rules
- leave community
- private membership enforcement
- community rooms
- community events

Primary API family: `/clubs/*` plus `/scheduled-rooms/*`.

### D. Events/scheduling consumer view
- upcoming sessions/events
- event/session detail
- reminders
- community event directory
- ticket state where applicable

### E. Messaging
- inbox
- direct/group conversations as backend authorizes
- conversation detail
- send/read
- current Website parity first
- message reactions/advanced backend capabilities only when included in the approved Android parity matrix; do not invent divergence from Website/Creator behavior

Primary API: `/chat/*`.

### F. Notifications
- list
- unread count
- read one
- read all
- delete
- FCM push registration
- foreground/deep-link routing
- user-scoped realtime updates

### G. Live Room — listener experience
Must preserve backend/RTC authority for:
- room details/join preview
- public/private/invite-only/locked access
- ticketed access
- subscriber-only access
- verified-only access
- join/rejoin/leave
- listener participant state
- speaker stage state
- raise/cancel hand
- speaker invitation accept/reject behavior
- chat
- reactions/emojis
- in-room gifts
- participant updates
- host mute authority
- pause/resume/end events
- reconnect states
- ended-room summary/exit
- save/unsave room
- room activity join/leave duration

RTC: LiveKit is the only currently operational runtime provider and must be treated as such until backend authority changes.

### H. Host/speaker functions exposed by Consumer Website
Conditioned by backend role/capability:
- My Rooms
- Create Room
- Room Settings
- privacy/access controls
- schedule room
- edit/cancel schedule
- start/pause/resume/end broadcast
- microphone/publish control
- participant invitation
- stage approve/reject/invite/mute/remove
- polls
- quiz
- participant list
- ticket configuration
- subscriber/verified/invite restrictions

### I. Wallet/economy
- wallet overview
- wallet-only transaction history
- coin packages
- purchase preview
- Android Google Play purchase
- server validation
- purchase history
- diamond/coin conversion where exposed in final parity
- ticket purchases

### J. VIP
- tier catalogue with Admin-controlled artwork/data
- current membership
- benefits
- membership/subscription management
- purchase/upgrade/downgrade/renew according to Android backend readiness closure
- Cancel Renewal confirmation
- membership history
- rewards/badges/privileges if exposed by approved parity

### K. Referrals
- code generation
- summary
- history
- milestones
- rewards
- apply/claim flows as backend authorizes

### L. Gifts
- catalogue
- history
- send
- in-room send
- backend balance settlement only

### M. Store/inventory
- store catalogue
- purchase
- inventory
- equip/unequip

### N. Tasks/achievements/rankings
- tasks
- claim task rewards
- achievements
- XP/progress
- check-in
- season leaderboard
- rankings categories that have real backend authority
- **no Agency ranking** until Agency subsystem exists

### O. Replays/activity
- replay library
- replay player
- access control
- my room activity/history

### P. Settings/security/safety/content
- settings overview
- notification preferences
- privacy settings
- voice/audio preferences
- appearance/theme
- sessions/devices
- session/device detail
- revoke
- login activity
- Help/FAQ/legal CMS
- Report User/Room with human-readable target identity
- Safety Center
- Contact Support
- dynamic CMS pages
- About
- Maintenance
- Restricted/Suspended Account
- Session Expired

---

## 9. Creator Portal — complete Creator Studio parity scope

### A. Creator entry/access
- Creator Sign In
- role guard = `CREATOR`
- Creator access application
- maintenance state
- no Admin access

### B. Dashboard
`/creator/dashboard`
- canonical creator overview values only
- no locally fabricated analytics

### C. Analytics
- Creator Analytics period selection
- canonical backend `/analytics` authority

### D. Live Rooms
- creator room list
- create/update/delete where current Creator workflow exposes them
- start/pause/resume/end
- Live Room Console
- LiveKit host publishing
- authenticated Socket.IO
- participants/stage
- safe service-error messages

### E. Schedule
- scheduled room list
- create/edit/delete
- 12-hour UI where appropriate to match accepted Creator UX
- store canonical timezone-aware instant
- start scheduled broadcast

### F. Audience
- follower stats
- creator dashboard-derived audience metrics
- follower directory
- follow-back controls

### G. Creator Messages
- direct-message conversations only
- room chat excluded from creator inbox
- own messages right / other messages left
- Enter send / Shift+Enter newline equivalent behavior adapted to Android IME

### H. Followers
- pagination/search/sort
- follow/unfollow/follow-back

### I. Subscribers / Creator Plans
- create/update/archive creator subscription plans
- subscriber list
- subscription metrics
- no fake paid acquisition state
- Android consumer purchase path only after BR-03 financial closure

### J. Creator Wallet
- wallet balances
- summary
- transactions
- creator earnings context

### K. Earnings
- canonical creator earnings overview
- no fabricated values

### L. Gifts
- real received-gift/history values

### M. Payout Requests
- create payout request
- list
- detail/status
- threshold/balance validation remains backend authoritative

### N. Notifications
- creator notification list/unread behavior
- shared FCM + realtime core

### O. Creator Profile
- canonical creator/user profile
- save failures must be visible
- avatar/profile data authority stays backend

### P. Host Verification
- eligibility/status
- application data
- Government ID required
- selfie/profile verification asset required as current contract dictates
- optional supporting files
- private upload handling
- replacement flow
- no fabricated identity/experience defaults

### Q. Creator Settings
- canonical user/settings data
- streaming credentials where current Creator Studio exposes them
- no local fake persistence

### R. Help / Creator CMS
- persisted Contact Support submission to Admin inbox
- Creator/Host CMS pages based on backend audience rules
- no local `mailto:` fallback

---

## 10. Realtime and RTC implementation contract

### Socket.IO
Android realtime manager must:
- authenticate with current access token
- reconnect with bounded backoff
- update auth after refresh
- join/leave room presence authoritatively
- handle foreground/background lifecycle without creating ghost membership
- subscribe only to required user/room scopes

Relevant event families include:
- presence/join/leave/reconnect
- participant joined/left/reconnected
- room paused/resumed/ended
- room invitation/revocation/kick/ban
- stage/speaker queue/promote/demote/mute/unmute
- reaction broadcast
- gifts
- chat updates
- polls
- quiz
- notifications
- maintenance/feature changes where applicable

The exact event registry must be generated and frozen in PH01 from R06 server gateway source.

### LiveKit
Android must use official LiveKit Android client for:
- connect/disconnect
- token-based role authority
- listener receive-only state
- microphone publishing only when allowed
- participant tracks/state
- reconnection
- token refresh/rejoin

Do not display provider/key configuration details to users or creators.

---

## 11. Android billing contract

### Wallet coins
1. Read package and `googlePlayProductId` from `/config/mobile` or `/wallet/packages`.
2. Query Play Billing product details.
3. User completes Play purchase.
4. Call VoiceCloud purchase initiation/idempotency path as required.
5. Submit purchase token to `/wallet/purchase/validate` with `GOOGLE_PLAY`.
6. VoiceCloud verifies against Google Developer API and grants coins transactionally.
7. Only after backend success, acknowledge/consume through Play Billing as appropriate.
8. Reconnect/retry must use idempotency; never grant twice.

### VIP
Implement only after BR-02 backend closure. Android subscriptions must use real Play subscription authority, not web Stripe/Razorpay/PayPal checkout and not one-time purchase emulation.

### Website checkout providers
Stripe/Razorpay/PayPal hosted checkout remains Website authority for web flows. Android digital-goods UI must not simply open those Website checkout flows as a substitute for Google Play Billing.

---

## 12. UI/UX and branding contract

### Shared app branding
Centralized Android theme authority must include:
- logo/assets
- User Royal Sapphire palette
- Creator portal palette/profile matching approved Creator Studio identity
- typography
- dimensions/spacing
- shapes/radii
- elevation/shadows
- semantic colors
- iconography
- motion rules

No feature screen may hard-code brand colors.

### Consumer appearance
- fresh install / logged-out default: **Light**
- authenticated preference: Light / Dark / System, persisted through backend settings where supported

### Creator appearance
Creator Portal may retain its differentiated Creator Studio visual identity while still using the centralized shared branding layer.

### Mobile adaptation
Mobile layout may differ from desktop Website/Creator Studio for usability, but:
- feature names
- actions
- states
- permissions
- validation
- backend effects
must stay behaviorally equivalent.

---

# 13. LOCKED DEVELOPMENT ROADMAP

## VC-ANDROID-A0 — Android Backend Readiness Closure (pre-development gate)
**This is mandatory before feature implementation is considered locked.**

A0 does not build Android UI. It closes and freezes Android-specific backend contracts that the Website does not currently exercise:
- BR-02 Google Play VIP subscription product/base-plan mapping, server verification, renewal/cancellation and RTDN reconciliation
- BR-03 decision and authoritative settlement model for paid Creator subscriptions / subscriber-only access
- BR-05 verified reset-password App Link/domain contract
- final Android mobile-config schema including VIP/Play product authority
- explicit confirmation that Agency is excluded
- generated Android API contract manifest and backend readiness regression tests

A0 must be delivered as a backend corrective revision starting from the exact R06 Git baseline and must be Git-frozen before PH01. Android code must not invent temporary workarounds for these gaps.

---


## VC-ANDROID-PH01 — Foundation, Branding, Bootstrap & Contract Lock
**Scope**
- new Android project from scratch
- Gradle/version catalogue
- debug/staging/release variants
- Compose/Hilt/network/database/security foundations
- centralized User + Creator branding
- API error envelope/modeling
- `/config/mobile` bootstrap
- version/force-update/maintenance
- feature flags
- secure token vault scaffolding
- App-level logging/error policy
- canonical API manifest generated from R06
- canonical Socket.IO event manifest generated from R06
- canonical route/screen parity manifest for 74 Website + 22 Creator sources / 84 approved Website designs + R06 additions

**Acceptance**
The app can launch, retrieve backend mobile config, show real maintenance/update states, and has no feature business logic yet outside the locked foundation.

---

## VC-ANDROID-PH02 — Dual Portal Authentication & Account Lifecycle
**User Portal**
- portal selector
- email/username login
- registration
- phone OTP
- Google login
- guest login
- guest upgrade
- forgot/reset password
- first-time onboarding
- restricted account
- session expired

**Creator Portal**
- separate Creator login UI
- role = CREATOR validation
- Creator access application entry
- maintenance handling

**Shared**
- refresh-token single-flight
- logout/logout-all
- secure session restore
- device metadata
- FCM token registration foundation
- HTTPS App Link for reset-password

---

## VC-ANDROID-PH03 — Consumer Home, Discovery, Search, Profile & Social
- Home
- Explore
- Live Rooms discovery/listing
- People/Creators
- Search
- public profile
- my profile shell
- follow/unfollow
- followers/following
- friends/requests/suggestions
- public identity filtering
- self exclusion
- profile navigation and human-readable identity rules

---

## VC-ANDROID-PH04 — Communities, Events, Messaging & Notifications
- communities list/detail
- create/manage community
- public/private membership
- secure invite code
- Owner/Admin/Moderator/Member behaviors
- members, community rooms, community events
- upcoming events/session details/reminders
- messages inbox
- conversation
- read state
- notifications/unread/read/delete
- FCM routing
- user-scoped realtime notifications

---

## VC-ANDROID-PH05 — Live Room Listener, Access, Realtime & Engagement
- room detail/join preview
- LiveKit listener connection
- RTC token/join/rejoin/leave
- participants
- room access rules
- ticket/subscriber/verified/invite/private states
- raise hand
- speaker invitation acceptance/rejection
- chat
- reactions
- in-room gifts
- save room
- reconnect
- pause/resume/end
- activity tracking
- foreground/background room lifecycle correctness

---

## VC-ANDROID-PH06 — Host/Speaker Room Management, Scheduling, Stage, Polls & Quiz
- My Rooms
- Create Room
- Room Settings
- Room privacy/access
- Schedule + edit/delete
- participant invitations
- start/pause/resume/end
- microphone/publishing
- host/speaker mode
- stage approve/reject/invite/mute/remove
- participant management
- polls
- quiz
- ticket configuration/access
- backend-authoritative RTC state

---

## VC-ANDROID-PH07 — Consumer Economy & Progression
- wallet overview
- wallet-only transaction history
- Google Play coin top-up
- purchase validation/idempotency/restore
- purchase history
- VIP catalogue/membership/history/subscription management after BR-02 closure
- referrals
- gifts catalogue/history
- store
- inventory/equip/unequip
- tasks
- achievements
- XP/check-in
- rankings/leaderboards (no Agency fabrication)
- scheduled-room tickets
- creator paid subscription acquisition only after BR-03 closure

This phase cannot be accepted until Android-specific financial backend gaps are closed and regression-tested.

---

## VC-ANDROID-PH08 — Replays, Activity & Extended Profile
- replay library
- replay player
- access control
- My Activity
- Edit Profile
- avatar
- cover
- blocked users
- profile visitors
- robust media loading/fallback

---

## VC-ANDROID-PH09 — Preferences, Security, Safety, CMS & Support
- Settings overview
- notification preferences
- privacy
- voice/audio preferences
- Appearance: Light/Dark/System
- English-only current language state
- security overview
- sessions/devices
- detail/revoke
- login activity
- Help/FAQ/legal
- report User/Room
- Safety Center
- dynamic CMS pages
- Contact Support
- About
- maintenance/restricted/session-expired global handling

At PH09 completion, **Consumer Portal feature parity is complete**.

---

## VC-ANDROID-PH10 — Creator Portal Core, Dashboard, Profile, Settings & Support
- Creator portal shell/navigation
- Creator session role guard
- dashboard
- creator profile
- creator settings
- creator CMS
- Help/Contact Support
- Creator access application linkage
- maintenance handling
- cross-portal switch for CREATOR accounts only

---

## VC-ANDROID-PH11 — Creator Live Rooms & Schedule
- creator room list
- room creation/update/lifecycle
- Live Room Console
- LiveKit host audio
- start/pause/resume/end
- participant/stage controls
- safe RTC failure messages
- schedule create/edit/delete
- local timezone conversion
- 12-hour presentation where applicable
- scheduled room start

---

## VC-ANDROID-PH12 — Creator Audience, Messaging, Followers & Subscribers
- Audience
- follower metrics
- Followers directory/search/sort/follow-back
- Creator direct messages only
- direct conversation UI
- creator subscription plan create/update/archive
- subscriber list
- subscription metrics
- no unverified/fake payment state

---

## VC-ANDROID-PH13 — Creator Analytics, Wallet, Earnings, Gifts, Payouts & Host Verification
- Analytics
- Creator wallet
- Earnings
- Gifts
- Payout Requests create/list/detail
- Creator notifications
- Host eligibility/progression
- Host verification application
- private Government ID/selfie/support asset upload/replacement
- stream credentials where current Creator Studio exposes them
- authoritative error handling

At PH13 completion, **Creator Portal feature parity is complete**.

---

## VC-ANDROID-PH14 — Native Hardening, Full Parity Reconciliation & Production Certification
- FCM end-to-end notification delivery
- App Links/deep links
- background/foreground lifecycle
- LiveKit reconnect/permission edge cases
- Socket reconnect/auth refresh
- Google Play purchase recovery/replay tests
- theme/restoration/process-death tests
- offline/cache bounds
- accessibility
- performance/startup/memory
- release signing configuration process
- ProGuard/R8 rules
- crash-safe logging/redaction
- screenshot/privacy handling where applicable
- complete API parity audit against R06 + Android backend closure changes
- complete route/screen parity audit
- authorization/role audit
- migration/config compatibility verification
- production package hygiene
- clean-build verification

Only PH14 can certify the Android product as production-complete.

---

## 14. Per-phase acceptance workflow — mandatory

Every phase is implemented through controlled internal sequences, but only **one consolidated phase package/build** is delivered for acceptance.

Before a phase may be called complete, run in this order:

1. `compileDebugKotlin`
2. `compileStagingKotlin`
3. `compileReleaseKotlin`
4. phase unit tests
5. shared/domain/network/repository tests affected by the phase
6. API contract/parity tests
7. authorization/security tests
8. Android lint
9. `assembleDebug`
10. `assembleStaging`
11. `assembleRelease`
12. instrumentation/device tests applicable to the phase
13. phase-specific source/architecture regression checks
14. clean-package verification

No source/static verifier is sufficient by itself.

### Each phase delivery must contain
- complete phase source ZIP/package
- APK(s) appropriate for manual test
- implementation report
- automated/build acceptance evidence
- phase-specific manual QA checklist mapped to locked requirements
- API endpoint mapping for the phase
- known external credential dependencies, if any

### Approval/freeze sequence
1. Assistant completes phase and gates.
2. User runs manual QA / workstation acceptance.
3. Any issue is corrected in the **same phase revision**.
4. User approves.
5. Phase is committed/frozen to a new Git branch.
6. Next phase starts only from that exact accepted Git commit.

---

## 15. Testing strategy

### Unit
- ViewModels
- reducers/state machines
- use cases
- DTO/domain mapping
- token refresh
- payment idempotency
- access-state decisions

### Repository/API contract
- every Android endpoint mapped to exact backend method/path/DTO
- error status handling
- human-readable identity projection
- feature-flag behavior

### Realtime
- auth in-flight
- reconnect
- room membership
- stage state
- reactions/chat
- room pause/resume/end

### RTC
- listener
- speaker
- host
- mute authority
- token refresh/rejoin
- network loss/recovery
- background/foreground

### Financial
- Play purchase pending/cancelled/completed
- server verification failure
- duplicate callbacks
- replay tokens
- refund/revoked state when applicable
- VIP renewal/cancel after BR-02

### Security
- role gates
- portal isolation
- token storage
- logout revocation
- session/device visibility
- private community access
- private verification media
- report/moderation identity handling

### UI
- Light default
- Dark/System preference
- text contrast
- loading/empty/error states
- image fallbacks
- TalkBack/content descriptions
- font scaling
- small/large screen layouts

---

## 16. Change-control / anti-drift rule

Once approved, this plan is frozen.

A future implementation prompt must always include:
- authoritative Git branch + exact commit
- current Android phase and revision
- exact parent phase commit
- locked phase scope
- forbidden scope changes
- API authority statement
- branding authority statement
- mandatory acceptance commands
- expected delivery artifacts

No phase may silently:
- move a feature to a later phase
- delete a Website/Creator behavior
- create an unsupported Android-only business workflow
- replace backend authority with local state
- add Admin features
- enable an unimplemented provider
- expose technical IDs
- fake financial/RTC success

Any newly discovered backend defect required for Android parity must be documented as a **platform corrective dependency**, fixed at the backend with regression coverage, and then consumed by Android. It must never be worked around by Android in a way that creates a second business authority.

---

## 17. Final Definition of Done

VoiceCloud Android is complete only when all of the following are true:

1. One Android app provides both User and Creator portals.
2. Consumer Portal has parity with the finalized R06 Consumer Website plus accepted R06 corrections.
3. Creator Portal has parity with the finalized R06 Creator Studio.
4. Portal/role separation is enforced by backend identity.
5. All Android-relevant backend readiness gaps are closed.
6. All Android API calls are mapped to authoritative backend routes/DTOs.
7. LiveKit/Socket.IO behavior matches backend authority.
8. Google Play settlement is server-authoritative.
9. Admin-configured CMS, VIP, wallet, gifts, tasks, versions, providers and feature flags flow dynamically into Android where relevant.
10. No technical UUID is exposed where a human-readable identity exists.
11. No fake success/fabricated runtime data exists.
12. Every PH01–PH14 phase is build-accepted and manually approved.
13. Full final parity, security, API, realtime, RTC, billing, migration and release gates pass.
14. Final source is Git-frozen at an exact commit and clean working tree.

