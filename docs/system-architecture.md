# System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────┐
│                    sample app                    │
│  (Compose UI → ViewModel → dispatch actions)     │
└──────────────────────┬──────────────────────────┘
                       │ depends on
┌──────────────────────▼──────────────────────────┐
│                  dre-android                     │
│  DreStoreViewModel ─── wraps ──→ DreStore        │
│  SimpleDreStoreViewModel                         │
│                                                  │
└──────────────────────┬──────────────────────────┘
                       │ depends on (api)
┌──────────────────────▼──────────────────────────┐
│                   dre-core                       │
│  DreStore (dispatch loop)                        │
│  Reducer / SimpleReducer                         │
│  ReduceResult / SimpleReduceResult               │
│  SideEffectHandler                               │
│  DreState / DreAction / DreEffect / DreAsyncOp   │
└─────────────────────────────────────────────────┘
```

## Data Flow

```
UI Event
  │
  ▼
dispatch(Action)
  │
  ▼ (on mainImmediate dispatcher)
┌─────────────────────────────────┐
│  Reducer.reduce(state, action)  │  ← pure, synchronous
│  → ReduceResult                 │
│     ├── state: S                │
│     ├── sideEffects: List<E>    │
│     └── asyncOp: O?             │
└──────┬──────────┬───────────┬───┘
       │          │           │
       ▼          ▼           ▼
   StateFlow   Channel    launch { }
   .value =    .trySend   onAsyncOp(op, snapshot)
   newState    (effect)       │
       │          │           │
       ▼          ▼           ▼
   UI update  Handlers    I/O work
   (Compose)  (parallel)     │
                             ▼
                      dispatch(ResultAction)
                             │
                             └──→ (back to top)
```

## Component Responsibilities

### DreStore (dre-core)

Platform-agnostic dispatch loop. Owns:
- `MutableStateFlow<S>` — single source of truth
- `Channel<E>` — buffered side effect channel
- Dispatch serialization via `dispatchContext`
- Side effect fan-out to handlers
- Async op delegation via `onAsyncOp` callback

Lifecycle: caller manages via `CoroutineScope` + `close()`.

### Reducer (dre-core)

Pure function: `(State, Action) → ReduceResult(State, Effects, AsyncOp?)`.
No I/O, no coroutines, no side effects. Fully testable without infrastructure.

### SideEffectHandler (dre-core)

Fire-and-forget effect consumer. Each handler owns one concern. Runs in parallel. No dispatch callback — use async ops for feedback.

### DreStoreViewModel (dre-android)

Thin wrapper binding `DreStore` to Android `ViewModel`:
- Provides `viewModelScope` as store's coroutine scope
- Provides injected `CoroutineDispatcher` as dispatch context (defaults to `Dispatchers.Main.immediate`)
- Exposes abstract `executeAsyncOp` for subclass I/O handling
- Calls `store.close()` in `onCleared()`

## Thread Safety Model

All state mutations run on a single-threaded dispatcher (`mainImmediate`).
This serializes all `dispatch` calls — no lock/mutex needed.

```
Thread A: dispatch(A1) ──→ [queued on mainImmediate]
Thread B: dispatch(A2) ──→ [queued on mainImmediate]

mainImmediate executes: A1 → reduce → update state
                        A2 → reduce → update state (sees A1's result)
```

Async ops launch in child coroutines. When done, they call `dispatch()` which re-enters the serialized queue. No race conditions.

## Extension Points

1. **New platform**: Create module wrapping `DreStore` with platform lifecycle (e.g., `dre-ios`, `dre-desktop`)
2. **Middleware**: Not supported by design — keep reducers pure, use side effect handlers
3. **Persistence**: Implement as `SideEffectHandler` that saves state on specific effects
4. **Logging**: Implement as `SideEffectHandler` that logs all effects
