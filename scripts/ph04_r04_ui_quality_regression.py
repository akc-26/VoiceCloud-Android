from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')

root = read('app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt')
topbar = read('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt')
colors = read('core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudColors.kt')
branding = read('branding/voicecloud-brand.properties')
theme = read('core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt')
discovery = read('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
engagement = read('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt')
settings = read('settings.gradle.kts')
nav = read('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
accept = read('scripts/VC-ANDROID-PH04-R04-ACCEPTANCE.cmd')
app_build = read('app/build.gradle.kts')

checks = []
def ck(name: str, condition: bool):
    checks.append((name, bool(condition)))

# System bars / shared page chrome.
ck('safe drawing insets are consumed once at app root', '.safeDrawingPadding()' in root)
ck('root keeps branded background behind system bars', '.background(MaterialTheme.colorScheme.background)' in root)
ck('app directly declares Compose Foundation for safe-area APIs', 'implementation(libs.androidx.compose.foundation)' in app_build)
ck('shared secondary top bar exists', 'fun VoiceCloudPageTopBar(' in topbar)
ck('shared top bar uses icon-only back control', 'IconButton(' in topbar and 'Canvas(Modifier.size(22.dp))' in topbar)
ck('shared top bar back control has accessibility label', 'contentDescription = "Back"' in topbar)
ck('shared top bar title is centered', '.align(Alignment.Center)' in topbar and 'textAlign = TextAlign.Center' in topbar)
ck('shared top bar title is single-line and ellipsized', 'maxLines = 1' in topbar and 'overflow = TextOverflow.Ellipsis' in topbar)
ck('shared top bar provides stable navigation-row height', '.heightIn(min = 64.dp)' in topbar)
ck('shared top bar has no visible Back text label', not re.search(r'Text\s*\(\s*"(?:‹\s*)?Back"', topbar))
ck('discovery secondary layout uses shared top bar', 'topBar = { VoiceCloudPageTopBar(' in discovery)
ck('engagement secondary layout uses shared top bar', 'VoiceCloudPageTopBar(' in engagement and 'topBar = {' in engagement)

for screen in ['RoomsScreen', 'PeopleScreen', 'PublicProfileScreen', 'SocialListScreen']:
    start = discovery.index(f'fun {screen}(')
    next_pos = discovery.find('\n@Composable', start + 10)
    body = discovery[start: next_pos if next_pos != -1 else len(discovery)]
    ck(f'discovery secondary screen {screen} uses shared layout', 'SecondaryPageLayout(' in body)

for screen in ['CommunitiesScreen','CommunityDetailScreen','CommunityEditorScreen','CommunityMembersScreen','EventsScreen','EventDetailScreen','MessagesScreen','ConversationScreen','NotificationsScreen']:
    start = engagement.index(f'fun {screen}(')
    next_pos = engagement.find('\n@Composable', start + 10)
    body = engagement[start: next_pos if next_pos != -1 else len(engagement)]
    ck(f'engagement secondary screen {screen} uses shared layout', 'SecondaryPageLayout(' in body)

ck('legacy engagement Header helper removed', 'private fun Header(' not in engagement and 'Header(' not in engagement)
ck('secondary UI contains no visible text Back control', not re.search(r'Text\s*\(\s*"(?:‹\s*)?Back"', discovery + engagement))

# Button / navigation text resilience.
ck('Home shortcut is RowScope qualified', 'private fun RowScope.HomeShortcut(' in discovery)
ck('Home shortcut keeps equal widths', 'Modifier.weight(1f).heightIn(min = 44.dp)' in discovery)
ck('Home shortcut labels are forced single-line', 'Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)' in discovery)
ck('Home shortcut labels include Communities Messages Alerts', all(f'HomeShortcut("{x}"' in discovery for x in ['Communities','Messages','Alerts']))
ck('PH04 QuickAction remains RowScope qualified', 'private fun RowScope.QuickAction(' in engagement)
ck('PH04 QuickAction has minimum touch/card height', '.heightIn(min = 76.dp)' in engagement)
ck('PH04 QuickAction labels are forced single-line', 'Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)' in engagement)
ck('community Members action is single-line', 'Text("Members", maxLines = 1, softWrap = false)' in engagement)
ck('community Rooms and events action is single-line', 'Text("Rooms & events", maxLines = 1, softWrap = false' in engagement)

# Website-derived consumer presentation authority. PH05 moves the exact values into the
# single white-label properties file; the Kotlin theme must consume generated BuildConfig aliases.
for prop, literal in [
    ('consumer.sapphire','#0B7C86'),('consumer.sapphireDeep','#075762'),('consumer.sapphireSoft','#DDF2F4'),
    ('consumer.ice','#22C7CF'),('consumer.cloud','#F1F7F8'),('consumer.surfaceSoft','#E8F4F5'),
    ('consumer.ink','#071820'),('consumer.text','#10262E'),('consumer.textMuted','#50666E'),('consumer.border','#A9CED2'),
]:
    ck(f'website premium color {literal} retained through branding authority', f'{prop}={literal}' in branding)
for prop, literal in [
    ('consumer.primaryGradientStart','#087E8A'),('consumer.primaryGradientEnd','#075F70'),
    ('consumer.heroGradientStart','#F5FCFD'),('consumer.heroGradientMiddle','#ECF9FA'),('consumer.heroGradientEnd','#DEF5F7'),
]:
    ck(f'website gradient color {literal} retained through branding authority', f'{prop}={literal}' in branding)
ck('consumer colors resolve through white-label BuildConfig aliases', 'VoiceCloudBrand.color(BuildConfig.CONSUMER_SAPPHIRE)' in colors and 'VoiceCloudBrand.color(BuildConfig.CONSUMER_HEROGRADIENTEND)' in colors)
ck('primary Website gradient is centralized', 'ConsumerColors.PrimaryGradientStart, ConsumerColors.PrimaryGradientEnd' in colors)
ck('light Website hero gradient is centralized', 'ConsumerColors.HeroGradientStart, ConsumerColors.HeroGradientMiddle, ConsumerColors.HeroGradientEnd' in colors)
ck('Home uses Website hero brush', 'background(\n                        ConsumerBrushes.Hero' in discovery)
ck('public profile uses Website hero brush', 'ConsumerBrushes.Hero' in discovery[discovery.index('fun PublicProfileScreen('):])
ck('communities uses Website hero brush', 'background(ConsumerBrushes.Hero)' in engagement)
ck('old teal-to-indigo hero construction is absent', 'SapphireDeep, ConsumerColors.Indigo' not in discovery + engagement)
for role in ['surfaceContainerLowest','surfaceContainerLow','surfaceContainer =','surfaceContainerHigh','surfaceContainerHighest','surfaceBright','surfaceDim','outlineVariant']:
    ck(f'Material3 role {role} is explicitly branded', role in theme)
ck('centralized Website radii map to Material shapes', all(x in branding for x in ['radius.small=12','radius.medium=18','radius.large=24','radius.extraLarge=30']) and 'BuildConfig.RADIUS_SMALL.dp' in theme and 'BuildConfig.RADIUS_MEDIUM.dp' in theme and 'BuildConfig.RADIUS_LARGE.dp' in theme and 'BuildConfig.RADIUS_EXTRA_LARGE.dp' in theme)

# Product-facing copy hygiene.
string_literals = re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"', discovery + engagement)
for forbidden in ['PH03','PH04','PH05','finalized API','finalized APIs','canonical club','consumer-visible','administrative excluded','authority']:
    ck(f'user-visible UI excludes internal term {forbidden}', all(forbidden.lower() not in s.lower() for s in string_literals))

# Prevent scope/compiler regressions and later-phase pull-forward.
ck('R02 QuickAction compile fix remains present', 'private fun RowScope.QuickAction(' in engagement and 'private fun QuickAction(' not in engagement)
ck('HomeShortcut weight has a RowScope receiver', 'private fun RowScope.HomeShortcut(' in discovery)
ck('new shared topbar align calls live in Box content', 'Box(' in topbar and '.align(Alignment.CenterStart)' in topbar and '.align(Alignment.CenterEnd)' in topbar)
ck('RTC feature remains outside PH04', 'include(":feature:rtc")' not in settings)
ck('live room detail routes remain outside PH04', all(x not in nav for x in ['LiveRoomDetail','RoomJoin','RtcRoom']))

# Acceptance must retain every known prior correction before build/device gates.
for token in ['VC-ANDROID-PH04-R01-SOURCE-CHECK.ps1','VC-ANDROID-PH04-R02-SOURCE-CHECK.ps1','VC-ANDROID-PH04-R03-SOURCE-CHECK.ps1','VC-ANDROID-PH04-R04-SOURCE-CHECK.ps1','ph04_r02_compose_scope_regression.py','ph04_r03_device_gate_regression.py','ph04_r04_ui_quality_regression.py','ph03_r02_lazy_list_key_regression.py']:
    ck(f'R04 acceptance retains {token}', token in accept)
ck('R04 acceptance retains all three Kotlin compile variants', all(x in accept for x in [':app:compileDebugKotlin',':app:compileStagingKotlin',':app:compileReleaseKotlin']))
ck('R04 acceptance retains tests lint assemblies and androidTest APK', all(x in accept for x in [' test ','lintDebug','lintStaging','lintRelease',':app:assembleDebug',':app:assembleStaging',':app:assembleRelease',':app:assembleDebugAndroidTest']))
ck('R04 acceptance retains healthy isolated device gate', 'VC-ANDROID-DEVICE-INSTRUMENTATION.ps1' in accept and ':app:connectedDebugAndroidTest' not in accept)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(f"[{'PASS' if ok else 'FAIL'}] {name}")
if failed:
    print('\nFailed checks:')
    for name in failed:
        print(' -', name)
    raise SystemExit(1)
print(f'VC-ANDROID-PH04-R04 UI-quality regression: {len(checks)}/{len(checks)} PASS')
