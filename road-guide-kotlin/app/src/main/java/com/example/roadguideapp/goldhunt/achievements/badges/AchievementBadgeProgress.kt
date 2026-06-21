package com.example.roadguideapp.goldhunt.achievements.badges

internal data class AchievementBadgeProgress(
    val totalCount: Int,
    val unlockedCount: Int,
    val lockedCount: Int,
    val commonUnlockedCount: Int = 0,
    val rareUnlockedCount: Int = 0,
    val legendaryUnlockedCount: Int = 0,
) {
    val completionFraction: Float
        get() = if (totalCount <= 0) {
            0f
        } else {
            (unlockedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
        }
}
