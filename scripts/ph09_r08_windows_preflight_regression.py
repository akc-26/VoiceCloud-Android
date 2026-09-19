from pathlib import Path
import importlib.util
import json

ROOT = Path(__file__).resolve().parents[1]


def load(name: str, filename: str):
    path = ROOT / 'scripts' / filename
    spec = importlib.util.spec_from_file_location(name, path)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


authority = load('ph09_r08_gradle_authority', 'ph09_r08_gradle_properties_authority.py')
preserve = load('ph09_r08_preserve', 'ph09_r08_preservation_regression.py')
checks = 0


def ok(label: str, condition: bool):
    global checks
    if not condition:
        raise SystemExit(f'[FAIL] {label}')
    checks += 1
    print(f'[PASS] {label}')


manifest = ROOT / 'contracts/VC-ANDROID-PH09-R08-PH08-BASELINE-CANONICAL.sha256'
authority_file = ROOT / 'contracts/VC-ANDROID-PH09-R08-GRADLE-PROPERTIES-AUTHORITY.json'
ok('generic PH08 manifest exists', manifest.exists())
ok('Gradle properties authority exists', authority_file.exists())
ok('generic PH08 manifest does not contain root gradle.properties', not any(line.endswith('  gradle.properties') for line in manifest.read_text(encoding='utf-8').splitlines()))
authority.verify(ROOT)
preserve.verify(ROOT)
ok('delivered root passes Gradle authority and generic preservation', True)

auth = json.loads(authority_file.read_text(encoding='utf-8'))
raw = (ROOT / 'gradle.properties').read_bytes()
text = authority.decode_properties(raw).replace('\r\n', '\n').replace('\r', '\n')
base = authority.parse_properties(text)


def evaluation_for(data: bytes):
    parsed = authority.parse_properties(authority.decode_properties(data))
    return parsed, authority.evaluate(parsed, auth)


def passes(data: bytes) -> bool:
    _, result = evaluation_for(data)
    missing, changed, sensitive, runtime, unknown = result
    return not (missing or changed or sensitive or unknown)


variants = {
    'CRLF': text.replace('\n', '\r\n').encode('utf-8'),
    'UTF-8 BOM + CRLF': b'\xef\xbb\xbf' + text.replace('\n', '\r\n').encode('utf-8'),
    'UTF-16LE BOM': b'\xff\xfe' + text.encode('utf-16-le'),
    'comments/order/whitespace': ('# local representation comment\r\n' + '\r\n'.join(reversed([ln.strip() for ln in text.split('\n') if ln.strip() and not ln.lstrip().startswith('#')])) + '\r\n\r\n').encode('utf-8'),
}
for label, data in variants.items():
    ok(f'{label} preserves effective Gradle authority', passes(data))

runtime_variants = {
    'duplicate Android Studio tooling-parallel=true': (text + '\norg.gradle.tooling.parallel=true\n').encode('utf-8'),
    'additional Gradle tooling runtime property': (text + '\norg.gradle.tooling.parallel.ignore-legacy-default=true\n').encode('utf-8'),
    'machine-specific Gradle Java home': (text + '\norg.gradle.java.home=C:\\\\Program Files\\\\Android\\\\Android Studio\\\\jbr\n').encode('utf-8'),
    'system property proxy': (text + '\nsystemProp.http.proxyHost=localhost\n').encode('utf-8'),
}
for label, data in runtime_variants.items():
    _, result = evaluation_for(data)
    ok(f'{label} is accepted as non-source runtime/tooling state', not any(result[i] for i in (0, 1, 2, 4)))

tampers = {
    'payment mode change': text.replace('VOICECLOUD_ANDROID_PAYMENT_MODE=HOSTED_GATEWAY', 'VOICECLOUD_ANDROID_PAYMENT_MODE=GOOGLE_PLAY').encode('utf-8'),
    'Gradle parallel change': text.replace('org.gradle.parallel=true', 'org.gradle.parallel=false').encode('utf-8'),
    'tooling parallel controlled value change': text.replace('org.gradle.tooling.parallel=true', 'org.gradle.tooling.parallel=false').encode('utf-8'),
    'unknown VoiceCloud property': (text + '\nVOICECLOUD_UNREVIEWED_FLAG=true\n').encode('utf-8'),
    'unknown Android property': (text + '\nandroid.unreviewedFlag=true\n').encode('utf-8'),
    'unknown Kotlin property': (text + '\nkotlin.unreviewedFlag=true\n').encode('utf-8'),
    'unknown project property': (text + '\nmy.custom.project.flag=true\n').encode('utf-8'),
}
for label, data in tampers.items():
    ok(f'{label} remains detectable', not passes(data))

ok('delivered gradle.properties explicitly declares org.gradle.tooling.parallel=true', base.get('org.gradle.tooling.parallel') == 'true')
print(f'[PASS] VC-ANDROID-PH09-R08 Windows/tooling preflight regression: {checks}/{checks} PASS')
