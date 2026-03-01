---
name: tdd-integration
description: Enforce Test-Driven Development with strict Red-Green-Refactor cycle. Auto-triggers when implementing new features or functionality. Trigger phrases include "implement", "add feature", "build", "create", or any request to add new behavior. Does NOT trigger for bug fixes, documentation, configuration changes, or refactoring existing code.
---

# TDD Integration — Plan → Red → Green → Refactor

Enforce strict Test-Driven Development using a structured planning phase followed by isolated subagent execution. The orchestrator (you) handles ALL codebase exploration. Subagents receive pre-digested work orders and execute without searching.

A single TDD cycle covers 1–3 **units** ordered by dependency (e.g., Repository → UseCase → ViewModel, or UseCase A → UseCase B → ViewModel). Units can be in different layers or the same layer — the ordering principle is dependency, not architecture. This amortizes subagent overhead across the full feature rather than paying ~20k tokens per class.

---

## Phase 0: PLAN — Build the Work Order

**This phase runs in the orchestrator context (you). Do NOT delegate to a subagent.**

### Step 1 — Parse the user's request

Extract these fields from the user's input:

| Field | Required | Example |
|-------|----------|---------|
| **Feature** | yes | "Session duration validation" |
| **Classes** | yes | `ValidateSessionDurationUseCase` (new), `BuildSessionUseCase` (new), `CreateSessionViewModel` (existing) |
| **Behaviors** | yes (≥1 per class) | `when duration exceeds 90min, should return Warning` |

If ANY required field is missing or ambiguous, ask the user before proceeding. Do NOT guess. Do NOT explore the codebase speculatively. Example prompt:

> I need a few details before starting the TDD cycle:
> - **Classes:** Which classes does this feature touch? (include layer: Repository, UseCase, ViewModel)
> - **Behaviors:** What are the specific behaviors to test per class? (format: "when X, should Y")

**If a feature touches 4+ classes:** Split into two cycles. Complete the first before starting the second.

### Step 2 — Explore the codebase ONCE

After you have the required fields, explore to fill in the work order. Use Glob/Grep/Read to find:

1. **Does each class already exist?** → Glob for `**/ClassName.kt`
2. **Where does each belong?** → Find the correct module and package by looking at similar classes in the same layer
3. **What dependencies does each need?** → Read interfaces/classes they depend on
4. **Are there existing test files?** → Glob for `**/ClassNameTest.kt`
5. **What files should agents read?** → Identify the minimum set per class. Follow these rules:

**ViewModel tests always need:** the existing test file (if appending), the UiState class, any UI model types referenced in assertions (e.g., `TimeBlockUi`), and any event/action sealed classes the test will reference.

**UseCase tests always need:** the existing test file (if appending), the repository interface the use case depends on, and any domain model types used in inputs/outputs.

**Repository tests always need:** the existing test file (if appending), the DAO or API interface, and relevant entity/model types.

### Step 3 — Determine dependency order

Order units so that depended-on classes come first. If Unit 2 depends on Unit 1, Unit 1 must be listed first. If two classes have no dependency between them, order doesn't matter — pick the simpler one first.

### Step 4 — Produce the Work Order

```
## TDD Work Order

### Feature
Name: <feature name>
Requirement: <one-line behavior description>
Scope: <N> units

### Stack (dependency order — implement top to bottom)

#### Unit 1: <Layer> — <ClassName>
Class: <ClassName>
Module: <gradle module path>
Package: <full package>
Exists: <yes | no>
Path: <exact file path>
Test file: <exact test file path>
Test exists: <yes — append | no — create>
Behaviors:
  1. when <precondition>, should <expected outcome>
  2. when <precondition>, should <expected outcome>
Dependencies:
  - <InterfaceName> at <path> — <what it provides>
  (or "None — standalone class")
Files to read:
  Test writer:
    - <path> — <why>
  Implementer:
    - <path> — <why>
Test command: ./gradlew test --tests "<full.package.TestClassName>" 2>&1 | tail -30

#### Unit 2: <Layer> — <ClassName> (if applicable)
(same structure — list dependency on Unit 1 if relevant)

#### Unit 3: <Layer> — <ClassName> (if applicable)
(same structure)

### Refactorer — Files to read
- <path> — <why>
(Consolidated list of all implementation and test files across units)
```

### Step 5 — Present to user and WAIT

Show the work order to the user. Explicitly ask:

> **TDD Work Order ready.** Review the plan above. Reply "go" to start the Red-Green-Refactor cycle, or tell me what to adjust.

**Do NOT proceed until the user approves.** If they request changes, update the work order and re-present it.

---

## Phase 1: RED — Write Failing Tests

> 🔴 RED PHASE: Delegating to tdd-test-writer...

Invoke the `tdd-test-writer` subagent with this EXACT prompt structure:

```
## Work Order — RED Phase

Feature: <from work order>
Scope: <N> units

### Unit 1: <Layer> — <ClassName>
Class under test: <ClassName>
Package: <full package>
Test file: <exact test file path>
Test exists: <yes — append | no — create>
Behaviors:
  1. <behavior 1>
  2. <behavior 2>
Files to read:
  - <path> — <why>
Test command: <exact command>

### Unit 2: <Layer> — <ClassName> (if applicable)
(same structure)

### Unit 3: <Layer> — <ClassName> (if applicable)
(same structure)
```

Do NOT include dependency information, implementation hints, or architecture context. The test writer should think only about behavior, not implementation.

**Expected return:** Per-unit results with test file paths, failure types, and failure output.

**Do NOT proceed to GREEN until the test writer returns with all tests failing.**

---

## Phase 2: GREEN — Make It Pass

> 🟢 GREEN PHASE: Delegating to tdd-implementer...

Invoke the `tdd-implementer` subagent with this EXACT prompt structure:

```
## Work Order — GREEN Phase

Feature: <from work order>
Scope: <N> units

### Unit 1: <Layer> — <ClassName>
Test file: <test file path>
Failure type: <compile failure | assertion failure>
Failure output:
<paste failure output from RED>
Implementation target:
  Class: <ClassName>
  Package: <full package>
  Path: <exact implementation file path>
  Exists: <yes | no>
Dependencies:
  - <InterfaceName> at <path> — <what it provides>
Files to read:
  - <path> — <why>
Test command: <exact command>

### Unit 2: <Layer> — <ClassName> (if applicable)
(same structure — list dependency on Unit 1 if relevant)

### Unit 3: <Layer> — <ClassName> (if applicable)
(same structure)
```

**Expected return:** Per-unit results with files created/modified and test output.

**Handling results:**

**GREEN RESULT — PARTIAL:** Some units passed, some didn't. Present the status to the user:
> Unit 1 passed ✅, Unit 2 (UseCase — BuildSessionUseCase) incomplete after 3 attempts.
> Suspected issue: <from implementer>
> Should I:
> 1. Let you debug Unit 2 manually (Unit 1 changes are kept)
> 2. Re-run just Unit 2 with adjusted work order
> 3. Abort the entire cycle

**GREEN RESULT — BLOCKED:** Present the blocking issue to the user. Explain what the implementer found and what it recommends. Ask the user how to proceed:
> The implementer hit a scope boundary. It believes `<file>` needs to change because `<reason>`. Should I:
> 1. Expand the work order to include this change and re-run GREEN
> 2. Let you handle this manually, then I'll re-run GREEN
> 3. Abort this cycle

**Do NOT proceed to REFACTOR until all unit tests pass.** If partial, get user decision first.

---

## Phase 3: REFACTOR — Conditional Improvement

> 🔵 REFACTOR PHASE: Delegating to tdd-refactorer...

Invoke the `tdd-refactorer` subagent with:

```
## Work Order — REFACTOR Phase

### Test files
- <Unit 1 test file path>
- <Unit 2 test file path> (if applicable)
- <Unit 3 test file path> (if applicable)

### Implementation files
- <path> — <one-line description of what was created/modified>
- <path> — <one-line description>

### Files to read:
- <path> — <why>

### Test commands:
Unit 1: <exact command>
Unit 2: <exact command> (if applicable)
Unit 3: <exact command> (if applicable)
```

**Expected return:** changes made + test output, OR "no refactoring needed" with reasoning.

**Cycle complete when refactor phase returns.**

---

## Cycle Complete — Report

After REFACTOR returns, summarize:

```
✅ TDD Cycle Complete: <feature name> (<N> units)

🔴 RED:
  - Unit 1 (<Layer> — <ClassName>): <test file path> — <N behaviors>
  - Unit 2 (<Layer> — <ClassName>): <test file path> — <N behaviors>

🟢 GREEN:
  - Unit 1: <files created/modified>
  - Unit 2: <files created/modified>

🔵 REFACTOR: <changes made or "skipped — <reason>">
```

---

## Multiple Features

Complete the full cycle for EACH feature before starting the next:

- Feature 1: PLAN → 🔴 → 🟢 → 🔵 ✓
- Feature 2: PLAN → 🔴 → 🟢 → 🔵 ✓

---

## Phase Violations — Never Do These

- Skip PLAN or proceed without user approval
- Let a subagent explore the codebase with Glob/Grep (except implementer as fallback)
- Pass unstructured natural language to subagents instead of the work order format
- Write implementation code before a failing test exists
- Proceed to GREEN without RED returning
- Proceed to REFACTOR without all unit tests passing (or user approval for partial)
- Skip REFACTOR evaluation entirely
- Modify tests to make them pass — fix the implementation instead
- Start a new feature before the current cycle completes
- Stack more than 3 units in a single cycle — split into two cycles instead