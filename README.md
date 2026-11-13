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

The repository includes a lightweight Gradle bootstrap script. On macOS/Linux, run:

```bash
bash ./gradlew assembleDebug
bash ./gradlew testDebugUnitTest
bash ./gradlew lintDebug
```

On Windows, use `gradlew.bat assembleDebug`, `gradlew.bat testDebugUnitTest`, or `gradlew.bat lintDebug`. The first run downloads Gradle 8.13. Android SDK setup is still required for Android tasks.

## Development status

See [docs/PROGRESS.md](docs/PROGRESS.md) for the phase checklist and verification status. Decisions are recorded in [docs/DECISIONS.md](docs/DECISIONS.md).

## Privacy

Version 1 stores order/customer information locally on the device. Android backup is currently configured to include app databases and settings for cloud backup/device transfer. Customer names, phone numbers, and addresses are sensitive data: do not log them or include them in analytics or crash metadata.
