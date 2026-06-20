package com.example.roadguideapp.goldhunt.events.progress

import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal object SeasonalEventProgressCalculator {
    fun completionPercent(
        eventType: SeasonalEventType,
        treasuresCollected: Int,
        fragmentsEarned: Int,
    ): Double {
        val treasureTarget = SeasonalEventProgressSchema.CompletionTargets
            .treasuresTarget(eventType)
            .coerceAtLeast(1)
        val fragmentTarget = SeasonalEventProgressSchema.CompletionTargets
            .fragmentsTarget(eventType)
            .coerceAtLeast(1)
        val treasurePart = (treasuresCollected.toDouble() / treasureTarget)
            .coerceIn(0.0, 1.0)
        val fragmentPart = (fragmentsEarned.toDouble() / fragmentTarget)
            .coerceIn(0.0, 1.0)
        val weighted = treasurePart * SeasonalEventProgressSchema.TREASURE_WEIGHT +
            fragmentPart * SeasonalEventProgressSchema.FRAGMENT_WEIGHT
        return (weighted * 100.0).coerceIn(0.0, 100.0)
    }

    fun isCompleted(completionPercent: Double): Boolean =
        completionPercent >= 100.0
}
