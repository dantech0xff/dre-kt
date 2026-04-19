# Project Overview — dre-kt

## Product Summary

**dre-kt** is a lightweight Kotlin state management library implementing the Dispatch → Reduce → Effects (DRE) pattern. Designed for Android apps with Jetpack Compose, also usable in any Kotlin/JVM project.

## Goals

1. Provide a simple, testable, unidirectional state management solution
2. Keep reducers pure — no coroutines, no side effects, fully deterministic
3. Support async operations without leaking I/O into the state machine
4. Platform-agnostic core with thin Android integration layer

## Non-Goals

- Not a full framework — no navigation, no DI, no networking
- Not a replacement for MVI libraries with middleware/interceptors
- No multiplatform (KMP) support yet — core is JVM-only for now

## Module Structure

| Module | Purpose | Key Classes |
|--------|---------|-------------|
| `dre-core` | Dispatch loop, types, test utilities | `DreStore`, `Reducer`, `ReduceResult`, `SideEffectHandler` |
| `dre-android` | Android ViewModel binding | `DreStoreViewModel`, `SimpleDreStoreViewModel` |
| `sample` | Counter demo app | `CounterViewModel`, `CounterReducer` |

## Key Design Decisions

### Marker Interfaces over `Any`
Types (`DreState`, `DreAction`, `DreEffect`, `DreAsyncOp`) are marker interfaces — prevents accidentally passing wrong types into the DRE pipeline.

### Single-Writer via Dispatcher
State mutations serialize on the injected `CoroutineDispatcher` (defaults to `Dispatchers.Main.immediate`) — no mutex/lock needed. The dispatcher IS the synchronization mechanism.

### Async Ops as Data
Async operations are sealed classes returned by the reducer, not launched inside it. This keeps reducers pure and testable.

### Side Effects are Fire-and-Forget
`SideEffectHandler` has no dispatch callback. If you need to feed results back, use an async op instead.

### DreStore vs DreStoreViewModel
`DreStore` owns the dispatch loop (platform-agnostic). `DreStoreViewModel` wraps it with Android lifecycle. Clean separation.

## Target Audience

- Android developers using Jetpack Compose
- Teams wanting predictable state management
- Projects that need testable business logic

## Tech Stack

- Kotlin 2.3.0
- kotlinx.coroutines 1.10.2
- AndroidX Lifecycle 2.10.0
- Compose BOM 2026.01.00
- AGP 9.0.0, Gradle 9.1.0

## Testing Strategy

- **Reducers**: Pure function tests — no coroutines, use `assertReduce`/`assertNoChange`
- **DreStore**: Coroutine tests with `runTest` + Turbine for StateFlow
- **ViewModels**: Same as DreStore + `StandardTestDispatcher(testScheduler)`

## Current Test Coverage

- dre-core: 17 tests (DreStore, ReducerTestExtensions, ReduceResult)
- dre-android: 10 tests (DreStoreViewModel, SimpleDreStoreViewModel)
- Total: 27 tests, 0 failures
