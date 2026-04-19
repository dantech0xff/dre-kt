package dev.drekt.android

import dev.drekt.core.DreAction
import dev.drekt.core.DreEffect
import dev.drekt.core.DreState
import dev.drekt.core.Reducer
import dev.drekt.core.ReduceResult
import dev.drekt.core.SideEffectHandler
import dev.drekt.core.SimpleReducer

/**
 * Convenience [DreStoreViewModel] for screens that don't need async operations.
 *
 * ```kotlin
 * class SettingsViewModel(
 *     reducer: SettingsReducer,
 * ) : SimpleDreStoreViewModel<SettingsState, SettingsAction, SettingsEffect>(
 *     reducer = reducer,
 *     initialState = SettingsState.initial,
 * ) {
 *     fun toggleDarkMode() = dispatch(SettingsAction.ToggleDarkMode)
 * }
 * ```
 */
abstract class SimpleDreStoreViewModel<S : DreState, A : DreAction, E : DreEffect>(
    reducer: SimpleReducer<S, A, E>,
    sideEffectHandlers: List<SideEffectHandler<E>> = emptyList(),
    initialState: S,
    dispatchers: DreDispatchers = DefaultDreDispatchers,
) : DreStoreViewModel<S, A, E, Nothing>(
    reducer = reducer.asFullReducer(),
    sideEffectHandlers = sideEffectHandlers,
    initialState = initialState,
    dispatchers = dispatchers,
) {
    /** No async ops — unreachable by design ([Nothing] has no instances). */
    override suspend fun executeAsyncOp(op: Nothing, stateSnapshot: S) {
        // Nothing type — this is never called
    }
}

/**
 * Adapts a [SimpleReducer] to the full [Reducer] interface.
 * The async op type becomes [Nothing] — compiler guarantees no async op is ever set.
 */
private fun <S : DreState, A : DreAction, E : DreEffect> SimpleReducer<S, A, E>.asFullReducer(): Reducer<S, A, E, Nothing> {
    val simpleReducer = this
    return Reducer { state, action ->
        val result = simpleReducer.reduce(state, action)
        ReduceResult(
            state = result.state,
            sideEffects = result.sideEffects,
            asyncOp = null,
        )
    }
}
