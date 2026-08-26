from pathlib import Path
root=Path(__file__).resolve().parents[1]
ps=(root/'scripts/VC-ANDROID-PH01-R02-SOURCE-CHECK.ps1').read_text(encoding='utf-8')
cmd=(root/'scripts/VC-ANDROID-PH01-R02-ACCEPTANCE.cmd').read_text(encoding='utf-8')
route=(root/'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt').read_text(encoding='utf-8')
checks=[]
def ok(label, cond):
    if not cond: raise SystemExit(f'[FAIL] {label}')
    checks.append(label); print(f'[PASS] {label}')
for state in ['Loading','Maintenance','ForceUpdate','Ready','Error']:
    ok(f'BootstrapRoute contains {state} authority state', f'BootstrapState.{state}' in route)
    ok(f'Windows source gate checks {state} explicitly', f"Pass 'bootstrap surfaces {state} state'" in ps)
ok('fragile aggregate PowerShell state pipeline removed', "Pass 'bootstrap surfaces all authority states'" not in ps)
ok('Windows acceptance invokes R02 source checker', 'VC-ANDROID-PH01-R02-SOURCE-CHECK.ps1' in cmd)
ok('Windows acceptance cross-checks Python authority when available', 'python scripts\\ph01_source_check.py' in cmd)
print(f'VC-ANDROID-PH01-R02 Windows acceptance regression: {len(checks)}/{len(checks)} PASS')
