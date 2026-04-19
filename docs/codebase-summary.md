# Codebase Summary

## Directory Structure

```
dre-kt/
├── dre-core/                          # Platform-agnostic core
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/dev/drekt/core/
│       │   ├── DreTypes.kt            # Marker interfaces (DreState, DreAction, DreEffect, DreAsyncOp)
│       │   ├── DreStore.kt            # Dispatch loop — state, effects, async ops
│       │   ├── Reducer.kt             # Reducer + SimpleReducer fun interfaces
│       │   ├── ReduceResult.kt        # ReduceResult data class + SimpleReduceResult alias
│       │   ├── SideEffectHandler.kt   # Fire-and-forget effect handler
│       │   └── ReducerTestExtensions.kt # assertReduce, assertNoChange test DSL
│       └── test/kotlin/dev/drekt/core/
│           ├── TestFixtures.kt        # Shared test types and reducer
│           ├── DreStoreTest.kt        # 10 tests
│           ├── ReducerTestExtensionsTest.kt # 6 tests
│           └── ReduceResultTest.kt    # 3 tests
│
├── dre-android/                       # Android ViewModel integration
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/dev/drekt/android/
│       │   ├── DreStoreViewModel.kt   # ViewModel wrapping DreStore
│       │   └── SimpleDreStoreViewModel.kt # No-async-op convenience ViewModel
│       └── test/kotlin/dev/drekt/android/
│           ├── DreStoreViewModelTest.kt # 5 tests
│           └── SimpleDreStoreViewModelTest.kt # 5 tests
│
├── sample/                            # Counter demo app
│   ├── build.gradle.kts
│   └── src/main/kotlin/dev/drekt/sample/
│       ├── CounterContract.kt         # State, Action, Effect, AsyncOp definitions
│       ├── CounterReducer.kt          # Pure reducer
│       ├── CounterViewModel.kt        # DreStoreViewModel subclass
│       └── MainActivity.kt           # Compose UI
│
├── gradle/libs.versions.toml         # Version catalog
├── build.gradle.kts                  # Root build script
├── settings.gradle.kts               # Module includes
└── README.md                         # Project documentation
```

## File Count & Size

| Module | Source files | Test files | Total |
|--------|-------------|------------|-------|
| dre-core | 6 | 4 | 10 |
| dre-android | 4 | 3 | 7 |
| sample | 4 | 0 | 4 |
| **Total** | **14** | **7** | **21** |

## Dependencies

### dre-core
- `kotlinx-coroutines-core` (runtime)
- `junit`, `truth`, `turbine`, `kotlinx-coroutines-test` (test)

### dre-android
- `dre-core` (api — transitive)
- `androidx-lifecycle-viewmodel` (runtime)
- `kotlinx-coroutines-android` (runtime)
- `junit`, `truth`, `turbine`, `kotlinx-coroutines-test` (test)

### sample
- `dre-android` (runtime)
- Compose BOM, Material3, activity-compose, lifecycle-viewmodel-compose (runtime)

## Package Structure

- `dev.drekt.core` — core library types and dispatch loop
- `dev.drekt.android` — Android-specific ViewModel wrappers
- `dev.drekt.sample` — demo application
