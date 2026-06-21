package com.example.roadguideapp.goldhunt.events.progress

import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal object SeasonalEventProgressSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    const val TREASURE_WEIGHT = 0.60
    const val FRAGMENT_WEIGHT = 0.40

    object CompletionTargets {
        const val DEFAULT_TREASURES = 25
        const val DEFAULT_FRAGMENTS = 1

        fun treasuresTarget(type: SeasonalEventType): Int = when (type) {
            SeasonalEventType.SPRING_BLOSSOM -> 20
            SeasonalEventType.SUMMER_EXPLORER -> 25
            SeasonalEventType.AUTUMN_HARVEST -> 25
            SeasonalEventType.HALLOWEEN_MYSTERY -> 15
            SeasonalEventType.WINTER_CRYSTAL -> 20
            SeasonalEventType.NEW_YEAR_GOLDEN_HUNT -> 10
            SeasonalEventType.ANNIVERSARY_EVENT -> 30
            SeasonalEventType.COMMUNITY_CHALLENGE -> 35
        }

        fun fragmentsTarget(type: SeasonalEventType): Int = DEFAULT_FRAGMENTS
    }
}
