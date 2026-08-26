from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
checks=[]
def ok(label, cond):
    if not cond:
        raise SystemExit(f"[FAIL] {label}")
    checks.append(label); print(f"[PASS] {label}")
def read(rel): return (root/rel).read_text(encoding='utf-8')

logger=read('core/logging/src/main/java/app/voicecloud/core/logging/VoiceCloudLogger.kt')
ok('logging regex uses Kotlin raw string for Bearer whitespace pattern', 'Regex("""(?i)Bearer\\s+' in logger)
ok('logging regex uses Kotlin raw string for token whitespace pattern', '([^\\s&]+)' in logger and 'Regex("""(?i)(access' in logger)
ok('logging source has no illegal standard-string \\s escape', not re.search(r'Regex\("[^"\n]*\\s', logger))
ok('logging redaction behavior remains present', '[REDACTED]' in logger and '.replace(BEARER' in logger and '.replace(TOKEN)' in logger)

sdk=read('scripts/VC-ANDROID-SDK-ENV.cmd')
ok('Windows acceptance honors ANDROID_HOME', 'ANDROID_HOME' in sdk)
ok('Windows acceptance honors ANDROID_SDK_ROOT', 'ANDROID_SDK_ROOT' in sdk)
ok('Windows acceptance auto-detects standard Android Studio SDK folder', r'%LOCALAPPDATA%\Android\Sdk' in sdk)
ok('SDK auto-detection does not commit machine-specific local.properties', 'echo sdk.dir=' not in sdk.lower())
accept=read('scripts/VC-ANDROID-PH01-R04-ACCEPTANCE.cmd')
ok('R04 acceptance resolves Android SDK before Gradle compile gates', 'call scripts\\VC-ANDROID-SDK-ENV.cmd' in accept and accept.index('VC-ANDROID-SDK-ENV.cmd') < accept.index('[GATE 1]'))
ok('R04 acceptance retains debug compile gate', ':app:compileDebugKotlin' in accept)
ok('R04 acceptance retains staging compile gate', ':app:compileStagingKotlin' in accept)
ok('R04 acceptance retains release compile gate', ':app:compileReleaseKotlin' in accept)
print(f"VC-ANDROID-PH01-R04 SDK/logging regression: {len(checks)}/{len(checks)} PASS")
