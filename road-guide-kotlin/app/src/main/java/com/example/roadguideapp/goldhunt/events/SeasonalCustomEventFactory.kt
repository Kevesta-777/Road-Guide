package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

internal object SeasonalCustomEventFactory {
    fun fromDefinition(
        definition: SeasonalCustomEventDefinition,
        evaluatedAt: LocalDate,
    ): SeasonalCustomEvent {
        val (startDate, endDate) = definition.schedule.windowDates(evaluatedAt)
        val eventId = eventIdFor(definition, startDate)
        val eventSeed = eventSeedFor(definition, startDate)
        val event = SeasonalCustomEvent(
            eventId = eventId,
            eventKey = definition.eventKey,
            displayName = definition.displayName,
            active = false,
            startDate = startDate,
            endDate = endDate,
            rewardMultiplier = definition.rewardMultiplier,
            eventSeed = eventSeed,
            minExplorerLevel = definition.minExplorerLevel,
            achievementKey = definition.resolvedAchievementKey,
            storyFragmentKey = definition.resolvedStoryFragmentKey,
            legendaryRelicKey = definition.resolvedLegendaryRelicKey,
            extensionJson = definition.extensionJson,
        )
        return event.withActiveFlag(evaluatedAt)
    }

    fun allInstances(
        definitions: List<SeasonalCustomEventDefinition>,
        evaluatedAt: LocalDate,
    ): List<SeasonalCustomEvent> =
        definitions.map { fromDefinition(it, evaluatedAt) }

    fun activeInstances(
        definitions: List<SeasonalCustomEventDefinition>,
        evaluatedAt: LocalDate,
    ): List<SeasonalCustomEvent> =
        allInstances(definitions, evaluatedAt).filter { it.active }

    private fun eventIdFor(
        definition: SeasonalCustomEventDefinition,
        startDate: LocalDate,
    ): String = when (definition.schedule) {
        is SeasonalCustomEventSchedule.Annual -> {
            val cycleYear = SeasonalEventCycle.resolveCycleYear(
                definition.schedule.window,
                startDate,
            )
            SeasonalEventSchema.CustomEventIds.annualInstance(definition.eventKey, cycleYear)
        }
        is SeasonalCustomEventSchedule.FixedRange ->
            SeasonalEventSchema.CustomEventIds.fixedInstance(
                definition.eventKey,
                startDate.toEpochDay(),
            )
    }

    private fun eventSeedFor(
        definition: SeasonalCustomEventDefinition,
        startDate: LocalDate,
    ): String = when (definition.schedule) {
        is SeasonalCustomEventSchedule.Annual -> {
            val cycleYear = SeasonalEventCycle.resolveCycleYear(
                definition.schedule.window,
                startDate,
            )
            SeasonalEventSchema.CustomSeedPrefixes.annual(definition.eventKey, cycleYear)
        }
        is SeasonalCustomEventSchedule.FixedRange ->
            SeasonalEventSchema.CustomSeedPrefixes.fixed(
                definition.eventKey,
                startDate.toEpochDay(),
            )
    }
}
