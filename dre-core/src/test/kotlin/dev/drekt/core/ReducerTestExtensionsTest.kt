package dev.drekt.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReducerTestExtensionsTest {

    @Test
    fun `assertReduce provides result in block`() {
        testReducer.assertReduce(
            given = TestState(count = 0),
            action = TestAction.Increment,
        ) {
            assertThat(state.count).isEqualTo(1)
            assertThat(sideEffects).containsExactly(TestEffect.Log("incremented to 1"))
            assertThat(asyncOp).isNull()
        }
    }

    @Test
    fun `assertReduce with async op`() {
        testReducer.assertReduce(
            given = TestState(),
            action = TestAction.StartAsync,
        ) {
            assertThat(state.phase).isEqualTo("loading")
            assertThat(asyncOp).isEqualTo(TestAsyncOp.LoadData)
        }
    }

    @Test
    fun `assertNoChange passes for guard clause`() {
        val state = TestState(phase = "not-idle")
        testReducer.assertNoChange(state, TestAction.GuardedAction)
    }

    @Test(expected = IllegalStateException::class)
    fun `assertNoChange fails when state changes`() {
        testReducer.assertNoChange(TestState(), TestAction.Increment)
    }

    @Test(expected = IllegalStateException::class)
    fun `assertNoChange fails when side effects emitted`() {
        // Increment produces side effects
        val reducer = Reducer<TestState, TestAction, TestEffect, TestAsyncOp> { state, _ ->
            ReduceResult(state, sideEffects = listOf(TestEffect.Log("oops")))
        }
        reducer.assertNoChange(TestState(), TestAction.Increment)
    }

    @Test(expected = IllegalStateException::class)
    fun `assertNoChange fails when async op returned`() {
        val reducer = Reducer<TestState, TestAction, TestEffect, TestAsyncOp> { state, _ ->
            ReduceResult(state, asyncOp = TestAsyncOp.LoadData)
        }
        reducer.assertNoChange(TestState(), TestAction.Increment)
    }
}
