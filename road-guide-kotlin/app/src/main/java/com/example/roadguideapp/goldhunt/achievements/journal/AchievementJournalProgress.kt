package com.example.roadguideapp.goldhunt.achievements.journal

internal data class AchievementJournalProgress(
    val trackableCount: Int,
    val catalogCount: Int = trackableCount,
    val completedCount: Int,
    val incompleteCount: Int,
    val lockedCount: Int,
    val hiddenCount: Int,
    val undiscoveredSecretCount: Int = 0,
) {
    val visibleCount: Int get() = (trackableCount - hiddenCount).coerceAtLeast(0)

    val completionFraction: Float
        get() = if (trackableCount <= 0) {
            0f
        } else {
            (completedCount.toFloat() / trackableCount.toFloat()).coerceIn(0f, 1f)
        }

    val completionPercentage: Float get() = completionFraction * 100f
}
