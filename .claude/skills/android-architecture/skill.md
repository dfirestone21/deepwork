---
name: android-architecture
description: Project architecture conventions for the Pruvan Android codebase. Referenced by tdd-implementer and tdd-refactorer for consistent implementation patterns. Covers layer boundaries, language conventions, DI, and the custom Result type.
---

# Android Architecture — Pruvan Codebase Conventions

## Layer Boundaries

**MVVM + Clean Architecture (outermost → innermost):**
```
UI (ViewModel) → UseCase → Repository → DataSource (API/DB)
```

- ViewModels depend on UseCases — never on Repositories directly
- UseCases contain business logic and depend on Repository interfaces
- Repositories implement interfaces and depend on DataSources (Room DAOs, API clients)
- Never skip a layer — a ViewModel calling a Repository directly is a violation

## Language Convention

The codebase is mid-migration from Java to Kotlin. When working in an existing file, **match the file's language**. New files should be written in Kotlin.

## Dependency Injection (Hilt)

- `@HiltViewModel` + `@Inject constructor` for ViewModels
- `@Inject constructor` for UseCases and Repositories
- New interface bindings go in the appropriate module in `di/`
- Do not manually construct injectable classes

## ViewModel Conventions

- Use `StateFlow<UiState>` for state — not LiveData for new code
- Use `SharedFlow<UiEvent>` for one-shot events (navigation, toasts, dialogs)
- UI state should be a `sealed class` with meaningful subclasses (e.g. `Loading`, `Success`, `Error`)
- Expose immutable `StateFlow` publicly, back it with a private `MutableStateFlow`

## Kotlin Conventions

- `suspend fun` for one-shot async operations
- `Flow` for streams of values
- `sealed class` for state and result types — exhaustive `when` over open-ended `if/else`
- Prefer `?.let`, `?: return`, and `?: throw` over nested null checks

## Result Type

The project uses a custom `Result<T>` sealed class — do **not** use Kotlin's built-in `kotlin.Result`.

```kotlin
sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
```

**Constructing:**
```kotlin
Result.Success(value)
Result.Error(exception)
Result.of { someThrowingOperation() } // wraps a lambda in try/catch
```

**Checking:**
```kotlin
result.isSuccess  // Boolean
result.isError    // Boolean
```

**Extracting:**
```kotlin
result.getOrThrow()   // returns value or throws exception
result.getOrNull()    // returns value or null
result.exceptionOrNull() // returns exception or null
```

**Chaining:**
```kotlin
result
    .onSuccess { value -> /* side effect */ }
    .onError { exception -> /* side effect */ }

result.map { value -> transform(value) } // transforms Success value, passes Error through
```

**In tests:**
```kotlin
// Assert success value
assertEquals(expected, result.getOrThrow())

// Assert failure
assert(result.isError)

// Assert specific exception type
assertTrue(result.exceptionOrNull() is NetworkException)
```

Use `Result` at layer boundaries — between UseCase and ViewModel, and between Repository and UseCase. Do not use it for internal private functions where exceptions are fine.
