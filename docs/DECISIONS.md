# Architecture Decision Log

Decisions are recorded as short ADRs. Product scope remains governed by `CLAUDE.md`.

## ADR-0001: Single-module, package-layered architecture

**Context:** The initial product is a focused Android MVP and the brief explicitly recommends a single app module with package boundaries.

**Decision:** Start with one `:app` module and enforce `core`, `domain`, `data`, `presentation`, and `di` package boundaries. Reconsider Gradle modularization only when build time becomes painful or a second developer joins.

**Consequences:** Faster initial setup while preserving clear dependency direction and a mechanical path to future modules.

## ADR-0002: Local-first persistence

**Context:** Sellers must be able to use the core workflow without connectivity. Phase 1 explicitly prohibits network code.

**Decision:** Room is the order source of truth and Preferences DataStore stores settings. Do not add Supabase, authentication, INTERNET permission, or sync workers in Phase 1.

**Consequences:** Core order operations remain available offline. Cloud backup requires a separately approved Phase 2.

## ADR-0003: Keep sync readiness out of the domain model

**Context:** The brief asks for sync state to be persisted from day one while classifying it as a data-layer concern.

**Decision:** Persist `LOCAL_ONLY`, `DIRTY`, or `SYNCED` on the Room entity, not on the domain `Order`.

**Consequences:** Later synchronization can be introduced without exposing persistence mechanics to business rules.

## ADR-0004: Privacy-conscious local data handling

**Context:** Orders contain customer names, phone numbers, and addresses.

**Decision:** Do not log PII, request unnecessary permissions, or add analytics. Configure backup behavior deliberately and document it before release.

**Consequences:** Sensitive order data stays out of diagnostic telemetry; backup inclusion must be disclosed to users.

## Open verification items

- Confirm current stable Android Gradle Plugin, Kotlin, Compose BOM, Hilt/KSP, Room, DataStore, Navigation, and test dependency compatibility before pinning versions.
- Select and document one ktlint integration (Spotless or ktlint Gradle plugin) alongside the detekt configuration.
- Confirm target SDK and required JDK against the selected stable Android Gradle Plugin.
