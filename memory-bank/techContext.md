# Technical Context

## Tech Stack
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0) for SpeechRecognizer.createOnDeviceSpeechRecognizer()
- **Target SDK**: 34
- **Build**: Gradle with Kotlin DSL

## Key Libraries
- AndroidX Core
- SpeechRecognizer (Android built-in, on-device path)
- Room (local persistence)
- DataStore (preferences)
- ViewBinding

## Permissions Required
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## IME Configuration
- Service declared in AndroidManifest.xml
- Method.xml defines keyboard layout
- InputMethodService handles lifecycle

## Recognition Approach
Using `SpeechRecognizer.createOnDeviceSpeechRecognizer(context)` when available, with graceful degradation.

## Storage
- Room database for history
- DataStore for user preferences (mode, replacements)

## Performance Considerations
- Recognition runs on main thread callbacks (Android limitation)
- Formatting is lightweight string manipulation
- History writes are async
