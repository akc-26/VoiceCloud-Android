# VC-ANDROID-PH13-R19 — Physical-Device Video Fidelity Matrix

| Area exposed by device review | R15/R18 problem | R19 product correction | Authority preserved |
|---|---|---|---|
| Listener center nav | microphone-style center control opened Search | center Live action opens live-room/listening path; Search remains explicit in Discover/header | existing room/search routes |
| Listener navigation | Home/Explore/Search/Friends/Profile meanings were visually/product-wise confused | Home / Discover / Live / Messages / Profile semantics | existing navigation destinations |
| Home | Discover-like title and sparse/generic hierarchy | `For You`, featured live content, creator discovery, quick access | real discovery state |
| Discover/Search | Search treated like a primary bottom destination | Search becomes Discover-context capability | existing search API/state |
| Profile | profile behaved like wallet/settings hub | backend cover/avatar identity, stats, Edit Profile + Wallet actions | existing profile/economy state |
| Portal selector | standalone Speaker portal implied false account role | Listener + Creator only; Speaker remains runtime participation state | existing auth roles |
| Creator center nav | unexplained `+` and Rooms label | Studio label + live-audio visual | existing Creator destinations |
| Creator Dashboard | generic overview/empty presentation | honest Rooms/Audience/Earnings low-data states + creator quick actions | backend metrics only |
| Creator Live Studio | create/go-live controls exposed without reliable Host state | Host access checked first; approved-only hosting controls | existing `hostAccess()` authority |
| Creator Profile | primary Profile tab opened an edit form | profile overview first; explicit Edit Profile state | existing profile update callback |
| Creator analytics | static boards contain illustrative trend lines | no fabricated time series; real metrics/unavailable state retained | backend analytics truth |
| Full product surface | video findings risked being treated as isolated patches | prior approved-board reconstruction for Auth/Discovery/Engagement/Live/Economy/Profile/Settings/Creator/Hosting retained and re-audited | 91-screen implementation inventory |

## Explicit non-goals
- No fake balances, followers, rooms, ranks, analytics history or Host status are introduced to make the running app resemble sample data in a board.
- No new stream keys, provider secrets or privileged Host authority are exposed client-side.
- No payment/entitlement state is granted locally.
