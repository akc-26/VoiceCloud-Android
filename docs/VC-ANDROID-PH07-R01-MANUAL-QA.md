# VC-ANDROID-PH07-R01 Manual QA
1. Open My Profile > Economy & progression and verify all ten PH07 sections load without fabricated placeholder success.
2. Wallet: verify live balance, transactions, packages, and purchase history reflect server state.
3. Coin top-up: complete a Play test purchase; confirm coins appear only after backend `/wallet/purchase/validate` succeeds. Re-open app and verify restore/idempotency does not double-credit.
4. VIP: verify Android VIP catalog, membership/history, purchase verification, cancellation/renewal state remain backend-authoritative.
5. Referrals: verify summary/history/rewards against the same account on web/admin.
6. Gifts: verify catalog/history are server data.
7. Store: purchase, equip and unequip an item; relaunch and confirm inventory/equipped state persists from backend.
8. Tasks/Achievements: claim eligible task rewards once; verify repeated claim cannot duplicate rewards.
9. XP/Check-in: claim daily check-in; verify XP/streak data and duplicate-claim prevention.
10. Rankings: verify users/creators/hosts/rooms/gift senders/gift receivers/VIP. Confirm there is no Agency ranking.
11. Tickets: buy a scheduled-room ticket and confirm it appears in My Tickets and access is server-authoritative.
12. Confirm paid creator subscription purchase UI is absent until BR-03 backend settlement authority is closed.
