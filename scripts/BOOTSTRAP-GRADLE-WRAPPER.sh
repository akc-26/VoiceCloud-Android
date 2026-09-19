#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
VERSION=9.5.0
SHA256=553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746
CACHE="${HOME}/.voicecloud/gradle-bootstrap"
ZIP="$CACHE/gradle-${VERSION}-bin.zip"
HOME_DIR="$CACHE/gradle-${VERSION}"
mkdir -p "$CACHE"
if [[ ! -x "$HOME_DIR/bin/gradle" ]]; then
  if [[ ! -f "$ZIP" ]]; then
    echo "[INFO] Downloading Gradle $VERSION once into the VoiceCloud tool cache..."
    if command -v curl >/dev/null; then curl -fL "https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip" -o "$ZIP"; else wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip"; fi
  fi
  rm -rf "$HOME_DIR"
  unzip -q "$ZIP" -d "$CACHE"
fi
LOCAL_URI="$(python3 -c 'import pathlib,sys; print(pathlib.Path(sys.argv[1]).resolve().as_uri())' "$ZIP")"
"$HOME_DIR/bin/gradle" wrapper --gradle-version "$VERSION" --distribution-type bin --gradle-distribution-url "$LOCAL_URI"
ESCAPED_LOCAL_URI="${LOCAL_URI/:/\\:}"
cat > gradle/wrapper/gradle-wrapper.properties <<EOF
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionSha256Sum=$SHA256
distributionUrl=$ESCAPED_LOCAL_URI
networkTimeout=60000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF
[[ -f gradle/wrapper/gradle-wrapper.jar && -f gradlew && -f gradlew.bat ]] || { echo '[FAIL] Standard wrapper files were not generated.'; exit 1; }
grep -Fq 'distributionUrl=file\:///' gradle/wrapper/gradle-wrapper.properties || { echo '[FAIL] Wrapper runtime is not pinned to the verified local Gradle ZIP.'; exit 1; }
echo '[PASS] Standard Gradle Wrapper generated and pinned to verified local Gradle ZIP.'
