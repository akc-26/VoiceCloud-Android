from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
profile=read('feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileImageCropper.kt')
profileScreens=read('feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt')
settings=read('feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt')
discovery=read('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
auth=read('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt')
econ=read('feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt')
# media crop/preview
ck('Profile Photo crop output is 1080x1080', 'AVATAR("Profile Photo", 1080, 1080)' in profile)
ck('Cover Photo crop output is 1920x720', 'COVER("Cover Photo", 1920, 720)' in profile)
ck('profile media supports zoom 1x-4x', 'valueRange = 1f..4f' in profile and 'effectiveZoom = zoom.coerceIn(1f, 4f)' in profile)
ck('profile media supports horizontal repositioning', 'Horizontal Position' in profile and 'horizontal.coerceIn(-1f, 1f)' in profile)
ck('profile media supports vertical repositioning', 'Vertical Position' in profile and 'vertical.coerceIn(-1f, 1f)' in profile)
ck('large originals are decoded down for safe preview rather than rejected', 'longest > 4096' in profile and 'decoder.setTargetSize' in profile)
ck('Edit Profile launches crop dialog for avatar', 'ProfileCropKind.AVATAR' in profileScreens and 'pendingAvatar' in profileScreens)
ck('Edit Profile launches crop dialog for cover', 'ProfileCropKind.COVER' in profileScreens and 'pendingCover' in profileScreens)
# Settings / Safety / Profile IA
settings_block=settings.split('fun SettingsOverviewScreen',1)[1].split('@Composable',1)[0]
for item in ['Notifications','Privacy','Voice & Appearance','Security & Devices']:
    ck(f'Settings overview retains settings entry {item}', item in settings_block)
for item in ['Help & Legal','Contact Support','About VoiceCloud','Safety Center']:
    ck(f'Settings overview removes non-settings entry {item}', item not in settings_block)
safety=settings.split('fun SafetyCenterScreen',1)[1].split('@Composable',1)[0]
ck('Safety Center does not duplicate Communities navigation', 'Communit' not in safety)
nav=read('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
ck('legacy duplicate Profile Help routes are removed', 'HelpPages' not in nav and 'me/help/{slug}' not in nav)
profile_hub=discovery.split('fun MyProfileScreen',1)[1].split('@Composable\nfun SocialListScreen',1)[0]
for item in ['Settings','Safety Center','Help & Legal','Contact Support','About VoiceCloud','Wallet & Rewards']:
    ck(f'Profile hub owns {item}', item in profile_hub)
ck('Profile hub is grouped rather than long economy link list', 'ProfileHubTile("Wallet & Rewards"' in profile_hub and 'ProfileHubTile("VIP"' not in profile_hub and 'ProfileHubTile("Store"' not in profile_hub)
# portal selector premium two-card design
portal=auth.split('fun PortalSelectorScreen',1)[1].split('@Composable',1)[0]
ck('Portal selector has focused welcome hero', 'Welcome To ${VoiceCloudBrand.name}' in portal and 'Choose Your Experience' in portal)
ck('Portal selector exposes two interactive portal cards', 'PortalChoice(' in portal and 'VoiceCloud' in portal and 'Creator Studio' in portal)
ck('Portal selector copy is concise', 'One Account' in portal and 'Seamless Switching' in portal)
# economy visual redesign
ck('Economy uses dedicated visual cards', 'fun EconomyVisualCard(' in econ and 'EconomyHubScreen' in econ)
ck('Economy uses compact visual hub tiles', 'EconomyHubTile' in econ or 'EconomyNavigationCard' in econ)
ck('Economy limits raw metadata to concise details', 'details.take(2)' in econ and 'conciseDetails()' in econ)
ck('Wallet retains real Add Credits action', 'Add Credits' in econ and 'onBuyWalletCredits' in econ)
ck('VIP retains real purchase action', 'Choose VIP' in econ and 'onBuyVip' in econ)
ck('Store retains purchase/equip actions', all(x in econ for x in ['Purchase','Equip','Unequip']))
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH11-R02 requested-corrections regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH11-R02 requested-corrections regression: {passed}/{len(checks)} PASS')
