---
name: tdd-integration
description: Enforce Test-Driven Development with strict Red-Green-Refactor cycle. Auto-triggers when implementing new features or functionality. Trigger phrases include "implement", "add feature", "build", "create", or any request to add new behavior. Does NOT trigger for bug fixes, documentation, configuration changes, or refactoring existing code.
---

# TDD Integration — Plan → Red → Green → Refactor

Enforce strict Test-Driven Development using a structured planning phase followed by isolated subagent execution. The orchestrator (you) handles ALL codebase exploration. Subagents receive pre-digested work orders and execute without searching.

---

## Phase 0: PLAN — Build the Work Order

**This phase runs in the orchestrator context (you). Do NOT delegate to a subagent.**

### Step 1 — Parse the user's request

Extract these fields from the user's input:

| Field | Required | Example |
|-------|----------|---------|
| **Feature** | yes | "Workout detail view" |
| **Layer** | yes | ViewModel, UseCase, or Repository |
| **Class** | yes | `WorkoutDetailViewModel` (prefix with `new:` if it doesn't exist yet) |
| **Behaviors** | yes (≥1) | `when initialized with valid workout ID, should load details into Success state` |

If ANY required field is missing or ambiguous, ask the user before proceeding. Do NOT guess. Do NOT explore the codebase speculatively. Example prompt:

> I need a few details before starting the TDD cycle:
> - **Layer:** Is this a ViewModel, UseCase, or Repository?
> - **Behaviors:** What are the specific behaviors to test? (format: "when X, should Y")

### Step 2 — Explore the codebase ONCE

After you have the required fields, explore to fill in the work order. Use Glob/Grep/Read to find:

1. **Does the class already exist?** → Glob for `**/ClassName.kt`
2. **Where does it belong?** → Find the correct module and package by looking at similar classes in the same layer
3. **What dependencies does it need?** → Read interfaces/classes it will depend on
4. **Is there an existing test file?** → Glob for `**/ClassNameTest.kt`
5. **What files should agents read?** → Identify the minimum set. Follow these layer-specific rules:

**ViewModel tests always need:** the existing test file (if appending), the UiState class, any UI model types referenced in assertions (e.g., `TimeBlockUi`), and any event/action sealed classes the test will reference.

**UseCase tests always need:** the existing test file (if appending), the repository interface the use case depends on, and any domain model types used in inputs/outputs.

**Repository tests always need:** the existing test file (if appending), the DAO or API interface, and relevant entity/model types.

### Step 3 — Produce the Work Order

Write out this exact structure (fill in every field):

```
## TDD Work Order

### Feature
Name: <feature name>
Requirement: <one-line behavior description>

### Target
Layer: <ViewModel | UseCase | Repository>
Module: <gradle module path, e.g. app/>
Package: <full package, e.g. com.example.deepwork.feature.workout>
Class: <ClassName>
Exists: <yes | no>
Path: <exact file path — current location if exists, target path if new>

### Behaviors to Test
1. when <precondition>, should <expected outcome>
2. when <precondition>, should <expected outcome>
3. when <error condition>, should <expected outcome>

### Test Location
File: <exact test file path>
Exists: <yes — append new tests | no — create file>
Source set: <src/test/>

### Dependencies (for implementer)
- <InterfaceName> at <path> — <what it provides>
- <RepositoryName> at <path> — <what it provides>
(or "None — standalone class" if no dependencies)

### Files to Read
Test writer:
  - <path> — <why>
Implementer:
  - <path> — <why>
  - <path> — <why>
Refactorer:
  (same as implementer)

### Test Command
./gradlew test --tests "<full.package.TestClassName>" 2>&1 | tail -30
```

### Step 4 — Present to user and WAIT

Show the work order to the user. Explicitly ask:

> **TDD Work Order ready.** Review the plan above. Reply "go" to start the Red-Green-Refactor cycle, or tell me what to adjust.

**Do NOT proceed until the user approves.** If they request changes, update the work order and re-present it.

---

## Phase 1: RED — Write Failing Test

> 🔴 RED PHASE: Delegating to tdd-test-writer...

Invoke the `tdd-test-writer` subagent with this EXACT prompt structure:

```
## Work Order — RED Phase

Feature: <from work order>
Layer: <from work order>
Class under test: <ClassName>
Package: <full package>

### Behaviors to test:
1. <behavior 1>
2. <behavior 2>
3. <behavior 3>

### Test file
Path: <exact test file path>
Exists: <yes — append | no — create>

### Files to read before writing:
- <path> — <why>

### Test command:
<exact command>
```

Do NOT include dependency information, implementation hints, or architecture context. The test writer should think only about behavior, not implementation.

**Expected return:** test file path, failure type (compile or assertion), failure output, summary.

**Do NOT proceed to GREEN until the test writer returns.**

---

## Phase 2: GREEN — Make It Pass

> 🟢 GREEN PHASE: Delegating to tdd-implementer...

Invoke the `tdd-implementer` subagent with this EXACT prompt structure:

```
## Work Order — GREEN Phase

Feature: <from work order>
Layer: <from work order>

### Test
File: <test file path>
Failure type: <compile failure | assertion failure>
Failure output:
<paste the failure output from RED phase>

### Implementation target
Class: <ClassName>
Package: <full package>
Path: <exact implementation file path>
Exists: <yes | no>

### Dependencies
- <InterfaceName> at <path> — <what it provides>

### Files to read:
- <path> — <why>

### Test command:
<exact command>
```

**Expected return:** files created/modified, test success output, implementation summary.

**If the implementer returns GREEN RESULT — BLOCKED:** Present the blocking issue to the user. Explain what the implementer found and what it recommends. Ask the user how to proceed:
> The implementer hit a scope boundary. It believes `<file>` needs to change because `<reason>`. Should I:
> 1. Expand the work order to include this change and re-run GREEN
> 2. Let you handle this manually, then I'll re-run GREEN
> 3. Abort this cycle

**If the implementer returns GREEN RESULT — INCOMPLETE:** Present the failure to the user with the implementer's diagnosis. The user decides next steps.

**Do NOT proceed to REFACTOR until the test passes.**

---

## Phase 3: REFACTOR — Conditional Improvement

> 🔵 REFACTOR PHASE: Delegating to tdd-refactorer...

Invoke the `tdd-refactorer` subagent with:

```
## Work Order — REFACTOR Phase

### Test file
<test file path>

### Implementation files
- <path> — <one-line description of what was created/modified>

### Files to read:
- <path> — <why>

### Test command:
<exact command>
```

**Expected return:** changes made + test output, OR "no refactoring needed" with reasoning.

**Cycle complete when refactor phase returns.**

---

## Cycle Complete — Report

After REFACTOR returns, summarize:

```
✅ TDD Cycle Complete: <feature name>

🔴 RED: <test file path> — <N behaviors tested>
🟢 GREEN: <files created/modified>
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
- Proceed to REFACTOR without the test passing
- Skip REFACTOR evaluation entirely
- Modify tests to make them pass — fix the implementation instead
- Start a new feature before the current cycle completes