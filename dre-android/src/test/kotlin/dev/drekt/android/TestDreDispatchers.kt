package dev.drekt.android

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * Test [DreDispatchers] that routes all dispatchers to a single [StandardTestDispatcher].
 *
 * Use with `runTest` to control virtual time and verify dispatch serialization:
 *
 * ```kotlin
 * @Test fun `dispatch updates state`() = runTest {
 *     val dispatchers = TestDreDispatchers(testScheduler)
 *     val viewModel = MyViewModel(reducer, dispatchers = dispatchers)
 *
 *     viewModel.state.test {
 *         awaitItem() // initial
 *         viewModel.doSomething()
 *         advanceUntilIdle()
 *         val updated = awaitItem()
 *         assertThat(updated.phase).isEqualTo(Phase.Done)
 *     }
 * }
 * ```
 *
 * @param scheduler Test scheduler — typically `testScheduler` from `runTest`
 */
class TestDreDispatchers(scheduler: TestCoroutineScheduler) : DreDispatchers {
    private val testDispatcher = StandardTestDispatcher(scheduler)
    override val main: CoroutineDispatcher = testDispatcher
    override val mainImmediate: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
}
