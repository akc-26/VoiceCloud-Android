from pathlib import Path
import re, subprocess, sys
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def ck(label, cond, detail=''):
    checks.append(bool(cond)); print(('[PASS] ' if cond else '[FAIL] ')+label+(f' :: {detail}' if detail and not cond else ''))
def text(rel): return (ROOT/rel).read_text(encoding='utf-8',errors='ignore')

# Run the proven R08 whole-product compile-risk contract first.
proc=subprocess.run([sys.executable, str(ROOT/'scripts/ph13_r08_full_ui_compile_risk_regression.py')], cwd=ROOT)
ck('R08 full UI compile-risk authority remains green', proc.returncode==0)

visual=text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt')
premium=text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt')
required=['VoiceCloudGlossCard','VoiceCloudPictogram','VoiceCloudPosterArtwork','VoiceCloudPageHero','VoiceCloudMetricTile','VoiceCloudEmptyVisual','VoiceCloudAudioArtwork','VoiceCloudMiniChart']
for name in required: ck(f'R09 centralized visual primitive exists: {name}', re.search(rf'\bfun\s+{name}\s*\(',visual) is not None)
# Canvas overload safety: every drawArc in new visual source must use explicit named style.
arc_blocks=[]
for m in re.finditer(r'drawArc\s*\(',visual):
    start=m.start(); i=m.end(); depth=1
    while i<len(visual) and depth:
        if visual[i]=='(': depth+=1
        elif visual[i]==')': depth-=1
        i+=1
    arc_blocks.append(visual[start:i])
ck('all R09 drawArc calls use explicit color=', all('color =' in b for b in arc_blocks) and bool(arc_blocks))
ck('all R09 drawArc calls use explicit style=', all('style =' in b for b in arc_blocks) and bool(arc_blocks))
ck('new poster canvas imports and uses supported fillMaxSize', 'import androidx.compose.foundation.layout.fillMaxSize' in visual and 'Canvas(Modifier.fillMaxSize())' in visual)
ck('R09 has no matchParentSize dependency', 'matchParentSize' not in visual+premium)

# Visual kind references must all resolve.
kind_body=re.search(r'enum class\s+VoiceCloudVisualKind\s*\{(.*?)\}',visual,re.S)
kinds=set(re.findall(r'\b([A-Z][A-Z0-9_]*)\b',kind_body.group(1))) if kind_body else set()
refs=set()
for p in list(ROOT.glob('feature/**/ui/*Screens.kt'))+list((ROOT/'core/designsystem/src/main/java').rglob('*.kt')):
    refs.update(re.findall(r'VoiceCloudVisualKind\.(\w+)',p.read_text(encoding='utf-8',errors='ignore')))
ck('all VoiceCloudVisualKind references resolve', refs <= kinds, ', '.join(sorted(refs-kinds)))

# No direct literal Text calls: keep centralized Title Case policy intact.
direct=[]
for root in [ROOT/'feature', ROOT/'app/src/main/java', ROOT/'core/designsystem/src/main/java']:
    for p in root.rglob('*.kt'):
        if re.search(r'\bText\(\s*"',p.read_text(encoding='utf-8',errors='ignore')): direct.append(p.relative_to(ROOT).as_posix())
ck('no direct user-visible Compose Text literals introduced', not direct, ', '.join(direct))

# Screen-group coverage: each product area must contain a rich wrapper plus pictorial/gloss primitives.
coverage={
 'auth': ('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',['AuthPage(', 'VoiceCloudPictogram(', 'VoiceCloudGlossCard(']),
 'discovery': ('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',['VoiceCloudPosterArtwork(', 'VoiceCloudAudioArtwork(', 'VoiceCloudPageHero(', 'VoiceCloudGlossCard(']),
 'engagement': ('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',['VoiceCloudPageHero(', 'VoiceCloudGlossCard(', 'VoiceCloudEmptyVisual(']),
 'live': ('feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',['VoiceCloudAudioArtwork(', 'VoiceCloudAnimatedWaveform(', 'VoiceCloudSpeakingAvatar(', 'VoiceCloudLiveBadge(']),
 'hosting': ('feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',['HostPage(', 'VoiceCloudPageHero(', 'VoiceCloudGlossCard(', 'VoiceCloudPictogram(']),
 'economy': ('feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',['VoiceCloudPageHero(', 'VoiceCloudGlossCard(', 'VoiceCloudPictogram(', 'VoiceCloudEmptyVisual(']),
 'profile': ('feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',['ProfilePage(', 'VoiceCloudPageHero(', 'VoiceCloudGlossCard(', 'VoiceCloudEmptyVisual(']),
 'settings': ('feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',['VoiceCloudPageHero(', 'VoiceCloudGlossCard(', 'VoiceCloudPictogram(']),
 'creator': ('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',['CreatorPortalFrame(', 'VoiceCloudPageHero(', 'VoiceCloudGlossCard(', 'VoiceCloudMetricTile(', 'VoiceCloudMiniChart(']),
}
for area,(rel,tokens) in coverage.items():
    body=text(rel); ck(f'{area} page group uses full glossy/pictorial visual layer', all(tok in body for tok in tokens), ', '.join(tok for tok in tokens if tok not in body))

# Page-by-page coverage: every named feature screen must enter a premium/rich shell.
visual_shell_markers = (
    'AuthPage(', 'ProfilePage(', 'HostPage(', 'CreatorPortalFrame(',
    'VoiceCloudPageTopBar(', 'VoiceCloudPageHero(', 'VoiceCloudGlossCard(',
    'VoiceCloudPosterArtwork(', 'VoiceCloudAudioArtwork(', 'VoiceCloudHeroCard(', 'VoiceCloudPremiumBackdrop(',
    'CreatorPhase13Page(', 'SecondaryPageLayout(', 'Scaffold(',
)
feature_screen_count = 0
unstyled_screens = []
for p in sorted(ROOT.glob('feature/**/ui/*Screens.kt')):
    body = p.read_text(encoding='utf-8', errors='ignore')
    for match in re.finditer(r'^fun\s+(\w+Screen)\s*\(', body, re.M):
        feature_screen_count += 1
        name = match.group(1)
        brace = body.find('{', match.start())
        depth = 0
        i = brace
        while i < len(body):
            if body[i] == '{': depth += 1
            elif body[i] == '}':
                depth -= 1
                if depth == 0:
                    i += 1
                    break
            i += 1
        block = body[match.start():i]
        if name == 'AuthGateScreen':
            rich = all(token in block for token in ('VoiceCloudPremiumBackdrop(', 'VoiceCloudBrandMark(', 'VoiceCloudAnimatedWaveform('))
        else:
            rich = any(token in block for token in visual_shell_markers)
        if not rich:
            unstyled_screens.append(f'{p.relative_to(ROOT).as_posix()}::{name}')
ck('complete feature screen inventory remains present', feature_screen_count >= 87, str(feature_screen_count))
ck('every named End User and Creator/Host screen enters a premium visual shell', not unstyled_screens, ', '.join(unstyled_screens))

# Explicit route/screen inventory must remain intact from working R08 baseline.
parent=ROOT/'contracts/VC-ANDROID-PH13-R09-PH13-R08-PARENT-CANONICAL.sha256'
ck('R08 parent canonical manifest present', parent.is_file())
# no merge/conflict and rough structural closure on the complete R09 changed source surface.
changed=[
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt',
'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt',
'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt']
for rel in changed:
    b=text(rel)
    ck(f'no merge markers: {rel}', not any(x in b for x in ('<<<<<<<','=======','>>>>>>>')))
    ck(f'balanced braces: {rel}', b.count('{')==b.count('}'))

if not all(checks):
    raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R09 full UI compile-risk regression: {sum(checks)}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R09 full UI compile-risk regression: {len(checks)}/{len(checks)} PASS')
