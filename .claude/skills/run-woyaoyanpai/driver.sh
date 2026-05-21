#!/bin/bash
# 我要验牌 - ADB driver script
# Usage: bash driver.sh [command]
# Commands: install, launch, detect, screenshot, notebook, test

set -euo pipefail

ADB="${ANDROID_HOME:-$LOCALAPPDATA/Android/Sdk}/platform-tools/adb"
APK_DEBUG="app/build/intermediates/apk/debug/app-debug.apk"
APK_RELEASE="app/release/app-release.apk"
PACKAGE="com.yanpai.clipboardcleaner"
ACTIVITY="$PACKAGE/.MainActivity"
SCREENSHOT_DIR="screenshots"

mkdir -p "$SCREENSHOT_DIR"

# ---- helpers ----
choose_apk() {
    if [ -f "$APK_DEBUG" ]; then echo "$APK_DEBUG"; elif [ -f "$APK_RELEASE" ]; then echo "$APK_RELEASE"; else echo ""; fi
}

ss() { "$ADB" exec-out screencap -p > "$SCREENSHOT_DIR/$1"; echo "Screenshot: $SCREENSHOT_DIR/$1"; }
tap() { "$ADB" shell input tap "$1" "$2"; sleep 1; }

# ---- commands ----
cmd_install() {
    local apk=$(choose_apk)
    if [ -z "$apk" ]; then echo "No APK found. Build first: Android Studio → Build → Build APK(s)"; return 1; fi
    "$ADB" uninstall "$PACKAGE" 2>/dev/null || true
    "$ADB" install -r "$apk"
    echo "Installed: $apk"
}

cmd_launch() {
    "$ADB" shell am start -n "$ACTIVITY"
    sleep 3
    echo "App launched"
}

cmd_detect() {
    # Set obfuscated clipboard text and trigger detection
    local text="${1:-3月5号0点2分3更4夜}"
    "$ADB" shell cmd clipboard set "$text" 2>/dev/null || true
    sleep 1
    # Tap "检测并验牌" button (center of screen, below clipboard area)
    tap 630 700
    ss detect_result.png
}

cmd_screenshot() {
    ss "screen_$(date +%H%M%S).png"
}

cmd_notebook() {
    # Tap "打开笔记本" button (bottom center)
    tap 630 1900
    sleep 1
    ss notebook.png
}

cmd_switch_tab() {
    # Tap the second tab (视频) - roughly at x=800
    tap 800 340
    sleep 1
    ss notebook_video.png
}

cmd_toggle_theme() {
    # Tap dark mode toggle (top-right corner of app bar)
    tap 1200 80
    sleep 1
    ss theme_toggled.png
}

cmd_test() {
    echo "=== Full test: install → launch → detect → notebook ==="
    cmd_install
    cmd_launch
    cmd_detect "3月5号0点2分3更4夜"
    cmd_notebook
    cmd_switch_tab
    echo "=== Test complete. Screenshots in $SCREENSHOT_DIR/ ==="
}

cmd_devices() {
    "$ADB" devices
}

# ---- dispatch ----
case "${1:-test}" in
    install) cmd_install ;;
    launch) cmd_launch ;;
    detect) cmd_detect "${2:-}" ;;
    screenshot|ss) cmd_screenshot ;;
    notebook) cmd_notebook ;;
    switch-tab) cmd_switch_tab ;;
    toggle-theme) cmd_toggle_theme ;;
    test) cmd_test ;;
    devices) cmd_devices ;;
    *) echo "Usage: bash driver.sh [install|launch|detect|screenshot|notebook|switch-tab|toggle-theme|test|devices]" ;;
esac
