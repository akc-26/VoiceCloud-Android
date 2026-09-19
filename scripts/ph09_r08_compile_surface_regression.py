from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
checks = 0

def ok(label: str, condition: bool):
    global checks
    if not condition:
        raise SystemExit(f'[FAIL] {label}')
    checks += 1
    print(f'[PASS] {label}')

def text(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')

def imports_of(source: str) -> set[str]:
    return {line.strip()[7:] for line in source.splitlines() if line.startswith('import ')}

# Exact workstation compiler finding: Modifier was inherited from PH08 and accidentally dropped in PH09.
root_rel = 'app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt'
root = text(root_rel)
root_imports = imports_of(root)
ok('VoiceCloudRoot imports androidx.compose.ui.Modifier', 'androidx.compose.ui.Modifier' in root_imports)
ok('VoiceCloudRoot Modifier usages are backed by the Compose UI import', bool(re.search(r'\bModifier\b', root)) and 'androidx.compose.ui.Modifier' in root_imports)
ok('VoiceCloudRoot imports current PH09 appearance dependencies', all(x in root_imports for x in [
    'androidx.compose.foundation.isSystemInDarkTheme',
    'androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel',
    'androidx.lifecycle.compose.collectAsStateWithLifecycle',
    'app.voicecloud.core.preferences.ThemePreference',
    'app.voicecloud.feature.settings.ui.AppearanceViewModel',
]))

# Frozen-parent import regression: if an inherited explicit import is still referenced by simple name,
# it may not disappear silently during a PH09 edit.
authority = json.loads(text('contracts/VC-ANDROID-PH09-R08-INHERITED-IMPORT-AUTHORITY.json'))
for rel, inherited in authority.items():
    source = text(rel)
    current = imports_of(source)
    for imp in inherited:
        target = imp[7:] if imp.startswith('import ') else imp
        if target.endswith('.*') or ' as ' in target:
            continue
        simple = target.rsplit('.', 1)[-1]
        # Ignore imports whose simple name no longer appears outside import lines.
        body = '\n'.join(line for line in source.splitlines() if not line.startswith('import '))
        if re.search(rf'\b{re.escape(simple)}\b', body):
            ok(f'inherited import retained while still used: {rel} -> {simple}', target in current)

# Common Android/Compose symbols used by PH09-touched/new UI files must have direct or wildcard authority.
ui_files = [
    root_rel,
    'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
    'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
]
requirements = {
    'Modifier': ('androidx.compose.ui.Modifier',),
    'Alignment': ('androidx.compose.ui.Alignment',),
    'KeyboardOptions': ('androidx.compose.foundation.text.KeyboardOptions',),
    'KeyboardType': ('androidx.compose.ui.text.input.KeyboardType',),
    'TextOverflow': ('androidx.compose.ui.text.style.TextOverflow',),
    'FontWeight': ('androidx.compose.ui.text.font.FontWeight',),
    'Uri': ('android.net.Uri',),
    'Html': ('android.text.Html',),
    'Lifecycle': ('androidx.lifecycle.Lifecycle',),
    'LifecycleEventObserver': ('androidx.lifecycle.LifecycleEventObserver',),
    'LocalLifecycleOwner': ('androidx.lifecycle.compose.LocalLifecycleOwner',),
    'hiltViewModel': ('androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel',),
    'collectAsStateWithLifecycle': ('androidx.lifecycle.compose.collectAsStateWithLifecycle',),
    'rememberNavController': ('androidx.navigation.compose.rememberNavController',),
}
for rel in ui_files:
    source = text(rel)
    imports = imports_of(source)
    body = '\n'.join(line for line in source.splitlines() if not line.startswith('import '))
    for symbol, allowed in requirements.items():
        if re.search(rf'\b{re.escape(symbol)}\b', body):
            covered = any(x in imports for x in allowed)
            # runtime/layout/material star imports do not cover these explicit-package symbols.
            ok(f'{rel} resolves {symbol} through an explicit import', covered)

# New PH09 settings module external contracts.
api = text('feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsApi.kt')
api_imports = imports_of(api)
for symbol, imp in {
    'Body':'retrofit2.http.Body','DELETE':'retrofit2.http.DELETE','GET':'retrofit2.http.GET',
    'PATCH':'retrofit2.http.PATCH','POST':'retrofit2.http.POST','Path':'retrofit2.http.Path','Query':'retrofit2.http.Query'
}.items():
    ok(f'SettingsApi imports Retrofit {symbol}', imp in api_imports)

network = text('feature/settings/src/main/java/app/voicecloud/feature/settings/di/SettingsNetworkModule.kt')
network_imports = imports_of(network)
for symbol, imp in {
    'Module':'dagger.Module','Provides':'dagger.Provides','InstallIn':'dagger.hilt.InstallIn',
    'SingletonComponent':'dagger.hilt.components.SingletonComponent','Named':'javax.inject.Named','Singleton':'javax.inject.Singleton'
}.items():
    ok(f'SettingsNetworkModule imports {symbol}', imp in network_imports)

vm = text('feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsViewModel.kt')
vm_imports = imports_of(vm)
ok('SettingsViewModel imports HiltViewModel', 'dagger.hilt.android.lifecycle.HiltViewModel' in vm_imports)
ok('SettingsViewModel imports ViewModel and viewModelScope', 'androidx.lifecycle.ViewModel' in vm_imports and 'androidx.lifecycle.viewModelScope' in vm_imports)
ok('SettingsViewModel imports Retrofit HttpException', 'retrofit2.HttpException' in vm_imports)

# Signature/callsite compatibility across all PH09-modified boundaries.
nav = text('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
discovery = text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
live = text('feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt')
ok('public profile report callback signature matches navigation callsite', 'onReport: (String, String) -> Unit' in discovery and 'onReport = { userId, label ->' in nav)
ok('live room report callback signature matches navigation callsite', 'onReport: (String, String) -> Unit' in live and 'onReport = { id, label -> open(VoiceCloudRoutes.contextualReport(ReportTargetType.ROOM, id, label)) }' in nav)
ok('MyProfile PH09 callback additions match navigation callsite', all(x in discovery for x in ['onSettings: () -> Unit','onSecurity: () -> Unit','onSafety: () -> Unit']) and all(x in nav for x in ['onSettings = {','onSecurity = {','onSafety = {']))

# No merge debris or accidentally commented imports across the PH09 compile surface.
compile_surface = '\n'.join(text(rel) for rel in [
    root_rel,
    'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthRepository.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt',
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
    'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsApi.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsRepository.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/di/SettingsNetworkModule.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/model/SettingsModels.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsViewModel.kt',
])
ok('PH09 compile surface contains no merge/conflict markers', not re.search(r'^(<<<<<<<|=======|>>>>>>>)', compile_surface, re.M))
ok('PH09 compile surface contains no commented-out Modifier import', '// import androidx.compose.ui.Modifier' not in compile_surface)

print(f'[PASS] VC-ANDROID-PH09-R08 compile-surface/import regression: {checks}/{checks} PASS')
