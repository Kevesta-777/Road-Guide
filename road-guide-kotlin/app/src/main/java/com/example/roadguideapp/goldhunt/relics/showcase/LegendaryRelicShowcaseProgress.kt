package com.example.roadguideapp.goldhunt.relics.showcase

internal data class LegendaryRelicShowcaseProgress(
    val completedCount: Int = 0,
    val trackableCount: Int = 0,
    val badgesEarned: Int = 0,
    val titlesEarned: Int = 0,
    val totalCreditsEarned: Int = 0,
    val totalXpEarned: Long = 0L,
    val linkedAchievementsCompleted: Int = 0,
    val linkedAchievementsTotal: Int = 0,
) {
    val completionFraction: Float
        get() = if (trackableCount <= 0) {
            0f
        } else {
            (completedCount.toFloat() / trackableCount.toFloat()).coerceIn(0f, 1f)
        }
}
