from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
files=[ROOT/'scripts/VC-ANDROID-PH13-R19-ISOLATED-BUILD.ps1',ROOT/'scripts/VC-ANDROID-PH13-R19-SOURCE-DIAGNOSTIC.ps1']
fail=[];passes=0
def clean(s):
 out=[];ins=None;esc=False
 for ch in s:
  if ins:
   if esc: esc=False; continue
   if ch=='`': esc=True; continue
   if ch==ins: ins=None
   continue
  if ch in "'\"": ins=ch; continue
  out.append(ch)
 return ''.join(out),ins
for p in files:
 s=p.read_text(encoding='utf-8'); c,ins=clean(s)
 for op,cl,name in [('(',')','parentheses'),('{','}','braces'),('[',']','brackets')]:
  good=c.count(op)==c.count(cl); print('[PASS]' if good else '[FAIL]',p.name,name,'balanced'); passes+=int(good); fail += [] if good else [name]
 good=ins is None; print('[PASS]' if good else '[FAIL]',p.name,'no unterminated quote'); passes+=int(good); fail += [] if good else ['quote']
cmd=(ROOT/'scripts/VC-ANDROID-PH13-R19-ACCEPTANCE.cmd').read_text(encoding='utf-8')
good='EnableDelayedExpansion' in cmd and '!ERRORLEVEL!' in cmd and 'set "VC_ANDROID_RC=!ERRORLEVEL!"' in cmd
print('[PASS]' if good else '[FAIL]','R19 CMD delayed ERRORLEVEL capture'); passes+=int(good); fail += [] if good else ['cmd']
good='set "PYTHONUTF8=1"' in cmd and 'set "PYTHONIOENCODING=utf-8"' in cmd
print('[PASS]' if good else '[FAIL]','R19 CMD locale-independent Python mode'); passes+=int(good); fail += [] if good else ['python-utf8']
if fail: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R19 Windows harness structure: {passes}/{passes} PASS')
