from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BRAND = ROOT / 'branding/voicecloud-brand.properties'
TEST = ROOT / 'app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt'

props = {}
for raw in BRAND.read_text(encoding='utf-8').splitlines():
    line = raw.strip()
    if not line or line.startswith('#') or '=' not in line:
        continue
    k, v = line.split('=', 1)
    props[k.strip()] = v.strip().upper()

test = TEST.read_text(encoding='utf-8')
checks = []
def ck(label, cond):
    checks.append(bool(cond)); print(('[PASS] ' if cond else '[FAIL] ') + label)

def argb_literal(hex_rgb: str) -> str:
    value = hex_rgb.lstrip('#')
    if len(value) == 6:
        value = 'FF' + value
    return f'0x{value}.toInt()'

ck('approved premium consumer text authority is #18312D', props.get('consumer.text') == '#18312D')
ck('approved premium consumer primary authority is #006C63', props.get('consumer.sapphire') == '#006C63')
ck('instrumentation text ARGB assertion matches current centralized branding authority',
   f'assertEquals({argb_literal(props["consumer.text"])}, ConsumerColors.Text.toArgb())' in test)
ck('instrumentation primary ARGB assertion matches current centralized branding authority',
   f'assertEquals({argb_literal(props["consumer.sapphire"])}, ConsumerColors.Sapphire.toArgb())' in test)
ck('legacy pre-redesign text ARGB assertion is absent', '0xFF10262E.toInt()' not in test)
ck('legacy pre-redesign sapphire ARGB assertion is absent', '0xFF0B7C86.toInt()' not in test)
ck('runtime palette conversion guard remains intact', 'val renderedArgb = palette.map { it.toArgb() }' in test)
ck('runtime palette size guard remains intact', 'assertEquals(palette.size, renderedArgb.size)' in test)
ck('package identity instrumentation remains intact', 'packageIdentityMatchesGeneratedApplicationId' in test)
ck('microphone permission instrumentation remains intact', 'ph06DeclaresMicrophonePermissionForExplicitHostPublishing' in test)
ck('exactly three foundation instrumentation tests remain', test.count('@Test') == 3)
if not all(checks):
    raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R13 instrumentation palette correction regression: {len(checks)}/{len(checks)} PASS')
