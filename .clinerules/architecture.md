# Architecture Rules: Superwhisper Android Mini IME

## Goal

Build a focused Android proof-of-work for Superwhisper's Android Engineer role.

The role asks for:
- Android app built from scratch
- fast on-device ML model performance
- voice input UI/UX
- Kotlin and strong Android development
- performance-focused product ownership

Source signal: Superwhisper's attached Android role description emphasizes building the Android app from the ground up, on-device ML, voice input UX, and product collaboration. :contentReference[oaicite:0]{index=0}

## Product Thesis

This project should prove the Android wedge: system-wide voice input.

Do not build a generic notes app.
Do not build a big transcription dashboard.
Do not build a fake Superwhisper clone.

Build a minimal custom Android keyboard/IME that lets the user dictate into any focused text field.

Core loop:
1. User opens any app with a text field.
2. User switches to this custom keyboard.
3. User presses and holds a mic button.
4. App records/transcribes with on-device-first speech recognition.
5. Partial transcript appears while speaking.
6. On release, text is lightly cleaned.
7. Final text is inserted directly into the focused field.

## Non-negotiables

- Kotlin-first.
- Android IME-first using InputMethodService.
- On-device-first speech path.
- Direct insertion using currentInputConnection.commitText().
- Clipboard is fallback only.
- No cloud STT or LLM calls unless explicitly placed behind a debug-only fallback.
- Keep formatting conservative. Accuracy beats fancy rewriting.
- Prioritize a working demo over feature count.

## Recommended Android APIs

- InputMethodService for keyboard surface.
- SpeechRecognizer.createOnDeviceSpeechRecognizer() for first recognizer backend.
- SpeechRecognizer.isOnDeviceRecognitionAvailable() before startup.
- RecognitionListener for partial and final results.
- RecognizerIntent.EXTRA_PARTIAL_RESULTS for live preview.
- InputConnection.commitText() for insertion.
- ClipboardManager only as fallback.
- SharedPreferences or DataStore for mode and replacements.
- Room, JSON, or SharedPreferences for minimal local history.

## Suggested Package Structure

app/src/main/java/com/shehraan/superwhispermini/

- ime/
  - VoiceImeService.kt
  - ImeKeyboardView.kt
  - InputCommitter.kt

- speech/
  - RecognizerEngine.kt
  - AndroidOnDeviceRecognizer.kt
  - RecognitionState.kt

- formatting/
  - FormatterPipeline.kt
  - VoiceModeFormatter.kt
  - MessageModeFormatter.kt
  - ReplacementEngine.kt

- history/
  - DictationHistoryEntry.kt
  - HistoryRepository.kt

- settings/
  - MainActivity.kt
  - SettingsViewModel.kt

- util/
  - TimeMetrics.kt
  - Logger.kt

## Architecture

### VoiceImeService

Responsibilities:
- Own the IME lifecycle.
- Inflate the mic-first keyboard UI.
- Start recognition on press down.
- Stop recognition on release.
- Show partial transcript.
- Commit final text into focused field.
- Save local history.

Do not put formatting or recognizer internals directly in this class.

### RecognizerEngine

Use an interface so the backend can later swap from Android SpeechRecognizer to whisper.cpp or sherpa-onnx.

Interface shape:

```kotlin
interface RecognizerEngine {
    fun isAvailable(): Boolean
    fun start(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (Throwable) -> Unit
    )
    fun stop()
    fun cancel()
    fun destroy()
}
