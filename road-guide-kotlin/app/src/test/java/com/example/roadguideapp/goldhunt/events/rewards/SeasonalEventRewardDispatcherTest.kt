package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventRewardDispatcherTest {
    private val eventSpec = TreasureSpec(
        treasureId = "tr:event:v1:r:-0.5000:51.4000:halloween_mystery:2026:L1:3:4:s0",
        type = TreasureType.CRYSTAL,
        lat = 51.45,
        lng = -0.2,
        creditAmount = 8,
        placementKind = "EVENT_HALLOWEEN_MYSTERY",
        regionL1Id = "L1:3:4",
    )

    @Test
    fun dispatch_inactiveDate_returnsNull() {
        val outcome = SeasonalEventRewardDispatcher.dispatchTreasureCollection(
            spec = eventSpec,
            explorerLevel = 10,
            evaluatedAt = LocalDate.of(2026, 2, 1),
        )

        assertNull(outcome)
    }

    @Test
    fun dispatch_activeEvent_buildsScaledBundle() {
        val outcome = SeasonalEventRewardDispatcher.dispatchTreasureCollection(
            spec = eventSpec,
            explorerLevel = 10,
            evaluatedAt = LocalDate.of(2026, 10, 20),
        )

        assertNotNull(outcome)
        requireNotNull(outcome)
        assertEquals(SeasonalEventType.HALLOWEEN_MYSTERY, outcome.event.eventType)
        assertTrue(outcome.credits >= eventSpec.creditAmount)
        assertTrue(outcome.xp > 0L)
        assertEquals(RewardRuleType.SEASONAL_EVENT_TREASURE, outcome.creditGrant.ruleType)
        assertEquals(
            "seasonal_event:halloween_mystery:first_participation",
            outcome.bundle.achievementKey,
        )
        assertEquals(
            "story_fragment_seasonal_event:halloween_mystery",
            outcome.bundle.storyFragmentKey,
        )
    }

    @Test
    fun dispatch_lowExplorerLevel_returnsNullWhenGated() {
        val outcome = SeasonalEventRewardDispatcher.dispatchTreasureCollection(
            spec = eventSpec,
            explorerLevel = 3,
            evaluatedAt = LocalDate.of(2026, 10, 20),
        )

        assertNull(outcome)
    }

    @Test
    fun dispatch_sameInputs_isDeterministic() {
        val first = SeasonalEventRewardDispatcher.dispatchTreasureCollection(
            spec = eventSpec,
            explorerLevel = 10,
            evaluatedAt = LocalDate.of(2026, 10, 20),
        )
        val second = SeasonalEventRewardDispatcher.dispatchTreasureCollection(
            spec = eventSpec,
            explorerLevel = 10,
            evaluatedAt = LocalDate.of(2026, 10, 20),
        )

        assertEquals(first, second)
    }
}
