package dev.drekt.core

/**
 * Handles fire-and-forget side effects emitted by [Reducer].
 *
 * Each handler owns a single concern (timer, analytics, haptics, navigation)
 * and filters only the effects it cares about — ignoring the rest.
 *
 * Handlers run in parallel. They have NO dispatch callback by design —
 * if you need to feed results back into the state machine, use an async operation instead.
 *
 * ```kotlin
 * class AnalyticsHandler(
 *     private val tracker: AnalyticsTracker,
 * ) : SideEffectHandler<MyEffect> {
 *     override suspend fun handle(effect: MyEffect) {
 *         if (effect is MyEffect.LogAnalytics) {
 *             tracker.log(effect.event, effect.params)
 *         }
 *         // ignore other effects
 *     }
 * }
 * ```
 *
 * @param E Side effect type — typically a sealed interface
 */
fun interface SideEffectHandler<E : DreEffect> {
    suspend fun handle(effect: E)
}
