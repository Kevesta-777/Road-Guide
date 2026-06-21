package com.example.roadguideapp.goldhunt.achievements.titles

internal data class AchievementTitleProgress(
    val totalCount: Int,
    val unlockedCount: Int,
    val lockedCount: Int,
    val activeCount: Int,
) {
    val completionFraction: Float
        get() = if (totalCount <= 0) {
            0f
        } else {
            (unlockedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
        }
}
