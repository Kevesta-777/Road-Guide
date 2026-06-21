package com.example.roadguideapp.goldhunt.events

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventScheduleTest {
    @Test
    fun activeOn_summerDate_returnsSummerExplorer() {
        val active = SeasonalEventSchedule.activeOn(7, 15)

        assertTrue(SeasonalEventType.SUMMER_EXPLORER in active)
        assertFalse(SeasonalEventType.WINTER_CRYSTAL in active)
    }

    @Test
    fun activeOn_halloweenOverlap_includesMultipleEvents() {
        val active = SeasonalEventSchedule.activeOn(10, 20)

        assertTrue(SeasonalEventType.HALLOWEEN_MYSTERY in active)
        assertTrue(SeasonalEventType.AUTUMN_HARVEST in active)
    }

    @Test
    fun activeTemplatesOn_anniversaryWindow() {
        val templates = SeasonalEventSchedule.activeTemplatesOn(LocalDate.of(2026, 6, 7))

        assertEquals(2, templates.size)
        assertTrue(templates.any { it.type == SeasonalEventType.ANNIVERSARY_EVENT })
        assertTrue(templates.any { it.type == SeasonalEventType.SUMMER_EXPLORER })
    }
}
