from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
failures = []
passes = []

def ok(label, cond, detail=None):
    if cond:
        passes.append(label)
        print('[PASS]', label)
    else:
        msg = label if detail is None else f'{label}: {detail}'
        failures.append(msg)
        print('[FAIL]', msg)

# 1) Exact workstation compiler defect from R07.
auth = (ROOT/'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt').read_text(encoding='utf-8')
ok('Auth premium waveform binds to AuthUiState.busy', 'active = state.busy' in auth)
ok('Auth UI contains no nonexistent AuthUiState.loading reference', 'state.loading' not in auth)

# 2) Parse every UiState declaration and verify direct state.<property> references in screen functions.
state_props = {}
for path in ROOT.glob('feature/**/**/*.kt'):
    text = path.read_text(encoding='utf-8', errors='ignore')
    for m in re.finditer(r'data class\s+(\w*UiState)\s*\(', text):
        i = m.end(); depth = 1
        while i < len(text) and depth:
            if text[i] == '(': depth += 1
            elif text[i] == ')': depth -= 1
            i += 1
        body = text[m.end():i-1]
        state_props[m.group(1)] = set(re.findall(r'\bval\s+(\w+)\s*:', body))

def matching(text, start, open_ch, close_ch):
    depth = 0; in_string = False; quote = ''; escape = False; i = start
    while i < len(text):
        c = text[i]
        if in_string:
            if escape: escape = False
            elif c == '\\': escape = True
            elif c == quote: in_string = False
        else:
            if c in ('"', "'"):
                in_string = True; quote = c
            elif c == open_ch:
                depth += 1
            elif c == close_ch:
                depth -= 1
                if depth == 0: return i
        i += 1
    return None

state_ref_errors = []
for path in ROOT.glob('feature/**/ui/*Screens.kt'):
    text = path.read_text(encoding='utf-8', errors='ignore')
    for m in re.finditer(r'fun\s+(\w+)\s*\(', text):
        fn = m.group(1); ps = m.end()-1; pe = matching(text, ps, '(', ')')
        if pe is None: continue
        sig = text[ps+1:pe]
        sm = re.search(r'\bstate\s*:\s*(\w*UiState)\b', sig)
        if not sm: continue
        state_type = sm.group(1)
        bs = text.find('{', pe)
        if bs < 0: continue
        be = matching(text, bs, '{', '}')
        if be is None: continue
        refs = set(re.findall(r'\bstate\.(\w+)', text[bs:be+1]))
        unknown = refs - state_props.get(state_type, set()) - {'copy'}
        if unknown:
            state_ref_errors.append(f'{path.relative_to(ROOT)}::{fn}::{state_type} -> {sorted(unknown)}')
ok('All feature screen state property references resolve to declared UiState properties', not state_ref_errors, '; '.join(state_ref_errors))

# 3) Validate the new premium live-room model accesses against the real RTC model.
models = (ROOT/'feature/live/src/main/java/app/voicecloud/feature/live/model/LiveRoomModels.kt').read_text(encoding='utf-8')
m = re.search(r'data class\s+RtcPresenceState\s*\((.*?)\n\)', models, re.S)
rtc_props = set(re.findall(r'\bval\s+(\w+)\s*:', m.group(1))) if m else set()
needed_rtc = {'userId','role','isMuted','isSpeaking','handRaised','username'}
ok('Premium live StageRoster accesses only real RtcPresenceState fields', needed_rtc <= rtc_props, f'missing={sorted(needed_rtc-rtc_props)}')

# 4) Validate economy enum members used by new premium hero logic.
economy = (ROOT/'feature/economy/src/main/java/app/voicecloud/feature/economy/model/EconomyModels.kt').read_text(encoding='utf-8')
em = re.search(r'enum class\s+EconomySection[^\{]*\{(.*?)\n\}', economy, re.S)
enum_values = set(re.findall(r'^\s*([A-Z][A-Z0-9_]*)\s*\(', em.group(1), re.M)) if em else set()
needed_enum = {'WALLET','VIP','GIFTS','TASKS'}
ok('Premium economy hero references valid EconomySection constants', needed_enum <= enum_values, f'missing={sorted(needed_enum-enum_values)}')

# 5) Validate design token members referenced by the R05-R08 premium UI surface.
colors = (ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudColors.kt').read_text(encoding='utf-8')
def object_vals(name):
    mo = re.search(rf'object\s+{name}\s*\{{(.*?)\n\}}', colors, re.S)
    return set(re.findall(r'\bval\s+(\w+)\b', mo.group(1))) if mo else set()
consumer = object_vals('ConsumerColors'); creator = object_vals('CreatorColors')
consumer_refs=set(); creator_refs=set()
ui_files = list(ROOT.glob('feature/**/ui/*Screens.kt')) + list((ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem').glob('**/*.kt'))
for path in ui_files:
    txt=path.read_text(encoding='utf-8', errors='ignore')
    consumer_refs.update(re.findall(r'ConsumerColors\.(\w+)', txt))
    creator_refs.update(re.findall(r'CreatorColors\.(\w+)', txt))
ok('All ConsumerColors references resolve to centralized design tokens', consumer_refs <= consumer, f'unknown={sorted(consumer_refs-consumer)}')
ok('All CreatorColors references resolve to centralized design tokens', creator_refs <= creator, f'unknown={sorted(creator_refs-creator)}')

# 6) Validate all local premium primitive imports against declarations.
premium = (ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt').read_text(encoding='utf-8')
brandmark = (ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudBrandMark.kt').read_text(encoding='utf-8')
premium_symbols = set(re.findall(r'\bfun\s+(VoiceCloud\w+)\s*\(', premium + '\n' + brandmark))
required_primitives={'VoiceCloudPremiumBackdrop','VoiceCloudPremiumCard','VoiceCloudHeroCard','VoiceCloudSectionHeader','VoiceCloudAnimatedWaveform','VoiceCloudLiveBadge','VoiceCloudSpeakingAvatar','VoiceCloudShimmer','VoiceCloudBrandMark'}
ok('All required premium UI primitives are declared', required_primitives <= premium_symbols, f'missing={sorted(required_primitives-premium_symbols)}')

# 7) Guard the exact Compose overload/import failures already observed in R06/R07.
brand = brandmark
ok('Brand mark drawArc calls are explicit style-bound calls', brand.count('style = cloudStroke') == 3)
ok('No matchParentSize dependency remains in premium primitives', 'matchParentSize' not in premium)
ok('Speaking-ring canvas uses supported fillMaxSize modifier', 'Canvas(Modifier.fillMaxSize())' in premium)

# 8) Basic balanced-source / conflict audit across every R05-R08 changed Kotlin presentation file.
changed = [
 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudBrandMark.kt',
 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPageTopBar.kt',
 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/component/VoiceCloudPremium.kt',
 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudTheme.kt',
 'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
 'feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt',
 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
 'feature/economy/src/main/java/app/voicecloud/feature/economy/ui/EconomyScreens.kt',
 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
 'feature/hosting/src/main/java/app/voicecloud/feature/hosting/ui/HostingScreens.kt',
 'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
 'feature/profile/src/main/java/app/voicecloud/feature/profile/ui/ProfileScreens.kt',
 'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
]
for rel in changed:
    text=(ROOT/rel).read_text(encoding='utf-8')
    ok(f'No merge markers: {rel}', not any(x in text for x in ('<<<<<<<','=======','>>>>>>>')))
    # Existing compile-surface tests already do more sophisticated checks on legacy files;
    # this catches accidental edit damage in the complete premium surface.
    ok(f'Balanced braces: {rel}', text.count('{') == text.count('}'), f"open={text.count('{')} close={text.count('}')}")

# 9) Resolve local app.voicecloud imports used by the complete premium presentation surface.
all_kotlin = "\n".join(path.read_text(encoding="utf-8", errors="ignore") for path in ROOT.rglob("*.kt"))
declared = set(re.findall(r"\b(?:data\s+class|enum\s+class|sealed\s+(?:class|interface)|class|object|interface|fun|typealias)\s+(\w+)", all_kotlin))
local_import_errors=[]
for rel in changed:
    text=(ROOT/rel).read_text(encoding="utf-8")
    for imp in re.findall(r"^import\s+(app\.voicecloud\.[\w.]+)$", text, re.M):
        if imp.endswith(".*"): continue
        symbol=imp.rsplit(".",1)[-1]
        if symbol in {"R","BuildConfig"}: continue
        if symbol not in declared:
            local_import_errors.append(f"{rel} -> {imp}")
ok("All explicit local VoiceCloud imports in premium presentation files resolve to project declarations", not local_import_errors, "; ".join(local_import_errors))

# 10) Branding fields used by the new auth/splash presentation must exist in centralized authority.
brand_authority=(ROOT/'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudBrand.kt').read_text(encoding='utf-8')
ok('Centralized VoiceCloud brand authority exposes name', re.search(r'\bval\s+name\s*:', brand_authority) is not None)
ok('Centralized VoiceCloud brand authority exposes tagline', re.search(r'\bval\s+tagline\s*:', brand_authority) is not None)

if failures:
    print(f'[FAIL] VC-ANDROID-PH13-R08 full UI compile-risk audit: {len(passes)} PASS / {len(failures)} FAIL')
    raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R08 full UI compile-risk audit: {len(passes)}/{len(passes)} PASS')
