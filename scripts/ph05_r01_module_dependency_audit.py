from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def ck(name, cond):
    if not cond: raise SystemExit(f'[FAIL] {name}')
    checks.append(name); print(f'[PASS] {name}')
RULES=[
 ('androidx.compose.foundation.', 'libs.androidx.compose.foundation'),
 ('androidx.activity.', 'libs.androidx.activity.compose'),
 ('androidx.lifecycle.compose.', 'libs.androidx.lifecycle.runtime.compose'),
 ('androidx.lifecycle.viewmodel.compose.', 'libs.androidx.lifecycle.viewmodel.compose'),
 ('androidx.hilt.lifecycle.viewmodel.compose.', 'libs.androidx.hilt.lifecycle.viewmodel.compose'),
 ('retrofit2.', 'libs.retrofit.core'), ('kotlinx.coroutines.', 'libs.kotlinx.coroutines.android'),
 ('androidx.datastore.', 'libs.androidx.datastore.preferences'), ('androidx.room.', 'libs.androidx.room.runtime'),
 ('io.socket.', 'libs.socketio.client'), ('com.squareup.moshi.', 'libs.moshi.kotlin'),
 ('com.google.firebase.messaging.', 'libs.firebase.messaging'), ('io.livekit.', 'libs.livekit.android'),
 ('dagger.hilt.', 'libs.hilt.android'),
]
modules=[]
for build in ROOT.rglob('build.gradle.kts'):
    if build == ROOT/'build.gradle.kts': continue
    module=build.parent; src=list(module.rglob('*.kt'))
    if not src: continue
    source='\n'.join(p.read_text(encoding='utf-8') for p in src); bt=build.read_text(encoding='utf-8'); modules.append(module)
    for prefix, dep in RULES:
        if prefix in source: ck(f'{module.relative_to(ROOT)} directly declares dependency for {prefix}', dep in bt)
ck('module dependency audit inspected Android/Kotlin modules', len(modules) >= 13)
ck('PH05 live module included in dependency audit', ROOT/'feature/live' in modules)
live=(ROOT/'feature/live/build.gradle.kts').read_text(encoding='utf-8')
for dep in [':core:designsystem',':core:network',':core:security',':core:realtime']:
    ck(f'live module directly depends on {dep}', f'project("{dep}")' in live)
ck('live module directly declares LiveKit', 'implementation(libs.livekit.android)' in live)
ck('live module directly declares coroutine runtime', 'implementation(libs.kotlinx.coroutines.android)' in live)
ck('live module directly declares Retrofit', 'implementation(libs.retrofit.core)' in live)
ck('live module directly declares Hilt', 'implementation(libs.hilt.android)' in live)
print(f'VC-ANDROID-PH05-R01 module dependency audit: {len(checks)}/{len(checks)} PASS')
