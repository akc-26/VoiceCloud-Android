from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
def read(path): return (ROOT / path).read_text(encoding='utf-8')

auth = read('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt')
discovery = read('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
engagement = read('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt')
engagement_vm = read('feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementViewModel.kt')
live = read('feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt')
nav = read('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
topbar = read('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt')
metrics = read('core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudPageMetrics.kt')
ph05_ps = read('scripts/VC-ANDROID-PH05-R01-SOURCE-CHECK.ps1')
app_build = read('app/build.gradle.kts')

checks=[]
def ck(name, cond):
    checks.append((name, bool(cond)))

# Acceptance regression that triggered PH06-R01 failure.
ck('PH05 Windows checker uses defined screen variable', '$screen.Contains' in ph05_ps and '$screens.Contains' not in ph05_ps)
ck('PH06 phase version marker retained', 'versionName = "1.0.0-ph06"' in app_build)

# Portal selector and login refinement.
ck('portal selector uses concise premium titles', all(x in auth for x in ['"Choose your portal"','"User Portal"','"Creator Portal"']))
ck('portal selector uses short user copy', '"Listen · Join · Connect"' in auth)
ck('portal selector uses short creator copy', '"Host · Create · Grow"' in auth)
ck('portal selector uses dedicated user icon', 'R.drawable.vc_portal_user' in auth)
ck('portal selector uses dedicated creator icon', 'R.drawable.vc_portal_creator' in auth)
ck('old portal explanatory paragraph removed', 'never infers Creator access' not in auth and 'share one secure account authority' not in auth)
ck('user login uses password visibility control', 'R.drawable.vc_icon_visibility' in auth and 'passwordVisible' in auth and 'Show password' in auth)
ck('user login exposes Signup', 'TextAction("Signup", onRegister)' in auth)
ck('user login switches directly to Creator Portal', 'SecondaryButton("Creator Portal"' in auth and 'onBack = { open(VoiceCloudRoutes.CreatorSignIn) }' in nav)
ck('creator login switches directly to User Portal', 'SecondaryButton("User Portal"' in auth and 'onBack = { open(VoiceCloudRoutes.UserSignIn) }' in nav)
ck('creator login removes verbose review paragraph', 'Need Creator access? Applications are reviewed' not in auth)

# Home and navigation quality.
for icon in ['community','message','bell','host']:
    ck(f'Home has professional {icon} action icon', f'R.drawable.vc_icon_{icon}' in discovery)
ck('Home action cards keep icon and label on one row', 'private fun RowScope.HomeShortcut(label: String, iconRes: Int' in discovery and 'Row(' in discovery[discovery.index('private fun RowScope.HomeShortcut'):discovery.index('private fun StatusBlock')])
ck('Home hero copy is concise', '"Join the conversation"' in discovery and '"Live rooms, creators and communities."' in discovery)
ck('verbose old Home hero copy removed', 'Join live conversations and discover creators and people across' not in discovery)
for icon in ['home','explore','search','friends','profile']:
    ck(f'professional {icon} selected nav state retained', f'vc_nav_{icon}_selected' in discovery and (ROOT / f'core/designsystem/src/main/res/drawable/vc_nav_{icon}_selected.xml').exists())

# Shared adaptive chrome.
ck('top bar supports compact icon actions', 'fun VoiceCloudTopBarIconAction(' in topbar and 'actions: (@Composable RowScope.() -> Unit)?' in topbar)
ck('top bar title remains centered and single-line', '.align(Alignment.Center)' in topbar and 'maxLines = 1' in topbar)
ck('adaptive metrics retain compact phone breakpoint', 'widthDp < 360.dp' in metrics)
ck('adaptive metrics retain tablet breakpoint', 'widthDp < 600.dp' in metrics)
ck('discovery secondary pages consume adaptive padding', 'adaptivePagePadding()' in discovery)
ck('engagement secondary pages consume adaptive padding', 'adaptivePagePadding()' in engagement)
ck('Telegram-style directional tab motion retained', 'slideInHorizontally' in nav and 'slideOutHorizontally' in nav and 'fullWidth / 5' in nav)

# Communities flow.
communities_body = engagement[engagement.index('fun CommunitiesScreen('):engagement.index('fun CommunityDetailScreen(')]
ck('Communities top bar has search icon action', 'R.drawable.vc_icon_search' in communities_body and 'Search communities' in communities_body)
ck('Communities top bar has upcoming events icon action', 'R.drawable.vc_icon_calendar' in communities_body and 'Upcoming events' in communities_body)
ck('Communities body has no persistent search field', 'OutlinedTextField' not in communities_body)
ck('Communities body has no upcoming-events text link', 'Upcoming events →' not in communities_body)
ck('Communities main page loads unfiltered list', 'LaunchedEffect(Unit) { onLoad("") }' in communities_body)
ck('community quick actions use vectors instead of glyph placeholders', all(x in engagement for x in ['R.drawable.vc_icon_community','R.drawable.vc_icon_message','R.drawable.vc_icon_bell']) and '"◎"' not in engagement and '"◌"' not in engagement)
ck('community quick actions keep icon and text on same row', 'private fun RowScope.QuickAction(label: String, iconRes: Int' in engagement)
ck('create community has premium hero', '"Start a community"' in engagement and 'ConsumerBrushes.Hero' in engagement[engagement.index('fun CommunityEditorScreen('):engagement.index('fun CommunityMembersScreen(')])
ck('created community uses premium stats', 'CommunityStat(community.memberCount.toString(), "Members")' in engagement)
ck('create flow removes editor from back stack', 'popUpTo(VoiceCloudRoutes.CommunityCreate) { inclusive = true }' in nav)
ck('community search opens dedicated global-search destination', 'const val CommunitySearch = "search/communities"' in nav and 'onSearch = { open(VoiceCloudRoutes.CommunitySearch) }' in nav)

# Unified global search.
ck('global search has all core tabs', all(f'"{x}"' in discovery[discovery.index('fun SearchScreen('):discovery.index('fun PublicProfileScreen(')] for x in ['All','People','Creators','Rooms','Communities']))
ck('global search separates creator identities', 'search-creator:' in discovery and 'role?.uppercase() == "CREATOR"' in discovery)
ck('global search has community result namespace', 'search-community:' in discovery)
ck('global search invokes community backend search', 'engagementViewModel.loadCommunities(query)' in nav)
ck('community search opens Communities tab', 'initialTab = "Communities"' in nav)
ck('search tabs horizontally adapt on compact devices', '.horizontalScroll(rememberScrollState())' in discovery)

# Messages behavior.
messages_body = engagement[engagement.index('fun MessagesScreen('):engagement.index('fun ConversationScreen(')]
ck('Messages search is hidden until icon tap', 'searchVisible' in messages_body and 'Search conversations' in messages_body)
ck('Messages top bar uses search icon', 'R.drawable.vc_icon_search' in messages_body)
ck('Messages uses red delete icon', 'R.drawable.vc_icon_delete' in messages_body and 'tint = MaterialTheme.colorScheme.error' in messages_body)
ck('Messages single delete asks confirmation', 'AlertDialog(' in messages_body and 'Delete conversation?' in messages_body)
ck('Messages supports multi-select', 'selectionMode' in messages_body and 'Checkbox(' in messages_body and 'selected: Set' not in messages_body)
ck('Messages supports multi-delete ViewModel operation', 'fun deleteConversations(ids: Set<String>)' in engagement_vm)
ck('old Remove text action eliminated', 'Text("Remove")' not in messages_body)

# Live-room text density.
ck('verbose room access paragraph removed', 'Room access is checked securely when you join' not in live)
ck('live RTC status copy is concise', '-> "Connected"' in live and 'Connected as listener' not in live)
ck('live section labels are concise', all(x in live for x in ['SectionHeading("People"','SectionHeading("Chat"','SectionHeading("Gifts"']))

# Required shared icon resources.
for icon in ['search','calendar','community','message','bell','delete','check_all','host','visibility','visibility_off','portal_user','portal_creator']:
    ck(f'shared icon resource {icon} exists', (ROOT / f'core/designsystem/src/main/res/drawable/vc_{"portal_" if icon.startswith("portal_") else "icon_"}{icon[7:] if icon.startswith("portal_") else icon}.xml').exists() if icon.startswith('portal_') else (ROOT / f'core/designsystem/src/main/res/drawable/vc_icon_{icon}.xml').exists())

failed=[n for n,o in checks if not o]
for n,o in checks: print(f"[{'PASS' if o else 'FAIL'}] {n}")
if failed:
    print('\nFailed checks:')
    for n in failed: print(' -',n)
    raise SystemExit(1)
print(f'VC-ANDROID-PH06-R02 premium UI regression: {len(checks)}/{len(checks)} PASS')
