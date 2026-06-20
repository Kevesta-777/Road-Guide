package com.example.roadguideapp.goldhunt.events

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventLevelGateTest {
    @Test
    fun isUnlocked_respectsMinExplorerLevel() {
        val event = SeasonalEventFactory.fromType(
            SeasonalEventType.SUMMER_EXPLORER,
            LocalDate.of(2026, 7, 1),
        )

        assertFalse(SeasonalEventLevelGate.isUnlocked(event, explorerLevel = 2))
        assertTrue(SeasonalEventLevelGate.isUnlocked(event, explorerLevel = 3))
    }

    @Test
    fun levelsUntilUnlocked_returnsRemainingLevels() {
        val event = SeasonalEventFactory.fromType(
            SeasonalEventType.COMMUNITY_CHALLENGE,
            LocalDate.of(2026, 7, 15),
        )

        assertEquals(5, SeasonalEventLevelGate.levelsUntilUnlocked(event, explorerLevel = 5))
        assertEquals(0, SeasonalEventLevelGate.levelsUntilUnlocked(event, explorerLevel = 10))
    }

    @Test
    fun filterAccessible_requiresActiveAndUnlocked() {
        val date = LocalDate.of(2026, 6, 7)
        val events = SeasonalEventFactory.activeInstances(date)

        val accessible = SeasonalEventLevelGate.filterAccessible(events, explorerLevel = 1)

        assertEquals(1, accessible.size)
        assertEquals(SeasonalEventType.ANNIVERSARY_EVENT, accessible.single().eventType)
    }

    @Test
    fun unlockedTypes_listsCatalogTypesMeetingLevel() {
        val unlocked = SeasonalEventLevelGate.unlockedTypes(explorerLevel = 5)

        assertTrue(SeasonalEventType.SPRING_BLOSSOM in unlocked)
        assertTrue(SeasonalEventType.AUTUMN_HARVEST in unlocked)
        assertFalse(SeasonalEventType.HALLOWEEN_MYSTERY in unlocked)
        assertFalse(SeasonalEventType.COMMUNITY_CHALLENGE in unlocked)
    }
}
