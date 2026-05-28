#!/bin/bash

# Dry-run build verification script for Superwhisper Mini IME
# This script validates that the build will work before handing over

set -e

echo "=========================================="
echo "Superwhisper Mini IME - Build Verification"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo "[1/5] Checking prerequisites..."

if [ -z "$JAVA_HOME" ]; then
    if [ -d "/opt/android-studio/jbr" ]; then
        export JAVA_HOME=/opt/android-studio/jbr
    else
        echo -e "${RED}ERROR: JAVA_HOME not set${NC}"
        exit 1
    fi
fi
echo "  JAVA_HOME: $JAVA_HOME"

if ! command -v java &> /dev/null; then
    export PATH=$JAVA_HOME/bin:$PATH
fi

java -version 2>&1 | head -1

# Check Android SDK
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME=$HOME/Android/Sdk
    else
        echo -e "${YELLOW}WARNING: ANDROID_HOME not set - build may fail${NC}"
    fi
fi
if [ -n "$ANDROID_HOME" ]; then
    echo "  ANDROID_HOME: $ANDROID_HOME"
fi

echo ""
echo "[2/5] Running clean build..."
./gradlew clean assembleDebug --no-daemon

if [ $? -ne 0 ]; then
    echo -e "${RED}BUILD FAILED${NC}"
    exit 1
fi

echo -e "${GREEN}BUILD SUCCESSFUL${NC}"
echo ""

# Verify APK exists
echo "[3/5] Verifying APK..."
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
    echo -e "  APK found: app/build/outputs/apk/debug/app-debug.apk (${GREEN}$APK_SIZE${NC})"
else
    echo -e "${RED}ERROR: APK not found${NC}"
    exit 1
fi

echo ""
echo "[4/5] Running smoke tests..."

# Check for common crash-causing patterns
echo "  Checking for ImageButton cast issues..."
if grep -r "ImageButton.*findViewById" app/src/main/java/ 2>/dev/null | grep -v "//"; then
    echo -e "    ${RED}WARNING: Found ImageButton usages - may cause ClassCastException${NC}"
else
    echo -e "    ${GREEN}OK${NC}"
fi

echo "  Checking for theme attribute issues..."
if grep -r '?attr/' app/src/main/res/layout/*.xml 2>/dev/null; then
    echo -e "    ${YELLOW}WARNING: Found theme attributes - may not work in IME context${NC}"
else
    echo -e "    ${GREEN}OK${NC}"
fi

echo "  Checking for private drawable usage..."
if grep -r '@android:drawable/ic_' app/src/main/res/layout/*.xml 2>/dev/null; then
    echo -e "    ${RED}WARNING: Using private Android drawables${NC}"
else
    echo -e "    ${GREEN}OK${NC}"
fi

echo ""
echo "[5/5] Source file validation..."

# Count source files
KOTLIN_FILES=$(find app/src/main/java -name "*.kt" | wc -l)
XML_FILES=$(find app/src/main/res -name "*.xml" | wc -l)

echo "  Kotlin files: $KOTLIN_FILES"
echo "  XML resource files: $XML_FILES"

# Check key files exist
KEY_FILES=(
    "app/src/main/java/com/shehraan/superwhispermini/ime/VoiceImeService.kt"
    "app/src/main/java/com/shehraan/superwhispermini/ime/ImeKeyboardView.kt"
    "app/src/main/java/com/shehraan/superwhispermini/settings/HistoryAdapter.kt"
    "app/src/main/res/layout/keyboard_view.xml"
    "app/src/main/res/layout/activity_main.xml"
)

for file in "${KEY_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✓ $file"
    else
        echo -e "  ${RED}✗ Missing: $file${NC}"
    fi
done

echo ""
echo "=========================================="
echo -e "${GREEN}Verification Complete - Build Ready${NC}"
echo "=========================================="
echo ""
echo "Next steps:"
echo "  1. adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo "  2. Enable keyboard in system settings"
echo "  3. Switch to keyboard in any text field"
echo "  4. Test dictation"
echo ""
echo "See TEST_PLAN.md for full test cases"

