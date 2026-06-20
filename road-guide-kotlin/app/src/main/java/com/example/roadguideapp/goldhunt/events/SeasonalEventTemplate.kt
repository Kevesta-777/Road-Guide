package com.example.roadguideapp.goldhunt.events

/**
 * Read-only catalog template for one [SeasonalEventType] and reserved hook keys.
 */
internal data class SeasonalEventTemplate(
    val type: SeasonalEventType,
    val displayName: String,
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int,
    val eventMultiplier: Double,
    val minExplorerLevel: Int,
    val achievementKey: String,
    val storyFragmentKey: String,
    val legendaryRelicKey: String,
) {
    val id: String get() = type.id

    companion object {
        fun from(type: SeasonalEventType): SeasonalEventTemplate = SeasonalEventTemplate(
            type = type,
            displayName = type.displayName,
            startMonth = type.startMonth,
            startDay = type.startDay,
            endMonth = type.endMonth,
            endDay = type.endDay,
            eventMultiplier = type.eventMultiplier,
            minExplorerLevel = type.minExplorerLevel,
            achievementKey = SeasonalEventSchema.AchievementKeys.firstParticipation(type),
            storyFragmentKey = SeasonalEventSchema.StoryFragmentKeys.forType(type),
            legendaryRelicKey = SeasonalEventSchema.LegendaryRelicKeys.forType(type),
        )
    }
}
