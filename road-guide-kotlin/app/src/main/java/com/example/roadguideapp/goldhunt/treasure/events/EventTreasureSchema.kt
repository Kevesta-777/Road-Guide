package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.events.SeasonalEventType

/**
 * Schema for seasonal event treasure generation (offline, deterministic).
 */
internal object EventTreasureSchema {
    const val VERSION = 1
    const val SLOTS_PER_L1 = 1
    const val MAX_FEATURES = 40

    val SUPPORTED_EVENT_TYPES: Set<SeasonalEventType> = setOf(
        SeasonalEventType.SPRING_BLOSSOM,
        SeasonalEventType.SUMMER_EXPLORER,
        SeasonalEventType.HALLOWEEN_MYSTERY,
        SeasonalEventType.WINTER_CRYSTAL,
        SeasonalEventType.NEW_YEAR_GOLDEN_HUNT,
    )

    object PlacementKinds {
        const val PREFIX = "EVENT"

        fun forType(type: SeasonalEventType): String =
            "${PREFIX}_${type.name}"
    }

    object SeedPrefixes {
        const val GENERATION = "event_treasure"

        fun forSlot(eventSeed: String, treasureId: String): String =
            "$GENERATION:v$VERSION:$eventSeed|$treasureId"
    }
}
