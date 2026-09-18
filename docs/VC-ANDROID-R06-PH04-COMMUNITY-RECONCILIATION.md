# PH04 Community contract reconciliation

## Parity requirement (screen parity / master plan)

Consumer surfaces: Communities list, Community details, Members, Community rooms, Create community, Manage community (where authorized).

## Contract search performed

- `contracts/screens/VC-ANDROID-R06-SCREEN-PARITY-R01.json` — `CommunitiesPage`, `CommunityDetailsPage`, `CommunityMembersPage`, `CommunityRoomsPage`, `CreateCommunityPage`, `CommunityManagePage`
- `contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json` — full scan for `community`, `communities`, `club`, `clubs`, `members`

## Authoritative backend naming

VoiceCloud R06 exposes **clubs** REST APIs (not `/communities`):

| Operation | Method | Path |
| --- | --- | --- |
| List clubs | GET | `/clubs` |
| Create club | POST | `/clubs` |
| Club detail | GET | `/clubs/:param` |
| Update club | PATCH | `/clubs/:param` |
| Delete club | DELETE | `/clubs/:param` |
| Join | POST | `/clubs/:param/join` |
| Leave | POST | `/clubs/:param/leave` |
| Members | GET | `/clubs/:param/members` |
| Scheduled rooms | GET | `/clubs/:param/scheduled-rooms` |
| Member admin | PATCH/DELETE | `/clubs/:param/members/:param` |

Related discovery: `/trending/clubs`, `/rankings/leaderboard/clubs`, `/rankings/trending/clubs`

## Android implementation (2026-09-18)

- Routes: `user/communities`, `user/communities/{clubId}`, `user/communities/create`
- API → `ClubsApi` → `ClubsRepository` → `CommunitiesViewModel` / `CommunityDetailViewModel` / `CreateCommunityViewModel` → screens
- **Manage community** (full admin UI for roles, invite codes, member moderation): **PARTIAL** — API exists; dedicated manage screen not yet split from detail (join/leave/members/rooms on detail)

## BLOCKED

None for core consumer club list/detail/join/leave/members/scheduled-rooms/create when backend returns data.

## MISSING (non-club parity)

Separate website-only CMS/community event pages without matching mobile club endpoints remain tracked in the main audit until event/club event APIs are wired separately.
