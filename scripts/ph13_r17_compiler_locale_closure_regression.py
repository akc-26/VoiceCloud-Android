from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
PREMIUM = ROOT / 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt'
ACCEPT = ROOT / 'scripts/VC-ANDROID-PH13-R17-ACCEPTANCE.cmd'
DIAG = ROOT / 'scripts/VC-ANDROID-PH13-R17-SOURCE-DIAGNOSTIC.ps1'
R16_COMPILE = ROOT / 'scripts/ph13_r16_compile_surface_regression.py'
checks = []

def ck(cond, label):
    checks.append((bool(cond), label))
    print('[PASS]' if cond else '[FAIL]', label)

premium = PREMIUM.read_text(encoding='utf-8')
accept = ACCEPT.read_text(encoding='utf-8')
diag = DIAG.read_text(encoding='utf-8')
r16_compile = R16_COMPILE.read_text(encoding='utf-8')

# Exact real Kotlin compiler failure closure from the user's R16 compiler output.
ck('size: Dp = 58.dp' in premium, 'VoiceCloudSpeakingAvatar public size parameter remains API-compatible')
ck('val canvasSize = this.size' in premium, 'SpeakingAvatar Canvas captures DrawScope size explicitly')
ck('Offset(canvasSize.width / 2f, canvasSize.height * .36f)' in premium, 'SpeakingAvatar center uses DrawScope canvas width/height')
ck('canvasSize.minDimension * .19f' in premium, 'SpeakingAvatar head geometry uses DrawScope minDimension')
ck('Size(canvasSize.width * .68f, canvasSize.height * .48f)' in premium, 'SpeakingAvatar body arc uses DrawScope dimensions')
ck('Stroke(width = canvasSize.minDimension * .14f' in premium, 'SpeakingAvatar stroke uses DrawScope minDimension')

# Guard against recurrence: inside the SpeakingAvatar function, the Dp parameter named size
# must never be mistaken for DrawScope.size again.
start = premium.index('fun VoiceCloudSpeakingAvatar(')
end = premium.index('\n@Composable\nfun VoiceCloudShimmer', start)
speaking = premium[start:end]
canvas_pos = speaking.index('Canvas(Modifier.fillMaxSize().padding')
canvas_tail = speaking[canvas_pos:]
unqualified = re.findall(r'(?<![A-Za-z0-9_.])size\.(?:width|height|minDimension)\b', canvas_tail)
ck(not unqualified, 'SpeakingAvatar Canvas contains no unqualified size.width/height/minDimension shadowing')

# Windows Python 3.14/cp1252 failure closure.
ck('set "PYTHONUTF8=1"' in accept, 'R17 CMD forces Python UTF-8 mode before diagnostics and integrity')
ck('set "PYTHONIOENCODING=utf-8"' in accept, 'R17 CMD forces UTF-8 Python stdio')
ck("$env:PYTHONUTF8 = '1'" in diag, 'R17 PowerShell source diagnostic forces Python UTF-8 mode for every child regression')
ck("$env:PYTHONIOENCODING = 'utf-8'" in diag, 'R17 PowerShell source diagnostic forces UTF-8 stdio')

# The exact R16 checker that crashed must itself be locale-independent as retained evidence.
ck("read_text(encoding='utf-8')" in r16_compile, 'R16 compile-surface checker uses explicit UTF-8 reads after closure')
ck(".read_text()" not in r16_compile, 'R16 compile-surface checker has no implicit Path.read_text() locale dependency')

failed = [label for ok, label in checks if not ok]
if failed:
    raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R17 compiler/locale closure regression: {len(checks)}/{len(checks)} PASS')
