from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def ck(label,ok):
    if not ok: raise SystemExit(f'[FAIL] {label}')
    checks.append(label); print(f'[PASS] {label}')
creator=(ROOT/'feature/creator/src/test/java/app/voicecloud/feature/creator/model/CreatorModelsTest.kt').read_text(encoding='utf-8')
settings=(ROOT/'feature/settings/src/test/java/app/voicecloud/feature/settings/model/SettingsModelsTest.kt').read_text(encoding='utf-8')
ck('Creator unit test expects PH11 LIVE portal section', 'listOf("DASHBOARD", "LIVE", "PROFILE", "SETTINGS", "HELP")' in creator)
ck('Creator unit test no longer forbids LIVE', 'it.contains("LIVE")' not in creator)
ck('Creator unit test still forbids financial/payout sections', 'it.contains("WALLET")' in creator and 'it.contains("PAYOUT")' in creator)
ck('Privacy reflection ignores JVM/Kotlin synthetic fields', '.filterNot { it.isSynthetic || it.name.startsWith("$") }' in settings)
ck('Contact reflection uses the same compiler-field filter', settings.count('.filterNot { it.isSynthetic || it.name.startsWith("$") }') >= 2)
ck('Privacy business-field contract remains exact', 'setOf("showOnlineStatus", "showLastSeen", "allowDirectMessages", "showGifts")' in settings)
ck('Contact business-field contract remains exact', 'setOf("name", "email", "phoneNumber", "message")' in settings)
ck('R02 unit-test correction does not weaken credential-field safety test', 'safe security projections contain no credential token fields' in settings and 'pushtoken' in settings)
print(f'[PASS] VC-ANDROID-PH11-R02 unit-contract regression: {len(checks)}/{len(checks)} PASS')
