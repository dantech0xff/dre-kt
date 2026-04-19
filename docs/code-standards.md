# Code Standards

## Language & Tooling

- Kotlin 2.3.0, Java 17
- Gradle 9.1.0 with version catalog (`gradle/libs.versions.toml`)
- Android: AGP 9.0.0, compileSdk 36, minSdk 26

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Classes/Interfaces | PascalCase | `DreStore`, `DreStoreViewModel` |
| Files | PascalCase (Kotlin) | `DreStore.kt`, `ReduceResult.kt` |
| Functions | camelCase | `dispatch()`, `executeAsyncOp()` |
| Constants | SCREAMING_SNAKE | — |
| Packages | lowercase dot-separated | `dev.drekt.core` |
| Test methods | backtick descriptive | `` `dispatch updates state`() `` |

## Architecture Patterns

### DRE Pattern Contract Types

All types participating in the DRE pipeline must implement marker interfaces:

```kotlin
data class MyState(...) : DreState
sealed interface MyAction : DreAction { ... }
sealed interface MyEffect : DreEffect { ... }
sealed interface MyAsyncOp : DreAsyncOp { ... }
```

### Reducer Rules

- MUST be pure — no I/O, no coroutines, no side effects
- MUST be deterministic — same input → same output
- Use `sealed interface` for Action/Effect/AsyncOp — exhaustive `when`
- Use guard clauses to reject actions in invalid states
- Return `ReduceResult` with default empty effects and null asyncOp

### ViewModel Rules

- Extend `DreStoreViewModel` (with async ops) or `SimpleDreStoreViewModel` (without)
- Public methods just call `dispatch(Action)` — no business logic
- `executeAsyncOp` MUST call `dispatch()` with result action
- `executeAsyncOp` MUST use `stateSnapshot`, NOT `state.value`
- `executeAsyncOp` MUST handle errors internally

### Side Effect Handlers

- One handler per concern (analytics, haptics, navigation)
- Filter effects internally — ignore irrelevant ones
- No dispatch callback — use async ops for feedback loops

## Module Boundaries

| From → To | Allowed? | Mechanism |
|-----------|----------|-----------|
| sample → dre-android | ✓ | `implementation` |
| dre-android → dre-core | ✓ | `api` (transitive) |
| dre-core → dre-android | ✗ | — |
| dre-core → Android SDK | ✗ | — |

## Testing Standards

### Reducer Tests
- Pure function — no coroutines needed
- Use `assertReduce` for positive cases
- Use `assertNoChange` for guard clause verification
- Test every `when` branch

### Store/ViewModel Tests
- Use `runTest` + `StandardTestDispatcher(testScheduler)`
- Use Turbine (`state.test { ... }`) for StateFlow assertions
- Call `advanceUntilIdle()` after dispatch
- For tests with `sideEffectHandlers`, call `store.close()` at end

### Test Dependencies
- JUnit 4, Google Truth, Turbine, kotlinx-coroutines-test

## Code Style

- Max file size: ~200 lines (split if larger)
- Use `data class` for state, `sealed interface` for ADTs
- Use `fun interface` for single-method interfaces (SAM conversion)
- Prefer `typealias` over wrapper classes for simple type aliases
- KDoc on public API — skip on obvious code
