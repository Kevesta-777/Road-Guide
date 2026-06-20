package com.example.roadguideapp.goldhunt.events

/**
 * Canonical live seasonal event families (offline catalog).
 *
 * Each event repeats annually using [startMonth]/[startDay] through [endMonth]/[endDay].
 * [eventMultiplier] scales rewards while the window is active.
 *
 * Achievement, story-fragment, and legendary-relic hook keys are resolved in [SeasonalEventSchema].
 */
internal enum class SeasonalEventType(
    val displayName: String,
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int,
    val eventMultiplier: Double,
    val minExplorerLevel: Int,
) {
    SPRING_BLOSSOM(
        displayName = "Spring Blossom",
        startMonth = 3,
        startDay = 20,
        endMonth = 5,
        endDay = 31,
        eventMultiplier = 1.15,
        minExplorerLevel = 1,
    ),
    SUMMER_EXPLORER(
        displayName = "Summer Explorer",
        startMonth = 6,
        startDay = 1,
        endMonth = 8,
        endDay = 31,
        eventMultiplier = 1.20,
        minExplorerLevel = 3,
    ),
    AUTUMN_HARVEST(
        displayName = "Autumn Harvest",
        startMonth = 9,
        startDay = 1,
        endMonth = 11,
        endDay = 30,
        eventMultiplier = 1.25,
        minExplorerLevel = 5,
    ),
    HALLOWEEN_MYSTERY(
        displayName = "Halloween Mystery",
        startMonth = 10,
        startDay = 15,
        endMonth = 11,
        endDay = 5,
        eventMultiplier = 1.35,
        minExplorerLevel = 7,
    ),
    WINTER_CRYSTAL(
        displayName = "Winter Crystal",
        startMonth = 12,
        startDay = 1,
        endMonth = 1,
        endDay = 15,
        eventMultiplier = 1.30,
        minExplorerLevel = 8,
    ),
    NEW_YEAR_GOLDEN_HUNT(
        displayName = "New Year Golden Hunt",
        startMonth = 12,
        startDay = 28,
        endMonth = 1,
        endDay = 7,
        eventMultiplier = 1.50,
        minExplorerLevel = 5,
    ),
    ANNIVERSARY_EVENT(
        displayName = "Anniversary Event",
        startMonth = 6,
        startDay = 1,
        endMonth = 6,
        endDay = 14,
        eventMultiplier = 2.0,
        minExplorerLevel = 1,
    ),
    COMMUNITY_CHALLENGE(
        displayName = "Community Challenge",
        startMonth = 7,
        startDay = 1,
        endMonth = 7,
        endDay = 31,
        eventMultiplier = 1.40,
        minExplorerLevel = 10,
    ),
    ;

    val id: String get() = name

    companion object {
        val ALL_ORDERED: List<SeasonalEventType> = entries

        fun fromId(id: String): SeasonalEventType? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }

    init {
        require(displayName.isNotBlank()) { "$name displayName must not be blank" }
        require(startMonth in 1..12) { "$name startMonth must be 1..12" }
        require(endMonth in 1..12) { "$name endMonth must be 1..12" }
        require(startDay in 1..daysInMonth(startMonth)) { "$name startDay invalid for month" }
        require(endDay in 1..daysInMonth(endMonth)) { "$name endDay invalid for month" }
        require(eventMultiplier >= 1.0) { "$name eventMultiplier must be >= 1.0" }
        require(minExplorerLevel >= 1) { "$name minExplorerLevel must be at least 1" }
    }
}

private fun daysInMonth(month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> 28
    else -> 30
}
