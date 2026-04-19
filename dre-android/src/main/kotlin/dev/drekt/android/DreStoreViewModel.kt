package dev.drekt.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.drekt.core.DreAction
import dev.drekt.core.DreAsyncOp
import dev.drekt.core.DreEffect
import dev.drekt.core.DreState
import dev.drekt.core.DreStore
import dev.drekt.core.Reducer
import dev.drekt.core.SideEffectHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

/**
 * Android [ViewModel] wrapper around [DreStore].
 *
 * Binds [DreStore] to [viewModelScope] and the injected [CoroutineDispatcher].
 * The dispatch loop is platform-agnostic in [DreStore] — this class only
 * provides lifecycle scoping and the abstract [executeAsyncOp] hook.
 *
 * ```kotlin
 * class MyViewModel(reducer: MyReducer) : DreStoreViewModel<MyState, MyAction, MyEffect, MyAsyncOp>(
 *     reducer = reducer,
 *     initialState = MyState.initial,
 * ) {
 *     override suspend fun executeAsyncOp(op: MyAsyncOp, stateSnapshot: MyState) {
 *         when (op) {
 *             is MyAsyncOp.LoadData -> {
 *                 val data = repository.load()
 *                 dispatch(MyAction.DataLoaded(data))
 *             }
 *         }
 *     }
 *
 *     fun loadData() = dispatch(MyAction.StartLoading)
 * }
 * ```
 */
abstract class DreStoreViewModel<S : DreState, A : DreAction, E : DreEffect, O : DreAsyncOp>(
    reducer: Reducer<S, A, E, O>,
    sideEffectHandlers: List<SideEffectHandler<E>> = emptyList(),
    initialState: S,
    dispatchContext: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel() {

    private val store = DreStore(
        reducer = reducer,
        initialState = initialState,
        scope = viewModelScope,
        dispatchContext = dispatchContext,
        sideEffectHandlers = sideEffectHandlers,
        onAsyncOp = { op, snapshot -> executeAsyncOp(op, snapshot) },
    )

    /** Current state. Observe from UI via `collectAsState()`. */
    val state: StateFlow<S> = store.state

    /** Dispatch an action into the reduce loop. */
    protected fun dispatch(action: A) = store.dispatch(action)

    /**
     * Handle an async operation. Override to implement domain-specific I/O.
     *
     * Contract:
     * - MUST call [dispatch] with a result action when the operation completes
     * - MUST handle errors internally (catch + dispatch error action)
     * - MUST use [stateSnapshot] for data, NOT [state].value (race condition)
     *
     * @param op The async operation to execute
     * @param stateSnapshot State captured at dispatch time
     */
    protected abstract suspend fun executeAsyncOp(op: O, stateSnapshot: S)

    override fun onCleared() {
        super.onCleared()
        store.close()
    }
}
