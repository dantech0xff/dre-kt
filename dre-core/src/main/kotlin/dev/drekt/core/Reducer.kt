package dev.drekt.core

/**
 * Pure function that computes state transitions.
 *
 * Given current [S] state and an [A] action, returns a [ReduceResult] containing:
 * - **New state** — the updated state after applying the action
 * - **Side effects** — fire-and-forget effects (analytics, haptics, timer control)
 * - **Async operation** — optional I/O work that dispatches a result action when complete
 *
 * Contract:
 * - MUST be a pure function — no I/O, no coroutines, no side effects
 * - MUST be deterministic — same input always produces same output
 * - SHOULD use guard clauses to reject actions in invalid states
 *
 * ```kotlin
 * class MyReducer : Reducer<MyState, MyAction, MyEffect, MyAsyncOp> {
 *     override fun reduce(state: MyState, action: MyAction) = when (action) {
 *         is MyAction.Submit -> ReduceResult(
 *             state = state.copy(phase = Phase.Processing),
 *             sideEffects = listOf(MyEffect.LogAnalytics("submitted")),
 *             asyncOp = MyAsyncOp.CallApi(action.data),
 *         )
 *     }
 * }
 * ```
 *
 * @param S [DreState] — single source of truth for the screen
 * @param A [DreAction] — all possible events (user, system, async results)
 * @param E [DreEffect] — fire-and-forget effects handled by [SideEffectHandler]
 * @param O [DreAsyncOp] — I/O work that dispatches result actions back
 */
fun interface Reducer<S : DreState, A : DreAction, E : DreEffect, O : DreAsyncOp> {
    fun reduce(state: S, action: A): ReduceResult<S, E, O>
}

/**
 * Convenience reducer for screens that don't need async operations.
 *
 * Eliminates the need to specify `Nothing` for the async op type parameter.
 *
 * ```kotlin
 * class SettingsReducer : SimpleReducer<SettingsState, SettingsAction, SettingsEffect> {
 *     override fun reduce(state: SettingsState, action: SettingsAction) =
 *         SimpleReduceResult(state = newState, sideEffects = listOf(...))
 * }
 * ```
 */
fun interface SimpleReducer<S : DreState, A : DreAction, E : DreEffect> {
    fun reduce(state: S, action: A): SimpleReduceResult<S, E>
}
