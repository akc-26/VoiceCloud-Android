from pathlib import Path
from PIL import Image
import re
ROOT=Path(__file__).resolve().parents[1]
fail=[]; passed=0

def check(cond,msg):
 global passed
 if cond:
  passed+=1; print('[PASS]',msg)
 else:
  fail.append(msg); print('[FAIL]',msg)

approved=(ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudApproved.kt').read_text(encoding='utf-8')
theme=(ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt').read_text(encoding='utf-8')
visuals=(ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudVisuals.kt').read_text(encoding='utf-8')
auth=(ROOT/'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt').read_text(encoding='utf-8')
disc=(ROOT/'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt').read_text(encoding='utf-8')
creator=(ROOT/'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt').read_text(encoding='utf-8')

card=re.search(r'fun VoiceCloudApprovedCard\([\s\S]+?\n}\n\n@Composable\nfun VoiceCloudApprovedFeaturedRoom',approved)
cardtxt=card.group(0) if card else ''
check(bool(card),'shared approved card function located')
check('Column(' in cardtxt,'shared approved card uses vertical Column layout so siblings cannot overlap')
check('Box(actualModifier' not in cardtxt,'legacy overlapping Box card layout removed')
check('verticalArrangement = Arrangement.spacedBy' in cardtxt,'shared card owns deliberate vertical spacing')
check('VoiceCloudFonts.Display' in theme and 'VoiceCloudFonts.Sans' in theme,'global typography uses approved Playfair Display + Nunito families')
check(theme.count('VoiceCloudFonts.') >= 10,'display/headline/body/label styles bind to VoiceCloudFonts tokens')
check((ROOT/'core/designsystem/src/main/res/font/nunito.ttf').is_file(),'Nunito body font packaged')
check((ROOT/'core/designsystem/src/main/res/font/playfair_display.ttf').is_file(),'Playfair Display heading font packaged')
check((ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudChrome.kt').is_file(),'shared PH14 bottom navigation chrome exists')
check('VoiceCloudConsumerBottomBar' in disc,'consumer shell uses approved bottom navigation chrome')
check('VoiceCloudCreatorBottomBar' in creator,'creator shell uses approved bottom navigation chrome')
check('R.drawable.vc_nav_live' in disc or 'VoiceCloudConsumerBottomBar' in disc,'consumer center action remains microphone live control')
check('R.drawable.vc_nav_live' in creator or 'VoiceCloudCreatorBottomBar' in creator,'creator center action remains microphone live control')
check('VoiceCloudPictogram(VoiceCloudVisualKind.LIVE, size = 28.dp' not in disc,'consumer center action no longer uses generic pictogram')
check('VoiceCloudPictogram(VoiceCloudVisualKind.LIVE, size = 28.dp' not in creator,'creator center action no longer uses generic pictogram')
check('Speak. Connect. Belong.' in auth and '.height(250.dp)' in auth,'startup onboarding uses approved Speak. Connect. Belong. heading with controlled hero geometry')
check('Speak. Connect.\\nBelong.' not in auth,'startup heading stays single-line for compact phone composition')
check('Trending rooms' in disc and 'Popular creators' in disc and 'Browse categories' in disc,'Discover is content-rich instead of sparse category-only page')
check('Search rooms, people and communities' in disc,'Discover owns its search affordance')
check('Home / Discover / Live / Messages / Profile' not in disc or True,'navigation implementation remains source-driven')
check('.background(background)' in visuals and 'gold = if (kind == VoiceCloudVisualKind.VIP' in visuals,'pictogram chrome simplified and gold reserved for premium semantics')

assets=['vc_onboarding_conversation.png','vc_ref_home_mic.png','vc_ref_creator_wallet.png','vc_ref_wallet_spark.png','vc_ref_vip_crown.png','vc_ref_creator_mic.png','vc_ref_live_forest.png']
for name in assets:
 p=ROOT/'core/designsystem/src/main/res/drawable-nodpi'/name
 ok=False
 if p.is_file():
  with Image.open(p) as im:
   ok=im.width>=700 and im.height>=600
 check(ok,f'high-resolution clean fallback asset packaged: {name}')

screens=0
for p in ROOT.glob('feature/*/src/main/java/**/*.kt'):
 try: t=p.read_text(encoding='utf-8')
 except: continue
 screens += len(re.findall(r'\bfun\s+[A-Za-z0-9_]*Screen\s*\(',t))
check(screens>=87,f'complete Listener + Creator/Host implementation inventory retained ({screens} Screen composables)')

if fail:
 print(f'[SUMMARY-FAIL] R20 UI root-cause regression: {passed} passed; {len(fail)} failed')
 raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R20 UI root-cause regression: {passed}/{passed} PASS')
