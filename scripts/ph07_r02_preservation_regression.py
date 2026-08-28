from pathlib import Path
import hashlib,sys
ROOT=Path(__file__).resolve().parents[1]
manifest=ROOT/'contracts/VC-ANDROID-PH07-PRESERVATION-MANIFEST.sha256'
count=0
for line in manifest.read_text().splitlines():
    expected, rel=line.split('  ',1); p=ROOT/rel
    if not p.exists(): print('[FAIL] missing preserved file',rel); sys.exit(1)
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=expected: print('[FAIL] preserved baseline changed',rel); sys.exit(1)
    count+=1
nav=(ROOT/'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt').read_text()
discovery=(ROOT/'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt').read_text()
for name,ok in [
 ('PH06 host routes preserved',all(x in nav for x in ['HostStudio','HostRoomCreate','HostRoomManage','HostLiveConsole','HostInteractive'])),
 ('PH05 room routes preserved',all(x in nav for x in ['RoomPreview','RoomExperience'])),
 ('consumer profile remains present','fun MyProfileScreen(' in discovery),
 ('PH07 adds economy without replacing profile','Economy & progression' in discovery and 'onEconomy' in discovery),
]:
 if not ok: print('[FAIL]',name); sys.exit(1)
 print('[PASS]',name)
print(f'[PASS] {count} untouched PH01-PH06 source files match the PH06-R03 baseline')
