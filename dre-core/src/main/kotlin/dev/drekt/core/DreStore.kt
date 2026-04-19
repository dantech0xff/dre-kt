package dev.drekt.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Platform-agnostic dispatch loop for the DRE pattern.
 *
 * Owns:
 * - State ([StateFlow]) — reduce result applied synchronously
 * - Side effects — fanned out to [SideEffectHandler]s in parallel
 * - Async op kick-off — delegates to [onAsyncOp] callback
 *
 * Thread safety: all mutations run on [dispatchContext] (single-threaded dispatcher).
 *
 * @param reducer Pure function computing state transitions
 * @param initialState Starting state
 * @param scope Coroutine scope controlling the store's lifetime
 * @param dispatchContext Single-threaded context for serializing dispatch calls
 * @param sideEffectHandlers Handlers consuming fire-and-forget effects
 * @param onAsyncOp Callback for executing async operations — receives (op, stateSnapshot)
 */
class DreStore<S : DreState, A : DreAction, E : DreEffect, O : DreAsyncOp>(
    private val reducer: Reducer<S, A, E, O>,
    initialState: S,
    private val scope: CoroutineScope,
    private val dispatchContext: CoroutineContext,
    sideEffectHandlers: List<SideEffectHandler<E>> = emptyList(),
    private val onAsyncOp: (suspend (O, S) -> Unit)? = null,
) {
    private val _state = MutableStateFlow(initialState)

    /** Current state. Observe from UI via `collectAsState()`. */
    val state: StateFlow<S> = _state.asStateFlow()

    private val _sideEffects = Channel<E>(Channel.BUFFERED)

    init {
        if (sideEffectHandlers.isNotEmpty()) {
            scope.launch {
                for (effect in _sideEffects) {
                    sideEffectHandlers.forEach { handler ->
                        launch { handler.handle(effect) }
                    }
                }
            }
        }
    }

    /**
     * Dispatch an action into the reduce loop.
     *
     * 1. Calls [reducer].reduce(currentState, action) — pure, synchronous
     * 2. Updates state — synchronous
     * 3. Sends side effects to handlers — synchronous (trySend)
     * 4. Kicks off async op in child coroutine if present — non-blocking
     */
    fun dispatch(action: A) {
        scope.launch(dispatchContext) {
            val (newState, effects, asyncOp) = reducer.reduce(_state.value, action)
            _state.value = newState

            effects.forEach { effect ->
                _sideEffects.trySend(effect)
            }

            asyncOp?.let { op ->
                launch { onAsyncOp?.invoke(op, newState) }
            }
        }
    }

    /** Close the side effect channel. Call when the store's owner is destroyed. */
    fun close() {
        _sideEffects.close()
    }
}
