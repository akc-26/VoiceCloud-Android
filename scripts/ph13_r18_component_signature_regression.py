from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
fail=[];passes=0
def ck(cond,label):
 global passes
 if cond: passes+=1; print('[PASS]',label)
 else: fail.append(label); print('[FAIL]',label)

def skip_string(s,i):
 if s.startswith('"""',i):
  j=s.find('"""',i+3); return len(s) if j<0 else j+3
 q=s[i]; i+=1
 while i<len(s):
  if s[i]=='\\': i+=2; continue
  if s[i]==q: return i+1
  i+=1
 return i

def match_paren(s,start):
 d=1;i=start+1
 while i<len(s):
  if s.startswith('//',i):
   j=s.find('\n',i+2); i=len(s) if j<0 else j+1; continue
  if s.startswith('/*',i):
   j=s.find('*/',i+2); i=len(s) if j<0 else j+2; continue
  if s[i] in ('"',"'"): i=skip_string(s,i); continue
  if s[i]=='(': d+=1
  elif s[i]==')':
   d-=1
   if d==0: return i
  i+=1
 return -1

def split_top(s):
 out=[];start=0;stack=[];pairs={')':'(',']':'[','}':'{'};i=0
 while i<len(s):
  if s.startswith('//',i):
   j=s.find('\n',i+2); i=len(s) if j<0 else j; continue
  if s.startswith('/*',i):
   j=s.find('*/',i+2); i=len(s) if j<0 else j+2; continue
  if s[i] in ('"',"'"): i=skip_string(s,i); continue
  c=s[i]
  if c in '([{': stack.append(c)
  elif c in ')]}':
   if stack and stack[-1]==pairs[c]: stack.pop()
  elif c==',' and not stack: out.append(s[start:i]); start=i+1
  i+=1
 out.append(s[start:]); return out

kt=list(ROOT.rglob('*.kt')); funcs={}
for p in kt:
 s=p.read_text(encoding='utf-8')
 for m in re.finditer(r'\bfun\s+(VoiceCloud[A-Za-z0-9_]*)\s*\(',s):
  name=m.group(1); ps=s.find('(',m.start()); pe=match_paren(s,ps)
  if pe<0: continue
  params=set()
  for part in split_top(s[ps+1:pe]):
   mm=re.search(r'\b([A-Za-z_]\w*)\s*:',part)
   if mm: params.add(mm.group(1))
  funcs.setdefault(name,[]).append(params)
ck('VoiceCloudRemoteMedia' in funcs,'VoiceCloudRemoteMedia project signature discovered')
remote_allowed=set().union(*funcs.get('VoiceCloudRemoteMedia',[set()]))
ck('contentDescription' in remote_allowed and 'kind' in remote_allowed,'VoiceCloudRemoteMedia current parameter authority uses contentDescription + kind')
ck('label' not in remote_allowed and 'fallbackKind' not in remote_allowed,'VoiceCloudRemoteMedia legacy label/fallbackKind parameters are absent')
issues=[]; remote_calls=0
if funcs:
 pat=re.compile(r'\b('+'|'.join(sorted(map(re.escape,funcs),key=len,reverse=True))+r')\s*\(')
 for p in kt:
  s=p.read_text(encoding='utf-8')
  for m in pat.finditer(s):
   if re.search(r'fun\s*$',s[max(0,m.start()-8):m.start()]): continue
   name=m.group(1); ps=s.find('(',m.start()); pe=match_paren(s,ps)
   if pe<0: continue
   named=[]
   for part in split_top(s[ps+1:pe]):
    mm=re.match(r'\s*([A-Za-z_]\w*)\s*=\s*(?!=)',part,re.S)
    if mm: named.append(mm.group(1))
   if name=='VoiceCloudRemoteMedia': remote_calls+=1
   if named and not any(set(named)<=params for params in funcs[name]):
    allowed=set().union(*funcs[name]); bad=[x for x in named if x not in allowed]
    issues.append(f'{p.relative_to(ROOT).as_posix()}:{s.count(chr(10),0,m.start())+1} {name} invalid named args={bad}')
ck(not issues,'all project-declared VoiceCloud* named-argument callsites match current function signatures')
for item in issues: print('[DETAIL]',item)
ck(remote_calls>=10,f'VoiceCloudRemoteMedia callsite audit covered project usage ({remote_calls} calls)')
# Exact stale API tokens are forbidden only in the three R16 presentation modules that contained the defect.
for rel in [
 'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
 'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt']:
 s=(ROOT/rel).read_text(encoding='utf-8')
 # Inspect each RemoteMedia block to avoid confusing legitimate label= in Material fields/components.
 stale=[]; lines=s.splitlines()
 for i,line in enumerate(lines):
  if 'VoiceCloudRemoteMedia(' in line:
   block='\n'.join(lines[i:i+14])
   if 'fallbackKind =' in block or re.search(r'^\s*label\s*=',block,re.M): stale.append(i+1)
 ck(not stale,f'{rel} has no stale VoiceCloudRemoteMedia label/fallbackKind callsites')
# User-reported Economy hygiene closure.
econ=(ROOT/'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt').read_text(encoding='utf-8')
for imp in ['androidx.compose.foundation.clickable','androidx.compose.foundation.border','VoiceCloudPageTopBar','VoiceCloudBrandMark','VoiceCloudHeroCard','VoiceCloudPageHero','VoiceCloudGlossCard','VoiceCloudPosterArtwork','VoiceCloudPremiumCard','VoiceCloudApprovedMetricRow']:
 ck(imp not in econ,f'Economy stale unused import removed: {imp}')
ck('EconomySection.glyph()' not in econ,'Economy unused glyph helper removed')
if fail:
 print(f'[SUMMARY-FAIL] VC-ANDROID-PH13-R18 component-signature closure: {passes} passed; {len(fail)} failed')
 raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R18 component-signature closure: {passes}/{passes} PASS')
