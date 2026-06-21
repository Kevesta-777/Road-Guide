package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.treasure.TreasureType

internal object EventTreasureProfileCatalog {
    private val profilesByType: Map<SeasonalEventType, EventTreasureProfile> =
        EventTreasureSchema.SUPPORTED_EVENT_TYPES.associateWith { type ->
            when (type) {
                SeasonalEventType.SPRING_BLOSSOM -> EventTreasureProfile(
                    eventType = type,
                    spawnChance = 0.12,
                    typeWeights = mapOf(
                        TreasureType.FLOWER to 70,
                        TreasureType.STAR to 20,
                        TreasureType.CRYSTAL to 10,
                    ),
                )
                SeasonalEventType.SUMMER_EXPLORER -> EventTreasureProfile(
                    eventType = type,
                    spawnChance = 0.10,
                    typeWeights = mapOf(
                        TreasureType.STAR to 35,
                        TreasureType.FLOWER to 35,
                        TreasureType.CRYSTAL to 30,
                    ),
                )
                SeasonalEventType.HALLOWEEN_MYSTERY -> EventTreasureProfile(
                    eventType = type,
                    spawnChance = 0.08,
                    typeWeights = mapOf(
                        TreasureType.CRYSTAL to 40,
                        TreasureType.GIFT to 30,
                        TreasureType.FLOWER to 20,
                        TreasureType.STAR to 10,
                    ),
                )
                SeasonalEventType.WINTER_CRYSTAL -> EventTreasureProfile(
                    eventType = type,
                    spawnChance = 0.10,
                    typeWeights = mapOf(
                        TreasureType.CRYSTAL to 60,
                        TreasureType.FLOWER to 25,
                        TreasureType.STAR to 15,
                    ),
                )
                SeasonalEventType.NEW_YEAR_GOLDEN_HUNT -> EventTreasureProfile(
                    eventType = type,
                    spawnChance = 0.06,
                    typeWeights = mapOf(
                        TreasureType.GIFT to 50,
                        TreasureType.CRYSTAL to 30,
                        TreasureType.FLOWER to 20,
                    ),
                )
                else -> error("Unsupported event type for event treasures: $type")
            }
        }

    fun profileFor(type: SeasonalEventType): EventTreasureProfile =
        profilesByType.getValue(type)

    fun isSupported(type: SeasonalEventType): Boolean =
        type in EventTreasureSchema.SUPPORTED_EVENT_TYPES
}
