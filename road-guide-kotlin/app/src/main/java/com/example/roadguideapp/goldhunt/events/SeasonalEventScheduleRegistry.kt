package com.example.roadguideapp.goldhunt.events

/**
 * Offline schedule registry: built-in catalog plus runtime custom definitions.
 */
internal object SeasonalEventScheduleRegistry {
    private val customByKey = linkedMapOf<String, SeasonalCustomEventDefinition>()

    val catalogEntries: List<SeasonalEventScheduleEntry.Catalog> =
        SeasonalEventCatalog.displayOrder.map { SeasonalEventScheduleEntry.Catalog(it) }

    val customDefinitions: List<SeasonalCustomEventDefinition>
        get() = customByKey.values.filter { it.enabled }

    val customEntries: List<SeasonalEventScheduleEntry.Custom>
        get() = customDefinitions.map { SeasonalEventScheduleEntry.Custom(it) }

    fun allEntries(): List<SeasonalEventScheduleEntry> =
        catalogEntries + customEntries

    fun register(definition: SeasonalCustomEventDefinition) {
        customByKey[definition.eventKey] = definition
    }

    fun registerAll(definitions: Collection<SeasonalCustomEventDefinition>) {
        definitions.forEach { register(it) }
    }

    fun unregister(eventKey: String) {
        customByKey.remove(eventKey)
    }

    fun clearCustom() {
        customByKey.clear()
    }

    fun customDefinition(eventKey: String): SeasonalCustomEventDefinition? =
        customByKey[eventKey]
}
