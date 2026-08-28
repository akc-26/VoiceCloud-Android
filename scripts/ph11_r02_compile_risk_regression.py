from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
# All edited UI literals route through titlecase helper.
ui='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in (ROOT/'feature').rglob('*Screens.kt'))
ck('no direct literal Text calls remain after Title Case conversion', re.search(r'\bText\(\s*"',ui) is None)
# Media memory/bitmap risks
crop=read('feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileImageCropper.kt')
ck('cropper caps preview decode longest edge', 'longest > 4096' in crop)
ck('cropper never requires original source dimensions to match target', 'outputWidth' in crop and 'outputHeight' in crop and 'require(srcW >= 1f && srcH >= 1f)' in crop)
ck('crop bounds are clamped before Bitmap.createBitmap', 'coerceIn(0, max(0, bitmap.width - cropW.roundToInt()))' in crop and 'coerceAtMost(bitmap.width - left)' in crop)
ck('crop output uses explicit JPEG encoding', 'Bitmap.CompressFormat.JPEG' in crop and 'mimeType: String = "image/jpeg"' in crop)
# PH11 list/identity safety
hrepo=read('feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingRepository.kt')
ck('rooms and schedules are de-duplicated', 'distinctBy { it.id }' in hrepo)
ck('scheduled start prevents duplicate linked room creation when link exists', 'firstOrNull { it.scheduledRoomId == schedule.id }' in hrepo)
ck('invite search excludes self/guest and privileged admin roles', 'it.id != selfId' in hrepo and '!it.isGuest' in hrepo and 'setOf("USER", "CREATOR")' in hrepo)
# Error safety
for rel in ['feature/hosting/src/main/java/app/voicecloud/feature/hosting/data/HostingRepository.kt','feature/live/src/main/java/app/voicecloud/feature/live/data/LiveRoomRepository.kt','feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsViewModel.kt','feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt']:
    txt=read(rel); ck(f'technical error sanitization retained: {rel}', any(x in txt.lower() for x in ['sql','typeorm','http 400','tovoicecloudusermessage']))
# No known broken interpolation patterns introduced by Title Case rewrite
for rel in [str(p.relative_to(ROOT)) for p in (ROOT/'feature').rglob('*.kt') if '/ui/' in p.as_posix()] + ['app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt']:
    text=read(rel)
    ck(f'no obvious malformed Title Case interpolation: {rel}', 'ifBlank { ")' not in text and 'joinToString(")' not in text)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH11-R02 compile-risk regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH11-R02 compile-risk regression: {passed}/{len(checks)} PASS')
