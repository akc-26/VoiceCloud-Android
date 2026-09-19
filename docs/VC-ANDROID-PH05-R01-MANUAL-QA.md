# VC-ANDROID-PH05-R01 Manual QA Checklist

Run against the live Raspberry Pi/Tailscale backend on a physical Android device.

| ID | Test | Expected result |
|---|---|---|
| PH05-QA-001 | Cold launch | Temporary branded splash appears cleanly; no blank/legacy splash flash. |
| PH05-QA-002 | Bottom navigation | Home, Explore, Search, Friends, Profile show real distinct icons, not placeholder circles. |
| PH05-QA-003 | Tab label/layout | Icons and labels remain aligned and single-line at normal font scale. |
| PH05-QA-004 | White-label visual smoke | Consumer colors/gradients/surfaces match centralized Website-derived branding authority. |
| PH05-QA-005 | Open live room from Home | Room preview opens with professional shared header and no status-bar collision. |
| PH05-QA-006 | Open live room from Explore/Rooms | Same room preview/navigation behavior. |
| PH05-QA-007 | Room preview metadata | Title, status, listener/speaker counts, category/language render correctly. |
| PH05-QA-008 | Save room | Save/unsave updates correctly and survives reload. |
| PH05-QA-009 | Normal live room join | Listener enters room and receives remote audio. |
| PH05-QA-010 | Listener safety | App does not request microphone permission merely to listen. |
| PH05-QA-011 | Listener publishing | Listener does not publish microphone/video tracks automatically. |
| PH05-QA-012 | Participant list | Participants appear without duplicate-key crash while scrolling/refreshing. |
| PH05-QA-013 | Participant updates | Join/leave/reconnect presence changes appear without reopening room. |
| PH05-QA-014 | Locked room | Clear consumer-safe locked state; no provider/internal error exposed. |
| PH05-QA-015 | Invite-only room | Required invitation state is shown safely. |
| PH05-QA-016 | Ticket/premium room | Ticket-required state is shown; no nonexistent PH06/unsupported purchase flow is invented. |
| PH05-QA-017 | Subscriber-only room | Subscription restriction is shown correctly. |
| PH05-QA-018 | Verified-only room | Verification restriction is shown correctly. |
| PH05-QA-019 | Community room | Community-membership restriction is represented correctly. |
| PH05-QA-020 | Raise hand | Raise hand reaches backend/realtime and local state updates. |
| PH05-QA-021 | Lower hand | Raised hand can be cancelled cleanly. |
| PH05-QA-022 | Speaker invitation accept | Invitation can be accepted without enabling microphone publishing in PH05. |
| PH05-QA-023 | Speaker invitation reject | Invitation can be rejected and pending state clears. |
| PH05-QA-024 | Send room reaction | Reaction broadcasts and appears without UI growth/collision. |
| PH05-QA-025 | Room chat | Messages load and scroll; no duplicate-key crash. |
| PH05-QA-026 | Send chat message | Message sends and refreshes correctly. |
| PH05-QA-027 | React to chat message | Message reaction request succeeds and UI remains stable. |
| PH05-QA-028 | Gift catalogue | Available gifts load without blocking room audio. |
| PH05-QA-029 | Send gift | Gift send succeeds or returns a consumer-safe economy/access error. |
| PH05-QA-030 | Room pause | Pause state is reflected and listener actions are appropriately disabled. |
| PH05-QA-031 | Room resume | Listener returns to active room state without duplicate session. |
| PH05-QA-032 | Room end | End state closes/halts the listener session cleanly. |
| PH05-QA-033 | Network interruption | Reconnect path restores the current listener session when backend permits. |
| PH05-QA-034 | Background/foreground | Background then foreground preserves or cleanly restores the listener state without double audio. |
| PH05-QA-035 | Leave during initial join | Back/leave immediately after Join; no later stale connection opens the room. |
| PH05-QA-036 | Leave during reconnect | Leave while reconnecting; old reconnect result must not restore the room. |
| PH05-QA-037 | Rapid room switching | Start room A then quickly room B; only room B owns the final RTC session/audio. |
| PH05-QA-038 | Rapid retry | Repeated retry cannot create multiple simultaneous RTC connections. |
| PH05-QA-039 | Back navigation | Back arrow/header is aligned and returns to the previous discovery page. |
| PH05-QA-040 | Status/navigation bars | Preview/live/secondary pages stay clear of Android system bars. |
| PH05-QA-041 | Long room title | Header/card text ellipsizes cleanly; controls do not wrap over each other. |
| PH05-QA-042 | Large participant/message content | Scrolling remains stable and does not crash. |
| PH05-QA-043 | Logout after room use | Session/token/realtime cleanup still works after leaving room. |
| PH05-QA-044 | Creator/User portal preservation | Existing PH02 authentication and PH03/PH04 pages still work normally. |
