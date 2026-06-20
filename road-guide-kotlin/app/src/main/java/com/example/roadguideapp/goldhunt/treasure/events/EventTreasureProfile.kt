package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.treasure.TreasureType

/**
 * Per-event treasure spawn profile (type bias, spawn chance, credit scaling).
 */
internal data class EventTreasureProfile(
    val eventType: SeasonalEventType,
    val spawnChance: Double,
    val typeWeights: Map<TreasureType, Int>,
    val creditMultiplier: Double = eventType.eventMultiplier,
) {
    val placementKind: String
        get() = EventTreasureSchema.PlacementKinds.forType(eventType)

    init {
        require(spawnChance in 0.0..1.0) { "spawnChance must be 0..1" }
        require(typeWeights.isNotEmpty()) { "typeWeights must not be empty" }
        require(typeWeights.values.all { it > 0 }) { "typeWeights must be positive" }
        require(creditMultiplier >= 1.0) { "creditMultiplier must be >= 1.0" }
    }
}
