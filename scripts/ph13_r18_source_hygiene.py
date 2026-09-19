from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
GENERATED_DIRS={'.git','.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__'}
GENERATED_FILES={'local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties','.DS_Store'}
GENERATED_SUFFIXES={'.apk','.aab','.pyc','.pyo','.iml','.hprof'}
ROOM_PREFIX='core/database/schemas/app.voicecloud.core.database.VoiceCloudDatabase/'
TEXT_SUFFIXES={'.kt','.kts','.java','.xml','.json','.md','.txt','.properties','.toml','.yaml','.yml','.py','.ps1','.cmd','.bat','.csv'}
failures=[];ignored_room=[];checked=0
for p in ROOT.rglob('*'):
 if not p.is_file(): continue
 relp=p.relative_to(ROOT); rel=relp.as_posix()
 if any(part in GENERATED_DIRS for part in relp.parts): continue
 if p.name in GENERATED_FILES or p.suffix.lower() in GENERATED_SUFFIXES: continue
 if rel.startswith(ROOM_PREFIX) and rel.endswith('.json'): ignored_room.append(rel); continue
 if p.suffix.lower() not in TEXT_SUFFIXES and p.name!='gradle.properties': continue
 try: data=p.read_bytes()
 except OSError as e: failures.append(f'unreadable text file {rel}: {e}'); continue
 if b'\x00' in data: continue
 checked+=1
 try: text=data.decode('utf-8')
 except UnicodeDecodeError: continue
 if text and not text.endswith('\n'): failures.append(f'missing final newline: {rel}')
 for idx,line in enumerate(text.splitlines(),1):
  if line.rstrip(' \t') != line:
   failures.append(f'trailing whitespace: {rel}:{idx}')
   if len(failures)>50: break
 if len(failures)>50: break
if failures:
 for f in failures: print('[FAIL]',f)
 raise SystemExit(1)
print(f'[PASS] R18 source hygiene checked {checked} authored text files')
if ignored_room: print(f'[PASS] Ignored {len(ignored_room)} exact Room-generated schema JSON artifact(s); generated formatter output is not authored source')
print('[PASS] R18 authored source has final newlines and no trailing whitespace')
