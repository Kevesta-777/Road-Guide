package com.example.roadguideapp.goldhunt.events.statistics

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SeasonalEventStatisticsMapperTest {
    @Test
    fun fromEntity_readsExplorerProfileColumns() {
        val entity = ExplorerProfileEntity(
            seasonalEventStatsEventsJoined = 3,
            seasonalEventStatsRewardsEarned = 18,
            seasonalEventStatsBestEventType = SeasonalEventType.WINTER_CRYSTAL.id,
            seasonalEventStatsFavoriteEventType = SeasonalEventType.SPRING_BLOSSOM.id,
            seasonalEventStatsTreasuresCollected = 12,
            seasonalEventStatsXpEarned = 340L,
            seasonalEventStatsCreditsEarned = 90,
            seasonalEventStatsFragmentsEarned = 2,
            seasonalEventStatsCompletedCycles = 1,
        )

        val stats = SeasonalEventStatisticsMapper.fromEntity(entity)

        assertEquals(3, stats.eventsJoined)
        assertEquals(1, stats.eventsCompleted)
        assertEquals(18, stats.rewardsEarned)
        assertEquals(SeasonalEventType.WINTER_CRYSTAL, stats.bestEventType)
        assertEquals(SeasonalEventType.SPRING_BLOSSOM, stats.favoriteEventType)
        assertEquals(12, stats.treasuresCollected)
        assertEquals(340L, stats.xpEarned)
        assertEquals(90, stats.creditsEarned)
        assertEquals(2, stats.fragmentsEarned)
    }

    @Test
    fun applyToEntity_roundTripsStatisticsFields() {
        val stats = SeasonalEventStatistics(
            eventsJoined = 2,
            eventsCompleted = 1,
            rewardsEarned = 9,
            bestEventType = SeasonalEventType.NEW_YEAR_GOLDEN_HUNT,
            favoriteEventType = SeasonalEventType.SUMMER_EXPLORER,
            treasuresCollected = 9,
            xpEarned = 200L,
            creditsEarned = 45,
            fragmentsEarned = 1,
        )

        val entity = SeasonalEventStatisticsMapper.applyToEntity(ExplorerProfileEntity(), stats)
        val restored = SeasonalEventStatisticsMapper.fromEntity(entity)

        assertEquals(stats, restored)
    }
}
