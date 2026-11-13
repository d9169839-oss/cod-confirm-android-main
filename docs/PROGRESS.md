# Development Progress

The repository-root `CLAUDE.md` is the authoritative engineering brief. A task is complete only after implementation, tests, quality gates, commit, and this checklist update.

## Phase 0 — Foundation

- [x] 0.1 Scaffold project, Gradle Kotlin DSL, version catalog, minSdk 26 (initial scaffold committed; CI verification pending)
- [x] 0.2 Configure Hilt, KSP, Room, DataStore, Compose BOM, Navigation (initial wiring committed; CI verification pending)
- [ ] 0.3 Add ktlint + detekt and make gates pass on the empty project
- [ ] 0.4 Complete package skeleton and core/common primitives
- [ ] 0.5 Add architecture tests
- [x] 0.6 Add architecture docs, CI workflow, and .gitignore (CI currently runs assemble, lint, and JVM tests)
- [x] 0.7 Add Material 3 theme and four-screen bottom-navigation shell (initial placeholders)

## Phase 1 — Local-only MVP

- [ ] 1.1 Domain models, status machine, phone value object/normalizer, tests
- [ ] 1.2 Room entities, DAOs, database, converters, schema, mappers, tests
- [ ] 1.3 Order repository interface/implementation/fake and contract tests
- [ ] 1.4 Order use cases and tests
- [ ] 1.5 Settings DataStore, repository, use cases
- [ ] 1.6 Message templates, renderer, template screen and tests
- [ ] 1.7 Orders list screen and ViewModel tests
- [ ] 1.8 Order add/edit screen, validation, customer-history banner
- [ ] 1.9 Order detail, status actions, WhatsApp effect and copy fallback
- [ ] 1.10 WhatsApp launcher interface and Android implementation
- [ ] 1.11 Stats screen and calculator tests
- [ ] 1.12 CSV export, share sheet, escaping/injection tests
- [ ] 1.13 Urdu translations, RTL pass, previews
- [ ] 1.14 Settings screen
- [ ] 1.15 Release hardening and release instructions
- [ ] 1.16 Compose UI tests for main flow

## Phase 2 — Cloud backup (requires explicit owner approval)

- [ ] 2.1 Remote data source and auth interfaces
- [ ] 2.2 Supabase SQL and RLS migrations
- [ ] 2.3 Google sign-in
- [ ] 2.4 Sync use case and WorkManager worker
- [ ] 2.5 Sign-in, sync status, and restore settings UI
- [ ] 2.6 Preserve offline and signed-out usability

## Phase 3 — Monetization (requires explicit owner approval)

- [ ] 3.1 Entitlement repository and Play Billing
- [ ] 3.2 Free tier limit policy (default 30 new orders/month)
- [ ] 3.3 Paywall; never lock existing user data

## Verification log

| Date | Scope | Result |
|---|---|---|
| 2026-11-14 | Repository access and source brief reviewed | Write permission confirmed; repository initially empty |
| 2026-11-14 | Initial Android scaffold | Files committed; no Gradle build or tests executed in this environment; CI pending |
