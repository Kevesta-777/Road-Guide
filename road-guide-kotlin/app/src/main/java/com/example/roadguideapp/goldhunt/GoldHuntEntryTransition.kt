package com.example.roadguideapp.goldhunt

internal object GoldHuntEntryTransition {
    const val ZOOM_DURATION_MS = 900
    const val TREASURE_POP_IN_MS = 650
    const val TREASURE_POP_IN_START_SCALE = 0.25f
    const val SPARKLE_DURATION_MS = 1_400

    fun easeOutBack(t: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        val x = t - 1f
        return 1f + c3 * x * x * x + c1 * x * x
    }
}
