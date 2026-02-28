# Lightweight TDD — Single-Context Red-Green-Refactor

Use this for small, well-scoped changes: adding a method, validation logic, a new UseCase, or any change where you already know the class and behavior. No subagents — everything runs in this context.

**You MUST complete each phase fully before starting the next. Do NOT write implementation code during RED. Do NOT modify tests during GREEN.**

## Input

The user provides: $ARGUMENTS

Parse the request to identify:
- **Class under test** (e.g., `FormViewModel`, `ValidateEmailUseCase`)
- **Behavior to test** (e.g., "returns error when email is empty")
- **Architecture layer** (ViewModel, UseCase, Repository)

If the class or behavior is ambiguous, ask before proceeding. Do NOT explore the codebase speculatively.

---

## 🔴 RED — Write the Failing Test

**Goal:** A test file that fails — either compile failure or assertion failure. Both are valid.

**Rules:**
- Write ONLY test files (`src/test/`)
- Do NOT create or modify any file in `src/main/`
- Do NOT create stubs, empty classes, or placeholder implementations
- Mirror the source path: `src/main/java/com/example/deepwork/<pkg>/Foo.kt` → `src/test/java/com/example/deepwork/<pkg>/FooTest.kt`
- If a test file already exists for this class, ADD to it — don't create a duplicate

**Read only what you need.** If the class under test exists, read it. If there's an existing test file, read it. Do NOT glob/grep broadly to "understand the architecture."

**Conventions:**
- JUnit 4 (`org.junit.Test`, `org.junit.Before`, `org.junit.Assert.*`) — NOT JUnit 5
- MockK (`mockk()`, `coEvery`, `coVerify`)
- `StandardTestDispatcher` + `runTest` + `Dispatchers.setMain(dispatcher)` in `@Before`
- Custom `Result` type (`Result.Success`, `Result.Error`) — NOT `kotlin.Result`
- Test names: backtick format — `` `when X, should Y` ``
- Setup: `@Before fun setUp()`
- ViewModel tests: launch collection job, `advanceUntilIdle()`, cancel job — no Turbine
- Assert success: `assertEquals(expected, result.getOrThrow())`
- Assert failure: `assert(result.isError)`

**Run the test:**
```bash
./gradlew test --tests "com.example.deepwork.<package>.<TestClass>" 2>&1 | grep -E "(BUILD|PASSED|FAILED|> Task|tests completed|Compilation failed)" | head -10
```

**After running:** Report the test file path, whether it was a compile failure or assertion failure, and what behavior the test verifies. Then proceed to GREEN.

---

## 🟢 GREEN — Make It Pass

**Goal:** Minimal production code to make the test pass.

**Rules:**
- Fix the implementation, NEVER the test
- Minimal means minimal — if the test passes, you are done
- If the class doesn't exist, create a stub first, confirm assertion failure, then implement
- Match the existing file's language (Java or Kotlin)
- Follow existing patterns: `@HiltViewModel`, `@Inject constructor`, `var state by mutableStateOf(...)`, `Channel<UiEvent>`, etc.
- Read only the files you need to implement — the test file plus direct dependencies

**Run the test after implementation:**
```bash
./gradlew test --tests "com.example.deepwork.<package>.<TestClass>" 2>&1 | grep -E "(BUILD|PASSED|FAILED|> Task|tests completed|Compilation failed)" | head -10
```

**If the test fails:** Read the failure, fix your implementation, run again. Do NOT modify the test.

**After passing:** Report files created/modified and a one-line implementation summary. Then evaluate whether refactoring is needed.

---

## 🔵 REFACTOR — Conditional

**Skip refactoring and report the cycle as complete if ALL of these are true:**
- No new files were created (only modified existing files)
- The change was under ~20 lines of production code
- No obvious duplication with existing code

**Refactor only if:**
- Business logic ended up in the wrong layer (e.g., logic in ViewModel that belongs in a UseCase)
- Clear duplication with existing code elsewhere in the codebase
- A name actively obscures intent

If refactoring, run the test again afterward to confirm it still passes. If the test breaks, revert — don't touch the test.

---

## Summary

After the cycle completes, report:
- Test file path
- Production files created or modified
- Whether refactoring was applied or skipped
- Total: one-line description of what was built
