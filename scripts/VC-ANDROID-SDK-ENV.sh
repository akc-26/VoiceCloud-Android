#!/usr/bin/env sh
set -eu
if [ -n "${ANDROID_HOME:-}" ] && [ -d "$ANDROID_HOME" ]; then
  :
elif [ -n "${ANDROID_SDK_ROOT:-}" ] && [ -d "$ANDROID_SDK_ROOT" ]; then
  ANDROID_HOME="$ANDROID_SDK_ROOT"
elif [ -d "$HOME/Android/Sdk" ]; then
  ANDROID_HOME="$HOME/Android/Sdk"
elif [ -d "$HOME/Library/Android/sdk" ]; then
  ANDROID_HOME="$HOME/Library/Android/sdk"
else
  echo "[FAIL] Android SDK location was not found. Set ANDROID_HOME or ANDROID_SDK_ROOT." >&2
  exit 1
fi
export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
echo "[PASS] Android SDK detected: $ANDROID_HOME"
