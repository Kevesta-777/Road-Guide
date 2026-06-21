package com.example.roadguideapp.goldhunt.events

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalEventWindowTest {
    @Test
    fun contains_sameYearWindow_matchesSpringBlossom() {
        val window = SeasonalEventWindow.from(SeasonalEventType.SPRING_BLOSSOM)

        assertTrue(window.contains(4, 1))
        assertTrue(window.contains(3, 20))
        assertTrue(window.contains(5, 31))
        assertFalse(window.contains(3, 19))
        assertFalse(window.contains(6, 1))
    }

    @Test
    fun contains_wrapAroundWindow_matchesWinterCrystal() {
        val window = SeasonalEventWindow.from(SeasonalEventType.WINTER_CRYSTAL)

        assertTrue(window.contains(12, 15))
        assertTrue(window.contains(1, 10))
        assertFalse(window.contains(1, 20))
        assertFalse(window.contains(11, 30))
    }

    @Test
    fun contains_newYearGoldenHunt_spansYearBoundary() {
        val window = SeasonalEventWindow.from(SeasonalEventType.NEW_YEAR_GOLDEN_HUNT)

        assertTrue(window.contains(12, 31))
        assertTrue(window.contains(1, 1))
        assertTrue(window.contains(1, 7))
        assertFalse(window.contains(1, 8))
        assertFalse(window.contains(12, 27))
    }
}
