from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
models=read('feature/settings/src/main/java/app/voicecloud/feature/settings/model/SettingsModels.kt')
repo=read('feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsRepository.kt')
api=read('feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsApi.kt')
screens=read('feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt')
creatorApi=read('feature/creator/src/main/java/app/voicecloud/feature/creator/data/CreatorApi.kt')
# Privacy exact current backend DTO
for key in ['showOnlineStatus','showLastSeen','allowDirectMessages','showGifts']:
    ck(f'Privacy model and PATCH contain backend field {key}', key in models and f'"{key}" to value.{key}' in repo)
for old in ['messagingPermission','followPermission','invitationPermission','visitorPermission','allowVisitorTracking','anonymousVisiting']:
    ck(f'Privacy no longer submits obsolete field {old}', f'"{old}" to' not in repo and f'val {old}:' not in models)
ck('Privacy uses canonical GET/PATCH users/privacy', '@GET("users/privacy")' in api and '@PATCH("users/privacy")' in api)
# Devices canonical backend deviceId
ck('Device detail API path is canonical deviceId', '@GET("auth/devices/{deviceId}")' in api and '@Path("deviceId") deviceId: String' in api)
ck('Device revoke API path is canonical deviceId', '@DELETE("auth/devices/{deviceId}")' in api)
ck('Safe device canonicalizes backend deviceId before row id', 'val canonicalDeviceId = raw.string("deviceId").ifBlank { recordId }' in repo)
ck('Device list navigation passes canonical deviceId', 'onDevice(device.deviceId ?: device.id)' in screens)
# Contact exact current backend DTO
ck('Contact request uses phoneNumber field', 'val phoneNumber: String?' in models and 'phoneNumber = phone?.trim()' in repo)
ck('Contact request uses message field', 'val message: String' in models and 'message = description.trim().take(4000)' in repo)
ck('Contact request does not send legacy phone field', 'val phone: String?' not in models)
ck('Contact request does not send legacy description field', 'val description: String' not in models.split('data class ContactSupportRequest',1)[1].split(')',1)[0])
ck('Contact validates backend minimum message length', 'description.trim().length >= 10' in repo)
ck('Contact posts to canonical /contact', '@POST("contact")' in api)
# CMS audience isolation
ck('Consumer CMS uses public END_USER endpoint', '@GET("cms/pages")' in api and '@GET("cms/pages/{slug}")' in api)
ck('Consumer CMS applies defensive End User audience filter', 'page.audience.equals("end_user", true)' in repo)
ck('Creator CMS uses creator audience endpoints', '@GET("cms/creator/pages")' in creatorApi and '@GET("cms/creator/pages/{slug}")' in creatorApi)
ck('Consumer settings repository never calls creator CMS endpoint', 'cms/creator' not in repo)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH11-R02 backend-contract regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH11-R02 backend-contract regression: {passed}/{len(checks)} PASS')
