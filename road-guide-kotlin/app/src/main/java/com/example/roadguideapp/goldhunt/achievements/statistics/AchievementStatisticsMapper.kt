package com.example.roadguideapp.goldhunt.achievements.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity

internal object AchievementStatisticsMapper {
    fun fromEntity(
        entity: ExplorerProfileEntity,
        catalogSize: Int,
    ): AchievementStatistics = AchievementStatistics(
        totalCount = catalogSize.coerceAtLeast(0),
        completedCount = entity.achievementStatsCompletedCount.coerceAtLeast(0),
        rareCompletedCount = entity.achievementStatsRareCompleted.coerceAtLeast(0),
        legendaryCompletedCount = entity.achievementStatsLegendaryCompleted.coerceAtLeast(0),
        creditsEarned = entity.achievementStatsCreditsEarned.coerceAtLeast(0),
        xpEarned = entity.achievementStatsXpEarned.coerceAtLeast(0L),
    )

    fun applyToEntity(
        entity: ExplorerProfileEntity,
        stats: AchievementStatistics,
    ): ExplorerProfileEntity = entity.copy(
        achievementStatsCompletedCount = stats.completedCount.coerceAtLeast(0),
        achievementStatsRareCompleted = stats.rareCompletedCount.coerceAtLeast(0),
        achievementStatsLegendaryCompleted = stats.legendaryCompletedCount.coerceAtLeast(0),
        achievementStatsCreditsEarned = stats.creditsEarned.coerceAtLeast(0),
        achievementStatsXpEarned = stats.xpEarned.coerceAtLeast(0L),
    )
}
