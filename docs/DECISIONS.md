# Architecture Decision Log

Decisions are recorded as short ADRs. Product scope remains governed by `CLAUDE.md`.

## ADR-0001: Single-module, package-layered architecture

**Context:** The initial product is a focused Android MVP and the brief explicitly recommends a single app module with package boundaries.

**Decision:** Start with one `:app` module and enforce `core`, `domain`, `data`, `presentation`, and `di` package boundaries. Reconsider Gradle modularization only when build time becomes painful or a second developer joins.

**Consequences:** Faster initial setup while preserving clear dependency direction and a mechanical path to future modules.

## ADR-0002: Local-first persistence

**Context:** Sellers must be able to use the core workflow without connectivity. Phase 1 explicitly prohibits network code.

**Decision:** Room is the order source of truth and Preferences DataStore stores settings. Do not add Supabase, authentication, INTERNET permission, or sync workers in Phase 1. Room runtime/compiler dependencies and schema export configuration are pinned now; create the concrete database only alongside the first real entities in task 1.2.

**Consequences:** Core order operations remain available offline. Avoids introducing an invalid empty Room database before the order schema exists. Cloud backup requires a separately approved Phase 2.

## ADR-0003: Keep sync readiness out of the domain model

**Context:** The brief asks for sync state to be persisted from day one while classifying it as a data-layer concern.

**Decision:** Persist `LOCAL_ONLY`, `DIRTY`, or `SYNCED` on the Room entity, not on the domain `Order`.

**Consequences:** Later synchronization can be introduced without exposing persistence mechanics to business rules.

## ADR-0004: Privacy-conscious local data handling

**Context:** Orders contain customer names, phone numbers, and addresses.

**Decision:** Do not log PII, request unnecessary permissions, or add analytics. Configure Android cloud backup and device-transfer inclusion explicitly using XML backup rules; document that order data may be included in device backup.

**Consequences:** Sensitive order data stays out of diagnostic telemetry. The app currently opts into backup inclusion, which must be disclosed in the privacy policy before release.

## ADR-0005: Stable baseline Android toolchain

**Context:** The brief requires dependency versions to be checked rather than guessed. Official Kotlin compatibility documentation lists Kotlin Gradle Plugin 2.4.21 as fully supported with Gradle 7.6.3–9.7.0 and AGP 8.5.2–9.3.1. Android's AGP release documentation lists AGP 8.13.2 with Gradle 8.13.

**Decision:** Pin AGP 8.13.2, Gradle 8.13, Kotlin 2.4.21, KSP 2.3.10, Hilt 2.60.1, Room 2.8.5, Navigation 2.10.2, and Compose BOM 2026.08.00 in the version catalog. Java toolchain target is 17.

**Consequences:** Versions are centralized and reproducible. Validate the complete plugin/dependency matrix with CI before treating the foundation as complete.

## Open verification items

- Confirm the complete Kotlin/KSP/AGP/Hilt plugin matrix through a clean CI build.
- Select and document one ktlint integration (Spotless or ktlint Gradle plugin) alongside the detekt configuration.
- Revisit Android backup inclusion and privacy-policy wording before Play release.
