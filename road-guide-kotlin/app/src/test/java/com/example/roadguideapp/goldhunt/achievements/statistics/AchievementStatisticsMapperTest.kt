package com.example.roadguideapp.goldhunt.achievements.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementStatisticsMapperTest {
    @Test
    fun fromEntity_mapsPersistedCountersWithCatalogSize() {
        val entity = ExplorerProfileEntity(
            achievementStatsCompletedCount = 12,
            achievementStatsRareCompleted = 4,
            achievementStatsLegendaryCompleted = 2,
            achievementStatsCreditsEarned = 300,
            achievementStatsXpEarned = 720L,
        )

        val stats = AchievementStatisticsMapper.fromEntity(entity, catalogSize = 150)

        assertEquals(150, stats.totalCount)
        assertEquals(12, stats.completedCount)
        assertEquals(4, stats.rareCompletedCount)
        assertEquals(2, stats.legendaryCompletedCount)
        assertEquals(300, stats.creditsEarned)
        assertEquals(720L, stats.xpEarned)
        assertEquals(0.08f, stats.completionPercentage, 0.0001f)
    }

    @Test
    fun applyToEntity_roundTripsPersistedFields() {
        val entity = ExplorerProfileEntity()
        val stats = AchievementStatistics(
            totalCount = 150,
            completedCount = 9,
            rareCompletedCount = 3,
            legendaryCompletedCount = 1,
            creditsEarned = 225,
            xpEarned = 540L,
        )

        val mapped = AchievementStatisticsMapper.applyToEntity(entity, stats)

        assertEquals(9, mapped.achievementStatsCompletedCount)
        assertEquals(3, mapped.achievementStatsRareCompleted)
        assertEquals(1, mapped.achievementStatsLegendaryCompleted)
        assertEquals(225, mapped.achievementStatsCreditsEarned)
        assertEquals(540L, mapped.achievementStatsXpEarned)
    }
}
