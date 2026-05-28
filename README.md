# Superwhisper Mini IME for Android

## Why this exists
This is a focused Android proof-of-work for Superwhisper’s public Android Engineer role.
The goal is not full product parity.
The goal is to prove the Android wedge:
system-wide voice input, on-device-first speech, direct insertion, and low-friction UX.

## What it does
- Custom Android IME with mic-first UI
- Press-and-hold to dictate
- On-device-first recognizer path
- Partial transcript preview while speaking
- Two modes:
  - Voice: minimal cleanup
  - Message: conservative formatting and filler-word cleanup
- Direct insertion into focused text fields with InputConnection
- Clipboard fallback
- Local history with retry/copy
- Replacements / mini vocabulary mapping

## Why this scope
I optimized for the hiring signal in the public materials:
- build Android from scratch
- on-device ML
- system-wide voice input UX
- performance and polish
- bonus: audio / speech / input-method experience

## Architecture
- `VoiceImeService`: InputMethodService entry point
- `RecognizerEngine`: speech backend interface
- `AndroidOnDeviceRecognizer`: current implementation
- `FormatterPipeline`: Voice / Message modes + replacements
- `HistoryRepository`: local persistence for recent dictations
- `SettingsActivity`: onboarding, mode toggles, history access

## Current trade-offs
- Uses Android’s on-device recognizer path first for speed of implementation
- Does not yet ship bundled Whisper-family models
- Keeps formatting deterministic rather than cloud-LLM based
- Defers full context-aware “Super Mode,” sync, meetings, and model downloads

## How to run
1. Install APK
2. Enable the keyboard in Android settings
3. Switch to the keyboard in any text field
4. Press and hold the mic button to dictate
5. Release to finalize and insert

## Tested demo scenarios
- Messaging app
- Email / notes app
- Developer-oriented text field

## Known limitations
- On-device recognition availability depends on device/service support
- Language detection and segmented sessions vary by implementation
- Formatting is intentionally conservative
- No full parity with desktop product

## What I would build next
- `WhisperCppRecognizer` or `SherpaRecognizer`
- Downloadable local model packs
- Per-app mode activation
- Selected-text / clipboard-aware context features
- Pause / resume
- Richer mode system
- Reliability dashboards and issue reporting

## Sources I aligned against
Use the official role page, official product/docs/changelog, and public feedback threads cited in this report.

