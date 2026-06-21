package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema

internal object SeasonalEventRewards {
    fun treasureCollectionBundle(context: SeasonalEventRewardContext): SeasonalEventRewardBundle {
        val event = context.event
        val scaling = SeasonalEventRewardScaling.breakdown(context)
        val relicIncrement = SeasonalEventRewardScaling.legendaryRelicProgressIncrement(context.rarity)
        return SeasonalEventRewardBundle(
            credits = SeasonalEventRewardScaling.scaledCredits(context),
            xp = SeasonalEventRewardScaling.scaledXp(context),
            eventType = event.eventType,
            achievementKey = event.achievementKey,
            masteryAchievementKey = SeasonalEventSchema.AchievementKeys.mastery(event.eventType),
            storyFragmentKey = event.storyFragmentKey,
            legendaryRelicKey = event.legendaryRelicKey,
            legendaryRelicProgressIncrement = relicIncrement,
            recordStoryFragment = SeasonalEventRewardScaling.qualifiesForStoryFragment(context.rarity),
            recordLegendaryRelic = relicIncrement > 0,
            rewardMultiplier = scaling.combinedMultiplier,
        )
    }
}
