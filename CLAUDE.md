# CLAUDE.md — COD Confirm (Android)

> **This file is your complete brief.** Read it fully, then start working. Do not ask the owner to explain the project. Make decisions using the rules below, record them in `docs/DECISIONS.md`, and ask a question only if you hit something listed in section 19 ("When to stop and ask").

---

## 1. Start here (bootstrap protocol)

Do these in order, in your first session:

1. Read this entire file.
2. If `COD_Confirm_Development_Plan.md` exists in the repo root or `docs/`, read it too. It holds the product roadmap and background. If it conflicts with this file, **this file wins on engineering rules**; the plan wins on product scope.
3. Inspect the repository state (`git status`, file tree). If it is empty or a bare Android Studio template, scaffold the project per section 7.
4. Create `docs/PROGRESS.md` (checklist from section 16) and `docs/DECISIONS.md` (empty ADR log).
5. Work **one task at a time** through section 16. For each task: implement, write tests, run quality gates (section 13), commit, tick the box in `PROGRESS.md`.
6. Stop at the end of each phase with a short summary and wait for review.

**Default package / applicationId:** `com.codconfirm.app` (the owner can rename later; keep it in one place, `build.gradle.kts` + a single refactor).

---

## 2. Product in one paragraph

COD Confirm is an Android app for small Instagram/WhatsApp sellers (Pakistan first). The seller logs a Cash-on-Delivery order, taps one button to open WhatsApp with a pre-filled confirmation message (English / Urdu / Roman Urdu), marks the order Confirmed when the buyer replies, and tracks it through Dispatched to Delivered or Refused. The app shows refusal rate and money lost to refused parcels. **v1 is local-first with no backend.** Cloud backup (Supabase) comes in a later phase behind interfaces.

### Non-goals for v1 (do NOT build)
- Crowd-sourced/shared phone reputation (privacy and legal risk)
- WhatsApp Business API or automated message sending
- Courier API integrations
- iOS, multi-user/staff accounts
- Any AI/LLM feature
- Any network code before Phase 2

---

## 3. Non-negotiable rules

1. **Architecture:** MVVM + Clean Architecture layering (section 5). No exceptions.
2. **SOLID** applied as in section 6. Violations are bugs.
3. **Unidirectional data flow:** UI renders `UiState`, sends `UiEvent`s; ViewModel exposes `StateFlow<UiState>` and one-shot `UiEffect`s.
4. **Domain layer is pure Kotlin.** No `android.*`, no Room, no Hilt annotations beyond `@Inject` constructors, no Compose imports.
5. **No business logic in composables, activities or DAOs.**
6. **Kotlin only, Compose only** (no XML layouts; XML only for manifest/resources/themes).
7. **Every user-facing string** lives in `strings.xml` with an Urdu translation in `values-ur/`. No hard-coded UI text.
8. **No secrets in git.** Signing keys and API keys come from `local.properties` / environment variables.
9. **No dependency versions from memory.** Look up current stable versions when adding a dependency and put them in `gradle/libs.versions.toml`. Never hard-code versions in module build files.
10. **Tests are part of "done."** A task without tests is not finished.
11. **Never log PII** (names, phones, addresses) in release builds.
12. **Minimal permissions.** v1 needs none beyond what Android grants by default (`INTERNET` is added only in Phase 2).
13. **Small, reviewable commits** using Conventional Commits (section 14).

---

## 4. Tech stack

| Concern | Choice |
|---|---|
| Language | Kotlin (latest stable), coroutines + Flow |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Architecture | MVVM + Clean Architecture (presentation / domain / data) |
| DI | Hilt (KSP) |
| Local DB | Room (KSP), schema export ON, explicit migrations |
| Settings | Preferences DataStore |
| Background (Phase 2+) | WorkManager (with Hilt worker factory) |
| Backend (Phase 2+) | Supabase via supabase-kt (behind interfaces) |
| Logging | Timber (debug tree only; release tree reports non-PII crashes) |
| Crash reporting | Firebase Crashlytics (release only; add when `google-services.json` is provided) |
| Static analysis | detekt + ktlint (via Spotless or the ktlint Gradle plugin; pick one and document in DECISIONS) |
| Testing | JUnit 4/5, kotlin-test, Turbine (Flow), kotlinx-coroutines-test, Compose UI test, Konsist (architecture tests) |
| minSdk / target | **minSdk 26**; target/compile = latest stable SDK |
| Build | Gradle Kotlin DSL, version catalog, convention in `build-logic` only if/when modularized |

---

## 5. Architecture

### 5.1 Layers and dependency rule

```
┌────────────────────────────────────────────────────────────┐
│ presentation   (Compose UI, ViewModels, UiState, mappers)  │
│    depends on ──────────────────────────┐                  │
├─────────────────────────────────────────▼──────────────────┤
│ domain         (models, use cases, repository INTERFACES,  │
│                 business rules)  ← pure Kotlin             │
├─────────────────────────────────────────▲──────────────────┤
│ data           (Room, DataStore, repository IMPLEMENTATIONS│
│    depends on ──────────────────────────┘ remote later)    │
└────────────────────────────────────────────────────────────┘
          core/ (cross-cutting: dispatchers, result, clock, logging, util)
          di/   (Hilt modules wiring interfaces to implementations)
```

**Allowed imports**
- `presentation` → `domain`, `core`
- `data` → `domain`, `core`
- `domain` → `core` (only the pure-Kotlin parts)
- `di` → everything (it is the composition root)
- **Forbidden:** `domain` → `data` or `presentation`; `presentation` → `data`; `data` → `presentation`; any layer → `di`.

### 5.2 MVVM contract (every screen follows this exactly)

| Piece | Responsibility |
|---|---|
| `XxxUiState` | Immutable data class or sealed interface describing everything the screen shows |
| `XxxUiEvent` | Sealed interface of user actions (`OnSaveClicked`, `OnPhoneChanged(...)`) |
| `XxxUiEffect` | One-shot side effects (navigate, snackbar, open WhatsApp) delivered via `Channel`/`SharedFlow` |
| `XxxViewModel` | Holds state, handles events by calling **use cases only**, maps domain → UI models |
| `XxxScreen` (stateful) | Collects state with `collectAsStateWithLifecycle`, forwards events, handles effects |
| `XxxContent` (stateless) | Pure composable taking `state` + `onEvent`; previewable; no ViewModel reference |

ViewModels never reference Android `Context`, `Activity`, or Compose types. Anything needing Android (opening WhatsApp, sharing a CSV) is an **effect** executed by the screen layer, or an injected interface implemented in `data`/`core` with an application context.

### 5.3 Data flow

```
User → Composable → UiEvent → ViewModel → UseCase → Repository (interface)
                                                         │
                                           Repository impl → Room DAO / DataStore
                                                         │
Composable ← UiState ← ViewModel ← Flow<Domain> ←────────┘
```

### 5.4 Model separation (three models, mapped explicitly)

| Model | Lives in | Example |
|---|---|---|
| Entity (persistence) | `data/local/entity` | `OrderEntity` (Room annotations) |
| Domain model | `domain/model` | `Order` (no annotations) |
| UI model | `presentation/.../model` | `OrderListItemUi` (formatted strings, ready to render) |

Mappers are small extension functions or dedicated mapper classes: `OrderEntity.toDomain()`, `Order.toEntity()`, `Order.toListItemUi(formatter)`. Never leak entities into the UI or domain.

### 5.5 Error handling

- Domain/data functions that can fail expected failures return `AppResult<T>`; they do not throw for expected cases (validation, not found, storage failure).
- Unexpected exceptions are caught at the boundary (repository / use case) and converted to `AppError.Unknown`; coroutine `CancellationException` is **always rethrown**.
- ViewModels map `AppError` to a UI message resource id, never to raw exception text.

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data class Validation(val field: ValidationField, val reason: ValidationReason) : AppError
    data object NotFound : AppError
    data object InvalidStatusTransition : AppError
    data object Storage : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}
```

### 5.6 Concurrency

- Inject a `DispatcherProvider` (interface in `core`); **never** hard-code `Dispatchers.IO` in classes.
- Room and DataStore calls are main-safe; repositories use `withContext(dispatchers.io)` only where needed.
- ViewModels expose state with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Inject a `Clock` (interface or `java.time.Clock`) wherever time is read; never call `System.currentTimeMillis()` in domain/use cases.

---

## 6. SOLID — how it is applied here (be literal)

| Principle | Rule in this codebase | Example |
|---|---|---|
| **S**ingle Responsibility | One use case = one business action. One class = one reason to change. ViewModels orchestrate; they don't contain rules. | `AdvanceOrderStatusUseCase` only handles status changes; `RenderMessageTemplateUseCase` only builds message text |
| **O**pen/Closed | Add behavior by adding classes, not editing working ones. Use strategy/polymorphism instead of growing `when` chains. | New template language = new `MessageTemplate` data, no code change. A new sync backend = new `OrderRemoteDataSource` implementation |
| **L**iskov Substitution | Any implementation of an interface must be swappable without callers noticing. Fakes used in tests must obey the same contract as production classes. | `FakeOrderRepository` and `OrderRepositoryImpl` both satisfy `OrderRepository` contract tests |
| **I**nterface Segregation | Small, role-based interfaces. Don't make one giant `Repository`. | `OrderReader`, `OrderWriter` may be split if a consumer needs only one; separate `SettingsRepository` and `TemplateRepository` |
| **D**ependency Inversion | High-level code depends on abstractions defined in `domain`; `data` implements them; Hilt binds them. | `ObserveOrdersUseCase` depends on `OrderRepository` (interface), never on `OrderRepositoryImpl` or `OrderDao` |

Additional rules that enforce this:
- Constructor injection only. No service locators, no `object` singletons holding state, no `lateinit` dependencies.
- Prefer composition over inheritance. No deep base-class hierarchies. A tiny `BaseViewModel` is allowed **only** for shared effect-channel plumbing.
- Depend on interfaces for anything with side effects: time, dispatchers, WhatsApp launching, file export, preferences.
- Functions do one thing, ≤ ~30 lines. Classes ≤ ~250 lines. Composables ≤ ~80 lines (split into smaller composables).

---

## 7. Project structure (package-by-feature inside layers)

Start as a **single `app` module** with strict package boundaries (fast to build, easy to refactor). The structure is deliberately module-ready (see section 7.2).

```
app/
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── kotlin/com/codconfirm/app/
    │   │   ├── CodConfirmApplication.kt          # @HiltAndroidApp, Timber init
    │   │   ├── MainActivity.kt                   # single activity, hosts NavHost
    │   │   │
    │   │   ├── core/                             # cross-cutting, no feature logic
    │   │   │   ├── common/   AppResult.kt, AppError.kt, DispatcherProvider.kt, ClockProvider.kt
    │   │   │   ├── logging/  ReleaseTree.kt
    │   │   │   └── util/     (pure helpers only)
    │   │   │
    │   │   ├── domain/                           # PURE KOTLIN
    │   │   │   ├── model/        Order.kt, OrderStatus.kt, CustomerHistory.kt, MessageTemplate.kt,
    │   │   │   │                 OrderStats.kt, ShopSettings.kt, PhoneNumber.kt
    │   │   │   ├── repository/   OrderRepository.kt, TemplateRepository.kt, SettingsRepository.kt
    │   │   │   ├── usecase/
    │   │   │   │   ├── order/    CreateOrderUseCase, UpdateOrderUseCase, DeleteOrderUseCase,
    │   │   │   │   │             ObserveOrdersUseCase, ObserveOrderUseCase,
    │   │   │   │   │             AdvanceOrderStatusUseCase, GetCustomerHistoryUseCase
    │   │   │   │   ├── template/ ObserveTemplatesUseCase, RenderMessageUseCase
    │   │   │   │   ├── stats/    ObserveStatsUseCase
    │   │   │   │   ├── export/   BuildOrdersCsvUseCase
    │   │   │   │   └── settings/ ObserveSettingsUseCase, UpdateSettingsUseCase
    │   │   │   └── validation/   PhoneNormalizer.kt, OrderValidator.kt, OrderStatusMachine.kt
    │   │   │
    │   │   ├── data/
    │   │   │   ├── local/
    │   │   │   │   ├── db/       AppDatabase.kt, Converters.kt, Migrations.kt
    │   │   │   │   ├── dao/      OrderDao.kt, TemplateDao.kt
    │   │   │   │   ├── entity/   OrderEntity.kt, MessageTemplateEntity.kt
    │   │   │   │   └── datastore/ SettingsDataSource.kt
    │   │   │   ├── mapper/       OrderMappers.kt, TemplateMappers.kt, SettingsMappers.kt
    │   │   │   ├── repository/   OrderRepositoryImpl.kt, TemplateRepositoryImpl.kt, SettingsRepositoryImpl.kt
    │   │   │   └── export/       CsvFileWriter.kt (Android file/FileProvider details)
    │   │   │
    │   │   ├── presentation/
    │   │   │   ├── navigation/   AppNavHost.kt, Destinations.kt
    │   │   │   ├── designsystem/ theme/ (Color, Type, Theme), components/ (AppButton, StatusChip, EmptyState…)
    │   │   │   ├── orders/
    │   │   │   │   ├── list/     OrderListScreen, OrderListViewModel, OrderListUiState/Event/Effect
    │   │   │   │   ├── edit/     OrderEditScreen, OrderEditViewModel, …
    │   │   │   │   ├── detail/   OrderDetailScreen, OrderDetailViewModel, …
    │   │   │   │   └── model/    OrderListItemUi.kt, OrderUiMapper.kt
    │   │   │   ├── stats/        StatsScreen, StatsViewModel, …
    │   │   │   ├── templates/    TemplatesScreen, TemplatesViewModel, …
    │   │   │   ├── settings/     SettingsScreen, SettingsViewModel, …
    │   │   │   └── platform/     WhatsAppLauncher.kt (interface) + AndroidWhatsAppLauncher.kt
    │   │   │
    │   │   └── di/               DatabaseModule, RepositoryModule, DispatcherModule,
    │   │                         DataStoreModule, PlatformModule
    │   └── res/
    │       ├── values/strings.xml            # English (default)
    │       ├── values-ur/strings.xml         # Urdu
    │       ├── xml/                          # backup_rules.xml, data_extraction_rules.xml, file_paths.xml
    │       └── ...
    ├── test/            # JVM unit tests, mirrors main package tree
    ├── androidTest/     # instrumented + Compose UI tests
    └── debug/ release/  # variant-specific config if needed
config/detekt/detekt.yml
docs/  PROGRESS.md  DECISIONS.md  ARCHITECTURE.md
.github/workflows/ci.yml
gradle/libs.versions.toml
```

### 7.1 Naming conventions
- Use cases: verb phrase + `UseCase` (`AdvanceOrderStatusUseCase`), single `operator fun invoke`.
- Repository interface `OrderRepository`; implementation `OrderRepositoryImpl`.
- Test fakes: `FakeOrderRepository`, in `test/.../fake/` (shared via a `testFixtures` source set if modularized).
- Compose: screen = `XxxScreen`, stateless body = `XxxContent`, previews = `XxxPreview`.
- Resources: `snake_case`; string keys prefixed by feature (`orders_title`, `order_edit_phone_label`).

### 7.2 Future modularization trigger
Split into Gradle modules (`:core:*`, `:domain`, `:data`, `:feature:*`) **only when** build time becomes painful or a second developer joins. The layer rules and package layout above mean this is a mechanical move. Do not modularize in Phase 1.

---

## 8. Domain specification (implement exactly)

### 8.1 Order status machine

```
PENDING_CONFIRMATION → CONFIRMED → DISPATCHED → DELIVERED
        │                  │            │
        └──→ CANCELLED     └──→ CANCELLED
                                        └──→ REFUSED
```

```kotlin
enum class OrderStatus {
    PENDING_CONFIRMATION, CONFIRMED, DISPATCHED, DELIVERED, REFUSED, CANCELLED;

    val allowedNext: Set<OrderStatus> get() = when (this) {
        PENDING_CONFIRMATION -> setOf(CONFIRMED, CANCELLED)
        CONFIRMED            -> setOf(DISPATCHED, CANCELLED)
        DISPATCHED           -> setOf(DELIVERED, REFUSED)
        DELIVERED, REFUSED, CANCELLED -> emptySet()
    }
}
```
Rules: `REFUSED` requires a (possibly empty-allowed) reason field; every transition stamps a timestamp (`confirmedAt`, `dispatchedAt`, `closedAt`). Invalid transitions return `AppResult.Failure(AppError.InvalidStatusTransition)`. The state-machine logic lives in `OrderStatusMachine` / `AdvanceOrderStatusUseCase` and is fully unit-tested.

### 8.2 Domain model `Order`
Fields: `id: String (UUID)`, `customerName`, `phone: PhoneNumber` (value class, E.164), `address`, `city`, `productNote`, `amountPkr: Long`, `courier: String?`, `trackingId: String?`, `status`, `refusalReason: String?`, `createdAt`, `updatedAt`, `confirmedAt`, `dispatchedAt`, `closedAt` (all `Instant`/epoch millis, nullable where appropriate), `isDeleted` (soft delete).

Persist `syncState` (`LOCAL_ONLY`, `DIRTY`, `SYNCED`) on the entity from day one so Phase 2 sync needs no migration of semantics. It is a **data-layer concern**, not part of the domain `Order`.

### 8.3 Phone normalization (`PhoneNormalizer`, pure Kotlin)
Accept: `0300 1234567`, `0300-1234567`, `+92 300 1234567`, `92300...`, `300...` → output E.164 `+923001234567`. Reject (return null/validation failure) anything not a valid Pakistani mobile (`3xxxxxxxxx` after country code). Unit-test at least 15 cases including whitespace, dashes, parentheses, wrong length, landline, empty.

```kotlin
fun normalizePkPhone(raw: String): String? {
    val digits = raw.filter { it.isDigit() }
    val national = when {
        digits.startsWith("92") && digits.length == 12 -> digits.drop(2)
        digits.startsWith("0") && digits.length == 11 -> digits.drop(1)
        digits.length == 10 -> digits
        else -> return null
    }
    return if (national.startsWith("3") && national.length == 10) "+92$national" else null
}
```
Wrap the result in a `PhoneNumber` value class so un-normalized strings cannot reach the domain.

### 8.4 Message templates
Placeholders: `{name}`, `{product}`, `{amount}`, `{shop}`. `RenderMessageUseCase` replaces them (amount formatted with thousands separators). Seed these three defaults on first launch (idempotently, via a Room `RoomDatabase.Callback` or a first-run check in a use case, not scattered in UI):

| Lang | Body |
|---|---|
| `en` | `Hello {name}, your order of {product} (Rs. {amount}, Cash on Delivery) from {shop} is ready. Please reply YES to confirm so we can dispatch it.` |
| `ur` | `السلام علیکم {name}، {shop} سے آپ کا آرڈر {product} (رقم Rs. {amount}، کیش آن ڈیلیوری) تیار ہے۔ براہ کرم تصدیق کے لیے YES لکھ کر جواب دیں۔` |
| `ur-Latn` | `Assalam o Alaikum {name}, {shop} se aap ka order {product} (Rs. {amount}, COD) tayyar hai. Dispatch karne ke liye please YES likh kar confirm karein.` |

### 8.5 WhatsApp launching
Interface in presentation/platform (so ViewModels stay Android-free; the ViewModel emits an effect, the screen calls the launcher):

```kotlin
interface WhatsAppLauncher {
    fun open(phoneE164: String, message: String): LaunchResult   // Opened | NotInstalled
}
```
Implementation: `Intent(ACTION_VIEW, Uri.parse("https://wa.me/${e164.removePrefix("+")}?text=${Uri.encode(message)}"))`; catch `ActivityNotFoundException` → return `NotInstalled`; UI then shows a snackbar with a "Copy message" action.

### 8.6 Customer history (private, seller's own data only)
`GetCustomerHistoryUseCase(phone, excludeOrderId)` returns `CustomerHistory(total, delivered, refused)` computed from local orders. Show it as a banner on the order form once a valid phone is entered. There is **no shared database** in v1.

### 8.7 Stats
- Refusal rate = `REFUSED / (DELIVERED + REFUSED)` (guard divide-by-zero → 0%).
- Estimated money lost = `REFUSED × returnCostPkr` (setting, default 400).
- Confirmation lift = refusal rate of orders that went through `CONFIRMED` before `DISPATCHED` vs those that did not. Because the status machine forces `CONFIRMED` before dispatch, track this via a boolean `wasConfirmedBeforeDispatch` derived from `confirmedAt != null`; if v1 workflow later allows skipping confirmation, this metric stays valid.
- All math lives in `ObserveStatsUseCase` / a pure `StatsCalculator`, 100% unit-tested.

### 8.8 CSV export
`BuildOrdersCsvUseCase` builds CSV text in the domain (pure; proper quoting/escaping of commas, quotes, newlines, and a UTF-8 BOM friendly for Excel with Urdu). `data/export/CsvFileWriter` writes it to cache and exposes a `FileProvider` URI; the screen launches the share sheet via an effect.

---

## 9. Reference code patterns (follow these shapes)

### 9.1 Use case
```kotlin
class AdvanceOrderStatusUseCase @Inject constructor(
    private val orders: OrderRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: String, to: OrderStatus, refusalReason: String? = null): AppResult<Order> {
        val order = orders.getById(id) ?: return AppResult.Failure(AppError.NotFound)
        if (to !in order.status.allowedNext) return AppResult.Failure(AppError.InvalidStatusTransition)
        val now = clock.instant()
        val updated = order.copy(
            status = to,
            refusalReason = if (to == OrderStatus.REFUSED) refusalReason else order.refusalReason,
            confirmedAt = if (to == OrderStatus.CONFIRMED) now else order.confirmedAt,
            dispatchedAt = if (to == OrderStatus.DISPATCHED) now else order.dispatchedAt,
            closedAt = if (to in setOf(OrderStatus.DELIVERED, OrderStatus.REFUSED, OrderStatus.CANCELLED)) now else order.closedAt,
            updatedAt = now,
        )
        return orders.update(updated)
    }
}
```

### 9.2 Repository (interface in domain, impl in data)
```kotlin
// domain/repository
interface OrderRepository {
    fun observeByStatus(status: OrderStatus): Flow<List<Order>>
    fun observeById(id: String): Flow<Order?>
    suspend fun getById(id: String): Order?
    suspend fun create(order: Order): AppResult<Order>
    suspend fun update(order: Order): AppResult<Order>
    suspend fun softDelete(id: String): AppResult<Unit>
    suspend fun history(phone: PhoneNumber, excludeId: String?): CustomerHistory
}

// data/repository
class OrderRepositoryImpl @Inject constructor(
    private val dao: OrderDao,
    private val dispatchers: DispatcherProvider,
) : OrderRepository { /* map entity <-> domain, mark syncState = DIRTY on writes */ }
```

### 9.3 ViewModel
```kotlin
@HiltViewModel
class OrderListViewModel @Inject constructor(
    private val observeOrders: ObserveOrdersUseCase,
    private val mapper: OrderUiMapper,
) : ViewModel() {

    private val selectedStatus = MutableStateFlow(OrderStatus.PENDING_CONFIRMATION)
    private val query = MutableStateFlow("")
    private val _effects = Channel<OrderListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<OrderListUiState> =
        combine(selectedStatus, query) { s, q -> s to q }
            .flatMapLatest { (status, q) ->
                observeOrders(status, q).map<List<Order>, OrderListUiState> { list ->
                    OrderListUiState.Content(status, q, list.map(mapper::toListItem))
                }
            }
            .onStart { emit(OrderListUiState.Loading) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderListUiState.Loading)

    fun onEvent(event: OrderListEvent) = when (event) {
        is OrderListEvent.OnTabSelected -> selectedStatus.value = event.status
        is OrderListEvent.OnQueryChanged -> query.value = event.query
        is OrderListEvent.OnOrderClicked -> viewModelScope.launch { _effects.send(OrderListEffect.NavigateToDetail(event.id)) }.let { }
    }
}
```

### 9.4 Screen split
```kotlin
@Composable
fun OrderListScreen(viewModel: OrderListViewModel = hiltViewModel(), onNavigate: (OrderListEffect.NavigateToDetail) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.effects.collect { if (it is OrderListEffect.NavigateToDetail) onNavigate(it) } }
    OrderListContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun OrderListContent(state: OrderListUiState, onEvent: (OrderListEvent) -> Unit) { /* stateless */ }
```

### 9.5 Hilt binding
```kotlin
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun orders(impl: OrderRepositoryImpl): OrderRepository
    @Binds @Singleton abstract fun templates(impl: TemplateRepositoryImpl): TemplateRepository
    @Binds @Singleton abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository
}
```

---

## 10. Compose and UX standards

- Stateless composables; hoist state; pass lambdas, not ViewModels, down the tree.
- Every screen has `@Preview`s for: loading, empty, content, error, and Urdu/RTL where text-heavy.
- Material 3 theme with light/dark and dynamic color optional; define spacing/typography tokens in `designsystem`.
- Large touch targets (≥ 48dp), one-handed use, bottom navigation: **Orders · Stats · Templates · Settings**.
- Layout must mirror correctly for RTL (use `start/end`, never `left/right`).
- Accessibility: content descriptions on icons, state-aware semantics, sufficient contrast.
- Lists: `LazyColumn` with stable `key = { it.id }`.
- Target: new order → WhatsApp opened in **≤ 3 taps**.
- Empty states and error states are designed, not afterthoughts.

---

## 11. Persistence rules

- Room `exportSchema = true`; schema JSON committed under `app/schemas/`.
- **Never** use `fallbackToDestructiveMigration()`. Every schema change ships with a tested `Migration` (use `MigrationTestHelper`).
- DAO methods return `Flow` for observation and `suspend` for one-shot reads/writes.
- Indices on `phone`, `status`, `createdAt`.
- Use `Instant`/epoch millis via `TypeConverters`; enums stored as strings.
- Soft delete (`deleted = 1`) for sync-readiness; queries filter it out.
- DataStore only for settings: shop name, default template language, return cost, first-run flag.
- Backup: configure `data_extraction_rules.xml` and `backup_rules.xml` explicitly (decide and document in DECISIONS whether the DB is included in Android auto-backup; default: **include** so sellers keep orders when changing phones, and mention it in the privacy policy).

---

## 12. Security and privacy

- `android:allowBackup` set deliberately (see section 11), `android:exported` correct on every component, `usesCleartextTraffic=false`.
- Release build: R8 minify + resource shrinking ON, keep rules documented, mapping file archived by CI.
- No PII in logs, crash keys, analytics, or exception messages. A `ReleaseTree` must drop anything but sanitized events.
- No secrets in repo or `BuildConfig` defaults; read from `local.properties`/env with safe empty fallback for debug.
- Validate and sanitize all user input in the domain (`OrderValidator`), including max lengths.
- CSV export: guard against CSV injection (prefix cells starting with `=`, `+`, `-`, `@` with an apostrophe).
- Phase 2+: Supabase uses **anon key + Row Level Security only**; never ship a `service_role` key.

---

## 13. Quality gates (run before every commit/PR)

```bash
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug
# add connectedDebugAndroidTest when an emulator/device is available
```

| Gate | Requirement |
|---|---|
| Format | ktlint clean |
| Static analysis | detekt clean, config in `config/detekt/detekt.yml`; no baseline growth |
| Android Lint | zero errors; warnings triaged |
| Unit tests | all green |
| Coverage targets | domain ≥ 90%, ViewModels ≥ 80%, data mappers/repositories ≥ 70% |
| Architecture tests | all green (see 13.1) |
| Build | `assembleDebug` and `assembleRelease` succeed |

### 13.1 Architecture tests (Konsist; check its current docs for API)
Write tests in `test/.../architecture/` asserting:
1. `domain` package has **no** imports starting with `android.`, `androidx.`, `dagger.hilt`, or `androidx.room`.
2. `presentation` never imports from `data`.
3. `data` never imports from `presentation`.
4. Every class in `domain/usecase` ends with `UseCase` and exposes a public `operator fun invoke`.
5. Every `ViewModel` is annotated `@HiltViewModel`, ends with `ViewModel`, and has no `Context` constructor parameter.
6. Repository implementations live in `data` and implement a `domain` interface.
7. No usage of `Dispatchers.IO/Default/Main` outside `core/common` and `di`.

### 13.2 Testing practices
- **Fakes over mocks.** Hand-written fakes with test hooks; no mocking frameworks unless unavoidable (document why).
- ViewModel tests: `MainDispatcherRule`, Turbine for `StateFlow`/effects, inject a fixed `Clock`.
- Repository tests: in-memory Room (`Room.inMemoryDatabaseBuilder`) — in `androidTest`, or Robolectric if configured.
- Test names: `` `given pending order when advancing to dispatched then returns invalid transition` ``.
- Arrange–Act–Assert, one behavior per test, no logic in tests.
- Compose UI tests for: add order flow, status actions, empty state.

---

## 14. Git, CI and release

- `main` is always releasable. Branch per task: `feat/…`, `fix/…`, `chore/…`, `docs/…`.
- **Conventional Commits:** `feat(orders): add status machine`, `test(domain): cover phone normalizer`, `chore(build): add detekt`.
- `.gitignore` must exclude: `local.properties`, `*.jks`, `*.keystore`, `google-services.json` (if the owner prefers private), `build/`, `.gradle/`, `.idea/` (except shared run configs if desired).
- CI (free GitHub Actions) — create `.github/workflows/ci.yml`:

```yaml
name: CI
on:
  pull_request:
  push:
    branches: [main]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17' }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: reports
          path: |
            **/build/reports/**
            **/build/test-results/**
```
(Use the Java version required by the current Android Gradle Plugin; adjust if needed and note in DECISIONS.)

- Release: signing config reads from env/`local.properties`; produce a signed **AAB**; bump `versionCode`/`versionName` in one place.

---

## 15. Build variants and config

- `debug`: Timber debug tree, strict mode optional, applicationIdSuffix `.debug`.
- `release`: minify + shrink, no debug tools, Crashlytics enabled (when configured).
- Optional later: `demo` flavor with seeded sample orders for screenshots/store listing.
- `BuildConfig` only for non-secret flags. Feature flags (e.g. `CLOUD_SYNC_ENABLED`) live in a `FeatureFlags` interface provided via DI so Phase 2 code can be dark-launched.

---

## 16. Execution plan (work in this order, tick boxes in `docs/PROGRESS.md`)

Each task is "done" only when: code + tests + quality gates green + committed + progress ticked.

### Phase 0 — Foundation
- [ ] 0.1 Scaffold project, Gradle KTS, version catalog (look up current stable versions), min SDK 26
- [ ] 0.2 Configure Hilt, KSP, Room, DataStore, Timber, Compose BOM, Navigation
- [ ] 0.3 Add ktlint + detekt + `config/detekt/detekt.yml`; make gates pass on the empty project
- [ ] 0.4 Create package skeleton (section 7), `core/common` (AppResult, AppError, DispatcherProvider, Clock), `di` modules
- [ ] 0.5 Architecture tests (13.1) added and passing on the skeleton
- [ ] 0.6 `docs/ARCHITECTURE.md` (summary of sections 5–7), CI workflow, `.gitignore`
- [ ] 0.7 Design system: theme, typography, spacing tokens, 4–5 base components, bottom nav shell with four empty screens

### Phase 1 — Local-only MVP (no network)
- [ ] 1.1 Domain models, `OrderStatus` + machine, `PhoneNumber`, `PhoneNormalizer` (+ ≥15 tests)
- [ ] 1.2 Room: entities, DAOs, DB, converters, schema export, mappers (+ DAO/mapper tests)
- [ ] 1.3 `OrderRepository` interface + impl + `FakeOrderRepository` (+ shared contract tests run against both)
- [ ] 1.4 Order use cases: create, update, delete, observe, advance status, customer history (+ tests)
- [ ] 1.5 Settings (DataStore) + `SettingsRepository` + use cases
- [ ] 1.6 Templates: seed defaults, repository, `RenderMessageUseCase`, template screen (+ tests)
- [ ] 1.7 Orders list screen (tabs, search, empty state) with ViewModel (+ VM tests)
- [ ] 1.8 Order add/edit screen with live validation and customer-history banner (+ tests)
- [ ] 1.9 Order detail screen with status actions, WhatsApp effect, copy-message fallback
- [ ] 1.10 `WhatsAppLauncher` interface + Android impl
- [ ] 1.11 Stats screen + `StatsCalculator` (+ tests)
- [ ] 1.12 CSV export via share sheet (+ escaping/injection tests)
- [ ] 1.13 Urdu translations for every string, RTL pass, previews
- [ ] 1.14 Settings screen (shop name, language, return cost, export, about/privacy-policy link placeholder)
- [ ] 1.15 Release hardening: R8 rules, backup rules, lint zero errors, signed AAB instructions in `docs/RELEASE.md`
- [ ] 1.16 Compose UI tests for the main flow

**STOP after Phase 1 and report.** The owner will run a Play closed test.

### Phase 2 — Cloud backup (only when the owner says go)
- [ ] 2.1 Define `OrderRemoteDataSource` and `AuthRepository` interfaces in domain; Supabase implementations in `data/remote`
- [ ] 2.2 Supabase SQL + RLS migration files committed under `supabase/`
- [ ] 2.3 Google sign-in
- [ ] 2.4 `SyncOrdersUseCase` + WorkManager `SyncWorker` (push `DIRTY`, pull `updated_at > lastSyncAt`, last-write-wins, soft deletes)
- [ ] 2.5 Settings UI: sign in, sync status, restore on new device
- [ ] 2.6 App stays fully usable offline / signed-out; sync failures never block the UI

### Phase 3 — Monetization (only when the owner says go)
- [ ] 3.1 `EntitlementRepository` interface; Play Billing implementation
- [ ] 3.2 Free tier limit (default 30 new orders/month) enforced in `CreateOrderUseCase` via an injected `EntitlementPolicy`
- [ ] 3.3 Paywall screen; existing data is never locked

---

## 17. Definition of Done (per task)

- [ ] Follows layering and SOLID rules; architecture tests green
- [ ] No hard-coded strings; Urdu string added
- [ ] Unit tests added/updated; coverage targets respected
- [ ] `ktlintCheck`, `detekt`, `lintDebug`, `testDebugUnitTest`, `assembleDebug` all pass
- [ ] No PII logged; no new permissions without a DECISIONS entry
- [ ] KDoc on public domain APIs; non-obvious decisions recorded in `docs/DECISIONS.md`
- [ ] Conventional commit made; `docs/PROGRESS.md` updated

---

## 18. Working agreements for you (the agent)

- **Be autonomous.** Choose sensible defaults, record them as short ADRs (`## ADR-0007: <title>` — context, decision, consequences).
- **Look things up.** Dependency versions, Gradle/AGP compatibility, Konsist/detekt/Hilt APIs change. Check official docs rather than relying on memory, and write what you verified into DECISIONS.
- **Verify, don't assume.** Run the Gradle gates after each task. If the build fails, fix it before moving on. If the environment lacks the Android SDK, an emulator, or network access, say exactly what is missing and continue with what you can do (e.g. write code and JVM tests).
- **Keep changes small** and in the order of section 16. Don't refactor unrelated code. Don't add features outside the current task.
- **Don't invent product scope.** If a feature isn't in this file, don't build it; list it under "Ideas" in `docs/DECISIONS.md`.
- **Report concisely** at the end of each phase: what was built, what was verified (commands + results), what's open, what you need from the owner.

---

## 19. When to stop and ask the owner

Stop and ask only if you hit one of these:
1. A need for a credential, keystore, `google-services.json`, Supabase project URL/keys, or a Play Console action.
2. A legal/privacy decision (shared phone reputation, data retention, storing new categories of personal data).
3. A product-scope change that contradicts this file.
4. A pricing/monetization decision (tier limits, price points).
5. A destructive action (deleting files outside the project, force-pushing, rewriting history).
6. Two reasonable architecture options with materially different long-term cost **and** no clear winner under the rules above.

For everything else: decide, document, proceed.

---

## 20. Quick command reference

```bash
./gradlew clean
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew ktlintCheck detekt          # or spotlessCheck detekt, per DECISIONS
./gradlew connectedDebugAndroidTest   # needs emulator/device
./gradlew bundleRelease               # signed AAB when signing is configured
```