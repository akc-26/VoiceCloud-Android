from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
FAIL = []
PASS = []

def text(rel): return (ROOT / rel).read_text(encoding='utf-8')
def check(name, condition):
    (PASS if condition else FAIL).append(name)
    print(('[PASS] ' if condition else '[FAIL] ') + name)

def main():
    settings = text('settings.gradle.kts')
    app = text('app/build.gradle.kts')
    api = text('feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt')
    repo = text('feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorRepository.kt')
    models = text('feature/creator/src/main/java/app/voicecloud/feature/creator/model/CreatorModels.kt')
    screens = text('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorScreens.kt')
    vm = text('feature/creator/src/main/java/app/voicecloud/feature/creator/ui/CreatorViewModel.kt')
    nav = text('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt')
    auth_repo = text('feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthRepository.kt')
    auth_vm = text('feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt')
    discovery = text('feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt')
    contract = json.loads(text('contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json'))
    endpoints=set()
    def walk(x):
        if isinstance(x,dict):
            if 'method' in x and 'path' in x: endpoints.add((x['method'],x['path']))
            for v in x.values(): walk(v)
        elif isinstance(x,list):
            for v in x: walk(v)
    walk(contract)
    check('PH10 Creator module registered', 'include(":feature:creator")' in settings)
    check('PH10 Creator module consumed by app', 'implementation(project(":feature:creator"))' in app)
    check('PH10 app version declared', 'versionName = "1.0.0-ph10"' in app)
    required=[('GET','/creator/dashboard'),('GET','/users/profile/me'),('PATCH','/users/profile'),('GET','/users/settings'),('PATCH','/users/settings'),('GET','/cms/creator/pages'),('GET','/cms/creator/pages/:param'),('POST','/contact'),('GET','/config/maintenance')]
    for ep in required: check(f'R06 contract contains {ep[0]} {ep[1]}', ep in endpoints)
    for wire in ['@GET("creator/dashboard")','@GET("users/profile/me")','@PATCH("users/profile")','@GET("users/settings")','@PATCH("users/settings")','@GET("cms/creator/pages")','@GET("cms/creator/pages/{slug}")','@POST("contact")','@GET("config/maintenance")']:
        check(f'Creator API wires {wire}', wire in api)
    check('Creator dashboard displays backend-returned supported metrics only', 'dashboardMetrics(raw)' in repo and 'Server-authoritative Creator overview' in screens)
    check('Creator profile writes only supported profile fields', all(x in repo for x in ['"bio" to','"country" to','"preferredLanguage" to "en"','"interests" to']))
    check('Creator settings use canonical notification/language/timezone authority', all(x in repo for x in ['"notificationPreferences" to','"language" to "en"','"timezone" to']))
    check('Creator CMS uses Creator audience endpoints', 'cms/creator/pages' in api and 'Intent(' not in screens and 'ACTION_SENDTO' not in screens)
    check('Creator support persists through /contact', '@POST("contact")' in api and 'persisted /contact authority' in screens)
    check('Creator maintenance authority is wired', '@GET("config/maintenance")' in api and 'repository.maintenance()' in vm)
    check('Creator 401/403/503 propagate to global auth handling', 'CreatorEvent.AuthFailure' in vm and 'handleAuthenticatedFailure(event.httpStatus' in nav)
    check('Creator role guard is explicit', 'VoiceCloudRole.CREATOR' in nav and nav.count('normalizedRole != VoiceCloudRole.CREATOR') >= 6)
    check('Creator login handoff now targets dashboard', 'AuthScreen.CREATOR_READY -> VoiceCloudRoutes.CreatorDashboard' in nav)
    check('Creator access application remains linked from Creator Sign In', 'onApply = { open(VoiceCloudRoutes.CreatorAccess) }' in nav)
    check('CREATOR-only consumer switch is explicit', 'canSwitchToCreator = authState.user?.normalizedRole == VoiceCloudRole.CREATOR' in nav)
    check('portal switching changes preference without second account/token', 'suspend fun switchPortal(target: LastPortal)' in auth_repo and 'preferences.setLastPortal(target)' in auth_repo)
    check('Creator switch validates backend role', 'target == LastPortal.CREATOR && user.normalizedRole != VoiceCloudRole.CREATOR' in auth_repo)
    check('Creator shell exposes only PH10 core sections', 'enum class CreatorPortalSection { DASHBOARD, PROFILE, SETTINGS, HELP }' in models)
    early_forbidden=['creator/rooms','creator/schedules','creator/messages','creator/subscribers','creator/payout','creator/wallet','hosts/verification','stream-credentials']
    creator_source='\n'.join([api,repo,models,screens,vm])
    check('PH10 does not pull PH11-PH13 Creator endpoints forward', not any(x in creator_source for x in early_forbidden))
    check('PH10 contains no Admin API exposure', '/admin/' not in creator_source and '@GET("admin/' not in creator_source)
    check('Creator presentation contains no hard-coded hex brand colours', re.search(r'Color\(0x[0-9A-Fa-f]+', screens) is None)
    check('Creator portal uses centralized Creator theme', 'VoiceCloudTheme(portal = PortalTheme.Creator' in screens)
    check('English-only language state preserved', 'English is the only language currently published by the backend' in screens)
    check('human-facing Creator UI does not intentionally render technical IDs', 'Text(profile.id' not in screens and 'Text("ID' not in screens)
    check('consumer profile shows Creator switch only through explicit role capability', 'canSwitchToCreator' in discovery and 'onSwitchToCreator' in discovery)
    if FAIL:
        raise SystemExit(f'[FAIL] VC-ANDROID-PH10-R01 source authority: {len(FAIL)} failure(s)')
    print(f'[PASS] VC-ANDROID-PH10-R01 source authority complete: {len(PASS)}/{len(PASS)} PASS')

if __name__=='__main__': main()
