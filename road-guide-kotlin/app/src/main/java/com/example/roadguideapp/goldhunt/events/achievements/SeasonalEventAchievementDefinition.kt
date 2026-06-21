package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal data class SeasonalEventAchievementDefinition(
    val key: String,
    val tier: SeasonalEventAchievementTier,
    val displayName: String,
    val targetCount: Int,
    val eventType: SeasonalEventType? = null,
    val category: SeasonalEventCategory? = null,
    val customEventKey: String? = null,
    val enabled: Boolean = true,
) {
    init {
        require(key.isNotBlank()) { "key must not be blank" }
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(targetCount >= 1) { "targetCount must be at least 1" }
    }

    fun isComplete(currentCount: Int): Boolean = currentCount >= targetCount

    fun progressFraction(currentCount: Int): Double =
        (currentCount.toDouble() / targetCount.toDouble()).coerceIn(0.0, 1.0)
}
