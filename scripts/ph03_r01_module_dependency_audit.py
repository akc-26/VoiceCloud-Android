from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
RULES=[
    ('androidx.activity.', 'libs.androidx.activity.compose'),
    ('androidx.lifecycle.compose.', 'libs.androidx.lifecycle.runtime.compose'),
    ('androidx.lifecycle.viewmodel.compose.', 'libs.androidx.lifecycle.viewmodel.compose'),
    ('androidx.hilt.lifecycle.viewmodel.compose.', 'libs.androidx.hilt.lifecycle.viewmodel.compose'),
    ('retrofit2.', 'libs.retrofit.core'),
    ('kotlinx.coroutines.', 'libs.kotlinx.coroutines.android'),
    ('androidx.datastore.', 'libs.androidx.datastore.preferences'),
    ('androidx.room.', 'libs.androidx.room.runtime'),
    ('io.socket.', 'libs.socketio.client'),
    ('com.squareup.moshi.', 'libs.moshi.kotlin'),
]
def ok(name, cond):
    if not cond: raise SystemExit(f'[FAIL] {name}')
    checks.append(name); print(f'[PASS] {name}')
modules=[]
for build in ROOT.rglob('build.gradle.kts'):
    if build == ROOT/'build.gradle.kts': continue
    module=build.parent
    src=list(module.rglob('*.kt'))
    if not src: continue
    source='\n'.join(p.read_text(encoding='utf-8') for p in src)
    build_text=build.read_text(encoding='utf-8')
    modules.append(module)
    for prefix, dep in RULES:
        if prefix in source:
            ok(f'{module.relative_to(ROOT)} directly declares dependency for {prefix}', dep in build_text)
ok('module dependency audit inspected Android/Kotlin modules', len(modules) >= 10)
ok('PH03 discovery module included in dependency audit', ROOT/'feature/discovery' in modules)
print(f'VC-ANDROID-PH03-R01 module dependency audit: {len(checks)}/{len(checks)} PASS')
