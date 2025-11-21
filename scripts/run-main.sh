#!/usr/bin/env bash
set -euo pipefail

SRC_DIR="backend/main"
OUT_DIR="out"
SOURCES_FILE="/tmp/mybank_sources.txt"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# collect all .java sources under the backend/main source tree
find "$SRC_DIR" -name "*.java" > "$SOURCES_FILE"

if [ ! -s "$SOURCES_FILE" ]; then
  echo "No Java sources found in $SRC_DIR" >&2
  exit 1
fi

# compile using javac @filelist (no extra spaces)
javac -d "$OUT_DIR" @"$SOURCES_FILE"

# run the main class
java -cp "$OUT_DIR" main.application.Main

# cleanup
rm -f "$SOURCES_FILE"