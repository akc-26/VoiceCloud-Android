from pathlib import Path
import json
import re

DEFAULT_ROOT = Path(__file__).resolve().parents[1]


def decode_properties(raw: bytes) -> str:
    if raw.startswith(b'\xef\xbb\xbf'):
        return raw[3:].decode('utf-8')
    if raw.startswith(b'\xff\xfe'):
        return raw[2:].decode('utf-16-le')
    if raw.startswith(b'\xfe\xff'):
        return raw[2:].decode('utf-16-be')
    try:
        return raw.decode('utf-8')
    except UnicodeDecodeError:
        return raw.decode('cp1252')


def parse_properties(text: str) -> dict[str, str]:
    props: dict[str, str] = {}
    pending = ''
    normalized = text.replace('\r\n', '\n').replace('\r', '\n')
    for original in normalized.split('\n'):
        line = original.rstrip(' \t\ufeff')
        if pending:
            line = pending + line.lstrip()
            pending = ''
        slash_count = len(line) - len(line.rstrip('\\'))
        if slash_count % 2 == 1:
            pending = line[:-1]
            continue
        stripped = line.strip()
        if not stripped or stripped.startswith('#') or stripped.startswith('!'):
            continue
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
    return props


def classify_extra(key: str, authority: dict) -> str:
    for prefix in authority['sensitiveExtraPrefixes']:
        if key.startswith(prefix):
            return 'sensitive'
    for prefix in authority['allowedRuntimeExtraPrefixes']:
        if key.startswith(prefix):
            return 'runtime'
    return 'unknown'


def evaluate(actual: dict[str, str], authority: dict):
    required = dict(authority['baselineRequired'])
    controlled = dict(authority['controlledTooling'])
    expected = {**required, **controlled}
    missing = sorted(k for k in expected if k not in actual)
    changed = sorted(k for k, value in expected.items() if k in actual and actual[k] != value)
    extras = sorted(set(actual) - set(expected))
    sensitive_extras = [k for k in extras if classify_extra(k, authority) == 'sensitive']
    runtime_extras = [k for k in extras if classify_extra(k, authority) == 'runtime']
    unknown_extras = [k for k in extras if classify_extra(k, authority) == 'unknown']
    return missing, changed, sensitive_extras, runtime_extras, unknown_extras


def verify(root: Path = DEFAULT_ROOT) -> None:
    expected_path = root / 'contracts/VC-ANDROID-PH09-R08-GRADLE-PROPERTIES-AUTHORITY.json'
    actual_path = root / 'gradle.properties'
    if not expected_path.exists():
        raise SystemExit('[FAIL] PH09-R08 Gradle properties authority missing')
    if not actual_path.exists():
        raise SystemExit('[FAIL] gradle.properties missing')
    authority = json.loads(expected_path.read_text(encoding='utf-8'))
    try:
        actual = parse_properties(decode_properties(actual_path.read_bytes()))
    except Exception as exc:
        raise SystemExit(f'[FAIL] gradle.properties could not be parsed safely: {type(exc).__name__}')
    missing, changed, sensitive_extras, runtime_extras, unknown_extras = evaluate(actual, authority)
    if missing or changed or sensitive_extras or unknown_extras:
        print('[FAIL] gradle.properties violates VoiceCloud/build-critical authority')
        if missing:
            print('[FAIL] missing required keys: ' + ', '.join(missing))
        for key in changed:
            print(f'[FAIL] changed required value: {key}')
        if sensitive_extras:
            print('[FAIL] unexpected source-sensitive keys: ' + ', '.join(sensitive_extras))
        if unknown_extras:
            print('[FAIL] unclassified project keys: ' + ', '.join(unknown_extras))
        raise SystemExit(1)
    for key in runtime_extras:
        print(f'[INFO] accepted workstation/tooling Gradle property: {key}')
    total = len(authority['baselineRequired']) + len(authority['controlledTooling'])
    print(f'[PASS] {len(authority["baselineRequired"])} frozen PH08 product/build keys preserved')
    print(f'[PASS] {len(authority["controlledTooling"])} PH09-R08 controlled Gradle tooling key preserved')
    print('[PASS] org.gradle.tooling.parallel=true is explicit for Gradle 9.5 / current Android Studio sync compatibility')
    print('[PASS] extra org.gradle.* and systemProp.* workstation/runtime properties are non-source authority')
    print('[PASS] unknown VOICECLOUD_/android./kotlin./kapt./ksp. keys remain fail-closed')
    print(f'[PASS] gradle.properties effective authority satisfied: {total} required keys')


if __name__ == '__main__':
    verify()
