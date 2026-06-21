package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Runtime snapshot of active seasonal events for reward and progression hooks.
 */
internal data class SeasonalEventContext(
    val evaluatedAt: LocalDate,
    val activeEvents: List<SeasonalEvent>,
    val activeCustomEvents: List<SeasonalCustomEvent> = emptyList(),
    val combinedMultiplier: Double,
    val primaryEvent: SeasonalEvent?,
    val primaryCustomEvent: SeasonalCustomEvent? = null,
    val explorerLevel: Int? = null,
) {
    val hasActiveEvents: Boolean
        get() = activeEvents.isNotEmpty() || activeCustomEvents.isNotEmpty()

    val accessibleEvents: List<SeasonalEvent>
        get() = if (explorerLevel == null) {
            activeEvents
        } else {
            SeasonalEventLevelGate.filterAccessible(activeEvents, explorerLevel)
        }

    val accessibleCustomEvents: List<SeasonalCustomEvent>
        get() = if (explorerLevel == null) {
            activeCustomEvents
        } else {
            SeasonalEventLevelGate.filterAccessibleCustom(activeCustomEvents, explorerLevel)
        }

    val activeAchievementKeys: List<String>
        get() = accessibleEvents.map { it.achievementKey } +
            accessibleCustomEvents.map { it.achievementKey }

    val activeStoryFragmentKeys: List<String>
        get() = accessibleEvents.map { it.storyFragmentKey } +
            accessibleCustomEvents.map { it.storyFragmentKey }

    val activeLegendaryRelicKeys: List<String>
        get() = accessibleEvents.map { it.legendaryRelicKey } +
            accessibleCustomEvents.map { it.legendaryRelicKey }

    val activeEventIds: List<String>
        get() = accessibleEvents.map { it.eventId } +
            accessibleCustomEvents.map { it.eventId }

    companion object {
        val INACTIVE = SeasonalEventContext(
            evaluatedAt = LocalDate.EPOCH,
            activeEvents = emptyList(),
            activeCustomEvents = emptyList(),
            combinedMultiplier = SeasonalEventSchema.BASE_MULTIPLIER,
            primaryEvent = null,
            primaryCustomEvent = null,
            explorerLevel = null,
        )
    }
}
