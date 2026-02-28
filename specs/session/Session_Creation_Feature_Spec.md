# Session Creation â€” Consolidated Specification v2.0 (2025-08-16)

> **Purpose:** Replace and unify prior docs into a single, implementation-ready source of truth for the **Session Creation** feature. This covers authoring a session (or template), validation, persistence, and UX. Scheduling/recurrence is referenced but scoped to a separate feature.

---

## 1. Problem, Goals & Scope

### 1.1 Problem

Users need a fast, reliable way to design deep work sessions composed of focused blocks and breaks, assign categories, and optionally save the configuration as a template or one-off session. Prior documents disagreed on status models, limits, and UI behaviors.

### 1.2 Goals

- Create/edit **session definitions** with blocks and breaks.
- **Validate** structure and durations in real-time.
- **Persist** as template or one-off scheduled session.
- Provide **advisory warnings** (non-blocking) that nudge best practices.
- Make the editor efficient on mobile (Compose) with clear affordances.

### 1.3 In Scope

- Authoring flow: new from blank or from template.
- Managing blocks (add/reorder/resize/delete) and breaks.
- Category assignment (1â€“3 per work block).
- Optional â€œblock typeâ€ (DEEP/SHALLOW) per block for UI & analytics.
- Auto-save, undo/redo, optimistic UI; conflict recovery.
- Persist as **Template** or **Session** (optionally with planned start).
- Validation, warnings, and error messages.

### 1.4 Out of Scope (handled by other features)

- Recurrence patterns and calendar sync.
- Execution runtime (start/pause/interrupt/complete) and timers.
- Post-session reflections and analytics visualizations.

---

## 2. Key Concepts & Canonical Definitions

- **Session Definition**: An authored plan (name, description, ordered list of **TimeBlocks**). Can be **saved as Template** or **instantiated as a concrete Session** (with optional planned start time).
- **Template**: A named, reusable Session Definition that stores *relative* durations and layout, not absolute times.
- **Session (Instance)**: A concrete, scheduled/unscheduled instance created from a Session Definition (from-scratch or from Template). Execution state is managed elsewhere.
- **TimeBlock**: A single timeline unit with a duration and metadata. It **must always have a **`` âˆˆ {`DEEP_WORK`, `SHALLOW_WORK`, `BREAK`}. A **Break** is `BlockType.BREAK` and must have **0 categories**. A **Work Block** has `BlockType.DEEP_WORK` or `BlockType.SHALLOW_WORK` and requires **1â€“3 categories**.

**Durational Limits**

- Blocks per session: **1â€“12**
- Total scheduled duration (sum of all TimeBlocks): **â‰¤ 12 hours**

**Naming Rules**

- Name length 1â€“50, description â‰¤ 500; trim whitespace; alphanumeric + space + `-` + `_`; must start with alphanumeric; prevent script/HTML injection; template names must be unique per user.

---

## 3. Unified State Model (Resolved disagreements)

We distinguish **Authoring Status** from **Execution Status** to reconcile prior docs.

### 3.1 Authoring Status (for definitions)

- `DRAFT` â€“ created or edited with pending validations.
- `READY` â€“ passes validation (a.k.a. VALID).
- `TEMPLATE` â€“ saved as reusable template.
- `MATERIALIZED` â€“ saved as a concrete session instance (see below).

### 3.2 Session (Instance) Execution Status (owned by runtime feature)

- `NOT_STARTED`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`, `CANCELLED`, `INTERRUPTED`.

> **Implementation note:** The Session Creation feature *writes* instances but does not drive execution transitions.

---

## 4. User Journeys & Acceptance Criteria

### 4.1 Create from Blank

**Flow**: New â†’ **empty session** (no blocks or breaks) â†’ user adds blocks via FAB â†’ auto-save â†’ Save as Template or Save as Session.

**AC**

1. Given fresh launch, when user taps **New Session**, then a session opens **empty** (no blocks or breaks). **Save** actions remain disabled until at least one valid Work Block exists and all validations pass.
2. When the user adds blocks or breaks, the UI updates start/end times and positions; auto-save occurs within 200ms after idle.
3. User cannot place a Break at the start/end or two Breaks consecutively (error copy provided; see Â§8.3).
4. Saving as Template enforces template-name uniqueness per user and stores relative durations.
5. Saving as Session allows optional planned start time; stored as absolute epoch ms.

### 4.2 Create from Template

**Flow**: Choose Template â†’ copy blocks/breaks/categories â†’ edit freely â†’ save.

**AC**

1. All relative durations/material order are copied; categories preserved.
2. A new session name is required to save as Session or to save as **new** Template.
3. Modifications do not mutate the source Template.

### 4.3 Edit Structure

**AC**

1. Reordering blocks keeps associated Breaks adjacent. If the user drags a Break, it snaps between two Work Blocks.
2. Deleting the last remaining Work Block is disallowed.
3. All duration inputs are minute-precision and rounded to nearest minute.

### 4.4 Warnings (advisory)

**AC**

1. Warnings appear as inline icons on affected blocks and in a session-level panel; dismisses automatically when resolved.
2. Advisory thresholds (non-blocking):
    - Any single Work Block > 60m without a Break afterward â†’ **Yellow**.
    - Break < 5m after â‰¥ 60m work â†’ **Yellow**.
    - Break > 30m between < 60m blocks â†’ **Blue** info.
    - Break\:Work ratio > 1:3 across session â†’ **Yellow**.
    - Total *planned* work time > 4h â†’ **Blue** info.

### 4.5 Failure & Recovery

**AC**

1. If an auto-save fails, show a toast + inline banner with **Retry**; maintain a last-known-good snapshot for undo.
2. If an optimistic reorder fails server-side, revert the UI and surface the cause.

---

## 5. UX Specification (Compose)

### 5.1 Timeline Editor

- Layout: Horizontal timeline on tablet/desktop; vertical list alternative on phones.
- Interactions: Drag & drop with haptic feedback; snap-to-5m grid; proportional block sizing.
- Header: Total **planned work** duration; markers every 15m.
- FAB (Speed Dial): **Add Work Block**, **Add Break** â€” both navigate to the existing **TimeBlock Editor screen**. No default duration/type/category is prefilled; required fields must be chosen before saving.
- **Editor Screen**: Reuse the existing **TimeBlock creation screen** for both create and edit. Opens full-screen; returns result to the timeline. Validation occurs inline on that screen.

### 5.2 Category Management

- Chips show assigned categories with X/3 counter.
- Category Picker: searchable grid; selecting the 4th shows limit message; at least one category required for Work Blocks. **Starts with 0 selected**; Save is disabled until â‰¥ 1 category is chosen.

### 5.3 States & System Feedback

- Unsaved-dot in TopAppBar when local edits pending; spinner on auto-save.
- Error banners with retry; undo/redo in overflow menu.
- Accessibility: min 48dp targets; content descriptions; dynamic type; high-contrast mode.

---

## 6. Validation Rules (Authoring)

| Rule                      | Constraint                                                            | Blocking? |
| ------------------------- | --------------------------------------------------------------------- | --------- |
| Blocks count              | 1â€“12                                                                  | Yes       |
| Work Block duration       | **Deep:** 25â€“120 min; **Shallow:** 10â€“60 min                          | Yes       |
| Break duration            | 5â€“60 minutes                                                          | Yes       |
| Break identification      | `blockType == BREAK` and categories count == 0                        | Yes       |
| No consecutive Breaks     | No two `BREAK` blocks adjacent                                        | Yes       |
| No Break at edges         | First and last TimeBlock must be non-`BREAK`                          | Yes       |
| Categories per Work Block | 1â€“3 (Save disabled until â‰¥1 selected); **no duplicates**              | Yes       |
| Name                      | 1â€“50, pattern `^[A-Za-z0-9][A-Za-z0-9 _-]*$`                          | Yes       |
| Total scheduled duration  | â‰¤ 12 hours                                                            | Yes       |
| Consecutive deep work     | â‰¤ 150 min (2.5h) of **DEEP\_WORK** with no intervening non-DEEP block | Yes       |

> Note: Category names validated separately (see Â§15 & Category domain rules). Duplicate categories within a single block are disallowed.

## 7.  Domain Model (Kotlin)

**Source of truth:** Use the existing domain models you've written:

- `ScheduledSession`
- `ScheduledTimeBlock`
- `SessionTemplate`
- `TimeBlockTemplate`

**Invariant:** `blockType` is **never null**. A **Break** is `blockType == BREAK` **and** an empty categories set. A **Work** block has `blockType âˆˆ {DEEP_WORK, SHALLOW_WORK}` **and** 1â€“3 categories.

We keep a `SessionDefinitionValidator` interface for authoring-time checks:

```kotlin
interface SessionDefinitionValidator {
    fun validate(def: SessionDefinition): List<ValidationError>
}
```

---

## 8. Error Messages & Copy

### 8.1 Inputs

- **Name required**: â€œAdd a session name (1â€“50 characters).â€
- **Name invalid**: â€œUse letters, numbers, spaces, - or \_. Start with a letter/number.â€

### 8.2 Structure

- **Break at start**: â€œBreaks must sit between work blocks.â€
- **Consecutive breaks**: â€œCombine these breaks or place one after a work block.â€
- **Too many categories**: â€œUp to 3 categories per block.â€

### 8.3 Advisory (Warnings panel)

- â€œLong work stretchâ€”consider a short break.â€
- â€œShort breakâ€”consider at least 5 minutes to reset.â€
- â€œLong breakâ€”might disrupt momentum.â€
- â€œLots of break time compared to work.â€
- â€œLong sessionâ€”consider splitting into shorter units.â€

---

## 9. Persistence & Schema (Room/SQLite)

We **unify** the timeline: a single `timeblock`/`scheduled_timeblock` table represents both Work and Break. `block_type` is **NOT NULL** for all rows and stores one of `DEEP_WORK`, `SHALLOW_WORK`, `BREAK`. A Break is a row with `block_type = 'BREAK'` **and** an empty categories set; Work rows must have `block_type IN ('DEEP_WORK','SHALLOW_WORK')` and 1â€“3 categories.

### 9.1 Tables (additions/changes)

- `timeblock_template`: add `block_type TEXT NOT NULL` (values: `DEEP_WORK`/`SHALLOW_WORK`/`BREAK`); keep existing FKs/positions/durations.
- `timeblock`: add `block_type TEXT NOT NULL`.
- Categories remain in the join table (e.g., `timeblock_template_category` / `timeblock_category`).

### 9.2 Integrity & Indices

- Unique `(session_id, position)` for `timeblock` and `(template_id, position)` for `timeblock_template`.
- **Check constraints (recommended):**
- `block_type IN ('DEEP_WORK','SHALLOW_WORK','BREAK')`
- When `block_type = 'BREAK'` â‡’ categories count = 0
- When `block_type IN ('DEEP_WORK','SHALLOW_WORK')` â‡’ categories count BETWEEN 1 AND 3
- FK: deleting a Session/Template cascades to its timeblocks and join rows.

### 9.3 Migrations

- If `block_type` previously allowed `NULL`, migrate to **NOT NULL** and set `BREAK` for rows with empty categories; set `DEEP_WORK` (or appropriate) for work rows based on prior data.
- If Breaks were stored in a separate table, migrate them into `timeblock` with `block_type = 'BREAK'` and empty categories.
- Add CHECK constraints where supported.

---

## 10. Repositories & Use Cases (Clean Architecture)

### 10.1 Repositories

```kotlin
interface TemplateRepository {
    suspend fun saveTemplate(def: SessionDefinition): Result<TemplateId>
    suspend fun loadTemplate(id: TemplateId): Result<SessionDefinition>
    suspend fun findByNameUnique(name: String): Result<Boolean>
    suspend fun listTemplates(): Result<List<TemplateSummary>>
}

interface SessionRepository {
    suspend fun saveSession(def: SessionDefinition, plannedStartEpochMs: Long?): Result<SessionId>
    suspend fun loadSession(id: SessionId): Result<SessionDefinition>
}
```

### 10.2 Use Cases

- `CreateBlankSession()` â†’ returns default `SessionDefinition`.
- `InstantiateFromTemplate(templateId)`.
- `AddTimeBlock(position?)` (create Work or Break via editor; Break = 0 categories).
- `UpdateDuration(itemId, minutes)`.
- `ReorderBlocks(draggedId, targetIndex)` (no adjacent Breaks; edges must be Work).
- `AssignCategories(blockId, categories)` (0 categories **forces** `blockType = BREAK`; â‰¥1 categories **requires** `blockType âˆˆ {DEEP_WORK, SHALLOW_WORK}`; enforce consistency).
- `SaveAsTemplate(name)`.
- `SaveAsSession(name, plannedStartEpochMs?)`.
- `Validate(def)` â†’ `AuthoringStatus`.

Each use case is pure/domain-first; ViewModel orchestrates and triggers persistence.

---

## 11. ViewModel & UI State (Compose + MVVM)

- `SessionEditorState`: immutable snapshot with `items: List<TimelineItem>` where item = `WorkBlockItem` or `BreakItem` with `localId`, `duration`, `position`, `warnings`, `isSaving`.
- `UiEvents`: `ShowSnackbar`, `OpenTimeBlockEditor(itemId)`, `NavigateBackWithConfirmIfDirty`.
- Auto-save debounce: 300ms after last edit; cancel on navigation.
- Undo/Redo via a bounded state stack (depth 20).

---

## 12. Telemetry & Analytics

- Event: `session_create_opened`, `block_added`, `break_added`, `duration_changed`, `categories_opened`, `categories_assigned`, `saved_as_template`, `saved_as_session`, `validation_failed`, `autosave_failed`.
- Properties: counts, durations, block\_type distribution, warning types encountered.

---

## 13. Performance & Offline

- All edits operate against local DB first (optimistic) and sync later.
- Target â‰¤ 100ms for validation; â‰¤ 16ms per frame during drag.
- Memory: keep only current session in memory; stream lists.

---

## 14. Security & Safety

- Sanitize all text inputs; reject HTML/script.
- Enforce name uniqueness for templates per user.
- Log authoring mutations for audit (local only unless sync enabled).

---

## 15. Accessibility & i18n

- Content descriptions for blocks: â€œWork block, 45 minutes, 2 categories.â€
- Announce warnings via accessibility live region.
- Externalize strings; support pluralization and right-to-left.

---

## 15A. Category Naming Rules (Domain)

- Category name must be non-blank and â‰¤ **NAME\_MAX\_LENGTH** (per `CategoryValidator`).
- Enforced at creation & rename; error surfaced inline.
- Category duplicates (same id) cannot be assigned twice to the same block.

---

## 16. Testing Strategy (JUnit4 + MockK + Compose UI Test)

### 16.1 Unit Tests

- Validator rules: every constraint and edge case.
- Use cases: position logic, break adjacency, rounding to minutes.

### 16.2 Integration Tests

- Repo save/load round trip; migrations adding `block_type`.
- Auto-save triggers and failure recovery.

### 16.3 UI Tests (Compose)

- Add/reorder/remove blocks & breaks; bottom sheet validations.
- Warnings appear/disappear as thresholds crossed.
- Unsaved-change dialog on back press when dirty.

### 16.4 Example GWT Scenarios

- **Reorder keeps break adjacency:**
    - Given WB1 â€“ Break â€“ WB2, when WB2 is dragged before WB1, then Break stays between WB2 and WB1.
- **Prevent break at edges:**
    - Given a single WB, when user tries to add Break before first WB, then show error copy and no insert occurs.

---

## 17. Release Plan & Feature Flags

- Gate the entire editor behind `ff_session_creation_v2`.
- Stage 1: authoring with unified TimeBlock model & save as template.
- Stage 2: save as concrete session (planned start optional).
- Stage 3: advanced warnings & analytics events.

---

## 18. Decisions & Remaining Questions

**Resolved**

1. **Break modeling:** Unifiedâ€”Breaks are TimeBlocks with **0 categories**.
2. **Default BlockType:** No data-level default; **Deep** is preselected in the creation UI.
3. **Session name uniqueness:** No special uniqueness constraints beyond standard name validation.

**Still Open** 4. **Minimum device grid:** 5-minute snap is proposedâ€”acceptable for UX?

---

## 19. Glossary

- **WB** â€“ Work Block
- **AC** â€“ Acceptance Criteria
- **FF** â€“ Feature Flag

---

## 20. Summary of Changes vs Old Docs

- Split **Authoring** vs **Execution** status to resolve conflicts.
- **Unified timeline model**: Breaks are TimeBlocks with 0 categories; removed separate Break table/structures.
- Added explicit **BlockType (DEEP\_WORK/SHALLOW\_WORK/BREAK)** on all timeblocks (non-null); breaks use `BREAK`.
- Finalized constraints (1â€“12 blocks, Deep 25â€“120m, Shallow 10â€“60m, Break 5â€“60m, â‰¤12h total, â‰¤2.5h consecutive deep).
- Clarified advisory warnings and copy.
- Defined migrations and test plan aligned with Clean Architecture.

---

## 21. Implementation Alignment with Existing Domain Use Cases

This section tracks alignment and gaps between the spec and current domain code.

**Aligned**

- **TimeBlock validation** uses `minDuration`/`maxDuration` and `requiresCategories`; duplicate-category check enforced in domain.
- **CreateSession/TimeBlock** generate IDs server-side/app-side when missing.
- **No consecutive breaks** and **max blocks** enforced.

**Gaps to Address**

1. **No Break at end**: ensure Add/Insert logic prevents a trailing `BREAK` when appending or inserting at final position.
2. **Consecutive deep vs any work**: clarify the cap applies to **DEEP\_WORK only**. Update consecutive calculation to sum only deep blocks.
3. **Duplicate Validation in UI**: ensure editor respects domain errors (durations, categories) and disables Save accordingly.
4. **Reorder Use Case**: add a domain use case for reordering with the same invariants (no adjacent breaks, no breaks at edges).

**Next Steps**

- Patch AddTimeBlock to enforce end-break rule and deep-only sum.
- Add ReorderTimeBlockUseCase + tests.
- Hook validator errors to Compose UI flows (snackbar + inline messages).
