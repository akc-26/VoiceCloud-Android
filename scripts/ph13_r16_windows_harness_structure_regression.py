from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
files=[ROOT/'scripts/VC-ANDROID-PH13-R16-ISOLATED-BUILD.ps1',ROOT/'scripts/VC-ANDROID-PH13-R16-SOURCE-DIAGNOSTIC.ps1']
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
 s=p.read_text(); c,ins=clean(s)
 for op,cl,name in [('(',')','parentheses'),('{','}','braces'),('[',']','brackets')]:
  good=c.count(op)==c.count(cl); print('[PASS]' if good else '[FAIL]',p.name,name,'balanced'); passes+=good; fail += [] if good else [name]
 good=ins is None; print('[PASS]' if good else '[FAIL]',p.name,'no unterminated quote'); passes+=good; fail += [] if good else ['quote']
cmd=(ROOT/'scripts/VC-ANDROID-PH13-R16-ACCEPTANCE.cmd').read_text()
good='EnableDelayedExpansion' in cmd and '!ERRORLEVEL!' in cmd and 'set "VC_ANDROID_RC=!ERRORLEVEL!"' in cmd
print('[PASS]' if good else '[FAIL]','R16 CMD delayed ERRORLEVEL capture'); passes+=good; fail += [] if good else ['cmd']
if fail: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R16 Windows harness structure: {passes}/{passes} PASS')
