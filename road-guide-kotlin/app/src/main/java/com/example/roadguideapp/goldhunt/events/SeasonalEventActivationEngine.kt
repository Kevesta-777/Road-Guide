package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Offline activation engine: resolves active catalog and custom events from device date + schedule.
 */
internal object SeasonalEventActivationEngine {
    fun activate(
        evaluatedAt: LocalDate = SeasonalEventDeviceClock.today(),
        scheduleEntries: List<SeasonalEventScheduleEntry> = SeasonalEventScheduleRegistry.allEntries(),
    ): SeasonalEventActivationResult {
        val activeCatalog = mutableListOf<SeasonalEvent>()
        val activeCustom = mutableListOf<SeasonalCustomEvent>()

        for (entry in scheduleEntries) {
            when (entry) {
                is SeasonalEventScheduleEntry.Catalog -> {
                    val event = SeasonalEventFactory.fromType(entry.type, evaluatedAt)
                    if (event.active) {
                        activeCatalog += event
                    }
                }
                is SeasonalEventScheduleEntry.Custom -> {
                    if (!entry.definition.enabled) continue
                    val event = SeasonalCustomEventFactory.fromDefinition(
                        definition = entry.definition,
                        evaluatedAt = evaluatedAt,
                    )
                    if (event.active) {
                        activeCustom += event
                    }
                }
            }
        }

        if (activeCatalog.isEmpty() && activeCustom.isEmpty()) {
            return SeasonalEventActivationResult.INACTIVE.copy(evaluatedAt = evaluatedAt)
        }

        return SeasonalEventActivationResult(
            evaluatedAt = evaluatedAt,
            activeCatalogEvents = activeCatalog,
            activeCustomEvents = activeCustom,
        )
    }

    fun isCatalogEventActive(
        type: SeasonalEventType,
        evaluatedAt: LocalDate = SeasonalEventDeviceClock.today(),
    ): Boolean = SeasonalEventFactory.fromType(type, evaluatedAt).active

    fun isCustomEventActive(
        eventKey: String,
        evaluatedAt: LocalDate = SeasonalEventDeviceClock.today(),
    ): Boolean {
        val definition = SeasonalEventScheduleRegistry.customDefinition(eventKey) ?: return false
        return SeasonalCustomEventFactory.fromDefinition(definition, evaluatedAt).active
    }
}
