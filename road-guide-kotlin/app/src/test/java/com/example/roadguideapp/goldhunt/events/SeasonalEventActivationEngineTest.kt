package com.example.roadguideapp.goldhunt.events

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SeasonalEventActivationEngineTest {
    @Before
    fun setUp() {
        SeasonalEventScheduleRegistry.clearCustom()
    }

    @After
    fun tearDown() {
        SeasonalEventScheduleRegistry.clearCustom()
    }

    @Test
    fun activate_inactiveDate_returnsEmpty() {
        val result = SeasonalEventActivationEngine.activate(LocalDate.of(2026, 2, 10))

        assertFalse(result.hasActiveEvents)
        assertEquals(0, result.activeEventCount)
        assertEquals(LocalDate.of(2026, 2, 10), result.evaluatedAt)
    }

    @Test
    fun activate_overlapDate_returnsMultipleCatalogEvents() {
        val result = SeasonalEventActivationEngine.activate(LocalDate.of(2026, 6, 7))

        assertTrue(result.hasActiveEvents)
        assertEquals(2, result.activeCatalogEvents.size)
        assertEquals(0, result.activeCustomEvents.size)
        assertTrue(result.activeCatalogEvents.any { it.eventType == SeasonalEventType.ANNIVERSARY_EVENT })
        assertTrue(result.activeCatalogEvents.any { it.eventType == SeasonalEventType.SUMMER_EXPLORER })
        assertEquals(2.0, result.combinedRewardMultiplier, 0.0001)
    }

    @Test
    fun activate_wrapAroundCatalogEvent_usesCycleWindow() {
        val result = SeasonalEventActivationEngine.activate(LocalDate.of(2027, 1, 10))

        val winter = result.activeCatalogEvents.single {
            it.eventType == SeasonalEventType.WINTER_CRYSTAL
        }

        assertEquals(2026, winter.cycleYear)
        assertEquals(LocalDate.of(2026, 12, 1), winter.startDate)
        assertEquals(LocalDate.of(2027, 1, 15), winter.endDate)
        assertTrue(winter.active)
    }

    @Test
    fun activate_customAnnualEvent_activatesOnSchedule() {
        SeasonalEventScheduleRegistry.register(
            SeasonalCustomEventDefinition(
                eventKey = "FOUNDERS_WEEK",
                displayName = "Founders Week",
                schedule = SeasonalCustomEventSchedule.Annual(
                    window = SeasonalEventWindow(
                        startMonth = 3,
                        startDay = 1,
                        endMonth = 3,
                        endDay = 7,
                    ),
                ),
                rewardMultiplier = 1.8,
                minExplorerLevel = 2,
            ),
        )

        val active = SeasonalEventActivationEngine.activate(LocalDate.of(2026, 3, 5))
        val inactive = SeasonalEventActivationEngine.activate(LocalDate.of(2026, 3, 10))

        assertEquals(1, active.activeCustomEvents.size)
        assertEquals("FOUNDERS_WEEK", active.activeCustomEvents.single().eventKey)
        assertEquals("seasonal:custom:founders_week:2026", active.activeCustomEvents.single().eventId)
        assertTrue(active.activeCustomEvents.single().active)
        assertEquals(0, inactive.activeCustomEvents.size)
    }

    @Test
    fun activate_customFixedRangeEvent_activatesOnlyInsideWindow() {
        SeasonalEventScheduleRegistry.register(
            SeasonalCustomEventDefinition(
                eventKey = "BETA_CELEBRATION",
                displayName = "Beta Celebration",
                schedule = SeasonalCustomEventSchedule.FixedRange(
                    startDate = LocalDate.of(2026, 5, 1),
                    endDate = LocalDate.of(2026, 5, 14),
                ),
                rewardMultiplier = 1.6,
            ),
        )

        val active = SeasonalEventActivationEngine.activate(LocalDate.of(2026, 5, 10))
        val inactive = SeasonalEventActivationEngine.activate(LocalDate.of(2026, 5, 20))

        assertEquals(1, active.activeCustomEvents.size)
        assertEquals(
            "seasonal:custom:beta_celebration:fixed:${LocalDate.of(2026, 5, 1).toEpochDay()}",
            active.activeCustomEvents.single().eventId,
        )
        assertEquals(0, inactive.activeCustomEvents.size)
    }

    @Test
    fun activate_disabledCustomEvent_isSkipped() {
        SeasonalEventScheduleRegistry.register(
            SeasonalCustomEventDefinition(
                eventKey = "DISABLED_EVENT",
                displayName = "Disabled",
                schedule = SeasonalCustomEventSchedule.FixedRange(
                    startDate = LocalDate.of(2026, 1, 1),
                    endDate = LocalDate.of(2026, 12, 31),
                ),
                rewardMultiplier = 2.0,
                enabled = false,
            ),
        )

        val result = SeasonalEventActivationEngine.activate(LocalDate.of(2026, 6, 1))

        assertEquals(0, result.activeCustomEvents.size)
    }

    @Test
    fun activate_catalogPlusCustom_combinesActiveSets() {
        SeasonalEventScheduleRegistry.register(
            SeasonalCustomEventDefinition(
                eventKey = "SUMMER_BONUS",
                displayName = "Summer Bonus",
                schedule = SeasonalCustomEventSchedule.Annual(
                    window = SeasonalEventWindow(6, 1, 8, 31),
                ),
                rewardMultiplier = 1.45,
            ),
        )

        val result = SeasonalEventActivationEngine.activate(LocalDate.of(2026, 6, 7))

        assertTrue(result.activeCatalogEvents.isNotEmpty())
        assertEquals(1, result.activeCustomEvents.size)
        assertEquals("SUMMER_BONUS", result.activeCustomEvents.single().eventKey)
        assertEquals(2.0, result.combinedRewardMultiplier, 0.0001)
    }

    @Test
    fun manager_registerAndActivate_customEvent() {
        SeasonalEventManager.registerCustomEvent(
            SeasonalCustomEventDefinition(
                eventKey = "LOCAL_FESTIVAL",
                displayName = "Local Festival",
                schedule = SeasonalCustomEventSchedule.FixedRange(
                    startDate = LocalDate.of(2026, 10, 1),
                    endDate = LocalDate.of(2026, 10, 31),
                ),
                rewardMultiplier = 1.3,
            ),
        )

        assertTrue(
            SeasonalEventManager.isCustomEventActive(
                eventKey = "LOCAL_FESTIVAL",
                date = LocalDate.of(2026, 10, 15),
            ),
        )
        assertFalse(
            SeasonalEventManager.isCustomEventActive(
                eventKey = "LOCAL_FESTIVAL",
                date = LocalDate.of(2026, 11, 1),
            ),
        )

        SeasonalEventManager.unregisterCustomEvent("LOCAL_FESTIVAL")
        assertFalse(
            SeasonalEventManager.isCustomEventActive(
                eventKey = "LOCAL_FESTIVAL",
                date = LocalDate.of(2026, 10, 15),
            ),
        )
    }

    @Test
    fun resolver_includesCustomEventsInContext() {
        SeasonalEventScheduleRegistry.register(
            SeasonalCustomEventDefinition(
                eventKey = "ANNIVERSARY_BONUS",
                displayName = "Anniversary Bonus",
                schedule = SeasonalCustomEventSchedule.Annual(
                    window = SeasonalEventWindow(6, 1, 6, 14),
                ),
                rewardMultiplier = 1.75,
                minExplorerLevel = 1,
            ),
        )

        val context = SeasonalEventResolver.resolve(LocalDate.of(2026, 6, 7), explorerLevel = 1)

        assertTrue(context.hasActiveEvents)
        assertTrue(context.activeCustomEvents.isNotEmpty())
        assertTrue(
            context.activeAchievementKeys.contains(
                "seasonal_custom_event:anniversary_bonus:first_participation",
            ),
        )
        assertEquals(200L, SeasonalEventResolver.scaleAmount(100L, context))
    }
}
