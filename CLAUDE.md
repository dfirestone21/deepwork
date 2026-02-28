# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run a single test class
./gradlew :app:testDebugUnitTest --tests "com.example.deepwork.ui.session_management.create_session.CreateSessionViewModelTest"

# Run a single test method
./gradlew :app:testDebugUnitTest --tests "com.example.deepwork.ui.session_management.create_session.CreateSessionViewModelTest.onEvent() FabClicked should send CREATE_TIMEBLOCK Navigate UiEvent"

# Run instrumented (Android) tests
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

## Architecture

This is an Android app using **Clean Architecture** with three distinct layers:

### Domain Layer (`domain/`)
Pure Kotlin — no Android dependencies. Contains:
- **Models**: `ScheduledSession`, `ScheduledTimeBlock`, `Category`, `TemplateSession`, `TimeBlockTemplate`
- **Use Cases**: Single-responsibility classes invoked like functions (they implement `operator fun invoke`)
- **Validators**: `CategoryValidator`, `TimeBlockValidator` — throw typed exceptions on failure
- **Exceptions**: Typed domain exceptions (`SessionException`, `TimeBlockException`, `CategoryException`, `DatabaseException`)
- **`Result<T>`**: Custom sealed class (`Success`/`Error`) used throughout for operation results. Supports `.onSuccess {}`, `.onError {}`, `.map {}`, `.getOrThrow()`, `.getOrNull()`

### Data Layer (`data/`)
- **Room database** with entities, DAOs, and cross-reference tables
- **`CategoryDb` interface** acts as an abstraction over Room — the ViewModel-facing DB interface, implemented by `CategoryDbRoom`
- **Repositories** implement domain repository interfaces and delegate to the Db interfaces

### UI Layer (`ui/`)
- **Jetpack Compose** with **MVVM + UDF**: each screen has a `State` data class, `Event` sealed class, and `ViewModel`
- ViewModels expose:
  - `var state by mutableStateOf(...)` — Compose-observed UI state
  - `val uiEvent = Channel<UiEvent>.receiveAsFlow()` — one-shot events (navigation, snackbars)
- `UiEvent` sealed interface: `Navigate(route)`, `NavigateUp`, `ShowSnackbar(message)`
- Screens observe `uiEvent` via `ObserveAsEvents` utility in `ui/util/`
- Navigation routes are string constants in `Routes` object

### Dependency Injection (Hilt)
- `DbModule` — binds Db interfaces to Room implementations, provides `AppDatabase` and DAOs
- `RepositoryModule` — binds repository interfaces to implementations
- `UseCaseModule` — provides use case instances
- `App.kt` is the `@HiltApplication` entry point

### Key Patterns
- **`ScheduledTimeBlock.BlockType`** enum drives validation rules (DEEP_WORK, SHALLOW_WORK, BREAK) — each has `minDuration`/`maxDuration`
- **`TimeBlockTemplate`** factory methods (`deepWorkTemplate`, `shallowWorkTemplate`, `breakTemplate`) create templates for validation before persisting
- **Testing**: Uses MockK for mocking; ViewModel tests use `StandardTestDispatcher` + `advanceUntilIdle()`; validators and use cases are tested directly without mocks where possible

## TDD Workflow

This project uses a strict Test-Driven Development workflow enforced via Claude Code skills and subagents.

### The Rule

**No implementation code before a failing test exists. Ever.**

### Cycle

Every feature follows Red → Green → Refactor:

1. **🔴 Red** — Write a failing test that specifies the behavior. Run it. Confirm it fails.
2. **🟢 Green** — Write the minimal implementation to make the test pass. Nothing extra.
3. **🔵 Refactor** — Improve the code without changing behavior. Tests must stay green.

### Test Commands

```bash
# Run all unit tests
./gradlew test

# Run a specific test class
./gradlew :app:testDebugUnitTest --tests "com.example.deepwork.<package>.<TestClassName>"
```

### Test Conventions

- Test files live in `app/src/test/java/com/example/deepwork/` mirroring the source package
- Naming: `<ClassUnderTest>Test.kt`
- Test names use backtick format: `` `when X, then Y` ``
- Use MockK (`mockk()`, `coEvery`, `coVerify`) — never Mockito
- Use JUnit 4 (`org.junit.Test`, `org.junit.Before`, `org.junit.Assert.*`) — NOT JUnit 5
- Use `StandardTestDispatcher` + `runTest` + `Dispatchers.setMain(dispatcher)` for coroutine tests
- ViewModel tests: launch collection job, `advanceUntilIdle()`, cancel job — no Turbine
- Tests describe WHAT the system does, not HOW — no testing implementation details

### Hard Rules

- **Never modify a test to make it pass** — fix the implementation
- **Never write implementation before a failing test** — the red step is not optional
- **Never skip the refactor evaluation** — even if the answer is "no refactoring needed"
- **One feature per cycle** — complete Red → Green → Refactor before starting the next feature
