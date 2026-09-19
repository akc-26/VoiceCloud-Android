from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
fail=[]; passed=0

def ck(cond,label):
    global passed
    if cond:
        passed += 1; print('[PASS]',label)
    else:
        fail.append(label); print('[FAIL]',label)

def txt(rel): return (ROOT/rel).read_text(encoding='utf-8')

discovery=txt('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
auth=txt('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt')
creator=txt('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt')
hostvm=txt('feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingViewModel.kt')
nav=txt('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')

ck('private fun ConsumerScaffold' in discovery and 'onLive: () -> Unit' in discovery, 'consumer bottom navigation owns a dedicated Live action')
ck('label = "Discover"' in discovery, 'consumer second bottom tab is Discover')
center=re.search(r'Surface\(\s*onClick = onLive,.*?VoiceCloudPictogram\(VoiceCloudVisualKind\.LIVE.*?\n\s*\}', discovery, re.S)
ck(center is not None, 'consumer center action is a Live/microphone action rather than Search')
ck('ConsumerScaffold("search"' not in discovery and 'ConsumerScaffold("explore", onHome, onExplore, onLive' in discovery, 'Search belongs to Discover context instead of becoming a bottom tab')
ck('Text("For You"' in discovery, 'Home has a distinct For You identity')
ck('VoiceCloudApprovedTopBar(title = "Discover", onSearch = onSearch)' in discovery, 'Discover has its own dedicated page identity and search affordance')
ck('Creators to discover' in discovery and 'Quick access' in discovery, 'Home remains useful in data-light states with creator and quick-access sections')
ck('profile.coverUrl' in discovery and 'contentDescription = "${profile.displayName.ifBlank { profile.username }} cover"' in discovery, 'End User profile binds backend cover media')
ck('VoiceCloudVisualKind.WALLET' in discovery and 'Balance, gifts & VIP' in discovery, 'wallet is a compact profile action rather than the dominant profile hero')
ck('PortalChoice("Speaker"' not in auth, 'Speaker is not exposed as a fake account portal')
ck('PortalChoice("Listener"' in auth and 'PortalChoice("Creator"' in auth, 'portal selector exposes Listener and Creator experiences')
ck('Build your audience, manage creator tools and grow your community.' in auth, 'Creator portal copy does not falsely imply automatic Host authority')
ck('CreatorBoardNavItem("Studio"' in creator, 'Creator navigation exposes Studio explicitly')
ck('Text("+"' not in creator[:6000], 'Creator center navigation no longer uses an unexplained plus action')
ck('VoiceCloudPictogram(VoiceCloudVisualKind.LIVE, size = 28.dp' in creator, 'Creator center action visibly represents live audio')
ck('Welcome back, ${creatorName.ifBlank { "Creator" }}' in creator, 'Creator dashboard uses role-neutral welcome copy instead of hard-coded time-of-day greeting')
ck('Creator insights are ready for real activity' in creator and 'VoiceCloudApprovedStatCard("—", "Rooms"' in creator, 'Creator dashboard has a designed honest no-metrics state')
ck('val hostApproved = state.hostProfile?.status.equals("APPROVED", true)' in creator, 'Creator Live Studio derives actual Host approval state')
ck('Host access required' in creator and 'Review Host Access' in creator, 'unapproved Creator receives Host-access guidance instead of room-creation controls')
ck('else if (hostApproved)' in creator and 'VoiceCloudApprovedPrimaryButton("Go Live"' in creator, 'Go Live and scheduling controls are inside approved-Host branch')
ck('val (profile, eligibility) = repository.hostAccess()' in hostvm, 'Creator Studio loads authoritative Host profile and eligibility')
ck('val rooms = if (approved) repository.rooms() else emptyList()' in hostvm, 'Creator Studio does not load Host rooms before approval')
ck('var editing by rememberSaveable(profile?.id)' in creator and 'if (!editing)' in creator, 'Creator Profile opens as an overview with explicit edit mode')
ck('VoiceCloudApprovedPrimaryButton("Edit Profile"' in creator, 'Creator Profile exposes a deliberate Edit Profile action')
ck(nav.count('onLive = { open(VoiceCloudRoutes.Rooms) },') >= 5, 'consumer center Live action routes to the live-room directory across primary surfaces')
ck('onVerification = { open(VoiceCloudRoutes.CreatorVerification) },' in nav, 'Creator Studio Host-access CTA routes to verification')

# Screen inventory floor: ensure the broad product remains present.
all_kt='\n'.join(p.read_text(encoding='utf-8') for p in (ROOT/'feature').rglob('*.kt'))
screens=set(re.findall(r'\bfun\s+([A-Za-z0-9_]+Screen)\s*\(', all_kt))
ck(len(screens) >= 87, f'complete Listener + Creator/Host screen inventory retained ({len(screens)} Screen composables)')

if fail:
    print(f'[SUMMARY-FAIL] VC-ANDROID-PH13-R19 video/product architecture regression: {passed} passed; {len(fail)} failed')
    raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R19 video/product architecture regression: {passed}/{passed} PASS')
