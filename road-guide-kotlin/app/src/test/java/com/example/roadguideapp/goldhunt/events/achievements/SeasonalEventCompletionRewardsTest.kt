package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEventFactory
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventCompletionRewardsTest {
    @Test
    fun creditsAndXp_scaleWithEventMultiplierAndCategory() {
        val coreEvent = SeasonalEventFactory.fromType(
            SeasonalEventType.SPRING_BLOSSOM,
            LocalDate.of(2026, 4, 10),
        )
        val holidayEvent = SeasonalEventFactory.fromType(
            SeasonalEventType.HALLOWEEN_MYSTERY,
            LocalDate.of(2026, 10, 20),
        )

        val coreCredits = SeasonalEventCompletionRewards.creditsFor(coreEvent)
        val holidayCredits = SeasonalEventCompletionRewards.creditsFor(holidayEvent)
        val coreXp = SeasonalEventCompletionRewards.xpFor(coreEvent)
        val holidayXp = SeasonalEventCompletionRewards.xpFor(holidayEvent)

        assertTrue(coreCredits >= 1)
        assertTrue(holidayCredits > coreCredits)
        assertTrue(coreXp >= 1L)
        assertTrue(holidayXp > coreXp)
    }
}
