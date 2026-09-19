# VC-ANDROID-PH08-R02 Manual QA

## A. Home / Explore / Search
- [ ] A01 Home header shows VoiceCloud branding cleanly and gives substantially more space to live rooms than People/Creators.
- [ ] A02 Explore is visually/functionally distinct and emphasizes trending rooms, topics/categories, communities and events.
- [ ] A03 Open Search without typing: All shows People, Creators, Rooms and Communities sections with compact results and View all.
- [ ] A04 Open each Search tab before typing: relevant/default activity-ranked content is shown.
- [ ] A05 Search each tab and confirm relevant results replace defaults; an empty result shows a centered category-specific not-found state.

## B. Friends / Profile / CMS / Onboarding
- [ ] B01 Friends lists current friends, incoming requests, sent requests and suggestions.
- [ ] B02 Accept/reject incoming request and cancel sent request; refresh and verify backend persistence.
- [ ] B03 Profile exposes prioritized Economy modules directly; Referrals is lower priority.
- [ ] B04 Edit Profile opens separately and back navigation uses the top-left arrow icon.
- [ ] B05 Change avatar using a new account with no avatar, then replace it; repeat for cover. Verify persistence after relogin.
- [ ] B06 Recommended 800x800 profile and 1600x600 cover guidance is visible.
- [ ] B07 Help, terms & information lists Admin CMS pages; open multiple pages and verify server content.
- [ ] B08 Complete signup onboarding once, sign out/in, and verify onboarding does not repeat. Edit Profile remains the change path.

## C. Economy
- [ ] C01 Wallet, VIP, Store, Gifts, Tasks, Achievements, XP/Check-in, Rankings, Tickets and Referrals open without raw-object/debug text.
- [ ] C02 Break one optional read endpoint and verify unrelated Economy data still renders where available.
- [ ] C03 Mutation actions refresh server state and do not fabricate success.

## D. Hosted payment rail — current Stripe testing
Preparation: keep `VOICECLOUD_ANDROID_PAYMENT_MODE=HOSTED_GATEWAY`; Admin must have one active/enabled/healthy hosted payment profile. Current environment may use Stripe.
- [ ] D01 Wallet package Add credits opens secure hosted checkout; Android does not expose Stripe/Razorpay/PayPal choice or credentials.
- [ ] D02 Cancel checkout: Wallet balance does not change; returning to app refreshes canonical state.
- [ ] D03 Complete Stripe sandbox checkout: backend/webhook verification credits Wallet exactly once; history persists after relogin.
- [ ] D04 Repeat/reload provider return: no duplicate credit.
- [ ] D05 VIP Choose VIP opens hosted checkout; verified payment updates membership server-side.
- [ ] D06 Admin switches active healthy provider to Razorpay; same Android build opens Razorpay-hosted checkout without source changes.
- [ ] D07 Repeat with PayPal when configured/healthy; Android flow remains identical.
- [ ] D08 Provider/config errors shown to consumer are generic and do not expose keys/provider secrets.

## E. Google Play launch rail
Preparation: build with `VOICECLOUD_ANDROID_PAYMENT_MODE=GOOGLE_PLAY`, Play Console products/subscriptions and backend product mappings configured, and use licensed Play test account.
- [ ] E01 Wallet package shows Google Play rail and launches native purchase UI only for mapped products.
- [ ] E02 Cancel Play purchase: no Wallet credit.
- [ ] E03 Complete Play purchase: VoiceCloud verifies token before Wallet credit; restore/retry does not duplicate credit.
- [ ] E04 VIP mapped subscription launches native subscription UI; server verification precedes VIP activation.
- [ ] E05 Restore purchases recovers authoritative Wallet/VIP state after reinstall/relogin.

## F. Live room / navigation
- [ ] F01 Selecting a live room from Home/Explore/Search/Live Rooms enters the live experience directly without redundant Join card.
- [ ] F02 Long room title uses up to two header lines and is not unnecessarily truncated.
- [ ] F03 Chat composer remains fixed at bottom while room content scrolls; send a message successfully.
- [ ] F04 Emoji and Gift floating icons open contained pickers and send room actions.
- [ ] F05 Leave control is clearly identified in red without obstructing room controls.
- [ ] F06 Main tabs and secondary pages transition smoothly without jarring full-screen jumps.

## G. PH08-R01 preservation
- [ ] G01 Replay library/detail/player states still work for available, processing, unavailable and access-restricted replays.
- [ ] G02 My Activity still loads canonical room activity.
- [ ] G03 Blocked users and Profile Visitors/statistics still work and persist server-side.
