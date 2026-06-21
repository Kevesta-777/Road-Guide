package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgressSchema

internal object SeasonalEventAchievementSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    const val BASE_COMPLETION_CREDITS = 50
    const val BASE_COMPLETION_XP = 120L

    fun masteryTarget(type: SeasonalEventType): Int =
        SeasonalEventProgressSchema.CompletionTargets.treasuresTarget(type)
            .coerceAtLeast(SeasonalEventProgressSchema.CompletionTargets.DEFAULT_TREASURES)

    fun categoryCollectorTarget(category: SeasonalEventCategory): Int =
        SeasonalEventAchievementCatalog.eventTypesInCategory(category).size.coerceAtLeast(1)
}
