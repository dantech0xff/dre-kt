package dev.drekt.sample

import dev.drekt.core.DreAction
import dev.drekt.core.DreAsyncOp
import dev.drekt.core.DreEffect
import dev.drekt.core.DreState

/** Screen state for the counter. */
data class CounterState(
    val count: Int = 0,
    val loading: Boolean = false,
    val message: String? = null,
) : DreState

/** All possible user/system events. */
sealed interface CounterAction : DreAction {
    data object Increment : CounterAction
    data object Decrement : CounterAction
    data object Reset : CounterAction
    data object LoadRandom : CounterAction
    data class RandomLoaded(val value: Int) : CounterAction
    data class LoadFailed(val error: String) : CounterAction
    data object DismissMessage : CounterAction
}

/** Fire-and-forget side effects. */
sealed interface CounterEffect : DreEffect {
    data class ShowToast(val text: String) : CounterEffect
}

/** Async I/O operations. */
sealed interface CounterAsyncOp : DreAsyncOp {
    data object FetchRandom : CounterAsyncOp
}
