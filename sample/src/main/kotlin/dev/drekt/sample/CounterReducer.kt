package dev.drekt.sample

import dev.drekt.core.ReduceResult
import dev.drekt.core.Reducer

/** Pure function — no I/O, fully testable. */
class CounterReducer : Reducer<CounterState, CounterAction, CounterEffect, CounterAsyncOp> {

    override fun reduce(
        state: CounterState,
        action: CounterAction,
    ): ReduceResult<CounterState, CounterEffect, CounterAsyncOp> = when (action) {

        is CounterAction.Increment -> ReduceResult(
            state = state.copy(count = state.count + 1),
        )

        is CounterAction.Decrement -> ReduceResult(
            state = state.copy(count = state.count - 1),
        )

        is CounterAction.Reset -> ReduceResult(
            state = state.copy(count = 0),
            sideEffects = listOf(CounterEffect.ShowToast("Counter reset")),
        )

        is CounterAction.LoadRandom -> {
            if (state.loading) ReduceResult(state)
            else ReduceResult(
                state = state.copy(loading = true),
                asyncOp = CounterAsyncOp.FetchRandom,
            )
        }

        is CounterAction.RandomLoaded -> ReduceResult(
            state = state.copy(count = action.value, loading = false),
            sideEffects = listOf(CounterEffect.ShowToast("Loaded: ${action.value}")),
        )

        is CounterAction.LoadFailed -> ReduceResult(
            state = state.copy(loading = false, message = action.error),
        )

        is CounterAction.DismissMessage -> ReduceResult(
            state = state.copy(message = null),
        )
    }
}
