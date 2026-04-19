package dev.drekt.core

/**
 * Test DSL for asserting reducer output without coroutines.
 *
 * ```kotlin
 * @Test fun `submit transitions to processing`() {
 *     reducer.assertReduce(
 *         given = MyState(phase = Phase.Active),
 *         action = MyAction.Submit(data),
 *     ) {
 *         assertThat(state.phase).isEqualTo(Phase.Processing)
 *         assertThat(sideEffects).contains(MyEffect.PauseTimer)
 *         assertThat(asyncOp).isEqualTo(MyAsyncOp.CallApi(data))
 *     }
 * }
 * ```
 */
fun <S : DreState, A : DreAction, E : DreEffect, O : DreAsyncOp> Reducer<S, A, E, O>.assertReduce(
    given: S,
    action: A,
    block: ReduceResult<S, E, O>.() -> Unit,
) {
    reduce(given, action).block()
}

/**
 * Assert that a reduce call produces no state change, no effects, and no async op.
 * Useful for testing guard clauses that reject actions in invalid states.
 *
 * ```kotlin
 * @Test fun `submit while loading is ignored`() {
 *     val state = MyState(phase = Phase.Loading)
 *     reducer.assertNoChange(state, MyAction.Submit(data))
 * }
 * ```
 */
fun <S : DreState, A : DreAction, E : DreEffect, O : DreAsyncOp> Reducer<S, A, E, O>.assertNoChange(
    given: S,
    action: A,
) {
    val result = reduce(given, action)
    check(result.state == given) {
        "Expected no state change, but state changed:\n  before: $given\n  after:  ${result.state}"
    }
    check(result.sideEffects.isEmpty()) {
        "Expected no side effects, but got: ${result.sideEffects}"
    }
    check(result.asyncOp == null) {
        "Expected no async op, but got: ${result.asyncOp}"
    }
}
