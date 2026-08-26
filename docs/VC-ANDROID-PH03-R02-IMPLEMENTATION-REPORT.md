# VC-ANDROID-PH03-R02 Implementation Report

## Trigger
Physical-device runtime crash while scrolling Home after authenticated user login. Logcat reported `IllegalArgumentException: Key ... was already used` from Compose lazy-list measurement.

## Root cause
PH03-R01 rendered Home People and Home Creators inside the same `LazyColumn` with raw `user.id` keys. Creators are a valid subset of People, so the same UUID could occur in both sections and collide globally within the lazy list.

## Correction
- Every PH03 dynamic lazy-list key is section-qualified and includes an occurrence index.
- Home/Explore/Search/Social/Friends render boundaries deduplicate repeated DTO identities.
- Repository responses also deduplicate rooms, search rooms, friends, pending requests and friend suggestions.
- `ConsumerIdentityPolicy.filter()` now collapses duplicate identities while preserving the original discovery order.
- PH02 auth/bootstrap production code is untouched.

## Regression
`ph03_r02_lazy_list_key_regression.py` explicitly reproduces the reported condition where one UUID appears in both People and Creators and verifies the generated keys remain unique.

## Acceptance
Run `scripts\VC-ANDROID-PH03-R02-ACCEPTANCE.cmd` on the Windows Android workstation and then repeat authenticated Home scrolling on the physical device.
