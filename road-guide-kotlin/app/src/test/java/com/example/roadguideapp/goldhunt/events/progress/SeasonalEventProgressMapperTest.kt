package com.example.roadguideapp.goldhunt.events.progress

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import org.junit.Assert.assertEquals
import org.junit.Test

class SeasonalEventProgressMapperTest {
    @Test
    fun roundTrip_preservesProgressFields() {
        val progress = SeasonalEventProgress(
            eventId = "seasonal:event:winter_crystal:2026",
            eventType = SeasonalEventType.WINTER_CRYSTAL,
            cycleYear = 2026,
            treasuresCollected = 5,
            xpEarned = 120L,
            creditsEarned = 40,
            fragmentsEarned = 1,
            completionPercent = 42.5,
            isCompleted = false,
            updatedAtMs = 1_700_000_000_000L,
        )

        val entity = SeasonalEventProgressMapper.toEntity(progress)
        val restored = SeasonalEventProgressMapper.toDomain(entity)

        assertEquals(progress, restored)
    }

}
