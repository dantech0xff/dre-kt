package dev.drekt.android

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.drekt.core.DreAction
import dev.drekt.core.DreEffect
import dev.drekt.core.DreState
import dev.drekt.core.SideEffectHandler
import dev.drekt.core.SimpleReduceResult
import dev.drekt.core.SimpleReducer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimpleDreStoreViewModelTest {

    // region Test types

    data class SimpleState(val value: String = "initial") : DreState

    sealed interface SimpleAction : DreAction {
        data class SetValue(val value: String) : SimpleAction
        data object Reset : SimpleAction
    }

    sealed interface SimpleEffect : DreEffect {
        data class Notify(val message: String) : SimpleEffect
    }

    // endregion

    // region Test reducer

    private val simpleReducer = SimpleReducer<SimpleState, SimpleAction, SimpleEffect> { state, action ->
        when (action) {
            is SimpleAction.SetValue -> SimpleReduceResult(
                state = state.copy(value = action.value),
            )

            is SimpleAction.Reset -> SimpleReduceResult(
                state = SimpleState(),
                sideEffects = listOf(SimpleEffect.Notify("reset")),
            )
        }
    }

    // endregion

    // region Test ViewModel

    private class TestSimpleViewModel(
        reducer: SimpleReducer<SimpleState, SimpleAction, SimpleEffect>,
        handlers: List<SideEffectHandler<SimpleEffect>> = emptyList(),
        dispatchers: DreDispatchers,
    ) : SimpleDreStoreViewModel<SimpleState, SimpleAction, SimpleEffect>(
        reducer = reducer,
        sideEffectHandlers = handlers,
        initialState = SimpleState(),
        dispatchers = dispatchers,
    ) {
        fun setValue(value: String) = dispatch(SimpleAction.SetValue(value))
        fun reset() = dispatch(SimpleAction.Reset)
    }

    // endregion

    // region Tests

    @Test
    fun `initial state is correct`() = runTest {
        val vm = TestSimpleViewModel(simpleReducer, dispatchers = TestDreDispatchers(testScheduler))
        assertThat(vm.state.value).isEqualTo(SimpleState())
    }

    @Test
    fun `dispatch updates state`() = runTest {
        val vm = TestSimpleViewModel(simpleReducer, dispatchers = TestDreDispatchers(testScheduler))

        vm.state.test {
            assertThat(awaitItem()).isEqualTo(SimpleState()) // initial
            vm.setValue("updated")
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(SimpleState(value = "updated"))
        }
    }

    @Test
    fun `multiple dispatches serialize correctly`() = runTest {
        val vm = TestSimpleViewModel(simpleReducer, dispatchers = TestDreDispatchers(testScheduler))

        vm.state.test {
            awaitItem() // initial
            vm.setValue("one")
            vm.setValue("two")
            vm.setValue("three")
            advanceUntilIdle()

            val states = listOf(awaitItem(), awaitItem(), awaitItem())
            assertThat(states.last().value).isEqualTo("three")
        }
    }

    @Test
    fun `side effects are delivered`() = runTest {
        val received = mutableListOf<SimpleEffect>()
        val handler = SideEffectHandler<SimpleEffect> { received.add(it) }
        val vm = TestSimpleViewModel(
            simpleReducer,
            handlers = listOf(handler),
            dispatchers = TestDreDispatchers(testScheduler),
        )

        vm.state.test {
            awaitItem() // initial
            vm.setValue("changed")
            advanceUntilIdle()
            awaitItem()

            vm.reset()
            advanceUntilIdle()
            awaitItem()
        }

        assertThat(received).hasSize(1)
        assertThat(received.first()).isEqualTo(SimpleEffect.Notify("reset"))
    }

    @Test
    fun `reset returns to initial state`() = runTest {
        val vm = TestSimpleViewModel(simpleReducer, dispatchers = TestDreDispatchers(testScheduler))

        vm.state.test {
            awaitItem() // initial
            vm.setValue("changed")
            advanceUntilIdle()
            awaitItem()

            vm.reset()
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(SimpleState())
        }
    }

    // endregion
}
