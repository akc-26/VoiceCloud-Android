# VoiceCloud Android — R06 Product Coverage Audit R01

Generated: 2026-09-18 15:09 UTC

## Authority
- Master plan: `docs/VC-ANDROID-R06-MASTER-DEVELOPMENT-PLAN-R01.md`
- Screen parity: `contracts/screens/VC-ANDROID-R06-SCREEN-PARITY-R01.json`
- API contract: `contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json`
- Realtime contract: `contracts/realtime/VC-ANDROID-R06-SOCKET-EVENTS-R01.json`

## Executive summary

| Metric | Count |
| --- | ---: |
| Website + Creator parity page sources | 96 |
| Backend operations in contract | 700 |
| Realtime inbound subscribe messages | 60 |
| Realtime outbound emit literals | 102 |
| Android navigation routes (current) | 19 |
| Pages with partial UI shell only | 13 |
| Auth/account pages (PH02 scope) | 5 |
| Pages with no Android implementation | 78 |

**Status:** Android is **not** product-complete. Current branch delivers PH01 foundation + redesigned presentation shells for a subset of routes. Backend API/realtime wiring, auth lifecycle, live room authority, economy settlement, and the majority of parity pages remain to be implemented per master plan phases PH02–PH14.

## A. Discovered screens/features (parity sources)

Total page sources: **96** (Consumer website 74, Creator Studio 22)

## B–D. Implementation totals

| Tier | Count | Meaning |
| --- | ---: | --- |
| Partial UI shell (no production data/API) | 13 | Compose layout exists; placeholders / empty states |
| Auth lifecycle (contracted, in progress PH02) | 5 | Requires `feature:auth` + navigation gates |
| Missing Android surface | 78 | No route/screen/workflow |

## G. Current Android navigation routes

- `bootstrap`
- `user`
- `creator`
- `user/home`
- `user/discover`
- `user/search`
- `user/room/preview`
- `user/live`
- `user/messages`
- `user/messages/thread`
- `user/profile`
- `user/wallet`
- `user/settings`
- `creator/dashboard`
- `creator/audience`
- `creator/live`
- `creator/analytics`
- `creator/workspace`
- `creator/settings`

## H. API areas (contract inventory)

Top operation prefixes (first 4 path segments):

- `rtc/rooms/:param` — 13 operations
- `chat/conversations/:param` — 12 operations
- `admin/users/:param` — 10 operations
- `chat/messages/:param` — 10 operations
- `admin/providers/:param` — 9 operations
- `analytics/rooms/:param` — 7 operations
- `admin/wallet/creator` — 6 operations
- `gifts/admin/:param` — 6 operations
- `moderation/users/:param` — 6 operations
- `admin/backups/:param` — 4 operations
- `admin/backups/schedules` — 4 operations
- `admin/creator-access-applications/:param` — 4 operations
- `admin/referrals/campaigns` — 4 operations
- `admin/store/items` — 4 operations
- `admin/tasks-achievements/achievements` — 4 operations
- `admin/tasks-achievements/seasons` — 4 operations
- `admin/tasks-achievements/tasks` — 4 operations
- `admin/wallet/packages` — 4 operations
- `admin/wallet/transactions` — 4 operations
- `gifts/admin/categories` — 4 operations
- `users/friends/request` — 4 operations
- `vip/admin/plans` — 4 operations
- `vip/admin/tiers` — 4 operations
- `admin/backups/restore` — 3 operations
- `admin/cms/:param` — 3 operations
- `admin/contact-submissions/:param` — 3 operations
- `admin/messaging/conversations` — 3 operations
- `admin/referrals/blacklist` — 3 operations
- `android/billing/creator` — 3 operations
- `/announcements/:param` — 3 operations
- `clubs/:param/members` — 3 operations
- `/clubs/:param` — 3 operations
- `creator/plans/:param` — 3 operations
- `hosts/verification/assets` — 3 operations
- `moderation/reports/:param` — 3 operations
- `/rooms/:param` — 3 operations
- `rooms/saved/:param` — 3 operations
- `/scheduled-rooms/:param` — 3 operations
- `/search/history` — 3 operations
- `/storage/:param` — 3 operations

## I. Realtime contract

- Inbound subscribe messages: **60**
- Outbound emit literals: **102**

## Missing parity pages (sample)

- `creator/src/pages/CreatorAccessApplicationPage.tsx`
- `creator/src/pages/CreatorCmsPage.tsx`
- `creator/src/pages/CreatorMaintenancePage.tsx`
- `creator/src/pages/EarningsPage.tsx`
- `creator/src/pages/FollowersPage.tsx`
- `creator/src/pages/GiftsPage.tsx`
- `creator/src/pages/HelpPage.tsx`
- `creator/src/pages/HostVerificationPage.tsx`
- `creator/src/pages/LiveRoomConsolePage.tsx`
- `creator/src/pages/LiveRoomsPage.tsx`
- `creator/src/pages/NotificationsPage.tsx`
- `creator/src/pages/PayoutRequestsPage.tsx`
- `creator/src/pages/SchedulePage.tsx`
- `creator/src/pages/SubscribersPage.tsx`
- `website/src/pages/AboutPage.tsx`
- `website/src/pages/AchievementsPage.tsx`
- `website/src/pages/ActivityHistoryPage.tsx`
- `website/src/pages/BlockedUsersPage.tsx`
- `website/src/pages/CmsPublicPage.tsx`
- `website/src/pages/CommunitiesPage.tsx`
- `website/src/pages/CommunityDetailsPage.tsx`
- `website/src/pages/CommunityEventsPage.tsx`
- `website/src/pages/CommunityManagePage.tsx`
- `website/src/pages/CommunityMembersPage.tsx`
- `website/src/pages/CommunityRoomsPage.tsx`
- `website/src/pages/ContactPage.tsx`
- `website/src/pages/CreateCommunityPage.tsx`
- `website/src/pages/CreateRoomPage.tsx`
- `website/src/pages/DevicesSessionsPage.tsx`
- `website/src/pages/EditProfilePage.tsx`
- `website/src/pages/EventDetailsPage.tsx`
- `website/src/pages/EventsPage.tsx`
- `website/src/pages/FeaturePlaceholderPage.tsx`
- `website/src/pages/FriendsPage.tsx`
- `website/src/pages/GiftCatalogPage.tsx`
- `website/src/pages/GiftHistoryPage.tsx`
- `website/src/pages/HelpLegalPage.tsx`
- `website/src/pages/HomeFoundationPage.tsx`
- `website/src/pages/HostSchedulePage.tsx`
- `website/src/pages/InventoryPage.tsx`
- `website/src/pages/LiveRoomsPage.tsx`
- `website/src/pages/LoginActivityPage.tsx`
- `website/src/pages/MaintenancePage.tsx`
- `website/src/pages/MyProfilePage.tsx`
- `website/src/pages/MyRoomsPage.tsx`
- `website/src/pages/NotFoundPage.tsx`
- `website/src/pages/NotificationPreferencesPage.tsx`
- `website/src/pages/NotificationsPage.tsx`
- `website/src/pages/PeoplePage.tsx`
- `website/src/pages/PreferencesPage.tsx`
- `website/src/pages/PrivacySettingsPage.tsx`
- `website/src/pages/ProfileVisitorsPage.tsx`
- `website/src/pages/RankingsPage.tsx`
- `website/src/pages/ReferralPage.tsx`
- `website/src/pages/ReplayLibraryPage.tsx`
- `website/src/pages/ReplayPlayerPage.tsx`
- `website/src/pages/ReportPage.tsx`
- `website/src/pages/RoomDetailsPage.tsx`
- `website/src/pages/RoomExperiencePage.tsx`
- `website/src/pages/RoomSettingsPage.tsx`
- … and 18 more

## Phase mapping (master plan)

| Phase | Scope | Android status |
| --- | --- | --- |
| PH01 | Foundation, bootstrap, contracts | **Implemented** |
| PH02 | Dual-portal auth & account lifecycle | **In progress** |
| PH03 | Home, discovery, search, profile, social | UI shells only |
| PH04 | Communities, events, messaging, notifications | UI shells / missing |
| PH05–PH06 | Live room listener + host/stage | UI shells / missing RTC wiring |
| PH07 | Consumer economy | UI shell only |
| PH08–PH09 | Replays, security, safety, CMS | Missing |
| PH10–PH13 | Creator product | UI shells only |
| PH14 | Parity reconciliation & certification | Not started |

## Blocked / backend-limited (from master plan BR items)

- **VIP Google Play subscriptions (BR-02):** backend subscription mapping incomplete — Android must not fake VIP purchase.
- **Paid creator subscriptions (BR-03):** platform economy not closed — display authorized state only.
- **Agency (BR-04):** excluded — no Android agency product.

---

Regenerate: `python3 tools/generate_product_coverage_audit.py`
