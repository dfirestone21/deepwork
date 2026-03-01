# CLAUDE.md

## Build & Test Commands

```bash
./gradlew assembleDebug                # Build debug APK
./gradlew test                         # Run all unit tests
./gradlew :app:testDebugUnitTest --tests "com.example.deepwork.<package>.<TestClass>"  # Single test class
./gradlew connectedAndroidTest         # Instrumented tests
./gradlew lint                         # Lint
```

## TDD Workflow

**No implementation code before a failing test exists. Ever.**

Every feature follows Red → Green → Refactor. One feature per cycle. See `.claude/skills/tdd-integration/` for the full orchestrated workflow.

### Hard Rules

- Never modify a test to make it pass — fix the implementation
- Never write implementation before a failing test
- Never skip the refactor evaluation
- Complete the full cycle before starting the next feature