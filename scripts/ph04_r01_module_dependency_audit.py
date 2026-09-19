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
 ('com.google.firebase.messaging.', 'libs.firebase.messaging'),
]
modules=[]
for build in ROOT.rglob('build.gradle.kts'):
    if build == ROOT/'build.gradle.kts': continue
    module=build.parent; src=list(module.rglob('*.kt'))
    if not src: continue
    source='\n'.join(p.read_text(encoding='utf-8') for p in src); bt=build.read_text(encoding='utf-8'); modules.append(module)
    for prefix, dep in RULES:
        if prefix in source: ck(f'{module.relative_to(ROOT)} directly declares dependency for {prefix}', dep in bt)
ck('module dependency audit inspected Android/Kotlin modules', len(modules) >= 12)
ck('PH04 engagement included in dependency audit', ROOT/'feature/engagement' in modules)
eng=(ROOT/'feature/engagement/build.gradle.kts').read_text(encoding='utf-8')
ck('engagement exports discovery model dependency used by public models', 'api(project(":feature:discovery"))' in eng)
ck('engagement directly depends on PH02 auth FCM foundation', 'implementation(project(":feature:auth"))' in eng)
print(f'VC-ANDROID-PH04-R01 module dependency audit: {len(checks)}/{len(checks)} PASS')
