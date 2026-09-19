from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
R16_ALLOWED = (ROOT / 'contracts/VC-ANDROID-PH13-R16-ALLOWED-PRESENTATION-CHANGES.txt').read_text(encoding='utf-8').splitlines()
KT = [ROOT / rel for rel in R16_ALLOWED if rel.endswith('.kt')]
# Include the foundation instrumented test because it is the final executable device gate.
KT.append(ROOT / 'app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt')
fail = []
passes = 0

def ck(cond, label):
    global passes
    if cond:
        passes += 1
        print('[PASS]', label)
    else:
        fail.append(label)
        print('[FAIL]', label)

def strip_strings_comments(s):
    s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
    s = re.sub(r'//.*', '', s)
    s = re.sub(r'""".*?"""', '""', s, flags=re.S)
    s = re.sub(r'"(?:\\.|[^"\\])*"', '""', s)
    return s

def has_import(imports, fq):
    if fq in imports:
        return True
    pkg = '.'.join(fq.split('.')[:-1]) + '.*'
    return pkg in imports

# Extensions that are ordinary top-level imports (RowScope/ColumnScope member extensions such as weight are intentionally omitted).
extension_imports = {
    '.fillMaxSize(': 'androidx.compose.foundation.layout.fillMaxSize',
    '.fillMaxWidth(': 'androidx.compose.foundation.layout.fillMaxWidth',
    '.fillMaxHeight(': 'androidx.compose.foundation.layout.fillMaxHeight',
    '.width(': 'androidx.compose.foundation.layout.width',
    '.height(': 'androidx.compose.foundation.layout.height',
    '.size(': 'androidx.compose.foundation.layout.size',
    '.padding(': 'androidx.compose.foundation.layout.padding',
    '.offset(': 'androidx.compose.foundation.layout.offset',
    '.aspectRatio(': 'androidx.compose.foundation.layout.aspectRatio',
    '.imePadding(': 'androidx.compose.foundation.layout.imePadding',
    '.navigationBarsPadding(': 'androidx.compose.foundation.layout.navigationBarsPadding',
    '.statusBarsPadding(': 'androidx.compose.foundation.layout.statusBarsPadding',
    '.clip(': 'androidx.compose.ui.draw.clip',
    '.shadow(': 'androidx.compose.ui.draw.shadow',
    '.alpha(': 'androidx.compose.ui.draw.alpha',
    '.rotate(': 'androidx.compose.ui.draw.rotate',
    '.scale(': 'androidx.compose.ui.draw.scale',
    '.drawBehind(': 'androidx.compose.ui.draw.drawBehind',
    '.drawWithContent(': 'androidx.compose.ui.draw.drawWithContent',
    '.graphicsLayer(': 'androidx.compose.ui.graphics.graphicsLayer',
    '.clickable(': 'androidx.compose.foundation.clickable',
    '.combinedClickable(': 'androidx.compose.foundation.combinedClickable',
    '.background(': 'androidx.compose.foundation.background',
    '.border(': 'androidx.compose.foundation.border',
    '.horizontalScroll(': 'androidx.compose.foundation.horizontalScroll',
    '.verticalScroll(': 'androidx.compose.foundation.verticalScroll',
}

seen = set()
for p in KT:
    rel = p.relative_to(ROOT).as_posix()
    if rel in seen:
        continue
    seen.add(rel)
    ck(p.is_file(), f'R18 audited Kotlin exists: {rel}')
    if not p.is_file():
        continue
    s = p.read_text(encoding='utf-8')
    clean = strip_strings_comments(s)
    ck('<<<<<<<' not in s and '>>>>>>>' not in s and '=======' not in s, f'no merge markers: {rel}')
    ck(clean.count('{') == clean.count('}'), f'balanced braces: {rel}')
    ck(clean.count('(') == clean.count(')'), f'balanced parentheses: {rel}')
    ck(clean.count('[') == clean.count(']'), f'balanced brackets: {rel}')
    imports = set(re.findall(r'^import\s+([\w.*]+)', s, flags=re.M))
    for token, imp in extension_imports.items():
        if token in clean:
            ck(has_import(imports, imp), f'{rel} resolves Compose extension import for {token}')

# Exact compiler failure class: Dp parameter `size` shadowing DrawScope.size inside Canvas.
# Scan all project Kotlin functions with a `size: Dp` parameter and reject unqualified size.width/height/minDimension.
for p in ROOT.rglob('*.kt'):
    s = p.read_text(encoding='utf-8')
    for match in re.finditer(r'fun\s+([A-Za-z0-9_]+)\s*\((?P<params>.*?)\)\s*\{', s, flags=re.S):
        if not re.search(r'\bsize\s*:\s*Dp\b', match.group('params')):
            continue
        brace_start = s.find('{', match.start())
        depth = 0
        end = None
        for idx in range(brace_start, len(s)):
            ch = s[idx]
            if ch == '{': depth += 1
            elif ch == '}':
                depth -= 1
                if depth == 0:
                    end = idx + 1
                    break
        body = s[brace_start:end] if end else s[brace_start:]
        if 'Canvas(' not in body:
            continue
        # `this.size` and aliases derived from it are safe; bare `size.width` resolves to the Dp parameter.
        bad = re.findall(r'(?<![A-Za-z0-9_.])size\.(?:width|height|minDimension)\b', strip_strings_comments(body))
        ck(not bad, f'{p.relative_to(ROOT).as_posix()}::{match.group(1)} has no DrawScope size shadowing')

# Drawable/resource references in the changed UI resolve.
refs = set()
for p in KT:
    if p.is_file():
        refs.update(re.findall(r'R\.drawable\.([A-Za-z0-9_]+)', p.read_text(encoding='utf-8')))
resources = set()
for base in (ROOT/'core/designsystem/src/main/res', ROOT/'app/src/main/res'):
    if base.exists():
        resources.update(p.stem for p in base.rglob('*') if p.is_file())
for r in sorted(refs):
    ck(r in resources, f'drawable resource resolves: {r}')

# R16/R18 known compiler and warning closure.
discovery = (ROOT/'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt').read_text(encoding='utf-8')
ck('Text(descriptor ?:' not in discovery, 'Discovery non-null descriptor avoids redundant Elvis compiler warning')
host = (ROOT/'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt').read_text(encoding='utf-8')
ck(re.search(r'(?<!\?)\bstage\.', host) is None, 'Hosting nullable stage has no unsafe direct dereference')
ck('import androidx.compose.ui.draw.clip' in host, 'Hosting clip compiler closure retained')
feedback = (ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt').read_text(encoding='utf-8')
ck('joinToString(separator = "")' in feedback and 'return@joinToString token' in feedback and 'isWhitespace()' in feedback, 'Title Case formatter preserves whitespace')
premium = (ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt').read_text(encoding='utf-8')
ck('val canvasSize = this.size' in premium, 'SpeakingAvatar compiler closure captures DrawScope size explicitly')
ck('Offset(size.width / 2f, size.height * .36f)' not in premium, 'stale SpeakingAvatar shadowed size.width expression removed')
ck('size.minDimension * .19f' not in premium[premium.index('fun VoiceCloudSpeakingAvatar'):premium.index('fun VoiceCloudShimmer')], 'stale SpeakingAvatar shadowed size.minDimension expression removed')

# New R16 callback signatures and navigation callsites agree.
auth = (ROOT/'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt').read_text(encoding='utf-8')
nav = (ROOT/'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt').read_text(encoding='utf-8')
creator = (ROOT/'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt').read_text(encoding='utf-8')
ck('onGuest: () -> Unit' in auth and 'onGuest = { authViewModel.guestLogin() }' in nav, 'PortalSelector guest callback signature matches navigation')
ck('onAnalytics: () -> Unit' in creator and 'onAnalytics = { open(VoiceCloudRoutes.CreatorAnalytics) }' in nav, 'Creator portal analytics callback signature matches navigation')

if fail:
    print(f'[SUMMARY-FAIL] VC-ANDROID-PH13-R18 compile-surface regression: {passes} passed; {len(fail)} failed')
    raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R18 compile-surface regression: {passes}/{passes} PASS')
