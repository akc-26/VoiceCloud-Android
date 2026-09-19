from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
ui = (ROOT / 'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt').read_text(encoding='utf-8')
repo = (ROOT / 'feature/profile/src/main/java/app/voicecloud/feature/profile/data/ProfileRepository.kt').read_text(encoding='utf-8')
nav = (ROOT / 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt').read_text(encoding='utf-8')
models = (ROOT / 'feature/profile/src/main/java/app/voicecloud/feature/profile/model/ProfileModels.kt').read_text(encoding='utf-8')
all_profile = '\n'.join(p.read_text(encoding='utf-8') for p in (ROOT / 'feature/profile/src/main/java').rglob('*.kt'))

checks = {
    'Coil painter state is collected before branching': 'val mediaState by painter.state.collectAsState()' in ui,
    'Coil painter state is not compared directly': 'when (painter.state)' not in ui and 'painter.state is ' not in ui,
    'replay MediaPlayer construction is guarded': 'val playerResult = remember(url)' in ui and 'runCatching {' in ui and 'val player = playerResult.getOrNull()' in ui,
    'replay MediaPlayer has unavailable fallback': 'if (player == null)' in ui and 'Replay playback is unavailable.' in ui,
    'replay MediaPlayer is released': 'mediaPlayer.release()' in ui,
    'invalid String.ifBlank nullable regression absent': 'ifBlank { null }' not in all_profile,
    'nullable list fields remain backend resilient': 'val interests: List<String>? = null' in models and 'val customTags: List<String>? = null' in models,
    'profile mutation callbacks use explicit navigation lambdas': 'onSave = { body -> profileViewModel.saveProfile(body) }' in nav and 'onUnblock = { userId -> profileViewModel.unblock(userId) }' in nav,
    'repository does not grant or fabricate replay access': 'accessAllowed = true' not in repo and 'canAccess = true' not in repo,
    'technical IDs are not rendered as user labels': 'Text(person.id' not in ui and 'Text(entry.blockedUserId' not in ui and 'Text(visit.visitorUserId' not in ui,
}
for name, ok in checks.items():
    if not ok:
        print('[FAIL]', name)
        sys.exit(1)
    print('[PASS]', name)
print(f'[PASS] VC-ANDROID-PH08-R01 compile-risk regression: {len(checks)}/{len(checks)} PASS')
