package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEventFactory
import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardSchema
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonalEventCompletionRewardDispatcherTest {
    @Test
    fun dispatch_buildsIdempotentCompletionGrant() {
        val event = SeasonalEventFactory.fromType(
            SeasonalEventType.NEW_YEAR_GOLDEN_HUNT,
            LocalDate.of(2026, 1, 2),
        )
        val outcome = SeasonalEventCompletionRewardDispatcher.dispatch(event)

        assertEquals(
            SeasonalEventSchema.AchievementKeys.completion(SeasonalEventType.NEW_YEAR_GOLDEN_HUNT),
            outcome.achievementKey,
        )
        assertEquals(RewardRuleType.SEASONAL_EVENT_COMPLETION, outcome.creditGrant.ruleType)
        assertEquals(
            SeasonalEventSchema.EventIds.rewardGrant(
                type = event.eventType,
                grantKey = SeasonalEventRewardSchema.GrantKeys.COMPLETION,
                year = event.cycleYear,
            ),
            outcome.creditGrant.eventId,
        )
        assertTrue(outcome.creditGrant.amount >= 1)
        assertTrue(outcome.xp >= 1L)
    }
}
