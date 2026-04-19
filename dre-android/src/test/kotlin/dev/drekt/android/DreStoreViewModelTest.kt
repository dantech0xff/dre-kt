package dev.drekt.android

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.drekt.core.ReduceResult
import dev.drekt.core.Reducer
import dev.drekt.core.DreAction
import dev.drekt.core.DreAsyncOp
import dev.drekt.core.DreEffect
import dev.drekt.core.DreState
import dev.drekt.core.SideEffectHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DreStoreViewModelTest {

    // region Test types

    data class TestState(val count: Int = 0, val phase: String = "idle") : DreState

    sealed interface TestAction : DreAction {
        data object Increment : TestAction
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

    // endregion

    // region Test reducer

    private val testReducer = Reducer<TestState, TestAction, TestEffect, TestAsyncOp> { state, action ->
        when (action) {
            is TestAction.Increment -> ReduceResult(
                state = state.copy(count = state.count + 1),
                sideEffects = listOf(TestEffect.Log("incremented to ${state.count + 1}")),
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
                if (state.phase != "idle") ReduceResult(state, emptyList())
                else ReduceResult(state.copy(phase = "guarded"), emptyList())
            }
        }
    }

    // endregion

    // region Test ViewModel

    private class TestViewModel(
        reducer: Reducer<TestState, TestAction, TestEffect, TestAsyncOp>,
        handlers: List<SideEffectHandler<TestEffect>> = emptyList(),
        dispatchers: DreDispatchers,
    ) : DreStoreViewModel<TestState, TestAction, TestEffect, TestAsyncOp>(
        reducer = reducer,
        sideEffectHandlers = handlers,
        initialState = TestState(),
        dispatchers = dispatchers,
    ) {
        var lastAsyncOp: TestAsyncOp? = null
        var lastSnapshot: TestState? = null

        override suspend fun executeAsyncOp(op: TestAsyncOp, stateSnapshot: TestState) {
            lastAsyncOp = op
            lastSnapshot = stateSnapshot
            when (op) {
                is TestAsyncOp.LoadData -> {
                    dispatch(TestAction.AsyncDone("loaded"))
                }
            }
        }

        fun increment() = dispatch(TestAction.Increment)
        fun startAsync() = dispatch(TestAction.StartAsync)
        fun guardedAction() = dispatch(TestAction.GuardedAction)
    }

    // endregion

    // region Tests

    @Test
    fun `dispatch updates state`() = runTest {
        val vm = TestViewModel(testReducer, dispatchers = TestDreDispatchers(testScheduler))

        vm.state.test {
            assertThat(awaitItem()).isEqualTo(TestState()) // initial
            vm.increment()
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(TestState(count = 1, phase = "idle"))
        }
    }

    @Test
    fun `multiple dispatches serialize correctly`() = runTest {
        val vm = TestViewModel(testReducer, dispatchers = TestDreDispatchers(testScheduler))

        vm.state.test {
            awaitItem() // initial
            vm.increment()
            vm.increment()
            vm.increment()
            advanceUntilIdle()
            // Should see count go to 3
            val states = mutableListOf<TestState>()
            states.add(awaitItem())
            states.add(awaitItem())
            states.add(awaitItem())
            assertThat(states.last().count).isEqualTo(3)
        }
    }

    @Test
    fun `async op receives state snapshot`() = runTest {
        val vm = TestViewModel(testReducer, dispatchers = TestDreDispatchers(testScheduler))

        vm.state.test {
            awaitItem() // initial
            vm.startAsync()
            advanceUntilIdle()
            // Snapshot should be the state AFTER reduce (phase = "loading")
            assertThat(vm.lastSnapshot?.phase).isEqualTo("loading")
            assertThat(vm.lastAsyncOp).isEqualTo(TestAsyncOp.LoadData)
            // Final state after async dispatches back
            skipItems(1) // loading
            assertThat(awaitItem().phase).isEqualTo("loaded")
        }
    }

    @Test
    fun `side effects are delivered to handlers`() = runTest {
        val receivedEffects = mutableListOf<TestEffect>()
        val handler = SideEffectHandler<TestEffect> { effect -> receivedEffects.add(effect) }
        val vm = TestViewModel(
            testReducer,
            handlers = listOf(handler),
            dispatchers = TestDreDispatchers(testScheduler),
        )

        vm.state.test {
            awaitItem() // initial
            vm.increment()
            advanceUntilIdle()
            awaitItem() // count=1
        }

        assertThat(receivedEffects).hasSize(1)
        assertThat(receivedEffects.first()).isEqualTo(TestEffect.Log("incremented to 1"))
    }

    @Test
    fun `guard clause rejects action in wrong state`() = runTest {
        val vm = TestViewModel(testReducer, dispatchers = TestDreDispatchers(testScheduler))

        vm.state.test {
            awaitItem() // initial (phase=idle)
            vm.startAsync()
            advanceUntilIdle()
            skipItems(1) // loading
            awaitItem() // loaded

            // Now phase is "loaded", guard should reject
            vm.guardedAction()
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    // endregion
}
