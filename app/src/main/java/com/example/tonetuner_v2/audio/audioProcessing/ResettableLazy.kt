package com.example.tonetuner_v2.audio.audioProcessing

/**
 * Similar to lazy. Resettable lazy has a backing variable that acts as a cache. This cache can be invalidated by
 * calling [reset].
 *
 * To be used for AudioSample when changes to dropAndAdd() are made
 */
class ResettableLazy<T>(
    private val initializer: () -> T
){
    val value: T
        get() = cache ?: initializer.invoke().also { cache = it }

    private var cache: T? = null

    /** Invalidates cache.
     * This ensures that, the next time [value] is accessed, it will recalculate cache by re-invoking [initializer]
     */
    fun reset(){
        cache = null
    }
}