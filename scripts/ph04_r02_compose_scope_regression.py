from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
screens = (ROOT / 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt').read_text(encoding='utf-8')
checks = [
    ('QuickAction is a RowScope extension', 'private fun RowScope.QuickAction(' in screens),
    ('QuickAction no longer exists as an unscoped composable', 'private fun QuickAction(' not in screens),
    ('QuickAction retains equal-width Modifier.weight layout', 'Modifier.weight(1f)' in screens and 'clickable(onClick = onClick)' in screens),
    ('Quick actions remain hosted in a Row', 'fun Ph04QuickActions' in screens and 'Row(Modifier.fillMaxWidth()' in screens),
]
for name, ok in checks:
    if not ok:
        raise SystemExit(f'[FAIL] {name}')
    print(f'[PASS] {name}')
print(f'VC-ANDROID-PH04-R02 Compose scope regression: {len(checks)}/{len(checks)} PASS')
