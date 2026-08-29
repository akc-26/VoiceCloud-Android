from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
TEXT_SUFFIXES={'.kt','.kts','.py','.md','.txt','.cmd','.ps1','.properties','.toml','.xml','.json','.csv'}
trailing=[]; missing_eof=[]
for p in ROOT.rglob('*'):
    if not p.is_file() or any(part in {'.git','.gradle','.idea','build','__pycache__'} for part in p.relative_to(ROOT).parts): continue
    if p.suffix.lower() not in TEXT_SUFFIXES and p.name!='.gitignore': continue
    try: data=p.read_bytes(); text=data.decode('utf-8')
    except UnicodeDecodeError: continue
    for line_no,line in enumerate(text.splitlines(),1):
        if line.rstrip(' \t') != line: trailing.append(f'{p.relative_to(ROOT)}:{line_no}')
    if data and not data.endswith(b'\n'): missing_eof.append(str(p.relative_to(ROOT)))
if trailing: raise SystemExit('[FAIL] trailing whitespace: '+', '.join(trailing[:20]))
if missing_eof: raise SystemExit('[FAIL] missing final newline: '+', '.join(missing_eof[:20]))
print('[PASS] PH13 whitespace hygiene: no trailing whitespace and all text files end with a newline')
