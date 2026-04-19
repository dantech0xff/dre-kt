package dev.drekt.android

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Production [DreDispatchers] implementation backed by Android's main looper.
 *
 * - [mainImmediate]: `Dispatchers.Main.immediate` — single-threaded, serializes all
 *   dispatch calls. Acts as the mutex for state mutations.
 * - [io]: `Dispatchers.IO` — for network/disk I/O in async operations.
 */
object DefaultDreDispatchers : DreDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val mainImmediate: CoroutineDispatcher = Dispatchers.Main.immediate
    override val io: CoroutineDispatcher = Dispatchers.IO
}
