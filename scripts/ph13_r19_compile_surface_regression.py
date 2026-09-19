from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
files=[
'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingViewModel.kt',
]
fail=[]; passes=0
def ck(cond,label):
 global passes
 if cond: passes+=1; print('[PASS]',label)
 else: fail.append(label); print('[FAIL]',label)
def balanced(s,a,b):
 d=0; i=0; quote=None
 while i<len(s):
  if quote:
   if s[i]=='\\': i+=2; continue
   if s.startswith(quote,i): i+=len(quote); quote=None; continue
   i+=1; continue
  if s.startswith('"""',i): quote='"""'; i+=3; continue
  if s[i] in "\"'": quote=s[i]; i+=1; continue
  if s.startswith('//',i):
   j=s.find('\n',i); i=len(s) if j<0 else j+1; continue
  if s.startswith('/*',i):
   j=s.find('*/',i+2); i=len(s) if j<0 else j+2; continue
  if s[i]==a: d+=1
  elif s[i]==b:
   d-=1
   if d<0: return False
  i+=1
 return d==0 and quote is None
texts={}
for rel in files:
 p=ROOT/rel; ck(p.is_file(),f'R19 Kotlin exists: {rel}')
 if not p.is_file(): continue
 s=p.read_text(encoding='utf-8'); texts[rel]=s
 ck('<<<<<<<' not in s and '>>>>>>>' not in s and '=======' not in s,f'no merge markers: {rel}')
 ck(balanced(s,'{','}'),f'balanced braces: {rel}')
 ck(balanced(s,'(',')'),f'balanced parentheses: {rel}')
 ck(balanced(s,'[',']'),f'balanced brackets: {rel}')

disc=texts.get(files[2],''); nav=texts.get(files[0],''); creator=texts.get(files[3],''); host=texts.get(files[4],'')
for fun in ['HomeScreen','ExploreScreen','SearchScreen','MyProfileScreen','FriendsScreen']:
 m=re.search(rf'fun\s+{fun}\s*\((.*?)\)\s*\{{',disc,re.S)
 ck(bool(m and 'onLive: () -> Unit' in m.group(1)),f'{fun} exposes center Live callback')
ck(nav.count('onLive = { open(VoiceCloudRoutes.Rooms) },') >= 5,'NavHost supplies Live callback to primary consumer screens')
m=re.search(r'fun\s+CreatorLiveStudioScreen\s*\((.*?)\)\s*\{',creator,re.S)
ck(bool(m and 'onVerification: () -> Unit' in m.group(1)),'CreatorLiveStudioScreen exposes Host verification callback')
ck('onVerification = { open(VoiceCloudRoutes.CreatorVerification) },' in nav,'NavHost Creator Live call matches new verification callback')
ck('repository.hostAccess()' in host,'HostingViewModel resolves existing Host authority API')
# Compose extension/import checks for changed presentation files.
extensions={'.clip(':'androidx.compose.ui.draw.clip','.background(':'androidx.compose.foundation.background','.clickable(':'androidx.compose.foundation.clickable','.horizontalScroll(':'androidx.compose.foundation.horizontalScroll'}
for rel in [files[1],files[2],files[3]]:
 s=texts.get(rel,'')
 for token,imp in extensions.items():
  if token in s: ck(imp in s or 'androidx.compose.foundation.layout.*' in s and token in {'.fillMaxWidth(','.fillMaxSize('},f'{rel} resolves import for {token}')
if fail:
 print(f'[SUMMARY-FAIL] VC-ANDROID-PH13-R19 compile surface: {passes} passed; {len(fail)} failed')
 raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R19 compile surface: {passes}/{passes} PASS')
