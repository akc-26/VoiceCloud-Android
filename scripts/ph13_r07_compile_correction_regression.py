from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[1]
brand = ROOT / 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudBrandMark.kt'
premium = ROOT / 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt'
checks=[]
def check(label, cond):
    checks.append((label,bool(cond)))
    print(('[PASS] ' if cond else '[FAIL] ') + label)

b=brand.read_text(encoding='utf-8')
p=premium.read_text(encoding='utf-8')
check('Brand mark retains three cloud drawArc primitives', b.count('drawArc(') == 3)
check('All brand drawArc calls use named color arguments', b.count('color = teal') >= 3)
check('All brand drawArc calls use named startAngle arguments', b.count('startAngle =') == 3)
check('All brand drawArc calls use named sweepAngle arguments', b.count('sweepAngle =') == 3)
check('All brand drawArc calls use named useCenter arguments', b.count('useCenter = false') == 3)
check('All brand drawArc calls use named topLeft arguments', b.count('topLeft = Offset(') == 3)
check('All brand drawArc calls use named size arguments', b.count('size = Size(') == 3)
check('All brand drawArc calls pass Stroke through style=', b.count('style = cloudStroke') == 3)
check('Legacy positional drawArc overload misuse is absent', not re.search(r'drawArc\(\s*teal\s*,', b))
check('matchParentSize import is absent', 'import androidx.compose.foundation.layout.matchParentSize' not in p)
check('matchParentSize invocation is absent', '.matchParentSize()' not in p)
check('fillMaxSize import is present for speaking-ring canvas', 'import androidx.compose.foundation.layout.fillMaxSize' in p)
check('speaking-ring canvas uses fillMaxSize', 'Canvas(Modifier.fillMaxSize())' in p)
check('new premium Canvas lines use named strokeWidth/cap arguments', 'strokeWidth = barWidth, cap = StrokeCap.Round' in p)
# guard against the exact workstation compiler signatures from R06
check('no positional Stroke object can bind to drawArc alpha slot', not re.search(r'drawArc\([^\n]+cloudStroke\s*\)', b))

if not all(ok for _,ok in checks):
    raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R07 compile correction regression: {sum(ok for _,ok in checks)}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R07 compile correction regression: {len(checks)}/{len(checks)} PASS')
