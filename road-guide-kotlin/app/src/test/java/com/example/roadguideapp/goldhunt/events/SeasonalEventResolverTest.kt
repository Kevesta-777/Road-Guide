package com.example.roadguideapp.goldhunt.events

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventResolverTest {
    @Test
    fun resolve_inactiveDate_returnsBaseMultiplier() {
        val context = SeasonalEventResolver.resolve(LocalDate.of(2026, 2, 10))

        assertFalse(context.hasActiveEvents)
        assertEquals(SeasonalEventSchema.BASE_MULTIPLIER, context.combinedMultiplier, 0.0001)
        assertEquals(null, context.primaryEvent)
    }

    @Test
    fun resolve_anniversaryDate_usesHighestMultiplier() {
        val context = SeasonalEventResolver.resolve(LocalDate.of(2026, 6, 7))

        assertTrue(context.hasActiveEvents)
        assertEquals(2.0, context.combinedMultiplier, 0.0001)
        assertEquals(SeasonalEventType.ANNIVERSARY_EVENT, context.primaryEvent?.eventType)
    }

    @Test
    fun scaleAmount_appliesCombinedMultiplier() {
        val context = SeasonalEventResolver.resolve(LocalDate.of(2026, 6, 7))

        assertEquals(200L, SeasonalEventResolver.scaleAmount(100L, context))
        assertEquals(20, SeasonalEventResolver.scaleAmount(10, context))
    }

    @Test
    fun resolve_withLowExplorerLevel_filtersAccessibleRewards() {
        val context = SeasonalEventResolver.resolve(
            date = LocalDate.of(2026, 6, 7),
            explorerLevel = 1,
        )

        assertEquals(2, context.activeEvents.size)
        assertEquals(1, context.accessibleEvents.size)
        assertEquals(SeasonalEventType.ANNIVERSARY_EVENT, context.primaryEvent?.eventType)
        assertEquals(200L, SeasonalEventResolver.scaleAmount(100L, context))
    }

    @Test
    fun template_exposesFutureHookKeys() {
        val template = SeasonalEventCatalog.templateFor(SeasonalEventType.HALLOWEEN_MYSTERY)

        assertEquals(
            "seasonal_event:halloween_mystery:first_participation",
            template.achievementKey,
        )
        assertEquals(
            "story_fragment_seasonal_event:halloween_mystery",
            template.storyFragmentKey,
        )
        assertEquals(
            "legendary_relic_seasonal_event:halloween_mystery",
            template.legendaryRelicKey,
        )
    }
}
