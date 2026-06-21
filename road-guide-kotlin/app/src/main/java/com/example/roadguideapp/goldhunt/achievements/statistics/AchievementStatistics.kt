package com.example.roadguideapp.goldhunt.achievements.statistics

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantResult

/**
 * Lifetime achievement statistics integrated with the explorer profile.
 *
 * [totalCount] reflects the live catalog size; [completionPercentage] is derived at read time.
 */
internal data class AchievementStatistics(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val rareCompletedCount: Int = 0,
    val legendaryCompletedCount: Int = 0,
    val creditsEarned: Int = 0,
    val xpEarned: Long = 0L,
) {
    val completionPercentage: Float
        get() = if (totalCount <= 0) {
            0f
        } else {
            (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
        }

    fun withCatalogSize(catalogSize: Int): AchievementStatistics =
        copy(totalCount = catalogSize.coerceAtLeast(0))

    fun withGrant(
        achievement: Achievement,
        grantResult: AchievementRewardGrantResult,
    ): AchievementStatistics {
        if (!grantResult.isNewGrant) return this
        val tier = AchievementStatisticsCalculator.rarityTier(achievement)
        return copy(
            completedCount = completedCount + 1,
            rareCompletedCount = rareCompletedCount + if (tier == AchievementRarityTier.RARE) 1 else 0,
            legendaryCompletedCount = legendaryCompletedCount +
                if (tier == AchievementRarityTier.LEGENDARY) 1 else 0,
            creditsEarned = creditsEarned + grantResult.creditsGranted.coerceAtLeast(0),
            xpEarned = xpEarned + grantResult.xpGranted.coerceAtLeast(0L),
        )
    }

    companion object {
        val EMPTY = AchievementStatistics()
    }
}
