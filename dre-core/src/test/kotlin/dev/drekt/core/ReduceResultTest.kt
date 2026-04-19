package dev.drekt.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReduceResultTest {

    @Test
    fun `destructuring works correctly`() {
        val result = ReduceResult(
            state = TestState(count = 5),
            sideEffects = listOf(TestEffect.Log("test")),
            asyncOp = TestAsyncOp.LoadData,
        )

        val (state, effects, op) = result
        assertThat(state.count).isEqualTo(5)
        assertThat(effects).hasSize(1)
        assertThat(op).isEqualTo(TestAsyncOp.LoadData)
    }

    @Test
    fun `defaults to empty effects and null async op`() {
        val result = ReduceResult<TestState, TestEffect, TestAsyncOp>(
            state = TestState(),
        )

        assertThat(result.sideEffects).isEmpty()
        assertThat(result.asyncOp).isNull()
    }

    @Test
    fun `SimpleReduceResult alias enforces no async op`() {
        val result: SimpleReduceResult<TestState, TestEffect> = ReduceResult(
            state = TestState(count = 1),
            sideEffects = listOf(TestEffect.Log("simple")),
        )

        assertThat(result.state.count).isEqualTo(1)
        assertThat(result.sideEffects.first()).isEqualTo(TestEffect.Log("simple"))
    }
}
