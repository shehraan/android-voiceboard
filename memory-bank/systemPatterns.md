# System Patterns

## Architecture Pattern
Clean separation between IME lifecycle, speech recognition, and text formatting.

## Key Patterns

### RecognizerEngine Interface
Abstraction layer allows swapping from Android's recognizer to WhisperCpp or Sherpa without touching IME code.

### Formatter Pipeline
Two-stage processing: raw ->  mode-specific -> replacements

### InputCommitter
Encapsulates insertion logic with fallback strategy:
1. Try direct commitText via InputConnection
2. Fall back to clipboard if commit fails

### State Management
RecognitionState enum drives UI state:
- Idle
- Listening
- Processing
- Error

## Dependencies Flow
```
VoiceImeService -> RecognizerEngine
VoiceImeService -> FormatterPipeline
VoiceImeService -> InputCommitter
VoiceImeService -> HistoryRepository

AndroidOnDeviceRecognizer :implements RecognizerEngine
FormatterPipeline -> VoiceModeFormatter
FormatterPipeline -> MessageModeFormatter
FormatterPipeline -> ReplacementEngine
```
