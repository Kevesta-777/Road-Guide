package com.example.roadguideapp.goldhunt.events

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventFactoryTest {
    @Test
    fun fromType_sameYearWindow_setsDatesAndActiveFlag() {
        val date = LocalDate.of(2026, 4, 10)
        val event = SeasonalEventFactory.fromType(SeasonalEventType.SPRING_BLOSSOM, date)

        assertEquals("seasonal:event:spring_blossom:2026", event.eventId)
        assertEquals("seasonal_event:v1:spring_blossom:2026", event.eventSeed)
        assertEquals(LocalDate.of(2026, 3, 20), event.startDate)
        assertEquals(LocalDate.of(2026, 5, 31), event.endDate)
        assertEquals(1.15, event.rewardMultiplier, 0.0001)
        assertTrue(event.active)
    }

    @Test
    fun fromType_outsideWindow_marksInactive() {
        val date = LocalDate.of(2026, 2, 10)
        val event = SeasonalEventFactory.fromType(SeasonalEventType.SPRING_BLOSSOM, date)

        assertFalse(event.active)
    }

    @Test
    fun fromType_wrapAroundWindow_januaryUsesPriorCycleYear() {
        val date = LocalDate.of(2027, 1, 10)
        val event = SeasonalEventFactory.fromType(SeasonalEventType.WINTER_CRYSTAL, date)

        assertEquals(2026, event.cycleYear)
        assertEquals(LocalDate.of(2026, 12, 1), event.startDate)
        assertEquals(LocalDate.of(2027, 1, 15), event.endDate)
        assertTrue(event.active)
        assertEquals("seasonal:event:winter_crystal:2026", event.eventId)
    }

    @Test
    fun fromType_wrapAroundWindow_decemberUsesCurrentCycleYear() {
        val date = LocalDate.of(2026, 12, 20)
        val event = SeasonalEventFactory.fromType(SeasonalEventType.WINTER_CRYSTAL, date)

        assertEquals(2026, event.cycleYear)
        assertTrue(event.active)
    }

    @Test
    fun activeInstances_returnsOnlyActiveEvents() {
        val active = SeasonalEventFactory.activeInstances(LocalDate.of(2026, 6, 7))

        assertEquals(2, active.size)
        assertTrue(active.all { it.active })
        assertTrue(active.any { it.eventType == SeasonalEventType.ANNIVERSARY_EVENT })
        assertTrue(active.any { it.eventType == SeasonalEventType.SUMMER_EXPLORER })
    }

    @Test
    fun fromTemplate_matchesFromType() {
        val template = SeasonalEventCatalog.templateFor(SeasonalEventType.HALLOWEEN_MYSTERY)
        val date = LocalDate.of(2026, 10, 20)

        val fromTemplate = SeasonalEventFactory.fromTemplate(template, date)
        val fromType = SeasonalEventFactory.fromType(SeasonalEventType.HALLOWEEN_MYSTERY, date)

        assertEquals(fromType, fromTemplate)
    }
}
