package com.example.roadguideapp.goldhunt.events

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventModelTest {
    @Test
    fun exposesAchievementStoryAndLevelHookKeys() {
        val event = SeasonalEventFactory.fromType(
            SeasonalEventType.HALLOWEEN_MYSTERY,
            LocalDate.of(2026, 10, 20),
        )

        assertEquals(
            "seasonal_event:halloween_mystery:first_participation",
            event.achievementKey,
        )
        assertEquals(
            "story_fragment_seasonal_event:halloween_mystery",
            event.storyFragmentKey,
        )
        assertEquals(
            "legendary_relic_seasonal_event:halloween_mystery",
            event.legendaryRelicKey,
        )
        assertEquals(
            "seasonal_event_level:halloween_mystery:unlocked",
            event.explorerUnlockAchievementKey,
        )
        assertEquals(
            "seasonal:event:halloween_mystery:participation:2026",
            event.participationEventId,
        )
    }

    @Test
    fun context_accessibleEvents_respectsExplorerLevel() {
        val context = SeasonalEventResolver.resolve(
            date = LocalDate.of(2026, 6, 7),
            explorerLevel = 1,
        )

        assertEquals(2, context.activeEvents.size)
        assertEquals(1, context.accessibleEvents.size)
        assertEquals(
            listOf("seasonal_event:anniversary_event:first_participation"),
            context.activeAchievementKeys,
        )
    }

    @Test
    fun manager_isEventAccessible_checksActiveAndLevel() {
        assertTrue(
            SeasonalEventManager.isEventAccessible(
                type = SeasonalEventType.ANNIVERSARY_EVENT,
                explorerLevel = 1,
                date = LocalDate.of(2026, 6, 7),
            ),
        )
        assertFalse(
            SeasonalEventManager.isEventAccessible(
                type = SeasonalEventType.SUMMER_EXPLORER,
                explorerLevel = 1,
                date = LocalDate.of(2026, 6, 7),
            ),
        )
        assertFalse(
            SeasonalEventManager.isEventAccessible(
                type = SeasonalEventType.SPRING_BLOSSOM,
                explorerLevel = 10,
                date = LocalDate.of(2026, 2, 1),
            ),
        )
    }
}
