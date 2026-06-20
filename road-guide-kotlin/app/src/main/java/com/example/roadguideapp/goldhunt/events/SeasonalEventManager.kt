package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Entry point for seasonal event state (offline, no UI).
 */
internal object SeasonalEventManager {
    fun activate(
        date: LocalDate = SeasonalEventDeviceClock.today(),
    ): SeasonalEventActivationResult = SeasonalEventActivationEngine.activate(date)

    fun currentContext(
        date: LocalDate = SeasonalEventDeviceClock.today(),
        explorerLevel: Int? = null,
    ): SeasonalEventContext = SeasonalEventResolver.resolve(date, explorerLevel)

    fun activeEvents(date: LocalDate = SeasonalEventDeviceClock.today()): List<SeasonalEvent> =
        SeasonalEventActivationEngine.activate(date).activeCatalogEvents

    fun activeCustomEvents(
        date: LocalDate = SeasonalEventDeviceClock.today(),
    ): List<SeasonalCustomEvent> =
        SeasonalEventActivationEngine.activate(date).activeCustomEvents

    fun registerCustomEvent(definition: SeasonalCustomEventDefinition) {
        SeasonalEventScheduleRegistry.register(definition)
    }

    fun unregisterCustomEvent(eventKey: String) {
        SeasonalEventScheduleRegistry.unregister(eventKey)
    }

    fun accessibleEvents(
        explorerLevel: Int,
        date: LocalDate = LocalDate.now(),
    ): List<SeasonalEvent> =
        SeasonalEventLevelGate.filterAccessible(activeEvents(date), explorerLevel)

    fun eventInstance(
        type: SeasonalEventType,
        date: LocalDate = LocalDate.now(),
    ): SeasonalEvent = SeasonalEventFactory.fromType(type, date)

    fun isEventActive(
        type: SeasonalEventType,
        date: LocalDate = SeasonalEventDeviceClock.today(),
    ): Boolean = SeasonalEventActivationEngine.isCatalogEventActive(type, date)

    fun isCustomEventActive(
        eventKey: String,
        date: LocalDate = SeasonalEventDeviceClock.today(),
    ): Boolean = SeasonalEventActivationEngine.isCustomEventActive(eventKey, date)

    fun isEventAccessible(
        type: SeasonalEventType,
        explorerLevel: Int,
        date: LocalDate = LocalDate.now(),
    ): Boolean {
        val event = eventInstance(type, date)
        return event.active && SeasonalEventLevelGate.isUnlocked(event, explorerLevel)
    }

    fun template(type: SeasonalEventType): SeasonalEventTemplate =
        SeasonalEventCatalog.templateFor(type)
}
