package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEvent
import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardSchema
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType

internal data class SeasonalEventCompletionRewardOutcome(
    val event: SeasonalEvent,
    val achievementKey: String,
    val creditGrant: CreditGrant,
    val xp: Long,
)

internal object SeasonalEventCompletionRewardDispatcher {
    fun dispatch(event: SeasonalEvent): SeasonalEventCompletionRewardOutcome {
        val credits = SeasonalEventCompletionRewards.creditsFor(event)
        val xp = SeasonalEventCompletionRewards.xpFor(event)
        val achievementKey = SeasonalEventSchema.AchievementKeys.completion(event.eventType)
        return SeasonalEventCompletionRewardOutcome(
            event = event,
            achievementKey = achievementKey,
            creditGrant = CreditGrant(
                eventId = SeasonalEventSchema.EventIds.rewardGrant(
                    type = event.eventType,
                    grantKey = SeasonalEventRewardSchema.GrantKeys.COMPLETION,
                    year = event.cycleYear,
                ),
                ruleType = RewardRuleType.SEASONAL_EVENT_COMPLETION,
                amount = credits,
                label = event.eventId,
            ),
            xp = xp,
        )
    }
}
