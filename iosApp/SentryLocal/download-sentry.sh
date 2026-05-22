#!/bin/sh
# Downloads Sentry.xcframework 9.14.0 into this directory (gitignored, ~62MB)
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
ZIP="$DIR/Sentry.xcframework.zip"
curl -L -o "$ZIP" "https://github.com/getsentry/sentry-cocoa/releases/download/9.14.0/Sentry.xcframework.zip"
echo "252b05ff468b0066047a4a293b8eb0ca630f5ac23416b07f3e8ae08913a311d5  $ZIP" | shasum -a 256 -c -
unzip -q "$ZIP" -d "$DIR"
rm "$ZIP"
echo "Done — Sentry.xcframework is ready."
