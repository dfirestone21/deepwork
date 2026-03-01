---
name: deepwork-architecture
description: Project architecture conventions for the DeepWork Android codebase. Referenced by tdd-implementer and tdd-refactorer for consistent implementation patterns. Covers layer boundaries, Compose state patterns, DI, and the custom Result type.
---

# DeepWork Architecture Conventions

## Layer Boundaries

**MVVM + Clean Architecture:**
```
UI (Composable + ViewModel) → UseCase → Repository → DataSource (Database)
```

- ViewModels depend on UseCases — never on Repositories directly
- UseCases contain business logic and depend on Repository interfaces (in `domain/repository/`)
- Repository implementations live in `data/repository/`, interfaces in `domain/repository/`
- Never skip a layer

## Package Structure

```
com.example.deepwork/
├── data/
│   ├── database/                        (Room DAOs, entities, database)
│   └── repository/                      (Repository implementations)
├── di/
│   ├── DbModule.kt                     (Room/database bindings)
│   ├── RepositoryModule.kt             (Repository interface → impl bindings)
│   └── UseCaseModule.kt                (UseCase interface → impl bindings)
├── domain/
│   ├── business/                        (business rules / domain services)
│   ├── exception/                       (domain-specific exceptions)
│   ├── model/                           (domain models)
│   ├── repository/                      (Repository interfaces)
│   └── usecase/                         (UseCase interfaces + implementations)
├── ui/
│   ├── components/                      (shared/reusable Composables)
│   ├── model/                           (UI-layer models, UiState, UiEvent, Actions)
│   ├── navigation/                      (nav graph, routes)
│   ├── <feature_name>/                  (feature screens + ViewModels, e.g. session_management.create_session/)
│   ├── theme/                           (Material theme, colors, typography)
│   └── util/                            (UI utility functions)
├── App.kt                              (Application class)
└── MainActivity.kt
```

### Key structural rules
- **Repository interfaces** live in `domain/repository/` — NOT in `data/`
- **Repository implementations** live in `data/repository/`
- **Feature UI packages** live directly under `ui/` (e.g., `ui/session_management/`)
- **Domain models** are in `domain/model/` — shared across layers
- **UI models** (UiState, Actions, UiEvents) are in `ui/model/` or co-located with the feature
- **Business rules** that aren't tied to a single UseCase go in `domain/business/`
- **DI modules** are split by concern: `DbModule`, `RepositoryModule`, `UseCaseModule`

## Dependency Injection (Hilt)

- `@HiltViewModel` + `@Inject constructor` for ViewModels
- `@Inject constructor` for UseCases (concrete classes — no module binding needed)
- `@Inject constructor` for Repositories (bound via interface in module)
- New Repository bindings → `di/RepositoryModule.kt`
- New database/DAO bindings → `di/DbModule.kt`
- `di/UseCaseModule.kt` exists but is only needed if a UseCase has special scoping
- Do not manually construct injectable classes

## ViewModel Conventions (Compose)

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val myUseCase: MyUseCase,
) : ViewModel() {

    var state by mutableStateOf<MyUiState>(MyUiState.Loading)
        private set

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: MyAction) {
        when (action) {
            is MyAction.Submit -> handleSubmit(action)
        }
    }
}
```

- Use `var state by mutableStateOf(...)` for UI state — NOT StateFlow/LiveData
- Use `Channel<UiEvent>` + `receiveAsFlow()` for one-shot events
- UI state: `sealed class` or `sealed interface` with meaningful subtypes
- Actions: `sealed class` or `sealed interface` representing user intents
- Expose `onAction(action)` as the single entry point for UI interactions

## UseCase Conventions

```kotlin
class MyUseCase @Inject constructor(
    private val repository: MyRepository,  // interface from domain/repository/
) {
    suspend operator fun invoke(input: MyInput): Result<MyOutput> {
        // business logic
    }
}
```

- UseCases are **concrete classes** — no interface/impl split (unlike the Pruvan project)
- Use `operator fun invoke()` for the main operation
- Return `Result<T>` at the boundary
- `@Inject constructor` is sufficient — no Hilt module binding needed since there's no interface to bind

## Repository Conventions

```kotlin
// Interface in domain/repository/
interface MyRepository {
    suspend fun getData(id: String): Result<MyModel>
}

// Implementation in data/repository/
class MyRepositoryImpl @Inject constructor(
    private val dao: MyDao,
) : MyRepository {
    override suspend fun getData(id: String): Result<MyModel> { ... }
}
```

- Bind interface → impl in `di/RepositoryModule.kt`

## Kotlin Conventions

- `suspend fun` for one-shot async operations
- `Flow` for streams of values
- `sealed class` / `sealed interface` for state and result types
- Prefer `?.let`, `?: return`, and `?: throw` over nested null checks
- `when` over `if/else` chains for exhaustive matching

## Result Type

Custom `Result<T>` sealed class at `com.example.deepwork.domain.model.Result` — do NOT use `kotlin.Result`.

```kotlin
sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
```

**Constructing:** `Result.Success(value)`, `Result.Error(exception)`, `Result.of { ... }`
**Checking:** `result.isSuccess`, `result.isError`
**Extracting:** `result.getOrThrow()`, `result.getOrNull()`, `result.exceptionOrNull()`
**Chaining:** `result.map { }`, `result.onSuccess { }`, `result.onError { }`

Use at layer boundaries (UseCase↔ViewModel, Repository↔UseCase). Not for internal private functions.

## Testing Conventions

**Framework:** JUnit 4, MockK, Coroutines Test
**Test command:** `./gradlew test --tests "<full.package.TestClass>" 2>&1 | tail -30`
**Assert success:** `assertEquals(expected, result.getOrThrow())`
**Assert failure:** `assert(result.isError)`
**ViewModel tests:** launch collection, `advanceUntilIdle()`, cancel job — no Turbine