from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
FILES=[
'app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudMedia.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt',
'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt',
'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt',
'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
]
checks=[]
def ck(name,ok):
    checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)

def strip_kotlin(src:str)->str:
    out=[]; i=0; n=len(src); mode='code'; quote=None
    while i<n:
        if mode=='code':
            if src.startswith('//',i): mode='line'; out.extend('  '); i+=2; continue
            if src.startswith('/*',i): mode='block'; out.extend('  '); i+=2; continue
            if src.startswith('"""',i): mode='triple'; out.extend('   '); i+=3; continue
            if src[i] in ('"',"'"): quote=src[i]; mode='string'; out.append(' '); i+=1; continue
            out.append(src[i]); i+=1; continue
        if mode=='line':
            if src[i]=='\n': mode='code'; out.append('\n')
            else: out.append(' ')
            i+=1; continue
        if mode=='block':
            if src.startswith('*/',i): mode='code'; out.extend('  '); i+=2
            else: out.append('\n' if src[i]=='\n' else ' '); i+=1
            continue
        if mode=='triple':
            if src.startswith('"""',i): mode='code'; out.extend('   '); i+=3
            else: out.append('\n' if src[i]=='\n' else ' '); i+=1
            continue
        if mode=='string':
            if src[i]=='\\': out.append(' '); i+=1; out.append(' ' if i<n else ''); i+=1; continue
            if src[i]==quote: mode='code'; quote=None; out.append(' '); i+=1; continue
            out.append('\n' if src[i]=='\n' else ' '); i+=1; continue
    return ''.join(out)

def balanced(src, opener, closer):
    level=0
    for ch in strip_kotlin(src):
        if ch==opener: level+=1
        elif ch==closer:
            level-=1
            if level<0:return False
    return level==0

for rel in FILES:
    p=ROOT/rel
    ck(f'R14 Kotlin file exists: {rel}', p.is_file())
    if not p.is_file(): continue
    s=p.read_text(encoding='utf-8')
    ck(f'No merge markers: {rel}', not any(x in s for x in ('<<<<<<<','=======','>>>>>>>')))
    ck(f'Balanced braces: {rel}', balanced(s,'{','}'))
    ck(f'Balanced parentheses: {rel}', balanced(s,'(',')'))

media=(ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudMedia.kt').read_text()
ck('VoiceCloudMedia imports Coil SubcomposeAsyncImage APIs', all(x in media for x in ('coil3.compose.AsyncImagePainter','coil3.compose.SubcomposeAsyncImage','coil3.compose.SubcomposeAsyncImageContent')))
ck('VoiceCloudMedia collects painter state with Compose runtime import', 'import androidx.compose.runtime.collectAsState' in media and 'painter.state.collectAsState()' in media)
ck('VoiceCloudMedia has required ContentScale import', 'import androidx.compose.ui.layout.ContentScale' in media)
ck('Deprecated quadraticBezierTo removed from R14 premium visuals', 'quadraticBezierTo' not in (ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt').read_text())
ck('R14 core design system declares Coil runtime dependencies', 'coil-compose:3.5.0' in (ROOT/'core/designsystem/build.gradle.kts').read_text() and 'coil-network-okhttp:3.5.0' in (ROOT/'core/designsystem/build.gradle.kts').read_text())

failed=len(checks)-sum(checks)
if failed: raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R14 Kotlin compile-risk regression: {sum(checks)}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R14 Kotlin compile-risk regression: {len(checks)}/{len(checks)} PASS')
