# VoiceCloud Android — Google Sign-In configuration

VoiceCloud Android uses **Android Credential Manager** with the **Google Identity** library to obtain a real Google **ID token**, then exchanges it with the VoiceCloud backend at `POST /auth/google/login`.

## Required Google Cloud setup

1. Open [Google Cloud Console](https://console.cloud.google.com/) for the project that verifies Google tokens on the VoiceCloud backend.
2. Enable the **Google Identity Services** / Sign-In APIs used by your backend.
3. Create an **OAuth 2.0 Client ID** of type **Web application**.  
   This Web client ID is what Android passes to Credential Manager (`serverClientId`). It must be the same client ID your VoiceCloud server uses to verify Google ID tokens.
4. Create an **OAuth 2.0 Client ID** of type **Android** (recommended for production):
   - Package name: `app.voicecloud.android` (release) or `app.voicecloud.android.debug` (debug builds)
   - SHA-1 certificate fingerprint from your signing keystore (debug and release as needed)

## Gradle configuration (no secrets in source)

Set these in the project **`gradle.properties`** (or `~/.gradle/gradle.properties`):

| Property | Used by |
| --- | --- |
| `VOICECLOUD_GOOGLE_WEB_CLIENT_ID` | Fallback Web client ID for all build types if the debug/release-specific property is empty |
| `VOICECLOUD_GOOGLE_WEB_CLIENT_ID_DEBUG` | `debug` and `staging` build types → `BuildConfig.GOOGLE_WEB_CLIENT_ID` |
| `VOICECLOUD_GOOGLE_WEB_CLIENT_ID_RELEASE` | `release` build type → `BuildConfig.GOOGLE_WEB_CLIENT_ID` |

Example (replace with your Web client ID from Google Cloud):

```properties
VOICECLOUD_GOOGLE_WEB_CLIENT_ID_DEBUG=123456789012-abcdef.apps.googleusercontent.com
VOICECLOUD_GOOGLE_WEB_CLIENT_ID_RELEASE=987654321098-zyxwvu.apps.googleusercontent.com
```

Rebuild the app after changing these values.

## Runtime behavior

- If the Web client ID is **empty**, the Google Sign-In screen shows a configuration error (no token is fabricated).
- If **Google Play services** are missing or outdated, the user sees a recoverable error.
- User **cancellation** returns to the screen without calling the backend.
- A successful ID token is sent to **`/auth/google/login`**; tokens and user session follow the same path as email/password login (portal/role gates unchanged).

## Verification checklist

- [ ] Web client ID matches backend Google token verification configuration  
- [ ] Android OAuth client registered with correct package name + SHA-1  
- [ ] `supportedLoginMethods` from `/config/mobile` includes `google` (entry point visibility)  
- [ ] Sign-in completes and `/auth/me` returns the expected VoiceCloud user  
