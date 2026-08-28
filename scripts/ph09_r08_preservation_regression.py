from pathlib import Path
import hashlib
import re
import sys

DEFAULT_ROOT = Path(__file__).resolve().parents[1]
TEXT_EXTENSIONS = {
    '.cmd', '.csv', '.gradle', '.json', '.kt', '.kts', '.md', '.properties',
    '.pro', '.ps1', '.py', '.sh', '.toml', '.txt', '.xml', '.yaml', '.yml',
}
TEXT_FILENAMES = {'.gitignore', 'gradlew'}

ALLOWED = {
    'README.md',
    'CHANGELOG.md',
    'settings.gradle.kts',
    'app/build.gradle.kts',
    'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt',
    'app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/data/AuthRepository.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthViewModel.kt',
    'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt',
    'feature/live/src/main/java/app/voicecloud/feature/live/ui/LiveRoomScreens.kt',
    # R08 closure corrections discovered by the full R06 Windows/device gate.
    'scripts/VC-ANDROID-DEVICE-INSTRUMENTATION.ps1',
    'app/src/main/java/app/voicecloud/android/notifications/VoiceCloudFirebaseMessagingService.kt',
    'feature/live/src/main/java/app/voicecloud/feature/live/rtc/RtcAudioEngine.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/data/DeviceMetadataProvider.kt',
    'feature/auth/src/main/java/app/voicecloud/feature/auth/ui/AuthScreens.kt',
    'feature/engagement/src/main/java/app/voicecloud/feature/engagement/data/PushNotificationCoordinator.kt',
    'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt',
}

REQUIRED_NEW = [
    'feature/settings/build.gradle.kts',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsApi.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsRepository.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/di/SettingsNetworkModule.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/model/SettingsModels.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt',
    'feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsViewModel.kt',
    'feature/settings/src/test/java/app/voicecloud/feature/settings/model/SettingsModelsTest.kt',
]


def _decode_text(raw: bytes) -> str:
    # Windows editors may add a UTF-8 BOM. Treat that as representation only.
    if raw.startswith(b'\xef\xbb\xbf'):
        raw = raw[3:]
    return raw.decode('utf-8')


def _canonical_properties(text: str) -> bytes:
    """Canonicalize Java/Gradle properties by effective key/value semantics.

    Blank lines, comments, line endings, BOMs, harmless trailing whitespace and
    property ordering do not change the effective configuration. Duplicate keys
    follow properties semantics: the last declaration wins.
    """
    props: dict[str, str] = {}
    pending = ''
    for original in text.replace('\r\n', '\n').replace('\r', '\n').split('\n'):
        line = original.rstrip(' \t')
        if pending:
            line = pending + line.lstrip()
            pending = ''
        # Basic Java-properties continuation support: odd terminal backslash continues.
        slash_count = len(line) - len(line.rstrip('\\'))
        if slash_count % 2 == 1:
            pending = line[:-1]
            continue
        stripped = line.strip()
        if not stripped or stripped.startswith('#') or stripped.startswith('!'):
            continue
        # Split on the first unescaped '=' or ':'; otherwise first whitespace.
        split_at = None
        escaped = False
        for i, ch in enumerate(stripped):
            if escaped:
                escaped = False
                continue
            if ch == '\\':
                escaped = True
                continue
            if ch in '=:' or ch.isspace():
                split_at = i
                break
        if split_at is None:
            key, value = stripped, ''
        else:
            key = stripped[:split_at].strip()
            rest = stripped[split_at:]
            rest = re.sub(r'^\s*[:=]?\s*', '', rest, count=1)
            value = rest.strip()
        props[key] = value
    if pending.strip():
        props[pending.strip()] = ''
    return ('\n'.join(f'{k}={props[k]}' for k in sorted(props)) + '\n').encode('utf-8')


def canonical_bytes(rel: str, raw: bytes) -> bytes:
    p = Path(rel)
    suffix = p.suffix.lower()
    if suffix not in TEXT_EXTENSIONS and p.name not in TEXT_FILENAMES:
        return raw
    try:
        text = _decode_text(raw)
    except UnicodeDecodeError:
        # Never weaken binary preservation because of a misleading extension.
        return raw
    if suffix == '.properties':
        return _canonical_properties(text)
    text = text.replace('\r\n', '\n').replace('\r', '\n')
    lines = [line.rstrip(' \t') for line in text.split('\n')]
    while lines and lines[-1] == '':
        lines.pop()
    return ('\n'.join(lines) + '\n').encode('utf-8')


def verify(root: Path = DEFAULT_ROOT) -> None:
    manifest = root / 'contracts/VC-ANDROID-PH09-R08-PH08-BASELINE-CANONICAL.sha256'
    if not manifest.exists():
        raise SystemExit('[FAIL] PH09-R08 canonical PH08 baseline manifest missing')

    count = 0
    changed = []
    for line in manifest.read_text(encoding='utf-8').splitlines():
        if not line.strip():
            continue
        expected, rel = line.split('  ', 1)
        path = root / rel
        if not path.exists():
            if rel in ALLOWED:
                continue
            raise SystemExit(f'[FAIL] preserved PH08 file missing: {rel}')
        actual = hashlib.sha256(canonical_bytes(rel, path.read_bytes())).hexdigest()
        if actual != expected:
            if rel not in ALLOWED:
                raise SystemExit(f'[FAIL] unexpected PH08 baseline content modification: {rel}')
            changed.append(rel)
        else:
            count += 1

    for rel in REQUIRED_NEW:
        if not (root / rel).exists():
            raise SystemExit(f'[FAIL] PH09 required new file missing: {rel}')

    print(f'[PASS] {count} PH08 baseline files remain semantically preserved outside PH09 allowlist')
    print(f'[PASS] PH09 controlled baseline changes: {len(changed)}')
    print('[PASS] Windows BOM/LF/CRLF/trailing-whitespace representation differences are normalized for preserved text')
    print('[PASS] preserved .properties files use semantic key/value comparison; root gradle.properties uses dedicated PH08 + R08 tooling authority')
    print('[PASS] PH09 settings module additive files present')


if __name__ == '__main__':
    verify()
