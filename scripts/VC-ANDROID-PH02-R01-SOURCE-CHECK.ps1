$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
$settings=T 'settings.gradle.kts'; $app=T 'app/build.gradle.kts'; $api=T 'feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthApi.kt'; $repo=T 'feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthRepository.kt'; $network=T 'core/network/src/main/java/app/voicecloud/core/network/ApiClientFactory.kt'; $refresh=T 'feature/auth/src/main/java/app/voicecloud/feature/auth/data/SingleFlightTokenRefreshCoordinator.kt'; $screens=T 'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt'; $nav=T 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'; $manifest=T 'app/src/main/AndroidManifest.xml'; $prefs=T 'core/preferences/src/main/java/app/voicecloud/core/preferences/VoiceCloudPreferences.kt'; $google=T 'feature/auth/src/main/java/app/voicecloud/feature/auth/data/FirebaseGoogleTokenExchange.kt'; $catalog=T 'gradle/libs.versions.toml'
P 'PH02 auth module registered' $settings.Contains('include(":feature:auth")')
P 'PH02-or-later version marker retained' ([regex]::IsMatch($app, 'versionName\s*=\s*"1\.0\.0-ph(?:0?[2-9]|[1-9][0-9]+)"'))
@('auth/login','auth/register','auth/phone/send-otp','auth/phone/login','auth/google/login','auth/guest/login','auth/guest/upgrade','auth/forgot-password','auth/reset-password','auth/refresh','auth/me','auth/logout','auth/logout-all','notifications/register-device','creator-access/applications')|%{P "endpoint $_ retained" $api.Contains($_)}
P 'public and authenticated Retrofit authority separated' ($network.Contains('createPublicRetrofit') -and $network.Contains('createAuthenticatedRetrofit') -and $network.Contains('RefreshingAuthenticator'))
P 'single-flight refresh and token rotation retained' ($refresh.Contains('synchronized(lock)') -and $refresh.Contains('tokenVault.save(body.accessToken, body.refreshToken)'))
P 'Creator role validation retained' ($repo.Contains('user.normalizedRole != VoiceCloudRole.CREATOR') -and $repo.Contains('Enter a valid Creator email address.'))
P 'admin portal blocking retained' ($repo.Contains('VoiceCloudRole.ADMIN') -and $repo.Contains('VoiceCloudRole.SUPER_ADMIN'))
P 'dual portal routes retained' ($nav.Contains('PortalSelector') -and $nav.Contains('UserSignIn') -and $nav.Contains('CreatorSignIn'))
P 'per-user onboarding completion avoids repeat loop' ($repo.Contains('markOnboardingCompleted(user.id)') -and $repo.Contains('hasCompletedOnboarding(user.id)') -and $prefs.Contains('onboarding_completed_users'))
P 'reset HTTPS App Link retained' ($manifest.Contains('android:autoVerify="true"') -and $manifest.Contains('android:scheme="https"') -and $manifest.Contains('/auth/reset-password'))
P 'Google token is exchanged through Firebase Auth' ($google.Contains('GoogleAuthProvider.getCredential') -and $google.Contains('signInWithCredential') -and $google.Contains('getIdToken(true)'))
P 'Firebase Android BoM pinned' $catalog.Contains('firebaseBom = "34.18.0"')
P 'Creator auth uses Creator presentation authority' $screens.Contains('darkTheme = creator')
P 'PH02 required feature modules retained' ($settings.Contains('include(":feature:bootstrap")') -and $settings.Contains('include(":feature:auth")'))
Write-Host "VC-ANDROID-PH02-R01 Windows source authority: $script:count/$script:count PASS"
