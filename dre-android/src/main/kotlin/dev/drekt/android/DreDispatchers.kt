package dev.drekt.android

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Abstraction over coroutine dispatchers for testability.
 *
 * In production, use [DefaultDreDispatchers].
 * In tests, use `TestDreDispatchers` to control virtual time and serialization.
 *
 * Why not use `Dispatchers.Main` directly?
 * - Cannot swap in tests without `Dispatchers.setMain()` (global, leaky)
 * - Constructor injection makes dispatcher choice explicit and testable
 *
 * @see [mainImmediate] — used by dispatch loop for single-writer serialization
 */
interface DreDispatchers {
    /** Main thread dispatcher. Used for UI-bound work. */
    val main: CoroutineDispatcher

    /**
     * Main thread with immediate dispatch.
     * Used by [DreViewModel.dispatch] to ensure single-writer serialization.
     * All state mutations happen on this dispatcher — it acts as the mutex.
     */
    val mainImmediate: CoroutineDispatcher

    /** I/O dispatcher for blocking/network operations. */
    val io: CoroutineDispatcher
}
