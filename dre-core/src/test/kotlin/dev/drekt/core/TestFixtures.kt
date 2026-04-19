package dev.drekt.core

/** Shared test types for dre-core tests. */

data class TestState(val count: Int = 0, val phase: String = "idle") : DreState

sealed interface TestAction : DreAction {
    data object Increment : TestAction
    data object Decrement : TestAction
    data class SetPhase(val phase: String) : TestAction
    data object StartAsync : TestAction
    data class AsyncDone(val result: String) : TestAction
    data object GuardedAction : TestAction
}

sealed interface TestEffect : DreEffect {
    data class Log(val message: String) : TestEffect
}

sealed interface TestAsyncOp : DreAsyncOp {
    data object LoadData : TestAsyncOp
}

val testReducer = Reducer<TestState, TestAction, TestEffect, TestAsyncOp> { state, action ->
    when (action) {
        is TestAction.Increment -> ReduceResult(
            state = state.copy(count = state.count + 1),
            sideEffects = listOf(TestEffect.Log("incremented to ${state.count + 1}")),
        )

        is TestAction.Decrement -> ReduceResult(
            state = state.copy(count = state.count - 1),
        )

        is TestAction.SetPhase -> ReduceResult(
            state = state.copy(phase = action.phase),
        )

        is TestAction.StartAsync -> ReduceResult(
            state = state.copy(phase = "loading"),
            sideEffects = listOf(TestEffect.Log("loading started")),
            asyncOp = TestAsyncOp.LoadData,
        )

        is TestAction.AsyncDone -> ReduceResult(
            state = state.copy(phase = action.result),
        )

        is TestAction.GuardedAction -> {
            if (state.phase != "idle") ReduceResult(state)
            else ReduceResult(state.copy(phase = "guarded"))
        }
    }
}
