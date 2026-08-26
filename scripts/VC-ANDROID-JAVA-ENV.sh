#!/usr/bin/env sh
set -eu
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  echo "[PASS] Java detected: $JAVA_HOME"
  exit 0
fi
if command -v java >/dev/null 2>&1; then
  echo "[PASS] Java detected on PATH."
  exit 0
fi
echo '[FAIL] A Java 17+ runtime was not found.' >&2
exit 1
