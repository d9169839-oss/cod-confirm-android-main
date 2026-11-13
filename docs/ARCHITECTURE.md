# Architecture

This document summarizes the engineering contract in the repository-root `CLAUDE.md`. That file is authoritative if this summary is incomplete.

## Product boundary

Version 1 is local-first and has no backend or network code. Room is the source of truth for orders; Preferences DataStore stores shop settings and preferences. Supabase is a later, explicitly gated phase.

## Layers and dependency direction

```text
presentation (Compose screens, ViewModels, UI state/effects)
       | depends on
       v
domain (pure Kotlin models, rules, use cases, repository interfaces)
       ^
       | implemented by
data (Room, DataStore, entities, mappers, repository implementations)

core: shared primitives; di: composition root
```

Allowed dependencies:
- presentation -> domain, core
- data -> domain, core
- domain -> pure-Kotlin core only
- di -> all layers

Forbidden dependencies include domain -> data/presentation, presentation -> data, data -> presentation, and any layer -> di.

## Presentation contract

Each screen uses:
- Immutable `XxxUiState`
- `XxxUiEvent` for user actions
- `XxxUiEffect` for one-shot navigation, snackbar, WhatsApp, or share actions
- ViewModel exposing StateFlow and invoking use cases only
- Stateful screen collecting state and effects
- Stateless, previewable content composable

ViewModels must not depend on Android Context, Activity, or Compose types.

## Persistence

- Room database with schema export enabled and committed schemas.
- Explicit migrations; never use destructive migration fallback.
- DAO observation uses Flow; one-shot operations are suspend functions.
- Store timestamps as epoch milliseconds / Instant through converters.
- Soft delete records; exclude deleted records from normal reads.
- Store sync state on the data entity from day one, but do not implement syncing in Phase 1.
- DataStore is reserved for shop name, default message language, return cost, and first-run state.

## Core domain invariants

- Order lifecycle: PENDING_CONFIRMATION -> CONFIRMED -> DISPATCHED -> DELIVERED or REFUSED. Cancellation is permitted from pending confirmation and confirmed.
- Every status transition is validated and timestamped.
- Pakistani mobile numbers normalize to E.164 (+92...) before entering the domain.
- Customer history is private to the seller's own local database.
- Refusal rate is REFUSED / (DELIVERED + REFUSED), with zero when there are no closed outcomes.
- Estimated refusal loss is refused order count multiplied by configured return cost (default Rs. 400).
- CSV export must quote/escape correctly and mitigate spreadsheet formula injection.

## UX and localization

Bottom navigation: Orders, Stats, Templates, Settings. Use Material 3, minimum 48dp touch targets, start/end layout directions, stable LazyColumn keys, and accessible descriptions. All user-facing strings belong in default English resources and Urdu translations in `values-ur`.

## Security and privacy

No PII in logs, crash metadata, or analytics. Minimal permissions only. No secrets in Git. Release builds enable R8 and resource shrinking. WhatsApp is launched with an ACTION_VIEW deep link and a copy-message fallback; the app does not send messages automatically.
