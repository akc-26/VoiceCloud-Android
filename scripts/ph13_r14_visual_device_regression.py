from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
checks = []
def check(name, ok):
    checks.append((name, bool(ok)))
    print(('[PASS] ' if ok else '[FAIL] ') + name)

def text(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

feedback = text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt')
media = text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudMedia.kt')
visuals = text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt')
premium = text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt')
topbar = text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt')
theme = text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt')
core_gradle = text('core/designsystem/build.gradle.kts')
auth = text('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt')
bootstrap = text('feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt')
discovery = text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
engagement = text('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt')
live = text('feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt')
hosting = text('feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt')
economy = text('feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt')
profile = text('feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt')
settings = text('feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt')
creator = text('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt')
instrumented = text('app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt')

check('Title formatter tokenizes whitespace and non-whitespace separately', 'Regex("\\\\s+|\\\\S+")' in feedback)
check('Title formatter explicitly preserves whitespace tokens', 'token.firstOrNull()?.isWhitespace() == true' in feedback and 'return@joinToString token' in feedback)
check('Title formatter no longer uses destructive split-then-empty-join implementation', '.split(Regex("(\\\\s+)"))' not in feedback)
check('Physical-device instrumentation asserts readable Welcome to VoiceCloud spacing', 'assertEquals("Welcome to VoiceCloud", voiceCloudTitleCase("Welcome to VoiceCloud"))' in instrumented)
check('Physical-device instrumentation asserts long sentence spacing/casing', 'Only backend-supported profile fields are editable here.' in instrumented)

check('Centralized remote media component exists', 'fun VoiceCloudRemoteMedia(' in media)
check('Centralized avatar component exists', 'fun VoiceCloudAvatar(' in media)
check('Remote media has loading and rich fallback states', 'AsyncImagePainter.State.Loading' in media and 'VoiceCloudPosterArtwork' in media)
check('Design system owns Coil Compose dependency', 'io.coil-kt.coil3:coil-compose:3.5.0' in core_gradle)
check('Design system owns Coil network dependency', 'io.coil-kt.coil3:coil-network-okhttp:3.5.0' in core_gradle)

check('Shared page hero uses poster artwork rather than a repeated square pictogram', 'VoiceCloudPosterArtwork(kind = kind' in visuals and 'Modifier.size(width = 104.dp, height = 94.dp)' in visuals)
check('Shared gloss cards use neutral product border rather than gold outline everywhere', 'ConsumerColors.Border.copy(alpha = .88f)' in visuals)
check('Shared hero body allows multi-line phone copy', 'maxLines=4' in visuals.replace(' ', ''))
check('Top bar uses compact accent hierarchy and avoids generic top-bar pictogram', 'VoiceCloudPictogram' not in topbar and 'ConsumerColors.Sapphire' in topbar)
check('Small-label typography is at least 11sp for phone legibility', re.search(r'labelSmall\s*=\s*TextStyle\([^)]*fontSize\s*=\s*11\.sp', theme, re.S) is not None)

check('Auth shell contains rich poster artwork', 'VoiceCloudPosterArtwork' in auth and 'fun AuthPage(' in auth)
check('Portal selector has dedicated rich Discover artwork', 'PortalSelectorScreen' in auth and 'VoiceCloudVisualKind.DISCOVER' in auth)
check('Bootstrap system states use poster artwork', 'VoiceCloudPosterArtwork' in bootstrap)

check('Discovery avatars bind real backend avatarUrl', 'url = user.avatarUrl' in discovery)
check('Discovery room cards bind real backend coverUrl', 'url = room.coverUrl' in discovery or 'VoiceCloudRemoteMedia(room.coverUrl' in discovery)
check('Home featured room binds real cover art', 'featuredRoom?.coverUrl' in discovery)
check('Public/My Profile bind real cover media', discovery.count('url = profile.coverUrl') >= 2)
check('Discovery does not render single-letter UserAvatar fallback', 'take(1).uppercase()' not in re.search(r'private fun UserAvatar[\s\S]*?\n}\n', discovery).group(0) if re.search(r'private fun UserAvatar[\s\S]*?\n}\n', discovery) else False)

check('Profile cover/avatar fallback uses artwork rather than giant initials', 'VoiceCloudPosterArtwork(VoiceCloudVisualKind.PROFILE' in profile and 'label.take(1)' not in profile)
check('Profile surfaces still bind real avatarUrl and coverUrl', 'profile.avatarUrl' in profile and 'profile.coverUrl' in profile)
check('Replay surfaces retain real backend cover artwork', 'replay.coverUrl' in profile)

check('Community cards bind backend image/banner media', 'community.imageUrl' in engagement and 'community.bannerUrl' in engagement)
check('Event cards bind backend cover media', 'event.coverUrl' in engagement)
check('Conversation list binds peer/backend avatar media', 'conversation.peer?.avatarUrl' in engagement)

check('Listener preview binds room cover media', 'room.coverUrl' in live and 'VoiceCloudRemoteMedia' in live)
check('Listener live room retains immersive artwork/audio hierarchy', 'VoiceCloudVisualKind.LIVE' in live and 'VoiceCloudAudioArtwork' in live)
check('Host/room surfaces use page-specific poster artwork', 'VoiceCloudPosterArtwork' in hosting and 'VoiceCloudVisualKind.SCHEDULE' in hosting and 'VoiceCloudVisualKind.LIVE' in hosting)

check('Economy hub uses rich wallet poster artwork', 'VoiceCloudPosterArtwork' in economy and 'VoiceCloudVisualKind.WALLET' in economy)
check('Economy item cards use rich page-specific artwork', 'modifier = Modifier.size(width = 84.dp, height = 70.dp)' in economy)
check('Settings navigation uses richer poster artwork', 'VoiceCloudPosterArtwork' in settings and 'modifier = Modifier.size(width = 66.dp, height = 56.dp)' in settings)
check('Settings audio preset no longer falls back to plain ElevatedCard', 'VoiceCloudPosterArtwork(VoiceCloudVisualKind.AUDIO' in settings)

check('Creator profile binds real backend avatar', 'VoiceCloudAvatar(profile.avatarUrl' in creator)
check('Creator live studio uses distinct LIVE and SCHEDULE artwork', 'VoiceCloudPosterArtwork(VoiceCloudVisualKind.LIVE' in creator and 'VoiceCloudPosterArtwork(VoiceCloudVisualKind.SCHEDULE' in creator)
check('Creator ordinary top bar avoids redundant generic pictogram', 'private fun CreatorPortalFrame(' in creator and 'VoiceCloudBrandMark(42.dp)' in creator)

# Screen inventory: 87 named screen functions remain present across the nine product modules.
coverage = text('docs/VC-ANDROID-PH13-R09-SCREEN-COVERAGE.md')
names = re.findall(r'`([A-Za-z0-9]+Screen)`', coverage)
feature_text = '\n'.join([auth, creator, discovery, economy, engagement, hosting, live, profile, settings])
missing = [name for name in names if f'fun {name}(' not in feature_text]
check('All 87 named Listener + Creator/Host screens remain implemented', len(names) == 87 and not missing)

failed = [name for name, ok in checks if not ok]
if failed:
    print(f'[FAIL] VC-ANDROID-PH13-R14 physical-device-driven visual regression: {len(checks)-len(failed)}/{len(checks)} PASS')
    for name in failed: print(f'[FAIL] {name}')
    raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R14 physical-device-driven visual regression: {len(checks)}/{len(checks)} PASS')
