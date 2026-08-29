# VC-ANDROID-PH12-R01 Manual QA

Use a CREATOR account and real backend data. Do not accept locally invented state.

- **PH12-QA-001 Audience:** Open Creator Dashboard → Audience. Follower/following values agree with backend profile/social data. Subscriber total shows a number only when backend pagination supplies it; otherwise it shows unavailable/unknown rather than `0`.
- **PH12-QA-002 Followers search:** Search by a known follower name/username. Matching backend followers appear; non-matching rows do not.
- **PH12-QA-003 Followers sort:** Switch Name / Popularity / Online and verify deterministic reordering without changing membership.
- **PH12-QA-004 Follow back:** Follow a follower back. Button/state reconciles from the server response. Reopen the screen and verify persistence.
- **PH12-QA-005 Unfollow:** Unfollow a followed-back follower and verify server-backed state after reload.
- **PH12-QA-006 Direct inbox isolation:** Creator Messages lists direct conversations only. Room and group conversations must not appear.
- **PH12-QA-007 Direct detail isolation:** Attempt to deep-link a room/group conversation into Creator Messages. The Creator direct-message path must reject it rather than render it.
- **PH12-QA-008 Messaging alignment:** Own messages render right; other participant messages render left.
- **PH12-QA-009 Keyboard send:** Enter text and use keyboard Send; exactly one message is submitted. Blank messages remain blocked.
- **PH12-QA-010 Conversation removal:** Delete one/multiple direct conversations. Inbox refresh remains direct-only.
- **PH12-QA-011 Create plan:** Create a plan with title, monthly price, optional yearly price, benefits and visibility. Reload and verify backend persistence.
- **PH12-QA-012 Update plan:** Edit title/description/prices/benefits/visibility/status. Reload and verify server state.
- **PH12-QA-013 Archive plan:** Archive a plan. Verify backend archive/deactivate state and that Android does not hard-delete or fabricate a payment event.
- **PH12-QA-014 Subscribers:** Open Subscribers and filter All/Active/Pending/Cancelled/Expired. Rows and totals match backend results.
- **PH12-QA-015 Payment truth:** Verify PH12 contains no UI claiming a subscription purchase succeeded merely from local state or subscription intent.
- **PH12-QA-016 Role guard:** Sign in as non-CREATOR and attempt PH12 creator routes; privileged Creator pages must not open.
- **PH12-QA-017 Regression:** Creator Live/Schedule, Profile, Settings and Help from PH10/PH11 remain operational.
