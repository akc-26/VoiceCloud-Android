from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
brand_kt = (ROOT / "core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudBrand.kt").read_text(encoding="utf-8")
colors_kt = (ROOT / "core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudColors.kt").read_text(encoding="utf-8")
instrumented = (ROOT / "app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt").read_text(encoding="utf-8")
branding_text = (ROOT / "branding/voicecloud-brand.properties").read_text(encoding="utf-8")
acceptance = (ROOT / "scripts/VC-ANDROID-PH05-R04-ACCEPTANCE.cmd").read_text(encoding="utf-8") if (ROOT / "scripts/VC-ANDROID-PH05-R04-ACCEPTANCE.cmd").exists() else ""

checks = []
def ck(name, ok):
    checks.append((name, bool(ok)))
    print(f"[{'PASS' if ok else 'FAIL'}] {name}")

ck("brand parser uses 32-bit ARGB Int constructor", "return Color(argb.toInt())" in brand_kt)
ck("packed ULong constructor is forbidden for branding ARGB", "Color(argb.toULong())" not in brand_kt)
ck("brand parser still supports six-digit RGB with opaque alpha", "0xFF000000L or raw.toLong(16)" in brand_kt)
ck("brand parser still supports eight-digit ARGB", "8 -> raw.toLong(16)" in brand_kt)

props = {}
for line in branding_text.splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, v = line.split("=", 1)
    props[k.strip()] = v.strip()
color_props = {k: v for k, v in props.items() if k.startswith(("consumer.", "creator.", "common.")) and k not in {"consumer": ""}}
# Only color-valued visual tokens are hex; radii/motion are not under these prefixes except common colors.
hex_color_props = {k: v for k, v in color_props.items() if v.startswith("#")}
ck("all centralized color tokens are #RRGGBB or #AARRGGBB", bool(hex_color_props) and all(re.fullmatch(r"#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?", v) for v in hex_color_props.values()))
ck("reported consumer.text color remains #10262E", props.get("consumer.text") == "#10262E")
reported_argb = int("FF" + props.get("consumer.text", "#000000").lstrip("#"), 16)
ck("reported crash reproduces packed-color low-bit index 46", (reported_argb & 0x3F) == 46)
ck("consumer palette continues through central parser", "VoiceCloudBrand.color(BuildConfig.CONSUMER_TEXT)" in colors_kt)
ck("creator palette continues through central parser", "VoiceCloudBrand.color(BuildConfig.CREATOR_PRIMARY)" in colors_kt)
ck("common palette continues through central parser", "VoiceCloudBrand.color(BuildConfig.COMMON_ERROR)" in colors_kt)
ck("instrumentation contains centralized palette runtime test", "centralizedBrandPaletteUsesValidArgbColors" in instrumented)
ck("instrumentation executes Compose Color toArgb conversion", ".toArgb()" in instrumented)
ck("instrumentation asserts reported text ARGB", "0xFF10262E.toInt(), ConsumerColors.Text.toArgb()" in instrumented)
ck("instrumentation asserts primary sapphire ARGB", "0xFF0B7C86.toInt(), ConsumerColors.Sapphire.toArgb()" in instrumented)
ck("instrumentation includes consumer creator and common palettes", all(token in instrumented for token in ["ConsumerColors.Sapphire", "CreatorColors.Primary", "CommonColors.Error"]))
ck("instrumentation identity follows generated application id", "BuildConfig.APPLICATION_ID" in instrumented and '"app.voicecloud.android.debug"' not in instrumented)
ck("R04 acceptance invokes Windows R04 source checker", "VC-ANDROID-PH05-R04-SOURCE-CHECK.ps1" in acceptance)
ck("R04 acceptance invokes brand color runtime regression", "ph05_r04_brand_color_runtime_regression.py" in acceptance)
ck("R04 acceptance retains Debug Staging Release compile gates", all(task in acceptance for task in [":app:compileDebugKotlin", ":app:compileStagingKotlin", ":app:compileReleaseKotlin"]))
ck("R04 acceptance retains isolated device instrumentation", "VC-ANDROID-DEVICE-INSTRUMENTATION.ps1" in acceptance)

failed = [name for name, ok in checks if not ok]
print(f"VC-ANDROID-PH05-R04 brand-color runtime regression: {len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    sys.exit(1)
