#!/usr/bin/env sh
set -eu

GRADLE_VERSION="8.13"
GRADLE_USER_HOME_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}"
GRADLE_INSTALL_ROOT="$GRADLE_USER_HOME_DIR/cod-confirm-wrapper"
GRADLE_HOME="$GRADLE_INSTALL_ROOT/gradle-$GRADLE_VERSION"
GRADLE_EXEC="$GRADLE_HOME/bin/gradle"

if [ ! -x "$GRADLE_EXEC" ]; then
    mkdir -p "$GRADLE_INSTALL_ROOT"
    ARCHIVE="$GRADLE_INSTALL_ROOT/gradle-$GRADLE_VERSION-bin.zip"
    TEMP_DIR="$GRADLE_INSTALL_ROOT/.extract"
    if command -v curl >/dev/null 2>&1; then
        curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
        wget "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -O "$ARCHIVE"
    else
        echo "curl or wget is required to bootstrap Gradle." >&2
        exit 1
    fi
    rm -rf "$TEMP_DIR"
    mkdir -p "$TEMP_DIR"
    unzip -q "$ARCHIVE" -d "$TEMP_DIR"
    rm -rf "$GRADLE_HOME"
    mv "$TEMP_DIR/gradle-$GRADLE_VERSION" "$GRADLE_HOME"
    rm -rf "$TEMP_DIR" "$ARCHIVE"
fi

exec "$GRADLE_EXEC" "$@"
