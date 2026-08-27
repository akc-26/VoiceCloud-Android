from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
app = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
design = (ROOT / 'core/designsystem/build.gradle.kts').read_text(encoding='utf-8')
versions = (ROOT / 'gradle/libs.versions.toml').read_text(encoding='utf-8')
checks = []

def ck(name, ok):
    checks.append((name, bool(ok)))
    print(f"[{'PASS' if ok else 'FAIL'}] {name}")

# Exact workstation failures from PH05-R01.
ck('app enables resValues before using resValue', 'resValues = true' in app and 'resValue(' in app)
ck('design system enables resValues before using resValue', 'resValues = true' in design and 'resValue(' in design)
ck('design system explicitly enables Android resource processing', 'androidResources {' in design and 'enable = true' in design)
ck('legacy sourceSets AndroidLibrarySourceSet cast removed', 'sourceSets.getByName("main").res' not in design)
ck('AGP9 variant API configures static branding resources', 'androidComponents {' in design and 'variant.sources.res?.addStaticSourceDirectory("../../branding/res")' in design)
ck('branding resources remain in single root authority', (ROOT / 'branding/res/drawable/vc_brand_splash.xml').exists() and (ROOT / 'branding/res/drawable/vc_brand_app_icon_foreground.xml').exists())
ck('design system still consumes centralized branding properties', 'branding/voicecloud-brand.properties' in design)
ck('app still consumes centralized branding properties', 'branding/voicecloud-brand.properties' in app)
ck('AGP 9.3.0 authority retained', 'agp = "9.3.0"' in versions)
ck('no deprecated LibraryBuildFeatures androidResources toggle introduced', 'buildFeatures {\n        androidResources' not in design)

failed = [name for name, ok in checks if not ok]
print(f"VC-ANDROID-PH05-R02 AGP9 branding Gradle regression: {len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    sys.exit(1)
