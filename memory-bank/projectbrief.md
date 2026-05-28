# Project Brief: Superwhisper Mini IME

## Goal
Build a focused Android proof-of-work for Superwhisper's Android Engineer role.

## Scope
- Custom Android IME (InputMethodService) for system-wide voice input
- On-device-first speech recognition
- Press-and-hold mic button UX
- Two formatting modes: Voice and Message
- Local history with persistence
- Minimal main activity for onboarding and settings

## Non-negotiables
- Kotlin-first
- IME-first architecture (not a notes app)
- On-device-first speech path (no cloud APIs)
- Direct insertion via InputConnection
- Clipboard fallback only
- Clean architecture for future recognizer swaps (WhisperCpp, Sherpa)

## Success Criteria
- Working demo on real phone in under 6 hours
- Can dictate into any app's text field
- Partial transcript preview while speaking
- Clean, insertable final output
