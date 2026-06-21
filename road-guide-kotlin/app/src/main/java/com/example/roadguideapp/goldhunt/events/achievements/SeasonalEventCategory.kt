package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEventType

/**
 * Groups seasonal events for achievement families and future catalog expansion.
 */
internal enum class SeasonalEventCategory(val displayName: String) {
    CORE_SEASONAL("Core Seasonal"),
    HOLIDAY("Holiday"),
    SPECIAL("Special"),
    CUSTOM("Custom"),
    ;

    companion object {
        val ALL_ORDERED: List<SeasonalEventCategory> = entries

        fun forType(type: SeasonalEventType): SeasonalEventCategory = when (type) {
            SeasonalEventType.SPRING_BLOSSOM,
            SeasonalEventType.SUMMER_EXPLORER,
            SeasonalEventType.AUTUMN_HARVEST,
            SeasonalEventType.WINTER_CRYSTAL,
            -> CORE_SEASONAL
            SeasonalEventType.HALLOWEEN_MYSTERY,
            SeasonalEventType.NEW_YEAR_GOLDEN_HUNT,
            -> HOLIDAY
            SeasonalEventType.ANNIVERSARY_EVENT,
            SeasonalEventType.COMMUNITY_CHALLENGE,
            -> SPECIAL
        }

        fun fromId(id: String): SeasonalEventCategory? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}
