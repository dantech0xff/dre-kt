package dev.drekt.core

/**
 * Output of [Reducer.reduce] — a triple of (state, sideEffects, asyncOp?).
 *
 * This is the architectural signature of the DRE pattern:
 * - **[state]** — new state to render. Applied immediately and synchronously.
 * - **[sideEffects]** — fire-and-forget work. Handled by [SideEffectHandler]s in parallel.
 *   Does NOT affect state. Examples: analytics, haptics, timer, toast.
 * - **[asyncOp]** — optional I/O work. Executed by ViewModel, dispatches result action
 *   back into the reduce loop when complete. Max 1 per reduce call.
 *
 * Supports destructuring:
 * ```kotlin
 * val (newState, effects, asyncOp) = reducer.reduce(state, action)
 * ```
 *
 * @param S State type
 * @param E Side effect type
 * @param O Async operation type
 */
data class ReduceResult<S : DreState, E : DreEffect, O : DreAsyncOp>(
    val state: S,
    val sideEffects: List<E> = emptyList(),
    val asyncOp: O? = null,
)

/**
 * [ReduceResult] for reducers that don't use async operations.
 * The async op type is [Nothing] — compiler enforces no async op can be set.
 */
typealias SimpleReduceResult<S, E> = ReduceResult<S, E, Nothing>
