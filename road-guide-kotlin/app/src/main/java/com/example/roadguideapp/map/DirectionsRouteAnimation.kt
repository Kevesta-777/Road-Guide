package com.example.roadguideapp.map

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

internal object DirectionsRouteAnimation {

    const val FETCH_DEBOUNCE_MS = 180L
    const val REVEAL_DURATION_MS = 1_400
    const val CAMERA_DURATION_MS = 900
    private const val FRAME_MS = 16L

    fun easeOutCubic(t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        val inv = 1f - x
        return 1f - inv * inv * inv
    }

    suspend fun animateReveal(onProgress: suspend (Float) -> Unit) {
        val start = System.currentTimeMillis()
        while (coroutineContext.isActive) {
            val elapsed = System.currentTimeMillis() - start
            val linear = (elapsed.toFloat() / REVEAL_DURATION_MS).coerceIn(0f, 1f)
            val eased = easeOutCubic(linear)
            onProgress(eased)
            if (linear >= 1f) break
            delay(FRAME_MS)
        }
        onProgress(1f)
    }
}
