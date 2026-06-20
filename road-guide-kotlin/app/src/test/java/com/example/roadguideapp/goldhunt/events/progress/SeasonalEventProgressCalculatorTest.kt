package com.example.roadguideapp.goldhunt.events.progress

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalEventProgressCalculatorTest {
    @Test
    fun completionPercent_weightsTreasuresAndFragments() {
        val percent = SeasonalEventProgressCalculator.completionPercent(
            eventType = SeasonalEventType.SPRING_BLOSSOM,
            treasuresCollected = 10,
            fragmentsEarned = 1,
        )

        assertEquals(70.0, percent, 0.1)
    }

    @Test
    fun completionPercent_capsAtOneHundred() {
        val percent = SeasonalEventProgressCalculator.completionPercent(
            eventType = SeasonalEventType.NEW_YEAR_GOLDEN_HUNT,
            treasuresCollected = 100,
            fragmentsEarned = 5,
        )

        assertEquals(100.0, percent, 0.0001)
        assertTrue(SeasonalEventProgressCalculator.isCompleted(percent))
    }

    @Test
    fun completionPercent_zeroProgress_returnsZero() {
        val percent = SeasonalEventProgressCalculator.completionPercent(
            eventType = SeasonalEventType.SUMMER_EXPLORER,
            treasuresCollected = 0,
            fragmentsEarned = 0,
        )

        assertEquals(0.0, percent, 0.0001)
        assertFalse(SeasonalEventProgressCalculator.isCompleted(percent))
    }
}
