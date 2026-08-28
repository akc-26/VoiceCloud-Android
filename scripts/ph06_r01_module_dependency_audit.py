from pathlib import Path
R=Path(__file__).resolve().parents[1]; c=[]
def t(x): return (R/x).read_text()
def ck(n,v):
 if not v: raise SystemExit('[FAIL] '+n)
 c.append(n); print('[PASS] '+n)
h=t('feature/hosting/build.gradle.kts'); app=t('app/build.gradle.kts')
for dep in [':core:designsystem',':core:network',':core:security',':core:realtime',':feature:live']:
 ck('hosting direct project dependency '+dep,f'project("{dep}")' in h)
for dep in ['libs.androidx.core.ktx','libs.androidx.activity.compose','libs.androidx.lifecycle.viewmodel.compose','libs.androidx.lifecycle.runtime.compose','libs.hilt.android','libs.retrofit.core','libs.moshi.kotlin','libs.kotlinx.coroutines.android','libs.androidx.compose.ui','libs.androidx.compose.foundation','libs.androidx.compose.animation','libs.androidx.compose.material3']:
 ck('hosting direct dependency '+dep,dep in h)
ck('hosting KSP hilt compiler','ksp(libs.hilt.compiler)' in h)
ck('app consumes hosting','implementation(project(":feature:hosting"))' in app)
ck('app directly consumes compose animation for nav motion','implementation(libs.androidx.compose.animation)' in app)
ck('hosting module registered','include(":feature:hosting")' in t('settings.gradle.kts'))
print(f'VC-ANDROID-PH06-R01 module dependency audit: {len(c)}/{len(c)} PASS')
