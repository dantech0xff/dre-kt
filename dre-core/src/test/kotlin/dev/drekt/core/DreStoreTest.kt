package dev.drekt.core

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DreStoreTest {

    @Test
    fun `initial state is emitted`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
        )

        assertThat(store.state.value).isEqualTo(TestState())
    }

    @Test
    fun `dispatch updates state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
        )

        store.state.test {
            assertThat(awaitItem()).isEqualTo(TestState()) // initial
            store.dispatch(TestAction.Increment)
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(TestState(count = 1))
        }
    }

    @Test
    fun `multiple dispatches serialize correctly`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
        )

        store.state.test {
            awaitItem() // initial
            store.dispatch(TestAction.Increment)
            store.dispatch(TestAction.Increment)
            store.dispatch(TestAction.Increment)
            advanceUntilIdle()

            val states = listOf(awaitItem(), awaitItem(), awaitItem())
            assertThat(states.last().count).isEqualTo(3)
        }
    }

    @Test
    fun `side effects are delivered to handlers`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val received = mutableListOf<TestEffect>()
        val handler = SideEffectHandler<TestEffect> { received.add(it) }

        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
            sideEffectHandlers = listOf(handler),
        )

        store.state.test {
            awaitItem() // initial
            store.dispatch(TestAction.Increment)
            advanceUntilIdle()
            awaitItem()
        }

        assertThat(received).hasSize(1)
        assertThat(received.first()).isEqualTo(TestEffect.Log("incremented to 1"))
        store.close()
    }

    @Test
    fun `multiple handlers receive same effect`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val received1 = mutableListOf<TestEffect>()
        val received2 = mutableListOf<TestEffect>()

        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
            sideEffectHandlers = listOf(
                SideEffectHandler { received1.add(it) },
                SideEffectHandler { received2.add(it) },
            ),
        )

        store.state.test {
            awaitItem()
            store.dispatch(TestAction.Increment)
            advanceUntilIdle()
            awaitItem()
        }

        assertThat(received1).hasSize(1)
        assertThat(received2).hasSize(1)
        store.close()
    }

    @Test
    fun `async op callback is invoked with state snapshot`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var capturedOp: TestAsyncOp? = null
        var capturedSnapshot: TestState? = null

        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
            onAsyncOp = { op, snapshot ->
                capturedOp = op
                capturedSnapshot = snapshot
            },
        )

        store.state.test {
            awaitItem()
            store.dispatch(TestAction.StartAsync)
            advanceUntilIdle()
            awaitItem() // loading
        }

        assertThat(capturedOp).isEqualTo(TestAsyncOp.LoadData)
        assertThat(capturedSnapshot?.phase).isEqualTo("loading")
    }

    @Test
    fun `async op can dispatch back into store`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)

        lateinit var store: DreStore<TestState, TestAction, TestEffect, TestAsyncOp>
        store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
            onAsyncOp = { op, _ ->
                when (op) {
                    is TestAsyncOp.LoadData -> store.dispatch(TestAction.AsyncDone("loaded"))
                }
            },
        )

        store.state.test {
            awaitItem() // initial
            store.dispatch(TestAction.StartAsync)
            advanceUntilIdle()
            skipItems(1) // loading
            assertThat(awaitItem().phase).isEqualTo("loaded")
        }
    }

    @Test
    fun `no async op when reducer returns null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var asyncCalled = false

        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
            onAsyncOp = { _, _ -> asyncCalled = true },
        )

        store.state.test {
            awaitItem()
            store.dispatch(TestAction.Increment) // no async op
            advanceUntilIdle()
            awaitItem()
        }

        assertThat(asyncCalled).isFalse()
    }

    @Test
    fun `no side effects when reducer returns empty list`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val received = mutableListOf<TestEffect>()

        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
            sideEffectHandlers = listOf(SideEffectHandler { received.add(it) }),
        )

        store.state.test {
            awaitItem()
            store.dispatch(TestAction.Decrement) // no effects
            advanceUntilIdle()
            awaitItem()
        }

        assertThat(received).isEmpty()
        store.close()
    }

    @Test
    fun `guard clause rejects action in wrong state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)

        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(phase = "not-idle"),
            scope = this,
            dispatchContext = dispatcher,
        )

        store.state.test {
            awaitItem() // initial
            store.dispatch(TestAction.GuardedAction) // rejected — phase != "idle"
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `close stops side effect processing`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = DreStore(
            reducer = testReducer,
            initialState = TestState(),
            scope = this,
            dispatchContext = dispatcher,
        )

        store.close()
        // Should not throw
    }
}
