# CLAUDE.md

## Project

**dre-kt** — Dispatch → Reduce → Effects state management library for Kotlin & Android.

## Modules

- `dre-core`: Platform-agnostic dispatch loop (`DreStore`, `Reducer`, `ReduceResult`, `SideEffectHandler`)
- `dre-android`: Android ViewModel integration (`DreStoreViewModel`, `SimpleDreStoreViewModel`)
- `sample`: Counter demo app with Compose UI

## Build & Test

```bash
./gradlew :dre-core:test                    # Core unit tests
./gradlew :dre-android:testDebugUnitTest    # Android unit tests
./gradlew :sample:assembleDebug             # Build sample APK
```

## Architecture

- **DRE pattern**: Action → Reducer (pure) → ReduceResult (state + effects + asyncOp?)
- **Thread safety**: All mutations serialize on `mainImmediate` dispatcher — no mutex
- **Marker interfaces**: `DreState`, `DreAction`, `DreEffect`, `DreAsyncOp` — type safety enforced at compile time
- **DreStore** (core) owns dispatch loop; **DreStoreViewModel** (android) wraps with lifecycle

## Code Conventions

- Reducers MUST be pure — no I/O, no coroutines
- `sealed interface` for Action/Effect/AsyncOp types
- `data class` for State
- Tests: JUnit 4 + Truth + Turbine + `StandardTestDispatcher`
- Tests with `sideEffectHandlers` must call `store.close()` before test ends

## Documentation

- `docs/project-overview-pdr.md` — Goals, design decisions, tech stack
- `docs/codebase-summary.md` — Directory structure, dependencies
- `docs/code-standards.md` — Naming, patterns, module boundaries
- `docs/system-architecture.md` — Data flow, thread safety, extension points
