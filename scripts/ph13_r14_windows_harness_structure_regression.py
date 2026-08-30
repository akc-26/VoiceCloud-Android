from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
ps_files=[ROOT/'scripts/VC-ANDROID-PH13-R14-ISOLATED-BUILD.ps1',ROOT/'scripts/VC-ANDROID-PH13-R14-SOURCE-DIAGNOSTIC.ps1']
cmd=ROOT/'scripts/VC-ANDROID-PH13-R14-ACCEPTANCE.cmd'
checks=[]
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)

def ps_code(s:str):
    out=[];i=0;n=len(s);mode='code';quote=''
    while i<n:
        if mode=='code':
            if s[i]=='#': mode='comment';out.append(' ');i+=1;continue
            if s[i] in "'\"": quote=s[i];mode='string';out.append(' ');i+=1;continue
            out.append(s[i]);i+=1;continue
        if mode=='comment':
            if s[i]=='\n':mode='code';out.append('\n')
            else:out.append(' ')
            i+=1;continue
        if mode=='string':
            if quote=='"' and s[i]=='`' and i+1<n: out.extend('  ');i+=2;continue
            if s[i]==quote:
                # PowerShell single-quote escape is doubled single quote.
                if quote=="'" and i+1<n and s[i+1]=="'": out.extend('  ');i+=2;continue
                mode='code';quote='';out.append(' ');i+=1;continue
            out.append('\n' if s[i]=='\n' else ' ');i+=1;continue
    return ''.join(out),mode

def balanced(code,a,b):
    n=0
    for c in code:
        if c==a:n+=1
        elif c==b:
            n-=1
            if n<0:return False
    return n==0

for p in ps_files:
    s=p.read_text(encoding='utf-8'); code,mode=ps_code(s)
    ck(f'{p.name} has no unterminated quoted string',mode!='string')
    ck(f'{p.name} braces balanced',balanced(code,'{','}'))
    ck(f'{p.name} parentheses balanced',balanced(code,'(',')'))
    ck(f'{p.name} brackets balanced',balanced(code,'[',']'))

c=cmd.read_text(encoding='utf-8')
# Remove quoted spans and escaped parenthesis before balance check.
c_clean=re.sub(r'"[^"\r\n]*"','',c)
c_clean=c_clean.replace('^(','').replace('^)','')
ck('R14 CMD parenthesized control blocks are balanced',balanced(c_clean,'(',')'))
ck('R14 CMD uses delayed ERRORLEVEL capture', 'set "VC_ANDROID_RC=!ERRORLEVEL!"' in c)
ck('R14 CMD has no stale percent-expanded ERRORLEVEL capture', 'VC_ANDROID_RC=%ERRORLEVEL%' not in c)
ck('R14 full build exposes success/failure/pending exits', all(x in (ROOT/'scripts/VC-ANDROID-PH13-R14-ISOLATED-BUILD.ps1').read_text() for x in ('exit 0','exit 1','exit 2')))
ck('R14 acceptance exposes success/failure/pending exits', all(x in c for x in ('exit /b 0','exit /b 1','exit /b 2')))
failed=len(checks)-sum(checks)
if failed: raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R14 Windows harness structure: {sum(checks)}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R14 Windows harness structure: {len(checks)}/{len(checks)} PASS')
