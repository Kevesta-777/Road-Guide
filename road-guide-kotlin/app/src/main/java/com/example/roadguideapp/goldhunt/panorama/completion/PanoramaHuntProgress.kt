package com.example.roadguideapp.goldhunt.panorama.completion

/**
 * Runtime progress for one panorama hunt session.
 */
internal data class PanoramaHuntProgress(
    val huntId: String,
    val targetsFound: Int,
    val targetsTotal: Int,
    val collectedSlotIndices: Set<Int>,
    val isComplete: Boolean,
    val completedAtMs: Long? = null,
) {
    val progressFraction: Float
        get() = if (targetsTotal <= 0) {
            0f
        } else {
            (targetsFound.toFloat() / targetsTotal.toFloat()).coerceIn(0f, 1f)
        }

    val remainingCount: Int
        get() = (targetsTotal - targetsFound).coerceAtLeast(0)
}
