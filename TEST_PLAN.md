# Superwhisper Mini IME - Test Plan

## Build Verification Commands

### Prerequisites
```bash
# Set Java and Android SDK paths
export JAVA_HOME=/opt/android-studio/jbr
export ANDROID_HOME=$HOME/Android/Sdk

# Verify Java version
java -version

# Verify Gradle
./gradlew --version
```

### Build Commands
```bash
# Clean build
./gradlew clean assembleDebug

# Build with no-daemon (recommended for CI)
./gradlew assembleDebug --no-daemon

# Verify APK exists
ls -la app/build/outputs/apk/debug/app-debug.apk
```

## Manual Test Cases

### Test Case 1: App Installation
**Steps:**
1. `adb install -r app/build/outputs/apk/debug/app-debug.apk`
2. Verify app appears in launcher
3. Open app

**Expected Result:**
- App opens without crash
- Shows "Superwhisper Mini" with setup buttons

### Test Case 2: Microphone Permission
**Steps:**
1. Tap "Grant Microphone Permission"
2. Allow permission
3. Button state should update

**Expected Result:**
- Permission dialog appears
- Button text changes to "Microphone Permission Granted"
- Button becomes disabled

### Test Case 3: Keyboard Enablement
**Steps:**
1. Tap "Enable Keyboard"
2. Toggle "Superwhisper Mini" in system settings
3. Return to app

**Expected Result:**
- Opens system settings
- Keyboard becomes enabled
- "Enable Keyboard" button becomes disabled

### Test Case 4: Keyboard Usage
**Steps:**
1. Open any app with text field (e.g., Messages, Notes)
2. Tap text field
3. Tap keyboard switch icon in navigation bar
4. Select "Superwhisper Mini"

**Expected Result:**
- Keyboard appears with:
  - Preview text area (top)
  - Settings button (S)
  - Mode toggle (M)
  - Mic button (🎤) - green by default
  - Backspace button (⌫)
  - Status text (shows "Voice Mode")

### Test Case 5: Settings Button
**Steps:**
1. With keyboard showing, tap Settings button (S)

**Expected Result:**
- MainActivity opens
- Shows setup screen with history

### Test Case 6: Mode Toggle
**Steps:**
1. Tap Mode toggle button (M) multiple times

**Expected Result:**
- Status text changes: "Voice Mode" ↔ "Message Mode"
- Logs show: "Mode toggled to: VOICE/MESSAGE"

### Test Case 7: Voice Dictation
**Steps:**
1. Hold Mic button (🎤)
2. Speak clearly
3. Watch preview text
4. Release button
5. Observe text insertion

**Expected Result:**
- Mic button changes to 🎙️ (red background)
- Status shows "Listening..."
- Preview text shows partial transcription
- On release, formatted text inserts into focused field
- "Text committed directly" appears in logs

### Test Case 8: Backspace
**Steps:**
1. Type some text in a field
2. With keyboard showing, tap Backspace button

**Expected Result:**
- Last character deleted from current field

### Test Case 9: Mode Formatting
**Steps:**
1. In Message Mode, dictate: "um I will be there"
2. In Voice Mode, dictate same phrase
3. Compare outputs

**Expected Result:**
- Message Mode: "I will be there." (filler removed, period added)
- Voice Mode: "um I will be there" (original transcription)

### Test Case 10: History Recording
**Steps:**
1. Complete several successful dictations
2. Open MainActivity
3. Check history list

**Expected Result:**
- History items show:
  - Final text
  - Mode (Voice/Message)
  - Status (OK/Failed)
  - Latency (ms)
  - Timestamp
- Copy button works
- Delete button works

## Error Handling Tests

### Test Case 11: Recognition Error Display
**Steps:**
1. Start dictation
2. Release mic immediately without speaking
3. Observe error handling

**Expected Result:**
- Keyboard shows brief error message in red
- No crash, keyboard remains usable

### Test Case 12: Network Unavailable (on-device)
**Steps:**
1. Enable airplane mode
2. Complete dictation

**Expected Result:**
- Still works (on-device recognition)
- No network errors

## Regression Tests

### Test Case 13: No Layout/SystemTheme Errors
**Steps:**
1. Verify keyboard never crashes with:
   - java.lang.UnsupportedOperationException
   - Theme attribute errors
   - ClassCastException

### Test Case 14: Settings Button No Crash
**Steps:**
1. Repeatedly tap Settings button
2. Navigate back and forth

**Expected Result:**
- MainActivity opens without ClassCastException
- No MaterialButton/ImageButton cast errors

## Quick Smoke Test Script

```bash
# Build
./gradlew clean assembleDebug --no-daemon

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Clear previous crash logs
adb logcat -c

# Launch app
adb shell am start com.shehraan.superwhispermini/.settings.MainActivity

# Watch logs
adb logcat -s "SuperwhisperMini:SuperwhisperMini:*" | grep -E "(FATAL|ERROR|VoiceImeService|ImeKeyboard)"
```

## Known Limitations

1. **Settings button keyboard issue**: Fixed - was ClassCastException with MaterialButton/ImageButton
2. **Recognizer busy errors**: Occasional, handled gracefully
3. **Partial results**: May not show on all devices (Android limitation)

## Checklist Before Release

- [ ] Build succeeds without errors
- [ ] APK size < 10MB
- [ ] No hardcoded credentials
- [ ] All permissions declared in manifest
- [ ] No debug logging in release
- [ ] Tested on Android 12+ device
- [ ] Keyboard UI renders correctly
- [ ] Settings opens without crash
- [ ] Dictation inserts text
- [ ] Backspace works
- [ ] Mode toggle works
- [ ] History saves and displays

