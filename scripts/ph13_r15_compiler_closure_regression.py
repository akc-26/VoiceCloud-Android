from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
host=ROOT/'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt'
disc=ROOT/'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt'
files=[
 ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt',
 ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudMedia.kt',
 ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
 ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt',
 ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt',
 ROOT/'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',ROOT/'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt',
 ROOT/'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',ROOT/'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
 ROOT/'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',ROOT/'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
 ROOT/'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',ROOT/'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
 ROOT/'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',ROOT/'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt']
checks=[]
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
h=host.read_text(encoding='utf-8'); d=disc.read_text(encoding='utf-8')
ck('R14 compiler failure closure imports androidx.compose.ui.draw.clip in HostingScreens','import androidx.compose.ui.draw.clip' in h)
ck('all three reported HostingScreens clip callsites remain present',h.count('.clip(RoundedCornerShape(')>=3)
ck('R14 Discovery redundant non-null Elvis warning removed','Text(subtitle ?: ""' not in d and 'Text(subtitle, color = ConsumerColors.TextOnDarkSecondary' in d)
exts={'.clip(':'androidx.compose.ui.draw.clip','.shadow(':'androidx.compose.ui.draw.shadow','.alpha(':'androidx.compose.ui.draw.alpha','.rotate(':'androidx.compose.ui.draw.rotate','.blur(':'androidx.compose.ui.draw.blur','.graphicsLayer(':'androidx.compose.ui.graphics.graphicsLayer','.drawBehind(':'androidx.compose.ui.draw.drawBehind','.background(':'androidx.compose.foundation.background','.border(':'androidx.compose.foundation.border','.clickable(':'androidx.compose.foundation.clickable','.combinedClickable(':'androidx.compose.foundation.combinedClickable'}
issues=[]
for p in files:
    txt=p.read_text(encoding='utf-8'); imports={ln.removeprefix('import ').strip() for ln in txt.splitlines() if ln.startswith('import ')}
    for token,imp in exts.items():
        if token in txt and imp not in imports and not any(x.endswith('.*') and imp.startswith(x[:-1]) for x in imports): issues.append(f'{p.relative_to(ROOT)} uses {token} without {imp}')
ck('R14/R15 modified presentation files have required Compose extension imports',not issues)
if issues:
    for x in issues: print('[DETAIL]',x)
ck('deprecated quadraticBezierTo remains absent from premium visual source','quadraticBezierTo(' not in (ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt').read_text(encoding='utf-8'))
# Diff exactness: remove the only R15 import / warning-hygiene edits and compare to R14 content carried in package canonical hashes indirectly via preservation.
ck('Hosting clip import occurs exactly once',h.count('import androidx.compose.ui.draw.clip')==1)
failed=len(checks)-sum(checks)
if failed: raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R15 compiler closure regression: {sum(checks)}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R15 compiler closure regression: {len(checks)}/{len(checks)} PASS')
