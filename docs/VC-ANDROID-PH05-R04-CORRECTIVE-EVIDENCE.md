# VC-ANDROID-PH05-R04 — Runtime Corrective Evidence

## Trigger
After PH05-R03 installed and displayed the splash screen, the app terminated as Compose started rendering the first text content. The supplied Logcat showed:

- `java.lang.ArrayIndexOutOfBoundsException: length=20; index=46`
- `androidx.compose.ui.graphics.Color.getColorSpace-impl`
- `Color.convert` / `Color.toArgb`
- `AndroidTextPaint.setColor`

## Root cause
`VoiceCloudBrand.color()` parsed normal `#RRGGBB` / `#AARRGGBB` values into a 32-bit ARGB number and then called `Color(argb.toULong())`. The `ULong` constructor represents Compose's packed internal color value, so low bits from an ordinary ARGB value were interpreted as a color-space id. For the reported `consumer.text=#10262E`, the opaque ARGB value is `0xFF10262E`; its low six bits are `46`, matching the crash index.

## Correction
The centralized parser now calls `Color(argb.toInt())`, selecting Compose's 32-bit ARGB constructor. No screen-level color patches were made.

## Durable regression
`ph05_r04_brand_color_runtime_regression.py` asserts the parser uses the ARGB `Int` constructor, rejects the packed `ULong` path, validates every centralized hex color token, reproduces the reported low-bit index, and requires the Android palette instrumentation test.

`VoiceCloudFoundationInstrumentedTest.centralizedBrandPaletteUsesValidArgbColors()` executes `toArgb()` across every Consumer, Creator and Common centralized color and asserts the reported text/sapphire ARGB values. The same test also keeps package identity white-label safe through generated `BuildConfig.APPLICATION_ID`.

## Production-source delta
PH05-R03 → PH05-R04 production runtime change is restricted to:

- `core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudBrand.kt`

The instrumentation test changes only acceptance coverage. No LiveKit, API, repository, auth, navigation, Firebase, database, security or realtime implementation changes are included.
