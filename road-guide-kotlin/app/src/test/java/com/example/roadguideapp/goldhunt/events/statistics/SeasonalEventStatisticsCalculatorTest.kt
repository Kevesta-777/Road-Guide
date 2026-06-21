package com.example.roadguideapp.goldhunt.events.statistics

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeasonalEventStatisticsCalculatorTest {
    @Test
    fun resolveBestEventType_prefersHighestRewardScore() {
        val progress = listOf(
            progress(
                eventType = SeasonalEventType.SPRING_BLOSSOM,
                treasuresCollected = 3,
                creditsEarned = 30,
                xpEarned = 60L,
            ),
            progress(
                eventType = SeasonalEventType.WINTER_CRYSTAL,
                treasuresCollected = 2,
                creditsEarned = 80,
                xpEarned = 120L,
            ),
        )

        assertEquals(
            SeasonalEventType.WINTER_CRYSTAL,
            SeasonalEventStatisticsCalculator.resolveBestEventType(progress),
        )
    }

    @Test
    fun resolveFavoriteEventType_prefersMostTreasuresCollected() {
        val progress = listOf(
            progress(
                eventType = SeasonalEventType.SPRING_BLOSSOM,
                treasuresCollected = 8,
            ),
            progress(
                eventType = SeasonalEventType.SPRING_BLOSSOM,
                cycleYear = 2025,
                treasuresCollected = 2,
            ),
            progress(
                eventType = SeasonalEventType.HALLOWEEN_MYSTERY,
                treasuresCollected = 5,
            ),
        )

        assertEquals(
            SeasonalEventType.SPRING_BLOSSOM,
            SeasonalEventStatisticsCalculator.resolveFavoriteEventType(progress),
        )
    }

    @Test
    fun resolveBestAndFavorite_returnNullWhenNoParticipation() {
        assertNull(SeasonalEventStatisticsCalculator.resolveBestEventType(emptyList()))
        assertNull(SeasonalEventStatisticsCalculator.resolveFavoriteEventType(emptyList()))
    }

    private fun progress(
        eventType: SeasonalEventType,
        cycleYear: Int = 2026,
        treasuresCollected: Int = 1,
        creditsEarned: Int = 0,
        xpEarned: Long = 0L,
        fragmentsEarned: Int = 0,
        completionPercent: Double = 10.0,
        isCompleted: Boolean = false,
    ): SeasonalEventProgress = SeasonalEventProgress(
        eventId = "seasonal:event:${eventType.name.lowercase()}:$cycleYear",
        eventType = eventType,
        cycleYear = cycleYear,
        treasuresCollected = treasuresCollected,
        xpEarned = xpEarned,
        creditsEarned = creditsEarned,
        fragmentsEarned = fragmentsEarned,
        completionPercent = completionPercent,
        isCompleted = isCompleted,
    )
}
