package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Builds [SeasonalEvent] instances from catalog types and evaluation dates.
 */
internal object SeasonalEventFactory {
    fun fromType(
        type: SeasonalEventType,
        evaluatedAt: LocalDate = LocalDate.now(),
        cycleYear: Int = SeasonalEventCycle.resolveCycleYear(type, evaluatedAt),
    ): SeasonalEvent {
        val (startDate, endDate) = SeasonalEventCycle.windowDates(type, cycleYear)
        val event = SeasonalEvent(
            eventId = SeasonalEventSchema.EventIds.instance(type, cycleYear),
            eventType = type,
            active = false,
            startDate = startDate,
            endDate = endDate,
            rewardMultiplier = type.eventMultiplier,
            eventSeed = SeasonalEventSchema.SeedPrefixes.forInstance(type, cycleYear),
            cycleYear = cycleYear,
            minExplorerLevel = type.minExplorerLevel,
            achievementKey = SeasonalEventSchema.AchievementKeys.firstParticipation(type),
            storyFragmentKey = SeasonalEventSchema.StoryFragmentKeys.forType(type),
            legendaryRelicKey = SeasonalEventSchema.LegendaryRelicKeys.forType(type),
        )
        return event.withActiveFlag(evaluatedAt)
    }

    fun fromTemplate(
        template: SeasonalEventTemplate,
        evaluatedAt: LocalDate = LocalDate.now(),
        cycleYear: Int = SeasonalEventCycle.resolveCycleYear(template.type, evaluatedAt),
    ): SeasonalEvent = fromType(template.type, evaluatedAt, cycleYear)

    fun allInstances(evaluatedAt: LocalDate = LocalDate.now()): List<SeasonalEvent> =
        SeasonalEventCatalog.displayOrder.map { fromType(it, evaluatedAt) }

    fun activeInstances(evaluatedAt: LocalDate = LocalDate.now()): List<SeasonalEvent> =
        allInstances(evaluatedAt).filter { it.active }
}
