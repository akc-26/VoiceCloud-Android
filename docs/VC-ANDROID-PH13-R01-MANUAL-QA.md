# VC-ANDROID-PH13-R01 Manual QA

Run `scripts\\VC-ANDROID-PH13-R01-ACCEPTANCE.cmd` first and do not Git-freeze on a failed mandatory gate.

## Creator Analytics / economy
- PH13-QA-001: Creator Dashboard exposes Analytics, Wallet, Earnings, Gifts, Payouts, Notifications and Host Verification without regressing PH12 Audience/Messages.
- PH13-QA-002: Analytics renders backend values and a truthful empty state when no analytics are returned.
- PH13-QA-003: Creator Wallet shows backend balances/summary/recent ledger; changing screens does not mutate balances locally.
- PH13-QA-004: Earnings is read-only and matches Creator Studio/backend for the same account.
- PH13-QA-005: Gifts shows only backend gift-history records and does not compute settlement client-side.

## Payout requests
- PH13-QA-006: Amount below 100 cannot be submitted by UI.
- PH13-QA-007: Submit a valid payout; it appears only after backend acknowledgement/refresh.
- PH13-QA-008: Verify BANK_TRANSFER/PAYPAL/STRIPE/CRYPTO selections send the exact backend enum.
- PH13-QA-009: Open payout detail and verify status/rejection reason matches backend/Admin state.
- PH13-QA-010: Double-tap/retry does not create invented local completion; backend idempotency remains authority.

## Notifications
- PH13-QA-011: Creator Notifications list/read/read-all/delete works through the existing notification authority.
- PH13-QA-012: Notification navigation never exposes Admin-only or prohibited stream-credential pages.

## Host verification
- PH13-QA-013: Eligibility and progression match backend Host state.
- PH13-QA-014: Eligible account can submit Host application; PENDING/APPROVED/REJECTED/SUSPENDED states remain backend-authoritative.
- PH13-QA-015: Government ID upload uses private endpoint and succeeds with an allowed image/PDF.
- PH13-QA-016: Verification selfie upload uses private profile-photo endpoint.
- PH13-QA-017: Supporting-document upload uses private documents endpoint.
- PH13-QA-018: Rejected asset can be replaced; old asset ID is linked to the new backend-returned asset ID.
- PH13-QA-019: Verification files/URLs are not exposed in public profile/media screens or logs.

## Prohibited Android surface
- PH13-QA-020: No Creator screen reveals stream key, RTMP URL, RTC provider secret, or credential regeneration control.

## Cross-phase
- PH13-QA-021: Re-run PH12 Audience/Followers/Messages/Plans/Subscribers.
- PH13-QA-022: Re-run PH11 Creator room lifecycle/schedule/live console.
- PH13-QA-023: Switch between Consumer and Creator portals with the same authenticated CREATOR account and verify session continuity.
