from pathlib import Path
import re, struct
ROOT=Path(__file__).resolve().parents[1]
fail=[]; passes=0
def ok(cond,label):
 global passes
 if cond: passes+=1; print('[PASS]',label)
 else: fail.append(label); print('[FAIL]',label)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')
boards=['01-overall-design-system.png', '02-auth-onboarding.png', '03-discover-search-community.png', '04-listener-live-room.png', '05-economy-profile-settings.png', '06-creator-dashboard-growth.png', '07-creator-room-creation-scheduling.png', '08-host-controls-moderation.png', '09-creator-showcase.png']
for name in boards:
 p=ROOT/'docs/reference/approved-r16'/name
 ok(p.is_file(),f'approved board bundled: {name}')
 if p.is_file():
  try:
   data=p.read_bytes()[:24]
   width,height=struct.unpack('>II',data[16:24]) if data[:8]==b'\x89PNG\r\n\x1a\n' else (0,0)
   ok(width>=1400 and height>=1000,f'approved board retained at high resolution: {name}')
  except Exception: ok(False,f'approved board readable: {name}')
approved='core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudApproved.kt'
a=text(approved)
for symbol in ['VoiceCloudApprovedTopBar','VoiceCloudApprovedPrimaryButton','VoiceCloudApprovedTextField','VoiceCloudApprovedSearchBar','VoiceCloudApprovedChip','VoiceCloudApprovedCard','VoiceCloudApprovedFeaturedRoom','VoiceCloudApprovedRoomRow','VoiceCloudApprovedPersonRow','VoiceCloudApprovedMetricRow','VoiceCloudApprovedStatCard']:
 ok(('fun '+symbol) in a,f'board-derived component exists: {symbol}')
feedback=text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudFeedback.kt')
ok('Regex("\\\\s+|\\\\S+")' in feedback and 'return@joinToString token' in feedback,'central title formatter preserves whitespace tokens')
feature_files=list((ROOT/'feature').rglob('*Screens.kt'))+[ROOT/'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt']
combined='\n'.join(p.read_text(encoding='utf-8') for p in feature_files if p.is_file())
ok('VoiceCloudPageHero(' not in combined,'rejected generic PageHero composition is not used by feature screens')
checks={
'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt':['VoiceCloudApprovedTextField','VoiceCloudOtpDigitsField','vc_onboarding_conversation','acceptedTerms'],
'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt':['VoiceCloudApprovedSearchBar','VoiceCloudApprovedFeaturedRoom','VoiceCloudApprovedRoomRow','avatarUrl','coverUrl'],
'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt':['VoiceCloudApprovedCard','Community','Messages'],
'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt':['VoiceCloudLiveBadge','VoiceCloudSpeakingAvatar','StageRoster'],
'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt':['VoiceCloudApprovedCard','WALLET','VIP'],
'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt':['RemoteMedia','coverUrl','avatarUrl'],
'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt':['VoiceCloudApprovedCard','Notifications','Privacy'],
'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt':['VoiceCloudApprovedStatCard','CreatorAnalyticsScreen','CreatorEarningsScreen','CreatorPayoutsScreen'],
'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt':['HostLiveConsoleScreen','PollsQuizScreen','ScheduleEditorScreen','RoomEditorScreen'],
}
for rel,needles in checks.items():
 t=text(rel)
 for needle in needles: ok(needle in t,f'{Path(rel).parts[1]} approved composition retains {needle}')
creator=text('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt')
ok('VoiceCloudMiniChart(listOf(' not in creator,'creator analytics/earnings do not fabricate hard-coded time-series data')
ok('time-series analytics' in creator and 'time-series settlement data' in creator,'creator charts fail honest when backend time-series is unavailable')
assets=['vc_onboarding_conversation','vc_ref_home_mic','vc_ref_live_forest','vc_ref_wallet_spark','vc_ref_vip_crown','vc_ref_creator_mic','vc_ref_creator_wallet']
media=text('core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudMedia.kt')
ok('fallbackDrawable' in media,'remote backend media remains primary with board-derived fallback support')
for asset in assets: ok((ROOT/'core/designsystem/src/main/res/drawable-nodpi'/f'{asset}.png').is_file(),f'board-derived visual asset packaged: {asset}')
# 87 named screens authority from prior inventory still present by function declarations
count=0
for p in feature_files:
 if p.is_file(): count += len(re.findall(r'\bfun\s+[A-Za-z0-9_]+Screen\s*\(',p.read_text(encoding='utf-8')))
ok(count>=87,f'complete screen implementation inventory remains present ({count} Screen composables detected)')
if fail: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R16 approved-design fidelity regression: {passes}/{passes} PASS')
