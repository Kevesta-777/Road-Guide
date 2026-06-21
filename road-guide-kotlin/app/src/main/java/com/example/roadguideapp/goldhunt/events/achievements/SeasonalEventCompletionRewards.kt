package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEvent

internal object SeasonalEventCompletionRewards {
    fun creditsFor(event: SeasonalEvent): Int {
        val categoryMultiplier = when (SeasonalEventCategory.forType(event.eventType)) {
            SeasonalEventCategory.CORE_SEASONAL -> 1.0
            SeasonalEventCategory.HOLIDAY -> 1.15
            SeasonalEventCategory.SPECIAL -> 1.25
            SeasonalEventCategory.CUSTOM -> 1.10
        }
        return (SeasonalEventAchievementSchema.BASE_COMPLETION_CREDITS *
            event.rewardMultiplier *
            categoryMultiplier)
            .toInt()
            .coerceAtLeast(1)
    }

    fun xpFor(event: SeasonalEvent): Long {
        val categoryMultiplier = when (SeasonalEventCategory.forType(event.eventType)) {
            SeasonalEventCategory.CORE_SEASONAL -> 1.0
            SeasonalEventCategory.HOLIDAY -> 1.15
            SeasonalEventCategory.SPECIAL -> 1.30
            SeasonalEventCategory.CUSTOM -> 1.10
        }
        return (SeasonalEventAchievementSchema.BASE_COMPLETION_XP *
            event.rewardMultiplier *
            categoryMultiplier)
            .toLong()
            .coerceAtLeast(1L)
    }
}
