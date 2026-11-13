# COD Confirm

COD Confirm is an offline-first Android app for small Instagram and WhatsApp sellers in Pakistan. It helps sellers record Cash-on-Delivery orders, prepare customer confirmation messages for WhatsApp, track fulfilment, and understand refused-parcel losses.

## Product scope

- **Phase 1:** Local-only MVP. Orders and settings are stored on-device with Room and DataStore. No backend or network code.
- **Phase 2:** Optional Supabase cloud backup, only after explicit owner approval.
- **Phase 3:** Monetization, only after explicit owner approval.

The app supports English, Urdu, and Roman Urdu message templates. It does not automate WhatsApp sending; it opens WhatsApp with a pre-filled message for the seller to send.

## Technology

- Kotlin, Jetpack Compose, Material 3
- MVVM + Clean Architecture
- Hilt with KSP
- Room and Preferences DataStore
- Coroutines and Flow
- minSdk 26
- Package / application ID: `com.codconfirm.app`

## Repository layout

The initial implementation is a single Android `app` module with package-level boundaries for `core`, `domain`, `data`, `presentation`, and `di`. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Build

Open the project in the latest compatible Android Studio and allow Gradle sync. From a shell with the Android SDK configured:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

The required quality gates are documented in [CLAUDE.md](CLAUDE.md). Local signing files, `local.properties`, and credentials must never be committed.

## Development status

See [docs/PROGRESS.md](docs/PROGRESS.md) for the phase checklist and verification status. Decisions are recorded in [docs/DECISIONS.md](docs/DECISIONS.md).

## Privacy

Version 1 stores order/customer information locally on the device. Treat customer names, phone numbers, and addresses as sensitive data: do not log them or include them in analytics or crash metadata.
