---
name: tdd-integration
description: Enforce Test-Driven Development with strict Red-Green-Refactor cycle. Auto-triggers when implementing new features or functionality. Trigger phrases include "implement", "add feature", "build", "create", or any request to add new behavior. Does NOT trigger for bug fixes, documentation, configuration changes, or refactoring existing code.
---

# TDD Integration — Red-Green-Refactor Orchestrator

Enforce strict Test-Driven Development using the Red-Green-Refactor cycle with dedicated subagents. Each phase runs in an isolated context to prevent implementation details from polluting test design.

## Mandatory Workflow

Every new feature MUST follow this strict 3-phase cycle. Do NOT skip or merge phases.

---

### 🔴 Phase 1: RED — Write Failing Test

> RED PHASE: Delegating to tdd-test-writer...

Invoke the `tdd-test-writer` subagent with:
- The feature requirement from the user request
- The expected behavior to verify (inputs, outputs, side effects)
- Relevant existing files or context (ViewModel, Repository, UseCase names if known)

The subagent returns one of two outcomes:

**Outcome A — Compile failure:** The class or method under test doesn't exist yet.
**Outcome B — Assertion failure:** The test compiles and fails with a meaningful assertion.

Both are valid. Proceed to Green phase in either case.

**Do NOT proceed to Green phase until the test writer has returned.**

---

### 🟢 Phase 2: GREEN — Make It Pass

> GREEN PHASE: Delegating to tdd-implementer...

Invoke the `tdd-implementer` subagent with:
- Test file path from RED phase
- The outcome from RED phase (compile failure or assertion failure)
- Feature requirement context
- Any relevant architecture layer (ViewModel, UseCase, Repository, etc.)

The implementer owns all production source files. If RED returned a compile failure, the implementer will create the necessary stubs first, confirm a meaningful assertion failure, then implement fully.

The subagent returns:
- Files created or modified
- Test success output confirming the test passes
- A brief implementation summary

**Do NOT proceed to Refactor phase until the test passes.**

---

### 🔵 Phase 3: REFACTOR — Improve Without Breaking

> REFACTOR PHASE: Delegating to tdd-refactorer...

Invoke the `tdd-refactorer` subagent with:
- Test file path
- Implementation files from GREEN phase

The subagent returns either:
- Changes made + test success output confirming tests still pass, OR
- "No refactoring needed" with brief reasoning

**Cycle complete when refactor phase returns.**

---

## Multiple Features

Complete the full cycle for EACH feature before starting the next:

- Feature 1: 🔴 → 🟢 → 🔵 ✓
- Feature 2: 🔴 → 🟢 → 🔵 ✓

## Phase Violations — Never Do These

- Write implementation code before a failing test exists
- Have the test writer create or modify any file in `src/main/`
- Proceed to Green without the test writer returning
- Proceed to Refactor without the test passing
- Skip the Refactor evaluation entirely
- Start a new feature before the current cycle completes
- Modify tests to make them pass — fix the implementation instead
