package com.example.roadguideapp.goldhunt.events

/**
 * One schedulable seasonal event source (catalog enum or custom definition).
 */
internal sealed class SeasonalEventScheduleEntry {
    abstract val scheduleKey: String

    data class Catalog(
        val type: SeasonalEventType,
    ) : SeasonalEventScheduleEntry() {
        override val scheduleKey: String
            get() = type.id
    }

    data class Custom(
        val definition: SeasonalCustomEventDefinition,
    ) : SeasonalEventScheduleEntry() {
        override val scheduleKey: String
            get() = definition.eventKey
    }
}
