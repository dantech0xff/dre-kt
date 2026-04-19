package dev.drekt.sample

import dev.drekt.android.DreStoreViewModel
import dev.drekt.core.SideEffectHandler
import kotlinx.coroutines.delay

class CounterViewModel(
    reducer: CounterReducer = CounterReducer(),
) : DreStoreViewModel<CounterState, CounterAction, CounterEffect, CounterAsyncOp>(
    reducer = reducer,
) {
    override val initialState: CounterState
        get() = CounterState()

    override val sideEffectHandlers: List<SideEffectHandler<CounterEffect>>
        get() = emptyList()

    override suspend fun executeAsyncOp(op: CounterAsyncOp, stateSnapshot: CounterState) {
        when (op) {
            is CounterAsyncOp.FetchRandom -> {
                try {
                    delay(1_000) // simulate network
                    val random = (1..100).random()
                    dispatch(CounterAction.RandomLoaded(random))
                } catch (e: Exception) {
                    dispatch(CounterAction.LoadFailed(e.message ?: "Unknown error"))
                }
            }
        }
    }

    fun increment() = dispatch(CounterAction.Increment)
    fun decrement() = dispatch(CounterAction.Decrement)
    fun reset() = dispatch(CounterAction.Reset)
    fun loadRandom() = dispatch(CounterAction.LoadRandom)
    fun dismissMessage() = dispatch(CounterAction.DismissMessage)
}
