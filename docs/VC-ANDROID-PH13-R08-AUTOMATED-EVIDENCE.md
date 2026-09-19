# VC-ANDROID-PH13-R08 Automated Evidence

## Exact R07 compiler failure closed
- `AuthScreens.kt:80:40 Unresolved reference 'loading'`
- `AuthUiState` authority property: `busy`
- R08 implementation: `active = state.busy`
- Regression explicitly rejects `state.loading` in Auth UI.

## New R08 source gates
- `ph13_r08_acceptance_wiring_regression.py`
- `ph13_r08_full_ui_compile_risk_regression.py`
- `ph13_r08_product_preservation_regression.py`

## Build gates retained
1. `:app:compileDebugKotlin --continue`
2. `:app:compileStagingKotlin --continue`
3. `:app:compileReleaseKotlin --continue`
4. Unit tests
5. Debug/Staging/Release lint
6. Debug/Staging/Release assemblies + debug androidTest assembly
7. Physical-device instrumentation when an authorized healthy device is available
8. Product/delivery integrity

Dependent gates remain fail-closed if mandatory compilation fails.

## Assistant-side source closure
- R08 full premium UI compile-risk audit: 42/42 PASS
- R08 acceptance wiring: 18/18 PASS
- R08 source/regression chain: 25/25 PASS
