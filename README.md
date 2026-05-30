# Voiceboard - Voice IME for Android

<img src="app.jpg" width="45%" /> <img src="keyboard.jpg" width="45%" />

## Why this exists

This is a focused Android proof-of-work for a voice-first IME. The goal is not full product parity. The goal is to **prove the Android wedge**: system-wide voice input, on-device-first speech, direct insertion, and low-friction UX.

The project prioritizes a working demo over feature completeness, following the architecture rules strictly:
- Kotlin-first
- IME-first (not a notes app)
- On-device-first (no cloud APIs in core path)
- Clean architecture enabling future recognizer swaps

## What it does

- **Custom Android IME** with mic-first UI
- **Press-and-hold** to dictate - the most natural control pattern
- **On-device-first recognizer** path using Android's SpeechRecognizer
- **Partial transcript preview** while speaking
- **Two modes:**
  - Voice: minimal cleanup, punctuation insertion
  - Message: conservative formatting with filler-word cleanup
- **Direct insertion** into focused text fields with InputConnection
- **Clipboard fallback** when direct insertion fails
- **Local history** with retry/copy functionality
- **Mini replacement vocabulary** for common voice commands

## Architecture

### Package Structure

```
com.shehraan.voiceboard/
├── ime/
│   ├── VoiceImeService.kt          # InputMethodService entry point
│   ├── ImeKeyboardView.kt          # Custom keyboard UI
│   └── InputCommitter.kt           # Text insertion with fallback
├── speech/
│   ├── RecognizerEngine.kt         # Speech backend interface
│   ├── AndroidOnDeviceRecognizer.kt # Android on-device implementation
│   └── RecognitionState.kt         # State management
├── formatting/
│   ├── FormatterPipeline.kt        # Format orchestration
│   ├── VoiceModeFormatter.kt       # Minimal cleanup
│   ├── MessageModeFormatter.kt     # Conservative formatting
│   └── ReplacementEngine.kt        # Vocabulary mapping
├── history/
│   ├── DictationHistoryEntry.kt    # Data class
│   └── HistoryRepository.kt        # Room persistence
├── settings/
│   ├── MainActivity.kt             # Onboarding & history
│   └── SettingsViewModel.kt        # Settings logic
└── util/
    ├── TimeMetrics.kt              # Latency tracking
    └── Logger.kt                   # Debug logging
```

### Key Components

**VoiceImeService**
- Owns the IME lifecycle
- Handles press-and-hold mic interaction
- Shows partial transcript previews
- Commits final text via InputCommitter
- Saves dictations to history

**RecognizerEngine Interface**
```kotlin
interface RecognizerEngine {
    fun isAvailable(): Boolean
    fun start(onPartial: (String) -> Unit, onFinal: (String) -> Unit, onError: (Throwable) -> Unit)
    fun stop()
    fun cancel()
    fun destroy()
}
```

This abstraction allows swapping Android's recognizer for WhisperCpp or Sherpa-onnx without touching IME code.

**AndroidOnDeviceRecognizer**
- Uses `SpeechRecognizer.createOnDeviceSpeechRecognizer()`
- Requires minSdk 26 (Android 8.0)
- Checks `SpeechRecognizer.isOnDeviceRecognitionAvailable()` before startup
- Handles partial and final results via RecognitionListener

**FormatterPipeline**
Orchestrates formatting in two stages:
1. Apply replacements ("period" -> ".", "new line" -> "\n")
2. Apply mode-specific formatting (Voice vs Message)

**InputCommitter**
Encapsulates insertion strategy:
1. Try `inputConnection.commitText()` (primary)
2. Fall back to ClipboardManager (backup)

## Why IME-first

The core value proposition is **system-wide voice input**. A notes app would demonstrate nothing about the critical Android integration challenges. An IME proves:

1. **System integration** - Working with InputMethodService lifecycle
2. **Text insertion** - Using InputConnection properly
3. **UI constraints** - Keyboard must be compact and responsive
4. **Performance under pressure** - Recognition UI must not lag

## Why On-device-first

- **Privacy** - No audio leaves the device
- **Latency** - No network round-trips
- **Reliability** - Works offline, no API keys to manage
- **Platform alignment** - Superwhisper emphasizes on-device ML

The current implementation uses Android's on-device recognizer for speed. A production build would swap in Whisper.cpp or SherpaOnnxRecognizer behind the RecognizerEngine interface.

## Current Trade-offs

1. **Recognition engine**: Uses Android's on-device recognizer, not custom Whisper models. Enables a working demo without 500MB+ model downloads.

2. **Formatting**: Deterministic string manipulation, no LLM calls. Accuracy beats fancy rewriting at this stage.

3. **Language support**: Limited to what Android's on-device recognizer supports (typically en-US on most devices).

4. **History UI**: Minimal - just a list with copy/delete. No search, no sync.

5. **No real-time visualizer**: RMS callback exists but UI keeps it simple.

## How to Install and Demo

### Prerequisites
- Android Studio Giraffe or newer
- Android device or emulator running API 26+ (Android 8.0+)
- Microphone permission

### Build Steps

```bash
# Clone or extract project
cd Voiceboard

# Open in Android Studio or build from command line
./gradlew assembleDebug

# Install to connected device
./gradlew installDebug
```

### Demo Steps

1. **Install APK** on your Android device

2. **Open the app** - You'll see "Voiceboard" with Setup buttons

3. **Grant microphone permission** - Tap "Grant Microphone Permission"

4. **Enable keyboard** - Tap "Enable Keyboard" button (opens system settings)
   - Toggle on "Voiceboard" in the list
   - Confirm the security warning

5. **Switch to the keyboard** - Go to any app with a text field (Messages, Notes, etc.)
   - Tap the text field
   - Pull down the notification shade
   - Tap "Select keyboard" and choose "Voiceboard"
   - OR tap "Switch Keyboard" in the app

6. **Dictate** - Press and hold the large green mic button
   - Speak clearly
   - Watch the preview text appear
   - Release to finalize

7. **Try different modes** - Tap the mode toggle (speaker icon) to switch between Voice and Message mode

8. **Check history** - Open the main app to see your dictation history with latency metrics

### Test Scenarios

```
Test: Basic dictation
Input: "Hello world period"
Expected: "Hello world."

Test: Message mode formatting
Mode: Message
Input: "um I will be there at five"
Expected: "I will be there at five."

Test: Replacements
Input: "New line This is on a new line"
Expected: "\nThis is on a new line"

Test: Direct insertion
Action: Dictate into Messages app text field
Expected: Text appears directly in the field, not just copied to clipboard
```

## What I Would Build Next

### Immediate (Next Sprint)
- **WhisperCppRecognizer**: Implement the RecognizerEngine interface using whisper.cpp for better accuracy and offline first-class support
- **Per-app mode activation**: Remember preferred mode per input field context
- **Clipboard-aware context**: Detect existing text for context-aware formatting

### Short Term (Next Month)
- **Model downloads**: UI for downloading and managing local language models
- **Language switching**: Support multiple languages
- **Rich history**: Search, export, batch operations
- **Settings screen**: More granular replacement vocabulary, customization

### Longer Term
- **Pause/resume**: Mid-dictation pausing
- **Super Mode parity**: Advanced formatting, custom vocabularies
- **Sync**: Optional cloud history sync
- **Selected text handling**: Context-aware rewrite of highlighted text
- **Reliability dashboard**: Error reporting, metrics

## Technical Decisions

### Why DataStore + Room?
DataStore for simple key-value preferences, Room for structured history. Both are AndroidX standards, both handle threading correctly.

### Why Press-and-hold UX?
Tried-and-true pattern from voice assistants. Clear intent (holding = recording), natural release-to-send.

### Why Two Modes?
Different contexts need different cleanup. Voice mode keeps filler words for natural speech patterns. Message mode cleans up for text communication.

### Why No View Binding in IME?
IME uses findViewById for code clarity. MainActivity uses ViewBinding as per standard Android practices.

## Sources Consulted

- Architecture rules: `/home/sh/code/projects/superWhisper/.clinerules/architecture.md`
- Android docs: InputMethodService, SpeechRecognizer
- Memory bank: `memory-bank/` directory for context tracking

## Contact / Feedback

This is a proof-of-work. For questions about the implementation approach or architecture decisions, refer to the inline comments and architecture documentation.
