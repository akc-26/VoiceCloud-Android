from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "build.gradle.kts"
text = APP.read_text(encoding="utf-8")

checks = []
def check(name: str, condition: bool):
    if not condition:
        raise SystemExit(f"[FAIL] {name}")
    checks.append(name)
    print(f"[PASS] {name}")

check("Gradle Kotlin DSL imports java.net.URI explicitly", "import java.net.URI" in text)
check("Gradle Kotlin DSL avoids shadow-prone java.net.URI qualification", "java.net.URI(" not in text)
check("centralized webHost helper uses imported URI type", "fun webHost(url: String): String = URI(url).host" in text)
check("invalid Web endpoint host fails closed", "VoiceCloud Web endpoint must contain a valid host" in text)
check("default manifest placeholder uses webHost helper", 'defaultConfig.manifestPlaceholders["voicecloudWebHost"] = webHost(debugWebUrl)' in text)
check("debug manifest placeholder uses webHost helper", 'manifestPlaceholders["voicecloudWebHost"] = webHost(debugWebUrl)' in text)
check("staging manifest placeholder uses webHost helper", 'manifestPlaceholders["voicecloudWebHost"] = webHost(stagingWebUrl)' in text)
check("release manifest placeholder uses webHost helper", 'manifestPlaceholders["voicecloudWebHost"] = webHost(releaseWebUrl)' in text)

print(f"VC-ANDROID-PH02-R02 Gradle Kotlin DSL regression: {len(checks)}/{len(checks)} PASS")
