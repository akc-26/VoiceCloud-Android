from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
ALLOWED=(ROOT/'contracts/VC-ANDROID-PH13-R16-ALLOWED-PRESENTATION-CHANGES.txt').read_text(encoding='utf-8').splitlines()
kt=[ROOT/r for r in ALLOWED if r.endswith('.kt')]
fail=[];passes=0
def ok(c,l):
 global passes
 if c: passes+=1;print('[PASS]',l)
 else: fail.append(l);print('[FAIL]',l)

def strip_strings_comments(s):
 s=re.sub(r'/\*.*?\*/','',s,flags=re.S); s=re.sub(r'//.*','',s)
 s=re.sub(r'""".*?"""','""',s,flags=re.S); s=re.sub(r'"(?:\\.|[^"\\])*"','""',s)
 return s
for p in kt:
 rel=p.relative_to(ROOT).as_posix(); ok(p.is_file(),f'R16 Kotlin exists: {rel}')
 if not p.is_file(): continue
 s=p.read_text(encoding='utf-8'); clean=strip_strings_comments(s)
 ok('<<<<<<<' not in s and '>>>>>>>' not in s and '=======' not in s,f'no merge markers: {rel}')
 ok(clean.count('{')==clean.count('}'),f'balanced braces: {rel}')
 ok(clean.count('(')==clean.count(')'),f'balanced parentheses: {rel}')
 # Common Compose extension imports required when used outside fully-qualified form.
 imports=set(re.findall(r'^import\s+([\w.]+)',s,flags=re.M))
 checks={
  '.clip(':'androidx.compose.ui.draw.clip', '.shadow(':'androidx.compose.ui.draw.shadow',
  '.alpha(':'androidx.compose.ui.draw.alpha', '.graphicsLayer(':'androidx.compose.ui.graphics.graphicsLayer',
  '.horizontalScroll(':'androidx.compose.foundation.horizontalScroll', '.verticalScroll(':'androidx.compose.foundation.verticalScroll',
  '.clickable(':'androidx.compose.foundation.clickable', '.background(':'androidx.compose.foundation.background',
 }
 for token,imp in checks.items():
  if token in clean and imp not in imports and imp not in clean:
   ok(False,f'{rel} imports {imp} for {token}')
# drawable refs resolve
refs=set()
for p in kt:
 if p.is_file(): refs.update(re.findall(r'R\.drawable\.([A-Za-z0-9_]+)',p.read_text(encoding='utf-8')))
res=set(p.stem for p in (ROOT/'core/designsystem/src/main/res').rglob('*') if p.is_file())
res.update(p.stem for p in (ROOT/'app/src/main/res').rglob('*') if p.is_file())
for r in sorted(refs): ok(r in res,f'drawable resource resolves: {r}')

# Null-safety and warning closure retained across R16 UI reconstruction.
discovery=(ROOT/'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt').read_text(encoding='utf-8')
ok('Text(descriptor ?:' not in discovery,'Discovery non-null descriptor avoids redundant Elvis compiler warning')
host=(ROOT/'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt').read_text(encoding='utf-8')
ok(re.search(r'(?<!\?)\bstage\.',host) is None,'Hosting nullable stage has no unsafe direct dereference')
ok('stage?.participants.orEmpty().filter { p -> stage?.speakers.orEmpty().none' in host,'Hosting participant/speaker filter remains null-safe')
feedback=(ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt').read_text(encoding='utf-8')
ok('joinToString(separator = "")' in feedback and 'return@joinToString token' in feedback and 'isWhitespace()' in feedback,'Title Case formatter preserves whitespace after R16 reconstruction')

# known R15 compiler closure remains
host=(ROOT/'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt').read_text(encoding='utf-8')
ok('import androidx.compose.ui.draw.clip' in host,'Hosting clip compiler closure retained')
# explicit R16 auth surface imports
auth=(ROOT/'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt').read_text(encoding='utf-8')
ok('import androidx.compose.foundation.text.BasicTextField' in auth,'OTP BasicTextField import resolves')
ok('import androidx.compose.ui.graphics.SolidColor' in auth,'OTP SolidColor cursor import resolves')
ok('import androidx.compose.foundation.shape.CircleShape' in auth,'Auth CircleShape import resolves')
if fail: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R16 compile-surface regression: {passes}/{passes} PASS')
