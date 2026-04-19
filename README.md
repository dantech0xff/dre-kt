# dre-kt

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dantech0xff/dre-core)](https://central.sonatype.com/namespace/io.github.dantech0xff)

**Dispatch → Reduce → Effects** — A lightweight, opinionated state management library for Kotlin & Android.

## Overview

dre-kt implements the DRE pattern: a unidirectional data flow architecture where **Actions** are dispatched, a pure **Reducer** computes new state + side effects, and **Effects** are handled externally. Async operations feed results back as new actions.

## Installation

### Gradle

Add to your `build.gradle.kts`:

Check the latest version on [Maven Central](https://central.sonatype.com/namespace/io.github.dantech0xff).

```kotlin
dependencies {
    // Core only (no Android dependency)
    implementation("io.github.dantech0xff:dre-core:<latest-version>")

    // Android ViewModel integration (includes dre-core)
    implementation("io.github.dantech0xff:dre-android:<latest-version>")
}
```

### Claude Code Skill

Install the `dre-integrate` skill to let Claude Code help you integrate dre-kt:

```bash
curl -fsSL https://raw.githubusercontent.com/dantech0xff/dre-kt/master/install-skill.sh | bash
```

Then use in Claude Code:
- `/dre-integrate setup` — Add dependency to your project
- `/dre-integrate feature login` — Scaffold a new feature
- `/dre-integrate migrate` — Migrate existing ViewModel to DRE

## Modules

| Module | Description | Dependencies |
|--------|-------------|--------------|
| `dre-core` | Platform-agnostic dispatch loop, reducer, and types | `kotlinx-coroutines` |
| `dre-android` | Android ViewModel integration | `dre-core`, `lifecycle-viewmodel` |
| `sample` | Counter app demonstrating the DRE pattern | `dre-android`, Compose |

## Quick Start

### 1. Define your contract

```kotlin
data class MyState(val count: Int = 0) : DreState

sealed interface MyAction : DreAction {
    data object Increment : MyAction
    data class DataLoaded(val value: Int) : MyAction
}

sealed interface MyEffect : DreEffect {
    data class ShowToast(val msg: String) : MyEffect
}

sealed interface MyAsyncOp : DreAsyncOp {
    data object FetchData : MyAsyncOp
}
```

### 2. Write a pure reducer

```kotlin
class MyReducer : Reducer<MyState, MyAction, MyEffect, MyAsyncOp> {
    override fun reduce(state: MyState, action: MyAction) = when (action) {
        is MyAction.Increment -> ReduceResult(state.copy(count = state.count + 1))
        is MyAction.DataLoaded -> ReduceResult(
            state = state.copy(count = action.value),
            sideEffects = listOf(MyEffect.ShowToast("Loaded!")),
        )
    }
}
```

### 3. Create a ViewModel (Android)

```kotlin
class MyViewModel(reducer: MyReducer) : DreStoreViewModel<MyState, MyAction, MyEffect, MyAsyncOp>(
    reducer = reducer,
) {
    override val initialState = MyState()

    override suspend fun executeAsyncOp(op: MyAsyncOp, stateSnapshot: MyState) {
        when (op) {
            is MyAsyncOp.FetchData -> {
                val data = repository.load()
                dispatch(MyAction.DataLoaded(data))
            }
        }
    }

    fun increment() = dispatch(MyAction.Increment)
}
```

### 4. Or use DreStore directly (no ViewModel)

```kotlin
val store = DreStore(
    reducer = MyReducer(),
    initialState = MyState(),
    scope = coroutineScope,
    dispatchContext = Dispatchers.Main.immediate,
    onAsyncOp = { op, snapshot -> /* handle async */ },
)

store.dispatch(MyAction.Increment)
store.state.collect { /* observe */ }
```

## Architecture

```
Action ──→ Reducer (pure) ──→ ReduceResult
               │                    │
               │              ┌─────┼──────────┐
               │              ▼     ▼           ▼
               │           State  Effects    AsyncOp
               │           (Flow)  (handlers)  (I/O)
               │                                │
               └────────────────────────────────┘
                        (result action)
```

## Testing

Reducers are pure functions — test without coroutines:

```kotlin
@Test fun `increment updates count`() {
    reducer.assertReduce(given = MyState(0), action = MyAction.Increment) {
        assertThat(state.count).isEqualTo(1)
    }
}

@Test fun `action rejected in wrong state`() {
    reducer.assertNoChange(MyState(phase = "loading"), MyAction.Submit)
}
```

## Requirements

- Kotlin 2.3+
- Android: minSdk 26, compileSdk 36
- Java 17

## License

MIT License — free to use, modify, distribute. Do whatever you want with it.

Just give me a star if you find it useful.

See [LICENSE](LICENSE) for details.
